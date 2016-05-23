<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 1.3

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

<%
String ctx = request.getContextPath();
%>

<div id="gridWrapper">
    <script src="/materialFlowResources/public/js/jquery.auto-complete.js"></script>

    <script src="/basic/public/js/jquery.jqGrid.min.js"></script>
    <script src="/materialFlowResources/public/js/angular.js"></script>

    <c:choose>
        <c:when test="${locale == 'pl'}">
            <script type="text/ecmascript" src="/basic/public/js/grid.locale-pl.js"></script>
        </c:when>
        <c:otherwise>
            <script type="text/ecmascript" src="/basic/public/js/grid.locale-en.js"></script>
        </c:otherwise>
    </c:choose>

    <script src="/materialFlowResources/public/js/gridOptions.js"></script>
    
    <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-1.11.4.min.js?ver=${buildNumber}"></script>
    <script src="/materialFlowResources/public/js/jquery.blockUI.js"></script>

    <link rel="stylesheet" type="text/css" media="screen" href="/basic/public/css/ui.jqgrid.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="/basic/public/css/_jquery-ui-1.8.5.custom.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/window.css?ver=${buildNumber}" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/grid.css?ver=${buildNumber}" type="text/css" />
    <link rel="stylesheet" type="text/css" media="screen" href="/materialFlowResources/public/css/custom.css">
    <link rel="stylesheet" type="text/css" media="screen" href="/materialFlowResources/public/css/jquery.auto-complete.css">

        <script type="text/javascript">
            var QCD = QCD || {};

            QCD.currentLang = '<c:out value="${locale}" />';

            QCD.translate = function (key) {
                var msg = QCD.translations[key];
                return msg === undefined ? '[' + key + ']' : msg;
            };

            QCD.translations = {};
            <c:forEach items="${translationsMap}" var="translation">
                QCD.translations['<c:out value="${translation.key}" />'] = '<c:out value="${fn:replace(translation.value, '\\\'','\\\\\\'')}" escapeXml="false" />';
            </c:forEach>
        </script>

    <div class="windowContainer" style="background:#9b9b9b;" ng-app="gridApp" ng-controller="GridController" id="GridController">

        <div id="gridContainer">
            <ng-jq-grid config="config" data="data"></ng-jq-grid>
            <table id="grid"></table>
            <div id="jqGridPager"></div>
        </div>
    </div>
</div>
