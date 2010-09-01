<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

		
	<link rel="stylesheet" href="../css/jquery-ui-1.8.4.custom.css" type="text/css" />
	<link rel="stylesheet" href="../css/ui.jqgrid.css" type="text/css" />
	<link rel="stylesheet" href="../css/productGrid.css" type="text/css" />

	<script type="text/javascript" src="../js/lib/json_sans_eval.js"></script>
	<script type="text/javascript" src="../js/lib/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../js/lib/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../js/lib/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="../js/lib/encoder.js"></script>
	<script type="text/javascript" src="../js/qcd/elements/qcdGrid.js"></script>
	<script type="text/javascript" src="../js/lib/jquery.ba-serializeobject.min.js"></script>
	
	<script type="text/javascript" src="../js/qcd/utils/logger.js"></script>
	<script type="text/javascript" src="../js/qcd/core/pageController.js"></script>
	<script type="text/javascript" src="../js/qcd/core/pageConstructor.js"></script>
	<script type="text/javascript" src="../js/qcd/elements/qcdForm.js"></script>
	
	<script type="text/javascript">

		var viewName = "${viewDefinition.name}";
		var entityId = "${entityId}";

		jQuery(document).ready(function(){
			var controller = new QCD.PageController(viewName);
			controller.init(entityId);
		});

	</script>
</head>
<body>

<c:forEach items="${viewDefinition.elements}" var="viewElement">

	<div>
		<c:choose>
			<c:when test="${viewElement.type == 1}">
			
				<table class="element_table" id="${viewElement.name}">
					<td class=element_options>
						${viewElementsOptions[viewElement.name]}
					</td>
				</table>
				
			</c:when>
			<c:when test="${viewElement.type == 2}">
				<div class="element_form" id="${viewElement.name}">
					<div class=element_options>
						${viewElementsOptions[viewElement.name]}
					</div>
					<tiles:insertTemplate template="formTemplate.jsp">
						<tiles:putAttribute name="formId" value="${ viewElement.name}" />
						<tiles:putAttribute name="dataDefinition" value="${ viewElement.dataDefinition}" />
					</tiles:insertTemplate>
				</div>

			</c:when>
		</c:choose>
	</div>
	<br/>

</c:forEach>

</body>
</html>