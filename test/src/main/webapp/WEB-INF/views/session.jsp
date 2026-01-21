<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <div class="container">
        <form action="${pageContext.request.contextPath}/session/get" method="post">
            <label for="value">Clé :</label>
            <input type="text" id="value" name="value" value="Koto" required>
            <br>
            <button type="submit">Enregistrer en session</button>
        </form>
    </div>
</body>
</html>