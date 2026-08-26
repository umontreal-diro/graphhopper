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
import org.junit.jupiter.api.Disabled;

import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class DistanceCalcEarthTest {
    private DistanceCalc dc = new DistanceCalcEarth();

    @Test
    public void testCalcCircumference() {
        assertEquals(DistanceCalcEarth.C, dc.calcCircumference(0), 1e-7);
    }

    @Test
    public void testDistance() {
        float lat = 24.235f;
        float lon = 47.234f;

        DistanceCalc approxDist = new DistancePlaneProjection();
        double res = 15051;
        assertEquals(res, dc.calcDist(lat, lon, lat - 0.1, lon + 0.1), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 0.1, lon + 0.1), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat - 0.1, lon + 0.1), 1);

        res = 15046;
        assertEquals(res, dc.calcDist(lat, lon, lat + 0.1, lon - 0.1), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 0.1, lon - 0.1), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat + 0.1, lon - 0.1), 1);

        res = 150748;
        assertEquals(res, dc.calcDist(lat, lon, lat - 1, lon + 1), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 1, lon + 1), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat - 1, lon + 1), 10);

        res = 150211;
        assertEquals(res, dc.calcDist(lat, lon, lat + 1, lon - 1), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 1, lon - 1), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat + 1, lon - 1), 10);

        res = 1527919;
        assertEquals(res, dc.calcDist(lat, lon, lat - 10, lon + 10), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 10, lon + 10), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat - 10, lon + 10), 10000);

        res = 1474016;
        assertEquals(res, dc.calcDist(lat, lon, lat + 10, lon - 10), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 10, lon - 10), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat + 10, lon - 10), 10000);

        res = 1013735.28;
        assertEquals(res, dc.calcDist(lat, lon, lat, lon - 10), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat, lon - 10), 1);
        // 1013952.659
        assertEquals(res, approxDist.calcDist(lat, lon, lat, lon - 10), 1000);

        // if we have a big distance for latitude only then PlaneProjection is exact!!
        res = 1111949.3;
        assertEquals(res, dc.calcDist(lat, lon, lat + 10, lon), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 10, lon), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat + 10, lon), 1);
    }

    @Test
    public void testEdgeDistance() {
        double dist = dc.calcNormalizedEdgeDistance(49.94241, 11.544356,
                49.937964, 11.541824,
                49.942272, 11.555643);
        double expectedDist = dc.calcNormalizedDist(49.94241, 11.544356,
                49.9394, 11.54681);
        assertEquals(expectedDist, dist, 1e-4);

        // test identical lats
        dist = dc.calcNormalizedEdgeDistance(49.936299, 11.543992,
                49.9357, 11.543047,
                49.9357, 11.549227);
        expectedDist = dc.calcNormalizedDist(49.936299, 11.543992,
                49.9357, 11.543992);
        assertEquals(expectedDist, dist, 1e-4);
    }

    @Test
    public void testEdgeDistance3d() {
        double dist = dc.calcNormalizedEdgeDistance3D(49.94241, 11.544356, 0,
                49.937964, 11.541824, 0,
                49.942272, 11.555643, 0);
        double expectedDist = dc.calcNormalizedDist(49.94241, 11.544356,
                49.9394, 11.54681);
        assertEquals(expectedDist, dist, 1e-4);

        // test identical lats
        dist = dc.calcNormalizedEdgeDistance3D(49.936299, 11.543992, 0,
                49.9357, 11.543047, 0,
                49.9357, 11.549227, 0);
        expectedDist = dc.calcNormalizedDist(49.936299, 11.543992,
                49.9357, 11.543992);
        assertEquals(expectedDist, dist, 1e-4);
    }

    @Test
    public void testEdgeDistance3dEarth() {
        double dist = dc.calcNormalizedEdgeDistance3D(0, 0.5, 10,
                0, 0, 0,
                0, 1, 0);
        assertEquals(10, dc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testEdgeDistance3dEarthNaN() {
        double dist = dc.calcNormalizedEdgeDistance3D(0, 0.5, Double.NaN,
                0, 0, 0,
                0, 1, 0);
        assertEquals(0, dc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testEdgeDistance3dPlane() {
        DistanceCalc calc = new DistancePlaneProjection();
        double dist = calc.calcNormalizedEdgeDistance3D(0, 0.5, 10,
                0, 0, 0,
                0, 1, 0);
        assertEquals(10, calc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testEdgeDistanceStartEndSame() {
        DistanceCalc calc = new DistancePlaneProjection();
        // just change elevation
        double dist = calc.calcNormalizedEdgeDistance3D(0, 0, 10,
                0, 0, 0,
                0, 0, 0);
        assertEquals(10, calc.calcDenormalizedDist(dist), 1e-4);
        // just change lat
        dist = calc.calcNormalizedEdgeDistance3D(1, 0, 0,
                0, 0, 0,
                0, 0, 0);
        assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
        // just change lon
        dist = calc.calcNormalizedEdgeDistance3D(0, 1, 0,
                0, 0, 0,
                0, 0, 0);
        assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testEdgeDistanceStartEndDifferentElevation() {
        DistanceCalc calc = new DistancePlaneProjection();
        // just change elevation
        double dist = calc.calcNormalizedEdgeDistance3D(0, 0, 10,
                0, 0, 0,
                0, 0, 1);
        assertEquals(0, calc.calcDenormalizedDist(dist), 1e-4);
        // just change lat
        dist = calc.calcNormalizedEdgeDistance3D(1, 0, 0,
                0, 0, 0,
                0, 0, 1);
        assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
        // just change lon
        dist = calc.calcNormalizedEdgeDistance3D(0, 1, 0,
                0, 0, 0,
                0, 0, 1);
        assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testValidEdgeDistance() {
        assertTrue(dc.validEdgeDistance(49.94241, 11.544356, 49.937964, 11.541824, 49.942272, 11.555643));
        assertTrue(dc.validEdgeDistance(49.936624, 11.547636, 49.937964, 11.541824, 49.942272, 11.555643));
        assertTrue(dc.validEdgeDistance(49.940712, 11.556069, 49.937964, 11.541824, 49.942272, 11.555643));

        // left bottom of the edge
        assertFalse(dc.validEdgeDistance(49.935119, 11.541649, 49.937964, 11.541824, 49.942272, 11.555643));
        // left top of the edge
        assertFalse(dc.validEdgeDistance(49.939317, 11.539675, 49.937964, 11.541824, 49.942272, 11.555643));
        // right top of the edge
        assertFalse(dc.validEdgeDistance(49.944482, 11.555446, 49.937964, 11.541824, 49.942272, 11.555643));
        // right bottom of the edge
        assertFalse(dc.validEdgeDistance(49.94085, 11.557356, 49.937964, 11.541824, 49.942272, 11.555643));

        // rounding error
        // assertFalse(dc.validEdgeDistance(0.001, 0.001, 0.001, 0.002, 0.00099987, 0.00099987));
    }

    @Test
    public void testPrecisionBug() {
        DistanceCalc dist = new DistancePlaneProjection();
//        DistanceCalc dist = new DistanceCalc();
        double queryLat = 42.56819, queryLon = 1.603231;
        double lat16 = 42.56674481705006, lon16 = 1.6023790821964834;
        double lat17 = 42.56694505140808, lon17 = 1.6020622462495173;
        double lat18 = 42.56715199128878, lon18 = 1.601682266630581;

        // segment 18
        assertEquals(171.487, dist.calcDist(queryLat, queryLon, lat18, lon18), 1e-3);
        // segment 17
        assertEquals(168.298, dist.calcDist(queryLat, queryLon, lat17, lon17), 1e-3);
        // segment 16
        assertEquals(175.188, dist.calcDist(queryLat, queryLon, lat16, lon16), 1e-3);

        assertEquals(167.385, dist.calcDenormalizedDist(dist.calcNormalizedEdgeDistance(queryLat, queryLon, lat16, lon16, lat17, lon17)), 1e-3);

        assertEquals(168.213, dist.calcDenormalizedDist(dist.calcNormalizedEdgeDistance(queryLat, queryLon, lat17, lon17, lat18, lon18)), 1e-3);

        // 16_17
        assertEquals(new GHPoint(42.567048, 1.6019), dist.calcCrossingPointToEdge(queryLat, queryLon, lat16, lon16, lat17, lon17));
        // 17_18
        // assertEquals(new GHPoint(42.566945,1.602062), dist.calcCrossingPointToEdge(queryLat, queryLon, lat17, lon17, lat18, lon18));
    }

    @Test
    public void testPrecisionBug2() {
        DistanceCalc distCalc = new DistancePlaneProjection();
        double queryLat = 55.818994, queryLon = 37.595354;
        double tmpLat = 55.81777239183573, tmpLon = 37.59598350366913;
        double wayLat = 55.818839128736535, wayLon = 37.5942968784488;
        assertEquals(68.25, distCalc.calcDist(wayLat, wayLon, queryLat, queryLon), .1);

        assertEquals(60.88, distCalc.calcDenormalizedDist(distCalc.calcNormalizedEdgeDistance(queryLat, queryLon,
                tmpLat, tmpLon, wayLat, wayLon)), .1);

        assertEquals(new GHPoint(55.81863, 37.594626), distCalc.calcCrossingPointToEdge(queryLat, queryLon,
                tmpLat, tmpLon, wayLat, wayLon));
    }

    @Test
    public void testDistance3dEarth() {
        DistanceCalc distCalc = new DistanceCalcEarth();
        assertEquals(1, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 1
        ), 1e-6);
    }

    @Test
    public void testDistance3dEarthNaN() {
        DistanceCalc distCalc = new DistanceCalcEarth();
        assertEquals(0, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, Double.NaN
        ), 1e-6);
        assertEquals(0, distCalc.calcDist3D(
                0, 0, Double.NaN,
                0, 0, 10
        ), 1e-6);
        assertEquals(0, distCalc.calcDist3D(
                0, 0, Double.NaN,
                0, 0, Double.NaN
        ), 1e-6);
    }

    @Test
    public void testDistance3dPlane() {
        DistancePlaneProjection distCalc = new DistancePlaneProjection();
        assertEquals(1, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 1
        ), 1e-6);
        assertEquals(10, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 10
        ), 1e-6);
    }

    @Test
    public void testDistance3dPlaneNaN() {
        DistancePlaneProjection distCalc = new DistancePlaneProjection();
        assertEquals(0, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, Double.NaN
        ), 1e-6);
        assertEquals(0, distCalc.calcDist3D(
                0, 0, Double.NaN,
                0, 0, 10
        ), 1e-6);
        assertEquals(0, distCalc.calcDist3D(
                0, 0, Double.NaN,
                0, 0, Double.NaN
        ), 1e-6);
    }

    @Test
    public void testIntermediatePoint() {
        DistanceCalc distCalc = new DistanceCalcEarth();
        GHPoint point = distCalc.intermediatePoint(0, 0, 0, 0, 0);
        assertEquals(0, point.getLat(), 1e-5);
        assertEquals(0, point.getLon(), 1e-5);

        point = distCalc.intermediatePoint(0.5, 0, 0, 10, 0);
        assertEquals(5, point.getLat(), 1e-5);
        assertEquals(0, point.getLon(), 1e-5);

        point = distCalc.intermediatePoint(0.5, 0, 0, 0, 10);
        assertEquals(0, point.getLat(), 1e-5);
        assertEquals(5, point.getLon(), 1e-5);

        // cross international date line going west
        point = distCalc.intermediatePoint(0.5, 45, -179, 45, 177);
        assertEquals(45, point.getLat(), 1);
        assertEquals(179, point.getLon(), 1e-5);

        // cross international date line going east
        point = distCalc.intermediatePoint(0.5, 45, 179, 45, -177);
        assertEquals(45, point.getLat(), 1);
        assertEquals(-179, point.getLon(), 1e-5);

        // cross north pole
        point = distCalc.intermediatePoint(0.25, 45, -90, 45, 90);
        assertEquals(67.5, point.getLat(), 1e-1);
        assertEquals(-90, point.getLon(), 1e-5);
        point = distCalc.intermediatePoint(0.75, 45, -90, 45, 90);
        assertEquals(67.5, point.getLat(), 1e-1);
        assertEquals(90, point.getLon(), 1e-5);
    }

    /**
     * Nom du test: testCreateBBoxWithZeroRadius
     * Intention: Vérifier que la méthode createBBox lève une IllegalArgumentException
     *            lorsqu'elle est appelé avec un rayon nul.
     * Motivation des données: Un rayon de 0 n'a aucun sens physique pour une boîte englobante
     *                         Le test utilise ce cas pour s'assurer que la validation d'entrée
     *                         empêche toute création invalide
     * Oracle: Le comportement attendu est la levée d'une IllegalArgumentException, détectée par
     *         assertThrows()
     */
    @Test
    @Disabled
    public void testCreateBBoxWithZeroRadius() {
        assertThrows(IllegalArgumentException.class, () -> {
            dc.createBBox(50.0, 10.0, 0);
        });
    }


    /**
     * Nom du test: testCalcDist3DWithSameElevation
     * Intention: S'assurer que calcDist3D() retourne la même valeur que calcDist()
     *            lorsque les deux points ont la même altitude
     * Motivation des données: Deux points ayant la même élévation ne devraient pas introduire
     *                         de différence verticale dans le calcul 3D.
     * Oracle: La distance 3D et la distance 2D doivent être égales, comparées par assertEquals()
     */
    @Test
    @Disabled
    public void testCalcDist3DWithSameElevation() {
        double lat1 = 48.8, lon1 = 2.3;
        double lat2 = 41.9, lon2 = 12.5;
        double elevation = 100; // Altitude constante

        double dist2D = dc.calcDist(lat1, lon1, lat2, lon2);
        double dist3D = dc.calcDist3D(lat1, lon1, elevation, lat2, lon2, elevation);

        assertEquals(dist2D, dist3D, 1e-9);
    }

    /**
     * Nom du test: testCalcDist3DWithOneNaNElevation
     * Intention: Vérifier que la méthode calcDist3D() se comporte comme calcDist() lorsqu'une
     *            altitude est NaN
     * Motivation des données: Les données GPS peuvent contenir des altitudes manquantes. On
     *                         s'assure que la méthode gère ce cas sans erreur.
     * Oracle: La distance 3D doit être identique à la distance 2D.
     */
    @Test
    @Disabled
    public void testCalcDist3DWithOneNaNElevation() {
        double lat1 = 50.0, lon1 = 10.0, ele1 = 100;
        double lat2 = 50.1, lon2 = 10.1;

        double dist2D = dc.calcDist(lat1, lon1, lat2, lon2);
        double dist3D_nan = dc.calcDist3D(lat1, lon1, ele1, lat2, lon2, Double.NaN);

        assertEquals(dist2D, dist3D_nan, 1e-9);
    }

    /**
     * Nom du test: testCrossingPointOnHorizontalEdge
     * Intention: Vérifier la projection correcte d'un point sur une arête horizontale.
     * Motivation des données: Cas limite où la latitude est constante. Ce snénario suit une branche
     *                         spécifique du code (delta_lat == 0)
     * Oracle: Le point projeté doit avoir la même latitude que l'arête et la longitude du point
     *         de requête
     */
    @Test
    @Disabled
    public void testCrossingPointOnHorizontalEdge() {
        // Point r(1, 0.5), Arête a(0, 0) -> b(0, 1)
        GHPoint crossingPoint = dc.calcCrossingPointToEdge(1.0, 0.5, 0.0, 0.0, 0.0, 1.0);
        assertEquals(0.0, crossingPoint.getLat(), 1e-9);
        assertEquals(0.5, crossingPoint.getLon(), 1e-9);
    }

    /**
     * Nom du test: testCrossingPointOnVerticalEdge
     * Intention: Vérifier la projection correcte d'un point sur une arête verticale
     * Motivation des données: Cas limite où la longitude est constante (delta_lon == 0)
     * Oracle: Le point projeté doit avoir la même longitude que l'arête et la latitude au point
     *         de requête
     */
    @Test
    @Disabled
    public void testCrossingPointOnVerticalEdge() {
        // Point r(0.5, 1), Arête a(0, 0) -> b(1, 0)
        GHPoint crossingPoint = dc.calcCrossingPointToEdge(0.5, 1.0, 0.0, 0.0, 1.0, 0.0);
        assertEquals(0.5, crossingPoint.getLat(), 1e-9);
        assertEquals(0.0, crossingPoint.getLon(), 1e-9);
    }

    /**
     * Nom du test: testIsCrossBoundaryReturnsTrue
     * Intention: Vérifier que iscrossBoundary() détecte bien un passage de l'antiméridien
     * Motivation des données: On utilise des longitudes de part et d'autre de la ligne
     *                        de changement de date (-170 et 170). La différence absolue
     *                        (340) est supérieure à 300, donc la méthode doit retourner true.
     * Oracle: La méthode isCrossBoundary() doit retourner true.
     */
    @Disabled
    @Test
    public void testIsCrossBoundaryReturnsTrue() {
        assertTrue(dc.isCrossBoundary(170.0, -170.0));
    }

    /**
     * Nom du test: testIsCrossBoundaryReturnsFalse
     * Intention: S'assurer que l'heuristique isCrossBoundary ne signale pas de faux
     *            positif pour une grande distance qui ne traverse pas l'antiméridien.
     * Motivation des données: On utilise deux points très éloignés (Lisbonne et Moscou) mais
     *                        du même côté de la ligne de date. La différence de longitude est
     *                        grande (~47) mais bien inférieure à 300.
     * Oracle: La méthode isCrossBoundary() doit retourner false.
     */
    @Disabled
    @Test
    public void testIsCrossBoundaryReturnsFalse() {
        double lonLisbon = -9.1393;
        double lonMoscow = 37.6173;
        assertFalse(dc.isCrossBoundary(lonLisbon, lonMoscow));
    }

    /**
     * Nom du test : testCircumferenceAtLatitude
     * Intention : Vérifier la formule de circonférence à une latitude donnée.
     * Motivation des données : À 45°, cos(45°) ≠ 1, ce qui permet de détecter un
     *                          mutant remplaçant une multiplication par une division.
     * Oracle : La valeur attendue est 2πR·cos(45°).
     */
    @Test
    public void testCircumferenceAtLatitude() {
        double expected = 2 * Math.PI * DistanceCalcEarth.R * Math.cos(Math.toRadians(45));
        assertEquals(expected, dc.calcCircumference(45), 1e-7);
    }


    /**
     * Nom du test: testCalcDist3DWithPythagoras (Tueur de Mutant 2)
     * Intention: Vérifier la cohérence du calcul 3D via le théorème de Pythagore
     * Motivation des données: Un triangle 3-4-5 (4000m horizontaux, 3000m verticaux) permet
     *                         de tester la bonne combinaison quadratique
     * Oracle: La distance doit correspondre à l'hypoténuse d'un triangle 3-4-5, soit 5000m.
     */
    @Test
    public void testCalcDist3DWithPythagoras() {
        // Crée deux points séparés horizontalement par 4000m et verticalement par 3000m.
        // Pour simplifier, on trouve des coordonnées GPS qui donnent ~4000m
        double lat1 = 50.0, lon1 = 10.0, ele1 = 1000;
        // Un point à environ 4km au nord
        GHPoint point2 = dc.projectCoordinate(lat1, lon1, 4000, 0);
        double lat2 = point2.getLat();
        double lon2 = point2.getLon();
        double ele2 = ele1 + 3000; // 3km plus haut

        // La distance 3D doit être de 5000m (Pythagore : sqrt(4000^2 + 3000^2))
        assertEquals(5000.0, dc.calcDist3D(lat1, lon1, ele1, lat2, lon2, ele2), 1); // Tolérance de 1m
    }
}
