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
<tiles:useAttribute name="componentFullName"/>
<tiles:useAttribute name="componentFullNameWithDots"/>
<tiles:useAttribute name="viewName" ignore="true"/>
<tiles:useAttribute name="pluginIdentifier" ignore="true"/>

<div style="display: none;">
	<div class="calendar" id="calendar"></div>
</div>


<tiles:insertTemplate template="formComponent.jsp">
	<tiles:putAttribute name="component" value="${component}" />
	<tiles:putAttribute name="componentType" value="calendar" />
	<tiles:putAttribute name="componentFullName" value="${componentFullName}" />
	<tiles:putAttribute name="componentFullNameWithDots" value="${componentFullNameWithDots}" />
	<tiles:putAttribute name="viewName" value="${viewName}" />
	<tiles:putAttribute name="pluginIdentifier" value="${pluginIdentifier}" />
	<tiles:putAttribute name="componentBody">
		<input type="text" id="${componentFullName}_input" name="fields[${component.name}]" />
		<div id="${componentFullName}_calendar" class="calendar"></div>
	</tiles:putAttribute>
</tiles:insertTemplate>