<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<link rel="stylesheet" href="css/dashboard.css" type="text/css" />
	
</head>
<body>

	<div id="dashboardContentWrapper">
		<div id="userElement">
			Witaj <span id="userLogin">michal.nawalany@qcadoo.com</span>
		</div>
		<div id="descriptionElement">
			<div id="descriptionHeader">
				Manage your production with ease
			</div>
			<div id="descriptionContent">
				Introducing Qcadoo.
				Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum pulvinar nisi ut elit dictum quis egestas ipsum auctor. Nunc lacus tortor, ornare eu commodo sit amet, pulvinar quis est.
			</div>
		</div>
		<div id="buttonsElement">
			<div class="dashboardButton">
				<div class="dashboardButtonBg_Top"><div class="dashboardButtonBg_NW"></div><div class="dashboardButtonBg_N"></div><div class="dashboardButtonBg_NE"></div></div>
				<div class="dashboardButtonBg_Middle">
					<div class="dashboardButtonBg_W"></div><div class="dashboardButtonIcon icon1"></div><div class="dashboardButtonContent">
						<div class="dashboardButtonContentHeader">
							Organize
						</div>
						<div class="dashboardButtonContentText">
						 	Duis sed lorem pulvinar lorem posuere lobortis. Vestibulum scelerisque mi ut turpis convallis interdum.
						</div>
						<div class="dashboardButtonContentLink">
							<a href="#" onclick="window.parent.goToMenuPosition('products.productionOrders')">Zlecenia produkcyjne</a>
						</div>
					</div><div class="dashboardButtonBg_E"></div>
				</div>
				<div class="dashboardButtonBg_Bottom"><div class="dashboardButtonBg_SW"></div><div class="dashboardButtonBg_S"></div><div class="dashboardButtonBg_SE"></div></div>
			</div>
			<div class="dashboardButton">
				<div class="dashboardButtonBg_Top"><div class="dashboardButtonBg_NW"></div><div class="dashboardButtonBg_N"></div><div class="dashboardButtonBg_NE"></div></div>
				<div class="dashboardButtonBg_Middle">
					<div class="dashboardButtonBg_W"></div><div class="dashboardButtonIcon icon2"></div><div class="dashboardButtonContent">
						<div class="dashboardButtonContentHeader">
							Define
						</div>
						<div class="dashboardButtonContentText">
						 	Duis sed lorem pulvinar lorem posuere lobortis. Vestibulum scelerisque mi ut turpis convallis interdum.
						</div>
						<div class="dashboardButtonContentLink">
							<a href="#" onclick="window.parent.goToMenuPosition('products.instructions')">Instrukcje materiałowe</a>
						</div>
					</div><div class="dashboardButtonBg_E"></div>
				</div>
				<div class="dashboardButtonBg_Bottom"><div class="dashboardButtonBg_SW"></div><div class="dashboardButtonBg_S"></div><div class="dashboardButtonBg_SE"></div></div>
			</div>
			<div class="dashboardButton">
				<div class="dashboardButtonBg_Top"><div class="dashboardButtonBg_NW"></div><div class="dashboardButtonBg_N"></div><div class="dashboardButtonBg_NE"></div></div>
				<div class="dashboardButtonBg_Middle">
					<div class="dashboardButtonBg_W"></div><div class="dashboardButtonIcon icon3"></div><div class="dashboardButtonContent">
						<div class="dashboardButtonContentHeader">
							React
						</div>
						<div class="dashboardButtonContentText">
						 	Duis sed lorem pulvinar lorem posuere lobortis. Vestibulum scelerisque mi ut turpis convallis interdum.
						</div>
						<div class="dashboardButtonContentLink">
							<a href="#" onclick="window.parent.goToMenuPosition('products.materialRequirements')">Grupowanie zapotrzebowania materiałowego</a>
						</div>
					</div><div class="dashboardButtonBg_E"></div>
				</div>
				<div class="dashboardButtonBg_Bottom"><div class="dashboardButtonBg_SW"></div><div class="dashboardButtonBg_S"></div><div class="dashboardButtonBg_SE"></div></div>
			</div>
		</div>
	</div>
</body>
</html>