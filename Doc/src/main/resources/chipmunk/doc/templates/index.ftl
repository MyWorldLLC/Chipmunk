<!DOCTYPE html>
<html lang="en">
    <head>
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