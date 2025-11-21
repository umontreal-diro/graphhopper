# Tâche 3 - Tests d'intégration et automatisation

Amir Hannache - 20060308
Novembre 2025

---

## Tests avec Mocks

### Configuration Mockito

Ajout dans `core/pom.xml` :

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.14.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.14.2</version>
    <scope>test</scope>
</dependency>
```

### Test 1 : DateRangeParserMockTest

**Fichier :** `core/src/test/java/com/graphhopper/reader/osm/conditional/DateRangeParserMockTest.java`

**Classe testée :** DateRangeParser - Parse des plages de dates OpenStreetMap

**Classe mockée :** Calendar

**Pourquoi ?** Éviter la dépendance à l'horloge système et aux timezones. Tester uniquement la logique de parsing.

**Tests :**
- `testCreateParserWithMockedCalendar` : Vérifier l'injection de dépendances
- `testGetRangeWithMockedCalendar` : Tester getRange("Mar-Oct") avec mock

**Configuration mock :** Passage du mockCalendar au constructeur. DateRangeParser crée ses propres Calendar en interne.

### Test 2 : PathMergerMockTest

**Fichier :** `core/src/test/java/com/graphhopper/util/PathMergerMockTest.java`

**Classe testée :** PathMerger - Fusion de chemins de routage

**Classes mockées :** Graph et Weighting

**Pourquoi ?** Graph et Weighting sont complexes (données, calculs). Mocker évite de construire toute l'infrastructure.

**Tests :**
- `testCreatePathMergerWithMockedDependencies` : Créer PathMerger avec mocks
- `testSetCalcPointsWithMockedDependencies` : Tester pattern builder

**Configuration mocks :**
```java
when(mockGraph.wrapWeighting(mockWeighting)).thenReturn(mockWeighting);
```

**Valeurs simulées :** Le mock retourne le Weighting passé en paramètre, comme le fait wrapWeighting normalement.

**Résultats :** 4 tests, 0 échecs

```bash
cd core && mvn test -Dtest=DateRangeParserMockTest,PathMergerMockTest
```

---

## Workflow GitHub Actions

### Modifications apportées

Ajout de 5 étapes dans `.github/workflows/build.yml` :

**1. Exécuter PIT**
```yaml
- name: Run PIT mutation testing
  if: matrix.java-version == 24
  run: |
    cd core
    mvn org.pitest:pitest-maven:mutationCoverage
```

**2. Extraire le score**
```yaml
- name: Extract mutation score
  if: matrix.java-version == 24
  run: |
    MUTATION_SCORE=$(grep -oP '<td>Line Coverage</td><td>\K[0-9]+' core/target/pit-reports/*/index.html | head -1)
    echo "CURRENT_SCORE=$MUTATION_SCORE" >> $GITHUB_ENV
    echo "Current mutation score: $MUTATION_SCORE%"
```

**3. Télécharger le score précédent**
```yaml
- name: Download previous mutation score
  if: matrix.java-version == 24
  continue-on-error: true
  uses: actions/download-artifact@v4
  with:
    name: mutation-score
    path: .
```

**4. Comparer les scores**
```yaml
- name: Compare mutation scores
  if: matrix.java-version == 24
  run: |
    if [ -f mutation_score.txt ]; then
      PREVIOUS_SCORE=$(cat mutation_score.txt)
      echo "Previous mutation score: $PREVIOUS_SCORE%"
      echo "Current mutation score: $CURRENT_SCORE%"

      if [ "$CURRENT_SCORE" -lt "$PREVIOUS_SCORE" ]; then
        echo "Mutation score decreased from $PREVIOUS_SCORE% to $CURRENT_SCORE%"
        exit 1
      else
        echo "Mutation score maintained or improved"
      fi
    else
      echo "No previous score found, saving current score as baseline"
    fi
```

**5. Sauvegarder le nouveau score**
```yaml
- name: Save mutation score
  if: matrix.java-version == 24 && success()
  run: |
    echo "$CURRENT_SCORE" > mutation_score.txt

- name: Upload mutation score
  if: matrix.java-version == 24 && success()
  uses: actions/upload-artifact@v4
  with:
    name: mutation-score
    path: mutation_score.txt
```

### Justifications

- **Java 24 uniquement :** Gagner du temps, pas besoin de tester sur toutes les versions
- **grep pour extraction :** Score dans le HTML généré par PIT
- **continue-on-error :** Premier build n'a pas de score précédent
- **exit 1 si baisse :** Fait échouer le build
- **Sauvegarde si success() :** Artifact disponible pour le prochain build

### Validation

Test avec plusieurs scénarios :
- Premier build : Score sauvegardé comme référence
- Score identique : Build passe
- Score amélioré : Build passe
- Score baissé : Build échoue

---

## Rickroll

Ajout d'une étape d'humour :

```yaml
- name: Rickroll on test failure
  if: failure()
  run: |
    echo "Never gonna give you up"
    echo "Never gonna let you down"
    echo "Never gonna run around and desert you"
    echo "Never gonna make you cry"
    echo "Never gonna say goodbye"
    echo "Never gonna tell a lie and hurt you"
    echo ""
    echo "Watch: https://www.youtube.com/watch?v=dQw4w9WgXcQ"
```

**Choix :** Echo simple au lieu d'action externe. Plus direct et pas de dépendances.
