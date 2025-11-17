package com.graphhopper.routing;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.exceptions.MaximumNodesExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour FlexiblePathCalculator avec Mockito
 * 
 * JUSTIFICATION DU CHOIX DE LA CLASSE:
 * FlexiblePathCalculator est un excellent candidat pour les tests avec mocks car:
 * 1. Il orchestre la création et l'exécution d'algorithmes de routage
 * 2. Il a plusieurs dépendances externes clairement définies (QueryGraph, RoutingAlgorithmFactory, Weighting)
 * 3. La logique métier peut être testée indépendamment des implémentations réelles
 * 4. Il gère des cas limites importants (nodes max exceeded, edge restrictions)
 * 
 * CLASSES SIMULÉES:
 * 1. QueryGraph - Mock: Représente le graphe de requête. Simulé car créer un vrai graphe 
 *    nécessiterait beaucoup de setup. Le mock permet de tester uniquement la logique 
 *    de FlexiblePathCalculator.
 * 
 * 2. RoutingAlgorithmFactory - Mock: Fabrique d'algorithmes de routage. Simulé pour 
 *    contrôler précisément quel algorithme est créé et tester différents scénarios.
 * 
 * 3. RoutingAlgorithm - Mock: L'algorithme de routage lui-même. Simulé pour:
 *    - Tester les différents résultats possibles (succès, échec, dépassement de nodes)
 *    - Vérifier que le calculator appelle correctement l'algorithme
 *    - Simuler des conditions spécifiques sans exécuter de vrais calculs de chemins
 * 
 * 4. Weighting - Mock: La pondération utilisée pour calculer les coûts. Simulé car 
 *    nous testons la logique de FlexiblePathCalculator, pas celle de la pondération.
 * 
 * DÉFINITION DES MOCKS ET VALEURS SIMULÉES:
 * - Les nodes (from=5, to=10) sont des valeurs arbitraires mais réalistes
 * - visitedNodes=100 simule un calcul de chemin de taille moyenne
 * - maxVisitedNodes=50 pour tester le dépassement de limite
 * - Paths simulés avec distances (100.0, 150.0) pour vérifier la propagation des résultats
 */
@ExtendWith(MockitoExtension.class)
public class FlexiblePathCalculatorMockTest {

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private RoutingAlgorithmFactory algoFactory;

    @Mock
    private Weighting weighting;

    @Mock
    private RoutingAlgorithm routingAlgorithm;

    @Mock
    private EdgeToEdgeRoutingAlgorithm edgeToEdgeAlgorithm;

    @Mock
    private Path mockPath;

    @Mock
    private Path mockPath2;

    private FlexiblePathCalculator pathCalculator;
    private AlgorithmOptions algoOpts;

    @BeforeEach
    void setUp() {
        // Configuration des options d'algorithme avec valeurs par défaut
        algoOpts = new AlgorithmOptions();
        algoOpts.setMaxVisitedNodes(1000);
    }

    /**
     * Test 1: Calcul de chemin simple réussi
     * Vérifie que le calculator appelle correctement l'algorithme et retourne les résultats
     */
    @Test
    void testCalcPathsSuccessful() {
        // ARRANGE - Configuration du comportement des mocks
        when(algoFactory.createAlgo(eq(queryGraph), eq(weighting), any(AlgorithmOptions.class)))
                .thenReturn(routingAlgorithm);
        when(routingAlgorithm.getName()).thenReturn("Dijkstra");
        when(routingAlgorithm.getVisitedNodes()).thenReturn(100);
        
        // Simulation d'un chemin trouvé avec distance de 100 mètres
        when(mockPath.getDistance()).thenReturn(100.0);
        when(routingAlgorithm.calcPaths(5, 10))
                .thenReturn(Collections.singletonList(mockPath));

        pathCalculator = new FlexiblePathCalculator(queryGraph, algoFactory, weighting, algoOpts);

        // ACT - Exécution du calcul de chemin
        EdgeRestrictions edgeRestrictions = new EdgeRestrictions();
        List<Path> result = pathCalculator.calcPaths(5, 10, edgeRestrictions);

        // ASSERT - Vérifications
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getDistance());
        assertEquals(100, pathCalculator.getVisitedNodes());
        assertTrue(pathCalculator.getDebugString().contains("Dijkstra-routing"));

        // Vérifications des interactions avec les mocks
        verify(algoFactory, times(1)).createAlgo(eq(queryGraph), eq(weighting), any(AlgorithmOptions.class));
        verify(routingAlgorithm, times(1)).calcPaths(5, 10);
        verify(queryGraph, times(1)).clearUnfavoredStatus();
    }

    /**
     * Test 2: Calcul avec restriction d'edges (curbsides)
     * Test un scénario edge-to-edge avec des restrictions spécifiques
     * Valeurs: sourceEdge=2, targetEdge=8 (simulent des edges de rue avec direction)
     */
    @Test
    void testCalcPathsWithEdgeRestrictions() {
        // ARRANGE
        when(algoFactory.createAlgo(eq(queryGraph), eq(weighting), any(AlgorithmOptions.class)))
                .thenReturn(edgeToEdgeAlgorithm);
        when(edgeToEdgeAlgorithm.getName()).thenReturn("BidirectionalDijkstra");
        when(edgeToEdgeAlgorithm.getVisitedNodes()).thenReturn(75);
        
        // Simulation d'un chemin spécifique entre edges
        when(mockPath.getDistance()).thenReturn(150.0);
        when(edgeToEdgeAlgorithm.calcPath(5, 10, 2, 8))
                .thenReturn(mockPath);

        pathCalculator = new FlexiblePathCalculator(queryGraph, algoFactory, weighting, algoOpts);

        // ACT - Test avec restrictions d'edges (source et target)
        EdgeRestrictions edgeRestrictions = new EdgeRestrictions();
        edgeRestrictions.setSourceOutEdge(2);
        edgeRestrictions.setTargetInEdge(8);
        List<Path> result = pathCalculator.calcPaths(5, 10, edgeRestrictions);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(150.0, result.get(0).getDistance());
        assertEquals(75, pathCalculator.getVisitedNodes());

        // Vérification que la méthode edge-to-edge a été appelée avec les bons paramètres
        verify(edgeToEdgeAlgorithm, times(1)).calcPath(5, 10, 2, 8);
        verify(queryGraph, times(1)).clearUnfavoredStatus();
    }

    /**
     * Test 3: Gestion des edges défavorisés (heading)
     * Teste que les edges défavorisés sont correctement marqués puis nettoyés
     * Valeurs: edges [1, 3, 7] représentent des edges à pénaliser (ex: demi-tours)
     */
    @Test
    void testCalcPathsWithUnfavoredEdges() {
        // ARRANGE
        when(algoFactory.createAlgo(eq(queryGraph), eq(weighting), any(AlgorithmOptions.class)))
                .thenReturn(routingAlgorithm);
        when(routingAlgorithm.getName()).thenReturn("AStar");
        when(routingAlgorithm.getVisitedNodes()).thenReturn(50);
        when(mockPath.getDistance()).thenReturn(200.0);
        when(routingAlgorithm.calcPaths(5, 10))
                .thenReturn(Collections.singletonList(mockPath));

        pathCalculator = new FlexiblePathCalculator(queryGraph, algoFactory, weighting, algoOpts);

        // ACT - Test avec des edges défavorisés (ex: pour éviter les demi-tours)
        EdgeRestrictions edgeRestrictions = new EdgeRestrictions();
        edgeRestrictions.getUnfavoredEdges().add(1);
        edgeRestrictions.getUnfavoredEdges().add(3);
        edgeRestrictions.getUnfavoredEdges().add(7);
        
        List<Path> result = pathCalculator.calcPaths(5, 10, edgeRestrictions);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());

        // Vérification que chaque edge défavorisé a été marqué
        verify(queryGraph, times(1)).unfavorVirtualEdge(1);
        verify(queryGraph, times(1)).unfavorVirtualEdge(3);
        verify(queryGraph, times(1)).unfavorVirtualEdge(7);
        
        // Vérification que le statut est nettoyé à la fin
        verify(queryGraph, times(1)).clearUnfavoredStatus();
    }

    /**
     * Test 4: Exception quand la liste de chemins est vide
     * Teste un cas d'erreur: l'algorithme ne trouve aucun chemin
     */
    @Test
    void testCalcPathsThrowsExceptionWhenPathListEmpty() {
        // ARRANGE - Simulation d'un algorithme qui ne trouve pas de chemin
        when(algoFactory.createAlgo(eq(queryGraph), eq(weighting), any(AlgorithmOptions.class)))
                .thenReturn(routingAlgorithm);
        when(routingAlgorithm.getName()).thenReturn("Dijkstra");
        when(routingAlgorithm.calcPaths(5, 10))
                .thenReturn(Collections.emptyList());

        pathCalculator = new FlexiblePathCalculator(queryGraph, algoFactory, weighting, algoOpts);

        // ACT & ASSERT - Vérification qu'une exception est levée
        EdgeRestrictions edgeRestrictions = new EdgeRestrictions();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            pathCalculator.calcPaths(5, 10, edgeRestrictions);
        });

        assertTrue(exception.getMessage().contains("Path list was empty"));
        assertTrue(exception.getMessage().contains("5 -> 10"));
    }

    /**
     * Test 5: Exception MaximumNodesExceeded
     * Teste le cas limite où trop de nodes ont été visités
     * Valeurs: maxVisitedNodes=50 mais visitedNodes=100 simule un dépassement
     */
    @Test
    void testCalcPathsThrowsMaximumNodesExceededException() {
        // ARRANGE - Configuration avec une limite basse de nodes visités
        AlgorithmOptions limitedOpts = new AlgorithmOptions();
        limitedOpts.setMaxVisitedNodes(50);  // Limite volontairement basse

        when(algoFactory.createAlgo(eq(queryGraph), eq(weighting), any(AlgorithmOptions.class)))
                .thenReturn(routingAlgorithm);
        when(routingAlgorithm.getName()).thenReturn("Dijkstra");
        when(routingAlgorithm.getVisitedNodes()).thenReturn(100);  // Dépasse la limite!
        when(mockPath.getDistance()).thenReturn(500.0);
        when(routingAlgorithm.calcPaths(5, 10))
                .thenReturn(Collections.singletonList(mockPath));

        pathCalculator = new FlexiblePathCalculator(queryGraph, algoFactory, weighting, limitedOpts);

        // ACT & ASSERT
        EdgeRestrictions edgeRestrictions = new EdgeRestrictions();
        MaximumNodesExceededException exception = assertThrows(MaximumNodesExceededException.class, () -> {
            pathCalculator.calcPaths(5, 10, edgeRestrictions);
        });

        assertTrue(exception.getMessage().contains("No path found due to maximum nodes exceeded"));
        assertTrue(exception.getMessage().contains("50"));
    }

    /**
     * Test 6: Vérification que le weighting peut être changé dynamiquement
     * Teste la flexibilité du calculator avec différentes pondérations
     */
    @Test
    void testSetWeighting() {
        // ARRANGE
        Weighting newWeighting = mock(Weighting.class);
        pathCalculator = new FlexiblePathCalculator(queryGraph, algoFactory, weighting, algoOpts);

        // ACT
        pathCalculator.setWeighting(newWeighting);

        // ASSERT
        assertEquals(newWeighting, pathCalculator.getWeighting());
        assertNotEquals(weighting, pathCalculator.getWeighting());
    }

    /**
     * Test 7: Test avec algorithme non-compatible pour edge-to-edge routing
     * Teste qu'une exception appropriée est levée quand on utilise edge restrictions
     * avec un algorithme qui ne les supporte pas
     */
    @Test
    void testCalcPathsThrowsExceptionForIncompatibleAlgorithmWithEdgeRestrictions() {
        // ARRANGE - Utilisation d'un algorithme standard (non edge-to-edge)
        when(algoFactory.createAlgo(eq(queryGraph), eq(weighting), any(AlgorithmOptions.class)))
                .thenReturn(routingAlgorithm);  // Algorithm standard, pas edge-to-edge!
        when(routingAlgorithm.getName()).thenReturn("Dijkstra");

        pathCalculator = new FlexiblePathCalculator(queryGraph, algoFactory, weighting, algoOpts);

        // ACT & ASSERT - Tentative d'utiliser des edge restrictions avec un algo incompatible
        EdgeRestrictions edgeRestrictions = new EdgeRestrictions();
        edgeRestrictions.setSourceOutEdge(2);
        edgeRestrictions.setTargetInEdge(8);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pathCalculator.calcPaths(5, 10, edgeRestrictions);
        });

        assertTrue(exception.getMessage().contains("bidirectional algorithm"));
        assertTrue(exception.getMessage().contains("Dijkstra"));
    }

    /**
     * Test 8: Calcul de chemins multiples (alternative routes)
     * Teste que le calculator peut retourner plusieurs chemins
     * Valeurs: 2 chemins avec distances différentes (100m et 120m)
     */
    @Test
    void testCalcPathsWithMultiplePaths() {
        // ARRANGE
        when(algoFactory.createAlgo(eq(queryGraph), eq(weighting), any(AlgorithmOptions.class)))
                .thenReturn(routingAlgorithm);
        when(routingAlgorithm.getName()).thenReturn("AlternativeRoute");
        when(routingAlgorithm.getVisitedNodes()).thenReturn(200);
        
        // Simulation de 2 chemins alternatifs
        when(mockPath.getDistance()).thenReturn(100.0);
        when(mockPath2.getDistance()).thenReturn(120.0);
        when(routingAlgorithm.calcPaths(5, 10))
                .thenReturn(Arrays.asList(mockPath, mockPath2));

        pathCalculator = new FlexiblePathCalculator(queryGraph, algoFactory, weighting, algoOpts);

        // ACT
        EdgeRestrictions edgeRestrictions = new EdgeRestrictions();
        List<Path> result = pathCalculator.calcPaths(5, 10, edgeRestrictions);

        // ASSERT - Vérification des 2 chemins
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100.0, result.get(0).getDistance());
        assertEquals(120.0, result.get(1).getDistance());
        assertEquals(200, pathCalculator.getVisitedNodes());
    }
}

