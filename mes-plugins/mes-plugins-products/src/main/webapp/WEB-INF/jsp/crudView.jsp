<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ page import="com.qcadoo.mes.core.data.definition.FieldTypeFactory" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

		
	<link rel="stylesheet" href="../css/jquery-ui-1.8.4.custom.css" type="text/css" />
	<link rel="stylesheet" href="../css/ui.jqgrid.css" type="text/css" />
	<link rel="stylesheet" href="../css/productGrid.css" type="text/css" />

	<script type="text/javascript" src="../js/json_sans_eval.js"></script>
	<script type="text/javascript" src="../js/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../js/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../js/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="../js/encoder.js"></script>
	<script type="text/javascript" src="../js/qcdGrid.js"></script>
	<script type="text/javascript" src="../js/jquery.ba-serializeobject.min.js"></script>
	
	<script type="text/javascript">

		var definedParentEntities = new Object();
		<c:forEach items="${parentEntities}" var="entity">
			definedParentEntities["${entity.key}"] = "${entity.value}";
		</c:forEach>

		var viewElements = new Object();
		
		jQuery(document).ready(function(){

			console.info(definedParentEntities);
			
			$(".element_table").each(function(i,e){

				var optionsElement = $("#"+e.id+" .element_options");
				console.info(optionsElement);
				var options = jsonParse(optionsElement.html());
				console.info(options.columns);

				optionsElement.remove();
				
				var colNames = new Array();
				var colModel = new Array();

				for (var i in options.columns) {
					colNames.push(options.columns[i]);
					colModel.push({name:options.columns[i], index:options.columns[i], width:100, sortable: false});
				}

				var gridOptions = new Object();

				gridOptions.element = e.id;
				gridOptions.viewName = "${viewDefinition.name}",
				gridOptions.viewElementName = e.id;
				gridOptions.colNames = colNames;
				gridOptions.colModel = colModel;
				gridOptions.loadingText = '<spring:message code="commons.loading.gridLoading"/>';
				gridOptions.paging = options.options.paging == "true" ? true : false;
				gridOptions.parentDefinition = options.parentDefinition ? options.parentDefinition : null;
				if (options.options) {
					gridOptions.paging = options.options.paging == "true" ? true : false;
					gridOptions.sortable = options.options.sortable == "true" ? true : false;
					gridOptions.filter = options.options.filter == "true" ? true : false;
					gridOptions.multiselect = options.options.multiselect == "true" ? true : false;
					if (options.options.height) { gridOptions.height = parseInt(options.options.height); }
				}

				gridOptions.events = options.events;
				gridOptions.viewElements = viewElements;

				var grid = new QCDGrid(gridOptions);

				if (options.parentDefinition == null) {
					grid.enable();
					grid.refresh();
				} else {
					console.info(options.parentDefinition);
					var entity = definedParentEntities[options.parentDefinition];
					console.info(entity);
					if (entity) {
						grid.setParentId(entity);
					} else {
						grid.disable();
					}
				}
				viewElements[gridOptions.viewElementName] = grid;
			});

			$(".element_form").each(function(i,e){
				var formElement = new Object();
				
				var optionsElement = $("#"+e.id+" .element_options");
				console.info("form");
				console.info(optionsElement);
				var options = jsonParse(optionsElement.html());
				console.info(options);
				optionsElement.remove();

				formElement.id = e.id;
				formElement.options = options;
				
				viewElements[formElement.id] = formElement;
			});

		});

		editEntityApplyClick = function(formId, validatorPrefix, validResponseFunction) {

			//console.info(formElements[formId]);
			
			console.info(formId+"-"+ validatorPrefix);
			var formData = $('#'+formId+"_form").serializeObject();

			var form = formElements[formId];
			var url = "${viewDefinition.name}/"+form.id+"/save.html";
			
			$("."+validatorPrefix+"_validatorGlobalMessage").html('');
			$("."+validatorPrefix+"_fieldValidatorMessage").html('');
			$.ajax({
				url: url,
				type: 'POST',
				data: formData,
				success: function(response) {
					if (response.valid) {
						//validResponseFunction.call();
						console.info("ok")
					} else {
						$("."+validatorPrefix+"_validatorGlobalMessage").html(response.globalMessage);
						for (var field in response.fieldMessages) {
							$("#"+validatorPrefix+"_"+field+"_validateMessage").html(response.fieldMessages[field]);
						}
					}
				},
				error: function(xhr, textStatus, errorThrown){
					alert(textStatus);
				}
	
			});
			return false;
		}

	

	</script>
</head>
<body>

<c:forEach items="${viewDefinition.elements}" var="viewElement">

	<div>
		<c:choose>
			<c:when test="${viewElement.type == 1}">
			
				<table class="element_table" id="${viewElement.name}">
					<td class=element_options>
						{
						"dataDefinition"="${viewElement.dataDefinition.entityName}",
						<c:if test="${viewElement.options != null}">"options"={
							<c:forEach items="${viewElement.options}" var="option">
								"${option.key}": "${option.value}",
							</c:forEach>
							},
						</c:if>
						<c:if test="${viewElement.events != null}">"events"={
							<c:forEach items="${viewElement.events}" var="event">
								"${event.key}": "${event.value}",
							</c:forEach>
							},
						</c:if>
						<c:if test="${viewElement.parentDefinition != null}">"parentDefinition"="${viewElement.parentDefinition.entityName}"</c:if>,
						
						"columns"=[
							<c:forEach items="${viewElement.columns}" var="column">
								"${column.name}",
							</c:forEach>
						]
						}
					</td>
				</table>
				
			</c:when>
			<c:when test="${viewElement.type == 2}">
				<div class="element_form" id="${viewElement.name}">
					<div class=element_options>
						{
						"dataDefinition"="${viewElement.dataDefinition.entityName}",
						<c:if test="${viewElement.options != null}">"options"={
							<c:forEach items="${viewElement.options}" var="option">
								"${option.key}": "${option.value}",
							</c:forEach>
							},
						</c:if>
						<c:if test="${viewElement.events != null}">"events"={
							<c:forEach items="${viewElement.events}" var="event">
								"${event.key}": "${event.value}",
							</c:forEach>
							},
						</c:if>
						<c:if test="${viewElement.parentDefinition != null}">"parentDefinition"=${viewElement.parentDefinition.entityName}</c:if>,
						
						}
					</div>
					<tiles:insertTemplate template="formTemplate.jsp">
						<tiles:putAttribute name="formId" value="${ viewElement.name}" />
						<tiles:putAttribute name="dataDefinition" value="${ viewElement.dataDefinition}" />
						<tiles:putAttribute name="entity" value="${ entities[viewElement.name]}" />
					</tiles:insertTemplate>
				</div>

			</c:when>
		</c:choose>
	</div>
	<br/>

</c:forEach>

</body>
</html>