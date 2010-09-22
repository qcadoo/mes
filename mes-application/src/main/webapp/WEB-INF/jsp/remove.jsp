<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String ctx = request.getContextPath();
%>
<html>
    <head>
        <title>Usuwanie modułów</title>
    </head>
    <body>
        <h1>Usunięto moduł</h1>
		<input type="button" value="Wróć" name="return" onclick="location.href='install.html'"/>
    </body>
</html>