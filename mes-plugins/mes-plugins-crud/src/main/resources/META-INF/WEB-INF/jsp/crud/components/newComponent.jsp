<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />

<div id="${component.pathName}" class="component" style="height: 100%">
	<div class="element_js_object" style="display: none">${component.javaScriptObjectName}</div>
	<div class="element_options" style="display: none">${component.staticJavaScriptOptions}</div>
	
	<tiles:insertTemplate template="${component.jspFilePath}">
		<tiles:putAttribute name="component" value="${component}" />
	</tiles:insertTemplate>
</div>


	