<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.3.0

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
		<c:when test="${compressStaticResources}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/qcadoo.min.css" type="text/css" />
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/qcadoo.min.js"></script>
		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/dashboard.css" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css" type="text/css" />
			
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js"></script>
		</c:otherwise>
	</c:choose>
	
	<script type="text/javascript">

		jQuery(document).ready(function(){
			if (window.parent.hasMenuPosition && window.parent.hasMenuPosition('orders.productionOrders')) {
				$("#productionOrdersLink").show();
			}
			if (window.parent.hasMenuPosition && window.parent.hasMenuPosition('technology.technologies')) {
				$("#technologiesLink").show();
			}
			if (window.parent.hasMenuPosition && window.parent.hasMenuPosition('reports.materialRequirements')) {
				$("#materialRequirementsLink").show();
			}
		});

		function goToMenuPosition(position) {
			if (window.parent.goToMenuPosition) {
				window.parent.goToMenuPosition(position);
			} else {
				window.location = "/main.html"
			}
		}
		
	</script>
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
					<div class="dashboardButtonContentLink" id="productionOrdersLink" style="display: none;">
						<a href="#" onclick="goToMenuPosition('orders.productionOrders')">${translationsMap['core.dashboard.organize.link']}</a>
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
					<div class="dashboardButtonContentLink" id="technologiesLink" style="display: none;">
						<a href="#" onclick="goToMenuPosition('technology.technologies')">${translationsMap['core.dashboard.define.link']}</a>
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
					<div class="dashboardButtonContentLink" id="materialRequirementsLink" style="display: none;">
						<a href="#" onclick="goToMenuPosition('reports.materialRequirements')">${translationsMap['core.dashboard.react.link']}</a>
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