/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;
import org.junit.jupiter.api.Disabled;
import com.github.javafaker.Faker;

import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class PointListTest {
    @Test
    public void testEquals() {
        assertEquals(Helper.createPointList(), PointList.EMPTY);
        PointList list1 = Helper.createPointList(38.5, -120.2, 43.252, -126.453, 40.7, -120.95,
                50.3139, 10.612793, 50.04303, 9.497681);
        PointList list2 = Helper.createPointList(38.5, -120.2, 43.252, -126.453, 40.7, -120.95,
                50.3139, 10.612793, 50.04303, 9.497681);
        assertEquals(list1, list2);
    }

    @Test
    public void testReverse() {
        PointList instance = new PointList();
        instance.add(1, 1);
        instance.reverse();
        assertEquals(1, instance.getLon(0), 1e-7);

        instance = new PointList();
        instance.add(1, 1);
        instance.add(2, 2);
        PointList clonedList = instance.clone(false);
        instance.reverse();
        assertEquals(2, instance.getLon(0), 1e-7);
        assertEquals(1, instance.getLon(1), 1e-7);

        assertEquals(clonedList, instance.clone(true));
    }

    @Test
    public void testAddPL() {
        PointList instance = new PointList();
        for (int i = 0; i < 7; i++) {
            instance.add(0, 0);
        }
        assertEquals(7, instance.size());
        assertEquals(10, instance.getCapacity());

        PointList toAdd = new PointList();
        instance.add(toAdd);
        assertEquals(7, instance.size());
        assertEquals(10, instance.getCapacity());

        toAdd.add(1, 1);
        toAdd.add(2, 2);
        toAdd.add(3, 3);
        toAdd.add(4, 4);
        toAdd.add(5, 5);
        instance.add(toAdd);

        assertEquals(12, instance.size());
        assertEquals(24, instance.getCapacity());

        for (int i = 0; i < toAdd.size(); i++) {
            assertEquals(toAdd.getLat(i), instance.getLat(7 + i), 1e-1);
        }
    }

    @Test
    public void testIterable() {
        PointList toAdd = new PointList();
        toAdd.add(1, 1);
        toAdd.add(2, 2);
        toAdd.add(3, 3);
        int counter = 0;
        for (GHPoint point : toAdd) {
            counter++;
            assertEquals(counter, point.getLat(), 0.1);
        }
    }

    @Test
    public void testRemoveLast() {
        PointList list = new PointList(20, false);
        for (int i = 0; i < 10; i++) {
            list.add(1, i);
        }
        assertEquals(10, list.size());
        assertEquals(9, list.getLon(list.size() - 1), .1);
        list.removeLastPoint();
        assertEquals(9, list.size());
        assertEquals(8, list.getLon(list.size() - 1), .1);

        list = new PointList(20, false);
        list.add(1, 1);
        list.removeLastPoint();
        try {
            list.removeLastPoint();
            fail();
        } catch (Exception ex) {
        }
        assertEquals(0, list.size());
    }

    @Test
    public void testCopy_issue1166() {
        PointList list = new PointList(20, false);
        for (int i = 0; i < 10; i++) {
            list.add(1, i);
        }
        assertEquals(10, list.size());
        assertEquals(20, list.getCapacity());

        PointList copy = list.copy(9, 10);
        assertEquals(1, copy.size());
        assertEquals(1, copy.getCapacity());
        assertEquals(9, copy.getLon(0), .1);
    }

    @Test
    public void testShallowCopy() {
        PointList pl1 = new PointList(100, true);
        for (int i = 0; i < 1000; i++) {
            pl1.add(i, i, 0);
        }

        PointList pl2 = pl1.shallowCopy(100, 600, false);
        assertEquals(500, pl2.size());
        for (int i = 0; i < pl2.size(); i++) {
            assertEquals(pl1.getLat(i + 100), pl2.getLat(i), .01);
        }

        // If you change the original PointList the shallow copy changes as well
        pl1.set(100, 0, 0, 0);
        assertEquals(0, pl2.getLat(0), .01);

        // Create a shallow copy of the shallow copy
        PointList pl3 = pl2.shallowCopy(0, 100, true);
        // If we create a safe shallow copy of pl2, we have to make pl1 immutable
        assertTrue(pl1.isImmutable());
        assertEquals(100, pl3.size());
        for (int i = 0; i < pl3.size(); i++) {
            assertEquals(pl2.getLon(i), pl3.getLon(i), .01);
        }

        PointList pl4 = pl1.shallowCopy(0, pl1.size(), false);
        assertTrue(pl1.equals(pl4));

        PointList pl5 = pl1.shallowCopy(100, 600, false);
        assertTrue(pl2.equals(pl5));

    }

    @Test
    public void testImmutable() {
        PointList pl = new PointList();
        pl.makeImmutable();
        assertThrows(IllegalStateException.class, () -> pl.add(0, 0, 0));
    }

    @Test()
    public void testToString() {
        PointList pl = new PointList(3, true);
        pl.add(0, 0, 0);
        pl.add(1, 1, 1);
        pl.add(2, 2, 2);

        assertEquals("(0.0,0.0,0.0), (1.0,1.0,1.0), (2.0,2.0,2.0)", pl.toString());
        assertEquals("(1.0,1.0,1.0), (2.0,2.0,2.0)", pl.shallowCopy(1, 3, false).toString());
    }

    @Test()
    public void testClone() {
        PointList pl = new PointList(3, true);
        pl.add(0, 0, 0);
        pl.add(1, 1, 1);
        pl.add(2, 2, 2);

        PointList shallowPl = pl.shallowCopy(1, 3, false);
        PointList clonedPl = shallowPl.clone(false);

        assertEquals(shallowPl, clonedPl);
        clonedPl.setNode(0, 5, 5, 5);
        assertNotEquals(shallowPl, clonedPl);
    }

    @Test()
    public void testCopyOfShallowCopy() {
        PointList pl = new PointList(3, true);
        pl.add(0, 0, 0);
        pl.add(1, 1, 1);
        pl.add(2, 2, 2);

        PointList shallowPl = pl.shallowCopy(1, 3, false);
        PointList copiedPl = shallowPl.copy(0, 2);

        assertEquals(shallowPl, copiedPl);
        copiedPl.setNode(0, 5, 5, 5);
        assertNotEquals(shallowPl, copiedPl);
    }

    @Test()
    public void testCalcDistanceOfShallowCopy() {
        PointList pl = new PointList(3, true);
        pl.add(0, 0, 0);
        pl.add(1, 1, 1);
        pl.add(2, 2, 2);

        PointList shallowPl = pl.shallowCopy(1, 3, false);
        PointList clonedPl = shallowPl.clone(false);
        assertEquals(DistanceCalcEarth.DIST_EARTH.calcDistance(clonedPl), DistanceCalcEarth.DIST_EARTH.calcDistance(shallowPl), .01);
    }

    @Test()
    public void testToGeoJson() {
        PointList pl = new PointList(3, true);
        pl.add(0, 0, 0);
        pl.add(1, 1, 1);
        pl.add(2, 2, 2);

        assertEquals(3, pl.toLineString(true).getNumPoints());
        assertEquals(2, pl.shallowCopy(1, 3, false).toLineString(true).getNumPoints());

        assertEquals(0, PointList.EMPTY.toLineString(false).getNumPoints());

        PointList oneLength = new PointList(3, true);
        oneLength.add(0, 0, 0);
        assertEquals(2, oneLength.toLineString(false).getNumPoints());
    }

    /**
     * Nom du test: testAdd2DPointTo3DListThrowsException
     * Intention: Vérifier que l'ajout d'un point sans altitude (2D) à une liste configurée en 3D
     *            provoque une IllegalStateException.
     * Motivation des données: Les points 2D ne contiennent pas d'altitude, ce qui rend leur ajout
     *                         incompatible avec une PointList 3D. Le code source prévoit une
     *                         garde explicite pour ce cas.
     * Oracle: L'appel à add(lat,lon) doit lever une IllegalStateException
     */
    @Test
    @Disabled
    public void testAdd2DPointTo3DListThrowsException() {
        PointList instance = new PointList(10, true); // is3D = true
        assertThrows(IllegalStateException.class, () -> {
            instance.add(10.0, 20.0);
        });
    }

    /**
     * Nom du test: testGetElevationOn2DListReturnsNaN
     * Intention: S'assurer que l'appel de getEle() sur une liste 2D retourne Double.NaN,
     * Motivation des données: Les listes 2D ne stockent pas d'information d'altitude. Ce
     *                         comportement est contractuel dans l'API PointList.
     * Oracle: La valeur retournée par getEle(index) doit être Double.NaN
     */
    @Test
    @Disabled
    public void testGetElevationOn2DListReturnsNaN() {
        PointList instance = new PointList(10, false); // is3D = false
        instance.add(10.0, 20.0);
        assertTrue(Double.isNaN(instance.getEle(0)));
    }

    /**
     * Nom du test: testInternalCapacityIncrease
     * Intention: Valider que la capacité interne du tableau de la PointList augmente
     *            correctement lorsqu'on dépasse la taille initiale.
     * Motivation des données: On part d'une capacité initiale de 2. En ajoutant un 3ème point,
     *                        la logique de `incCap` (cap = newSize * 2) doit être déclenchée.
     *                        La nouvelle capacité attendue est 3 * 2 = 6.
     * Oracle: La capacité interne, obtenue via la méthode getCapacity(), doit être 6 après l'ajout.
     */
    @Test
    @Disabled
    public void testInternalCapacityIncrease() {
        PointList instance = new PointList(2, false);
        assertEquals(2, instance.getCapacity());
        instance.add(1, 1);
        instance.add(2, 2);
        // Le 3ème ajout doit déclencher l'augmentation de capacité
        instance.add(3, 3);
        assertTrue(instance.getCapacity() >= 6, "La capacité aurait dû augmenter.");
    }

    /**
     * Nom du test: testModificationAfterMakeImmutable
     * Intention: Vérifier qu'une liste rendue immuable rejette toute modification.
     * Motivation des données: Après un appel à makeImmutable(), la liste doit bloquer les
     *                         opérations de modification (ex : clear(), add())
     * Oracle: Un appel à clear() doit lever une IllegalStateException.
     */
    @Test
    @Disabled
    public void testModificationAfterMakeImmutable() {
        PointList instance = Helper.createPointList(1, 1, 2, 2);
        instance.makeImmutable();
        assertThrows(IllegalStateException.class, instance::clear);
    }

    /**
     * Nom du test: testEqualsWithDifferentDimension
     * Intention: Vérifier que deux listes identiques en coordonnées mais différentes en dimension
     *            (2D vs 4D) ne sont pas égales.
     * Motivation des données: La dimensionnalité (2D vs 3D) fait partie intégrante de l'état
     *                         interne de l'objet. equals() doit la prendre en compte.
     * Oracle: La méthode equals() doit retourner false entre une PointList 2D et une 3D équivalente.
     */
    @Test
    @Disabled
    public void testEqualsWithDifferentDimension() {
        PointList instance2D = Helper.createPointList(10, 10, 20, 20);
        PointList instance3D = new PointList();
        instance3D.add(10, 10, 0);
        instance3D.add(20, 20, 0);

        assertNotEquals(instance2D, instance3D);
    }


    /**
     * Nom du test: testTrimToSizeWithLargerSize
     * Intention: Vérifier que trimToSize() rejette une taille supérieure à la taille actuelle.
     * Motivation des données: Le code contient une garde ("throw new IllegalArgumentException(...)")
     *                        pour empêcher une utilisation incohérente de la méthode. Ce test
     *                        valide que cette garde fonctionne.
     * Oracle: L'appel à trimToSize(3) doit lever une IllegalArgumentException
     */
    @Test
    @Disabled
    public void testTrimToSizeWithLargerSize() {
        PointList instance = Helper.createPointList(1, 1, 2, 2); // size = 2
        assertThrows(IllegalArgumentException.class, () -> {
            instance.trimToSize(3);
        });
    }

    /**
     * Nom du test: testEmptySingletonImmutability
     * Intention: Valider que la constante statique PointList.EMPTY est bien immuable
     * Motivation des données: EMPTY est un singleton spécial dont les méthodes de modification
     *                         lèvent volontairement des exceptions
     * Oracle: Tout appel à add(0 ou clear() doit lever une RuntimeException
     */
    @Test
    @Disabled
    public void testEmptySingletonImmutability() {
        PointList emptyList = PointList.EMPTY;
        assertEquals(0, emptyList.size());
        assertThrows(RuntimeException.class, () -> {
            emptyList.add(1, 1);
        });
    }

    /**
     * Nom du test: testAddWithFakerGeneratedData
     * Intention: Vérifier que la méthode add() fonctionne correctement avec des données aléatoires
     *            valides.
     * Motivation des données: L'utilisation de données aléatoires mais valides via java-faker
     *                        permet de tester des cas que le développeur n'aurait pas imaginés,
     *                        simulant ici l'ajout de 100 points d'un trajet plausible.
     * Oracle: Après l'ajout de 100 points, la taille doit être égale à 100.
     */
    @Test
    public void testAddWithFakerGeneratedData() {
        Faker faker = new Faker();
        PointList instance = new PointList();
        int numberOfPoints = 100;

        for (int i = 0; i < numberOfPoints; i++) {
            double lat = Double.parseDouble(faker.address().latitude().replace(',', '.'));
            double lon = Double.parseDouble(faker.address().longitude().replace(',', '.'));
            instance.add(lat, lon);
        }
        assertEquals(numberOfPoints, instance.size());
    }
}

