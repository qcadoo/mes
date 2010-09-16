<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />
<tiles:useAttribute name="componentPath"/>

<div class="component component_container component_container_form" id="${componentPath}">
	<div class="elementHeader">${translationsMap[viewElement.header]} ${component.header }</div>
	<div class=element_options style="display: none">
		${viewElementsOptions[componentPath]}
	</div>
	
	<div class="containerComponents">
		<tiles:insertTemplate template="../components.jsp">
			<tiles:putAttribute name="componentsMap" value="${component.components}" />
			<tiles:putAttribute name="componentPath" value="${componentPath}" />
		</tiles:insertTemplate>
	</div>
	
	<button id="${componentPath}_saveButton">${translationsMap["commons.form.button.accept"] }</button>
	<button id="${componentPath}_saveCloseButton">${translationsMap["commons.form.button.acceptAndClose"] }</button>
	<button id="${componentPath}_cancelButton">${translationsMap["commons.form.button.cancel"] }</button>
	
</div>

