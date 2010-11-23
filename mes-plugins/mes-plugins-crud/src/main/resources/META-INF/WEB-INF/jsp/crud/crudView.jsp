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
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<link rel="stylesheet" href="../../css/core/jquery-ui-1.8.5.custom.css" type="text/css" />
	<link rel="stylesheet" href="../../css/crud/jquery.datepick.css" type="text/css" /> 
	<link rel="stylesheet" href="../../css/core/ui.jqgrid.css" type="text/css" />
	<link rel="stylesheet" href="../../css/crud/jstree/style.css" type="text/css" />
	<link rel="stylesheet" href="../../css/core/qcd.css" type="text/css" />
	<!--<link rel="stylesheet" href="../../css/menuRibbon.css" type="text/css" />-->
	<link rel="stylesheet" href="../../css/core/menu/style.css" type="text/css" />
	<link rel="stylesheet" href="../../css/core/notification.css" type="text/css" />
	<link rel="stylesheet" href="../../css/crud/components/window.css" type="text/css" />
	<link rel="stylesheet" href="../../css/crud/components/grid.css" type="text/css" />
	<link rel="stylesheet" href="../../css/crud/components/form.css" type="text/css" />
	<link rel="stylesheet" href="../../css/crud/components/tree.css" type="text/css" />
	<link rel="stylesheet" href="../../css/crud/components/elementHeader.css" type="text/css" />
	
	<script type="text/javascript" src="../../js/core/lib/json_sans_eval.js"></script>
	<script type="text/javascript" src="../../js/core/lib/json2.js"></script>
	<script type="text/javascript" src="../../js/core/lib/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../../js/core/lib/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../../js/core/lib/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="../../js/core/lib/jquery.jstree.js"></script>
	<script type="text/javascript" src="../../js/core/lib/jquery.cookie.js"></script>
	<script type="text/javascript" src="../../js/core/lib/jquery-ui-1.8.5.custom.min.js"></script>
	<script type="text/javascript" src="../../js/core/lib/jquery-ui-i18n.js"></script>
	<script type="text/javascript" src="../../js/core/lib/jquery.pnotify.min.js"></script>
	<script type="text/javascript" src="../../js/core/lib/encoder.js"></script>
	
	<script type="text/javascript" src="../../js/core/qcd/utils/logger.js"></script>
	<script type="text/javascript" src="../../js/core/qcd/utils/serializator.js"></script>
	<script type="text/javascript" src="../../js/core/qcd/utils/connector.js"></script>
	<script type="text/javascript" src="../../js/core/qcd/utils/options.js"></script>
	<script type="text/javascript" src="../../js/core/qcd/utils/pageConstructor.js"></script>
	<script type="text/javascript" src="../../js/core/qcd/core/messagesController.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/core/pageController.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/component.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/container.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/containers/window.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/containers/form.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/utils/elementHeaderUtils.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/formComponent.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/grid.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/grid/gridHeader.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/textInput.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/textArea.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/passwordInput.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/dynamicComboBox.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/entityComboBox.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/lookup.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/checkBox.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/linkButton.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/tree.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/calendar.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/elements/staticComponent.js"></script>
	<script type="text/javascript" src="../../js/crud/qcd/components/ribbon.js"></script>
	
	<script type="text/javascript">

		var viewName = "${viewDefinition.name}";
		var pluginIdentifier = "${viewDefinition.pluginIdentifier}";
		var entityId = "${entityId}";
		var context = '${context}';
		var locale = '${locale}';

		var hasDataDefinition = '${viewDefinition.dataDefinition}' == '' ? false : true;

		var lookupComponentName = '${lookupComponentName}';

		var controller = null;

		window.init = function(serializationObject) {
			controller.init(entityId, serializationObject);
		}

		window.canClose = function() {
			return controller.canClose();
		}

		window.getComponent = function(componentPath) {
			return controller.getComponent(componentPath);
		}

		jQuery(document).ready(function(){

			if (! window.parent.getCurrentUserLogin) {
				window.location = "/main.html";
			}
			
			controller = new QCD.PageController(viewName, pluginIdentifier, context, lookupComponentName, hasDataDefinition);
			if (window.opener) {
				window.opener[lookupComponentName+"_onReadyFunction"].call();
		    }
		});

		window.translationsMap = new Object();
		<c:forEach items="${translationsMap}" var="translation">
			window.translationsMap["${translation.key}"] = "${translation.value}";
		</c:forEach>
	</script>
</head>
<body>

		<tiles:insertTemplate template="components/component.jsp">
			<tiles:putAttribute name="component" value="${viewDefinition.root}" />
			<tiles:putAttribute name="viewName" value="${viewDefinition.name}" />
			<tiles:putAttribute name="pluginIdentifier" value="${viewDefinition.pluginIdentifier}" />
		</tiles:insertTemplate>

</body>
</html>