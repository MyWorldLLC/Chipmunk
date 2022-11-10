<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Chipmunk Docs</title>
    </head>
    <body id="body">
    <h1>Modules</h1>
    <ul>
        <#list moduleRoots as module>
        <li>
            <a href="${module.getName()}.html">${module.getName()}</a>
        </li>
        </#list>
    </ul>
    </body>
</html>