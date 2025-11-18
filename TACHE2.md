# Tâche 2
William Bisson (20237296) et Jérémie Dupuis (20276905) – IFT3913 A25

Nous avons choisi les classes [`navigation.NavigateResource`](https://github.com/WB667/graphhopper/blob/master/navigation/src/main/java/com/graphhopper/navigation/NavigateResource.java) et [`util.PointList`](https://github.com/WB667/graphhopper/blob/master/web-api/src/main/java/com/graphhopper/util/PointList.java) (dans le dossier `/web-api`).


### [`NavigateResourceTest`](https://github.com/WB667/graphhopper/blob/master/navigation/src/test/java/com/graphhopper/navigation/NavigateResourceTest.java)
Pour **`NavigateResource`**, il existait déjà un test qui couvrait ~9% des instructions, ~10% des branches, et ~10% des mutations.
L'ajout de 4 tests dont un paramétrique a permis d'augmenter la couverture à ~40% des instructions, ~51% des branches, et ~43% des mutations.
1. [getBearing_parsingTest()](https://github.com/WB667/graphhopper/blob/master/navigation/src/test/java/com/graphhopper/navigation/NavigateResourceTest.java#L46): Vérifie quelques cas limites problématiques pour la fonction [`getBearing()`](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/navigation/src/main/java/com/graphhopper/navigation/NavigateResource.java#L267).
2. [doGet_guardsTest()](https://github.com/WB667/graphhopper/blob/master/navigation/src/test/java/com/graphhopper/navigation/NavigateResourceTest.java#L97): Vérifie les gardes initiaux de la fonction [`doGet()`](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/navigation/src/main/java/com/graphhopper/navigation/NavigateResource.java#L85).
3. [doPost_requiresTypeMapbox()](https://github.com/WB667/graphhopper/blob/master/navigation/src/test/java/com/graphhopper/navigation/NavigateResourceTest.java#L149): Vérifie le garde pour la valeur obligatoire de l'attribut `type` dans la fonction [`doPost()`](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/navigation/src/main/java/com/graphhopper/navigation/NavigateResource.java#L158).
4. [doPost_guardsTest()](https://github.com/WB667/graphhopper/blob/master/navigation/src/test/java/com/graphhopper/navigation/NavigateResourceTest.java#L174): Vérifie les gardes initiaux de la fonction [`doPost()`](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/navigation/src/main/java/com/graphhopper/navigation/NavigateResource.java#L158). 

→ Voir le code pour la documentation complète des tests.


### [`PointListTest`](https://github.com/WB667/graphhopper/blob/master/web-api/src/test/java/com/graphhopper/util/PointListTest.java)
Pour **`PointList`**, malgré qu'il n'existait aucun test dédié, d'autres classe de test couvraient ~28% des instructions, ~27% des branches, mais 0% des mutations.
L'ajout de 3 tests utilisant un **Faker** a permis d'augmenter la couverture à ~46% des instructions, ~41% des branches, et ~29% des mutations.
5. [reverseTest()](https://github.com/WB667/graphhopper/blob/master/web-api/src/test/java/com/graphhopper/util/PointListTest.java#L32): Test de propriété pour la fonction [`reverse()`](https://github.com/WB667/graphhopper/blob/master/web-api/src/main/java/com/graphhopper/util/PointList.java#L294).
6. [copyTest()](https://github.com/WB667/graphhopper/blob/master/web-api/src/test/java/com/graphhopper/util/PointListTest.java#L48): Vérifie que la fonction [`copy()`](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/web-api/src/main/java/com/graphhopper/util/PointList.java#L445) produit une instance déconnectée de l'original. 
7. [shallowCopyTest()](https://github.com/WB667/graphhopper/blob/master/web-api/src/test/java/com/graphhopper/util/PointListTest.java#L67): Vérifie que la fonction [`shallowCopy()`](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/web-api/src/main/java/com/graphhopper/util/PointList.java#L477) produit une instance synchronisée à l'original.

→ Voir le code pour la documentation complète des tests.


### Exécuter les tests et générer les rapports
Pour compiler les tests, les exécuter et générer les rapports, exécutez `mvn test-compile test org.pitest:pitest-maven:mutationCoverage` dans les dossiers `/navigation` et `/web-api`.

Pitest est [configuré](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/pom.xml#L308C17-L317C33) pour ne traiter que les classes `NavigateResource` et `PointList`.
