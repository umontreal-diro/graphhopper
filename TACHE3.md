# Tâche 3
William Bisson (20237296) et Jérémie Dupuis (20276905) – IFT3913 A25

Nous avons choisi à nouveau la classe [`navigation.NavigateResource`](https://github.com/WB667/graphhopper/blob/master/navigation/src/main/java/com/graphhopper/navigation/NavigateResource.java) pour la tâche 3.


### [`NavigateResourceTest`](https://github.com/WB667/graphhopper/blob/master/navigation/src/test/java/com/graphhopper/navigation/NavigateResourceTest.java)
Pour **`NavigateResource`**, on avait déjà une couverture de ~40% des instructions, ~51% des branches, et ~43% des mutations.
L'ajout de 2 tests utilisant des mocks a permis d'augmenter la couverture à ~76% des instructions, ~75% des branches, et ~61% des mutations.
1. [doGet_okResponse()](https://github.com/WB667/graphhopper/blob/master/navigation/src/test/java/com/graphhopper/navigation/NavigateResourceTest.java#L270): Vérifie que [`doGet()`](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/navigation/src/main/java/com/graphhopper/navigation/NavigateResource.java#L85) retourne une réponse acceptable lorsque la demande est valide.
2. [doGet_errorResponse()](https://github.com/WB667/graphhopper/blob/master/navigation/src/test/java/com/graphhopper/navigation/NavigateResourceTest.java#L293): Vérifie que [`doGet()`](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/navigation/src/main/java/com/graphhopper/navigation/NavigateResource.java#L85) retourne une erreur lorsque la demande est invalide.

Ces deux tests utilisent les deux mêmes **mocks**: un pour la classe `HttpServletRequest` de requête HTTP, et un pour la classe `TranslationMap` de traduction.

→ Voir le code pour la documentation complète des tests.


### Exécuter les tests et générer les rapports
Pour compiler les tests, les exécuter et générer les rapports, exécutez `mvn test-compile test org.pitest:pitest-maven:mutationCoverage` dans le dossier `/navigation`.

Pitest est [configuré](https://github.com/WB667/graphhopper/blob/ca5bc248917f752ba14ddcfb597b61bb787cce0c/pom.xml#L308C17-L317C33) pour ne traiter que les classes `NavigateResource` et `PointList`.


### GitHub Action
Nous avons ajouter le workflow GitHub [Mutation Testing](https://github.com/WB667/graphhopper/blob/master/.github/workflows/mutation.yml), qui vérifie à chaque push que le score de mutation ne descend pas.

Le workflow exécute les étapes suivantes:
1. Les classes sous tests sont compilées.
2. PIT est exécuté avec la commande `mvn -B -pl navigation,web-api -am org.pitest:pitest-maven:1.20.3:mutationCoverage`. Le score de mutation est calculé en divisant le nombre de mutants détectés par le nombre de mutants générés
3. Le score précédent est téléchargé à l'aide de l'action [`dawidd6/action-download-artifact`](https://github.com/dawidd6/action-download-artifact). 
4. Le score est comparé au score précédent. Si le nouveau score est plus faible que le précédent, le workflow s'arrête, et une erreur est affichée.
5. Le nouveau score est sauvegardé en tant qu'artefact à l'aide l'action prédéfinie [`upload-artifact`](https://github.com/actions/upload-artifact).
