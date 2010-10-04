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

<div class="component component_element component_element_grid" id="${componentFullName}">
	<div class=element_options style="display: none">
		${component.optionsAsJson}
	</div>
	
	<div style="font-weight: bold; margin-bottom: 10px;">
		<c:if test="${component.options['header']}">
			<c:set var="headerLabel" value="${pluginIdentifier}.${viewName}.${componentFullNameWithDots}.header"/>
			${translationsMap[headerLabel]}
		</c:if>
	</div>
	<div>
		<table class="element_table" id="${componentFullName}_grid">
		</table>
	</div>
</div>