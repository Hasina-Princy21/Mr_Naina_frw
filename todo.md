# TODO: Flux d'upload (explication)

But: expliquer comment fonctionne l'upload dans le projet et où regarder si ça casse.

1) Résumé rapide
- L'utilisateur soumet le formulaire `POST /upload/save` (formulaire `enctype="multipart/form-data"`).
- Le `FrontServlet` reçoit la requête et délègue à `DispatcherServlet.invoke()`.
- `DispatcherServlet` trouve la méthode du contrôleur et bind les paramètres : pour un fichier il appelle `request.getPart(name)` et crée un `UploadedFile`.
- Le contrôleur (`TestController.uploadSave`) valide le `UploadedFile` et utilise `FileUploadUtil.saveFile(...)` pour écrire le fichier sur disque.

2) Fichiers et classes clés
- Formulaire client : test/src/main/webapp/WEB-INF/views/upload.jsp (input `name="file"`, `enctype="multipart/form-data"`).
- Servlet frontal : framework/src/main/java/com/hasinaFramework/FrontServlet.java — route et appel de `DispatcherServlet`.
- Routage et binding : framework/src/main/java/com/hasinaFramework/DispatcherServlet.java — crée `UploadedFile` depuis `Part`.
- DTO fichier : framework/src/main/java/com/hasinaFramework/util/UploadedFile.java.
- Contrôleur : framework/src/main/java/com/hasinaFramework/controller/TestController.java (méthode `uploadSave`).
- Sauvegarde disque : framework/src/main/java/com/hasinaFramework/util/FileUploadUtil.java.
- Déploiement/web.xml (multipart-config) : test/src/main/webapp/WEB-INF/web.xml (doit contenir `<multipart-config>` pour `FrontServlet`).

3) Flux d'appel détaillé
- Navigateur envoie POST multipart -> FrontServlet.service(req,res).
- `FrontServlet` calcule le `path` et appelle `dispatcherServlet.invoke(path, req)`.
- `DispatcherServlet` :
  - Trouve la méthode du contrôleur via les annotations `@PostMapping`.
  - Pour chaque paramètre annoté `@RequestParam("file")` et de type `UploadedFile`, il appelle `request.getPart("file")`.
  - Si `Part` est présent et `getSubmittedFileName()` non vide -> crée `UploadedFile(name, contentType, size, inputStream)`.
  - Sinon logge un message diagnostique (`[DEBUG multipart]` et/ou `Fichier présent mais nom vide`).
- `TestController.uploadSave(UploadedFile file)` reçoit l'objet :
  - Vérifie `file != null`, `file.getFileName()` et `file.getInputStream()` (ou `getBytes()` fallback).
  - Appelle `FileUploadUtil.saveFile(fileName, inputStream)` qui crée le dossier `uploads` si besoin et copie le flux.
  - Retourne message de succès ou erreur.

4) Diagnostics courants et vérifications
- Logs utiles :
  - `[DEBUG multipart] part name=..., submittedFileName=..., size=..., contentType=...` (liste des parts)
  - `[UPLOAD] Received UploadedFile: fileName=..., size=...` (log dans `TestController`)
  - `Fichier présent mais nom vide pour param: file` (si Part présent mais submittedFileName vide)
- Si vous voyez `part` avec `submittedFileName` non vide mais `UploadedFile.fileName == null` : probablement l'artefact déployé (JAR/WAR) n'est pas à jour ; rebuild + copier le JAR de `framework` dans `test/src/main/webapp/WEB-INF/lib` ou `mvn install`.
- Vérifier que `web.xml` déployé (dans le WAR) contient `<multipart-config>` pour `FrontServlet` si vous dépendez de la config declarative.
- Vérifier le conteneur : `request.getPart()` nécessite Servlet 3.x+. Assurez-vous que le serveur (Tomcat/Jetty) supporte la version Jakarta utilisée.

5) Étapes reproductibles (rapide)
- Rebuild framework :
```
mvn -DskipTests package -f framework/pom.xml
```
- Copier le JAR dans le projet test (si utilisation de `systemPath`) :
```
copy framework\target\framework-1.0-SNAPSHOT.jar test\src\main\webapp\WEB-INF\lib\framework-1.0-SNAPSHOT.jar
```
- Builder le WAR test :
```
mvn -DskipTests package -f test/pom.xml
```
- Déployer `test/target/test-project-1.0-SNAPSHOT.war` sur le conteneur, reproduire l'upload puis consulter les logs du serveur.

6) Recommandations
- Préférer `mvn install` pour installer `framework` dans le repo local et référencer normalement la dépendance dans `test/pom.xml` (éviter `systemPath`).
- Laisser en place les logs de debug jusqu'à résolution complète (`[DEBUG multipart]`, `[UPLOAD]`).
- Ajouter des tests automatisés d'intégration si possible (upload via HTTP client) pour valider le parsing multipart.

7) Résolution rapide si vous voyez `Aucun nom de fichier` malgré `submittedFileName` non nul
- Rebuild `framework` + update JAR dans `test` (ou `mvn install`), redeploy le WAR — le plus souvent le code déployé n'est pas la version modifiée.


---
Fichier créé automatiquement par l'assistant pour diagnostic et reproduction.
