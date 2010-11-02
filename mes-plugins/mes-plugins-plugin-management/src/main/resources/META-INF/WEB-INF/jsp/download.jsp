<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String ctx = request.getContextPath();
%>
<html>
    <head>
        <title>${headerLabel}</title>
    </head>
    <body>
    	<div style="margin: 20px;">
	        <h1>${headerLabel}</h1>
	        <form method="post" action="<%=ctx%>/${downloadAction}" enctype="multipart/form-data">
	            <input type="file" name="file"/>
	            <input type="submit" value="${buttonLabel}"/>
	        </form>
        </div>
    </body>
</html>