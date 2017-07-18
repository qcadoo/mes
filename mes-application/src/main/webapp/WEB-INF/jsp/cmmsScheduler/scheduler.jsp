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
<!doctype html>
<head>
	<script src="/cmmsScheduler/public/codebase/dhtmlxscheduler.js" type="text/javascript" charset="utf-8"></script>
	<script src='/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_timeline.js' type="text/javascript" charset="utf-8"></script>
	<script src="/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_limit.js" type="text/javascript" charset="utf-8"></script>

	<c:choose>
		<c:when test="${plLocale}">
			<script src="/cmmsScheduler/public/codebase/locale/locale_pl.js" type="text/javascript" charset="utf-8"></script>
		</c:when>
	</c:choose>
	<link rel="stylesheet" href="/cmmsScheduler/public/codebase/dhtmlxscheduler.css" type="text/css" media="screen" title="no title" charset="utf-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/dashboard.css?ver=2016_03_18_11_54" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css?ver=2016_03_18_11_54" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/window.css?ver=2016_03_18_11_54" type="text/css" />

	<style media="screen">
		html, body {
			margin: 20px;
			padding: 0px;
			height: 100%;
			overflow: hidden;
		}

		.red_section {
			background-color: red;
			opacity: 0.25;
			filter: alpha(opacity = 25);
		}

		.yellow_section {
			background-color: #ffa749;
			opacity: 0.25;
			filter: alpha(opacity = 25);
		}

		.green_section {
			background-color: #12be00;
			opacity: 0.25;
			filter: alpha(opacity = 25);
		}

		.blue_section {
			background-color: #2babf5;
			opacity: 0.27;
			filter: alpha(opacity = 27);
		}

		.pink_section {
			background-color: #6a36a5;
			opacity: 0.30;
			filter: alpha(opacity = 30);
		}

		.dark_blue_section {
			background-color: #2ca5a9;
			opacity: 0.40;
			filter: alpha(opacity = 40);
		}

		.dhx_cal_lsection {
			font-size: 12px;
		}
	</style>

    <script type="text/javascript" charset="utf-8">
        function init() {

            scheduler.locale.labels.timeline_tab = "Timeline";
            scheduler.locale.labels.section_custom="Section";
            scheduler.config.details_on_create=true;
            scheduler.config.details_on_dblclick=true;
			scheduler.config.limit_time_select = true;

            scheduler.config.xml_date="%Y-%m-%d %H:%i";

            //===============
            //Configuration
            //===============


            scheduler.createTimelineView({
                name:	"timeline",
               	x_unit:	"hour",
               	x_date:	"%H:%i",
               	x_step:	2,
               	x_size: 12,
               	x_length:12,
               	x_start:3,
                y_unit: scheduler.serverList("sections"),
                y_property:	"section_id",
                render:"bar",
                second_scale:{
                    x_unit: "day", // unit which should be used for second scale
                    x_date: "%F %d" // date format which should be used for second scale, "July 01"
                }
            });

            dhtmlxAjax.get("/rest/cmms/scheduler/workers", function(resp){
                var sections = JSON.parse(resp.xmlDoc.responseText);
                scheduler.updateCollection("sections", sections);
            });

            //===============
            //Data loading
            //===============
            scheduler.config.lightbox.sections=[
                {name:"description", height:130, map_to:"text", type:"textarea" , focus:true},
                {name:"custom", height:23, type:"select", options:scheduler.serverList("sections"), map_to:"section_id" },
                {name:"time", height:72, type:"time", map_to:"auto"}
            ];



            scheduler.init('scheduler_here', new Date(Date.now() - 28800000),"timeline");
            scheduler.setLoadMode("week")
            scheduler.load("/rest/cmms/scheduler/events","json");
            var dp = new dataProcessor("/rest/cmms/scheduler/events");
            dp.setTransactionMode("REST");

            dp.init(scheduler);


             scheduler.addMarkedTimespan({
                        				start_date: new Date(2015, 15, 7, 22),
                        				end_date: new Date(2015, 15, 8, 6),
                        				css: "red_section",
                        				sections: {
                        					timeline: [1, 3]
                        				}
                        			});

        }


    </script>
</head>
<body onload="init();">
	<div id="scheduler_here" class="dhx_cal_container" style='width:100%; height:90%;'>
		<div class="dhx_cal_navline">
			<div class="dhx_cal_prev_button">&nbsp;</div>
			<div class="dhx_cal_next_button">&nbsp;</div>
			<div class="dhx_cal_today_button"></div>
			<div class="dhx_cal_date"></div>
			<div class="dhx_cal_tab" name="day_tab" style="right:204px;"></div>
			<div class="dhx_cal_tab" name="week_tab" style="right:140px;"></div>
			<div class="dhx_cal_tab" name="timeline_tab" style="right:280px;"></div>
			<div class="dhx_cal_tab" name="month_tab" style="right:76px;"></div>
		</div>
		<div class="dhx_cal_header">
		</div>
		<div class="dhx_cal_data">
		</div>
	</div>
</body>