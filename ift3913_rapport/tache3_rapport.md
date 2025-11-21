# Tâche 3 - Rapport - MOUSSA DIABATE & POLZIN

Cette tâche portait sur les Github Actions et sur l'utilisation de mocks, grâce à la librairie Mockito.

## Modification du workflow - ajout de Github Actions

Une action permettant de ne pas accepter un "git push" dont le score de mutation est inférieur au score précédent a été ajoutée.
Pour trouver les scores précédents, nous utilisons les balises : "workflow_search: true" qui va chercher dans les résultats précédents et "workflow_conclusion: success" qui permet de filtrer uniquement les workflow qui ont fonctionnés. Le premier run n'a par définition pas de workflow qui le précède, on utilise alors "continue-on-error: true" pour ce premier tour.

On aimerai désormais générer le score PIT et en extraire les données pertinentes, ici le nombre de mutations tuées, cela grâce à un script shell : 
        
        mvn clean test-compile -pl core org.pitest:pitest-maven:mutationCoverage | tee core/core-score

        Core_Score=$(grep -A15 Statistics core/core-score | grep Killed | grep -oP '\(\K[0-9]+')
        echo "le score de mutation pour le module core est $Core_Score%" 

        if [ -f core-artefact/Prec_Score ]; then
        Prev_Score=$(cat core-artefact/Prec_Score)
        else
        Prev_Score=0
        fi

        if [ "$Core_Score" -lt "$Prev_Score" ]; then
        echo "Le score de mutation dans le module core a baisse $Prev_Score% ---> $Core_Score%"
        exit 1
        fi
        echo "ancien score: $Prev_Score%, nouveau_score: $Core_Score%"
        echo "$Core_Score" > ./Prec_Score

Ce script permet dans un premier temps d'extraire le score de mutation, puis de le comparer au score juste précédent et de prendre une décision en fonction du résultat. Si le score est inférieur, on sort avec "exit 1". Sinon, l'ancien score est mit à jour et prend la valeur du score de mutation calculé.

Ce script ne fonctionne que sur le module "core". Un second script, similaire à celui-ci, permettra de faire la même chose pour le module "reader-gtfs".

Il ne faut pas non plus oublier les artefacts et les mettre à jour : exemple pour le module "core" : 

        - name: 'Upload precedent core-score'
            uses: actions/upload-artifact@v4
            with:
            name: core-artefact-${{ matrix.java-version }}
            path: ./Prec_Score


## Mockito

Nous avons utilisé cette librairie dans les classes "HelperTest.java" du module web-api ainsi que "DownloaderTest.java" de la classe "Util" du module "Core".
Le mock est incontournable pour tester certaines situations. Ici, nous l'utilisons pour tester une fonction qui efface tout un dossier. Nous ne voulons pas effectivement éliminer tous les fichiers, ni en créer expressément pour cette fonction, alors nous utilisons des mocks :

    @InjectMocks
    Helper helper;
    @Mock
    File directory;
    @Mock
    File f1;
    @Mock
    File f2;
    @Mock
    File f3;

    @Test
    public void testRemoveDirectory() {
        when(f1.exists()).thenReturn(true);
        when(f1.delete()).thenReturn(true);

        when(f2.exists()).thenReturn(true);
        when(f2.delete()).thenReturn(true);

        when(f3.exists()).thenReturn(true);
        when(f3.delete()).thenReturn(true);

        when(directory.exists()).thenReturn(true);
        when(directory.delete()).thenReturn(true);

        File[] list = {f1, f2, f3};

        when(directory.isDirectory()).thenReturn(true);
        when(directory.listFiles()).thenReturn(list);

        assertTrue(removeDir(directory));
    }

Nous avons besoin de la vraie classe Helper mais allons utiliser des faux paramètres d'entrées de fonctions comme "removeDir" qui attend un paramètre File (pouvant être un dossier). En quelques lignes seulement, nous ne craignons pas avoir effacé des fichiers importants du projet.

De la même manière : 

    @InjectMocks
    Downloader downloader;

    @Mock
    HttpURLConnection connection;

    @Test
    public void fetchTestNullInputStream(){
        boolean readErrorStreamNoException = false;
        try {
            when(connection.getInputStream()).thenReturn(null);
            assertThrows(IOException.class, ()-> downloader.fetch(connection, readErrorStreamNoException));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

On met en place le mock, on lui dit comment réagir face aux évènements et on vérifie que l'on a bien la réponse attendue. Ici, nous voulons tester le cas où l'utilisateur n'entre rien ou en tout cas une donnée erronée (when(connection.getInputStream()).thenReturn(null)), on s'attend alors à une exception.

## Rickroll

Le but de cette sous tâche est de rickroll l'utilisateur lorsqu'il "git push" et qu'au moins un test ne fonctionne pas.

    - name: Build ${{ matrix.java-version }}
        run: mvn -B clean test

      - name: Rick Roll On Fail
        if: failure()
        run: |
            echo "![RickRoll](https://media.giphy.com/media/Vuw9m5wXviFIQ/giphy.gif)"
            cat .github/workflows/rickroll.txt

Donc lorsqu'un utilisateur push, une suite d'action sont effectuées, en commençant par lancer tous les tests. Au départ, nous avions mit "continue-on-error: true" car nous ne voulions pas que lorsqu'un test fail le reste des tests soient ignorés. Or cela ne change rien, alors en voici une version simplifiée.

On retrouve effectivement un fichier rickroll.txt dans le dossier workflow, il s'agit d'un ASCII Art de Rick Astley. On retrouve ce portrait dans les logs des actions de Github. Le gif en revanche est affiché par URL seulement (comme ci-dessus).
