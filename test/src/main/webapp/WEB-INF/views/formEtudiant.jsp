<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>etudiant</title>
</head>
<body>
    <div class="container">
        <form action="${pageContext.request.contextPath}/etu/saves" method="post">
            <div>
                <label for="nom">Nom</label>
                <input type="text" name="etudiant.nom" id="nom" value="Hasina" required>
            </div>
            <div>
                <label for="prenom">Prenom</label>
                <input type="text" name="etudiant.prenom" id="prenom" value="Princy" required>
            </div>
            <button type="submit">Valider</button>
        </form>
    </div>
</body>
</html>