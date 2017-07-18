<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo Framework
    Version: 1.4

    This file is part of Qcadoo.

    Qcadoo is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation; either version 3 of the License,
    or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    ***************************************************************************

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<title>QCADOO :: Report Development :: Report</title>
</head>
<body>

	<c:if test="${!isParameter}">
		<h1>Select the report file</h1>
	
		<form method="post" enctype="multipart/form-data">
			<input type="file" name="file" />
			<input type="submit" name="Next" value="Next" />
		</form>
	</c:if>
	
	<c:if test="${isParameter}">
		
		<form action="/developReport/generate.html" method="post">
			<input type="hidden" name="file" value="/Users/kasi/qcadoo/qcadoo/qcadoo-report/pom.xml" />
			<h1>Modify report template</h1>
			<textarea name="template" rows="30" cols="150">${template}</textarea></br>
			<h1>Fill the report parameters</h1>
			<label for="type">Type</label> <select name="type"><option value="pdf">pdf</option><option value="xls">xls</option><option value="csv">csv</option></select></br>
			<label for="locale">Locale</label> <input type="text" name="locale" value="${locale}" /></br>
			<c:forEach items="${params}" var="p">
				<label for="params[${p.name}]">${p.name} (${p.clazz})</label> <input type="text" name="params[${p.name}]" value="${p.value}" /></br>
			</c:forEach>
			<input type="submit" name="Generate" value="Generate" />
		</form>
	</c:if>
	
	<c:if test="${isFileInvalid}">
		<p>Report file must be a valid JRXML file.</p>
	</c:if>
	
	<c:if test="${isError}">
		<p>${exceptionMessage}</p>
		<p>${exception}</p>
	</c:if>
	
</body>
</html>