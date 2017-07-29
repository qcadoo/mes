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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<html>
<head>
	<c:choose>
		<c:when test="${useCompressedStaticResources}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/custom.css?ver=2016_03_19_15_07" type="text/css" />
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-1.8.3.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-1.11.4.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.jqGrid.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.searchFilter.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.shorten.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.maskedinput-1.3.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.js?ver=2016_03_19_15_07"></script>
		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/_jquery-ui-1.8.5.custom.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/jquery.datepick.css?ver=2016_03_19_15_07" type="text/css" /> 
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/ui.jqgrid.css?ver=2016_03_19_15_07" type="text/css" /> 
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/jstree/style.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/qcd.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/notification.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/jquery.bubblepopup.v2.3.1.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/window.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/grid.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/form.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/layout.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/tree.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/elementHeader.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/awesomeDynamicList.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/gantt.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/contextualHelpButton.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/custom.css?ver=2016_03_19_15_07" type="text/css" />
			
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-1.8.3.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/json_sans_eval.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/json2.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.blockUI.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.jqGrid.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.searchFilter.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.jstree.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.cookie.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-1.11.4.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-i18n.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.pnotify.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.form.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.maskedinput-1.3.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/encoder.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.shorten.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/logger.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/serializator.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/connector.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/options.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/pageConstructor.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/modal.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/core/messagesController.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/core/pageController.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/core/actionEvaluator.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/core/tabController.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/component.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/container.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/containers/layout/layout.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/containers/window.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/containers/windowTab.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/containers/form.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/contextualHelpButton.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/utils/elementHeaderUtils.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/utils/loadingIndicator.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/formComponent.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/grid.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/grid/gridHeader.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/textInput.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/timeInput.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/hiddenInput.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/file.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/textArea.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/passwordInput.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/dynamicComboBox.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/entityComboBox.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/lookup.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/lookup/lookupDropdown.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/checkBox.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/linkButton.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/tree.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/calendar.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/staticComponent.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/awesomeDynamicList.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/ribbon.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/gantt/ganttChart.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/gantt/ganttChartHeader.js?ver=2016_03_19_15_07"></script>

            <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/track.js?ver=2016_03_19_15_07"></script>
            <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/trackutils.js?ver=2016_03_19_15_07"></script>
            
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

	window.init = function(serializationObject, dimensions) {
		controller.init(serializationObject, dimensions);
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
		controller = new QCD.PageController();
		controller.constructor(viewName, pluginIdentifier, hasDataDefinition, popup);
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
