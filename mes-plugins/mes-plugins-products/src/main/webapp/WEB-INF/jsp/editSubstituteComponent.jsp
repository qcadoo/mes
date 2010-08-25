<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<link rel="stylesheet" href="../css/jqModal.css" type="text/css" />

</head>
<body>
	<div class="modalHeader">substitute component</div>
	<div class="modalContent">
		hello
		<br />substitute: ${substituteId}
		<c:if test="${componentId != null}">
			<br />component: ${componentId}
		</c:if>
	</div>
	<div class="modalFooter"><button class="jqmClose">zamknij</button></div>
</body>
</html>