"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import MetricsBar from "@/components/MetricsBar";
import { dummyRankings, dummyRooms, dummyMetrics } from "@/lib/types/dummyData";
import { exportJson } from "@/lib/utils/exportJson";
import { api } from "@/lib/api/index";
import { Play, Download, Building2, Loader2, CheckCircle2, XCircle, Thermometer, Volume2, Accessibility } from "lucide-react";

export default function Task4Page() {
  const [result, setResult] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleRun() {
    setLoading(true);
    try {
      const data = await api.task4.rank({ exam_id: "EX_101", rooms: dummyRooms });
      setResult(data.rankings || data);
      setMetrics(data.metrics || dummyMetrics.task4);
    } catch {
      setResult(dummyRankings);
      setMetrics(dummyMetrics.task4);
    } finally {
      setLoading(false);
    }
  }

  const rankingData = result || dummyRankings;

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Room Ranking</h1>
        <p className="text-muted-foreground mt-1">Score and rank rooms by how suitable they are for each exam — based on capacity, air conditioning, and noise levels.</p>
      </div>

      <MetricsBar metrics={metrics} />
      
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Input Card — Room Registry */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Building2 className="w-5 h-5 text-primary" />
              Available Rooms
            </CardTitle>
            <CardDescription>Room features used for scoring.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-3">
              {dummyRooms.map((room) => (
                <div key={room.room_id} className="bg-muted/50 rounded-lg border border-border p-3">
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-semibold">{room.room_id}</span>
                    <Badge variant="outline">{room.capacity} seats</Badge>
                  </div>
                  <div className="flex flex-wrap gap-2 text-xs">
                    <span className="flex items-center gap-1">
                      <Thermometer className="w-3 h-3" />
                      {room.has_ac ? "Air Conditioned" : "No AC"}
                    </span>
                    <span className="flex items-center gap-1">
                      <Volume2 className="w-3 h-3" />
                      Quiet: {room.noise_level}/5
                    </span>
                    <span className="flex items-center gap-1">
                      <Accessibility className="w-3 h-3" />
                      {room.is_accessible ? "Accessible" : "Not Accessible"}
                    </span>
                  </div>
                </div>
              ))}
            </div>
            <Button className="w-full" onClick={handleRun} disabled={loading}>
              {loading ? <Loader2 className="w-4 h-4 animate-spin mr-2" /> : <Play className="w-4 h-4 mr-2" />}
              {loading ? "Ranking..." : "Rank Rooms for EX_101"}
            </Button>
          </CardContent>
        </Card>
        
        {/* Output Card — Rankings */}
        <Card>
          <CardHeader>
            <CardTitle>Suitability Rankings</CardTitle>
            <CardDescription>Rooms ranked by TOPSIS score (closer to 1.0 = better fit).</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-16">Rank</TableHead>
                  <TableHead>Room</TableHead>
                  <TableHead>Score</TableHead>
                  <TableHead className="text-right">Meets Requirements</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rankingData.map((ranking, index) => (
                  <TableRow key={index} className={index === 0 ? "bg-primary/5" : ""}>
                    <TableCell>
                      <span className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold ${
                        index === 0 ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"
                      }`}>
                        {ranking.rank}
                      </span>
                    </TableCell>
                    <TableCell className="font-medium">{ranking.room_id}</TableCell>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <span className="font-mono text-sm font-semibold">{ranking.score.toFixed(3)}</span>
                        <div className="h-2 flex-1 max-w-[100px] bg-muted rounded-full overflow-hidden">
                          <div 
                            className="h-full bg-primary rounded-full transition-all duration-500" 
                            style={{ width: `${ranking.score * 100}%` }}
                          />
                        </div>
                      </div>
                    </TableCell>
                    <TableCell className="text-right">
                      {ranking.meets_hard_constraints ? (
                        <span className="inline-flex items-center gap-1 text-green-500 text-xs font-medium">
                          <CheckCircle2 className="w-4 h-4" /> Yes
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-red-400 text-xs font-medium">
                          <XCircle className="w-4 h-4" /> No
                        </span>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            
            <div className="mt-4">
              <p className="text-xs text-muted-foreground mb-2">Criteria Weights (AHP-derived)</p>
              <div className="flex flex-wrap gap-2">
                <Badge variant="outline" className="text-xs">Capacity: 50%</Badge>
                <Badge variant="outline" className="text-xs">Air Conditioning: 30%</Badge>
                <Badge variant="outline" className="text-xs">Noise Level: 20%</Badge>
              </div>
            </div>
          </CardContent>
          <CardFooter className="border-t pt-4">
            <Button variant="outline" className="w-full" onClick={() => exportJson(rankingData, "output_room_rankings.json")}>
              <Download className="w-4 h-4 mr-2" /> Download Ranking Data
            </Button>
          </CardFooter>
        </Card>
      </div>
    </div>
  );
}
