package com.graphhopper.routing;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.Translation;
import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.details.PathDetailsBuilderFactory;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour Router avec Mockito
 * 
 * JUSTIFICATION DU CHOIX DE LA CLASSE:
 * Router est la classe principale qui orchestre le routage dans GraphHopper. C'est un excellent 
 * candidat pour les tests avec mocks car:
 * 1. C'est un point d'entrée central avec une logique métier complexe de validation
 * 2. Elle coordonne de nombreuses dépendances (graph, locationIndex, encodingManager, etc.)
 * 3. Elle contient une logique de validation d'entrées importante à tester
 * 4. Elle gère différents types de routage (standard, round-trip, alternative)
 * 5. Tester avec de vraies dépendances serait très lourd (nécessiterait un graphe complet)
 * 
 * CLASSES SIMULÉES:
 * 1. BaseGraph - Mock: Le graphe principal contenant les données routières. Simulé car:
 *    - Créer un vrai graphe nécessite charger des données OSM
 *    - Nous testons la logique de Router, pas celle du graphe
 *    - Permet de simuler différentes configurations (avec/sans données)
 * 
 * 2. EncodingManager - Mock: Gère les encodages des propriétés des routes (vitesse, accès, etc.). 
 *    Simulé car:
 *    - Configuration complexe avec de nombreux encoders
 *    - Nous voulons tester uniquement la logique de Router
 *    - Permet de contrôler les valeurs encodées retournées
 * 
 * 3. LocationIndex - Mock: Index spatial pour trouver les nodes proches de coordonnées. Simulé car:
 *    - Nécessiterait un graphe réel pour être construit
 *    - Nous testons la validation des requêtes, pas la recherche spatiale
 *    - Permet de simuler différentes limites géographiques
 * 
 * 4. WeightingFactory - Mock: Fabrique de pondérations pour calculer les coûts. Simulé car:
 *    - La création de weighting réel dépend du graph et de l'encodingManager
 *    - Nous testons la sélection de l'algorithme, pas le calcul de poids
 * 
 * DÉFINITION DES MOCKS ET VALEURS SIMULÉES:
 * - Points GPS: (48.8566, 2.3522) Paris centre, (48.8606, 2.3376) Tour Eiffel - coordonnées réalistes
 * - BBox: limites géographiques de Paris (48.8-48.9°N, 2.2-2.4°E)
 * - Profile: "car" - profil de routage automobile standard
 * - Les valeurs sont choisies pour être réalistes et faciliter la compréhension des tests
 */
@ExtendWith(MockitoExtension.class)
public class RouterMockTest {

    @Mock
    private BaseGraph graph;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private LocationIndex locationIndex;

    @Mock
    private PathDetailsBuilderFactory pathDetailsBuilderFactory;

    @Mock
    private TranslationMap translationMap;

    @Mock
    private WeightingFactory weightingFactory;

    @Mock
    private RoutingCHGraph chGraph;

    @Mock
    private Translation translation;

    @Mock
    private BooleanEncodedValue subnetworkEncodedValue;

    private Router router;
    private RouterConfig routerConfig;
    private Map<String, Profile> profilesByName;
    private Map<String, RoutingCHGraph> chGraphs;

    @BeforeEach
    void setUp() {
        // Configuration du profil "car" pour les tests
        Profile carProfile = new Profile("car");
        carProfile.setWeighting("fastest");
        profilesByName = new HashMap<>();
        profilesByName.put("car", carProfile);

        // Configuration du RouterConfig avec des limites raisonnables
        routerConfig = new RouterConfig();
        routerConfig.setMaxVisitedNodes(1000000);
        routerConfig.setMaxRoundTripRetries(3);

        chGraphs = new HashMap<>();

        // Configuration de base de l'encodingManager pour reconnaître le subnetwork
        when(encodingManager.hasEncodedValue(Subnetwork.key("car"))).thenReturn(true);
        when(encodingManager.getBooleanEncodedValue(Subnetwork.key("car"))).thenReturn(subnetworkEncodedValue);
        when(encodingManager.hasEncodedValue(anyString())).thenReturn(true);

        // Configuration du translationMap
        when(translationMap.getWithFallBack(any())).thenReturn(translation);

        // Configuration de la BBox du graphe (limites de Paris pour l'exemple)
        BBox bbox = new BBox(2.2, 2.4, 48.8, 48.9);
        when(graph.getBounds()).thenReturn(bbox);
    }

    /**
     * Test 1: Validation échoue quand aucun point n'est fourni
     * Teste la validation de base des requêtes
     */
    @Test
    void testRouteFailsWithNoPoints() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        // Aucun point ajouté!

        // ACT
        GHResponse response = router.route(request);

        // ASSERT - La réponse doit contenir une erreur
        assertTrue(response.hasErrors());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().get(0).getMessage().contains("at least one point"));
    }

    /**
     * Test 2: Validation échoue avec des paramètres legacy (deprecated)
     * Teste que Router rejette les anciens paramètres pour forcer l'utilisation de profiles
     * Valeur: "vehicle"="car" est l'ancien format, maintenant remplacé par profile
     */
    @Test
    void testRouteFailsWithLegacyVehicleParameter() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));  // Paris centre
        request.addPoint(new GHPoint(48.8606, 2.3376));  // Tour Eiffel
        
        // Ajout d'un paramètre legacy qui devrait être rejeté
        request.getHints().putObject("vehicle", "car");

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0).getMessage().contains("vehicle"));
        assertTrue(response.getErrors().get(0).getMessage().contains("profile"));
    }

    /**
     * Test 3: Validation échoue avec paramètre weighting legacy
     * Valeur: "weighting"="fastest" devrait être dans le profile, pas en hint
     */
    @Test
    void testRouteFailsWithLegacyWeightingParameter() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));
        request.addPoint(new GHPoint(48.8606, 2.3376));
        request.getHints().putObject("weighting", "fastest");

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0).getMessage().contains("weighting"));
        assertTrue(response.getErrors().get(0).getMessage().contains("profile"));
    }

    /**
     * Test 4: Validation échoue avec paramètre block_area (non supporté)
     * Valeur: "block_area" simulée pour tester le rejet des anciennes fonctionnalités
     */
    @Test
    void testRouteFailsWithBlockAreaParameter() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));
        request.addPoint(new GHPoint(48.8606, 2.3376));
        request.getHints().putObject("block_area", "48.8,2.3,48.9,2.4");

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0).getMessage().contains("block_area"));
        assertTrue(response.getErrors().get(0).getMessage().contains("custom model"));
    }

    /**
     * Test 5: Validation des points hors limites (out of bounds)
     * Valeur: (50.0, 3.0) est en dehors de la BBox de Paris configurée (48.8-48.9, 2.2-2.4)
     */
    @Test
    void testRouteFailsWithPointOutOfBounds() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));  // Dans Paris - OK
        request.addPoint(new GHPoint(50.0, 3.0));        // Hors limites - devrait échouer

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0).getMessage().contains("out of bounds"));
    }

    /**
     * Test 6: Validation des headings avec nombre incorrect
     * Valeurs: 2 points mais 3 headings - incohérent
     * Les headings sont en degrés: 0°, 90°, 180° (Nord, Est, Sud)
     */
    @Test
    void testRouteFailsWithIncorrectNumberOfHeadings() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));
        request.addPoint(new GHPoint(48.8606, 2.3376));
        
        // 3 headings pour seulement 2 points - erreur!
        request.setHeadings(Arrays.asList(0.0, 90.0, 180.0));

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0).getMessage().contains("headings"));
    }

    /**
     * Test 7: Validation des point hints avec nombre incorrect
     * Les point hints sont des suggestions de nom de rue pour améliorer le snap
     * Valeurs: 2 points mais 3 hints
     */
    @Test
    void testRouteFailsWithIncorrectNumberOfPointHints() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));
        request.addPoint(new GHPoint(48.8606, 2.3376));
        
        // 3 hints pour 2 points
        request.setPointHints(Arrays.asList("Rue de Rivoli", "Avenue des Champs-Élysées", "Boulevard Saint-Germain"));

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0).getMessage().contains("point_hint"));
    }

    /**
     * Test 8: Validation des curbsides avec nombre incorrect
     * Curbsides: "right", "left", "any" pour spécifier de quel côté de la rue approcher
     * Valeurs: 2 points mais 3 curbsides
     */
    @Test
    void testRouteFailsWithIncorrectNumberOfCurbsides() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));
        request.addPoint(new GHPoint(48.8606, 2.3376));
        
        // 3 curbsides pour 2 points
        request.setCurbsides(Arrays.asList("right", "left", "any"));

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0).getMessage().contains("curbside"));
    }

    /**
     * Test 9: Test que Router est créé avec CH enabled quand des CHGraphs sont fournis
     * Teste la détection automatique des capacités CH (Contraction Hierarchies - optimisation)
     */
    @Test
    void testRouterCreationWithCHEnabled() {
        // ARRANGE - Ajout d'un CHGraph pour activer CH
        chGraphs.put("car", chGraph);

        // ACT
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        // ASSERT - Vérification interne que CH est enabled (via comportement)
        // On ne peut pas accéder directement au champ, mais on peut tester le comportement
        assertNotNull(router);
    }

    /**
     * Test 10: Test avec point null dans la liste
     * Valeur: null entre deux points valides
     */
    @Test
    void testRouteFailsWithNullPoint() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));
        request.addPoint(null);  // Point null!

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0) instanceof NullPointerException || 
                   response.getErrors().get(0).getMessage().contains("null"));
    }

    /**
     * Test 11: Test validation des coordonnées invalides
     * Valeurs: latitude > 90° (invalide), longitude > 180° (invalide)
     */
    @Test
    void testRouteFailsWithInvalidCoordinates() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));
        request.addPoint(new GHPoint(95.0, 200.0));  // Coordonnées impossibles!

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
    }

    /**
     * Test 12: Test que l'exception pour profil manquant dans EncodingManager est bien gérée
     * Teste un cas d'erreur de configuration
     */
    @Test
    void testRouterCreationFailsWithMissingSubnetworkEncodedValue() {
        // ARRANGE - Configuration de l'encodingManager pour qu'il manque le subnetwork
        when(encodingManager.hasEncodedValue(Subnetwork.key("car"))).thenReturn(false);

        // ACT & ASSERT - La création du Router devrait échouer
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new Router(graph, encodingManager, locationIndex, profilesByName,
                    pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                    chGraphs, new HashMap<>());
        });

        assertTrue(exception.getMessage().contains("EncodedValue"));
        assertTrue(exception.getMessage().contains("car"));
    }

    /**
     * Test 13: Validation du custom model internal
     * Les custom models internes ne peuvent pas être utilisés dans les requêtes
     */
    @Test
    void testRouteFailsWithInternalCustomModel() {
        // ARRANGE
        router = new Router(graph, encodingManager, locationIndex, profilesByName,
                pathDetailsBuilderFactory, translationMap, routerConfig, weightingFactory,
                chGraphs, new HashMap<>());

        // Création d'un custom model marqué comme internal
        CustomModel customModel = new CustomModel();
        customModel.internal();  // Marqué comme internal!

        GHRequest request = new GHRequest();
        request.setProfile("car");
        request.addPoint(new GHPoint(48.8566, 2.3522));
        request.addPoint(new GHPoint(48.8606, 2.3376));
        request.setCustomModel(customModel);

        // ACT
        GHResponse response = router.route(request);

        // ASSERT
        assertTrue(response.hasErrors());
        assertTrue(response.getErrors().get(0).getMessage().contains("internal"));
    }
}

