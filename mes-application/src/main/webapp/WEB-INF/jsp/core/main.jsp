<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.2.0

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

	<title>QCADOO MES</title>
	
	<c:choose>
		<c:when test="${compressStaticResources}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/qcadoo.min.css" type="text/css" />
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/qcadoo.min.js"></script>
		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/core/mainPage.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/core/menuTopLevel.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/core/menu/style.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/core/notification.css" type="text/css" />
		
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/core/lib/_jquery-1.4.2.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/core/lib/jquery.pnotify.min.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/core/qcd/utils/logger.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/core/qcd/menu/model.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/core/qcd/menu/menuController.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/core/qcd/core/windowController.js"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/core/qcd/core/messagesController.js"></script>
			
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/core/qcd/utils/snow.js"></script>
		</c:otherwise>
	</c:choose>
	
	<link rel="shortcut icon" href="/img/core/icons/favicon.png">
	
	<script type="text/javascript">

		var menuStructure = ${menuStructure}

		var windowController;
		
		jQuery(document).ready(function(){
			
			windowController = new QCD.WindowController(menuStructure);
			
			$("#mainPageIframe").load(function() {
				try {
					el = $('body', $('iframe').contents());
					el.click(function() {windowController.restoreMenuState()});
				} catch(e) {
				}
			});
		});

		window.goToPage = function(url, serializationObject, isPage) {
			windowController.goToPage(url, serializationObject, isPage);
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

		window.hasMenuPosition = function(position) {
			return windowController.hasMenuPosition(position);
		}

		window.getCurrentUserLogin = function() {
			return "${userLogin}";
		}
	
		window.translationsMap = new Object();
		<c:forEach items="${commonTranslations}" var="translation">
			window.translationsMap["${translation.key}"] = "${translation.value}";
		</c:forEach>
	
		
	</script>

</head>
<body>

	<div id="mainTopMenu">
		<div id="topLevelMenu">
			<img id="logoImage" src="css/core/images/logo_small.png" onclick="windowController.goToMenuPosition('home.home')"></img>
			<div id="topRightPanel">
				<span id="userInfo">${userLogin}</span>
				<a href='#' id="profileButton" onclick="windowController.goToMenuPosition('home.profile')">${commonTranslations["commons.button.userProfile"] }</a>
				<a href='#' onclick="windowController.performLogout()">${commonTranslations["commons.button.logout"] }</a>
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