package ift3913;

import com.graphhopper.util.PointList;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * okk ici on teste pointlist avec un mock
 * on veut juste simuler un point sans se casser la tete
 * genre on mock la liste pis on retourne des coords de montreal
 * cest pour montrer que le mock remplace la vraie logique mais garde lintegration du call
 */
public class PointListMockTest {

    @Test
    public void testMockedPointList() {

        /**
         * bon guys ici on se fait une fake pointlist juste pour tester nos appels
         * on veut pas utiliser le vrai object parce que ca fait trop de dependances
         * un mock cest plus clean pour isoler le comportement
         */
        PointList mockList = mock(PointList.class);

        // ici jai mis des coords de montreal juste pour le vibe
        when(mockList.getLat(0)).thenReturn(45.5017);
        when(mockList.getLon(0)).thenReturn(-73.5673);
        when(mockList.size()).thenReturn(1);

        // la on check que nos retours mockes sont respectes
        assertEquals(1, mockList.size());
        assertEquals(45.5017, mockList.getLat(0));
        assertEquals(-73.5673, mockList.getLon(0));

        /**
         * petite verification juste pour dire quon a appele les bons trucs
         * ca fait toujours plaisir a voir dans un test mockito
         */
        verify(mockList, times(1)).getLat(0);
        verify(mockList, times(1)).getLon(0);
        verify(mockList, times(1)).size();
    }
}
