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

<%
String ctx = request.getContextPath();
%>

<script src="/ozgohome/public/js/jquery-1.8.3.min.js"></script>
<script src="/ozgohome/public/js/jquery.jqGrid.min.js"></script>
<script src="/ozgohome/public/js/gridOptions.js"></script>
<script type="text/ecmascript" src="/ozgohome/public/js/grid.locale-en.js"></script>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/dashboard.css?ver=2016_03_18_11_53" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css?ver=2016_03_18_11_53" type="text/css" />
    <link rel="stylesheet" type="text/css" media="screen" href="/ozgohome/public/css/ui.jqgrid.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="/ozgohome/public/css/_jquery-ui-1.8.5.custom.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/window.css?ver=2016_03_18_11_53" type="text/css" />
    <link rel="stylesheet" type="text/css" media="screen" href="/ozgohome/public/css/custom.css" />

	<div id="windowContainer">
		<div id="windowContainerRibbon">
			<div id="q_row3_out">
				<div id="q_menu_row3"></div>
			</div>
			<div id="q_row4_out"></div>
		</div>
		<div id="windowContainerContentBody">
            <div id="gridContainer">
                <table id="grid"></table>
                <div id="jqGridPager"></div>
            </div>
        </div>
    </div>


