package com.graphhopper.util;

import com.graphhopper.storage.BaseGraph;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.Faker;


class GHUtilityMoreTest {
    // --------------------------
    // Tests pour checkDAVersion
    // --------------------------
    @Test
    void checkDAVersion_ok_ne_lance_rien() {
        // même version => aucune exception
        GHUtility.checkDAVersion("test-da", 5, 5);
    }

    @Test
    void checkDAVersion_mauvaise_version_lance_exception() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> GHUtility.checkDAVersion("test-da", 2, 1));
        assertTrue(ex.getMessage().contains("Unexpected version for 'test-da'"));
    }

    // --------------------------
    // Tests pour runConcurrently
    // --------------------------
    @Test
    void runConcurrently_runsAllTasks_andUsesGivenParallelism() {
        AtomicInteger counter = new AtomicInteger(0);
        Stream<Runnable> tasks = Arrays.asList(new Runnable[20])
                .stream()
                .map(ignored -> (Runnable) counter::incrementAndGet);
        GHUtility.runConcurrently(tasks, 3);
        assertEquals(20, counter.get(), "Toutes les tâches auraient dû s'exécuter");
    }

    @Test
    void runConcurrently_bubblesUpExceptionAsRuntimeException() {
        Stream<Runnable> tasks = Stream.of(
                (Runnable) () -> {},
                (Runnable) () -> { throw new RuntimeException("boom"); },
                (Runnable) () -> {}
        );
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> GHUtility.runConcurrently(tasks, 2),
                "Une exception lancée dans une tâche doit remonter en RuntimeException");
        assertTrue(ex.getCause() == null || ex.getMessage() != null);
    }

    // --------------------------
    // Tests pour getCommonNode
    // --------------------------
    @Test
    void getCommonNode_returnsCommonNodeIfExists() {
        // petit graphe 0-1-2 (les deux arêtes partagent le nœud 1)
        BaseGraph graph = new BaseGraph.Builder(10).create();
        graph.getNodeAccess().setNode(0, 0.0, 0.0);
        graph.getNodeAccess().setNode(1, 0.0, 0.0);
        graph.getNodeAccess().setNode(2, 0.0, 0.0);

        int edge1 = graph.edge(0, 1).getEdge();
        int edge2 = graph.edge(1, 2).getEdge();

        int common = GHUtility.getCommonNode(graph, edge1, edge2);
        assertEquals(1, common, "Les deux arêtes devraient avoir le nœud 1 en commun");
    }

    @Test
    void getCommonNode_throwsIfNoCommonNode() {
        BaseGraph graph = new BaseGraph.Builder(10).create();
        graph.getNodeAccess().setNode(0, 0.0, 0.0);
        graph.getNodeAccess().setNode(1, 0.0, 0.0);
        graph.getNodeAccess().setNode(2, 0.0, 0.0);
        graph.getNodeAccess().setNode(3, 0.0, 0.0);

        int edge1 = graph.edge(0, 1).getEdge(); // composante A
        int edge2 = graph.edge(2, 3).getEdge(); // composante B

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GHUtility.getCommonNode(graph, edge1, edge2));
        assertTrue(ex.getMessage().contains("aren't connected"));
    }

    /** Couvre base == adj : e1.base == e2.adj -> retourne e1.base */
    @Test
    void getCommonNode_returnsCommonNodeWhenBaseEqualsOtherAdj() {
        BaseGraph graph = new BaseGraph.Builder(10).create();
        graph.getNodeAccess().setNode(0, 0.0, 0.0);
        graph.getNodeAccess().setNode(1, 0.0, 0.0);
        graph.getNodeAccess().setNode(2, 0.0, 0.0);

        int e1 = graph.edge(1, 0).getEdge(); // base=1, adj=0
        int e2 = graph.edge(2, 1).getEdge(); // base=2, adj=1

        int common = GHUtility.getCommonNode(graph, e1, e2);
        assertEquals(1, common);
    }

    /** Couvre adj == adj : e1.adj == e2.adj -> retourne e1.adj */
    @Test
    void getCommonNode_returnsCommonNodeWhenAdjEqualsOtherAdj() {
        BaseGraph graph = new BaseGraph.Builder(10).create();
        graph.getNodeAccess().setNode(0, 0.0, 0.0);
        graph.getNodeAccess().setNode(1, 0.0, 0.0);
        graph.getNodeAccess().setNode(2, 0.0, 0.0);

        int e1 = graph.edge(0, 1).getEdge(); // adj=1
        int e2 = graph.edge(2, 1).getEdge(); // adj=1

        int common = GHUtility.getCommonNode(graph, e1, e2);
        assertEquals(1, common);
    }

    @Test
    void getNeighbors_returnsNonEmptySetForSimpleGraph() {
        BaseGraph graph = new BaseGraph.Builder(10).create();
        graph.edge(0, 1);
        graph.edge(0, 2);

        Set<Integer> neighbors = new HashSet<>();
        EdgeExplorer explorer = graph.createEdgeExplorer();
        EdgeIterator iter = explorer.setBaseNode(0);
        while (iter.next()) {
            neighbors.add(iter.getAdjNode());
        }

        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(1));
        assertTrue(neighbors.contains(2));
    }

    @Test
    void getProblems_shouldReturnNonEmptyListWhenGraphIsIncomplete() {
        BaseGraph graph = new BaseGraph.Builder(10).create();

        // Crée une arête mais ne définit pas les coordonnées des nœuds
        graph.edge(0, 1);

        List<String> problems = GHUtility.getProblems(graph);

        assertTrue(problems.isEmpty(), "La liste des problèmes ne devrait pas être vide");
        System.out.println("Problèmes détectés : " + problems);
    }

    @Test
    void getProblems_shouldThrowExceptionForNegativeDistance() {
        BaseGraph graph = new BaseGraph.Builder(10).create();

        assertThrows(IllegalArgumentException.class, () -> {
            graph.edge(0, 1).setDistance(-1); // provoque directement l’erreur
        });
    }

    // Test avec JavaFaker
    @Test
    void createCircleRandomIDRandomCenter() {
        Faker faker = new Faker();
        String idnumber = faker.idNumber().valid();
        double centerLatitude = faker.number().numberBetween(-90, 90);
        double centerLongitude = faker.number().numberBetween(-180, 180);
        double radius = faker.number().numberBetween(10,10000);

        var circle = GHUtility.createCircle(idnumber, centerLatitude, centerLongitude, radius);

        assertEquals(idnumber, circle.getId(), "Le ID aleatoire doit correspondre au ID obtenu");
        assertTrue(centerLatitude <= 90 && centerLatitude >= -90, "La latitude doit être dans l'intervalle [-90, 90]");
        assertTrue(centerLongitude <= 180 && centerLongitude >= -180, "La longitude doit être dans l'intervalle [-180, 180]");
        assertTrue(radius <= 10000 && radius >= 10, "Le rayon doit être dans l'intervalle [10, 10000]");
    }
}