<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <title>IMDBQ</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="splitScreen">
        <form action="/" method="GET">
            <label for="query"></label><input type="text" name="query" id="query" placeholder="enter search text"/>
            <input type="submit" value="find">
        </form>
    </div>
    <div class="splitScreen">
        <c:if test="${results != null}">
            <c:choose>
                <c:when test="${empty results}">
                    <h1>results for ${query}</h1>
                    <div>
                        no matches found
                    </div>
                </c:when>
                <c:otherwise>
                    <h1>results for ${query}</h1>
                    <ul>
                        <c:forEach items="${results}" var="result">
                            <li>${result}</li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </c:if>
    </div>
</body>
</html>
