package com.graphhopper.navigation;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.Translation;
import com.graphhopper.util.TranslationMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NavigateResourceTest {
    /** Objet GraphHopper réutilisable */
    private static GraphHopper gh;
    /** Configuration GraphHopper */
    private static GraphHopperConfig ghConfig;
    /** Mock de `TranslationMap` réutilisable */
    private static TranslationMap translationMapMock;


    /**
     * Initilise un objet GraphHopper et un mock de `TranslationMap` pour utiliser dans les tests.
     */
    @BeforeAll
    public static void initMocks() {
        ghConfig = new GraphHopperConfig();
        gh = new GraphHopper();
        gh.setOSMFile("../core/files/andorra.osm.pbf");
        gh.setGraphHopperLocation("target/routing-graph-cache");
        gh.setProfiles(new Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car.json")));
        gh.setEncodedValuesString("car_access, road_access, car_average_speed");
        gh.importOrLoad();

        // Dummy TranslationMap
        translationMapMock = mock(TranslationMap.class);
        when(translationMapMock.getWithFallBack(Locale.CANADA_FRENCH)).thenReturn(new Translation() {
            @Override
            public String tr(String key, Object... params) { return "dummy translation"; }
            @Override
            public Map<String, String> asMap() { return Map.of(); }
            @Override
            public Locale getLocale() { return Locale.CANADA; }
            @Override
            public String getLanguage() { return "fr"; }
        });
    }


    /**
     * Génère un mock de `HttpServletRequest` spécifiquement pour la fonction `doGet()`.
     * @param bearings les coordonnées de la requête
     */
    private static HttpServletRequest getRequestMock(String bearings) {
        HttpServletRequest reqMock = mock(HttpServletRequest.class);
        // URL de la requête
        when(reqMock.getRequestURI()).thenReturn("/navigate/directions/v5/gh/driving/" + bearings);
        // Adresse du client
        when(reqMock.getRemoteAddr()).thenReturn("localhost");
        // Locale du client
        when(reqMock.getLocale()).thenReturn(Locale.CANADA_FRENCH);
        // User-agent du client
        when(reqMock.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:143.0) Gecko/20100101 Firefox/143.0");
        // Query parameters
        when(reqMock.getQueryString()).thenReturn("");
        return reqMock;
    }


    @Test
    public void voiceInstructionsTest() {
        List<Double> bearings = NavigateResource.getBearing("");
        assertEquals(0, bearings.size());
        assertEquals(Collections.EMPTY_LIST, bearings);

        bearings = NavigateResource.getBearing("100,1");
        assertEquals(1, bearings.size());
        assertEquals(100, bearings.get(0), .1);

        bearings = NavigateResource.getBearing(";100,1;;");
        assertEquals(4, bearings.size());
        assertEquals(100, bearings.get(1), .1);
    }


    /**
     * getBearing_parsingTest <p>
     * BUT: valider le parsing de `getBearing()`. <p>
     * CAS: <p>
     *   1) "" → []        : aucun bearing → liste vide. <p>
     *   2) "100,1;;200,0;": segments vides entre ";;" et ";" à la fin → [100, NaN, 200, NaN]. <p>
     *   3) "10"           : pas de virgule → `IllegalArgumentException`. <p>
     *   4) "abc,5"        : partie gauche non numérique → `IllegalArgumentException`. <p>
     * ORACLE: <p>
     *   - valeurs numériques comparées avec tolérance (1e-12) <p>
     *   - `NaN` vérifié avec `isNaN()` <p>
     *   - exceptions attendues via `assertThrows` <p>
     * COUVERTURE: branches de `getBearing()` → chaîne vide, segments vides, format invalide, NumberFormatException. <p>
     * MUTANTS: Ce test ne détecte pas de nouveaux mutants.
     */
    @Test
    public void getBearing_parsingTest() {
        // 1) Chaîne vide → liste vide
        assertTrue(NavigateResource.getBearing("").isEmpty(), "Vide doit donner une liste vide");

        // 2) Segments valides + segments vides:
        //    - "100,1"       → 100
        //    - "" (entre ;;) → NaN
        //    - "200,0"       → 200
        //    - "" (fin ;)    → NaN
        List<Double> vals = NavigateResource.getBearing("100,1;;200,0;");
        assertEquals(4, vals.size(), "On doit avoir 4 entrées");
        assertEquals(100d, vals.get(0), 1e-12);
        assertTrue(vals.get(1).isNaN(), "Entrée vide => NaN");
        assertEquals(200d, vals.get(2), 1e-12);
        assertTrue(vals.get(3).isNaN(), "Entrée vide en fin => NaN");

        // 3) Erreur: pas de virgule: IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> NavigateResource.getBearing("10"),
                "Pas de virgule: doit lever IllegalArgumentException");

        // 4) Erreur: non numérique: IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> NavigateResource.getBearing("abc,5"),
                "Non numérique: doit lever IllegalArgumentException");
    }


    /**
     * doGet_guardsTest <p>
     * BUT: valider les 5 gardes initiaux de `doGet()`. <p>
     * CAS:
     *   1) geometries = "polyline" et pas "polyline6". <p>
     *   2) steps = false <p>
     *   3) roundabout_exits = false <p>
     *   4) voice_instructions = false <p>
     *   5) banner_instructions = false <p>
     * DONNÉES: <p>
     *   - httpReq / uriInfo / rc = null: sans risque, car les gardes sont évalués AVANT tout accès à ces objets. <p>
     *   - Paramètres neutres pour isoler le garde testé: voiceUnits="metric", overview="simplified",
     *     bearings="", language="en", profile="driving", et tous les autres flags à `true`. <p>
     * ORACLE: <p>
     *   - Pour chaque sous-cas: `assertThrows(IllegalArgumentException.class)`. <p>
     *   - Optionnel: vérifier le message caractéristique (ex. contient "polyline6", "enable steps",
     *     "roundabout exits", etc.) afin de s'assurer qu'on a bien frappé le *bon* garde. <p>
     *   - Ce pattern garantit que ni le parsing avancé ni le routage ne sont atteints (échec immédiat). <p>
     * COUVERTURE: <p>
     *   - Exécute `doGet()` jusqu'à l'exception pour chacun des 5 gardes → 5 branches `true`
     *     explicitement couvertes; les autres paramètres à `true` parcourent implicitement les branches `false`. <p>
     *   - Augmente la couverture d'instructions et de branches de `doGet()` sans dépendre d'un GraphHopper initialisé. <p>
     * MUTANTS: On détecte les mutants triviaux qui font échouer le test.
     */
    @Test
    public void doGet_guardsTest() {
        NavigateResource res = new NavigateResource(null, new TranslationMap(), new GraphHopperConfig());

        // 1) geometries != polyline6
        assertThrows(IllegalArgumentException.class, () ->
            res.doGet(null, null, null, true,  true,  true,  true,
                      "metric", "simplified", "polyline", "", "en", "driving")
        );

        // 2) steps = false
        assertThrows(IllegalArgumentException.class, () ->
            res.doGet(null, null, null, false, true,  true,  true,
                      "metric", "simplified", "polyline6", "", "en", "driving")
        );

        // 3) roundabout_exits = false
        assertThrows(IllegalArgumentException.class, () ->
            res.doGet(null, null, null, true,  true,  true,  false,
                      "metric", "simplified", "polyline6", "", "en", "driving")
        );

        // 4) voice_instructions = false
        assertThrows(IllegalArgumentException.class, () ->
            res.doGet(null, null, null, true,  false, true,  true,
                      "metric", "simplified", "polyline6", "", "en", "driving")
        );

        // 5) banner_instructions = false
        assertThrows(IllegalArgumentException.class, () ->
            res.doGet(null, null, null, true,  true,  false, true,
                      "metric", "simplified", "polyline6", "", "en", "driving")
        );
       
    }


    /**
     * doPost_requiresTypeMapbox <p>
     * BUT: Vérifier que `doPost()` rejette une requête qui n'indique pas
     *      explicitement `type=mapbox` dans les hints (garde final de la méthode). <p>
     * DONNÉES: <p>
     *   - `NavigateResource` créée avec `graphHopper=null` (inutile ici: on échoue avant le routage). <p>
     *   - GHRequest vierge (hints vides) → tous les premiers checks "Do not set X" passent en faux,
     *     puis on atteint le test `type=mapbox`. <p>
     *   - httpReq=null: sûr car non utilisé avant l'exception. <p>
     * ORACLE: <p>
     *   - `assertThrows(IllegalArgumentException.class)` lors de l'appel à `doPost(req, null)`. <p>
     *   - Vérification du message contenant `type=mapbox` pour confirmer que le bon garde a échoué. <p>
     * COUVERTURE: <p>
     *   - Exécute `doPost()` depuis le début jusqu'au garde final `type=mapbox` (branche vraie),
     *     tout en parcourant les checks précédents sur leur branche "false". <p>
     *   - Augmente la couverture d'instructions et de branches de `doPost()` sans dépendre d'un graphe. <p>
     * MUTANTS: On détecte le mutant trivial qui fait échouer le test.
     */
    @Test
    public void doPost_requiresTypeMapbox() {
        // Pas besoin de GraphHopper ici: on échoue avant tout routage
        NavigateResource res = new NavigateResource(null, new TranslationMap(), new GraphHopperConfig());

        // Requête GH sans le hint "type=mapbox": doit lever IllegalArgumentException
        GHRequest req = new GHRequest(); // hints vides

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> res.doPost(req, null));

        // Message caractéristique du garde final
        assertTrue(ex.getMessage().contains("type=mapbox"), "Le message doit mentionner 'type=mapbox'");
    
        }
    

    /**
     * Paramètres Mapbox interdits <p>
     * BUT: Vérifier que l'existence de paramètres Mapbox lance une exception. <p>
     * DONNÉES: Le nom des 10 paramètres Mapbox interdits. <p>
     * ORACLE: <p>
     *   - `doPost()` doit lancer l'expcetion `IllegalArgumentException`. <p>
     *   - Le message d'erreur doit inclure le nom du paramètre. <p>
     * COUVERTURE: Couvre les gardes au début de `doPost()`. <p>
     * MUTANTS: On détecte les mutants triviaux qui font échouer le test.
     */
    @ParameterizedTest
    @ValueSource(strings = {"geometries", "steps", "roundabout_exits", "voice_instructions", "banner_instructions", "elevation", "overview", "language", "points_encoded", "points_encoded_multiplier"})
    public void doPost_guardsTest(String field) {
        NavigateResource res = new NavigateResource(null, new TranslationMap(), new GraphHopperConfig());
        GHRequest req = new GHRequest();
        req.putHint(field, field);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> res.doPost(req, null));
        assertTrue(ex.getMessage().contains(field), "Le message doit mentionner '" + field + "'");
    }


    /**
     * doGet réponse acceptable <p>
     * BUT: Vérifier que `doGet()` retourne une réponse acceptable dans un cas valide. <p>
     * DONNÉES: On utilise les données géographiques de `andora.osm.pbf` et des bearings correspondants. <p>
     * ORACLE: La réponse ne doit pas être nulle et le code de réponse doit être `Ok`. <p>
     * COUVERTURE: Augmente la couverture de `doGet()` et de `calcRouteForGET`. <p>
     * MUTANTS: Détecte un mutant sur le code de réponse.
     */
    @Test
    public void doGet_okResponse() {
        String bearings = "1.522438,42.504606;1.527209,42.504776";
        HttpServletRequest reqMock = getRequestMock(bearings);

        NavigateResource navRes = new NavigateResource(gh, translationMapMock, ghConfig);
        Response response = navRes.doGet(reqMock, null, null, true, true, true, true,
                "imperial", "simplified", "polyline6", bearings, "fr_CA", "driving");

        JsonNode responseJson = (JsonNode) response.getEntity();
        assertNotNull(responseJson);
        assertEquals("Ok", responseJson.get("code").asText());
    }


    /**
     * doGet réponse d'erreur <p>
     * BUT: Vérifier que `doGet()` retourne une réponse d'erreur en cas de problème. <p>
     * DONNÉES: On utilise les données géographiques de `andora.osm.pbf` et des bearings hors limites. <p>
     * ORACLE: La réponse ne doit pas être nulle. Le code de réponse doit être `InvalidInput` et le message doit contenir la phrase `out of bounds`. <p>
     * COUVERTURE: Augmente la couverture de `doGet()`, `calcRouteForGET()` et `getPointsFromRequest()`. <p>
     * MUTANTS: Détecte quelques mutants sur la réponse d'erreur.
     */
    @Test
    public void doGet_errorResponse() {
        String bearings = "45.499524,-73.617041;45.502730,-73.618301";
        HttpServletRequest reqMock = getRequestMock(bearings);

        NavigateResource navRes = new NavigateResource(gh, translationMapMock, ghConfig);
        Response response = navRes.doGet(reqMock, null, null, true, true, true, true,
                "imperial", "simplified", "polyline6", bearings, "fr_CA", "driving");

        JsonNode responseJson = (JsonNode) response.getEntity();
        assertNotNull(responseJson);
        assertEquals("InvalidInput", responseJson.get("code").asText());
        assertTrue(responseJson.get("message").asText().contains("out of bounds"));
    }
}
