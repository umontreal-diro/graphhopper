package com.graphhopper.util;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PointListTest {
    /**
     * Utilitaire pour générer une liste de points (2D) aléatoires à l'aide de `JavaFaker`.
     * @param n nombre de points à générer
     * @return une `PointList` 2D de taille n
     */
    private PointList randomPointList(int n) {
        Faker faker = new Faker();
        PointList list = new PointList();
        for (int i = 0; i < n; i++) {
            list.add(faker.number().randomDouble(10, -90, 90),
                    faker.number().randomDouble(10, -180, 180));
        }
        return list;
    }

    /**
     * Invariance sous inversion <p>
     * BUT: Vérifier qu'inverser une liste 2 fois de suite revient au point de départ. <p>
     * DONNÉES: Une liste de 5 points 2D aux coordonnées aléatoires. <p>
     * ORACLE: La sortie devrait être égale à l'entrée après transformation. <p>
     * COUVERTURE: Couvre l'essentiel des méthodes `clone()` et `reverse()`. <p>
     * MUTANTS: <p>
     *   - On détecte les mutants triviaux qui font échouer la fonction. <p>
     *   - Comme le test de propriété vérifie toutes les valeurs de la liste, on détecte aussi des
     *     mutants en lien avec les bornes et la longueur de la liste.
     */
    @Test
    public void reverseTest() {
        PointList list1 = randomPointList(4);
        PointList list2 = list1.clone(true);
        list2.reverse();

        assertEquals(list1, list2);
    }

    /**
     * Copie par "valeur" <p>
     * BUT: Vérifier que `copy` produit une instance déconnectée de l'originale. <p>
     * DONNÉES: Une liste de 3 points 2D aux coordonnées aléatoires. <p>
     * ORACLE: Les valeurs modifiées après la copie ne devraient pas être égales. <p>
     * COUVERTURE: Couvre l'essentiel de la méthode `copy()`. <p>
     * MUTANTS: <p>
     *   - On détecte les mutants triviaux qui font échouer la fonction. <p>
     *   - Comme on vérifie toutes les valeurs de la liste, et pas seulement la valeur modifiée, on
     *     détecte aussi des mutants en lien avec les bornes et la longueur de la liste.
     */
    @Test
    public void copyTest() {
        PointList list1 = randomPointList(3);
        PointList list2 = list1.copy(0, 3);
        list2.set(1, 0, 0, Double.NaN);

        assertEquals(list1.get(0), list2.get(0));
        assertNotEquals(list1.get(1), list2.get(1));
        assertEquals(list1.get(2), list2.get(2));
    }

    /**
     * Copie par "référence" <p>
     * BUT: Vérifier que `shallowCopy()` produit une instance synchronisée avec l'originale. <p>
     * DONNÉES: Une liste de 3 points 2D aux coordonnées aléatoires. <p>
     * ORACLE: <p>
     *   - Les valeurs modifiées après la copie devraient rester synchronisées. <p>
     *   - Modifier la copie devrait lancer une exception. <p>
     * COUVERTURE: Couvre l'essentiel de la méthode `shallowCopy()`. <p>
     * MUTANTS: On détecte les mutants triviaux qui font échouer la fonction.
     */
    @Test void shallowCopyTest() {
        PointList list1 = randomPointList(3);
        PointList list2 = list1.shallowCopy(0, 3, false);
        list1.set(1, 0, 0, Double.NaN);

        assertEquals(list1.get(0), list2.get(0));
        assertEquals(list1.get(1), list2.get(1));
        assertEquals(list1.get(2), list2.get(2));

        assertThrows(UnsupportedOperationException.class, () -> list2.set(1, 0, 0, Double.NaN));
    }
}
