<%--

    ********************************************************************
    Code developed by amazing QCADOO developers team.
    Copyright © Qcadoo Limited sp. z o.o. (2010)
    ********************************************************************

--%>

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
	        	<div>
	            	<input type="file" name="file" size="50"/>
	            </div>
	            <div style="margin-top: 10px; margin-left: 130px;">
	            	<input type="submit" value="${buttonLabel}" style="width: 200px; cursor: pointer;"/>
	            </div>
	        </form>
        </div>
    </body>
</html>