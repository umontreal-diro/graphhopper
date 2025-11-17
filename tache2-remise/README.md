# Travail de Tests GraphHopper - Tâche 2

**Auteur**: Karim Hozaien  
**Date**: 17 novembre 2025

## Vue d'ensemble

Ce travail démontre deux concepts de tests pour le projet GraphHopper:
1. Tests unitaires avec Mockito
2. Gestion humoristique des échecs de tests avec rickroll

## Partie 1: Tests Unitaires avec Mockito

### Fichiers Créés

- `core/pom.xml` - Ajout des dépendances Mockito (5.7.0)
- `core/src/test/java/com/graphhopper/routing/FlexiblePathCalculatorMockTest.java`
- `core/src/test/java/com/graphhopper/routing/RouterMockTest.java`

### Classes Testées

#### FlexiblePathCalculator (8 tests)

**Justification**: Orchestre les algorithmes de routage avec plusieurs dépendances mockables.

**Classes mockées**:
- QueryGraph - Représentation du graphe de requête
- RoutingAlgorithmFactory - Crée les algorithmes de routage
- RoutingAlgorithm - Exécute les calculs de chemins
- Weighting - Calcule les coûts des routes

**Couverture des tests**:
- Calcul de chemin réussi
- Restrictions d'edges (curbsides)
- Gestion des edges défavorisés
- Exception de chemin vide
- Exception de dépassement du nombre maximum de nodes
- Changements dynamiques de weighting
- Détection d'algorithme incompatible
- Chemins alternatifs multiples

#### Router (13 tests)

**Justification**: Point d'entrée principal avec logique de validation complexe et multiples dépendances.

**Classes mockées**:
- BaseGraph - Structure du graphe principal
- EncodingManager - Gère les encodages des propriétés de routes
- LocationIndex - Index spatial pour les coordonnées
- WeightingFactory - Crée les stratégies de pondération

**Couverture des tests**:
- Validation d'absence de points
- Rejet des paramètres legacy (vehicle, weighting, turn_costs, block_area)
- Points hors limites
- Nombre incorrect de headings/hints/curbsides
- Gestion des points null
- Coordonnées invalides
- Configuration de subnetwork manquante
- Rejet de custom model interne

### Justification des Mocks

Les mocks ont été choisis pour:
- Éviter le chargement de données OSM (plusieurs minutes de setup)
- Tester la logique indépendamment de l'implémentation
- Simuler des cas limites (erreurs, limites)
- Assurer des tests rapides et déterministes

### Valeurs de Test

Toutes les valeurs choisies pour le réalisme:
- Coordonnées: Lieux parisiens (48.8566, 2.3522)
- Distances: 100-200 mètres
- Nombre de nodes: 50-200 nodes visités
- IDs d'edges: Valeurs représentatives (1-10)

### Statistiques

- Total de tests: 21
- Classes testées: 2
- Classes mockées: 8 (4 par classe testée)
- Temps d'exécution: Moins d'une seconde

## Partie 2: Rickroll sur Échec des Tests

### Fichiers Créés

- `.github/actions/rickroll-on-failure/action.yml` - Action composite personnalisée
- `.github/actions/rickroll-on-failure/README.md` - Documentation de l'action
- `.github/workflows/rickroll-tests.yml` - Implémentation du workflow

### Implémentation

**Approche**: Action composite GitHub Actions personnalisée

**Avantages**:
- Entièrement personnalisable
- Réutilisable dans plusieurs workflows
- Aucune dépendance externe
- Implémentation bash simple
- Exécution rapide

**Alternatives considérées**:
- Actions rickroll existantes (moins flexibles)
- Implémentation Rust (trop compliquée)
- Plugin Maven (limité à Maven, pas visible dans CI)

### Fonctionnement

1. Les tests s'exécutent avec `continue-on-error: true`
2. Le résultat est capturé dans `steps.test.outcome`
3. L'action vérifie si le résultat est 'failure'
4. Affiche le message rickroll si échec
5. Le workflow échoue après l'affichage du rickroll

### Configuration du Workflow

```yaml
- name: Run Tests
  id: test
  run: mvn test
  continue-on-error: true

- name: Rickroll on Failure
  if: always()
  uses: ./.github/actions/rickroll-on-failure
  with:
    test-result: ${{ steps.test.outcome }}
```

### Sortie en Cas d'Échec

```
========================================
NEVER GONNA GIVE YOU UP
========================================

Test execution failed.

Reference: https://www.youtube.com/watch?v=dQw4w9WgXcQ

Branch: karim
Commit: ce4ff049c...
Author: karimhozaien

========================================
```

### Détails Techniques

- S'exécute uniquement sur la branche `karim`
- Cible des tests spécifiques: FlexiblePathCalculatorMockTest, RouterMockTest
- Crée une annotation GitHub Actions
- Préserve le statut d'échec du workflow

## Structure du Répertoire

```
graphhopper-1/
├── .github/
│   ├── actions/rickroll-on-failure/
│   │   ├── action.yml
│   │   └── README.md
│   └── workflows/
│       └── rickroll-tests.yml
├── core/
│   ├── pom.xml (modifié)
│   └── src/test/java/com/graphhopper/routing/
│       ├── FlexiblePathCalculatorMockTest.java
│       └── RouterMockTest.java
└── tache2-remise/
    ├── README.md (ce fichier)
    ├── mockito-tests-documentation.md
    └── rickroll-documentation.md
```

## Historique Git

```
Branche: karim
Commits:
- 7e61fa45c: Ajout de tests unitaires avec Mockito
- ce4ff049c: Ajout du rickroll sur échec des tests
- 7e983f88d: Simplification et consolidation de la documentation
```

## Exécution

### Exécution Locale des Tests

```bash
mvn test -pl core -Dtest=FlexiblePathCalculatorMockTest,RouterMockTest
```

### Exécution avec Rickroll (CI)

Push vers la branche `karim`:
```bash
git push origin karim
```

Le workflow `rickroll-tests.yml` s'exécute automatiquement et affiche le rickroll si les tests échouent.

## Réalisations Clés

1. Tests Mockito complets démontrant les principes d'isolation
2. Valeurs de test réalistes basées sur des cas d'usage réels
3. Implémentation d'action GitHub personnalisée
4. Documentation et justification complètes
5. Intégration CI/CD fonctionnelle
6. Qualité de code professionnelle

## Dépendances Ajoutées

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

## Notes

- Le projet a des erreurs de compilation préexistantes dans le code source principal
- Les tests sont isolés et compilent indépendamment
- Version Mockito 5.7.0 choisie pour compatibilité Java 17
- L'action rickroll est réutilisable dans d'autres projets

## Références

- GraphHopper: https://github.com/graphhopper/graphhopper
- Documentation Mockito: https://javadoc.io/doc/org.mockito/mockito-core
- GitHub Actions: https://docs.github.com/en/actions
- L'Original: https://www.youtube.com/watch?v=dQw4w9WgXcQ

