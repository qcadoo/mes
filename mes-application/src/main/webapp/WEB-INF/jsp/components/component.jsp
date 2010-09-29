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
		<c:when test='${component.type == "dynamicComboBox"}'>
			<c:set var="componentJsp" value="elements/dynamicComboBox.jsp"/>
		</c:when>
		<c:when test='${component.type == "entityComboBox"}'>
			<c:set var="componentJsp" value="elements/entityComboBox.jsp"/>
		</c:when>
		<c:when test='${component.type == "checkBox"}'>
			<c:set var="componentJsp" value="elements/checkBox.jsp"/>
		</c:when>
		<c:when test='${component.type == "linkButton"}'>
			<c:set var="componentJsp" value="elements/linkButton.jsp"/>
		</c:when>
		
	</c:choose>
	
	<tiles:insertTemplate template="${componentJsp}">
		<tiles:putAttribute name="component" value="${component}" />
		<tiles:putAttribute name="componentFullName" value="${componentFullName}" />
		<tiles:putAttribute name="componentFullNameWithDots" value="${componentFullNameWithDots}" />
		<tiles:putAttribute name="viewName" value="${viewName}" />
		<tiles:putAttribute name="pluginIdentifier" value="${pluginIdentifier}" />
	</tiles:insertTemplate>
	
	
