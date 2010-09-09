<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	
	<link rel="stylesheet" href="css/login.css" type="text/css" />
	
</head>
<body>

	<div class="errorBox">
		<c:out value="${translation['login.message.timeout']}"/>
	</div>

	<form name="loginForm" action="<c:url value='j_spring_security_check'/>" method="POST">
	<!-- <form name="loginForm" action="performLogin.html" method="POST">-->
      <table>
        <tr><td>${translation["login.form.label.login"]}</td><td><input type='text' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
        <tr><td>${translation["login.form.label.password"]}</td><td><input type='password' name='j_password'></td></tr>
 		
 		
 		 </table>
 		<input type="hidden" name="redirectAddress" value="http://www.google.pl"/>
        <div><input name="submit" type="submit" value="${translation['login.form.button.logIn']}"></div>
    </form>

</body>
</html>