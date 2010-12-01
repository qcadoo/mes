<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.1

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

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />
<tiles:useAttribute name="parentComponentFullName" ignore="true"/>
<tiles:useAttribute name="parentComponentFullNameWithDots" ignore="true"/>
<tiles:useAttribute name="viewName" ignore="true"/>
<tiles:useAttribute name="pluginIdentifier" ignore="true"/>

	<c:choose>
		<c:when test='${parentComponentFullName == null}'>
			<c:set var="componentFullName" value="${component.name}"/>
			<c:set var="componentFullNameWithDots" value="${component.name}"/>
		</c:when>
		<c:otherwise>
			<c:set var="componentFullName" value="${parentComponentFullName}-${component.name}"/>
			<c:set var="componentFullNameWithDots" value="${parentComponentFullNameWithDots}.${component.name}"/>
		</c:otherwise>
	</c:choose>
	
	<c:choose>
		<c:when test='${component.type == "window"}'>
			<c:set var="componentJsp" value="containers/window.jsp"/>
		</c:when>
		<c:when test='${component.type == "form"}'>
			<c:set var="componentJsp" value="containers/form.jsp"/>
		</c:when>
		<c:when test='${component.type == "grid"}'>
			<c:set var="componentJsp" value="elements/grid.jsp"/>
		</c:when>
		<c:when test='${component.type == "textInput"}'>
			<c:set var="componentJsp" value="elements/textInput.jsp"/>
		</c:when>
		<c:when test='${component.type == "textArea"}'>
			<c:set var="componentJsp" value="elements/textArea.jsp"/>
		</c:when>		
		<c:when test='${component.type == "passwordInput"}'>
			<c:set var="componentJsp" value="elements/passwordInput.jsp"/>
		</c:when>
		<c:when test='${component.type == "dynamicComboBox"}'>
			<c:set var="componentJsp" value="elements/dynamicComboBox.jsp"/>
		</c:when>
		<c:when test='${component.type == "entityComboBox"}'>
			<c:set var="componentJsp" value="elements/entityComboBox.jsp"/>
		</c:when>
		<c:when test='${component.type == "lookupComponent"}'>
			<c:set var="componentJsp" value="elements/lookup.jsp"/>
		</c:when>
		<c:when test='${component.type == "checkBox"}'>
			<c:set var="componentJsp" value="elements/checkBox.jsp"/>
		</c:when>
		<c:when test='${component.type == "linkButton"}'>
			<c:set var="componentJsp" value="elements/linkButton.jsp"/>
		</c:when>
		<c:when test='${component.type == "tree"}'>
			<c:set var="componentJsp" value="elements/tree.jsp"/>
		</c:when>
		<c:when test='${component.type == "calendar"}'>
			<c:set var="componentJsp" value="elements/calendar.jsp"/>
		</c:when>
		<c:when test='${component.type == "staticPage"}'>
			<c:set var="componentJsp" value="elements/staticPage.jsp"/>
		</c:when>
		
	</c:choose>
	
	<tiles:insertTemplate template="${componentJsp}">
		<tiles:putAttribute name="component" value="${component}" />
		<tiles:putAttribute name="componentFullName" value="${componentFullName}" />
		<tiles:putAttribute name="componentFullNameWithDots" value="${componentFullNameWithDots}" />
		<tiles:putAttribute name="viewName" value="${viewName}" />
		<tiles:putAttribute name="pluginIdentifier" value="${pluginIdentifier}" />
	</tiles:insertTemplate>
	
	
