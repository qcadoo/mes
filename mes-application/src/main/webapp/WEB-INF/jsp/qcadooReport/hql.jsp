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
	<title>QCADOO :: Report Development :: HQL</title>
</head>
<body>

	<h1>Type the HQL query</h1>
	
	<p>Please see the <a href="http://javadoc.qcadoo.org/com/qcadoo/model/api/search/SearchQueryBuilder.html">reference</a>.</p>  

	<form method="post">
		<textarea name="hql" rows="30" cols="150">${hql}</textarea>
		<br>
		<input type="submit" name="Query" value="Query" />
	</form>
	
	<c:if test="${isEmpty || isError || isOk}">
		<h1>Query results</h1>
	</c:if>
	
	<c:if test="${isEmpty}">
		<p>There are no results for that query.</p>  
	</c:if>
	
	<c:if test="${isError}">
		<p>${exceptionMessage}</p>
		<p>${exception}</p>
	</c:if>
	
	<c:if test="${isOk}">
		<table border="1">
			<thead>
				<tr>
					<c:forEach items="${headers}" var="hCell">
						<th>${hCell}</th>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${rows}" var="row">
				 	<tr>
				 		<c:forEach items="${row}" var="cell">
							<td>${cell}</td>
						</c:forEach>
			 		</tr>
				</c:forEach>
			</tbody>
		</table>
	</c:if>
	
</body>
</html>