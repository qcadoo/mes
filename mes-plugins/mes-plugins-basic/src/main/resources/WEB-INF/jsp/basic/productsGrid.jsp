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
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
String ctx = request.getContextPath();
%>

<script src="/basic/public/js/jquery-1.8.3.min.js"></script>
<script src="/basic/public/js/jquery.jqGrid.min.js"></script>
<script src="/basic/public/js/gridOptions.js"></script>
<script type="text/ecmascript" src="/basic/public/js/grid.locale-en.js"></script>

    <link rel="stylesheet" type="text/css" media="screen" href="/basic/public/css/ui.jqgrid.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="/basic/public/css/_jquery-ui-1.8.5.custom.css" />

<table id="grid"></table>
<div id="jqGridPager"></div>
