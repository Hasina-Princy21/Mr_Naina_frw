set ABSOLUE=.
set RETOUR=..\
set PATH_WAR=%ABSOLUE%\target
set PATH_TOMCAT=C:\apache-tomcat-10.1.49
set PATH_JAR=%RETOUR%\framework\target
set PATH_TEST=%ABSOLUE%\src\main\webapp\WEB-INF\lib

@echo off
echo ======================
echo COPIE DU FRAMEWORK JAR
echo ======================
REM
if exist "%PATH_JAR%\framework-1.0-SNAPSHOT.jar" (
    echo Le JAR du framework existe, remplacement...
    cmd /c copy /Y "%PATH_JAR%\framework-1.0-SNAPSHOT.jar" "%PATH_TEST%"
) else (
    echo ERREUR : Le fichier JAR du framework est introuvable !
    pause
    exit /b 1
)

echo =====================
echo COMPILATION DU PROJET
echo =====================
cmd /c mvn clean package

if NOT "%ERRORLEVEL%"=="0" (
    echo Erreur de compilation !
    pause
    exit /b 1
)

echo Build termine avec succes !

echo ====================
echo COPIE DE FICHIER WAR
echo ====================

REM --- Vérifie si le WAR existe avant de copier ---
if exist "%PATH_WAR%\test-project-1.0-SNAPSHOT.war" (
    echo Le WAR existe, remplacement...
    cmd /c copy /Y "%PATH_WAR%\test-project-1.0-SNAPSHOT.war" "%PATH_TOMCAT%\webapps"
) else (
    echo ERREUR : Le fichier WAR est introuvable !
    pause
    exit /b 1
)

echo Deploiement termine avec succes !
pause
