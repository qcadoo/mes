<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String ctx = request.getContextPath();
%>
<html>
    <head>
        <title>Pobieranie modułów</title>
    </head>
    <body>
        <h1>Pobierz moduł</h1>
        <form method="post" action="<%=ctx%>/download.html" enctype="multipart/form-data">
            <input type="file" name="file"/>
            <input type="submit" value="Pobierz"/>
        </form>
    </body>
</html>