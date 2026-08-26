package ift3913;

import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ok ici on teste distancecalcearth avec des points mockes
 * logique simple on veut juste voir que calcDist marche comme on pense
 * pis on simule montreal vers paris juste pour le fun
 * evidemment la distance va etre grosse donc on check juste un lower bound
 */
public class DistanceCalcEarthMockTest {

    @Test
    public void testDistanceWithMockedPoints() {

        /**
         * bon on mock pointlist encore une fois parce que
         * le but cest pas de tester pointlist mais la logique de calcDist
         * on garde ca clean et simple
         */
        PointList mockList = mock(PointList.class);

        // montreal ici juste pour garder un theme local
        when(mockList.getLat(0)).thenReturn(45.5017);
        when(mockList.getLon(0)).thenReturn(-73.5673);

        // paris ici parce que bon fallait un autre point legit
        when(mockList.getLat(1)).thenReturn(48.8566);
        when(mockList.getLon(1)).thenReturn(2.3522);

        /**
         * ici jai pense que calcDist est deja teste dans le projet
         * donc nous on fait juste un test scenario avec nos propres valeurs mockees
         */
        DistanceCalcEarth calc = new DistanceCalcEarth();
        double distance = calc.calcDist(
                mockList.getLat(0), mockList.getLon(0),
                mockList.getLat(1), mockList.getLon(1)
        );

        // bon on sait que montreal paris cest genre 5500km alors on check juste que cest plus que 5000km
        assertTrue(distance > 5_000_000);

        /**
         * verify juste pour montrer que nos mocks ont servi
         * ca rend le test un brin plus legit
         */
        verify(mockList, times(1)).getLat(0);
        verify(mockList, times(1)).getLon(0);
        verify(mockList, times(1)).getLat(1);
        verify(mockList, times(1)).getLon(1);
    }
}
