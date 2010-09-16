<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="componentsMap" />
<tiles:useAttribute name="componentPath" ignore="true"/>

<c:forEach items="${componentsMap}" var="componentEntry">
	<c:set var="component" value="${componentEntry.value}"/>
	
	<c:choose>
		<c:when test='${componentPath == null}'>
			<c:set var="entryPath" value="${component.name}"/>
		</c:when>
		<c:otherwise>
			<c:set var="entryPath" value="${componentPath}-${component.name}"/>
		</c:otherwise>
	</c:choose>
	
	<c:choose>
		<c:when test="${component.type == 2}">
			<c:set var="componentJsp" value="containers/form.jsp"/>
		</c:when>
		<c:when test="${component.type == 3}">
			<c:set var="componentJsp" value="elements/grid.jsp"/>
		</c:when>
		<c:when test="${component.type == 4}">
			<c:set var="componentJsp" value="elements/textInput.jsp"/>
		</c:when>
	</c:choose>
	<tiles:insertTemplate template="${componentJsp}">
		<tiles:putAttribute name="component" value="${component}" />
		<tiles:putAttribute name="componentPath" value="${entryPath}" />
	</tiles:insertTemplate>
	
	
</c:forEach>