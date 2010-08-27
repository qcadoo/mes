<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page import="com.qcadoo.mes.core.data.definition.FieldTypeFactory" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	
</head>
<body>
	<div class="modalHeader"><spring:message code="${entityType}.form.header"/></div>
	<div class="modalContent">
		<form id="substituteForm">
			<%@ include file="formTable.jsp" %>
		</form>
	</div>
	<div class="modalFooter">
		<button id="ajaxSubmit" onclick="editSubstituteApplyClick()"><spring:message code="${entityType}.form.apply"/></button>
		<button class="jqmClose"><spring:message code="${entityType}.form.cancel"/></button>
	</div>
	
</body>
</html>