<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>login</title>
</head>
<body>
    <div class="container">
        <form action="${pageContext.request.contextPath}/login" method="post">
            <div>
                <label for="email">Email</label>
                <input type="email" name="email" id="email" value="hasina+1admin@gmail.com" required>
            </div>
            <div>
                <label for="password">Password</label>
                <input type="password" name="password" id="password" value="hasina1" required>
            </div>
            <button type="submit">Se connecter</button>
        </form>
    </div>
</body>
</html>