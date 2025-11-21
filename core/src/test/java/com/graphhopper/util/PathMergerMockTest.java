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

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Tester PathMerger avec Graph et Weighting mockés
// Amir Hannache - Matricule 20060308
@ExtendWith(MockitoExtension.class)
public class PathMergerMockTest {

    @Mock
    private Graph mockGraph;

    @Mock
    private Weighting mockWeighting;

    // Tester la création de PathMerger avec Graph et Weighting mockés
    // Input: mockGraph et mockWeighting configurés
    // Output: PathMerger valide
    // Vérifie que PathMerger peut être créé avec des dépendances mockées
    // Exemple: new PathMerger(mockGraph, mockWeighting)
    @Test
    public void testCreatePathMergerWithMockedDependencies() {
        when(mockGraph.wrapWeighting(mockWeighting)).thenReturn(mockWeighting);

        PathMerger pathMerger = new PathMerger(mockGraph, mockWeighting);

        assertNotNull(pathMerger);
        verify(mockGraph).wrapWeighting(mockWeighting);
    }

    // Tester setCalcPoints avec Graph et Weighting mockés
    // Input: mockGraph, mockWeighting, calcPoints=false
    // Output: PathMerger configuré
    // Permet de tester la configuration sans créer de vraies dépendances
    // Exemple: setCalcPoints(false) avec mocks
    @Test
    public void testSetCalcPointsWithMockedDependencies() {
        when(mockGraph.wrapWeighting(mockWeighting)).thenReturn(mockWeighting);

        PathMerger pathMerger = new PathMerger(mockGraph, mockWeighting);
        PathMerger result = pathMerger.setCalcPoints(false);

        assertNotNull(result);
        assertSame(pathMerger, result);
        verify(mockGraph).wrapWeighting(mockWeighting);
    }
}
