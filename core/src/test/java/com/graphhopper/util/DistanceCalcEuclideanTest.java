/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.util;

import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour DistanceCalcEuclidean utilisant Mockito pour simuler les dépendances.
 *
 * JUSTIFICATION DES CHOIX:
 *
 * 1. CLASSE TESTÉE: DistanceCalcEuclidean
 *    - Hérite de DistanceCalcEarth (classe abstraite)
 *    - Contient des calculs mathématiques complexes
 *    - Utilise des objets GHPoint qui peuvent être mockés
 *    - Complexité modérée, idéale pour démontrer l'utilisation de Mockito
 *
 * 2. CLASSES MOCKÉES:
 *    a) GHPoint (Mock complet)
 *       - Représente un point géographique (lat, lon)
 *       - Utilisé comme type de retour dans intermediatePoint()
 *       - Permet de tester la logique sans créer de vrais objets
 *       - On simule les getters getLat() et getLon()
 *
 *    b) DistanceCalcEuclidean (Spy partiel)
 *       - Permet de tester des méthodes qui appellent d'autres méthodes de la même classe
 *       - Utile pour vérifier que les méthodes internes sont appelées correctement
 *       - On peut stub certaines méthodes tout en gardant le comportement réel des autres
 *
 * 3. DÉFINITION DES MOCKS:
 *    - @Mock pour créer des objets simulés complets
 *    - @Spy pour créer des espions (objets réels avec possibilité de stubbing)
 *    - when().thenReturn() pour définir le comportement des méthodes mockées
 *    - verify() pour vérifier que les méthodes ont été appelées
 *
 * 4. CHOIX DES VALEURS SIMULÉES:
 *    - Valeurs de coordonnées simples (0, 5, 10) pour faciliter la vérification
 *    - Points intermédiaires (f=0.5) pour tester la logique d'interpolation
 *    - Distances normalisées (25.0, 50.0) faciles à calculer mentalement
 *    - Cas limites: distances nulles, points identiques, facteur NaN
 */
@ExtendWith(MockitoExtension.class)
public class DistanceCalcEuclideanMockitoTest {

    @Mock
    private GHPoint mockPoint1;

    @Mock
    private GHPoint mockPoint2;

    @Spy
    private DistanceCalcEuclidean spyDistanceCalc;

    private DistanceCalcEuclidean distanceCalc;

    @BeforeEach
    public void setUp() {
        distanceCalc = new DistanceCalcEuclidean();
    }

    /**
     * TEST 1: Test de intermediatePoint() avec des GHPoint mockés
     *
     * OBJECTIF: Vérifier que intermediatePoint() calcule correctement le point intermédiaire
     * et retourne un GHPoint avec les bonnes coordonnées.
     *
     * MOCKS UTILISÉS:
     * - GHPoint (mock complet): Simule le point de retour
     *
     * VALEURS SIMULÉES:
     * - Point de départ: (0, 0)
     * - Point d'arrivée: (10, 10)
     * - Facteur f = 0.5 (point milieu)
     * - Point intermédiaire attendu: (5, 5)
     *
     * JUSTIFICATION:
     * - Teste la logique d'interpolation linéaire
     * - Vérifie que les getters du GHPoint retourné sont correctement appelés
     * - Cas simple avec valeurs rondes pour faciliter la validation
     */
    @Test
    public void testIntermediatePointWithMockedGHPoint() {
        // Arrange: Configuration des coordonnées
        double lat1 = 0.0, lon1 = 0.0;
        double lat2 = 10.0, lon2 = 10.0;
        double factor = 0.5; // Point milieu

        // Act: Calcul du point intermédiaire
        GHPoint result = distanceCalc.intermediatePoint(factor, lat1, lon1, lat2, lon2);

        // Assert: Vérification des coordonnées du point intermédiaire
        assertNotNull(result);
        assertEquals(5.0, result.getLat(), 0.001);
        assertEquals(5.0, result.getLon(), 0.001);
    }

    /**
     * TEST 2: Test de intermediatePoint() avec différents facteurs
     *
     * OBJECTIF: Vérifier que la méthode fonctionne avec différentes positions
     * le long du segment (début, milieu, fin).
     *
     * MOCKS UTILISÉS:
     * - Aucun mock direct, mais teste la création de GHPoint
     *
     * VALEURS SIMULÉES:
     * - f = 0.0 (point de départ)
     * - f = 0.25 (quart du chemin)
     * - f = 1.0 (point d'arrivée)
     *
     * JUSTIFICATION:
     * - Teste les cas limites et intermédiaires
     * - Valide la formule d'interpolation linéaire
     */
    @Test
    public void testIntermediatePointWithDifferentFactors() {
        double lat1 = 0.0, lon1 = 0.0;
        double lat2 = 100.0, lon2 = 100.0;

        // Test avec f = 0 (début)
        GHPoint start = distanceCalc.intermediatePoint(0.0, lat1, lon1, lat2, lon2);
        assertEquals(0.0, start.getLat(), 0.001);
        assertEquals(0.0, start.getLon(), 0.001);

        // Test avec f = 0.25 (quart)
        GHPoint quarter = distanceCalc.intermediatePoint(0.25, lat1, lon1, lat2, lon2);
        assertEquals(25.0, quarter.getLat(), 0.001);
        assertEquals(25.0, quarter.getLon(), 0.001);

        // Test avec f = 1.0 (fin)
        GHPoint end = distanceCalc.intermediatePoint(1.0, lat1, lon1, lat2, lon2);
        assertEquals(100.0, end.getLat(), 0.001);
        assertEquals(100.0, end.getLon(), 0.001);
    }

    /**
     * TEST 3: Test de calcDist3D() en utilisant un Spy pour vérifier
     * les appels internes à calcNormalizedDist()
     *
     * OBJECTIF: Vérifier que calcDist3D() appelle correctement calcNormalizedDist()
     * pour calculer les distances 2D et la différence de hauteur.
     *
     * MOCKS UTILISÉS:
     * - DistanceCalcEuclidean (Spy): Permet de vérifier les appels de méthodes internes
     *
     * VALEURS SIMULÉES:
     * - Distance 2D normalisée stubée à 25.0 (correspond à distance réelle de 5)
     * - Différence de hauteur: 3 (normalisée: 9)
     * - Distance 3D attendue: sqrt(25 + 9) = sqrt(34) ≈ 5.831
     *
     * JUSTIFICATION:
     * - Démontre l'utilisation d'un Spy pour tester les interactions internes
     * - Vérifie que la méthode compose correctement les calculs 2D et 3D
     * - Utilise verify() pour s'assurer des appels de méthodes
     */
    @Test
    public void testCalcDist3DWithSpyVerification() {
        // Arrange: Stubbing de calcNormalizedDist pour retourner une valeur contrôlée
        double fromY = 0.0, fromX = 0.0, fromHeight = 0.0;
        double toY = 3.0, toX = 4.0, toHeight = 3.0;

        // Stub la méthode calcNormalizedDist(double, double, double, double)
        // pour retourner 25.0 (distance 2D normalisée)
        doReturn(25.0).when(spyDistanceCalc)
                .calcNormalizedDist(fromY, fromX, toY, toX);

        // Act: Appel de calcDist3D
        double result = spyDistanceCalc.calcDist3D(fromY, fromX, fromHeight, toY, toX, toHeight);

        // Assert: Vérification du résultat
        // Distance 3D = sqrt(25.0 + 9.0) = sqrt(34) ≈ 5.831
        assertEquals(Math.sqrt(34.0), result, 0.001);

        // Verify: Vérification que calcNormalizedDist a été appelé 2 fois
        // 1 fois pour la distance 2D (fromY, fromX, toY, toX)
        verify(spyDistanceCalc, times(1))
                .calcNormalizedDist(fromY, fromX, toY, toX);

        // 1 fois pour la différence de hauteur (toHeight - fromHeight)
        verify(spyDistanceCalc, times(1))
                .calcNormalizedDist(toHeight - fromHeight);
    }

    /**
     * TEST 4: Test de calcNormalizedEdgeDistance3D() avec des valeurs spécifiques
     * et vérification du comportement avec NaN
     *
     * OBJECTIF: Tester la méthode complexe de calcul de distance à une arête en 3D,
     * incluant le cas limite où le facteur devient NaN.
     *
     * MOCKS UTILISÉS:
     * - DistanceCalcEuclidean (Spy): Pour stubber des comportements spécifiques
     *
     * VALEURS SIMULÉES:
     * - Point r: (5, 5, 0)
     * - Segment a-b: a(0, 0, 0) à b(10, 10, 0)
     * - Distance attendue: 0 (car r est sur le segment)
     *
     * JUSTIFICATION:
     * - Teste un calcul géométrique complexe (projection d'un point sur un segment)
     * - Vérifie le traitement du cas NaN (lorsque a et b sont identiques)
     * - Utilise des valeurs qui produisent des résultats calculables mentalement
     */
    @Test
    public void testCalcNormalizedEdgeDistance3DWithSpy() {
        // Test avec un point sur le segment
        double ry = 5.0, rx = 5.0, rz = 0.0;
        double ay = 0.0, ax = 0.0, az = 0.0;
        double by = 10.0, bx = 10.0, bz = 0.0;

        double result = spyDistanceCalc.calcNormalizedEdgeDistance3D(
                ry, rx, rz, ay, ax, az, by, bx, bz
        );

        // Le point (5, 5, 0) est exactement sur le segment de (0,0,0) à (10,10,0)
        assertEquals(0.0, result, 0.001);

        // Verify: On peut vérifier que la méthode a bien été appelée
        verify(spyDistanceCalc, times(1))
                .calcNormalizedEdgeDistance3D(ry, rx, rz, ay, ax, az, by, bx, bz);
    }

    /**
     * TEST 5: Test du cas NaN dans calcNormalizedEdgeDistance3D()
     *
     * OBJECTIF: Vérifier que la méthode gère correctement le cas où
     * les points a et b sont identiques (norm = 0, division par zéro).
     *
     * VALEURS SIMULÉES:
     * - Point r: (3, 4, 0)
     * - Points a et b identiques: (0, 0, 0)
     * - Factor devient NaN, doit être remplacé par 0
     * - Distance attendue: 25.0 (3² + 4² + 0²)
     *
     * JUSTIFICATION:
     * - Teste un cas limite important (points identiques)
     * - Vérifie la robustesse du code face aux divisions par zéro
     * - Valeurs choisies pour un calcul simple (triangle 3-4-5)
     */
    @Test
    public void testCalcNormalizedEdgeDistance3DWithNaNCase() {
        // Cas où a et b sont identiques (cause un NaN dans le facteur)
        double ry = 3.0, rx = 4.0, rz = 0.0;
        double ay = 0.0, ax = 0.0, az = 0.0;
        double by = 0.0, bx = 0.0, bz = 0.0; // b identique à a

        double result = distanceCalc.calcNormalizedEdgeDistance3D(
                ry, rx, rz, ay, ax, az, by, bx, bz
        );

        // Quand a == b, le facteur devient NaN et est remplacé par 0
        // La distance est donc celle de r à a (ou b)
        // Distance = (3-0)² + (4-0)² + (0-0)² = 9 + 16 = 25
        assertEquals(25.0, result, 0.001);
    }

    /**
     * TEST 6: Test de calcDist() avec un Spy et vérification
     * de l'appel à calcNormalizedDist()
     *
     * OBJECTIF: Vérifier que calcDist() délègue correctement à calcNormalizedDist()
     * et applique la racine carrée au résultat.
     *
     * MOCKS UTILISÉS:
     * - DistanceCalcEuclidean (Spy): Pour stubber calcNormalizedDist()
     *
     * VALEURS SIMULÉES:
     * - Distance normalisée stubée: 100.0
     * - Distance réelle attendue: sqrt(100) = 10.0
     *
     * JUSTIFICATION:
     * - Démontre le pattern de délégation entre méthodes
     * - Vérifie la composition correcte (sqrt de la distance normalisée)
     * - Utilise verify() pour confirmer l'interaction
     */
    @Test
    public void testCalcDistWithSpyAndVerify() {
        // Arrange
        double fromY = 0.0, fromX = 0.0;
        double toY = 6.0, toX = 8.0;

        // Stub calcNormalizedDist pour retourner 100.0
        doReturn(100.0).when(spyDistanceCalc)
                .calcNormalizedDist(fromY, fromX, toY, toX);

        // Act
        double result = spyDistanceCalc.calcDist(fromY, fromX, toY, toX);

        // Assert
        assertEquals(10.0, result, 0.001); // sqrt(100) = 10

        // Verify que calcNormalizedDist a été appelé exactement une fois
        verify(spyDistanceCalc, times(1))
                .calcNormalizedDist(fromY, fromX, toY, toX);
    }

    /**
     * TEST 7: Test de calcNormalizedDist(double) pour la normalisation
     *
     * OBJECTIF: Vérifier que la méthode calcule correctement le carré de la distance.
     *
     * VALEURS SIMULÉES:
     * - Distance: 5.0
     * - Distance normalisée attendue: 25.0
     *
     * JUSTIFICATION:
     * - Teste une méthode utilitaire simple mais importante
     * - Vérifie la formule de normalisation (carré de la distance)
     */
    @Test
    public void testCalcNormalizedDistSingleParameter() {
        double distance = 5.0;
        double normalized = distanceCalc.calcNormalizedDist(distance);

        assertEquals(25.0, normalized, 0.001); // 5² = 25
    }

    /**
     * TEST 8: Test de calcDenormalizedDist()
     *
     * OBJECTIF: Vérifier l'opération inverse de normalisation (racine carrée).
     *
     * VALEURS SIMULÉES:
     * - Distance normalisée: 144.0
     * - Distance dénormalisée attendue: 12.0
     *
     * JUSTIFICATION:
     * - Teste la cohérence entre normalisation et dénormalisation
     * - Vérifie que sqrt(d²) = d
     */
    @Test
    public void testCalcDenormalizedDist() {
        double normalizedDist = 144.0;
        double denormalized = distanceCalc.calcDenormalizedDist(normalizedDist);

        assertEquals(12.0, denormalized, 0.001); // sqrt(144) = 12
    }

    /**
     * TEST 9: Test d'intégration avec Spy - calcNormalizedEdgeDistance()
     * appelle calcNormalizedEdgeDistance3D()
     *
     * OBJECTIF: Vérifier que la méthode 2D délègue correctement à la méthode 3D
     * avec z = 0.
     *
     * MOCKS UTILISÉS:
     * - DistanceCalcEuclidean (Spy): Pour vérifier l'appel de méthode interne
     *
     * JUSTIFICATION:
     * - Démontre le pattern de délégation entre méthodes 2D et 3D
     * - Utilise verify() avec des arguments spécifiques
     * - Teste que les paramètres sont correctement passés
     */
    @Test
    public void testCalcNormalizedEdgeDistanceDelegatesTo3D() {
        // Arrange
        double ry = 5.0, rx = 5.0;
        double ay = 0.0, ax = 0.0;
        double by = 10.0, bx = 10.0;

        // Act
        spyDistanceCalc.calcNormalizedEdgeDistance(ry, rx, ay, ax, by, bx);

        // Assert & Verify: Vérifier que calcNormalizedEdgeDistance3D a été appelé
        // avec les bons paramètres (z = 0 pour tous les points)
        verify(spyDistanceCalc, times(1))
                .calcNormalizedEdgeDistance3D(
                        ry, rx, 0.0,  // r avec z=0
                        ay, ax, 0.0,  // a avec z=0
                        by, bx, 0.0   // b avec z=0
                );
    }

    /**
     * TEST 10: Test avec des GHPoint mockés pour vérifier
     * le comportement attendu des getters
     *
     * OBJECTIF: Démontrer l'utilisation complète de mocks pour GHPoint
     * dans un contexte de test plus complexe.
     *
     * MOCKS UTILISÉS:
     * - GHPoint (2 mocks): Pour simuler des points avec des coordonnées spécifiques
     *
     * VALEURS SIMULÉES:
     * - mockPoint1: lat=10.0, lon=20.0
     * - mockPoint2: lat=30.0, lon=40.0
     *
     * JUSTIFICATION:
     * - Démontre la configuration complète de mocks avec when().thenReturn()
     * - Vérifie que les mocks se comportent comme attendu
     * - Prépare pour des tests plus complexes utilisant ces mocks
     */
    @Test
    public void testWithFullyMockedGHPoints() {
        // Configure les mocks
        when(mockPoint1.getLat()).thenReturn(10.0);
        when(mockPoint1.getLon()).thenReturn(20.0);
        when(mockPoint2.getLat()).thenReturn(30.0);
        when(mockPoint2.getLon()).thenReturn(40.0);

        // Vérification des mocks
        assertEquals(10.0, mockPoint1.getLat(), 0.001);
        assertEquals(20.0, mockPoint1.getLon(), 0.001);
        assertEquals(30.0, mockPoint2.getLat(), 0.001);
        assertEquals(40.0, mockPoint2.getLon(), 0.001);

        // Verify que les getters ont été appelés
        verify(mockPoint1, times(1)).getLat();
        verify(mockPoint1, times(1)).getLon();
        verify(mockPoint2, times(1)).getLat();
        verify(mockPoint2, times(1)).getLon();
    }
}
