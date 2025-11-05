/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.coll.GHBitSet;
import com.graphhopper.coll.GHIntHashSet;
import com.graphhopper.coll.GHTBitSet;
import com.graphhopper.storage.BaseGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class BreadthFirstSearchTest {
    int counter;
    GHIntHashSet set = new GHIntHashSet();
    IntArrayList list = new IntArrayList();

    @BeforeEach
    public void setup() {
        counter = 0;
    }

    private BreadthFirstSearch createTrackingBFS() {
        return new BreadthFirstSearch() {
            @Override
            protected GHBitSet createBitSet() {
                return new GHTBitSet();
            }

            @Override
            public boolean goFurther(int v) {
                counter++;
                assertFalse(set.contains(v), "Node " + v + " visited twice!");
                set.add(v);
                list.add(v);
                return super.goFurther(v);
            }
        };
    }

    @Test
    public void testBFS() {
        BreadthFirstSearch bfs = new BreadthFirstSearch() {
            @Override
            protected GHBitSet createBitSet() {
                return new GHTBitSet();
            }

            @Override
            public boolean goFurther(int v) {
                counter++;
                assertFalse(set.contains(v), "v " + v + " is already contained in set. iteration:" + counter);
                set.add(v);
                list.add(v);
                return super.goFurther(v);
            }
        };

        BaseGraph g = new BaseGraph.Builder(1).create();
        g.edge(0, 1);
        g.edge(0, 2);
        g.edge(0, 3);
        g.edge(0, 5);
        g.edge(1, 6);
        g.edge(2, 7);
        g.edge(3, 8);
        g.edge(4, 8);
        g.edge(8, 10);
        g.edge(6, 9);
        g.edge(9, 10);
        g.edge(5, 10);

        bfs.start(g.createEdgeExplorer(), 0);

        assertTrue(counter > 0);
        assertEquals(g.getNodes(), counter);
        assertEquals("[0, 5, 3, 2, 1, 10, 8, 7, 6, 9, 4]", list.toString());
    }

    @Test
    public void testBFS2() {
        BreadthFirstSearch bfs = new BreadthFirstSearch() {
            @Override
            protected GHBitSet createBitSet() {
                return new GHTBitSet();
            }

            @Override
            public boolean goFurther(int v) {
                counter++;
                assertFalse(set.contains(v), "v " + v + " is already contained in set. iteration:" + counter);
                set.add(v);
                list.add(v);
                return super.goFurther(v);
            }
        };

        BaseGraph g = new BaseGraph.Builder(1).create();
        g.edge(1, 2);
        g.edge(2, 3);
        g.edge(3, 4);
        g.edge(1, 5);
        g.edge(5, 6);
        g.edge(6, 4);

        bfs.start(g.createEdgeExplorer(), 1);

        assertTrue(counter > 0);
        assertEquals("[1, 5, 2, 6, 3, 4]", list.toString());
    }



    @Test
    public void testCycleGraph() {
        /*
        Nom: testCycleGraph
        Intention du test :
            Vérifier la résistance aux cycles — que BFS ne boucle pas indéfiniment et visite chaque nœud une seule fois.
        Motivation des données de test :
            Le graphe forme un cycle complet : 0 -> 1 -> 2 -> 3 -> 0
        Ce cas permet de s’assurer que la structure visited empêche les revisites.

        Oracle :
            Ordre attendu : [0, 3, 1, 2].
            Le BFS part de 0, enfile ses voisins (3, 1), puis poursuit avec 3 (qui mène à 2), tout en ignorant les nœuds déjà visités (0).
            Le critère d’attente : chaque nœud du cycle est visité une seule fois.
        * */
        BreadthFirstSearch bfs = createTrackingBFS();
        BaseGraph g = new BaseGraph.Builder(1).create();
        g.edge(0, 1);
        g.edge(1, 2);
        g.edge(2, 3);
        g.edge(3, 0); // cycle

        bfs.start(g.createEdgeExplorer(), 0);

        assertEquals(4, counter);
        assertEquals("[0, 3, 1, 2]", list.toString());
    }

    @Test
    public void testDisconnectedGraph() {
        /*
        Nom: testDisconnectedGraph
        Intention du test :
        Vérifier que BFS reste confiné au composant connexe contenant le nœud de départ et n’explore pas les autres composantes du graphe.

        Motivation des données de test :
            Le graphe comporte :
                - une composante connectée {0,1,2},
                - une composante isolée {5,6}.
        Ce scénario teste la capacité du BFS à ignorer les parties non accessibles du graphe.

        Oracle :
        Ordre attendu : [0, 1, 2].
        Le BFS doit se limiter à la composante accessible depuis 0.
        Les nœuds 5 et 6 ne sont jamais atteints, ce qui se vérifie par !visited(5) et !visited(6).
        * */
        BreadthFirstSearch bfs = createTrackingBFS();
        BaseGraph g = new BaseGraph.Builder(1).create();
        g.edge(0, 1);
        g.edge(1, 2);
        // isolated component
        g.edge(5, 6);

        bfs.start(g.createEdgeExplorer(), 0);

        assertEquals(3, counter);
        assertEquals("[0, 1, 2]", list.toString());
        // ensure isolated nodes (5,6) are not visited
        assertFalse(set.contains(5));
        assertFalse(set.contains(6));
    }

}
