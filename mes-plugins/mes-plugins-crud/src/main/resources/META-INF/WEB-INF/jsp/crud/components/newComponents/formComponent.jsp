<%--

    ********************************************************************
    Code developed by amazing QCADOO developers team.
    Copyright (c) Qcadoo Limited sp. z o.o. (2010)
    ********************************************************************

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
<tiles:useAttribute name="componentBody"/>

<div class="component_element component_element_${componentType}">

	<c:set var="isInputBox" value="${'textarea' == componentType || 'input' == componentType || 'password' == componentType || 'calendar' == componentType || 'lookup' == componentType}"/>

	<div class="labelbox"><div class="label_h"></div><div class="label" ><span style="display: inline" id="${component['path']}_labelDiv">${component['jspOptions']['translations']['label']}</span><c:if test="${component['hasDescription']}"><div class="description_box">
				<div id="${component['path']}_description_icon" class="description_icon"></div>
				<div id="${component['path']}_description_message" class="description_message" style="display: none"><span>${component['jspOptions']['translations']['descriptionHeader']}</span><p>${component['jspOptions']['translations']['description']}</p></div></div></c:if><div class="error_box">
				<div id="${component['path']}_error_icon" class="error_icon"></div>
				<div id="${component['path']}_error_messages" class="error_messages" style="display: none"></div></div></div></div>
	
	<c:choose>
		<c:when test="${isInputBox && !component['jspOptions']['textRepresentationOnDisabled']}">
			<div class="component_container_form_w">
				<div class="component_container_form_inner">
					<div class="component_container_form_x"></div>
					<div class="component_container_form_y"></div>
					${componentBody}
				</div>
			</div>
		</c:when>
		<c:otherwise>
			<div class="component_container_form_w">
				${componentBody}
			</div>			
		</c:otherwise>
	</c:choose>
	
</div>