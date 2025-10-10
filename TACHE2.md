# Tâche 2 : Tests unitaires automatiques

## Étudiant
- Amir Hannache

## Classes sélectionnées
Dans le cadre de cette tâche, j'ai choisi d'améliorer la couverture de tests sur la classe suivante :
- `ParsedCalendar` (classe du package `com.graphhopper.reader.osm.conditional` permettant de représenter une date parsée avec différents niveaux de précision)

Cette classe avait une couverture partielle, avec une couverture de mutation de seulement 70% et 7 mutants survivants.
J'ai créé de nouveaux tests qui ont permis d'atteindre 100% de couverture de mutation pour cette classe et d'améliorer le score global du package de 88% à 93%.

## Documentation des tests

Chaque test est documenté avec :
- **Nom du test**
- **Intention** : comportement visé
- **Motivation des données de test** : pourquoi ces valeurs ont été choisies
- **Explication de l'oracle** : comportement attendu
- **Mutant ciblé** : quel mutant spécifique ce test tue

## ParsedCalendarTest

### 1. `testGetMaxSetsMillisecondCorrectly`
[voir le test dans le fichier source](core/src/test/java/com/graphhopper/reader/osm/conditional/ParsedCalendarTest.java)

- **Intention** : Vérifier que la méthode `getMax()` configure correctement le champ MILLISECOND à sa valeur maximale (999). Cette précision est critique pour les bornes de plages de dates.

- **Motivation des données de test** :
  - Utilisation du parseType `YEAR_MONTH_DAY` car il exerce la logique complète de configuration de date
  - Date choisie: 2024-03-15 (date typique de mi-mois pour éviter les cas limites)
  - Milliseconde initialisée à 0 pour démontrer le changement à 999

- **Explication de l'oracle** :
L'oracle vérifie que `Calendar.MILLISECOND` retourne 999, qui est la valeur maximale selon la documentation de l'API Calendar (`getActualMaximum(Calendar.MILLISECOND)` retourne toujours 999).

- **Mutant ciblé** :
Ligne 58 - "removed call to java/util/Calendar::set(MILLISECOND)"
Si cet appel est supprimé, le test échouera car la milliseconde ne sera pas configurée à 999.

### 2. `testGetMinSetsDayOfMonthCorrectly`
[voir le test dans le fichier source](core/src/test/java/com/graphhopper/reader/osm/conditional/ParsedCalendarTest.java)

- **Intention** : Vérifier que `getMin()` configure DAY_OF_MONTH à 1 (minimum) pour les dates sans jour spécifique (ex: "2024 Mar").

- **Motivation des données de test** :
  - ParseType `YEAR_MONTH` déclenche `isDayless() == true`
  - Cela active la logique de configuration de DAY_OF_MONTH à la ligne 65
  - Jour initialisé à 15 pour démontrer le changement à 1

- **Explication de l'oracle** :
Pour un parseType YEAR_MONTH, `getMin()` doit configurer DAY_OF_MONTH à 1 (premier jour du mois) pour créer le timestamp le plus précoce possible. Vérifié par `getActualMinimum(DAY_OF_MONTH) == 1`.

- **Mutant ciblé** :
Ligne 65 - "removed call to java/util/Calendar::set(DAY_OF_MONTH)"

### 3. `testGetMinSetsHourOfDayCorrectly`
[voir le test dans le fichier source](core/src/test/java/com/graphhopper/reader/osm/conditional/ParsedCalendarTest.java)

- **Intention** : Vérifier que `getMin()` configure HOUR_OF_DAY à 0 (minuit) pour représenter le début d'une journée.

- **Motivation des données de test** :
  - ParseType `MONTH_DAY` pour créer une date sans année (ex: "Mar 15")
  - Heure initialisée à 14 (2 PM) pour démontrer clairement le changement à 0

- **Explication de l'oracle** :
HOUR_OF_DAY doit être 0 (minuit en format 24h), valeur minimale retournée par `getActualMinimum(HOUR_OF_DAY)`. Combiné avec minute=0, seconde=0, milliseconde=0, cela crée 00:00:00.000.

- **Mutant ciblé** :
Ligne 67 - "removed call to java/util/Calendar::set(HOUR_OF_DAY)"

### 4. `testGetMinSetsMinuteCorrectly`
[voir le test dans le fichier source](core/src/test/java/com/graphhopper/reader/osm/conditional/ParsedCalendarTest.java)

- **Intention** : Vérifier que `getMin()` configure MINUTE à 0 pour représenter le début d'une heure.

- **Motivation des données de test** :
  - ParseType `YEAR_MONTH_DAY` avec date 2024-03-15
  - Minute initialisée à 45 pour démontrer le changement à 0

- **Explication de l'oracle** :
MINUTE doit être 0 (`getActualMinimum` retourne 0). Combiné avec HOUR=0, cela donne le format HH:00.

- **Mutant ciblé** :
Ligne 68 - "removed call to java/util/Calendar::set(MINUTE)"

### 5. `testGetMinSetsSecondCorrectly`
[voir le test dans le fichier source](core/src/test/java/com/graphhopper/reader/osm/conditional/ParsedCalendarTest.java)

- **Intention** : Vérifier que `getMin()` configure SECOND à 0 pour représenter le début d'une minute.

- **Motivation des données de test** :
  - ParseType `DAY` (ex: "Monday")
  - Seconde initialisée à 45 pour démontrer le changement à 0

- **Explication de l'oracle** :
SECOND doit être 0 (`getActualMinimum` retourne 0). Cela donne le format HH:MM:00.

- **Mutant ciblé** :
Ligne 69 - "removed call to java/util/Calendar::set(SECOND)"

### 6. `testGetMinSetsMillisecondCorrectly`
[voir le test dans le fichier source](core/src/test/java/com/graphhopper/reader/osm/conditional/ParsedCalendarTest.java)

- **Intention** : Vérifier que `getMin()` configure MILLISECOND à 0 pour la précision maximale dans le timestamp minimum.

- **Motivation des données de test** :
  - ParseType `MONTH` (ex: "March")
  - Milliseconde initialisée à 500 pour démontrer le changement

- **Explication de l'oracle** :
MILLISECOND doit être 0 (`getActualMinimum` retourne 0). C'est le point de précision temporelle minimale, donnant le format HH:MM:SS.000.

- **Mutant ciblé** :
Ligne 70 - "removed call to java/util/Calendar::set(MILLISECOND)"

### 7. `testToStringWithRandomDateFromFaker`
[voir le test dans le fichier source](core/src/test/java/com/graphhopper/reader/osm/conditional/ParsedCalendarTest.java)

- **Intention** : Tester la méthode `toString()` qui n'avait aucune couverture (NO_COVERAGE). Cette méthode doit retourner une chaîne combinant le ParseType et la date formatée.

- **Motivation des données de test** :
Utilisation de **Java-Faker** pour générer des dates aléatoires réalistes. Cette approche démontre que `toString()` fonctionne correctement avec diverses valeurs de dates, pas seulement des cas hardcodés.

- **Pourquoi Java-Faker est approprié** :
  1. `toString()` doit fonctionner avec N'IMPORTE QUELLE date valide
  2. Les dates aléatoires aident à détecter des bugs de formatage que des dates fixes pourraient manquer
  3. Valide que la méthode gère la variabilité des données du monde réel (différents mois, années, etc.)

- **Explication de l'oracle** :
L'oracle vérifie que:
  1. La valeur retournée n'est pas null
  2. La valeur retournée n'est pas une chaîne vide
  3. La chaîne contient le nom du ParseType (ex: "YEAR_MONTH_DAY")
  4. La chaîne contient un séparateur point-virgule (format: "ParseType; DateFormatée")

Je ne vérifie pas la date exacte formatée car Faker génère des dates aléatoires, mais je vérifie que la structure est correcte.

- **Mutant ciblé** :
Ligne 77 - "replaced return value with """
Si le mutant retourne une chaîne vide, les assertions échoueront.

## Tests avec java-faker

Le test `testToStringWithRandomDateFromFaker` utilise la bibliothèque java-faker pour générer des dates aléatoires. Cette approche permet de tester la robustesse de la méthode `toString()` avec une variété de valeurs d'entrée plutôt que des valeurs fixes.

## Résultats mutation (PITEST)

J'ai exécuté **PITEST** sur la classe `ParsedCalendar`. Le plugin a été ajouté dans le pom.xml du module graphhopper.core (voir [ici](core/pom.xml#L176)).

### Génération du rapport

Pour générer un rapport PIT dans core/target/pit-reports/index.html, exécutez la commande:

```bash
mvn org.pitest:pitest-maven:mutationCoverage -pl core
```

dans le dossier racine du projet graphhopper.

### Avant nos ajouts

**Score initial (package complet):**
- Couverture de mutation: 88% (121/137 mutants tués)
- Mutants générés: 137
- Mutants tués: 121
- Mutants survivants: 16

**Score initial (ParsedCalendar uniquement):**
- Couverture de mutation: 70% (16/23 mutants tués)
- Mutants générés: 23
- Mutants tués: 16
- Mutants survivants: 7

[Voir le rapport PiTest initial complet](core/target/pit-reports/index.html)

### Mutations tuées par mes ajouts

#### ParsedCalendar.getMax()

Le test `testGetMaxSetsMillisecondCorrectly` tue la mutation qui supprime l'appel `Calendar.set(MILLISECOND, 999)` à la ligne 58. L'oracle vérifie précisément que la valeur de MILLISECOND est 999 après l'appel à `getMax()`.

[Voir les mutations de ParsedCalendar.getMax()](core/target/pit-reports/com.graphhopper.reader.osm.conditional/ParsedCalendar.java.html#L58)

#### ParsedCalendar.getMin()

Les tests 2 à 6 (`testGetMinSetsDayOfMonthCorrectly`, `testGetMinSetsHourOfDayCorrectly`, `testGetMinSetsMinuteCorrectly`, `testGetMinSetsSecondCorrectly`, `testGetMinSetsMillisecondCorrectly`) tuent les mutations qui suppriment les appels `Calendar.set()` pour chaque champ de temps aux lignes 65, 67, 68, 69 et 70.

Chaque test cible un champ spécifique (DAY_OF_MONTH, HOUR_OF_DAY, MINUTE, SECOND, MILLISECOND) avec un oracle précis qui vérifie que le champ a été configuré à sa valeur minimale.

[Voir les mutations de ParsedCalendar.getMin()](core/target/pit-reports/com.graphhopper.reader.osm.conditional/ParsedCalendar.java.html#L65)

#### ParsedCalendar.toString()

Le test `testToStringWithRandomDateFromFaker` tue la mutation qui remplace la valeur de retour par une chaîne vide à la ligne 77. L'oracle vérifie que la chaîne retournée n'est pas vide et contient les éléments attendus (nom du ParseType et séparateur).

[Voir les mutations de ParsedCalendar.toString()](core/target/pit-reports/com.graphhopper.reader.osm.conditional/ParsedCalendar.java.html#L77)

### Après mes ajouts

**Score global (package complet):**
- Couverture de mutation: **93%** (128/137 mutants tués)
- Amélioration: **+5%** (de 88% à 93%)
- Nouveaux mutants détectés: **7**

**Score ParsedCalendar:**
- Couverture de mutation: **100%** (23/23 mutants tués)
- Amélioration: **+30%** (de 70% à 100%)
- Couverture de ligne: **100%** (22/22 lignes)
- Force des tests: **100%**

[Voir le rapport PiTest final complet](core/target/pit-reports/index.html)

[Voir le rapport détaillé de ParsedCalendar](core/target/pit-reports/com.graphhopper.reader.osm.conditional/ParsedCalendar.java.html)

Ces résultats montrent que mes tests ont significativement augmenté le score de mutation et renforcé la couverture effective.

## Résultats d'exécution
- Les **7 nouveaux tests** s'exécutent avec succès (`mvn test -pl core`).
- Tous les tests du module passent : 2558 tests exécutés, 0 échecs, 0 erreurs, 19 ignorés.
- **BUILD SUCCESS**

## Configuration du projet

### Ajout de PiTest

**Fichier modifié:** `core/pom.xml` (lignes 176-200)

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.0</version>
    <dependencies>
        <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.1</version>
        </dependency>
    </dependencies>
    <configuration>
        <targetClasses>
            <param>com.graphhopper.reader.osm.conditional.*</param>
        </targetClasses>
        <targetTests>
            <param>com.graphhopper.reader.osm.conditional.*</param>
        </targetTests>
        <outputFormats>
            <outputFormat>HTML</outputFormat>
            <outputFormat>XML</outputFormat>
        </outputFormats>
    </configuration>
</plugin>
```

### Ajout de Java-Faker

**Fichier modifié:** `core/pom.xml` (lignes 123-129)

```xml
<dependency>
    <groupId>com.github.javafaker</groupId>
    <artifactId>javafaker</artifactId>
    <version>1.0.2</version>
    <scope>test</scope>
</dependency>
```

## Conclusion
- 7 nouveaux tests ajoutés (dont 1 avec java-faker)
- Tous les tests passent localement et toutes les validations réussissent
- **Amélioration significative du score de mutation** : +5% global, +30% pour ParsedCalendar
- **Couverture de mutation de 100%** atteinte pour la classe ParsedCalendar
- Les 7 mutants survivants ont tous été tués par les nouveaux tests
- Amélioration de la couverture du code et de la force des tests
