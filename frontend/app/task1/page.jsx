"use client";

import { useEffect, useMemo, useState } from "react";
import { AccessibilityIcon, ClockIcon } from "lucide-react";
import { Panel } from "@/components/Panel";
import { ModuleHeader } from "@/components/ModuleHeader";
import MetricsBar from "@/components/MetricsBar";
import { RunButton } from "@/components/RunButton";
import { EmptyState } from "@/components/EmptyState";
import { JsonPreview } from "@/components/JsonPreview";
import { StatusBadge } from "@/components/StatusBadge";
import { useModuleRun } from "@/contexts/PipelineContext";
import { moduleById } from "@/lib/modules";
import { buildingGraph as sampleBuildingGraph, dispatchOrders as sampleDispatchOrders } from "@/lib/data/buildingGraph";
import { deliveryRoutes as sampleRoutes, routingMetrics } from "@/lib/data/outputs";
import { api } from "@/lib/api/index";

const FLOORS = [3, 2, 1, 0];
const BAND_HEIGHT = 104;
const MAP_WIDTH = 470;

const scaleX = (x) => 60 + x * 8.6;
const bandTop = (floor) => FLOORS.indexOf(floor) * BAND_HEIGHT + 14;
const scaleY = (node) => bandTop(node.floor) + ((node.coordinates.y - 6) / 20) * 66;

function FloorMap({ route, graph, nodeById, uniqueEdges }) {
  const pathPairs = new Set(
    route.path_sequence.slice(0, -1).map((node, index) => [node, route.path_sequence[index + 1]].sort().join("|"))
  );
  const onPath = new Set(route.path_sequence);

  return (
    <svg
      viewBox={`0 0 ${MAP_WIDTH} ${FLOORS.length * BAND_HEIGHT + 10}`}
      className="h-auto w-full"
      role="img"
      aria-label={`Delivery path for ${route.dispatch_id} from ${route.source_vault} to ${route.destination_room}`}
    >
      {FLOORS.map((floor) => (
        <g key={floor}>
          <rect x={44} y={bandTop(floor) - 12} width={MAP_WIDTH - 56} height={BAND_HEIGHT - 14} rx={4} fill={floor % 2 === 0 ? "#F5F7FA" : "#FFFFFF"} stroke="#DCE3EA" />
          <text x={16} y={bandTop(floor) + 26} className="mono" fontSize="10" fill="#8A98A2">
            {floor === 0 ? "GND" : `F${floor}`}
          </text>
        </g>
      ))}

      {uniqueEdges.map((edge) => {
        const from = nodeById.get(edge.from);
        const to = nodeById.get(edge.to);
        if (!from || !to) return null;
        const key = [edge.from, edge.to].sort().join("|");
        const active = pathPairs.has(key);
        return (
          <line
            key={key}
            x1={scaleX(from.coordinates.x)}
            y1={scaleY(from)}
            x2={scaleX(to.coordinates.x)}
            y2={scaleY(to)}
            stroke={active ? "#0F4C75" : "#DCE3EA"}
            strokeWidth={active ? 3 : 1.25}
            strokeDasharray={edge.stepFree ? undefined : "4 3"}
          />
        );
      })}

      {graph.map((node) => {
        const active = onPath.has(node.node_id);
        const isEndpoint = node.node_id === route.source_vault || node.node_id === route.destination_room;
        return (
          <g key={node.node_id} opacity={active ? 1 : 0.5}>
            <circle
              cx={scaleX(node.coordinates.x)}
              cy={scaleY(node)}
              r={isEndpoint ? 7 : active ? 5.5 : 4}
              fill={isEndpoint ? "#0F4C75" : active ? "#3282B8" : node.is_accessible ? "#FFFFFF" : "#EEF2F5"}
              stroke={active ? "#0F4C75" : "#B7C4CE"}
              strokeWidth={1.5}
            />
            <text x={scaleX(node.coordinates.x)} y={scaleY(node) - 10} textAnchor="middle" className="mono" fontSize="8" fill={active ? "#0F4C75" : "#8A98A2"} fontWeight={active ? 600 : 400}>
              {node.node_id}
            </text>
          </g>
        );
      })}
    </svg>
  );
}

export default function Task1Page() {
  const module = moduleById("task1");
  const { run, execute } = useModuleRun("task1");
  const [liveGraph, setLiveGraph] = useState(null);
  const [liveDispatchOrders, setLiveDispatchOrders] = useState(null);
  const [dispatchId, setDispatchId] = useState(sampleDispatchOrders[0].dispatch_id);
  const complete = run.state === "complete";

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled([api.task1.getBuildingGraph(), api.task1.getDispatchOrders()]).then(([graphResult, ordersResult]) => {
      if (cancelled) return;
      if (graphResult.status === "fulfilled") setLiveGraph(graphResult.value);
      if (ordersResult.status === "fulfilled") setLiveDispatchOrders(ordersResult.value);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  const buildingGraph = liveGraph ?? sampleBuildingGraph;
  const dispatchOrders = liveDispatchOrders ?? sampleDispatchOrders;

  const nodeById = useMemo(() => new Map(buildingGraph.map((node) => [node.node_id, node])), [buildingGraph]);
  const uniqueEdges = useMemo(() => {
    const seen = new Set();
    const list = [];
    buildingGraph.forEach((node) => {
      node.adjacent_edges.forEach((edge) => {
        const key = [node.node_id, edge.target_node].sort().join("|");
        if (seen.has(key) || !nodeById.has(edge.target_node)) return;
        seen.add(key);
        list.push({ from: node.node_id, to: edge.target_node, stepFree: edge.is_step_free });
      });
    });
    return list;
  }, [buildingGraph, nodeById]);

  const routes = run.data?.routes ?? (Array.isArray(run.data) ? run.data : null) ?? sampleRoutes;
  const route = routes.find((item) => item.dispatch_id === dispatchId) ?? routes[0];
  const order = dispatchOrders.find((item) => item.dispatch_id === dispatchId) ?? dispatchOrders[0];

  return (
    <div>
      <ModuleHeader
        module={module}
        description="Routes exam papers from the security vault to each exam room across a multi-floor building graph using A* with a 3D Euclidean heuristic and a floor-change penalty, enforcing step-free paths where required."
      />

      <div className="grid gap-6 px-8 py-7 xl:grid-cols-[340px_minmax(0,1fr)]">
        <div className="flex flex-col gap-6">
          <Panel
            title="Input"
            subtitle="Building graph + dispatch orders"
            actions={<RunButton label="Route" running={run.state === "running"} onClick={() => execute(dispatchOrders, { routes: sampleRoutes })} />}
          >
            <dl className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Graph nodes</dt>
                <dd className="mono mt-1 text-xl font-semibold text-ink">{buildingGraph.length}</dd>
              </div>
              <div>
                <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Dispatches</dt>
                <dd className="mono mt-1 text-xl font-semibold text-ink">{dispatchOrders.length}</dd>
              </div>
            </dl>
            <div className="mt-5 space-y-2">
              <JsonPreview fileName="input_building_graph.json" payload={buildingGraph} />
              <JsonPreview fileName="input_dispatch_orders.json" payload={dispatchOrders} />
            </div>
          </Panel>

          {complete && (
            <Panel title="Dispatches" subtitle="Select to inspect a route" bodyClassName="p-0">
              <ul className="divide-y divide-line">
                {routes.map((item) => (
                  <li key={item.dispatch_id}>
                    <button
                      type="button"
                      onClick={() => setDispatchId(item.dispatch_id)}
                      className={`flex w-full items-center gap-3 px-5 py-3 text-left transition-colors duration-150 ease-out hover:bg-canvas ${
                        item.dispatch_id === dispatchId ? "bg-primary-soft" : ""
                      }`}
                    >
                      <span className="mono text-xs font-semibold text-primary">{item.dispatch_id}</span>
                      <span className="flex-1 text-xs text-ink-muted">
                        {item.course_code} → {item.destination_room}
                      </span>
                      <span className="mono text-xs text-ink">{item.estimated_transit_time_seconds}s</span>
                    </button>
                  </li>
                ))}
              </ul>
            </Panel>
          )}
        </div>

        <div className="flex flex-col gap-6">
          {complete ? (
            <>
              <Panel
                title={`Floor map — ${route.dispatch_id}`}
                subtitle="Solid edges are step-free; dashed edges are staircases"
                actions={
                  <div className="flex items-center gap-2">
                    {route.requires_step_free_access && (
                      <StatusBadge label="Step-free required" tone="warning" icon={<AccessibilityIcon className="h-3.5 w-3.5" />} />
                    )}
                    <StatusBadge
                      label={`${route.estimated_transit_time_seconds}s · ${route.total_distance_meters} m`}
                      tone={route.within_time_limit ? "success" : "danger"}
                      icon={<ClockIcon className="h-3.5 w-3.5" />}
                    />
                  </div>
                }
              >
                <FloorMap route={route} graph={buildingGraph} nodeById={nodeById} uniqueEdges={uniqueEdges} />
              </Panel>

              <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_260px]">
                <Panel title="Turn-by-turn manifest" subtitle={route.path_sequence.join(" → ")} bodyClassName="p-0">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-line text-left text-[11px] uppercase tracking-wide text-ink-faint">
                        <th className="px-5 py-2 font-medium">Step</th>
                        <th className="px-5 py-2 font-medium">Action</th>
                        <th className="px-5 py-2 text-right font-medium">Time</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-line">
                      {(route.turn_by_turn_manifest ?? []).map((step) => (
                        <tr key={step.step}>
                          <td className="mono px-5 py-2.5 text-xs text-ink-faint">{step.step}</td>
                          <td className="px-5 py-2.5">
                            <p className="text-xs text-ink">{step.action}</p>
                            <p className="mono text-[11px] text-ink-faint">
                              {step.from} → {step.to}
                            </p>
                          </td>
                          <td className="mono px-5 py-2.5 text-right text-xs text-ink-muted">{step.time_sec}s</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </Panel>

                <Panel title="Dispatch detail">
                  <dl className="space-y-2.5 text-xs">
                    {[
                      ["Exam", route.exam_id],
                      ["Course", route.course_code],
                      ["Destination", route.destination_room],
                      ["Target floor", String(route.target_floor)],
                      ["Package", `${order?.package_weight_kg ?? "—"} kg`],
                      ["Transport", order?.transport_mode ?? "—"],
                      ["Step-free verified", String(route.step_free_verified)],
                      ["Time limit", `${order?.max_allowed_transit_seconds ?? "—"}s allowed`],
                    ].map(([label, value]) => (
                      <div key={label} className="flex justify-between gap-3">
                        <dt className="text-ink-muted">{label}</dt>
                        <dd className="mono truncate font-medium text-ink">{value}</dd>
                      </div>
                    ))}
                  </dl>
                </Panel>
              </div>

              <MetricsBar metrics={run.data?.metrics ?? routingMetrics} source={run.source} />

              <JsonPreview
                fileName="output_delivery_routes.json"
                payload={{ generation_timestamp: "2026-08-18T14:30:00Z", status: "OPTIMAL", total_dispatches: routes.length, successful_routes: routes.length, failed_routes: 0, routes }}
              />
            </>
          ) : (
            <Panel title="Output" subtitle="output_delivery_routes.json">
              <EmptyState
                title="No routes computed yet"
                description="Run the router to expand the building graph with A* and produce a step-free verified delivery path per dispatch."
              />
            </Panel>
          )}
        </div>
      </div>
    </div>
  );
}
