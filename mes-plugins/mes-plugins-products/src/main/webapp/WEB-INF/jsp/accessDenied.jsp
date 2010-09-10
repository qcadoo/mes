<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	
	<link rel="stylesheet" href="../css/login.css" type="text/css" />
	
</head>
<body>
	<div class="errorMessageHeader">
		${translation["login.message.accessDenied.header"] }
	</div>
	<div class="errorMessageContent">
		${translation["login.message.accessDenied.info"] }
	</div>

</body>
</html>