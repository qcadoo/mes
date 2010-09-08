<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<link rel="stylesheet" href="css/mainPage.css" type="text/css" />
	
	<script type="text/javascript" src="js/lib/jquery-1.4.2.min.js"></script>
	
	<script type="text/javascript" src="js/qcd/core/windowController.js"></script>
	<script type="text/javascript" src="js/qcd/utils/logger.js"></script>
	
	<script type="text/javascript">

		var windowController;
	
		jQuery(document).ready(function(){
			windowController = new QCD.WindowController();
		});
		
		window.goToPage = function(url, serializationObject) {
			windowController.goToPage(url, serializationObject);
		}

		window.goBack = function() {
			windowController.goBack();
		}

		window.commonTranslations = new Object();
		<c:forEach items="${commonTranslations}" var="translation">
			window.commonTranslations["${translation.key}"] = "${translation.value}";
		</c:forEach>

	</script>
	
</head>
<body>
	<table id="mainTable" cellspacing="0" cellpadding="0">
		<tr id="headerRow">
			<td id="headerDiv">
				<div id="leftPanel">
					${headerLabel}
					<img id="loadingIndicator" src="css/images/loading.gif"></img>
				</div>
				<div id="rightPanel">
					<div id="navigationDiv">
						<select id="viewsSelect">
							<c:forEach items="${viewsList}" var="view">
								<option>${view.name }</option>
							</c:forEach>
						</select>
						<button onclick="windowController.goToSelectedPage()">${commonTranslations["commons.button.go"] }</button>
					</div>
					<div id="logoutDiv">
						<button onclick="windowController.performLogout()">${commonTranslations["commons.button.logout"] }</button>
					</div>
				</div>
			</td>
		</tr>
		<tr id="contentRow">
			<td class="noMargin">
				<iframe id="mainPageIframe">
				</iframe>
			</td>
		</tr>
	</table>	
</body>
</html>