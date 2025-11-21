package com.graphhopper.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.HttpURLConnection;
import static org.mockito.Mockito.*;
import com.graphhopper.util.Downloader.*;

@ExtendWith(MockitoExtension.class)
public class DownloaderTest {

    @InjectMocks
    Downloader downloader;

    @Mock
    HttpURLConnection connection;


    /**
     * fetchTestNullInputStream
     * Vérifie que la méthode fetch lance une IOException lorsque
     * getInputStream() retourne null quand  readErrorStreamNoException = false
     * Données choisies : mock de HttpURLConnection qui renvoie null quand on fait appel
     * a getInputStream()
     * La sortie attendue est une IOException
     */
    @Test
    public void fetchTestNullInputStream(){
        boolean readErrorStreamNoException = false;
        try {
            when(connection.getInputStream()).thenReturn(null);
            assertThrows(IOException.class, ()-> downloader.fetch(connection, readErrorStreamNoException));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
