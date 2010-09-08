<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	
	<link rel="stylesheet" href="css/login.css" type="text/css" />
	
	<script type="text/javascript" src="js/lib/jquery-1.4.2.min.js"></script>
		
	<script type="text/javascript">

		jQuery(document).ready(function(){
			$("#languageSelect").val("${currentLanguage}");
		});
	
		changeLanguage = function(language) {
			window.location = "login.html?lang="+language;
		}

	</script>
	
</head>
<body>

	<div><img src="css/images/global.logo.png"></img></div>
	
	<c:if test="${not empty errorMessage}">
 		<div class="errorBox">
			<c:out value="${translation[errorMessage]}"/>
		</div>
    </c:if>
 	<c:if test="${not empty successMessage}">
 		<div class="successBox">
			<c:out value="${translation[successMessage]}"/>
		</div>
    </c:if>

	<div class="langiageDiv">
		${translation["login.form.label.language"]}
 		<select id="languageSelect" onchange="changeLanguage(this.value)">
 			<option value="pl">polski</option>
 			<option value="en">english</option>
 		</select>
 	</div>
 		
	<form name="loginForm" action="<c:url value='j_spring_security_check'/>" method="POST">
	<!-- <form name="loginForm" action="performLogin.html" method="POST">-->
      <table>
        <tr><td>${translation["login.form.label.login"]}</td><td><input type='text' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
        <tr><td>${translation["login.form.label.password"]}</td><td><input type='password' name='j_password'></td></tr>
 		
 		
 		 </table>
        <div><input name="submit" type="submit" value="${translation['login.form.button.logIn']}"></div>
    </form>
	
</body>
</html>