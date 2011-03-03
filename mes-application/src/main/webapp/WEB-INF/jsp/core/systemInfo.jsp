<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.3.0

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

<style type="text/css">
	#systemInfoContent {
		margin: 20px;
		font-family:Arial, Helvetica, sans-serif;
	}
	#systemInfoContent .systemInfoItem {
		margin-top: 10px;
		margin-left: 20px;
		font-size: 13px;
	}
	#systemInfoContent .systemInfoItem .systemInfoItemLabel {
		display: inline-block;
		width: 115px;
		color: #616161;
	}
	#systemInfoContent .systemInfoItem .systemInfoItemValue {
		
	}
</style>

<script type="text/JavaScript">
	jQuery(document).ready(function(){
		window.mainController.setWindowHeader("${translationsMap['core.systemInfo.header']}");	
	});
</script>

<div id="systemInfoContent">
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.buildApplicationName.label']}</span>
		<span class="systemInfoItemValue">${buildApplicationName}</span>
	</div>
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.buildApplicationVersion.label']}</span>
		<span class="systemInfoItemValue">${buildApplicationVersion}</span>
	</div>
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.buildNumber.label']}</span>
		<span class="systemInfoItemValue">${buildNumber}</span>
	</div>
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.buildTime.label']}</span>
		<span class="systemInfoItemValue">${buildTime}</span>
	</div>
	<div class="systemInfoItem">
		<span class="systemInfoItemLabel">${translationsMap['core.systemInfo.buildRevision.label']}</span>
		<span class="systemInfoItemValue">${buildRevision}</span>
	</div>
</div>