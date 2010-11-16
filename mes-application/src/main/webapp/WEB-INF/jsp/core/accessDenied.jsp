<%--

    ********************************************************************
    Code developed by amazing QCADOO developers team.
    Copyright (c) Qcadoo Limited sp. z o.o. (2010)
    ********************************************************************

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	
	<link rel="stylesheet" href="css/login.css" type="text/css" />
	
</head>
<body>
	<div class="errorMessageHeader">
		${translation["security.message.accessDenied.header"] }
	</div>
	<div class="errorMessageContent">
		${translation["security.message.accessDenied.info"] }
	</div>

</body>
</html>