<!DOCTYPE html>
<html lang="en">
    <head>
    </head>
    <body id="body">
        <h1>Module ${name}</h1>
        <hr />
        <p>
            <#list children as child>
            <h3>
                ${child.getName()}
            </h3>
            <p>
                ${child.getComment()}
            </p>
            </#list>
        </p>
    </body>
</html>