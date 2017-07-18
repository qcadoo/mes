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

<div style="display: none;">
	<div class="calendar" id="calendar"></div>
</div>

<tiles:insertTemplate template="formComponent.jsp">
	<tiles:putAttribute name="component" value="${component}" />
	<tiles:putAttribute name="componentType" value="calendar" />
	<tiles:putAttribute name="componentIgnoreBorder" value="true" />
	<tiles:putAttribute name="componentBody">
		<c:if test="${component['jspOptions']['withTimePicker']}">
			<div style="width:58%;float:left;margin-top:9px;">
		</c:if>
		<c:if test="${!component['jspOptions']['withTimePicker']}">
			<div style="margin-top:9px;">
		</c:if>
			<div class="component_container_form_inner_h"></div><div class="component_container_form_inner">
				<div class="lookupValueWrapper">
					<div class="lookupInputWrapper">
						<input type="text" id="${component['path']}_input" tabindex="${component['indexOrder']}" />
					</div>
					<div class="lookupButton calendarButton" id="${component['path']}_calendar"></div>
					<br/>
				</div>
				<div class="component_container_form_x"></div>
				<div class="component_container_form_y"></div>
			</div>
		</div>
		<c:if test="${component['jspOptions']['withTimePicker']}">
			<div style="width:40%;float:right;margin-top:9px;">
				<div class="component_container_form_inner_h"></div><div class="component_container_form_inner">
					<div class="lookupValueWrapper">
						<div class="lookupInputWrapper" style="width:100%">
							<input type="text" id="${component['path']}_timeInput" tabindex="${component['indexOrder']}"/>
						</div>
					</div>
					<div class="component_container_form_x"></div>
					<div class="component_container_form_y"></div>
				</div>
			</div>
		</c:if>
	</tiles:putAttribute>
</tiles:insertTemplate>
