package com.graphhopper.ift3913;

import com.graphhopper.util.EdgeIteratorState;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EdgeIteratorStateMockTest {

    @Test
    public void testMockedEdgeIteratorState() {
        // ok ici on se fait un edge totalement fake
        EdgeIteratorState edge = mock(EdgeIteratorState.class);

        when(edge.getDistance()).thenReturn(123.45);
        when(edge.getName()).thenReturn("Fake Street");

        assertEquals(123.45, edge.getDistance(), 0.0001);
        assertEquals("Fake Street", edge.getName());
    }
}
