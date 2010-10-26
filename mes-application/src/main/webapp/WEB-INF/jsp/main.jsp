<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<title>QCADOO MES</title>
	
	<link rel="stylesheet" href="css/mainPage.css" type="text/css" />
	<link rel="stylesheet" href="css/menuTopLevel.css" type="text/css" />
	<link rel="stylesheet" href="css/menu/style.css" type="text/css" />
	<link rel="stylesheet" href="css/notification.css" type="text/css" />

	<script type="text/javascript" src="js/lib/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="js/lib/jquery.pnotify.min.js"></script>
	<script type="text/javascript" src="js/qcd/utils/logger.js"></script>
	<script type="text/javascript" src="js/qcd/menu/model.js"></script>
	<script type="text/javascript" src="js/qcd/menu/menuController.js"></script>
	<script type="text/javascript" src="js/qcd/core/windowController.js"></script>
	<script type="text/javascript" src="js/qcd/core/messagesController.js"></script>
	
	<script type="text/javascript">

		var menuStructure = ${menuStructure}

		var windowController;
		
		jQuery(document).ready(function(){
			
			windowController = new QCD.WindowController(menuStructure);
			
			$("#mainPageIframe").load(function() {
				el = $('body', $('iframe').contents());
				el.click(function() {windowController.restoreMenuState()});
			});
		});

		window.goToPage = function(url, serializationObject) {
			windowController.goToPage(url, serializationObject);
		}

		window.goBack = function() {
			windowController.goBack();
		}

		window.goToLastPage = function() {
			windowController.goToLastPage();
		}

		window.onSessionExpired = function(serializationObject) {
			windowController.onSessionExpired(serializationObject);
		}

		window.addMessage = function(type, content) {
			windowController.addMessage(type, content);
		}

		window.goToMenuPosition = function(position) {
			windowController.goToMenuPosition(position);
		}
	
		</script>

</head>
<body>

	<div id="mainTopMenu">
		<div id="topLevelMenu">
			<img id="logoImage" src="css/images/logo_small.png"></img>
			<div id="topRightPanel">
				<button onclick="windowController.performLogout()">${commonTranslations["commons.button.logout"] }</button>
			</div>
		</div>
		<div id="firstLevelMenu">
		</div>
		<div id="secondLevelMenuWrapper">
			<div id="secondLevelMenu">
			</div>
			</div>
	</div>
	<div id="mainPageIframeWrapper"><iframe id="mainPageIframe" frameborder="0"></iframe></div>
</body>
</html>