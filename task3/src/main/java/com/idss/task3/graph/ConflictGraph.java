package com.idss.task3.graph;

import com.idss.task3.model.ConflictEdge;
import com.idss.task3.model.VertexResult;
import java.util.List;

public interface ConflictGraph {
    int[][] getConflictMatrix();
    List<ConflictEdge> getEdges();
    List<VertexResult> getVertices();
    List<String> getExamRegistry();
    double getGraphDensity();
    boolean isValid();
}
