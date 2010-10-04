<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<title>QCADOO MES</title>
	
	<link rel="stylesheet" href="css/mainPage.css" type="text/css" />
	<link rel="stylesheet" href="css/menuTopLevel.css" type="text/css" />
	<link rel="stylesheet" href="css/menu.css" type="text/css" />
	<link rel="stylesheet" href="css/menuRibbon.css" type="text/css" />
	
	<script type="text/javascript" src="js/lib/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="js/qcd/utils/logger.js"></script>
	<script type="text/javascript" src="js/qcd/menu/model.js"></script>
	<script type="text/javascript" src="js/qcd/menu/menuController.js"></script>
	<script type="text/javascript" src="js/qcd/core/windowController.js"></script>
	
	<script type="text/javascript">

		var menuStructure = ${menuStructure}

		var windowController;
		
		jQuery(document).ready(function(){
			windowController = new QCD.WindowController(menuController);

			var menuController = new QCD.menu.MenuController(menuStructure, windowController);
			
			$("#mainPageIframe").load(function() {
				el = $('body', $('iframe').contents());
				el.click(function() {menuController.restoreState()});
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
	
		</script>

</head>
<body>

	<table id="mainStructuralTable" cellspacing="0" cellpadding="0">
		<tr id="mainHeaderRow">
			<td id="mainHeaderCell">
				<div id="topLevelMenu">
					<img id="logoImage" src="css/images/logo_small.png"></img>
					<div id="topRightPanel">
						<button onclick="windowController.performLogout()">${commonTranslations["commons.button.logout"] }</button>
					</div>
				</div>
				<div id="firstLevelMenu">
				</div>
				<div id="secondLevelMenu">
				</div>
				<!--<div id="ribbonLevelMenu">
				</div>
			--></td>
		</tr>
		<tr id="mainContentRow">
			<td class="noMargin">
				<iframe id="mainPageIframe" src="testPage.html" >
				</iframe>
			</td>
		</tr>
	</table>

</body>
</html>