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

		var serverMessageType;
		var serverMessageHeader;
		var serverMessageContent;

		var messagePanel;
		var messagePanelHeader;
		var messagePanelContent;

		var loginErrorMessagePanel;
		var passwordErrorMessagePanel;
		
		var wrongLoginText = '${translation["security.message.wrongLogin"]}';
		var wrongPasswordText = '${translation["security.message.wrongPassword"]}';

		var errorHeaderText = '${translation["security.message.errorHeader"]}';
		var errorContentText = '${translation["security.message.errorContent"]}';
	
		<c:if test="${messageType != null }">
			serverMessageType = '<c:out value="${messageType}"/>';
			serverMessageHeader = '<c:out value="${translation[messageHeader]}"/>';
			serverMessageContent = '<c:out value="${translation[messageContent]}"/>';
		</c:if>

		jQuery(document).ready(function(){

			messagePanel = $("#messagePanel");
			messagePanelHeader = $("#messageHeader");
			messagePanelContent = $("#messageContent");
			
			loginErrorMessagePanel = $("#loginErrorMessagePanel");
			passwordErrorMessagePanel = $("#passwordErrorMessagePanel");
			
			if (serverMessageType) {
				showMessageBox(serverMessageType, serverMessageHeader, serverMessageContent);
			}
			
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

			hideLoginAndPasswordMessages();
			
			$.ajax({
				url: url,
				type: 'POST',
				data: formData,
				success: function(response) {
					response = $.trim(response);
					if (response == "loginSuccessfull") {
						if (window.parent.goToLastPage) {
							window.parent.goToLastPage();
						} else {
							window.location = "main.html"
						}
					} else {
						if (response == "loginUnsuccessfull:login") {
							hideMessageBox();
							addLoginMessage();
						} else if (response == "loginUnsuccessfull:password") {
							hideMessageBox();
							addPasswordMessage();
						} else {
							showMessageBox("error", errorHeaderText, errorContentText);
						} 
					}
				},
				error: function(xhr, textStatus, errorThrown){
					showMessageBox("error", errorHeaderText, errorMessage);
				}

			});
		}

		showMessageBox = function(type, header, content) {
			messagePanel.removeClass("info");
			messagePanel.removeClass("success");
			messagePanel.removeClass("error");
			messagePanel.addClass(type);
			messagePanelHeader.html(header);
			messagePanelContent.html(content);
			messagePanel.css("display", "block");
		}
		hideMessageBox = function() {
			messagePanel.css("display", "none");
		}
		addLoginMessage = function() {
			loginErrorMessagePanel.css("display", "block");
			$('#usernameInput').css("border-color", "#ec1c24");
		}
		addPasswordMessage = function() {
			passwordErrorMessagePanel.css("display", "block");
			$('#passwordInput').css("border-color", "#ec1c24");
		}
		hideLoginAndPasswordMessages = function() {
			loginErrorMessagePanel.css("display", "none");
			$('#usernameInput').css("border-color", "")
			passwordErrorMessagePanel.css("display", "none");
			$('#passwordInput').css("border-color", "");
		}

	</script>
	
</head>
<body>

	<div id="contentWrapperOuter">
	<div id="contentWrapperMiddle">
	<div id="contentWrapper">
	
		<div id="messagePanel" style="display: none;">
			<div id="messageHeader"></div>
			<div id="messageContent"></div>
		</div>
	
		<div id="loginContentWrapper">
		
			<div id="loginHeader">
				${translation["security.form.header"]}
				${iframe}
				<c:if test="${! iframe}">
					<div id="languageDiv">
				 		<select id="languageSelect" onchange="changeLanguage(this.value)">
				 			<option value="pl">polski</option>
				 			<option value="en">english</option>
				 		</select>
				 	</div>
			 	</c:if>
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
					 			<div id="loginErrorMessagePanel" style="display: none;">
					 				<div class="login_failed"></div>
					 				<span id="loginMessage" class="login_failed_message">${translation["security.message.wrongLogin"]}</span>
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
								<div id="passwordErrorMessagePanel" style="display: none;">
				 					<div class="login_failed"></div>
				 					<span id="passwordMessage" class="login_failed_message">${translation["security.message.wrongPassword"]}</span>								
								</div>
							</div>
						</div>
					</div>
			 		<div id="rememberMeRow">
			 			<label></label><input id="rememberMeCheckbox" type="checkbox" name="_spring_security_remember_me" /><label id="rememberMeLabel">${translation["security.form.label.rememberMe"]}</label>
			 		</div>
					<div id="loginButtonWrapper">
			 			<a href="#" id="loginButton" onclick="ajaxLogin(); return false;"><span>${translation['security.form.button.logIn']}</span></a>
					</div>
			    </form>
		 	</div>
	 
	 		<div id="loginFooter">
				<div id="loginFooterLine"></div>
				<div id="loginFooterLogo"></div>
			</div>
	 	</div>
 	</div>
 	</div>
 	</div>
</body>
</html>