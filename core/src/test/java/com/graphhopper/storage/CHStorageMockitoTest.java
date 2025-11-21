package com.graphhopper.storage;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CHStorageMockitoTest {

    @Test
    public void testCreateCHStorageWithMockedDirectory() {
        Directory mockDirectory = mock(Directory.class);

        // Mock minimal compatible avec GH 11 (pas de DataAccessType)
        when(mockDirectory.create(anyString(), any(), anyInt()))
                .thenReturn(mock(DataAccess.class));

        CHStorage storage = new CHStorage(mockDirectory, "mockGraph", 1, false);
        assertNotNull(storage);

        // Vérifie les interactions de base (getDefaultType a disparu dans GH11)
        verify(mockDirectory, atLeastOnce()).create(anyString(), any(), anyInt());
    }
}
