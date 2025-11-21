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
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.*;

import static com.graphhopper.util.GHUtility.getCommonNode;

import com.graphhopper.coll.GHIntLongHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class GHUtilityTest {

    protected BooleanEncodedValue carAccessEnc = new SimpleBooleanEncodedValue("car_access", true);
    protected DecimalEncodedValue carSpeedEnc = new DecimalEncodedValueImpl("car_speed", 5, 5, false);
    protected BooleanEncodedValue footAccessEnc = new SimpleBooleanEncodedValue("foot_access", true);
    protected DecimalEncodedValue footSpeedEnc = new DecimalEncodedValueImpl("foot_speed", 4, 1, true);
    protected String defaultGraphLoc = "./target/graphstorage/default";
    protected BaseGraph graph;
    EdgeFilter carOutFilter = AccessFilter.outEdges(carAccessEnc);
    EdgeFilter carInFilter = AccessFilter.inEdges(carAccessEnc);
    EdgeExplorer carOutExplorer;
    EdgeExplorer carInExplorer;
    EdgeExplorer carAllExplorer;



    protected EncodingManager createEncodingManager() {
        return new EncodingManager.Builder()
                .add(carAccessEnc).add(carSpeedEnc)
                .add(footAccessEnc).add(footSpeedEnc)
                .add(RoadClass.create())
                .build();
    }
    protected EncodingManager encodingManager = createEncodingManager();
    protected int defaultSize = 100;

    public BaseGraph createGHStorage(String location, boolean enabled3D) {
        // reduce segment size in order to test the case where multiple segments come into the game
        BaseGraph gs = newGHStorage(new RAMDirectory(location), enabled3D, defaultSize / 2);
        gs.create(defaultSize);
        return gs;
    }

    protected BaseGraph createGHStorage() {
        BaseGraph g = createGHStorage(defaultGraphLoc, false);
        carOutExplorer = g.createEdgeExplorer(carOutFilter);
        carInExplorer = g.createEdgeExplorer(carInFilter);
        carAllExplorer = g.createEdgeExplorer();
        return g;
    }

    protected BaseGraph newGHStorage(Directory dir, boolean enabled3D) {
        return newGHStorage(dir, enabled3D, -1);
    }

    protected BaseGraph newGHStorage(Directory dir, boolean enabled3D, int segmentSize) {
        return new BaseGraph.Builder(encodingManager).setDir(dir).set3D(enabled3D).setSegmentSize(segmentSize).build();
    }

    @Test
    public void testEdgeStuff() {
        assertEquals(2, GHUtility.createEdgeKey(1, false));
        assertEquals(3, GHUtility.createEdgeKey(1, true));
    }

    @Test
    public void testZeroValue() {
        GHIntLongHashMap map1 = new GHIntLongHashMap();
        assertFalse(map1.containsKey(0));
        // assertFalse(map1.containsValue(0));
        map1.put(0, 3);
        map1.put(1, 0);
        map1.put(2, 1);

        // assertTrue(map1.containsValue(0));
        assertEquals(3, map1.get(0));
        assertEquals(0, map1.get(1));
        assertEquals(1, map1.get(2));

        // instead of assertEquals(-1, map1.get(3)); with hppc we have to check before:
        assertTrue(map1.containsKey(0));

        // trove4j behaviour was to return -1 if non existing:
//        TIntLongHashMap map2 = new TIntLongHashMap(100, 0.7f, -1, -1);
//        assertFalse(map2.containsKey(0));
//        assertFalse(map2.containsValue(0));
//        map2.add(0, 3);
//        map2.add(1, 0);
//        map2.add(2, 1);
//        assertTrue(map2.containsKey(0));
//        assertTrue(map2.containsValue(0));
//        assertEquals(3, map2.get(0));
//        assertEquals(0, map2.get(1));
//        assertEquals(1, map2.get(2));
//        assertEquals(-1, map2.get(3));
    }

    /**
     * testCommonNodeTrue1
     * Vérifie que getCommonNode détecte correctement un nœud commun entre deux arêtes.
     * lorsque le noeud en commun est la base node
     * Données choisies : arêtes (0->1) et (0->2) partagent le nœud 0.
     * le nœud commun est 0, qui est le de départ de edge1 et edge2.
     */
    @Test
    public void testCommonNodeTrue1() {
        BaseGraph graph = createGHStorage();
        EdgeIteratorState edge1 = graph.edge(0, 1);  // Edge 0->1
        EdgeIteratorState edge2 = graph.edge(edge1.getBaseNode(), 2);  // Edge 0->2
        assertEquals(0, (getCommonNode(graph, edge1.getEdge(), edge2.getEdge())));

    }

    /**
     * testCommonNodeTrue2
     * Vérifie que getCommonNode détecte correctement un nœud commun entre deux arêtes.
     * lorsque le noeud en commun est adjacent
     * Données choisies : arêtes (0->1) et (2->1) partagent le nœud 1.
     * le nœud commun est 1, qui est le nœud d’arrivée de edge1 et de départ de edge2.
     */
    @Test
    public void testCommonNodeTrue2() {
        BaseGraph graph = createGHStorage();
        EdgeIteratorState edge1 = graph.edge(0, 1);  // Edge 0->1
        EdgeIteratorState edge2 = graph.edge(2, edge1.getAdjNode());  // Edge 2->1
        assertEquals(1, (getCommonNode(graph, edge1.getEdge(), edge2.getEdge())));

    }


    /**
     * testCommonNodeFalse
     * Vérifie que getCommonNode lance une exception si les arêtes ne partagent aucun nœud.
     * Données choisies : arêtes (0->1) et (2->3) n’ont aucun nœud en commun
     * IllegalArgumentException attendue car aucun nœud commun.
     */
    @Test
    public void testCommonNodeFalse() {
        BaseGraph graph = createGHStorage();
        EdgeIteratorState edge1 = graph.edge(0, 1);  // Edge 0-1
        EdgeIteratorState edge2 = graph.edge(edge1.getBaseNode(), 2);  // Edge 0-2
        EdgeIteratorState edge3 = graph.edge(edge2.getAdjNode(),3); //2-3
        assertThrows(IllegalArgumentException.class, ()-> getCommonNode(graph, edge1.getEdge(), edge3.getEdge()));

    }


    /**
     * TestCircularEdge
     * Vérifie que l’insertion d’une arête circulaire (0->0) est interdite.
     * tentative d’ajout d’une arête de 0 vers 0.
     * IllegalArgumentException attendue car les arêtes circulaires sont invalides, getCommonNode retournera une exection.
     */
    @Test
    public void TestCircularEdge() {
        BaseGraph graph = createGHStorage();
        EdgeIteratorState edge2 = graph.edge(0, 1);  // Edge 1-2
        assertThrows(IllegalArgumentException.class, () -> getCommonNode(graph, graph.edge(0, 0).getEdge(), edge2.getEdge()));

    }


    /**
     * TestEdgeThatFormACircle
     * Vérifie que getCommonNode rejette les arêtes formant un cycle.
     * Données choisies : arêtes (0->1) et (1->0) forment un cycle.
     * IllegalArgumentException attendue car les cycles ne sont pas autorisés.
     * return
     */
    @Test
    public void TestEdgeThatFormACircle() {
        BaseGraph graph = createGHStorage();
        EdgeIteratorState edge1 = graph.edge(0, 1);
        EdgeIteratorState edge2 = graph.edge(1, 0);
        assertThrows(IllegalArgumentException.class, () -> getCommonNode(graph, edge1.getEdge(), edge2.getEdge()));
    }


    /**
     * testCommonNodeWithNonExistentEdge
     * Vérifie que getCommonNode rejette une arête inexistante.
     * Données choisies : arête valide (0->1) et identifiant d’arête inexistant (14).
     * IllegalArgumentException attendue car l’arête 14 n’existe pas.
     */
    @Test
    public void testCommonNodeWithNonExistentEdge() {
        BaseGraph graph = createGHStorage();
        EdgeIteratorState e1 = graph.edge(0, 1);
        assertThrows(IllegalArgumentException.class, () -> getCommonNode(graph, e1.getEdge(), 14));
    }


    /**
     * testCommonNodeSymmetry
     * Vérifie que getCommonNode est symétrique : l’ordre des arêtes ne change pas le résultat.
     * Données choisies : arêtes (0->1) et (1->2) partagent le nœud 1.
     * le nœud commun est le même peu importe l’ordre des arêtes.
     */
    @Test
    public void testCommonNodeSymmetry() {
        BaseGraph graph = createGHStorage();
        EdgeIteratorState e1 = graph.edge(0, 1);
        EdgeIteratorState e2 = graph.edge(1, 2);
        assertEquals(getCommonNode(graph, e1.getEdge(), e2.getEdge()), getCommonNode(graph, e2.getEdge(), e1.getEdge()));
    }


}
