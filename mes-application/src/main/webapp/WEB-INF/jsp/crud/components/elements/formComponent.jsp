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
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />
<tiles:useAttribute name="componentType" />
<tiles:useAttribute name="componentBody" />
<tiles:useAttribute name="componentAlign" ignore="true" />
<tiles:useAttribute name="componentIgnoreBorder" ignore="true" />
<tiles:useAttribute name="componentCssClass" ignore="true" />

<div class="component_element component_form_element component_element_${componentType} ${componentCssClass}">

	<c:set var="hasLabel" value="${component['hasLabel'] && component['jspOptions']['translations']['label'] != ''}" />
	<c:set var="labelWidth" value="${hasLabel ? component['jspOptions']['labelWidth'] : 0}" />
	
	<c:set var="isInputBox" value="${'textarea' == componentType || 'input' == componentType || 'password' == componentType || 'calendar' == componentType || 'lookup' == componentType || 'file' == componentType || 'time' == componentType}"/>

	<c:if test="${! component['hasLabel']}">
		<c:set var="labelboxClass" value="noLabel"/>
	</c:if>
	
	<c:if test="${componentAlign != 'right'}">
		<c:set var="componentAlign" value="left"/>
	</c:if>
	
	<div class="labelbox ${labelboxClass}" style="width: ${labelWidth}%"><div class="label_h"></div><div class="label" ><c:if test="${hasLabel}"><span style="display: inline" id="${component['path']}_labelDiv">${component['jspOptions']['translations']['label']}</span></c:if><c:if test="${component['hasDescription']}"><div class="description_box">
				<div id="${component['path']}_description_icon" class="description_icon"></div>
				<div id="${component['path']}_description_message" class="description_message" style="display: none"><span>${component['jspOptions']['translations']['descriptionHeader']}</span><p>${component['jspOptions']['translations']['description']}</p></div></div></c:if><div class="error_box">
				<div id="${component['path']}_error_icon" class="error_icon"></div>
				<div id="${component['path']}_error_messages" class="error_messages" style="display: none"></div></div></div></div>
	
	<c:choose>
		<c:when test="${componentIgnoreBorder == 'true'}">
			<div class="component_container_form_w ${labelboxClass}" style="left: ${labelWidth}%; text-align: ${componentAlign}">
				${componentBody}
			</div>
		</c:when>
		<c:when test="${isInputBox && !component['jspOptions']['textRepresentationOnDisabled']}">
			<div class="component_container_form_w ${labelboxClass}" style="left: ${labelWidth}%; text-align: ${componentAlign}"><div class="component_container_form_inner_h"></div><div class="component_container_form_inner">
					${componentBody}
					<div class="component_container_form_x"></div>
					<div class="component_container_form_y"></div>
				</div>
			</div>
		</c:when>
		<c:otherwise>
			<div class="component_container_form_w ${labelboxClass}" style="left: ${labelWidth}%; text-align: ${componentAlign}">
				${componentBody}
			</div>			
		</c:otherwise>
	</c:choose>
	
</div>