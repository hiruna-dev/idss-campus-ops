const corridor = (target, distance_meters, base_transit_time_seconds) => ({
  target_node: target,
  distance_meters,
  base_transit_time_seconds,
  edge_type: "CORRIDOR",
  is_step_free: true,
  security_clearance_required: 1,
});

const shaft = (target, distance_meters, base_transit_time_seconds) => ({
  target_node: target,
  distance_meters,
  base_transit_time_seconds,
  edge_type: "ELEVATOR_SHAFT",
  is_step_free: true,
  security_clearance_required: 1,
});

const stair = (target, distance_meters, base_transit_time_seconds) => ({
  target_node: target,
  distance_meters,
  base_transit_time_seconds,
  edge_type: "STAIRCASE",
  is_step_free: false,
  security_clearance_required: 1,
});

export const buildingGraph = [
  { node_id: "VAULT_G01", node_name: "Central Exam Security Vault", floor: 0, coordinates: { x: 10, y: 12, z: 0 }, node_type: "VAULT", is_accessible: true, adjacent_edges: [{ ...corridor("HALLWAY_G_EAST", 14.5, 12), security_clearance_required: 3 }] },
  { node_id: "HALLWAY_G_EAST", node_name: "Ground Floor East Corridor Junction", floor: 0, coordinates: { x: 24.5, y: 12, z: 0 }, node_type: "JUNCTION", is_accessible: true, adjacent_edges: [corridor("VAULT_G01", 14.5, 12), corridor("ELEV_G", 8, 7), corridor("STAIR_G", 6.5, 5), corridor("ROOM_R004", 12, 10)] },
  { node_id: "ELEV_G", node_name: "Ground Floor Central Elevator", floor: 0, coordinates: { x: 32.5, y: 12, z: 0 }, node_type: "ELEVATOR", is_accessible: true, adjacent_edges: [corridor("HALLWAY_G_EAST", 8, 7), shaft("ELEV_F1", 4, 25), shaft("ELEV_F3", 12, 45)] },
  { node_id: "STAIR_G", node_name: "Ground Floor Main Stairwell", floor: 0, coordinates: { x: 30, y: 19.5, z: 0 }, node_type: "STAIRS", is_accessible: false, adjacent_edges: [corridor("HALLWAY_G_EAST", 6.5, 5), stair("STAIR_F1", 6, 18)] },
  { node_id: "ROOM_R004", node_name: "Ground Auditorium", floor: 0, coordinates: { x: 24.5, y: 24, z: 0 }, node_type: "ROOM", is_accessible: true, adjacent_edges: [corridor("HALLWAY_G_EAST", 12, 10)] },
  { node_id: "ELEV_F1", node_name: "First Floor Central Elevator", floor: 1, coordinates: { x: 32.5, y: 12, z: 4 }, node_type: "ELEVATOR", is_accessible: true, adjacent_edges: [shaft("ELEV_G", 4, 25), shaft("ELEV_F2", 4, 20), corridor("HALLWAY_F1_CENTRAL", 8.5, 7)] },
  { node_id: "STAIR_F1", node_name: "First Floor Stairwell", floor: 1, coordinates: { x: 30, y: 19.5, z: 4 }, node_type: "STAIRS", is_accessible: false, adjacent_edges: [stair("STAIR_G", 6, 18), stair("STAIR_F2", 6, 18), corridor("HALLWAY_F1_CENTRAL", 7.5, 6)] },
  { node_id: "HALLWAY_F1_CENTRAL", node_name: "Floor 1 Central Hallway", floor: 1, coordinates: { x: 24, y: 12, z: 4 }, node_type: "JUNCTION", is_accessible: true, adjacent_edges: [corridor("ELEV_F1", 8.5, 7), corridor("STAIR_F1", 7.5, 6), corridor("ROOM_R101", 13, 11), corridor("ROOM_R102", 13.5, 11)] },
  { node_id: "ROOM_R101", node_name: "Lecture Theatre 101", floor: 1, coordinates: { x: 12, y: 8, z: 4 }, node_type: "ROOM", is_accessible: true, adjacent_edges: [corridor("HALLWAY_F1_CENTRAL", 13, 11)] },
  { node_id: "ROOM_R102", node_name: "Lecture Theatre 102", floor: 1, coordinates: { x: 12, y: 18, z: 4 }, node_type: "ROOM", is_accessible: true, adjacent_edges: [corridor("HALLWAY_F1_CENTRAL", 13.5, 11)] },
  { node_id: "ELEV_F2", node_name: "Second Floor Central Elevator", floor: 2, coordinates: { x: 32.5, y: 12, z: 8 }, node_type: "ELEVATOR", is_accessible: true, adjacent_edges: [shaft("ELEV_F1", 4, 20), shaft("ELEV_F3", 4, 20), corridor("HALLWAY_F2_WEST", 8.5, 7)] },
  { node_id: "STAIR_F2", node_name: "Second Floor Stairwell", floor: 2, coordinates: { x: 30, y: 19.5, z: 8 }, node_type: "STAIRS", is_accessible: false, adjacent_edges: [stair("STAIR_F1", 6, 18), stair("STAIR_F3", 6, 18), corridor("HALLWAY_F2_WEST", 7.5, 6)] },
  { node_id: "HALLWAY_F2_WEST", node_name: "Floor 2 West Hallway", floor: 2, coordinates: { x: 24, y: 12, z: 8 }, node_type: "JUNCTION", is_accessible: true, adjacent_edges: [corridor("ELEV_F2", 8.5, 7), corridor("STAIR_F2", 7.5, 6), corridor("ROOM_R205", 13, 11), corridor("ROOM_LAB2B", 13.5, 12)] },
  { node_id: "ROOM_R205", node_name: "Seminar Hall 205", floor: 2, coordinates: { x: 12, y: 8, z: 8 }, node_type: "ROOM", is_accessible: false, adjacent_edges: [corridor("HALLWAY_F2_WEST", 13, 11)] },
  { node_id: "ROOM_LAB2B", node_name: "Computer Lab 2B", floor: 2, coordinates: { x: 12, y: 18, z: 8 }, node_type: "ROOM", is_accessible: false, adjacent_edges: [corridor("HALLWAY_F2_WEST", 13.5, 12)] },
  { node_id: "ELEV_F3", node_name: "Third Floor Central Elevator", floor: 3, coordinates: { x: 32.5, y: 12, z: 12 }, node_type: "ELEVATOR", is_accessible: true, adjacent_edges: [shaft("ELEV_F2", 4, 20), shaft("ELEV_G", 12, 45), corridor("HALLWAY_F3_NORTH", 8.5, 7)] },
  { node_id: "STAIR_F3", node_name: "Third Floor Stairwell", floor: 3, coordinates: { x: 30, y: 19.5, z: 12 }, node_type: "STAIRS", is_accessible: false, adjacent_edges: [stair("STAIR_F2", 6, 18), corridor("HALLWAY_F3_NORTH", 7.5, 6)] },
  { node_id: "HALLWAY_F3_NORTH", node_name: "Floor 3 North Corridor", floor: 3, coordinates: { x: 24, y: 12, z: 12 }, node_type: "JUNCTION", is_accessible: true, adjacent_edges: [corridor("ELEV_F3", 8.5, 7), corridor("STAIR_F3", 7.5, 6), corridor("ROOM_LAB3A", 13, 11), corridor("ROOM_R301", 14, 12)] },
  { node_id: "ROOM_LAB3A", node_name: "Computer Lab 3A", floor: 3, coordinates: { x: 12, y: 8, z: 12 }, node_type: "ROOM", is_accessible: false, adjacent_edges: [corridor("HALLWAY_F3_NORTH", 13, 11)] },
  { node_id: "ROOM_R301", node_name: "Exam Hall 301", floor: 3, coordinates: { x: 12, y: 18, z: 12 }, node_type: "ROOM", is_accessible: true, adjacent_edges: [corridor("HALLWAY_F3_NORTH", 14, 12)] },
];

export const dispatchOrders = [
  { dispatch_id: "DSP_001", exam_id: "EX_101", course_code: "PDSA201", source_vault_id: "VAULT_G01", destination_room_id: "ROOM_R101", destination_floor: 1, package_weight_kg: 4.8, transport_mode: "TROLLEY", requires_step_free_access: false, max_allowed_transit_seconds: 180 },
  { dispatch_id: "DSP_002", exam_id: "EX_102", course_code: "NET102", source_vault_id: "VAULT_G01", destination_room_id: "ROOM_LAB3A", destination_floor: 3, package_weight_kg: 3.6, transport_mode: "HAND_CARRY", requires_step_free_access: false, max_allowed_transit_seconds: 180 },
  { dispatch_id: "DSP_003", exam_id: "EX_103", course_code: "DBS301", source_vault_id: "VAULT_G01", destination_room_id: "ROOM_R301", destination_floor: 3, package_weight_kg: 6.2, transport_mode: "TROLLEY", requires_step_free_access: false, max_allowed_transit_seconds: 180 },
  { dispatch_id: "DSP_004", exam_id: "EX_104", course_code: "AIM401", source_vault_id: "VAULT_G01", destination_room_id: "ROOM_R101", destination_floor: 1, package_weight_kg: 2.8, transport_mode: "TROLLEY", requires_step_free_access: true, max_allowed_transit_seconds: 150 },
  { dispatch_id: "DSP_005", exam_id: "EX_108", course_code: "MAT101", source_vault_id: "VAULT_G01", destination_room_id: "ROOM_R004", destination_floor: 0, package_weight_kg: 7.0, transport_mode: "TROLLEY", requires_step_free_access: false, max_allowed_transit_seconds: 120 },
];
