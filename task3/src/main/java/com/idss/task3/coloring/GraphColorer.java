package com.idss.task3.coloring;

import com.idss.task3.graph.ConflictGraph;
import java.util.Map;

/**
 * Shared interface for exam graph colorers.
 */
public interface GraphColorer {
    ColoringResult color(ConflictGraph graph);

    class ColoringResult {
        private final Map<String, Integer> colorOf;
        private final int numColors;

        public ColoringResult(Map<String, Integer> colorOf, int numColors) {
            this.colorOf = colorOf;
            this.numColors = numColors;
        }

        public Map<String, Integer> getColorOf() {
            return colorOf;
        }

        public int getNumColors() {
            return numColors;
        }
    }
}
