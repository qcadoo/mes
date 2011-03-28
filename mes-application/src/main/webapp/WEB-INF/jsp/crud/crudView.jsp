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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<html>
<head>

	<c:choose>
		<c:when test="${compressStaticResources}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/qcadoo.min.css" type="text/css" />
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/qcadoo.min.js"></script>
		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/_jquery-ui-1.8.5.custom.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/jquery.datepick.css" type="text/css" /> 
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/ui.jqgrid.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/jstree/style.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/qcd.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/notification.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/window.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/grid.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/form.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/layout.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/tree.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/elementHeader.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/awesomeDynamicList.css" type="text/css" />
			
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/json_sans_eval.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/json2.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.blockUI.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.jqGrid.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.jstree.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.cookie.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-1.8.5.custom.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-i18n.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.pnotify.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/encoder.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/logger.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/serializator.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/connector.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/options.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/pageConstructor.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/modal.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/core/messagesController.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/core/pageController.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/core/actionEvaluator.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/core/tabController.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/component.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/container.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/containers/layout/layout.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/containers/window.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/containers/windowTab.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/containers/form.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/utils/elementHeaderUtils.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/utils/loadingIndicator.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/formComponent.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/grid.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/grid/gridHeader.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/textInput.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/textArea.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/passwordInput.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/dynamicComboBox.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/entityComboBox.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/lookup.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/lookup/lookupDropdown.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/checkBox.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/linkButton.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/tree.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/calendar.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/staticComponent.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/awesomeDynamicList.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/ribbon.js"></script>
			
			<c:forEach items="${model['jsFilePaths']}" var="jsFilePath">
				<c:if test="${jsFilePath != null }">
					<script type="text/javascript" src="${jsFilePath}"></script>
				</c:if>
			</c:forEach>
		</c:otherwise>
	</c:choose>

	<script type="text/javascript">
	<!--//--><![CDATA[//><!--

		var viewName = "${viewName}";
		var pluginIdentifier = "${pluginIdentifier}";
		var context = '${context}';
		var locale = '${locale}';

		var hasDataDefinition = ${model['hasDataDefinition']};

		var popup = ${popup};

		var controller = null;

		window.init = function(serializationObject) {
			controller.init(serializationObject);
		}

		window.canClose = function() {
			return controller.canClose();
		}

		window.getComponent = function(componentPath) {
			return controller.getComponent(componentPath);
		}

		window.onPopupInit = function() {
			return controller.onPopupInit();
		}

		jQuery(document).ready(function(){
			
			controller = new QCD.PageController(viewName, pluginIdentifier, hasDataDefinition, popup);
			
			context = $.trim(context);
			if (context && context != "") {
				controller.setContext(context);
			}
			if (popup && window.opener) {
				window.opener.onPopupInit();
			}

			window.mainController = controller;
		});

	//--><!]]>
	</script>
</head>
<body>

	<div id="pageOptions" style="display: none;">${model['jsOptions']}</div>

	<c:forEach items="${model['components']}" var="component">
		<tiles:insertTemplate template="components/component.jsp">
			<tiles:putAttribute name="component" value="${component.value}" />
		</tiles:insertTemplate>
	</c:forEach>

</body>
</html>