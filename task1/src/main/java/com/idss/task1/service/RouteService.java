package com.idss.task1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.idss.common.config.Canonical;
import com.idss.common.util.JsonLoader;
import com.idss.task1.algorithm.AStarEngine;
import com.idss.task1.algorithm.BuildingGraph;
import com.idss.task1.model.DeliveryRoute;
import com.idss.task1.model.DispatchOrder;
import com.idss.task1.model.Edge3D;
import com.idss.task1.model.Node3D;
import com.idss.task1.model.RouteSearchResult;
import com.idss.task1.model.TurnByTurnStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Core Service Orchestrator for Task 1: Secure Exam Paper Logistics & Indoor Routing.
 * (Student A Deliverable - Step 4).
 *
 * <p>Orchestrates spatial graph loading, dispatch order ingestion, A* multi-floor shortest path
 * calculation, turn-by-turn manifest generation, constraint validation (step-free & time limits),
 * and output delivery routes serialization.</p>
 */
@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);
    private static final String DEFAULT_SOURCE_VAULT = "VAULT_G01";

    private final AStarEngine aStarEngine;
    private final ManifestGenerator manifestGenerator;
    private final String buildingGraphPath;
    private final String dispatchOrdersPath;
    private final String outputRoutesPath;

    private BuildingGraph buildingGraph;

    public RouteService(
            @Value("${idss.task1.building-graph-path:data/input/input_building_graph.json}") String buildingGraphPath,
            @Value("${idss.task1.dispatch-orders-path:data/input/input_dispatch_orders.json}") String dispatchOrdersPath,
            @Value("${idss.task1.output-routes-path:data/shared/output_delivery_routes.json}") String outputRoutesPath) {
        this.aStarEngine = new AStarEngine();
        this.manifestGenerator = new ManifestGenerator();
        this.buildingGraphPath = buildingGraphPath;
        this.dispatchOrdersPath = dispatchOrdersPath;
        this.outputRoutesPath = outputRoutesPath;
        initializeGraph();
    }

    /**
     * Initializes or reloads the 3D Building Graph from JSON.
     */
    public synchronized void initializeGraph() {
        try {
            this.buildingGraph = BuildingGraph.fromFile(buildingGraphPath);
            log.info("Successfully loaded BuildingGraph with {} nodes from '{}'",
                    buildingGraph.getNodeCount(), buildingGraphPath);
        } catch (Exception e) {
            log.warn("Could not load BuildingGraph from '{}' (falling back to empty graph): {}",
                    buildingGraphPath, e.getMessage());
            this.buildingGraph = new BuildingGraph();
        }
    }

    public BuildingGraph getBuildingGraph() {
        return buildingGraph;
    }

    /**
     * Processes dispatch orders (from argument or default input file), calculates optimal
     * routes, validates hard constraints, writes output JSON, and returns the result wrapper.
     */
    public Map<String, Object> processDispatches(List<DispatchOrder> orders) {
        List<DispatchOrder> dispatchList = orders;
        if (dispatchList == null || dispatchList.isEmpty()) {
            dispatchList = loadDefaultDispatchOrders();
        }

        List<DeliveryRoute> routes = new ArrayList<>();
        int successful = 0;
        int failed = 0;

        for (DispatchOrder order : dispatchList) {
            DeliveryRoute route = calculateRoute(order);
            routes.add(route);
            if (route.getHardConstraintViolations() == 0 && route.isWithinTimeLimit()) {
                successful++;
            } else {
                failed++;
            }
        }

        String status = (failed == 0) ? "OPTIMAL" : (successful > 0 ? "FEASIBLE" : "INFEASIBLE");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generation_timestamp", Instant.now().toString());
        response.put("status", status);
        response.put("total_dispatches", routes.size());
        response.put("successful_routes", successful);
        response.put("failed_routes", failed);
        response.put("routes", routes);

        saveOutputRoutes(response);
        return response;
    }

    /**
     * Calculates the optimal route and turn-by-turn manifest for a single dispatch order.
     */
    public DeliveryRoute calculateRoute(DispatchOrder order) {
        String sourceVault = (order.getSourceVaultId() != null && !order.getSourceVaultId().isBlank())
                ? order.getSourceVaultId()
                : DEFAULT_SOURCE_VAULT;

        String rawDest = order.getDestinationRoomId();
        String destinationRoom = resolveRoomNodeId(rawDest);

        boolean stepFreeRequired = order.isRequiresStepFreeAccess();
        RouteSearchResult searchResult = aStarEngine.findShortestPath(
                buildingGraph, sourceVault, destinationRoom, stepFreeRequired
        );

        DeliveryRoute route = new DeliveryRoute();
        route.setDispatchId(order.getDispatchId());
        route.setExamId(order.getExamId());
        route.setCourseCode(order.getCourseCode());
        route.setSourceVault(sourceVault);
        route.setDestinationRoom(destinationRoom);
        route.setTargetFloor(order.getDestinationFloor());
        route.setRequiresStepFreeAccess(stepFreeRequired);

        if (searchResult.isPathFound()) {
            route.setTotalDistanceMeters(searchResult.getTotalDistanceMeters());
            route.setEstimatedTransitTimeSeconds(searchResult.getTotalTransitTimeSeconds());
            route.setPathSequence(searchResult.getPath());
            route.setNodesInPathCount(searchResult.getPath().size());

            List<TurnByTurnStep> manifest = manifestGenerator.generate(buildingGraph, searchResult.getPath());
            route.setTurnByTurnManifest(manifest);

            boolean isStepFree = verifyStepFree(searchResult.getPath());
            route.setStepFreeVerified(isStepFree);

            boolean withinTime = (order.getMaxAllowedTransitSeconds() <= 0)
                    || (searchResult.getTotalTransitTimeSeconds() <= order.getMaxAllowedTransitSeconds());
            route.setWithinTimeLimit(withinTime);

            int violations = 0;
            if (stepFreeRequired && !isStepFree) {
                violations++;
            }
            if (!withinTime) {
                violations++;
            }
            route.setHardConstraintViolations(violations);
        } else {
            route.setTotalDistanceMeters(0.0);
            route.setEstimatedTransitTimeSeconds(0);
            route.setPathSequence(Collections.emptyList());
            route.setTurnByTurnManifest(Collections.emptyList());
            route.setStepFreeVerified(false);
            route.setWithinTimeLimit(false);
            route.setHardConstraintViolations(1);
        }

        return route;
    }

    /**
     * Resolves a room identifier to its graph node key (handling aliases like R101 -> ROOM_R101).
     */
    public String resolveRoomNodeId(String roomId) {
        if (roomId == null) {
            return null;
        }
        if (buildingGraph.containsNode(roomId)) {
            return roomId;
        }
        String aliased = Canonical.toAliasedRoomId(roomId);
        if (buildingGraph.containsNode(aliased)) {
            return aliased;
        }
        return roomId;
    }

    /**
     * Verifies whether every edge along the path is step-free.
     */
    private boolean verifyStepFree(List<String> path) {
        if (path == null || path.size() < 2) {
            return true;
        }
        for (int i = 0; i < path.size() - 1; i++) {
            String u = path.get(i);
            String v = path.get(i + 1);
            boolean edgeFound = false;
            for (Edge3D edge : buildingGraph.getOutgoingEdges(u)) {
                if (v.equals(edge.getTargetNode())) {
                    edgeFound = true;
                    if (!edge.isStepFree()) {
                        return false;
                    }
                    break;
                }
            }
            if (!edgeFound) {
                return false;
            }
        }
        return true;
    }

    private List<DispatchOrder> loadDefaultDispatchOrders() {
        try {
            File file = JsonLoader.resolve(dispatchOrdersPath);
            if (file.exists()) {
                return JsonLoader.loadList(dispatchOrdersPath, DispatchOrder.class);
            }
        } catch (Exception e) {
            log.warn("Could not load dispatch orders from '{}': {}", dispatchOrdersPath, e.getMessage());
        }
        return Collections.emptyList();
    }

    private void saveOutputRoutes(Map<String, Object> payload) {
        try {
            File target = JsonLoader.resolve(outputRoutesPath);
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            ObjectMapper mapper = JsonLoader.mapper().copy().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(target, payload);
            log.info("Saved {} delivery routes to '{}'", payload.get("total_dispatches"), target.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write delivery routes to '{}': {}", outputRoutesPath, e.getMessage());
        }
    }
}
