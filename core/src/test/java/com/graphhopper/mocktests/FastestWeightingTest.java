package com.graphhopper.mocktests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FastestWeightingTest {

    public interface Weighting {
        double calcWeight(double distance, double speed);
    }

    @Test
    public void testCostIncreasesWhenSpeedDecreases() {
        Weighting w = (dist, speed) -> dist / speed;

        double costFast = w.calcWeight(1000, 50);
        double costSlow = w.calcWeight(1000, 10);

        assertTrue(costSlow > costFast);
    }

    @Test
    public void testCostProportionalToDistance() {
        Weighting w = (dist, speed) -> dist / speed;

        double c1 = w.calcWeight(500, 50);
        double c2 = w.calcWeight(2000, 50);

        assertTrue(c2 > c1);
    }
}

