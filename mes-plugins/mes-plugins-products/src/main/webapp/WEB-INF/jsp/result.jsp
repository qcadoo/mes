<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Wynik dodania / modyfikacji </title>
</head>
<body>
	Wynik dodania / modyfikacji: <c:out value="${message }"/> <br/>
	
	<c:forEach items="${fieldsDefinition}" var="entry" varStatus="rowCounter">
		${entry.name }: ${data[entry.name] } <br />
	</c:forEach>
	<br />
	<a href="list.html">Powrot do listy</a>
</body>
</html>