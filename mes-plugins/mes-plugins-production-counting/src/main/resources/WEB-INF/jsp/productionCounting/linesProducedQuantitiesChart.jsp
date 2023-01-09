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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<script type="text/javascript">
    var QCD = QCD || {};

    QCD.currentLang = '<c:out value="${locale}" />';

    QCD.translate = function (key) {
        return QCD.translations[key] || '[' + key + ']';
    };

    QCD.translations = {};
    <c:forEach items="${chartTranslations}" var="translation">
        QCD.translations['<c:out value="${translation.key}" />'] = '<c:out value="${fn:replace(translation.value, '\\\'','\\\\\\'')}" escapeXml="false" />';
    </c:forEach>
</script>

<link rel="stylesheet" href="/qcadooView/public/ChartJS/Chart.min.css?ver=${buildNumber}" type="text/css" />

<script type="text/javascript" src="/qcadooView/public/ChartJS/Chart.min.js?ver=${buildNumber}"></script>
<script type="text/javascript" src="/qcadooView/public/jspdf/jspdf.min.js"></script>

<div id="linesProducedQuantitiesChart" class="chart-container">
    <canvas id="chart"></canvas>
</div>

<script src="/productionCounting/public/js/linesProducedQuantitiesChart.js?ver=${buildNumber}"></script>
