<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />
<tiles:useAttribute name="componentType" />
<tiles:useAttribute name="componentFullName"/>
<tiles:useAttribute name="componentFullNameWithDots"/>
<tiles:useAttribute name="viewName" ignore="true"/>
<tiles:useAttribute name="pluginIdentifier" ignore="true"/>
<tiles:useAttribute name="componentBody"/>

<div class="component component_element component_element_${componentType}" id="${componentFullName}">
	<div class="element_options" style="display: none">
		${component.optionsAsJson}
	</div>
	
	<c:set var="labelKey" value="${pluginIdentifier}.${viewName}.${componentFullNameWithDots}.label"/>
	<c:set var="descriptionKey" value="${pluginIdentifier}.${viewName}.${componentFullNameWithDots}.description"/>
	<c:set var="descriptionHeaderKey" value="${pluginIdentifier}.${viewName}.${componentFullNameWithDots}.descriptionHeader"/>

	<c:set var="isInputBox" value="${'textArea' == componentType || 'textInput' == componentType || 'passwordInput' == componentType || 'calendar' == componentType || 'lookup' == componentType}"/>
	
	<div class="labelbox"><div class="label_h"></div><div class="label" ><span style="display: inline" id="${componentFullName}_labelDiv">${translationsMap[labelKey]}</span><c:if test="${component.hasDescription}"><div class="description_box">
				<div id="${componentFullName}_description_icon" class="description_icon"></div>
				<div id="${componentFullName}_description_message" class="description_message" style="display: none"><span>${translationsMap[descriptionHeaderKey]}</span><p>${translationsMap[descriptionKey]}</p></div></div></c:if><div class="error_box">
				<div id="${componentFullName}_error_icon" class="error_icon"></div>
				<div id="${componentFullName}_error_messages" class="error_messages" style="display: none"></div></div></div></div>
	
	<c:choose>
		<c:when test="${isInputBox}">
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