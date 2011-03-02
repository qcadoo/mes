<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.3.0

    This file is part of Qcadoo.

    Qcadoo is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation; either version 3 of the License,
    or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    ***************************************************************************

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<tiles:useAttribute name="component" />
<tiles:useAttribute name="componentFullName" ignore="true"/>
<tiles:useAttribute name="componentFullNameWithDots" ignore="true"/>
<tiles:useAttribute name="viewName" ignore="true"/>
<tiles:useAttribute name="pluginIdentifier" ignore="true"/>

<div class="component component_container component_container_window" id="${componentFullName}">
	<div class=element_options style="display: none">
		${component.optionsAsJson}
	</div>
	
	<div class="windowContainer">
		<div class="windowContainerRibbon">
			<div id="${componentFullName}_windowContainerRibbon">
			</div>
		</div>
		<div class="windowContainerContentBody" id="${componentFullName}_windowContainerContentBody">
			<div id="${componentFullName}_windowContainerContentBodyWidthMarker" style=" z-index: 5000;"></div>
			<div class="windowContent" id="${componentFullName}_windowContent">
				<c:if test="${component.jspOptions['header']}">
					<div class="windowHeader" id="${componentFullName}_windowHeader"></div>
				</c:if>
				<div class="windowComponents" id="${componentFullName}_windowComponents">
					<c:forEach items="${component.components}" var="componentEntry">
						<tiles:insertTemplate template="../component.jsp">
							<tiles:putAttribute name="component" value="${componentEntry.value}" />
							<tiles:putAttribute name="parentComponentFullName" value="${componentFullName}" />
							<tiles:putAttribute name="parentComponentFullNameWithDots" value="${componentFullNameWithDots}" />
							<tiles:putAttribute name="viewName" value="${viewName}" />
							<tiles:putAttribute name="pluginIdentifier" value="${pluginIdentifier}" />
						</tiles:insertTemplate>
					</c:forEach>
				</div>
			</div>
			
		</div>
	</div>
	
</div>


