package com.idss.task1;

import com.idss.task1.model.DeliveryRoute;
import com.idss.task1.model.DispatchOrder;
import com.idss.task1.model.TurnByTurnStep;
import com.idss.task1.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for RouteService (Student A Deliverable - Subtask 4.3).
 */
public class RouteServiceTest {

    private RouteService routeService;

    @BeforeEach
    public void setUp() {
        routeService = new RouteService(
                "data/input/input_building_graph.json",
                "data/input/input_dispatch_orders.json",
                "data/shared/output_delivery_routes.json"
        );
    }

    @Test
    @DisplayName("Should initialize graph with non-zero node count")
    public void testGraphInitialization() {
        assertNotNull(routeService.getBuildingGraph());
        assertTrue(routeService.getBuildingGraph().getVertexCount() >= 10);
    }

    @Test
    @DisplayName("Should resolve canonical room names seamlessly")
    public void testRoomIdResolution() {
        assertEquals("ROOM_R101", routeService.resolveRoomNodeId("R101"));
        assertEquals("ROOM_R101", routeService.resolveRoomNodeId("ROOM_R101"));
        assertEquals("ROOM_LAB3A", routeService.resolveRoomNodeId("ROOM_LAB3A"));
    }

    @Test
    @DisplayName("Should process default dispatch orders and write output JSON")
    public void testProcessDefaultDispatches() {
        Map<String, Object> result = routeService.processDispatches(null);

        assertNotNull(result);
        assertEquals("OPTIMAL", result.get("status"));
        assertTrue((int) result.get("total_dispatches") >= 2);
        assertTrue((int) result.get("successful_routes") >= 2);
        assertEquals(0, result.get("failed_routes"));

        @SuppressWarnings("unchecked")
        List<DeliveryRoute> routes = (List<DeliveryRoute>) result.get("routes");
        assertNotNull(routes);
        assertFalse(routes.isEmpty());

        File outputFile = new File("data/shared/output_delivery_routes.json");
        assertTrue(outputFile.exists(), "output_delivery_routes.json should be written to disk");
    }

    @Test
    @DisplayName("Should enforce step-free path and use elevator for accessible dispatch")
    public void testAccessibleDispatchOrder() {
        DispatchOrder order = new DispatchOrder();
        order.setDispatchId("DSP_TEST_ACC");
        order.setExamId("EX_TEST");
        order.setCourseCode("PDSA201");
        order.setSourceVaultId("VAULT_G01");
        order.setDestinationRoomId("R101");
        order.setDestinationFloor(1);
        order.setRequiresStepFreeAccess(true);
        order.setMaxAllowedTransitSeconds(300);

        DeliveryRoute route = routeService.calculateRoute(order);

        assertNotNull(route);
        assertEquals("DSP_TEST_ACC", route.getDispatchId());
        assertEquals("ROOM_R101", route.getDestinationRoom());
        assertTrue(route.isRequiresStepFreeAccess());
        assertTrue(route.isStepFreeVerified());
        assertTrue(route.isWithinTimeLimit());
        assertEquals(0, route.getHardConstraintViolations());

        // Path should use elevator rather than stairs
        assertTrue(route.getPathSequence().contains("ELEV_G"));
        assertTrue(route.getPathSequence().contains("ELEV_F1"));
        assertFalse(route.getPathSequence().contains("STAIR_G"));

        // Manifest check
        List<TurnByTurnStep> manifest = route.getTurnByTurnManifest();
        assertNotNull(manifest);
        assertFalse(manifest.isEmpty());
        assertTrue(manifest.stream().anyMatch(s -> s.getAction().contains("Elevator")));
    }

    @Test
    @DisplayName("Should use faster stair route when step-free access is not required")
    public void testNonAccessibleDispatchOrder() {
        DispatchOrder order = new DispatchOrder();
        order.setDispatchId("DSP_TEST_STAIRS");
        order.setExamId("EX_TEST_2");
        order.setCourseCode("NET102");
        order.setSourceVaultId("VAULT_G01");
        order.setDestinationRoomId("ROOM_LAB3A");
        order.setDestinationFloor(3);
        order.setRequiresStepFreeAccess(false);
        order.setMaxAllowedTransitSeconds(420);

        DeliveryRoute route = routeService.calculateRoute(order);

        assertNotNull(route);
        assertEquals("DSP_TEST_STAIRS", route.getDispatchId());
        assertFalse(route.isRequiresStepFreeAccess());
        assertTrue(route.isWithinTimeLimit());
        assertEquals(0, route.getHardConstraintViolations());

        // Path should start at vault and reach LAB3A within time limit
        assertEquals("VAULT_G01", route.getPathSequence().get(0));
        assertEquals("ROOM_LAB3A", route.getPathSequence().get(route.getPathSequence().size() - 1));
        assertTrue(route.getEstimatedTransitTimeSeconds() > 0);
        assertTrue(route.getTotalDistanceMeters() > 0);
    }

    @Test
    @DisplayName("Should flag unreachable rooms as constraint violations")
    public void testUnreachableDestination() {
        DispatchOrder order = new DispatchOrder();
        order.setDispatchId("DSP_TEST_INVALID");
        order.setExamId("EX_INVALID");
        order.setSourceVaultId("VAULT_G01");
        order.setDestinationRoomId("NON_EXISTENT_ROOM_999");

        DeliveryRoute route = routeService.calculateRoute(order);

        assertNotNull(route);
        assertFalse(route.isWithinTimeLimit());
        assertEquals(1, route.getHardConstraintViolations());
        assertTrue(route.getPathSequence().isEmpty());
    }
}
