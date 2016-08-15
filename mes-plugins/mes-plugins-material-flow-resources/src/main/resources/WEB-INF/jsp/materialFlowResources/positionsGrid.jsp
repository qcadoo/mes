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
    <script src="/qcadooView/public/js/crud/qcd/components/jqGrid/jquery.auto-complete.js?ver=${buildNumber}"></script>

    <script src="/qcadooView/public/js/crud/qcd/components/jqGrid/jquery.jqGrid.min.js?ver=${buildNumber}"></script>
    <script src="/qcadooView/public/js/crud/qcd/components/jqGrid/angular.js?ver=${buildNumber}"></script>

    <c:choose>
        <c:when test="${locale == 'pl'}">
            <script type="text/ecmascript" src="/qcadooView/public/js/crud/qcd/components/jqGrid/grid.locale-pl.js?ver=${buildNumber}"></script>
        </c:when>
        <c:otherwise>
            <script type="text/ecmascript" src="/qcadooView/public/js/crud/qcd/components/jqGrid/grid.locale-en.js?ver=${buildNumber}"></script>
        </c:otherwise>
    </c:choose>

    <script src="/materialFlowResources/public/js/gridOptions.js?ver=${buildNumber}"></script>
    
    <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-ui-1.11.4.min.js?ver=${buildNumber}"></script>
    <script src="/qcadooView/public/js/crud/qcd/components/jqGrid/jquery.blockUI.js?ver=${buildNumber}"></script>

    <link rel="stylesheet" type="text/css" media="screen" href="/basic/public/css/ui.jqgrid.css?ver=${buildNumber}" />
    <link rel="stylesheet" type="text/css" media="screen" href="/basic/public/css/_jquery-ui-1.8.5.custom.css?ver=${buildNumber}" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/window.css?ver=${buildNumber}" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/grid.css?ver=${buildNumber}" type="text/css" />
    <link rel="stylesheet" type="text/css" media="screen" href="/basic/public/css/custom.css?ver=${buildNumber}">

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
