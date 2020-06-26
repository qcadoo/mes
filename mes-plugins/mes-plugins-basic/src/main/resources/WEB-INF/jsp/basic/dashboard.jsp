<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<script type="text/javascript">
    var QCD = QCD || {};

    QCD.currentLang = '<c:out value="${locale}" />';

    QCD.translate = function (key) {
        return QCD.translations[key] || '[' + key + ']';
    };

    QCD.translations = {};
    <c:forEach items="${translationsMap}" var="translation">
    QCD.translations['<c:out value="${translation.key}" />'] = '<c:out value="${fn:replace(translation.value, '\\\'','\\\\\\'')}" escapeXml="false" />';
    </c:forEach>
</script>

<html>

<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/ChartJS/Chart.min.css?ver=${buildNumber}" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap.min.css?ver=${buildNumber}" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap-glyphicons.css?ver=${buildNumber}" type="text/css" />

	<c:choose>
		<c:when test="${useCompressedStaticResources}">
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.css?ver=${buildNumber}" type="text/css" />
		</c:when>
		<c:otherwise>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/dashboard.css?ver=${buildNumber}" type="text/css" />
			<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css?ver=${buildNumber}" type="text/css" />
		</c:otherwise>
	</c:choose>

    <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-3.2.1.min.js?ver=${buildNumber}"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/moment-with-locales.js?ver=${buildNumber}"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/ChartJS/Chart.min.js?ver=${buildNumber}"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/popper.min.js?ver=${buildNumber}"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/bootstrap.min.js?ver=${buildNumber}"></script>
</head>

<body>
	<div id="windowContainer">
		<div id="windowContainerContentBody">
			<div id="contentWrapperMiddle">
				<div id="dashboardContentWrapper">
					<div id="descriptionElement">
						<div id="descriptionHeader">
							${translationsMap['basic.dashboard.header']}
						</div>
					</div>
				</div>
				<div class="clear"></div>
                <c:if test="${showChartOnDashboard}">
                    <div id="chartElement" class="chart-container">
                        <canvas id="chart"></canvas>
                    </div>
                </c:if>
                <c:if test="${dashboardButtons.size() > 0}">
                    <div id="buttonsElement">
                        <div class="card-columns">
                            <c:forEach items="${dashboardButtons}" var="dashboardButton">
                                <c:set var = "identifier" value = "${dashboardButton.getStringField('identifier')}" />
                                <c:set var = "item" value = "${dashboardButton.getBelongsToField('item')}" />
                                <c:set var = "icon" value = "${dashboardButton.getStringField('icon')}" />

                                <c:if test="${item != null}">
                                    <c:set var = "category" value = "${item.getBelongsToField('category')}" />

                                    <c:set var = "categoryName" value = "${category.getStringField('name')}" />
                                    <c:set var = "itemName" value = "${item.getStringField('name')}" />

                                    <div class="card bg-warning text-white" style="display: none;"
                                        onclick="goToMenuPosition('${categoryName}.${itemName}')">
                                        <div class="card-body">
                                            <img class="img float-left" src="${icon}" alt="${translationsMap[identifier]}"/>
                                            <span class="glyphicon glyphicon-chevron-right float-right"></span>
                                            <h5 class="card-title">${translationsMap[identifier]}</h5>
                                        </div>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="${pageContext.request.contextPath}/basic/public/js/dashboard.js?ver=${buildNumber}"></script>
</body>

</html>