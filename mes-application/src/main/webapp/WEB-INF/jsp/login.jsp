<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	
	<link rel="stylesheet" href="css/login.css" type="text/css" />
	<link rel="stylesheet" href="css/components/form.css" type="text/css" />
	
	<script type="text/javascript" src="js/lib/jquery-1.4.2.min.js"></script>
	
	<script type="text/javascript" src="js/qcd/utils/serializator.js"></script>
		
	<script type="text/javascript">

		jQuery(document).ready(function(){
			$("#languageSelect").val("${currentLanguage}");
			$("#usernameInput").focus();
			$("#usernameInput").keypress(function(e) {
				var key=e.keyCode || e.which;
				if (key==13) {
					ajaxLogin();
				}
			});
			$("#passwordInput").keypress(function(e) {
				var key=e.keyCode || e.which;
				if (key==13) {
					ajaxLogin();
				}
			});
		});
	
		changeLanguage = function(language) {
			window.location = "login.html?lang="+language;
		}

		ajaxLogin = function() {
			var formData = QCDSerializator.serializeForm($("#loginForm"));
			var url = "j_spring_security_check";
			//$(".errorBox").hide();
			//$(".successBox").hide();
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
						$(".successBox").hide();
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

	<div id="loginContentWrapper">
	
		<div id="loginHeader">
			Logowanie do systemu
			<c:if test="${! iframe}">
				<div id="languageDiv">
			 		<select id="languageSelect" onchange="changeLanguage(this.value)">
			 			<option value="pl">polski</option>
			 			<option value="en">english</option>
			 		</select>
			 	</div>
		 	</c:if>
		</div>
		
		<div class="errorBox">
			<c:out value="${translation[errorMessage]}"/>
		</div>
		
	 	<div class="successBox">
			<c:out value="${translation[successMessage]}"/>
		</div>

		<div id="loginFormWrapper">
			<form id="loginForm" name="loginForm" action="<c:url value='j_spring_security_check'/>" method="POST">
		 		<div>
		 			<label>${translation["security.form.label.login"]}</label>
		 			<div class="component_container_form_w">
						<div class="component_container_form_inner">
							<div class="component_container_form_x"></div>
							<div class="component_container_form_y"></div>
		 					<input type='text' id="usernameInput" name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/>
			 			</div>
					</div>
		 		</div>
		 		<div>
		 			<label>${translation["security.form.label.password"]}</label>
		 			<div class="component_container_form_w">
						<div class="component_container_form_inner">
							<div class="component_container_form_x"></div>
							<div class="component_container_form_y"></div>
							<input type='password' id="passwordInput" name='j_password'>
						</div>
					</div>
				</div>
		 		<div><input id="rememberMeCheckbox" type="checkbox" name="_spring_security_remember_me" /><label id="rememberMeLabel">Zapamiętaj mnie na tym komputerze</label></div>
				<div id="loginButtonWrapper">
		 			<!--<input type="submit" value="${translation['security.form.button.logIn']}" onclick="ajaxLogin(); return false;" />-->
		 			<a href="#" id="loginButton" onclick="ajaxLogin(); return false;"></a>
				</div>
		    </form>
	 	</div>
 
 		<div id="loginFooter">
			<div id="loginFooterLine"></div>
			<div id="loginFooterLogo"></div>
		</div>
 	</div>
</body>
</html>