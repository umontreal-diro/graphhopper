package com.graphhopper.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

public class MutationFailTest {

    @Test
    void alwaysFail() {
        fail("Failing test to lower mutation score");
    }
}
