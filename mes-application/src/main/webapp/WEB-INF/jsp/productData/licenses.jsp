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
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.css?ver=2016_03_18_11_53" type="text/css" />
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js?ver=2016_03_18_11_53"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-1.8.5.custom.min.js?ver=2016_03_18_11_53"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.jqGrid.min.js?ver=2016_03_18_11_53"></script>
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.js?ver=2016_03_18_11_53"></script>
		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/dashboard.css?ver=2016_03_18_11_53" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css?ver=2016_03_18_11_53" type="text/css" />
			<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/_jquery-1.4.2.min.js?ver=2016_03_18_11_53"></script>

		</c:otherwise>
	</c:choose>

	<script type="text/javascript">

		function goToMenuPosition(position) {
			if (window.parent.goToMenuPosition) {
				window.parent.goToMenuPosition(position);
			} else {
				window.location = "/main.html"
			}
		}
		
	</script>
</head>
<body style="background-color: white">

	<div id="windowContainer">
		<div id="windowContainerRibbon">
			<div id="q_row3_out">
				<div id="q_menu_row3"></div>
			</div>
			<div id="q_row4_out"></div>
		</div>
		<div id="windowContainerContentBody">


	<div id="contentWrapperOuter"  style="background-image: none;">
	<div id="contentWrapperMiddle">
	<div id="dashboardContentWrapper" style="background-image: none;">
		<div id="buttonsElement">
			<div class="dashboardButton" style="background-color: white; background-image: none; height: 200px; margin-right: 200px">
				<div class="dashboardButtonContent" style="width: 100%">
					<div style="font-weight: bold; line-height: 30px">
						Moduł produkcyjny do ERP OPTIMA<br/>
						<table>
						<tr>
						    <td>Ilość stanowisk:</td><td>3 szt</td>
						</tr>
						</table>
					</div>
					<div style="color: #818181; font-size: 15px; line-height: 20px">
					 	Pozostałe licencje OPTIMA: <br/>
					 	<table>
					 	    <tr>
					 	    <td>- Handel:</td><td>3 szt</td>
					 	    </tr>
					 	    <tr>
					 	    <td>- Księga Handlowa:</td><td>1 szt</td>
					 	    </tr>
					 	    <tr>
					 	    <td>- Środki Trwałe:</td><td>1 szt</td>
					 	    </tr>
					 	    <tr>
					 	    <td>- Płace i Kadry:</td><td>1 szt</td>
					 	    </tr>
					 	</table>
					</div>
				</div>
			</div>
			<div class="dashboardButton" style="background-color: white; background-image: none; height: 200px">
				<div class="dashboardButtonContent" style="width: 100%">
					<div style="font-weight: bold; line-height: 30px">
					    Licencja dla firmy:
					</div>
					<div style="font-size: 15px; line-height: 20px">
					 	NBL Kompozyty Sp z o.o.<br/>
					 	43-300 Bielsko-Biała<br/>
					 	ul. Boruty-Spiechowicza 50<br/>
					 	NIP: 9372584717
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