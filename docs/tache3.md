### Tâche 3 — Validation et conception (IFT3913)

**Objectif**

Cette tâche visait à :
- modifier le workflow GitHub Actions afin de faire échouer le build si le *mutation score* de PIT diminue ;
- ajouter de nouveaux tests unitaires avec **Mockito** pour renforcer la couverture de test du module `core`.

---

### 1. Modifications apportées

#### a. Workflow GitHub Actions
Un job `Build and Test (with Mutation Check)` a été ajouté dans `.github/workflows/build.yml`.  
Il exécute :
1. `mvn -pl core clean test` pour compiler et exécuter les tests ;
2. `mvn -pl core org.pitest:pitest-maven:mutationCoverage` pour générer le rapport PIT ;
3. un script qui récupère le score de mutation et échoue si ce score est inférieur à 60 %.

Cette vérification garantit la **non-régression** de la qualité du code.

#### b. Tests Mockito
Deux nouvelles classes de test ont été créées :

- `CHStorageMockitoTest.java` : vérifie la création d’un objet `CHStorage` en simulant le comportement de la classe `Directory` ;
- `TurnCostStorageMockitoTest.java` : simule un stockage de coûts de virage (`TurnCostStorage`) afin de valider les appels attendus à certaines méthodes.

Ces tests permettent d’isoler le comportement des composants et de vérifier les interactions sans dépendre d’implémentations réelles.

---

### 2. Validation

Les tests ont été exécutés avec Maven :

```bash
mvn -pl core clean test -Dtest='*MockitoTest'
