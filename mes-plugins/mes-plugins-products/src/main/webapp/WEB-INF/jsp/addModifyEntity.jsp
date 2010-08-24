<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Modify entity</title>
	<link rel="stylesheet" href="../css/jquery-ui-1.8.4.custom.css" type="text/css" />
	<link rel="stylesheet" href="../css/ui.jqgrid.css" type="text/css" />
	<link rel="stylesheet" href="../css/productGrid.css" type="text/css" />
	
	<script type="text/javascript" src="../js/json_sans_eval.js"></script>
	<script type="text/javascript" src="../js/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../js/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../js/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="../js/qcdGrid.js"></script>
	
	<script type="text/javascript">

		var colNames = new Array();
		var colModel = new Array();
			colNames.push("f1");
			colModel.push({name:"f1", index:"f1", width:100, sortable: false});
			colNames.push("f2");
			colModel.push({name:"f2", index:"f2", width:100, sortable: false});
		var substitutesGrid;

		var spColNames = new Array();
		var spColModel = new Array();
			spColNames.push("f11");
			spColModel.push({name:"f11", index:"f11", width:100, sortable: false});
			spColNames.push("f12");
			spColModel.push({name:"f12", index:"f12", width:100, sortable: false});
		var substituteProductsGrid;
		
		jQuery(document).ready(function(){
			var substitutesGrid = new QCDGrid({
				element: 'substitutesGrid',
				dataSource: "substitute/data.html?productId=${entityId }",
				height: 150, 
				paging: false,
				colNames: colNames,
				colModel: colModel,
				loadingText: 'Wczytuje...',
				onSelectRow: function(id){
			        console.debug('row '+id);
			        substituteProductsGrid.setOption('dataSource','substitute/products.html?productId=${entityId }&substituteId='+id);
			        substituteProductsGrid.refresh();
			        $("#newSubstituteProductButton").attr("disabled", false);
			        $("#deleteSubstituteButton").attr("disabled", false);
			        $("#upSubstituteButton").attr("disabled", false);
			        $("#downSubstituteButton").attr("disabled", false);
		        }
			});

			substituteProductsGrid = new QCDGrid({
				element: 'substituteProductsGrid',
				height: 150, 
				paging: false,
				colNames: spColNames,
				colModel: spColModel,
				loadingText: 'Wczytuje...',
				onSelectRow: function(id){
					$("#deleteSubstituteProductButton").attr("disabled", false);
				}
			});

			 $("#newSubstituteProductButton").attr("disabled", true);
			 $("#deleteSubstituteProductButton").attr("disabled", true);
			 $("#deleteSubstituteButton").attr("disabled", true);
			 $("#upSubstituteButton").attr("disabled", true);
			 $("#downSubstituteButton").attr("disabled", true);

			 if ("${entityId }") {
			 	substitutesGrid.refresh();
			 } else {
				 $("#newSubstituteButton").attr("disabled", true);
			 }
		});
		
	
	</script>
</head>
<body>
	<h2 id="pageHeader"><spring:message code="addModifyEntity.header"/></h2>
	
	
		<c:choose><c:when test="${message == null}"> </c:when><c:otherwise><spring:message code="addModifyEntity.${message }"/> <br/></c:otherwise></c:choose>
		
		<form action="addModifyEntity.html" method="POST">
			<table>
				<c:forEach items="${fieldsDefinition}" var="entry">
					<tr>
						<c:choose>
							<c:when test="${entry.hidden=='false'}">
									<td><spring:message code="products.field.${entry.name}"/></td><td>
									<input type="text" name="fields[${entry.name}]"
									<c:if test="${entry.editable=='true'}">
										readonly="readonly"
									</c:if> 
									value="${entity[entry.name]}" />
									</td><c:choose><c:when test="${fieldsValidationInfo[entry.name] == null}"> </c:when><c:otherwise><td><spring:message code="addModifyEntity.${fieldsValidationInfo[entry.name] }"/></td> </c:otherwise></c:choose>
							</c:when> 
							<c:otherwise>
								<input type="hidden" name="fields[${entry.name}]" value="${entity[entry.name]}" />
							</c:otherwise> 
						</c:choose>  
					</tr>
				</c:forEach>	
			</table>	
			<input type="hidden" name="id" value="${entityId }"/>
			<input type="submit" name="button" value="<spring:message code="addModifyEntity.button"/>" />
			<input type="button" name="button" value="<spring:message code="addModifyEntity.cancel"/>" onClick="window.location='list.html'" />
		</form><br />
		
		
		<div>
			Substytuty:
			<button id="newSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.new"/></button>
			<button id="deleteSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.delete"/></button>
			<button id="upSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.up"/></button>
			<button id="downSubstituteButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.down"/></button>
		</div>
		<table id="substitutesGrid"></table>
		<div>
			Produkty substytutu:
			<button id="newSubstituteProductButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.new"/></button>
			<button id="deleteSubstituteProductButton" onClick="console.info('not implemented')"><spring:message code="addModifyEntity.delete"/></button>
		</div>
		<table id="substituteProductsGrid"></table> 
</body>
</html>



