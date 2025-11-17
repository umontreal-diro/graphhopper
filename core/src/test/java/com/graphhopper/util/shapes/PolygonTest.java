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

package com.graphhopper.util.shapes;

//import com.github.javafaker.Faker;
import com.github.javafaker.Faker;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Robin Boldt
 */
public class PolygonTest {
    private static final GeometryFactory factory = new GeometryFactory();

    @Test
    public void testContains(){

        /*
         * |----|
         * |    |
         * |----|
         */
        Polygon square = new Polygon(new double[]{0,0,20,20}, new double[]{0,20,20,0});
        assertTrue(square.contains(10,10));
        assertTrue(square.contains(16,10));
        assertFalse(square.contains(10,-20));
        assertTrue(square.contains(10,0.1));
        assertFalse(square.contains(10,20));
        assertTrue(square.contains(10,16));
        assertFalse(square.contains(20,20));

        /*
         * \-----|
         *   --| |
         *   --| |
         *  /----|
         */
        Polygon squareHole = new Polygon(new double[]{0,0,20,20,15,15,5,5}, new double[]{0,20,20,0,5,15,15,5});
        assertFalse(squareHole.contains(10,10));
        assertTrue(squareHole.contains(16,10));
        assertFalse(squareHole.contains(10,-20));
        assertFalse(squareHole.contains(10,0));
        assertFalse(squareHole.contains(10,20));
        assertTrue(squareHole.contains(10,16));
        assertFalse(squareHole.contains(20,20));



        /*
         * |----|
         * |    |
         * |----|
         */
        square = new Polygon(new double[]{1, 1, 2, 2}, new double[]{1, 2, 2, 1});

        assertTrue(square.contains(1.5,1.5));
        assertFalse(square.contains(0.5,1.5));

        /*
         * |----|
         * | /\ |
         * |/  \|
         */
        squareHole = new Polygon(new double[]{1, 1, 2, 1.1, 2}, new double[]{1, 2, 2, 1.5, 1});

        assertTrue(squareHole.contains(1.1,1.1));
        assertFalse(squareHole.contains(1.5,1.5));
        assertFalse(squareHole.contains(0.5,1.5));

    }


    @Test
    public void testConstructorWithMismatchedArraysThrows() {
        double[] lats = {0, 1, 2};
        double[] lons = {0, 1};
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Polygon(lats, lons));
        assertTrue(ex.getMessage().contains("Points must be of equal length"));
    }

    @Test
    public void testConstructorWithEmptyArrayThrows() {
        double[] lats = {};
        double[] lons = {};
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Polygon(lats, lons));
        assertTrue(ex.getMessage().contains("Points must not be empty"));
    }

    @Test
    public void testGetBoundsAndMinMaxCoordinates() {
        Polygon square = new Polygon(new double[]{0, 0, 2, 2}, new double[]{0, 2, 2, 0});
        assertEquals(0, square.getMinLat(), 1e-6);
        assertEquals(0, square.getMinLon(), 1e-6);
        assertEquals(2, square.getMaxLat(), 1e-6);
        assertEquals(2, square.getMaxLon(), 1e-6);
        assertNotNull(square.getBounds());
    }

    @Test
    public void testIsRectangleTrue() {
        org.locationtech.jts.geom.Polygon rect = factory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        });
        Polygon p = Polygon.create(rect);
        assertTrue(p.isRectangle());
    }

    @Test
    public void testIsRectangleFalse() {
        org.locationtech.jts.geom.Polygon triangle = factory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(0.5, 1),
                new Coordinate(0, 0)
        });
        Polygon p = Polygon.create(triangle);
        assertFalse(p.isRectangle());
    }

    @Test
    public void testIntersectsReturnsTrue() {
        Polygon square = new Polygon(new double[]{0, 0, 1, 1}, new double[]{0, 1, 1, 0});
        PointList path = new PointList();
        path.add(0.5, 0.5);
        path.add(0.5, 2);
        path.makeImmutable();
        assertTrue(square.intersects(path));
    }

    @Test
    public void testIntersectsReturnsFalse() {
        Polygon square = new Polygon(new double[]{0, 0, 1, 1}, new double[]{0, 1, 1, 0});
        PointList path = new PointList();
        path.add(2, 2);
        path.add(3, 3);
        path.makeImmutable();
        assertFalse(square.intersects(path));
    }

    @Test
    public void testToStringContainsGeometryInfo() {
        Polygon square = new Polygon(new double[]{0, 0, 1, 1}, new double[]{0, 1, 1, 0});
        String s = square.toString();
        assertTrue(s.contains("polygon"));
        assertTrue(s.contains("points"));
        assertTrue(s.contains("geometries"));
    }

    @Test
    public void testStaticCreateMethod() {
        org.locationtech.jts.geom.Polygon jtsPoly = factory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        });
        Polygon p = Polygon.create(jtsPoly);
        assertNotNull(p);
        assertEquals(p.prepPolygon.getGeometry(), jtsPoly);
    }


    @Test
    public void testContainsWithRandomPointsUsingFaker() {
        Faker faker = new Faker();
        double[] lats = {0, 0, 10, 10};
        double[] lons = {0, 10, 10, 0};
        Polygon square = new Polygon(lats, lons);

        double lat = faker.number().randomDouble(4, 0, 10);
        double lon = faker.number().randomDouble(4, 0, 10);

        boolean inside = square.contains(lat, lon);
        assertEquals((lat >= 0 && lat <= 10 && lon >= 0 && lon <= 10), inside);
    }
}
