package com.idss.task1;

import com.idss.task1.controller.RouteController;
import com.idss.task1.model.DeliveryRoute;
import com.idss.task1.repository.DeliveryRouteRepository;
import com.idss.task1.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc tests for RouteController (Student B Deliverable - Subtask 4.6).
 *
 * <p>{@link RouteService} and {@link DeliveryRouteRepository} are mocked via
 * {@code @WebMvcTest}, which only loads the web layer — no real MongoDB connection or
 * building-graph JSON is needed to exercise these endpoints.</p>
 */
@WebMvcTest(RouteController.class)
@TestPropertySource(properties = "idss.task1.output-metrics-path=data/shared/__nonexistent_test_metrics__.json")
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteService routeService;

    @MockBean
    private DeliveryRouteRepository deliveryRouteRepository;

    private DeliveryRoute sampleRoute;

    @BeforeEach
    void setUp() {
        sampleRoute = new DeliveryRoute();
        sampleRoute.setDispatchId("DSP_001");
        sampleRoute.setExamId("EX_101");
        sampleRoute.setDestinationRoom("ROOM_R101");
        sampleRoute.setTotalDistanceMeters(44.0);
        sampleRoute.setEstimatedTransitTimeSeconds(58);
        sampleRoute.setWithinTimeLimit(true);
        sampleRoute.setHardConstraintViolations(0);
        sampleRoute.setPathSequence(List.of("VAULT_G01", "HALLWAY_G_EAST", "ROOM_R101"));
    }

    @Test
    @DisplayName("POST /api/task1/route should return 200 OPTIMAL and persist routes when all dispatches succeed")
    void testComputeRoutesOptimal() throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "OPTIMAL");
        response.put("total_dispatches", 1);
        response.put("successful_routes", 1);
        response.put("failed_routes", 0);
        response.put("routes", List.of(sampleRoute));

        when(routeService.processDispatches(any())).thenReturn(response);

        mockMvc.perform(post("/api/task1/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPTIMAL"))
                .andExpect(jsonPath("$.routes[0].dispatch_id").value("DSP_001"));

        verify(deliveryRouteRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("POST /api/task1/route should return 422 when routes violate hard constraints")
    void testComputeRoutesInfeasibleReturns422() throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "INFEASIBLE");
        response.put("total_dispatches", 1);
        response.put("successful_routes", 0);
        response.put("failed_routes", 1);
        response.put("routes", List.of());

        when(routeService.processDispatches(any())).thenReturn(response);

        mockMvc.perform(post("/api/task1/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("INFEASIBLE"));
    }

    @Test
    @DisplayName("POST /api/task1/route should still return computed routes even if MongoDB persistence fails")
    void testComputeRoutesToleratesPersistenceFailure() throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "OPTIMAL");
        response.put("routes", List.of(sampleRoute));

        when(routeService.processDispatches(any())).thenReturn(response);
        when(deliveryRouteRepository.saveAll(anyList())).thenThrow(new RuntimeException("Mongo unreachable"));

        mockMvc.perform(post("/api/task1/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPTIMAL"));
    }

    @Test
    @DisplayName("GET /api/task1/routes/{dispatchId} should return 200 with the route when found")
    void testGetRouteFound() throws Exception {
        when(deliveryRouteRepository.findById("DSP_001")).thenReturn(Optional.of(sampleRoute));

        mockMvc.perform(get("/api/task1/routes/DSP_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dispatch_id").value("DSP_001"))
                .andExpect(jsonPath("$.destination_room").value("ROOM_R101"));
    }

    @Test
    @DisplayName("GET /api/task1/routes/{dispatchId} should return 404 when not found")
    void testGetRouteNotFound() throws Exception {
        when(deliveryRouteRepository.findById("DSP_MISSING")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/task1/routes/DSP_MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/task1/health should return status UP")
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/task1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/task1/benchmark should return 404 when no benchmark metrics file exists yet")
    void testBenchmarkNotYetGenerated() throws Exception {
        mockMvc.perform(get("/api/task1/benchmark"))
                .andExpect(status().isNotFound());
    }
}
