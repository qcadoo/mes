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
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" 
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<link href="/goodFoodGantt/public/css/goodFoodGantt.css?ver=2016_03_18_11_53" rel="stylesheet"
	type="text/css" />

<script src="/goodFoodGantt/public/js/QCD/init.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/Constants.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/ObjectUtils.js?ver=2016_03_18_11_53"></script>

<script type="text/javascript">
	var QCD = QCD || {};
	
	QCD.currentLang = '<c:out value="${locale}" />';
	
	QCD.translate = function (key) {
		return QCD.translations[key] || '[' + key + ']';
	};
	
	QCD.translations = {};
	<c:forEach items="${ganttTranslations}" var="translation">
	QCD.translations['<c:out value="${translation.key}" />'] = '<c:out value="${fn:replace(translation.value, '\\\'','\\\\\\'')}" escapeXml="false" />';
	</c:forEach>
</script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/Configuration.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/Grid/GridE.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/Events.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/DateFormat.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/BindingService.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/treeGrid/RunBoxParser.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/treeGrid/CssUtil.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/dateTime/Week.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/model/ModelUtils.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/model/ChangeoverDataProvider.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/model/DataProvider.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/model/Storage.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/model/DictionaryAccessor.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/BoxTypeResolver.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/dateTime/Util.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/background/BackgroundManager.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/shifts/ShiftUtil.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/shifts/Shift.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/shifts/ShiftsContainer.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/changesManagement/ChangesMarker.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/ChangeoverResolver.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/Zoom.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/ZoomController.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/DateTip.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/MarkersManager.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/BoxTipResolver.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/BoxTextResolver.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/Loader.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/Uploader.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/FormController.js?ver=2016_03_18_11_53"></script>

<script src="/goodFoodGantt/public/js/QCD/scheduler/AbstractTreeGridComponent.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/SourceGridComponent.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/MainGridComponent.js?ver=2016_03_18_11_53"></script>
<script src="/goodFoodGantt/public/js/QCD/scheduler/SchedulerComponent.js?ver=2016_03_18_11_53"></script>

<table style='width: 100%; height: 100%; margin: 10px auto;'>
	<tr>
		<td valign='top'>
			<div id="scheduler" class="schedulerWrapper">
				<div class="schedulerHeaderWrapper">
					<div id="Gantt_schedulerHeader" class="schedulerHeader elementHeader"></div>
				</div>
				<div id="Gantt_schedulerBody" class="schedulerBody">
					<div id="Gantt"
						style="height: 250px; overflow: hidden;">
					</div>
				</div>
			</div>
		</td>
		<td style='padding-left: 16px; width: 285px;' valign='top'>
			<div id="sources" class="schedulerWrapper">
				<div class="schedulerHeaderWrapper">
					<div id="RunSources_schedulerHeader" class="schedulerHeader elementHeader"></div>
				</div>
				<div id="RunSources_schedulerBody" class="schedulerBody">
					<div id='Sources'
						style='width: 285px; height: 250px; overflow: hidden;'>
					</div>
				</div>
			</div>
		</td>
	</tr>
</table>

<script type="text/JavaScript">
	jQuery(document).ready(function() {
		QCD.scheduler.SchedulerComponent.create();
	});
</script>