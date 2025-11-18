package com.graphhopper.mocktests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class DijkstraTest {

    static class Graph {
        Map<Integer, List<Integer>> edges = new HashMap<>();

        void connect(int a, int b) {
            edges.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        }

        boolean areConnected(int from, int to) {
            Set<Integer> visited = new HashSet<>();
            Queue<Integer> q = new LinkedList<>();

            q.add(from);
            visited.add(from);

            while (!q.isEmpty()) {
                int cur = q.poll();
                if (cur == to) return true;

                for (int next : edges.getOrDefault(cur, List.of())) {
                    if (!visited.contains(next)) {
                        visited.add(next);
                        q.add(next);
                    }
                }
            }
            return false;
        }
    }

    @Test
    public void testPathExistsBetweenConnectedNodes() {
        Graph g = new Graph();
        g.connect(0, 1);
        g.connect(1, 2);

        assertTrue(g.areConnected(0, 2));
    }

    @Test
    public void testNoPathBetweenUnconnectedNodes() {
        Graph g = new Graph();
        g.connect(0, 1);

        assertFalse(g.areConnected(0, 5));
    }
}

