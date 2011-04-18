<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
String ctx = request.getContextPath();
%>

<script type="text/JavaScript">

	var errorTimePassed = false;

	jQuery(document).ready(function(){

		window.mainController.setWindowHeader("${headerLabel}");

		setTimeout('errorTimePassed = true;', 60000);
		checkStatusTimer = setTimeout('checkStatus();', 5000);
		
		$.ajax({
			url: 'performRestart.html',
			type: 'POST',
			complete: function(XMLHttpRequest, textStatus) {
				setTimeout('checkStatus();',5000);
			}
		});	
	});

	function checkStatus() {
		
		if (errorTimePassed) {
			$("#restartMessage").hide();
			$("#errorMessage").show();
			return;
		}
		
		$.ajax({
			url: 'restartPage.html',
			type: 'GET',
			timeout: 2000, 
			complete: function(XMLHttpRequest, textStatus) {
				try {
					if (XMLHttpRequest.status == 200) {
						window.location = "${redirectPage}";
						return;
					} else {
						setTimeout("checkStatus();",1000);
					}
				} catch (e) {
					setTimeout("checkStatus();",1000);
				}
			}
		});
		
	}

</script>

<div style="margin: 10px;">
	<span id="restartMessage" style="padding-top: 10px; padding-bottom: 10px; font-size: 18px; height: 35px; line-height: 35px; background: transparent url('/qcadooView/public/img/core/loading_indicator.gif') no-repeat 0 0; padding-left: 35px;">
		${restartMessage}
	</span>
	<span id="errorMessage" style="display: none; padding-top: 10px; padding-bottom: 10px; font-size: 18px; height: 35px; line-height: 35px; color: red;">
		${restartErrorMessage}
	</span>
</div>