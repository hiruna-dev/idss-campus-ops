package com.idss.task3.controller;

import com.idss.task3.model.ConflictEdge;
import com.idss.task3.model.VertexResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ConflictGraphResponse {
    @JsonProperty("generation_timestamp")
    private String generationTimestamp;
    private String status;
    @JsonProperty("algorithm_used")
    private String algorithmUsed;
    @JsonProperty("total_exams")
    private int totalExams;
    private List<VertexResult> vertices;
    private List<ConflictEdge> edges;
    @JsonProperty("graph_density")
    private double graphDensity;

    public ConflictGraphResponse() {}

    public String getGenerationTimestamp() { return generationTimestamp; }
    public void setGenerationTimestamp(String generationTimestamp) { this.generationTimestamp = generationTimestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAlgorithmUsed() { return algorithmUsed; }
    public void setAlgorithmUsed(String algorithmUsed) { this.algorithmUsed = algorithmUsed; }

    public int getTotalExams() { return totalExams; }
    public void setTotalExams(int totalExams) { this.totalExams = totalExams; }

    public List<VertexResult> getVertices() { return vertices; }
    public void setVertices(List<VertexResult> vertices) { this.vertices = vertices; }

    public List<ConflictEdge> getEdges() { return edges; }
    public void setEdges(List<ConflictEdge> edges) { this.edges = edges; }

    public double getGraphDensity() { return graphDensity; }
    public void setGraphDensity(double graphDensity) { this.graphDensity = graphDensity; }
}
