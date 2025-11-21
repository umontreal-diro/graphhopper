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

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Peter Karich
 */
@ExtendWith(MockitoExtension.class)
public class CircleTest {

    @Mock
    private DistanceCalc mockDistanceCalc;

    @Test
    public void testIntersectCircleBBox() {
        assertTrue(new Circle(10, 10, 120000).intersects(new BBox(9, 11, 8, 9)));

        assertFalse(new Circle(10, 10, 110000).intersects(new BBox(9, 11, 8, 9)));
    }

    @Test
    public void testIntersectPointList() {
        Circle circle = new Circle(1.5, 0.3, DistanceCalcEarth.DIST_EARTH.calcDist(0, 0, 0, 0.7));
        PointList pointList = new PointList();
        pointList.add(5, 5);
        pointList.add(5, 0);
        assertFalse(circle.intersects(pointList));

        pointList.add(-5, 0);
        assertTrue(circle.intersects(pointList));

        pointList = new PointList();
        pointList.add(5, 1);
        pointList.add(-1, 0);
        assertTrue(circle.intersects(pointList));

        pointList = new PointList();
        pointList.add(5, 0);
        pointList.add(-1, 3);
        assertFalse(circle.intersects(pointList));

        pointList = new PointList();
        pointList.add(5, 0);
        pointList.add(2, 0);
        assertTrue(circle.intersects(pointList));

        pointList = new PointList();
        pointList.add(1.5, -2);
        pointList.add(1.5, 2);
        assertTrue(circle.intersects(pointList));
    }

    @Test
    public void testContains() {
        Circle c = new Circle(10, 10, 120000);
        assertTrue(c.contains(new BBox(9, 11, 10, 10.1)));
        assertFalse(c.contains(new BBox(9, 11, 8, 9)));
        assertFalse(c.contains(new BBox(9, 12, 10, 10.1)));
    }

    @Test
    public void testContainsCircle() {
        Circle c = new Circle(10, 10, 120000);
        assertTrue(c.contains(new Circle(9.9, 10.2, 90000)));
        assertFalse(c.contains(new Circle(10, 10.4, 90000)));
    }

    @Test
    public void testContainsAvecMockDistanceCalc() {
        when(mockDistanceCalc.calcNormalizedDist(1000.0)).thenReturn(0.0001);
        when(mockDistanceCalc.createBBox(48.8566, 2.3522, 1000.0))
                .thenReturn(new BBox(2.3400, 2.3644, 48.8477, 48.8655));
        
        Circle circle = new Circle(48.8566, 2.3522, 1000.0, mockDistanceCalc);
        
        double testLat = 48.8600;
        double testLon = 2.3600;
        double normalizedDist = 0.00005;
        
        when(mockDistanceCalc.calcNormalizedDist(48.8566, 2.3522, testLat, testLon))
                .thenReturn(normalizedDist);

        assertTrue(circle.contains(testLat, testLon));
        verify(mockDistanceCalc).calcNormalizedDist(48.8566, 2.3522, testLat, testLon);
    }
}
