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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://website.w3.org/TR/html4/loose.dtd">

<html>

    <head>
        <sec:csrfMetaTags/>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/ChartJS/Chart.min.css?ver=${buildNumber}" type="text/css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap.min.css?ver=${buildNumber}" type="text/css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap-glyphicons.css?ver=${buildNumber}" type="text/css" />
        <link href="${pageContext.request.contextPath}/qcadooView/public/css/core/notification.css?ver=${buildNumber}" rel="stylesheet" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/technologies/public/css/technologyConfigurator.css?ver=${buildNumber}" type="text/css" />

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
        <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/popper.min.js?ver=${buildNumber}"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/bootstrap.min.js?ver=${buildNumber}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.pnotify.js?ver=${buildNumber}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/core/messagesController.js?ver=${buildNumber}"></script>

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

            $(function () {
                var csrfMetaNameSelector = "meta[name='_csrf']";
                var csrfHeadMetaNameSelector = "meta[name='_csrf_header']";

                var csrfToken = $(csrfMetaNameSelector).attr("content");
                window.top.$(csrfMetaNameSelector).attr("content", csrfToken);
                window.top.$('iframe').contents().find(csrfMetaNameSelector).attr("content", csrfToken);

                $(document).ajaxSend(function(e, xhr, options) {
                    var token = $(csrfMetaNameSelector).attr("content");
                    var header = $(csrfHeadMetaNameSelector).attr("content");
                    xhr.setRequestHeader(header, token);
                });
            });
        </script>
    </head>

    <body id="documentBody" class="dashboardColorBackground" role="document">
        <div class="container" role="main">
            <div class="clear"></div>
            <div id="loader" class="modal" tabindex="-1" role="dialog" data-backdrop="static">
                <div class="modal-dialog modal-sm" role="document">
                    <div class="modal-content">
                        <div class="modal-body">
                            <img src="/qcadooView/public/img/core/loading_indicator32.gif"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <jsp:include page="technologyConfiguratorWizard.jsp" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/technologies/public/js/technologyConfigurator.js?ver=${buildNumber}"></script>
    </body>

</html>
