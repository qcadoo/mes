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
<tiles:useAttribute name="componentFullName"/>
<tiles:useAttribute name="componentFullNameWithDots"/>
<tiles:useAttribute name="viewName" ignore="true"/>
<tiles:useAttribute name="pluginIdentifier" ignore="true"/>

<tiles:insertTemplate template="formComponent.jsp">
	<tiles:putAttribute name="component" value="${component}" />
	<tiles:putAttribute name="componentType" value="textInput" />
	<tiles:putAttribute name="componentFullName" value="${componentFullName}" />
	<tiles:putAttribute name="componentFullNameWithDots" value="${componentFullNameWithDots}" />
	<tiles:putAttribute name="viewName" value="${viewName}" />
	<tiles:putAttribute name="pluginIdentifier" value="${pluginIdentifier}" />
	<tiles:putAttribute name="componentBody">
		<c:if test="${component.options['textRepresentationOnDisabled']}">
			<c:set var="displayHiddenIfTextRepresentationOnDisabled" value="display: none" />
		</c:if>
		<input type="text" id="${componentFullName}_input" name="fields[${component.name}]" style="${displayHiddenIfTextRepresentationOnDisabled}" />
		<c:if test="${component.options['textRepresentationOnDisabled']}">
			<span id="${componentFullName}_text" class="component_container_form_textRepresentation">&nbsp;</span>
			<span id="${componentFullName}_textHeight" style="display: inline-block">&nbsp;</span>
		</c:if>
	</tiles:putAttribute>
</tiles:insertTemplate>