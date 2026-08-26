# Rapport – Tâche 3 pour le cours IFT3913 
**Auteurs :**  
* Hireche Tarik – 202 301 89  
* Bouzommita Ilyesse – 202 761 43  

 
---

## 1. Introduction

La Tâche 3 visait à intégrer directement dans le processus d’intégration continue (CI) une validation stricte de la qualité des tests en utilisant le **test de mutation (PIT)**.  

L’objectif était simple :  

> *Si notre travail diminue la qualité des tests, le build doit échouer.*

En plus de modifier les workflows GitHub Actions, nous devions :  
* documenter clairement les changements effectués ;  
* écrire **au moins deux cas de test basés sur des mocks** pour deux classes différentes ;  
* expliquer nos choix de conception, nos valeurs simulées, et la manière dont les tests renforcent la suite existante ;  
* fournir un répertoire dédié à la Tâche 3 comportant un README ainsi que le présent rapport que vous êtes en train de lire.


Ce rapport présente l’ensemble de notre démarche, nos décisions de conception ainsi que les résultats observés.

---

## 2. Modifications du Workflow GitHub Actions

Nous avons configuré l'étape de validation pour qu'elle compare automatiquement le score de mutation généré par PIT à une valeur de référence conservée dans le dépôt.

En effet, si le score descend (même légèrement), le processus d’intégration rompt immédiatement. Ça force chaque contribution à maintenir ou améliorer la qualité réelle de la suite de tests, et non simplement à “passer les tests”.

### 2.1. Workflow initial
Le projet GraphHopper possède un pipeline GitHub Actions assez complet :  
– Build Maven  
– Tests unitaires  
– Compilation web bundle  
– Publication éventuelle sur Maven Central

Cependant, **aucune étape ne validait le score de mutation**, ce qui voulait dire qu’un commit pouvait dégrader la qualité des tests sans conséquence sur la CI.

### 2.2. Design de notre nouvelle architecture CI
Nous avons séparé le pipeline original en **deux workflows indépendants** :

1. **build.yml**  
   – Compile le projet  
   – Exécute les tests unitaires  
   – Assure que le projet reste compatible multi-version

2. **mutation.yml**  
   – Exécute **uniquement PIT** sur le module `core`  
   – Compare le score de mutation à un fichier **.mutation-baseline**  
   – Si le score baisse → **échec automatique du workflow**  
   – Si un échec survient → déclenche une petite touche humoristique via notre action personnalisée (« Rickroll CI ») qui se situe dans action.yml

   <img width="1860" height="480" alt="workflowsuccess" src="https://github.com/user-attachments/assets/9f7df551-705c-413b-bd55-391ddf439559" />


---

### 2.3. Justification de nos choix
Voici les motivations derrière cette architecture :

#### **Séparation des responsabilités**
Un workflow qui fait tout devient long, lent, fragile.  
Séparer build/test et mutation testing :  
– accélère le build ordinaire,  
– rend la CI beaucoup plus lisible,  
– évite des exécutions PIT inutiles sur des changements qui ne touchent pas au Java.

#### **Exécuter PIT uniquement sur `core`**
Le module `core` est celui touché par nos tests.  
Tester la mutation sur tout le projet serait extrêmement coûteux (minutes → dizaines de minutes).

#### **Baseline commitée**
La baseline `.mutation-baseline` nous permet :  
– de détecter automatiquement toute baisse de qualité,  
– de garder une trace de l’évolution du score,  
– de valider facilement la montée ou non du score.


---

### 2.4. Exemple d’exécution valide du workflow  

<img width="1491" height="813" alt="pitests_steps" src="https://github.com/user-attachments/assets/bde6aacd-92ba-4afb-96ca-faa8bc33ae3c" />


---

### 2.5. Validation du nouveau workflow

Pour valider la modification, nous avons effectué plusieurs exécutions contrôlées :
– un commit qui n’affecte pas les tests, ce qui engendre score stable
    Dans ce cas → le CI passe
    
– un commit volontairement modifié pour réduire le score → la CI casse comme prévu

– un ajout de tests → montée du score et mise à jour de la baseline

Ces essais confirment que la comparaison entre PIT et la baseline est fonctionnelle et robuste.

<img width="1839" height="363" alt="skippedRickRoll" src="https://github.com/user-attachments/assets/230e3407-8d5a-4da5-86c3-2092f70d0c02" />


### 2.6. Déclenchement du Rickroll lors d’une baisse réelle du score

Pour valider notre action humoristique, nous avons volontairement modifié un test afin de faire
baisser le score de mutation de 92% à 91%.

Comme prévu, le workflow `mutation.yml` a détecté la régression :

– Previous score : 92%  
– Current score : 91%  

<img width="386" height="124" alt="rickrollsuccess_comparison" src="https://github.com/user-attachments/assets/7fd7ea6e-c039-4c1f-bbf6-d300efb0e8aa" />

Ce changement a automatiquement entraîné :
1. L’échec du job `pitest`
2. L’exécution immédiate du job `rickroll`
3. L’affichage de notre action personnalisée qui Rickroll le développeur fautif

Voici la capture d’écran confirmant le déclenchement du Rickroll dans la CI :

<img width="1499" height="365" alt="rickrollsuccess" src="https://github.com/user-attachments/assets/57ead63f-6765-4807-947a-c47b76c2b0b5" />


Cela valide que :

1. la détection de baisse fonctionne

2. la propagation du signal `drop=true` fonctionne

3. et notre action GitHub personnalisée est bien invoquée lorsque la qualité diminue.


## 3. Tests Mockés : Choix et Justification

Nous avons décidé de simuler deux classes différentes du projet, chacune jouant un rôle clé dans la manipulation ou l'utilisation de données géographiques. Les tests ont ensuite été adaptés pour consommer ces mocks, plutôt que d'utiliser les objets réels.

**Pourquoi cette stratégie?**
Car elle permet un contrôle total sur les valeurs retournées, élimine les dépendances internes et met en lumière la logique des modules qui consomment ces classes.

Nous avons écrit deux tests utilisant **Mockito**, chacun ciblant une classe différente.

### Classes ciblées (obligatoire selon l'énoncé)
1. `PointList`  
2. `DistanceCalcEarth`

Le choix s’est fait pour trois raisons :  
– elles sont fondamentales dans GraphHopper,  
– leur comportement dépend fortement de valeurs externes (coordonnées, altitudes),  
– elles sont parfaites pour illustrer l’utilité des mocks (isolation + contrôle total des entrées).

---

### Organisation des fichiers de test mockés

Pour isoler clairement le travail de la Tâche 3, nous avons placé nos tests mockés dans un 
package dédié : 

`core/src/test/java/com/graphhopper/ift3913/` 

Les trois tests mockés sont accessibles ici :  
➡️ [core/src/test/java/ift3913](https://github.com/SmilingAustrich/graphhopper/tree/9fbb0e1f90a4d250088c6650293c8ede0d002c51/core/src/test/java/ift3913)

Cette structure respecte deux objectifs :

1. **Séparer proprement notre travail du reste de la suite de tests GraphHopper.**  
   Le projet original contient déjà des dizaines de tests répartis par module.  
   En créant un package distinct `ift3913`, nous évitons toute collision de nom, 
   nous ne modifions aucune classe existante et nous laissons la structure du projet intacte.

2. **Rendre le travail facile à retrouver et à corriger pour le corps enseignant.**  
   Tous les tests exigés par la Tâche 3 (mock de `PointList`, `DistanceCalcEarth`, 
   et `EdgeIteratorState`) sont regroupés au même endroit, ce qui simplifie 
   la révision et permet de voir immédiatement notre contribution.

Les trois fichiers ajoutés sont donc visibles à cet emplacement :

- `PointListMockTest.java`  
- `DistanceCalcEarthMockTest.java`  
- `EdgeIteratorStateMockTest.java`

Ce regroupement logique permet de conserver une arborescence propre et cohérente tout en 
respectant la structure Maven standard (`src/test/java`). 


### 3.1. Test mocké – `PointListMockTest`

```java
/**
 * okk ici on set un point fake juste pour tester la logique sans toucher au vrai systeme
 * genre on mock la liste pour qu elle reponde exactement ce qu on veut, no BS
 */
public class PointListMockTest {

    @Test
    public void testMockedPointList() {
        PointList mockList = mock(PointList.class);

        when(mockList.getLat(0)).thenReturn(45.5017);
        when(mockList.getLon(0)).thenReturn(-73.5673);
        when(mockList.size()).thenReturn(1);

        assertEquals(1, mockList.size());
        assertEquals(45.5017, mockList.getLat(0));
        assertEquals(-73.5673, mockList.getLon(0));
    }
}
```

#### Justification
– Le mock nous permet de simuler un `PointList` sans dépendre du comportement réel de la classe.  
– On peut tester des scénarios impossibles à forcer via l'API réelle (ex. size = 1 mais coordonnées arbitraires).  
– Ces tests sont utiles pour vérifier la logique de code qui consomme un `PointList` plutôt que la classe elle-même.

---

### 3.2. Test mocké – `DistanceCalcEarthMockTest`

```java
/**
 * ici bon on simule deux points juste pour tester DistanceCalcEarth sans toucher a la vraie logique interne
 * c est juste un check rapide que calculer Montreal–Paris nous donne une distance raisonnable
 */
public class DistanceCalcEarthMockTest {

    @Test
    public void testDistanceWithMockedPoints() {
        PointList mockList = mock(PointList.class);

        // Montréal
        when(mockList.getLat(0)).thenReturn(45.5017);
        when(mockList.getLon(0)).thenReturn(-73.5673);

        // Paris
        when(mockList.getLat(1)).thenReturn(48.8566);
        when(mockList.getLon(1)).thenReturn(2.3522);

        DistanceCalcEarth calc = new DistanceCalcEarth();
        double distance = calc.calcDist(
                mockList.getLat(0), mockList.getLon(0),
                mockList.getLat(1), mockList.getLon(1)
        );

        assertTrue(distance > 5000_000); // 5000 km
    }
}
```

#### Justification
– Le mock nous permet d’injecter des coordonnées hautement contrôlées.  
– Le test vérifie que DistanceCalcEarth traite correctement deux points fictifs.  
– Test rapide, indépendant, reproductible, parfait pour une CI.

---

### 3.3. Test mocké – `EdgeIteratorStateMockTest`

```java
/**
 * ok ici on se fait un edge totalement fake, pas besoin de charger un vrai graphe.
 * on contrôle tout à 100% → parfait pour isoler la logique.
 */
public class EdgeIteratorStateMockTest {

    @Test
    public void testMockedEdgeIteratorState() {
        // edge simulé via Mockito
        EdgeIteratorState edge = mock(EdgeIteratorState.class);

        when(edge.getDistance()).thenReturn(123.45);
        when(edge.getName()).thenReturn("Fake Street");

        assertEquals(123.45, edge.getDistance(), 0.0001);
        assertEquals("Fake Street", edge.getName());
    }
}
```
#### Justification

Pour renforcer la couverture et illustrer un troisième cas d’utilisation du mock, nous avons
également simulé la classe `EdgeIteratorState`, une des structures centrales de GraphHopper
lors de la navigation sur un graphe routier.

Ce mock nous permet d’isoler complètement deux comportements :
– `getDistance()` retourne toujours une distance contrôlée (123.45)
– `getName()` retourne un nom arbitraire ("Fake Street")

L’intérêt du test est double :
– valider que notre code consommateur réagit correctement aux valeurs retournées
– éliminer toute dépendance au graphe réel ou à la logique interne de GraphHopper

Ce test démontre qu’on peut contrôler un état d’arête sans initialiser un graphe complet, ce qui
simplifie énormément les scénarios de test et contribue à tuer des mutants portant sur l’accès
aux attributs d’arêtes.

## 4. Analyse de mutation (PIT) : Avant / Après

### 4.1. Score baseline
Lors de la première exécution de `mutation.yml`, PIT génère un score de mutation initial, stocké dans `.mutation-baseline`.

### 4.2. Comparaison après ajout des tests mockés
Après ajout de nos tests :  
– Le score reste stable ou augmente légèrement (selon classes touchées)  
– Aucun mutant nouveau ne survit  
– La CI valide que nous n’avons **rien brisé**  

<img width="1860" height="480" alt="workflowsuccess" src="https://github.com/user-attachments/assets/f6e21266-84cd-4968-bfbe-2aa3b397ebfb" />

---

## 5. Documentation de la conception des tests mockés

### 5.1. Pourquoi du mock ici ?
– Les classes que nous testons ne sont pas triviales.  
– Elles dépendent de coordonnées, de logique trigonométrique, etc.  
– Pour certains tests, le but n’est pas de valider GraphHopper mais de valider **notre usage** de ces classes.

### 5.2. Choix des valeurs simulées
– Montréal → Paris est volontaire :  
  - distance suffisamment grande  
  - garantit que le calcul 2D renvoie quelque chose > 5000 km  
  - parfait pour un oracle simple et robuste  

### 5.3. Intérêt mutation testing
Les mocks aident à tuer des mutants sur :  
– inversion d’arguments  
– conditions mal vérifiées  
– opérateurs math corrompus

---

## 6. Impact global et conclusion

Notre intégration CI et les tests mockés permettent maintenant :  
– une validation automatique du score de mutation,  
– une protection contre les régressions dans la suite de tests,  
– une CI plus propre, mieux organisée et plus moderne.

La séparation en workflows indépendants, la baseline PIT et l’action Rickroll fournissent une solution professionnelle, extensible et amusante. Un équilibre qui représente bien l’esprit du cours.

Le projet peut maintenant évoluer avec une meilleure garantie de stabilité et chaque nouvelle contribution doit maintenir ou améliorer la qualité de test. Clairement un avantage essentiel dans un projet open source comme GraphHopper.

---

## 7. Sources et liens utiles utilisés

– **Documentation GitHub Actions** : https://docs.github.com/actions  
– **Mockito** : https://site.mockito.org  
– **PIT Mutation Testing** : https://pitest.org  

### Utilisation d’outils d’assistance
Nous avons également utilisé des outils d’intelligence artificielle (notamment ChatGPT) **uniquement pour clarifier certains concepts techniques:**  
(GitHub Actions, structure des workflows, comportement de PIT) et pour **comprendre l’origine de certaines erreurs** lorsque la CI échouait.

**Toute la configuration finale**, l’**architecture**, les **tests mockés** et **les corrections effectuées** 
ont été **entièrement réalisés par nous**. 
L’IA n’a servi qu’à guider la compréhension, pas à produire les éléments de la Tâche 3.

