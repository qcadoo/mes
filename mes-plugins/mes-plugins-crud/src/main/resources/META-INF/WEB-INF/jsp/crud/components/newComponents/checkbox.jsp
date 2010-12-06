<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />

<tiles:insertTemplate template="formComponent.jsp">
	<tiles:putAttribute name="component" value="${component}" />
	<tiles:putAttribute name="componentType" value="checkbox" />
	<tiles:putAttribute name="componentBody">
		<c:if test="${component['jspOptions']['textRepresentationOnDisabled']}">
			<c:set var="displayHiddenIfTextRepresentationOnDisabled" value="display: none" />
		</c:if>
		<input type="checkbox" id="${component['path']}_input" style="${displayHiddenIfTextRepresentationOnDisabled}" />
		<c:if test="${component['jspOptions']['textRepresentationOnDisabled']}">
			<span id="${component['path']}_text" class="component_container_form_textRepresentation">&nbsp;</span>
		</c:if>
		<span id="${component['path']}_textHeight">&nbsp;</span>
	</tiles:putAttribute>
</tiles:insertTemplate>