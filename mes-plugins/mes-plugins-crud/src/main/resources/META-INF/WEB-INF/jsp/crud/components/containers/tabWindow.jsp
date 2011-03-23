<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />

<div class="windowContainer">
	<c:if test="${component.jspOptions['hasRibbon']}">
		<div class="windowContainerRibbon tabWindowRibbon">
			<div id="${component['path']}_windowContainerRibbon">
			</div>
		</div>
	</c:if>
	<div class="windowContainerContentBody" id="${component['path']}_windowContainerContentBody">
		<div id="${component['path']}_windowContainerContentBodyWidthMarker" style=" z-index: 5000;"></div>
		<div class="windowContent" id="${component['path']}_windowContent">
			<c:if test="${component.jspOptions['header']}">
				<div class="tabWindowHeader <c:if test="${component.jspOptions['oneTab']}">noTabs</c:if>" id="${component['path']}_windowHeader"></div>
			</c:if>
			<c:if test="${! component.jspOptions['oneTab']}">
				<div id="${component['path']}_windowTabs" class="windowTabs">
					<div></div>
				</div>
			</c:if>
			<div class="tabWindowComponents" id="${component['path']}_windowComponents">
				<c:forEach items="${component['children']}" var="component">
					<tiles:insertTemplate template="../component.jsp">
						<tiles:putAttribute name="component" value="${component.value}" />
					</tiles:insertTemplate>
				</c:forEach>
			</div></div>
	</div>
</div>