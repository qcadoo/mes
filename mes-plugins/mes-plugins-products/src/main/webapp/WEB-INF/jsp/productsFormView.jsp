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
	<link rel="stylesheet" href="../css/jqModal.css" type="text/css" />
	
	<script type="text/javascript" src="../js/json_sans_eval.js"></script>
	<script type="text/javascript" src="../js/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../js/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../js/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="../js/qcdGrid.js"></script>
	<script type="text/javascript" src="../js/jqModal.js"></script>
	<script type="text/javascript" src="../js/jquery.ba-serializeobject.min.js"></script>

</head>
<body>
	<h2 id="pageHeader"><spring:message code="productsFormView.header"/></h2>
	
			
		<form ID="productFormId">
			<%@ include file="formTable.jsp" %>
			
			
		</form>
		<button onclick="editEntityApplyClick('productFormId', 'saveEntity.html', 'products_product', function() {window.location='list.html'})"><spring:message code="productsFormView.button"/></button>
		<button onClick="window.location='list.html'"><spring:message code="productsFormView.cancel"/></button>
		<br />
		<br />
		<%@ include file="substitutes.jsp" %>

</body>
</html>



