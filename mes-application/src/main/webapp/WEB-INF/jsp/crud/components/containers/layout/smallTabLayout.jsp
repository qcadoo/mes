<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo Framework
    Version: 1.4

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

<div class="smallLayoutContainer">
	<div class="smallLayoutHeader">
		<div>
			<c:forEach items="${component.tabs}" var="tab">
				<a href="#" id="${component['path']}_headerItem_${tab.name}" class="smallLayoutHeaderItem">${component.tabTranslations[tab.name]}</a>
			</c:forEach>
		</div>
	</div>
	<div id="${component['path']}_layoutComponents" class="smallLayoutContent">
		<c:forEach items="${component.tabs}" var="tab">
			<div class="smallLayoutContentItem" id="${component['path']}_layoutComponent_${tab.name}">
				<c:forEach items="${tab.components}" var="childrenComponent">
					<tiles:insertTemplate template="../../component.jsp">
						<tiles:putAttribute name="component" value="${component.children[childrenComponent.name]}" />
					</tiles:insertTemplate>
				</c:forEach>
			</div>
		
		</c:forEach>
	</div>
	
</div>