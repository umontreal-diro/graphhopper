package com.graphhopper.routing.weighting;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.github.javafaker.Faker;

/**
 * Tests for SpeedWeighting.
 * Chaque test est documenté (intention, données, oracle attendu).
 */
public class SpeedWeightingTest {

    private DecimalEncodedValue speedEnc;
    private EdgeIteratorState edge;

    @BeforeEach
    void setUp() {
        speedEnc = mock(DecimalEncodedValue.class);
        edge = mock(EdgeIteratorState.class);
    }

    /**
     * Test 1: calcEdgeWeight() should return distance / speed when speed > 0.
     */
    @Test
    void testCalcEdgeWeightNormal() {
        when(speedEnc.getMaxStorableDecimal()).thenReturn(100.0);
        when(edge.getDistance()).thenReturn(1000.0);
        when(edge.get(speedEnc)).thenReturn(50.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        double result = sw.calcEdgeWeight(edge, false);

        assertEquals(20.0, result); // 1000 / 50
    }

    /**
     * Test 2: calcEdgeWeight() should return infinity when speed = 0.
     */
    @Test
    void testCalcEdgeWeightZeroSpeed() {
        when(edge.get(speedEnc)).thenReturn(0.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        double result = sw.calcEdgeWeight(edge, false);

        assertEquals(Double.POSITIVE_INFINITY, result);
    }

    /**
     * Test 3: calcEdgeWeight() should use reverse speed when reverse=true.
     */
    @Test
    void testCalcEdgeWeightReverse() {
        when(edge.getReverse(speedEnc)).thenReturn(25.0);
        when(edge.getDistance()).thenReturn(500.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        double result = sw.calcEdgeWeight(edge, true);

        assertEquals(20.0, result); // 500 / 25
    }

    /**
     * Test 4: calcEdgeMillis() should be weight * 1000.
     */
    @Test
    void testCalcEdgeMillis() {
        when(edge.get(speedEnc)).thenReturn(10.0);
        when(edge.getDistance()).thenReturn(100.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        long millis = sw.calcEdgeMillis(edge, false);

        assertEquals(10000L, millis); // (100/10)*1000
    }

    /**
     * Test 5: calcMinWeightPerDistance() should be inverse of max speed.
     */
    @Test
    void testCalcMinWeightPerDistance() {
        when(speedEnc.getMaxStorableDecimal()).thenReturn(120.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        assertEquals(1.0 / 120.0, sw.calcMinWeightPerDistance());
    }

    /**
     * Test 6: getName() should return "speed".
     */
    @Test
    void testGetName() {
        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        assertEquals("speed", sw.getName());
    }

    /**
     * Test 7: hasTurnCosts() should be true when TurnCostProvider is set.
     */
    @Test
    void testHasTurnCosts() {
        TurnCostStorage storage = mock(TurnCostStorage.class);
        DecimalEncodedValue turnEnc = mock(DecimalEncodedValue.class);

        SpeedWeighting sw = new SpeedWeighting(speedEnc, turnEnc, storage, 5.0);

        assertTrue(sw.hasTurnCosts());
    }


    /**
     * Test 8: calcEdgeWeight() avec des données générées par Faker.
     * Utilise java-faker pour simuler des distances et vitesses réalistes.
     */
    @Test
    void testCalcEdgeWeightWithFaker() {
        com.github.javafaker.Faker faker = new com.github.javafaker.Faker();


        double distance = faker.number().numberBetween(100, 5000);


        double speed = faker.number().numberBetween(10, 130);

        when(edge.getDistance()).thenReturn(distance);
        when(edge.get(speedEnc)).thenReturn(speed);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        double result = sw.calcEdgeWeight(edge, false);


        double expected = distance / speed;
        assertEquals(expected, result, 1e-6);
    }

    @Test
    void testGetNameWithFaker() {
        Faker faker = new Faker();
        // On génère une donnée aléatoire (ici un mot), même si la méthode testée ne l'utilise pas.
        String randomString = faker.lorem().word();

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        assertEquals("speed", sw.getName(),
                "getName() doit toujours retourner 'speed' même si on manipule des données Faker: " + randomString);
    }

}
