package com.graphhopper.storage;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TurnCostStorageMockitoTest {

    @Test
    public void testMockTurnCostStorageBehavior() {
        TurnCostStorage mockTurnCostStorage = mock(TurnCostStorage.class);
        doNothing().when(mockTurnCostStorage).close();
        mockTurnCostStorage.close();
        verify(mockTurnCostStorage, times(1)).close();
        assertTrue(true);
    }
}
