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
    <c:forEach items="${slickGridTranslations}" var="translation">
        QCD.translations['<c:out value="${translation.key}" />'] = '<c:out value="${fn:replace(translation.value, '\\\'','\\\\\\'')}" escapeXml="false" />';
    </c:forEach>
</script>

<link rel="stylesheet" href="/qcadooView/public/SlickGrid-2.4.14/slick.grid.css" type="text/css"/>
<link rel="stylesheet" href="/qcadooView/public/SlickGrid-2.4.14/controls/slick.pager.css" type="text/css"/>
<link rel="stylesheet" href="/qcadooView/public/SlickGrid-2.4.14/css/smoothness/jquery-ui-1.11.3.custom.css"
      type="text/css"/>
<link rel="stylesheet" href="/qcadooView/public/SlickGrid-2.4.14/css/custom.css?ver=${buildNumber}" type="text/css"/>

<script src="/qcadooView/public/SlickGrid-2.4.14/lib/jquery.event.drag-2.3.0.js"></script>

<script src="/qcadooView/public/SlickGrid-2.4.14/slick.core.js"></script>
<script src="/qcadooView/public/SlickGrid-2.4.14/slick.grid.js"></script>
<script src="/qcadooView/public/SlickGrid-2.4.14/slick.dataview.js"></script>
<script src="/qcadooView/public/SlickGrid-2.4.14/controls/slick.pager.js"></script>

<div id="resourceAttributesGrid" class="qcadoo-grid"></div>
<div id="pager" style="width:100%;height:20px;"></div>

<script src="/materialFlowResources/public/js/resourcesAttributes.js?ver=${buildNumber}"></script>
<script type="text/JavaScript">
    jQuery(document).ready(function() {
        QCD.resourcesAttributes.init();
    });
</script>