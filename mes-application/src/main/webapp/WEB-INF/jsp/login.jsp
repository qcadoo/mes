<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	
	<link rel="stylesheet" href="css/login.css" type="text/css" />
	
	<script type="text/javascript" src="js/lib/jquery-1.4.2.min.js"></script>
	
	<script type="text/javascript" src="js/qcd/utils/serializator.js"></script>
		
	<script type="text/javascript">

		jQuery(document).ready(function(){
			$("#languageSelect").val("${currentLanguage}");
			$("#usernameInput").focus();
		});
	
		changeLanguage = function(language) {
			window.location = "login.html?lang="+language;
		}

		ajaxLogin = function() {
			var formData = QCDSerializator.serializeForm($("#loginForm"));
			var url = "j_spring_security_check";
			$(".errorBox").hide();
			$(".successBox").hide();
			$.ajax({
				url: url,
				type: 'POST',
				data: formData,
				success: function(response) {
					response = $.trim(response);
					if (response == "loginSuccessfull") {
						if (window.parent.goToLastPage) {
							//window.parent.goToPage(url, serializationObject);
							window.parent.goToLastPage();
						} else {
							window.location = "main.html"
						}
					} else {
						showLoginError(response);
					}
				},
				error: function(xhr, textStatus, errorThrown){
					showLoginError(textStatus);
				}

			});
		}

		showLoginError = function(error) {
			$(".errorBox").show().html("${translation["security.message.error"]}");
		}

	</script>
	
</head>
<body>
	<c:if test="${! iframe}">
		<div><img src="css/images/global.logo.png"></img></div>
	</c:if>
	
	<div class="errorBox">
		<c:out value="${translation[errorMessage]}"/>
	</div>
	
 	<div class="successBox">
		<c:out value="${translation[successMessage]}"/>
	</div>

	<c:if test="${! iframe}">
		<div class="langiageDiv">
			${translation["security.form.label.language"]}
	 		<select id="languageSelect" onchange="changeLanguage(this.value)">
	 			<option value="pl">polski</option>
	 			<option value="en">english</option>
	 		</select>
	 	</div>
 	</c:if>
 		
	<form id="loginForm" name="loginForm" action="<c:url value='j_spring_security_check'/>" method="POST">
 		<table>
        	<tr><td>${translation["security.form.label.login"]}</td><td><input type='text' id="usernameInput" name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
 	       <tr><td>${translation["security.form.label.password"]}</td><td><input type='password' name='j_password'></td></tr>
		</table>
		<div>
 			<input type="submit" value="${translation['security.form.button.logIn']}" onclick="ajaxLogin(); return false;" />
		</div>
    </form>
</body>
</html>