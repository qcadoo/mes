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

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

    <head>
        <sec:csrfMetaTags/>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/ChartJS/Chart.min.css?ver=${buildNumber}" type="text/css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap.min.css?ver=${buildNumber}" type="text/css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap-glyphicons.css?ver=${buildNumber}" type="text/css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/notification.css?ver=${buildNumber}"/>
        <c:choose>
            <c:when test="${useCompressedStaticResources}">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/qcadoo-min.css?ver=${buildNumber}" type="text/css"/>
            </c:when>
            <c:otherwise>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/dashboard.css?ver=${buildNumber}" type="text/css"/>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css?ver=${buildNumber}" type="text/css"/>
            </c:otherwise>
        </c:choose>

        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery-3.2.1.min.js?ver=${buildNumber}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/moment-with-locales.js?ver=${buildNumber}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/ChartJS/Chart.min.js?ver=${buildNumber}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/popper.min.js?ver=${buildNumber}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/bootstrap.min.js?ver=${buildNumber}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/jquery.pnotify.js?ver=${buildNumber}"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/qcadooView/public/js/core/qcd/core/messagesController.js?ver=${buildNumber}"></script>

        <script type="text/javascript">
            var QCD = QCD || {};

            QCD.currentLang = '<c:out value="${locale}" />';
            QCD.wizardToOpen = '<c:out value="${wizardToOpen}" />';
            QCD.enableOrdersLinkOnDashboard = '<c:out value="${enableOrdersLinkOnDashboard}" />';
            QCD.enableRegistrationTerminalOnDashboard = '<c:out value="${enableRegistrationTerminalOnDashboard}" />';
            QCD.quantityMadeOnTheBasisOfDashboard = '<c:out value="${quantityMadeOnTheBasisOfDashboard}" />';
            QCD.enablePrintLabelOnDashboard = '<c:out value="${enablePrintLabelOnDashboard}" />';

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
            <c:if test="${showChartOnDashboard}">
                <div id="dashboardChart" class="chart-container">
                    <canvas id="chart"></canvas>
                </div>
            </c:if>
            <c:if test="${dashboardButtons.size() > 0}">
                <div id="dashboardButtons">
                    <div class="card-columns">
                        <c:forEach items="${dashboardButtons}" var="dashboardButton">
                            <c:set var = "identifier" value = "${dashboardButton.getStringField('identifier')}" />
                            <c:set var = "item" value = "${dashboardButton.getBelongsToField('item')}" />
                            <c:set var = "icon" value = "${dashboardButton.getStringField('icon')}" />

                            <c:if test="${item != null}">
                                <c:set var = "category" value = "${item.getBelongsToField('category')}" />

                                <c:set var = "categoryName" value = "${category.getStringField('name')}" />
                                <c:set var = "itemName" value = "${item.getStringField('name')}" />

                                <div class="card bg-secondary text-white" style="display: none;"
                                    onclick="goToMenuPosition('${categoryName}.${itemName}')">
                                    <div class="card-body">
                                        <span class="glyphicon glyphicon-chevron-right float-right"></span>
                                        <img src="${icon}" class="float-left"/>
                                        <h5 class="card-title float-left">${translationsMap[identifier]}</h5>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>
            </c:if>
            <div class="clear"></div>
            <c:if test="${showKanbanOnDashboard && whatToShowOnDashboard != null}">
                <div id="dashboardSearch" style="display: none;">
                    <div class="input-group">
                       <div class="input-group-prepend">
                            <button class="btn btn-outline-secondary bg-primary text-white">
                                <span class="glyphicon glyphicon-search"></span>
                            </button>
                       </div>
                       <input type="text" class="form-control" id="search"
                          placeholder="${translationsMap['basic.dashboard.search.label.focus']}" onChange="filterKanban(this.value)"/>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${whatToShowOnDashboard == '01orders'}">
                        <div id="dashboardKanban">
                            <div class="row">
                                <div class="col">
                                    <div class="card bg-light" style="display: none;">
                                        <div class="card-body p-3">
                                            <c:if test="${enableCreateOrdersOnDashboard}">
                                                <button id="addOrder" class="btn btn-success btn-sm float-right"
                                                    onclick="addOrder()">${translationsMap['basic.dashboard.orders.addNew.label']}</button>
                                            </c:if>
                                            <h6 class="card-title text-uppercase text-truncate py-2">${translationsMap['basic.dashboard.orders.pending.label']}</h6>
                                            <div id="ordersPending" class="items"></div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="card bg-light" style="display: none;">
                                        <div class="card-body p-3">
                                            <h6 class="card-title text-uppercase text-truncate py-2">${translationsMap['basic.dashboard.orders.inProgress.label']}</h6>
                                            <div id="ordersInProgress" class="items"></div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="card bg-light" style="display: none;">
                                        <div class="card-body p-3">
                                            <h6 class="card-title text-uppercase text-truncate py-2">${translationsMap['basic.dashboard.orders.completed.label']}</h6>
                                            <div id="ordersCompleted" class="items"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:when test="${whatToShowOnDashboard == '02operationalTasks'}">
                        <div id="dashboardKanban">
                            <div class="row">
                                <div class="col">
                                    <div class="card bg-light" style="display: none;">
                                        <div class="card-body p-3">
                                            <c:if test="${enableCreateOrdersOnDashboard}">
                                                <button id="addOperationalTask" class="btn btn-success btn-sm float-right"
                                                    onclick="addOperationalTask()">${translationsMap['basic.dashboard.operationalTasks.addNew.label']}</button>
                                             </c:if>

                                            <h6 class="card-title text-uppercase text-truncate py-2">${translationsMap['basic.dashboard.operationalTasks.pending.label']}</h6>
                                            <div id="operationalTasksPending" class="items"></div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="card bg-light" style="display: none;">
                                        <div class="card-body">
                                            <h6 class="card-title text-uppercase text-truncate py-2">${translationsMap['basic.dashboard.operationalTasks.inProgress.label']}</h6>
                                            <div id="operationalTasksInProgress" class="items"></div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="card bg-light" style="display: none;">
                                        <div class="card-body">
                                            <h6 class="card-title text-uppercase text-truncate py-2">${translationsMap['basic.dashboard.operationalTasks.completed.label']}</h6>
                                            <div id="operationalTasksCompleted" class="items">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:when>
                </c:choose>
            </c:if>

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
        <jsp:include page="orderDefinitionWizard.jsp" />
        <jsp:include page="operationalTasksDefinitionWizard.jsp" />
        <script type="text/javascript" src="${pageContext.request.contextPath}/basic/public/js/dashboard.js?ver=${buildNumber}"></script>
    </body>

</html>
