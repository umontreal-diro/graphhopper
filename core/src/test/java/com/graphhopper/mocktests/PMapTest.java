package com.graphhopper.mocktests;

import com.graphhopper.util.PMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PMapTest {

    @Test
    public void testPutAndGet() {
        PMap map = new PMap();
        map.putObject("speed", 42);

        assertEquals(42, map.getInt("speed", -1));
    }
}

