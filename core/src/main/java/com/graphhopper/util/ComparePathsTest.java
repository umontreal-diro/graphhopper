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

import com.graphhopper.util.GHUtility;
import com.graphhopper.routing.Path;
import com.graphhopper.storage.Graph;
import org.junit.jupiter.api.Test;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIndexedContainer;
import com.github.javafaker.Faker;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

// Zachary Bourgeois
public class ComparePathsTest {
    private final Faker faker = new Faker();

    @Mock
    private Path mockedRefPath;
    private Path mockedPath;
    private Graph mockedGraph;
    private Graph mockedOtherGraph;

    // Declaring all the values necessary in order to test all the branches of the
    // method "comparePaths"
    // Note: These values don't mean anything on their own

    // Reference properties (standard)
    private double weightMockRefPath;
    private IntIndexedContainer nodesMockRefPath = nodeFaker();
    private double distanceMockRefPath;
    private long timeMockRefPath;

    // Values used (to test limits and branches)
    private double limDiffWeight = 1.e-9;
    private double limDiffDistance = 1.e-9;
    private long limDiffTime = 50L;
    private IntIndexedContainer nodesExceptOneEdge = setupNodesExceptOneEdge();
    private IntIndexedContainer nodesExceptTwoEdges = setupNodesExceptTwoEdges();

    final int source = 1;
    final int target = 1;
    final long seed = 1L;

    // Create an IntIndexedContainer for when a Path mock calls the method
    // "calcNodes"
    private static IntIndexedContainer nodeFaker() {
        final IntArrayList nodes = new IntArrayList(16);
        for (int i = 1; i <= 10; i++) {
            nodes.add(i);
        }
        return nodes;
    }

    private static IntIndexedContainer setupNodesExceptOneEdge() {
        IntIndexedContainer nodes = nodeFaker();
        nodes.remove(2);

        return nodes;
    }

    private static IntIndexedContainer setupNodesExceptTwoEdges() {
        IntIndexedContainer nodes = nodeFaker();
        nodes.remove(4);
        nodes.remove(2); // 42 is the answer to life

        return nodes;
    }

    @BeforeEach
    public void setup() {
        mockedRefPath = Mockito.mock(Path.class);
        mockedPath = Mockito.mock(Path.class);

        mockedGraph = Mockito.mock(Graph.class);
        mockedOtherGraph = Mockito.mock(Graph.class);

        // Instantiate random values with Java-Faker
        weightMockRefPath = faker.number().randomDouble(0, 10, 100);
        distanceMockRefPath = faker.number().randomDouble(0, 10, 100);
        timeMockRefPath = faker.number().numberBetween(100L, 100000L);

        // Setup the mocked "refPath" and "path"
        // If the path we're testing is the exact same as the reference path
        // ("refPath"), then the method "comparePaths" should take
        // The happy path and we'll be able to modify the non-reference path in order
        // for our code to test all the different branches.
        when(mockedRefPath.getWeight()).thenReturn(weightMockRefPath);
        when(mockedPath.getWeight()).thenReturn(weightMockRefPath);
        when(mockedRefPath.calcNodes()).thenReturn(nodesMockRefPath);
        when(mockedPath.calcNodes()).thenReturn(nodesMockRefPath);
        when(mockedRefPath.getDistance()).thenReturn(distanceMockRefPath);
        when(mockedPath.getDistance()).thenReturn(distanceMockRefPath);
        when(mockedRefPath.getTime()).thenReturn(timeMockRefPath);
        when(mockedPath.getTime()).thenReturn(timeMockRefPath);
        when(mockedRefPath.getGraph()).thenReturn(mockedGraph);
        when(mockedPath.getGraph()).thenReturn(mockedGraph);
    }

    @Test
    public void testWeightDifferencePaths() {
        // First branch for the weight!
        // The difference in the weight of the two paths is lesser or equal than
        // limDiffWeight.
        // We can thus test the edge case when that difference is just inside the
        // boundary value (limDiffWeight).

        // Arrange
        when(mockedPath.getWeight()).thenReturn(weightMockRefPath + limDiffWeight);

        // Act and Assert
        assertDoesNotThrow(() -> {
            GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);
        }, "Should not throw an error if there's an insignificant difference in weight between the two paths!");

        // Second branch for the weight!
        // The difference in the weight of the two paths is greater than limDiffWeight.
        // We can thus test the edge case when that difference is just outside the
        // boundary value (limDiffWeight).

        // Arrange
        when(mockedPath.getWeight()).thenReturn(weightMockRefPath + limDiffWeight + 1);

        // Act and Assert
        assertThrows(AssertionError.class, () -> {
            GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);
        }, "Should throw an error if there's a notable weight difference between the two paths!");
    }

    @Test
    public void testDistanceDifferencePaths() {
        // First branch for the distance!
        // The difference in the distance of the two paths is lesser or equal than
        // limDiffDistance.
        // We can thus test the edge case when that difference is just inside the
        // boundary value (limDiffDistance).

        // Arrange
        when(mockedPath.getDistance()).thenReturn(distanceMockRefPath + limDiffDistance);

        // Act
        List<String> strictViolations = new ArrayList<>();
        strictViolations = GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);

        // Assert
        assertTrue(strictViolations.isEmpty(), "Should not have a strictViolation if there's an insignificant difference in distance between the two paths!");

        // Second branch for the distance!
        // The difference in the distance of the two paths is greater than
        // limDiffDistance.
        // We can thus test the edge case when that difference is just outside the
        // boundary value (limDiffDistance).

        // Arrange
        when(mockedPath.getDistance()).thenReturn(distanceMockRefPath + limDiffDistance + 1);

        // Act
        strictViolations.clear();
        strictViolations = GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);

        // Assert
        assertFalse(strictViolations.isEmpty(), "Should have a strictViolation if there's a notable distance difference between the two paths!");
    }

    @Test
    public void testTimeDifferencePaths() {
        // First branch for the time!
        // The time in the distance of the two paths is lesser or equal than
        // limDiffTime.
        // We can thus test the edge case when that difference is just inside the
        // boundary value (limDiffTime).

        // Arrange
        when(mockedPath.getTime()).thenReturn(timeMockRefPath + limDiffTime);

        // Act
        List<String> strictViolations = new ArrayList<>();
        strictViolations = GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);

        // Assert
        assertTrue(strictViolations.isEmpty(), "Should not have a strictViolation if there's an insignificant difference in time between the two paths!");

        // Second branch for the time!
        // The time in the distance of the two paths is greater than limDiffTime.
        // We can thus test the edge case when that difference is just outside the
        // boundary value (limDiffTime).

        // Arrange
        when(mockedPath.getTime()).thenReturn(timeMockRefPath + limDiffTime + 1);

        // Act
        strictViolations.clear();
        strictViolations = GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);

        // Assert
        assertFalse(strictViolations.isEmpty(), "Should have a strictViolation if there's a notable time difference between the two paths!");
    }

    @Test
    public void testGraphEqualityPaths() {
        // We isolate the method comparePaths from the other method it calls
        // ("pathsEqualExceptOneEdge").
        // Adding a try is apparently a good practice when mocking static methods, so I
        // added it here.
        try (MockedStatic<GHUtility> mockedGHUtility = Mockito.mockStatic(GHUtility.class)) {
            // First branch for the graphs!
            // We test for the case in which the two graphs are the same and there's one
            // edge different in one of the paths. (We need "refNodes" and "pathNodes" to be
            // different in order to test this path.)

            // Arrange
            mockedGHUtility.when(() -> GHUtility.pathsEqualExceptOneEdge(any(Graph.class),
                    any(IntIndexedContainer.class), any(IntIndexedContainer.class))).thenReturn(true);
            mockedGHUtility
                    .when(() -> GHUtility.comparePaths(any(Path.class), any(Path.class), anyInt(), anyInt(), anyLong()))
                    .thenCallRealMethod();
            mockedGHUtility
                    .when(() -> GHUtility.fail(anyString()))
                    .thenCallRealMethod();

            when(mockedPath.calcNodes()).thenReturn(nodesExceptOneEdge);

            // Act and Assert
            assertDoesNotThrow(() -> {
                GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);
            }, "Should not throw and error if there is no graph difference!");

            // Second branch for the graphs!
            // We test for the case in which the two graphs are the different and there's
            // one edge different in one of the paths. (We need "refNodes" and "pathNodes"
            // to be different in order to test this path.)

            // Arrange
            when(mockedPath.getGraph()).thenReturn(mockedOtherGraph);

            // Act and Assert
            assertThrows(AssertionError.class, () -> {
                GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);
            }, "Should throw and error if there is a graph difference!");
        }
    }

    @Test
    public void testExceptOneEdgePaths() {
        // We isolate the method comparePaths from the other method it calls
        // ("pathsEqualExceptOneEdge").
        try (MockedStatic<GHUtility> mockedGHUtility = Mockito.mockStatic(GHUtility.class)) {
            // First branch for the the call of the method pathsEqualExceptOneEdge!
            // We test the case in which we have one less node in "path" as opposed to
            // "refPath", which should NOT add any strictViolation.

            // Arrange
            mockedGHUtility.when(() -> GHUtility.pathsEqualExceptOneEdge(any(Graph.class),
                    any(IntIndexedContainer.class), any(IntIndexedContainer.class))).thenReturn(true);
            mockedGHUtility
                    .when(() -> GHUtility.comparePaths(any(Path.class), any(Path.class), anyInt(), anyInt(), anyLong()))
                    .thenCallRealMethod();

            when(mockedPath.calcNodes()).thenReturn(nodesExceptOneEdge);

            // Act
            List<String> strictViolations = new ArrayList<>();
            strictViolations = GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);

            // Assert
            assertTrue(strictViolations.isEmpty(), "Should not have any strictViolation if the paths are equal except for one edge!");

            // Second branch for the the call of the method pathsEqualExceptOneEdge!
            // We test the case in which we have one less node in "path" as opposed to
            // "refPath", which should NOT add any strictViolation.

            // Arrange
            mockedGHUtility.when(() -> GHUtility.pathsEqualExceptOneEdge(any(Graph.class),
                    any(IntIndexedContainer.class), any(IntIndexedContainer.class))).thenReturn(false);

            when(mockedPath.calcNodes()).thenReturn(nodesExceptTwoEdges);

            // Act
            strictViolations.clear();
            strictViolations = GHUtility.comparePaths(mockedRefPath, mockedPath, source, target, seed);

            // Assert
            assertFalse(strictViolations.isEmpty(), "Should have a strictViolation if there is a 2 or more edge difference between the two paths!");
        }
    }
}