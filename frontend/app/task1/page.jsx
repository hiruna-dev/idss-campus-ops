"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import MetricsBar from "@/components/MetricsBar";
import { dummyRoutes, dummyMetrics, buildingGraphNodes, buildingGraphEdges } from "@/lib/types/dummyData";
import { exportJson } from "@/lib/utils/exportJson";
import { api } from "@/lib/api/index";
import { Play, Download, MapPin, CheckCircle2, Loader2 } from "lucide-react";

export default function Task1Page() {
  const [result, setResult] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(false);

  const dispatchRequest = {
    dispatch_id: "DSP_001",
    destination_room_id: "R101",
    requires_step_free_access: true,
  };

  async function handleRun() {
    setLoading(true);
    try {
      const data = await api.task1.route(dispatchRequest);
      setResult(data);
      setMetrics(data.metrics || dummyMetrics.task1);
    } catch {
      // Backend not available — use dummy data
      setResult(dummyRoutes);
      setMetrics(dummyMetrics.task1);
    } finally {
      setLoading(false);
    }
  }

  const pathData = result || dummyRoutes;
  const pathSet = new Set(pathData.path_sequence);

  // Build edge lookup for SVG
  const edgeLines = buildingGraphEdges.map((e) => {
    const from = buildingGraphNodes.find((n) => n.id === e.from);
    const to = buildingGraphNodes.find((n) => n.id === e.to);
    if (!from || !to) return null;
    const isOnPath =
      pathData.path_sequence.some((node, i) => {
        const next = pathData.path_sequence[i + 1];
        return (node === e.from && next === e.to) || (node === e.to && next === e.from);
      });
    return { ...e, x1: from.x, y1: from.y, x2: to.x, y2: to.y, isOnPath };
  }).filter(Boolean);

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Paper Routing</h1>
        <p className="text-muted-foreground mt-1">Find the shortest path to deliver exam papers from the vault to the exam hall.</p>
      </div>

      <MetricsBar metrics={metrics} />
      
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Input Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <MapPin className="w-5 h-5 text-primary" />
              Delivery Request
            </CardTitle>
            <CardDescription>Configure where to deliver exam papers.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-3">
              <div className="flex justify-between items-center py-2 border-b border-border">
                <span className="text-sm text-muted-foreground">Dispatch ID</span>
                <span className="text-sm font-medium">{dispatchRequest.dispatch_id}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-border">
                <span className="text-sm text-muted-foreground">Destination Room</span>
                <Badge variant="outline">{dispatchRequest.destination_room_id}</Badge>
              </div>
              <div className="flex justify-between items-center py-2">
                <span className="text-sm text-muted-foreground">Step-Free Access Required</span>
                <Badge className="bg-green-500/15 text-green-500 hover:bg-green-500/20">Yes</Badge>
              </div>
            </div>
            <Button className="w-full" onClick={handleRun} disabled={loading}>
              {loading ? <Loader2 className="w-4 h-4 animate-spin mr-2" /> : <Play className="w-4 h-4 mr-2" />}
              {loading ? "Calculating Route..." : "Find Best Route"}
            </Button>
          </CardContent>
        </Card>
        
        {/* Output Card — Turn-by-turn */}
        <Card>
          <CardHeader>
            <CardTitle>Delivery Steps</CardTitle>
            <CardDescription>Step-by-step route from vault to destination.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-16">Step</TableHead>
                  <TableHead>Location</TableHead>
                  <TableHead className="text-right">Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {pathData.path_sequence.map((node, index) => {
                  const nodeInfo = buildingGraphNodes.find(n => n.id === node);
                  return (
                    <TableRow key={index}>
                      <TableCell>
                        <span className="w-6 h-6 rounded-full bg-primary/15 text-primary text-xs font-bold flex items-center justify-center">
                          {index + 1}
                        </span>
                      </TableCell>
                      <TableCell>
                        <div>
                          <span className="font-medium">{nodeInfo?.label || node}</span>
                          <span className="text-xs text-muted-foreground ml-2">({node})</span>
                        </div>
                      </TableCell>
                      <TableCell className="text-right">
                        {result && <CheckCircle2 className="w-4 h-4 text-green-500 inline-block" />}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
            {pathData.step_free_verified && (
              <div className="mt-3 flex items-center gap-2 text-xs text-green-500">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Route is step-free accessible (elevator path used)</span>
              </div>
            )}
          </CardContent>
          <CardFooter className="border-t pt-4">
            <Button variant="outline" className="w-full" onClick={() => exportJson(pathData, "output_delivery_route.json")}>
              <Download className="w-4 h-4 mr-2" /> Download Route Data
            </Button>
          </CardFooter>
        </Card>
      </div>

      {/* Floor Map */}
      <Card>
        <CardHeader>
          <CardTitle>Building Floor Map</CardTitle>
          <CardDescription>Visual path from vault to destination. Highlighted route shows the calculated path.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="w-full aspect-[2/1] bg-muted/30 border border-border rounded-lg overflow-hidden p-4">
            <svg width="100%" height="100%" viewBox="0 0 400 220" className="select-none">
              {/* Floor Labels */}
              <text x="5" y="45" className="text-[10px] fill-muted-foreground font-semibold">1st Floor</text>
              <text x="5" y="165" className="text-[10px] fill-muted-foreground font-semibold">Ground</text>
              <line x1="0" y1="130" x2="400" y2="130" stroke="var(--border)" strokeWidth="1" strokeDasharray="6 3" />

              {/* Edges */}
              {edgeLines.map((e, i) => (
                <line
                  key={i}
                  x1={e.x1} y1={e.y1} x2={e.x2} y2={e.y2}
                  stroke={e.isOnPath ? "var(--primary)" : "var(--border)"}
                  strokeWidth={e.isOnPath ? 3 : 1.5}
                  strokeLinecap="round"
                  opacity={e.isOnPath ? 1 : 0.5}
                />
              ))}

              {/* Nodes */}
              {buildingGraphNodes.map((node) => {
                const isOnPath = pathSet.has(node.id);
                const isStart = pathData.path_sequence[0] === node.id;
                const isEnd = pathData.path_sequence[pathData.path_sequence.length - 1] === node.id;
                return (
                  <g key={node.id}>
                    <circle
                      cx={node.x} cy={node.y}
                      r={isStart || isEnd ? 14 : 10}
                      fill={isOnPath ? "var(--primary)" : "var(--muted)"}
                      stroke={isOnPath ? "var(--primary)" : "var(--border)"}
                      strokeWidth={isOnPath ? 2 : 1}
                    />
                    <text
                      x={node.x} y={node.y + 3}
                      textAnchor="middle"
                      className={`text-[7px] font-bold ${isOnPath ? "fill-primary-foreground" : "fill-muted-foreground"}`}
                    >
                      {isStart ? "START" : isEnd ? "END" : node.label.substring(0, 5)}
                    </text>
                    <text
                      x={node.x} y={node.y + 24}
                      textAnchor="middle"
                      className="text-[8px] fill-muted-foreground"
                    >
                      {node.label}
                    </text>
                  </g>
                );
              })}
            </svg>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
