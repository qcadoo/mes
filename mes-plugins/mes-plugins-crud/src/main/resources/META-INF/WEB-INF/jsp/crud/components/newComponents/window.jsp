<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />

	<div class="windowContainer">
		<div class="windowContainerRibbon">
			<div id="${component.pathName}_windowContainerRibbon">
			</div>
		</div>
		
		<div class="windowContainerContentBody" id="${component.pathName}_windowContainerContentBody">
			<div id="${component.pathName}_windowContainerContentBodyWidthMarker" style=" z-index: 5000;"></div>
			<div class="windowContent" id="${component.pathName}_windowContent">
				<div class="windowComponents" id="${component.pathName}_windowComponents">
					<c:forEach items="${component.children}" var="componentEntry">
						<tiles:insertTemplate template="../newComponent.jsp">
							<tiles:putAttribute name="component" value="${componentEntry.value}" />
						</tiles:insertTemplate>
					</c:forEach>
				</div>
			</div>
			
		</div>
	</div>




	