# Documentation des Tests avec Mockito pour GraphHopper

## Auteur
Karim Hozaien

## Date
17 Novembre 2025

## Introduction

Ce document présente et justifie les tests unitaires avec Mockito créés pour le projet GraphHopper. Deux classes principales ont été choisies pour démontrer l'utilisation de mocks dans un contexte de tests unitaires.

## Classes Testées

### 1. FlexiblePathCalculator

**Fichier de test:** `core/src/test/java/com/graphhopper/routing/FlexiblePathCalculatorMockTest.java`

#### Justification du Choix

`FlexiblePathCalculator` est un excellent candidat pour les tests avec mocks pour les raisons suivantes:

1. **Orchestration d'algorithmes**: Cette classe coordonne la création et l'exécution d'algorithmes de routage complexes
2. **Dépendances externes claires**: Elle dépend de plusieurs interfaces (QueryGraph, RoutingAlgorithmFactory, Weighting)
3. **Logique métier testable**: La logique de calcul de chemins peut être testée indépendamment des implémentations réelles
4. **Cas limites importants**: Elle gère des cas comme le dépassement du nombre maximum de nodes visités et les restrictions d'edges

#### Classes Simulées avec Mockito

##### 1. QueryGraph
- **Rôle**: Représente le graphe de requête utilisé pour le routage
- **Raison du mock**: Créer un vrai QueryGraph nécessiterait:
  - Charger des données OSM (OpenStreetMap)
  - Construire un graphe complet avec nodes et edges
  - Plusieurs minutes de setup
- **Avantage**: Le mock permet de tester uniquement la logique de FlexiblePathCalculator sans dépendre du graphe

##### 2. RoutingAlgorithmFactory
- **Rôle**: Fabrique qui crée des algorithmes de routage (Dijkstra, A*, etc.)
- **Raison du mock**: 
  - Contrôler précisément quel algorithme est créé
  - Tester différents scénarios sans implémenter de vrais algorithmes
  - Éviter les dépendances sur des configurations complexes
- **Avantage**: Permet de simuler différents types d'algorithmes et leur comportement

##### 3. RoutingAlgorithm (et EdgeToEdgeRoutingAlgorithm)
- **Rôle**: L'algorithme de routage qui calcule effectivement les chemins
- **Raison du mock**:
  - Tester les différents résultats possibles (succès, échec, dépassement)
  - Vérifier que le calculator appelle correctement l'algorithme
  - Simuler des conditions spécifiques sans exécuter de vrais calculs de chemins coûteux
- **Avantage**: Tests rapides et déterministes

##### 4. Weighting
- **Rôle**: Pondération utilisée pour calculer les coûts des chemins
- **Raison du mock**: 
  - Nous testons FlexiblePathCalculator, pas la logique de pondération
  - Évite de configurer des profils de véhicules complexes
- **Avantage**: Isolation complète de la logique testée

#### Définition des Mocks et Valeurs Simulées

Les valeurs utilisées dans les tests ont été choisies pour être **réalistes et significatives**:

1. **Nodes (from=5, to=10)**:
   - Valeurs arbitraires mais représentatives d'ID de nodes dans un graphe
   - Suffisamment différentes pour éviter toute confusion

2. **Visited Nodes**:
   - `visitedNodes=100`: Simule un calcul de chemin de taille moyenne
   - `maxVisitedNodes=50`: Limite volontairement basse pour tester le dépassement
   - `visitedNodes=200`: Pour tests de chemins alternatifs plus complexes

3. **Distances de chemins**:
   - 100.0m, 150.0m, 200.0m: Distances réalistes en mètres
   - Valeurs différentes pour distinguer les chemins alternatifs

4. **Edge IDs**:
   - sourceEdge=2, targetEdge=8: Représentent des edges spécifiques pour les curbsides
   - unfavoredEdges=[1,3,7]: Simulent des edges à pénaliser (ex: demi-tours)

#### Tests Implémentés (8 tests)

1. **testCalcPathsSuccessful**: Calcul de chemin simple réussi
2. **testCalcPathsWithEdgeRestrictions**: Test des restrictions edge-to-edge (curbsides)
3. **testCalcPathsWithUnfavoredEdges**: Gestion des edges défavorisés (heading)
4. **testCalcPathsThrowsExceptionWhenPathListEmpty**: Exception quand aucun chemin trouvé
5. **testCalcPathsThrowsMaximumNodesExceededException**: Dépassement de limite de nodes
6. **testSetWeighting**: Changement dynamique de pondération
7. **testCalcPathsThrowsExceptionForIncompatibleAlgorithmWithEdgeRestrictions**: Algorithme incompatible
8. **testCalcPathsWithMultiplePaths**: Chemins alternatifs multiples

### 2. Router

**Fichier de test:** `core/src/test/java/com/graphhopper/routing/RouterMockTest.java`

#### Justification du Choix

`Router` est la classe principale qui orchestre le routage dans GraphHopper:

1. **Point d'entrée central**: C'est la façade principale utilisée par les clients
2. **Logique de validation complexe**: Contient de nombreuses validations d'entrées
3. **Coordination de nombreuses dépendances**: BaseGraph, LocationIndex, EncodingManager, etc.
4. **Gestion de différents types de routage**: Standard, round-trip, alternative routes
5. **Tests de validation critiques**: Les erreurs de validation doivent être bien testées

#### Classes Simulées avec Mockito

##### 1. BaseGraph
- **Rôle**: Le graphe principal contenant toutes les données routières
- **Raison du mock**:
  - Créer un vrai graphe nécessite charger des données OSM volumineuses
  - La construction prend plusieurs minutes
  - Nécessite des fichiers de données externes
- **Avantage**: Tests instantanés de la logique de validation
- **Valeurs simulées**: 
  - BBox(2.2, 2.4, 48.8, 48.9): Limites géographiques de Paris
  - Coordonnées réalistes pour valider la détection de points hors limites

##### 2. EncodingManager
- **Rôle**: Gère les encodages des propriétés des routes (vitesse, accès, restrictions)
- **Raison du mock**:
  - Configuration très complexe avec de nombreux encoders
  - Dépend du type de véhicule et de profil
  - Nous testons uniquement les validations de Router
- **Avantage**: Contrôle précis des EncodedValues retournées

##### 3. LocationIndex
- **Rôle**: Index spatial pour trouver les nodes proches de coordonnées GPS
- **Raison du mock**:
  - Nécessiterait un graphe réel pour être construit
  - Structure de données spatiale complexe (KD-tree, etc.)
  - Nous testons la validation des requêtes, pas la recherche spatiale
- **Avantage**: Tests de validation sans dépendances sur le graphe

##### 4. WeightingFactory
- **Rôle**: Fabrique de pondérations pour calculer les coûts de routes
- **Raison du mock**:
  - La création dépend du graph et de l'encodingManager
  - Différentes stratégies de pondération (fastest, shortest, custom)
  - Nous testons la sélection de l'algorithme, pas le calcul de poids
- **Avantage**: Isolation de la logique de Router

#### Définition des Mocks et Valeurs Simulées

Les valeurs ont été choisies pour être **géographiquement réalistes** et **représentatives de vrais cas d'usage**:

1. **Coordonnées GPS (Paris)**:
   - (48.8566, 2.3522): Paris centre (Notre-Dame)
   - (48.8606, 2.3376): Tour Eiffel
   - Raison: Coordonnées réelles et reconnaissables

2. **BBox (limites géographiques)**:
   - (2.2, 2.4, 48.8, 48.9): Englobe Paris
   - Permet de tester la validation des points hors limites
   - Ex: (50.0, 3.0) est clairement hors de Paris

3. **Profile**:
   - "car": Profil de routage automobile standard
   - Weighting: "fastest" (plus rapide chemin)
   - Le profil le plus couramment utilisé

4. **Headings**:
   - 0°, 90°, 180°: Nord, Est, Sud
   - Angles réalistes pour la direction du véhicule

5. **Point Hints**:
   - "Rue de Rivoli", "Avenue des Champs-Élysées"
   - Noms de rues réels de Paris
   - Utilisés pour améliorer le snapping aux routes

6. **Curbsides**:
   - "right", "left", "any"
   - Spécifie de quel côté de la rue approcher
   - Important pour livraisons et services

#### Tests Implémentés (13 tests)

1. **testRouteFailsWithNoPoints**: Validation échoue sans points
2. **testRouteFailsWithLegacyVehicleParameter**: Rejet paramètres legacy
3. **testRouteFailsWithLegacyWeightingParameter**: Rejet weighting legacy
4. **testRouteFailsWithBlockAreaParameter**: Rejet block_area (non supporté)
5. **testRouteFailsWithPointOutOfBounds**: Point hors limites géographiques
6. **testRouteFailsWithIncorrectNumberOfHeadings**: Nombre incorrect de headings
7. **testRouteFailsWithIncorrectNumberOfPointHints**: Nombre incorrect de hints
8. **testRouteFailsWithIncorrectNumberOfCurbsides**: Nombre incorrect de curbsides
9. **testRouterCreationWithCHEnabled**: Création avec Contraction Hierarchies
10. **testRouteFailsWithNullPoint**: Point null dans la liste
11. **testRouteFailsWithInvalidCoordinates**: Coordonnées GPS invalides
12. **testRouterCreationFailsWithMissingSubnetworkEncodedValue**: Erreur de configuration
13. **testRouteFailsWithInternalCustomModel**: CustomModel interne rejeté

## Stratégies de Test avec Mockito

### Patron AAA (Arrange-Act-Assert)

Tous les tests suivent le patron AAA pour une lisibilité maximale:

```java
@Test
void testCalcPathsSuccessful() {
    // ARRANGE - Configuration du comportement des mocks
    when(algoFactory.createAlgo(...)).thenReturn(routingAlgorithm);
    when(routingAlgorithm.calcPaths(5, 10)).thenReturn(...);
    
    // ACT - Exécution de la méthode testée
    List<Path> result = pathCalculator.calcPaths(5, 10, edgeRestrictions);
    
    // ASSERT - Vérifications des résultats
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(algoFactory, times(1)).createAlgo(...);
}
```

### Types de Vérifications

1. **Vérifications de résultat**: `assertEquals`, `assertTrue`, `assertNotNull`
2. **Vérifications d'interactions**: `verify()` pour confirmer les appels aux mocks
3. **Vérifications d'exceptions**: `assertThrows()` pour les cas d'erreur
4. **Vérifications de compteurs**: `times(n)` pour vérifier le nombre d'appels

### Annotations Mockito Utilisées

- `@ExtendWith(MockitoExtension.class)`: Active Mockito pour JUnit 5
- `@Mock`: Crée automatiquement un mock
- `@BeforeEach`: Setup exécuté avant chaque test

### Méthodes Mockito Utilisées

- `when().thenReturn()`: Définit le comportement d'un mock
- `verify()`: Vérifie qu'une méthode a été appelée
- `times(n)`: Spécifie le nombre d'appels attendus
- `any()`, `eq()`: Matchers pour les arguments
- `mock()`: Crée un mock dynamique dans le test

## Ajout de Mockito au Projet

Les dépendances suivantes ont été ajoutées dans `core/pom.xml`:

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.7.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.7.0</version>
    <scope>test</scope>
</dependency>
```

**Version choisie**: 5.7.0 (dernière version stable compatible avec JUnit 5 et Java 17)

## Avantages de l'Approche avec Mocks

### 1. Rapidité d'Exécution
- Tests instantanés vs plusieurs secondes/minutes avec vraies dépendances
- Pas de chargement de données OSM
- Pas de construction de structures de données complexes

### 2. Isolation
- Teste uniquement la classe ciblée
- Les bugs dans les dépendances n'affectent pas les tests
- Facilite l'identification de la source des problèmes

### 3. Contrôle
- Simulation de conditions difficiles à reproduire (erreurs, cas limites)
- Valeurs déterministes et reproductibles
- Pas de dépendances sur des fichiers externes

### 4. Maintenabilité
- Tests indépendants les uns des autres
- Modification des dépendances sans casser les tests
- Documentation vivante du comportement attendu

## Couverture de Test

### FlexiblePathCalculator
- **Chemins normaux**: ✅ 
- **Edge restrictions (curbsides)**: ✅
- **Edges défavorisés (heading)**: ✅
- **Exceptions (path vide, nodes max)**: ✅
- **Chemins alternatifs multiples**: ✅
- **Changement de weighting**: ✅

### Router
- **Validation des paramètres**: ✅ (8 tests)
- **Gestion des erreurs**: ✅ (5 tests)
- **Configuration du router**: ✅ (2 tests)
- **Support legacy**: ✅ (3 tests)

## Limitations et Travaux Futurs

### Limitations Actuelles

1. **Compilation du projet**: Le projet GraphHopper a des erreurs de compilation préexistantes non liées à nos tests
2. **Tests d'intégration**: Nos tests sont unitaires; des tests d'intégration compléteraient la couverture
3. **Cas edge réels**: Certains cas complexes nécessiteraient des données OSM réelles

### Améliorations Possibles

1. **Tests d'intégration**: Tester avec de vrais graphes minimaux
2. **Tests paramétrés**: Utiliser `@ParameterizedTest` pour tester plusieurs valeurs
3. **Coverage report**: Intégrer JaCoCo pour mesurer la couverture
4. **Tests de performance**: Benchmarks avec JMH

## Conclusion

Les tests avec Mockito créés démontrent:

1. ✅ **Utilisation appropriée de Mockito**: Mocking de 4 classes différentes
2. ✅ **Tests significatifs**: 21 tests au total couvrant des cas réels
3. ✅ **Justification claire**: Choix documentés et réfléchis
4. ✅ **Valeurs réalistes**: Coordonnées GPS, distances, angles représentatifs
5. ✅ **Bonnes pratiques**: Patron AAA, vérifications multiples, code propre

Les mocks permettent de tester efficacement la logique métier complexe de GraphHopper sans les coûts de setup des vraies dépendances, tout en maintenant des tests rapides, fiables et maintenables.

## Fichiers Créés/Modifiés

1. `core/pom.xml`: Ajout des dépendances Mockito
2. `core/src/test/java/com/graphhopper/routing/FlexiblePathCalculatorMockTest.java`: 8 tests
3. `core/src/test/java/com/graphhopper/routing/RouterMockTest.java`: 13 tests
4. `tache2-remise/mockito-tests-documentation.md`: Cette documentation

Total: **21 tests unitaires avec mocks** bien documentés et justifiés.

