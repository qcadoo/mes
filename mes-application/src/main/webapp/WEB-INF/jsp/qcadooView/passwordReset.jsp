<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo Framework
    Version: 1.4

    This file is part of Qcadoo.

    Qcadoo is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation; either version 3 of the License,
    or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    ***************************************************************************

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<c:choose>
		<c:when test="${useCompressedStaticResources}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/custom.css?ver=2016_03_19_15_07" type="text/css" />
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-1.8.5.custom.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.jqGrid.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.js?ver=2016_03_19_15_07"></script>
		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/qcd.css?ver=2016_03_19_15_07" type="text/css">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/passwordReset.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/form.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/jqModal.css?ver=2016_03_19_15_07" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/custom.css?ver=2016_03_19_15_07" type="text/css" />
			
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jqModal.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/serializator.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/logger.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/utils/modal.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.blockUI.js?ver=2016_03_19_15_07"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/crud/qcd/components/elements/utils/loadingIndicator.js?ver=2016_03_19_15_07"></script>
		</c:otherwise>
	</c:choose>

	<link rel="shortcut icon" href="/qcadooView/public/img/core/icons/favicon.png">
	
	<title>${applicationDisplayName} :: forgot password</title>
		
	<script type="text/javascript">

		var QCD = QCD || {};
		QCD.global = QCD.global || {};
		QCD.global.isSonowOnPage = false;

		var formModal;
		
		var serverMessageType;
		var serverMessageHeader;
		var serverMessageContent;

		var messagePanel;
		var messagePanelHeader;
		var messagePanelContent;

		var loginErrorMessagePanel;
		
		var wrongLoginText = '${translation["security.message.wrongLogin"]}';

		var errorHeaderText = '${translation["security.message.errorHeader"]}';
		var errorContentText = '${translation["security.message.errorContent"]}';
		
		var invalidMailAddressText = '${translation["security.message.invalidMailAddressContent"]}';
		var invalidConfigContentText = '${translation["security.message.invalidMailConfigContent"]}';
		var userNotFoundText = '${translation["security.message.loginNotFound"]}';
		
		var successHeaderText = '${translation["security.message.passwordReset.successHeader"]}';
		var successContentText = '${translation["security.message.passwordReset.successContent"]}'; 

		var isPopup = ${popup};
		var targetUrl = "${targetUrl}";

		var usernameInput;
	
		<c:if test="${messageType != null }">
			serverMessageType = '<c:out value="${messageType}"/>';
			serverMessageHeader = '<c:out value="${translation[messageHeader]}"/>';
			serverMessageContent = '<c:out value="${translation[messageContent]}"/>';
		</c:if>

		jQuery(document).ready(function(){
			formModal = $("#passwordResetContentWrapper");
			
			messagePanel = $("#messagePanel");
			messagePanelHeader = $("#messageHeader");
			messagePanelContent = $("#messageContent");
			
			loginErrorMessagePanel = $("#loginErrorMessagePanel");

			usernameInput = $("#usernameInput");
			
			if (serverMessageType) {
				showMessageBox(serverMessageType, serverMessageHeader, serverMessageContent);
			}

			usernameInput.focus();
			usernameInput.keypress(function(e) {
				var key=e.keyCode || e.which;
				if (key==13) {
					ajaxSubmit();
					return false;
				}
			});

			$("#languageSelect").val("${currentLanguage}");
			
		});
		
		blockForm = function() {
			QCD.components.elements.utils.LoadingIndicator.blockElement(formModal);
		}

		unblockForm = function() {
			QCD.components.elements.utils.LoadingIndicator.unblockElement(formModal);
		}
	
		changeLanguage = function(language) {
			window.location = "passwordReset.html?lang="+language;
		}

		ajaxSubmit = function() {
			usernameInput.attr("disabled", ""); // enable field to send it in form (and disable it later if neceserry)
			var formData = QCDSerializator.serializeForm($("#passwordResetForm"));
			if (window.parent.getCurrentUserLogin) {
				usernameInput.attr("disabled", "disabled");
			}
			var url = "passwordReset.html";

			hideLoginMessages();
			
			blockForm();
			
			$.ajax({
				url: url,
				type: 'POST',
				data: formData,
				success: function(response) {
					response = $.trim(response);
					switch(response) {
						case "success":
							window.location = "login.html?passwordReseted=true";
							break;
						case "userNotFound":
							showMessageBox("error", errorHeaderText, userNotFoundText);
							$('#usernameInput').css("border-co.css", "#ec1c24");
							break;
						case "loginIsBlank":
							hideMessageBox();
							addLoginMessage();
							break;
						case "invalidMailAddress":
							showMessageBox("error", errorHeaderText, invalidMailAddressText);
							break;
						case "invalidMailConfig":
							showMessageBox("error", errorHeaderText, invalidConfigContentText);
							break;
						default:
							showMessageBox("error", errorHeaderText, errorContentText);
							break;
					}
					unblockForm();
				},
				error: function(xhr, textStatus, errorThrown){
					showMessageBox("error", errorHeaderText, errorMessage);
					unblockForm();
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
		hideLoginMessages = function() {
			loginErrorMessagePanel.css("display", "none");
			$('#usernameInput').css("border-color", "")
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
	
		<div id="passwordResetContentWrapper">
		
			<div id="passwordResetHeader">
				${translation["security.form.header.passwordReset"]}
				<c:if test="${! iframe && ! popup}">
					<div id="languageDiv">
				 		<select id="languageSelect" onchange="changeLanguage(this.value)">
				 			<c:forEach items="${locales}" var="localesEntry">
				 				<option value="${localesEntry.key}">${localesEntry.value}</option>
				 			</c:forEach>
				 		</select>
				 	</div>
			 	</c:if>
			</div>
	
			<div id="passwordResetFormWrapper">
				<form id="passwordResetForm" name="passwordResetForm" method="POST">
			 		<div>
			 			<label>${translation["security.form.label.login"]}</label>
			 			<div class="component_form_element" style="height: 20px; width: 200px; vertical-align: middle; display: inline-block;">
			 			<div class="component_container_form_w" id="usernameInput_component_container_form_w" style="left: 0; right: 0;">
							<div class="component_container_form_inner">
								<div class="component_container_form_x"></div>
								<div class="component_container_form_y"></div>
				 				<input type='text' id="usernameInput" name='login' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/>
					 			<div id="loginErrorMessagePanel" class="errorMessagePanel" style="display: none;">
					 				<div class="login_failed"></div>
					 				<span id="loginMessage" class="login_failed_message">${translation["security.message.wrongLogin"]}</span>
					 			</div>
				 			</div>
						</div>
						</div>
			 		</div>
			 		<div id="passwordResetDescriptionWrapper">
			 			<span id="passwordResetDescription">
			 				${translation["security.form.content.passwordReset"]}
			 			</span>
			 		</div>
					<div id="passwordResetButtonWrapper">
			 			<a href="#" id="passwordResetButton" onclick="ajaxSubmit(); return false;"><span>${translation['security.form.button.passwordReset']}</span></a>
					</div>
			    </form>
		 	</div>
	 
	 		<div id="passwordResetFooter">
				<div id="passwordResetFooterLine"></div>
				<div id="passwordResetFooterLogo"></div>
			</div>
	 	</div>
 	</div>
 	</div>
 	</div>
</body>
</html>