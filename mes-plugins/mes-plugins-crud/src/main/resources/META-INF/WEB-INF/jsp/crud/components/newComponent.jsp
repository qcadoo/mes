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

	-- ${component.name }<br/>
	
	
<div id="${component.pathName}" style="margin-left: 20px; padding-left: 2px; border-left: solid black 1px;">
	<div class="JSObject">
		${component.javaScriptFilePath}
	</div>
	<div class="element_options" style="display: none">
	</div>
	<tiles:insertTemplate template="${component.jspPath}">
		<tiles:putAttribute name="component" value="${component}" />
	</tiles:insertTemplate>
</div>


	