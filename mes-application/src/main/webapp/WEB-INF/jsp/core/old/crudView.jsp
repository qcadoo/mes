<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.1

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
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

		
	<link rel="stylesheet" href="../../css/jquery-ui-1.8.4.custom.css" type="text/css" />
	<link rel="stylesheet" href="../../css/ui.jqgrid.css" type="text/css" />
	<link rel="stylesheet" href="../../css/qcd.css" type="text/css" />

	<script type="text/javascript" src="../../js/lib/json_sans_eval.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="../../js/lib/encoder.js"></script>
	<script type="text/javascript" src="../../js/qcd/elements/qcdGrid.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery.ba-serializeobject.min.js"></script>
	
	<script type="text/javascript" src="../../js/qcd/utils/logger.js"></script>
	<script type="text/javascript" src="../../js/qcd/utils/serializator.js"></script>
	<script type="text/javascript" src="../../js/qcd/core/pageController.js"></script>
	<script type="text/javascript" src="../../js/qcd/core/pageConstructor.js"></script>
	<script type="text/javascript" src="../../js/qcd/elements/qcdForm.js"></script>
	
	<script type="text/javascript">

		var viewName = "${viewDefinition.pluginIdentifier}/${viewDefinition.name}";
		var entityId = "${entityId}";
		var contextEntityId = "${contextEntityId}";

		var controller = null

		window.init = function(serializationObject) {
			controller = new QCD.PageController(viewName);
			controller.init(entityId, contextEntityId, serializationObject);
		}

		window.translationsMap = new Object();
		<c:forEach items="${translationsMap}" var="translation">
			window.translationsMap["${translation.key}"] = "${translation.value}";
		</c:forEach>

	</script>
</head>
<body>

<c:if test='${viewDefinition.header != null}'><div class="pageHeader">${translationsMap[viewDefinition.header]}</div></c:if>

<c:forEach items="${viewDefinition.elements}" var="viewElement">

	<div>
		<c:choose>
			<c:when test="${viewElement.type == 1}">
				<div class="elementHeader">${translationsMap[viewElement.header]}</div>
				<table class="element_table" id="${viewElement.name}">
					<td class=element_options style="display: none">
						${viewElementsOptions[viewElement.name]}
					</td>
				</table>
				
			</c:when>
			<c:when test="${viewElement.type == 2}">
				<div class="element_form" id="${viewElement.name}">
					<div class="elementHeader">${translationsMap[viewElement.header]}</div>
					<div class=element_options style="display: none">
						${viewElementsOptions[viewElement.name]}
					</div>
					<tiles:insertTemplate template="formTemplate.jsp">
						<tiles:putAttribute name="formElement" value="${viewElement}" />
					</tiles:insertTemplate>
				</div>

			</c:when>
		</c:choose>
	</div>
	<br/>

</c:forEach>

</body>
</html>