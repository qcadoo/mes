<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<link rel="stylesheet" href="css/core/dashboard.css" type="text/css" />
	<link rel="stylesheet" href="css/core/menu/style.css" type="text/css" />
	
</head>
<body>

	<div id="windowContainer">
		<div id="windowContainerRibbon">
			<div id="q_row3_out">
				<div id="q_menu_row3"></div>
			</div>
			<div id="q_row4_out"></div>
		</div>
		<div id="windowContainerContentBody">


	<div id="contentWrapperOuter">
	<div id="contentWrapperMiddle">
	<div id="dashboardContentWrapper">
		<div id="userElement">
			${translationsMap['core.dashboard.hello']} <span id="userLogin">${userLogin}</span>
		</div>
		<div id="descriptionElement">
			<div id="descriptionHeader">
				${translationsMap['core.dashboard.header']}
			</div>
			<div id="descriptionContent">
				${translationsMap['core.dashboard.content']}
			</div>
		</div>
		<div id="buttonsElement">
			<div class="dashboardButton">
				<div class="dashboardButtonIcon icon1"></div>
				<div class="dashboardButtonContent">
					<div class="dashboardButtonContentHeader">
						${translationsMap['core.dashboard.organize.header']}
					</div>
					<div class="dashboardButtonContentText">
					 	${translationsMap['core.dashboard.organize.content']}
					</div>
					<div class="dashboardButtonContentLink">
						<a href="#" onclick="window.parent.goToMenuPosition('products.productionOrders')">${translationsMap['core.dashboard.organize.link']}</a>
					</div>
				</div>
			</div>
			<div class="dashboardButton">
				<div class="dashboardButtonIcon icon2"></div>
				<div class="dashboardButtonContent">
					<div class="dashboardButtonContentHeader">
						${translationsMap['core.dashboard.define.header']}
					</div>
					<div class="dashboardButtonContentText">
					 	${translationsMap['core.dashboard.define.content']}
					</div>
					<div class="dashboardButtonContentLink">
						<a href="#" onclick="window.parent.goToMenuPosition('products.instructions')">${translationsMap['core.dashboard.define.link']}</a>
					</div>
				</div>
			</div>
			<div class="dashboardButton">
				<div class="dashboardButtonIcon icon3"></div>
				<div class="dashboardButtonContent">
					<div class="dashboardButtonContentHeader">
						${translationsMap['core.dashboard.react.header']}
					</div>
					<div class="dashboardButtonContentText">
					 	${translationsMap['core.dashboard.react.content']}
					</div>
					<div class="dashboardButtonContentLink">
						<a href="#" onclick="window.parent.goToMenuPosition('products.materialRequirements')">${translationsMap['core.dashboard.react.link']}</a>
					</div>
				</div>
			</div>
		</div>
	</div>
	</div>
	</div>
	
	</div>
	</div>
</body>
</html>