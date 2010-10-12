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
	<link rel="stylesheet" href="../../css/jstree/style.css" type="text/css" />
	<link rel="stylesheet" href="../../css/qcd.css" type="text/css" />
	<!--<link rel="stylesheet" href="/mes-application/css/menuRibbon.css" type="text/css" />-->
	<link rel="stylesheet" href="../../css/menuRibbon.css" type="text/css" />
	<link rel="stylesheet" href="../../css/components/window.css" type="text/css" />
	<link rel="stylesheet" href="../../css/components/grid.css" type="text/css" />
	<link rel="stylesheet" href="../../css/components/form.css" type="text/css" />
	<link rel="stylesheet" href="../../css/components/tree.css" type="text/css" />
	
	<script type="text/javascript" src="../../js/lib/json_sans_eval.js"></script>
	<script type="text/javascript" src="../../js/lib/json2.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery.jstree.js"></script>
	<script type="text/javascript" src="../../js/lib/jquery.cookie.js"></script>
	<script type="text/javascript" src="../../js/lib/encoder.js"></script>
	
	<script type="text/javascript" src="../../js/qcd/utils/logger.js"></script>
	<script type="text/javascript" src="../../js/qcd/utils/serializator.js"></script>
	<script type="text/javascript" src="../../js/qcd/utils/connector.js"></script>
	<script type="text/javascript" src="../../js/qcd/utils/options.js"></script>
	<script type="text/javascript" src="../../js/qcd/utils/pageConstructor.js"></script>
	<script type="text/javascript" src="../../js/qcd/core/pageController.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/component.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/container.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/containers/window.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/containers/form.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/elements/grid.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/elements/grid/gridHeader.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/elements/textInput.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/elements/dynamicComboBox.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/elements/entityComboBox.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/elements/checkBox.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/elements/linkButton.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/elements/tree.js"></script>
	<script type="text/javascript" src="../../js/qcd/components/ribbon.js"></script>
	
	<script type="text/javascript">

		var viewName = "${viewDefinition.name}";
		var pluginIdentifier = "${viewDefinition.pluginIdentifier}";
		var entityId = "${entityId}";
		//var contextEntityId = "${contextEntityId}";
		//var contextFieldName = "${contextFieldName}";
		var context = '${context}';

		var controller = null

		window.init = function(serializationObject) {
			controller = new QCD.PageController(viewName, pluginIdentifier, context);
			controller.init(entityId, serializationObject);
		}

		window.translationsMap = new Object();
		<c:forEach items="${translationsMap}" var="translation">
			window.translationsMap["${translation.key}"] = "${translation.value}";
		</c:forEach>

	</script>
</head>
<body>

	
		<!--<div style="color: red">
			${message}
		</div>-->
	
		<tiles:insertTemplate template="components/component.jsp">
			<tiles:putAttribute name="component" value="${viewDefinition.root}" />
			<tiles:putAttribute name="viewName" value="${viewDefinition.name}" />
			<tiles:putAttribute name="pluginIdentifier" value="${viewDefinition.pluginIdentifier}" />
		</tiles:insertTemplate>
	
</body>
</html>