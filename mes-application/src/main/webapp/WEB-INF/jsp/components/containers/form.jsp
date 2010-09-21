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

<div class="component component_container component_container_form" id="${componentFullName}" style="border: solid black 1px; margin: 10px; padding: 10px; width: 500px;">
	<div class=element_options style="display: none">
		${component.optionsAsJson}
	</div>
	
	<div style="font-weight: bold; margin-bottom: 10px;">
		<c:if test="${component.header}">
			<c:set var="headerLabel" value="${viewName}.${componentFullNameWithDots}.header"/>
			${translationsMap[headerLabel]}
		</c:if>
	</div>
	
	<div id="${componentFullName}_formComponents">
		<c:forEach items="${component.components}" var="componentEntry">
			<tiles:insertTemplate template="../component.jsp">
				<tiles:putAttribute name="component" value="${componentEntry.value}" />
				<tiles:putAttribute name="parentComponentFullName" value="${componentFullName}" />
				<tiles:putAttribute name="parentComponentFullNameWithDots" value="${componentFullNameWithDots}" />
				<tiles:putAttribute name="viewName" value="${viewName}" />
			</tiles:insertTemplate>
		</c:forEach>
	</div>
	
	<button id="${componentFullName}_saveButton" style="margin-top: 5px;">${translationsMap["commons.form.button.accept"] }</button>
	
</div>

