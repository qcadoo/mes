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

<script src="/cmmsScheduler/public/js/QCD/init.js?ver=2016_03_18_11_54"></script>

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
    </script>

	 <script src="/cmmsScheduler/public/codebase/grid/js/jquery.jqGrid.min.js"></script>
     <script type="text/ecmascript" src="/cmmsScheduler/public/codebase/grid/js/grid.locale-en.js"></script>
	 <script src="/cmmsScheduler/public/codebase/grid/js/gridOptions.js"></script>


	<script src="/cmmsScheduler/public/codebase/dhtmlxscheduler.js" type="text/javascript" charset="utf-8"></script>

    <script src="/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_limit.js" type="text/javascript" charset="utf-8"></script>

	<script src='/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_tooltip.js' type="text/javascript" charset="utf-8"></script>
	<script src='/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_serialize.js' type="text/javascript" charset="utf-8"></script>
	<script src='/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_readonly.js' type="text/javascript" charset="utf-8"></script>
		<script src='/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_minical.js' type="text/javascript" charset="utf-8"></script>

    <script src="/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_editors.js" type="text/javascript" charset="utf-8"></script>
    	<script src="/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_minical.js" type="text/javascript" charset="utf-8"></script>

	<script src='/cmmsScheduler/public/codebase/ext/dhtmlxscheduler_timeline.js' type="text/javascript" charset="utf-8"></script>


	<c:choose>
		<c:when test="${pageContext.request.locale.language == 'pl'}">
	<script src="/cmmsScheduler/public/codebase/locale/locale_pl.js" type="text/javascript" charset="utf-8"></script>
	</c:when>
	</c:choose>

	<script src="/cmmsScheduler/public/js/chosen.jquery.min.js" type="text/javascript" charset="utf-8"></script>

     <link rel="stylesheet" type="text/css" href="/cmmsScheduler/public/css/chosen.css" />

     <link rel="stylesheet" type="text/css" media="screen" href="/cmmsScheduler/public/codebase/grid/css/ui.jqgrid.css" />
     <link rel="stylesheet" type="text/css" media="screen" href="/cmmsScheduler/public/codebase/grid/css/_jquery-ui-1.8.5.custom.css" />


	<link rel="stylesheet" href="/cmmsScheduler/public/codebase/dhtmlxscheduler.css" type="text/css" media="screen" title="no title" charset="utf-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/dashboard.css?ver=2016_03_18_11_54" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/menu/style.css?ver=2016_03_18_11_54" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/window.css?ver=2016_03_18_11_54" type="text/css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/crud/components/grid.css?ver=2016_03_18_11_54" type="text/css" />
    <link rel="stylesheet" href="/cmmsScheduler/public/css/custom.css" type="text/css">


	<style media="screen">

		/*	html, body {
    			margin: 20px;
    			padding: 0px;
    			height: 500px;
    			overflow: hidden;
    		} */

		.shift_section {
			background-color: #cecece;
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

					.dots_section {
            			background-image: url(/cmmsScheduler/public/codebase/data/imgs/dots.png);
            		}
            		.fat_lines_section {
            			background-image: url(/cmmsScheduler/public/codebase/data/imgs/fat_lines.png);
            		}
            		.medium_lines_section {
            			background-image: url(/cmmsScheduler/public/codebase/data/imgs/medium_lines.png);
            		}
            		.small_lines_section {
            			background-image: url(/cmmsScheduler/public/codebase/data/imgs/small_lines.png);
            		}
		}

		.start_button {
            background-position: -2px 0px;
            width:20px;
		}

		.transfer_button_set {
            border: 1px solid #FFC398;
            background-color: #FDBE05;
            color: #fff;
            background-position: -2px 0px;
		}

		.confirm_button_set {
            border: 1px solid #FFC398;
            background-color: #FDBE05;
           	color: #fff;
            background-position: -2px 0px;
		}

		.event_button {
            background-position: -2px 0px;
            width:20px;
		}

        .dhx_section_time {
            float: left;
            margin-left: 10px;
        }

        .ui-pnotify {
            z-index: 10001;
        }

		.dhx_month_head.dhx_year_event {
            background-color: rgba(254,254,254,1);
        }

        .dhx_now .dhx_month_body, .dhx_now .dhx_month_head {
            background-color: rgba(254,254,254,1);
            font-weight: 800;
        }

	</style>

    <script type="text/javascript" charset="utf-8">
    	var directUp = true;
    	var miniCalendarEvent = false;
    	var firstLoading = true;
		var openFrameworkModal = false;

        function createScheduler() {
            scheduler.locale.labels.timeline_tab = "Timeline";
            scheduler.locale.labels.section_custom="Osoba";
            scheduler.config.details_on_create=false;
            scheduler.config.dblclick_create=false;
            scheduler.config.details_on_dblclick=true;
			scheduler.config.limit_time_select = true;
            scheduler.config.xml_date="%Y-%m-%d %H:%i";
			scheduler.config.limit_drag_out = true;
			scheduler.config.time_step = 15;
			scheduler.config.show_loading = true;
			scheduler.attachEvent("onBeforeDrag", function (id, mode, e){
                if(mode == "create"){
                	return false;
                }

				var ev = scheduler.getEvent(id);
				if(ev._readonly == true){
	               	return false;
				}
                return true;
            });
			scheduler.attachEvent("onClick",function(){return false;})

						var durations = {
            				day: 24 * 60 * 60 * 1000,
            				hour: 60 * 60 * 1000,
            				minute: 60 * 1000
            			};

            			var get_formatted_duration = function(start, end) {
            				var diff = end - start;

            				var days = Math.floor(diff / durations.day);
            				diff -= days * durations.day;
            				var hours = Math.floor(diff / durations.hour);
            				diff -= hours * durations.hour;
            				var minutes = Math.floor(diff / durations.minute);

            				var results = [];
            				if (days) results.push(days + " dni");
            				if (hours) results.push(hours + " h");
            				if (minutes) results.push(minutes + " min");
            				return results.join(", ");
            			};


            			var resize_date_format = scheduler.date.date_to_str(scheduler.config.hour_date);

            			scheduler.templates.event_bar_text = function(start, end, event) {
            				var state = scheduler.getState();
            				if (state.drag_id == event.id) {
            					return resize_date_format(start) + " - " + resize_date_format(end) + " (" + get_formatted_duration(start, end) + ")";
            				}
            				return event.text; // default
            			};
			//===============
			// Tooltip related code
			//===============

			// we want to save "dhx_cal_data" div in a variable to limit look ups
			var scheduler_container = document.getElementById("scheduler_here");
			var scheduler_container_divs = scheduler_container.getElementsByTagName("div");
			var dhx_cal_data = scheduler_container_divs[scheduler_container_divs.length-1];

			scheduler.templates.tooltip_text = function(start,end,event) {
            	return "<b>Zdarzenie: </b> "+event.event+"<br/><b>Rodzaj: </b> "+event.type+"<br/><b>Obiekt: </b> "+event.object+"<br/><b>Plan. rozpoczęcie :</b> "+scheduler.templates.tooltip_date_format(start)+"<br/><b>Plan. zakończenie: </b> "+scheduler.templates.tooltip_date_format(end)+"<br/><b>Status: </b> "+event.status+"<br/><b>Czynność: </b> "+event.action+"<br/><b>Pracownik: </b> "+event.person;
            }


			// while target has parent node and we haven't reached dhx_cal_data
			// we can keep checking if it is timeline section
			scheduler.dhtmlXTooltip.isTooltipTarget = function(target) {
				while (target.parentNode && target != dhx_cal_data) {
					var css = target.className.split(" ")[0];
					// if we are over matrix cell or tooltip itself
					if (css == "dhx_matrix_scell" || css == "dhtmlXTooltip") {
						return { classname: css };
					}
					target = target.parentNode;
				}
				return false;
			};

			scheduler.attachEvent("onMouseMove", function(id, e) {
				var timeline_view = scheduler.matrix[scheduler.getState().mode];

				// if we are over event then we can immediately return
				// or if we are not on timeline view
				if (id || !timeline_view) {
					return;
				}

				// native mouse event
				e = e||window.event;
				var target = e.target||e.srcElement;


				//make a copy of event, will be used in timed call, ie8 comp
				var ev = {'pageX':undefined,
					'pageY':undefined,
					'clientX':undefined,
					'clientY':undefined,
					'target':undefined,
					'srcElement':undefined
				};
				for(var i in ev){
					ev[i] = e[i];
				}

				var tooltip = scheduler.dhtmlXTooltip;
				var tooltipTarget = tooltip.isTooltipTarget(target);
				if (tooltipTarget) {
					if (tooltipTarget.classname == "dhx_matrix_scell") {
						// we are over cell, need to get what cell it is and display tooltip
						var section_id = scheduler.getActionData(e).section;
						var section = timeline_view.y_unit[timeline_view.order[section_id]];

					}
					if (tooltipTarget.classname == "dhtmlXTooltip") {
						dhtmlxTooltip.delay(tooltip.show, tooltip, [ev, tooltip.tooltip.innerHTML]);
					}
				}
			});

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
            var actionStateOptions = [
                 { key: '', label: '- brak -' },
                 { key: '01correct', label: 'Poprawna' },
                 { key: '02incorrect', label: 'Niepoprawna' }
             ];
            sections_event = [
                 { name:"details", height: 245, type:"template", map_to:"details"},
                 { name:"custom", height:23, type:"select", options:scheduler.serverList("sections"), map_to:"section_id" },
                 { name:"time", height:72, type:"calendar_time", map_to:"auto"},
                 { name:"execution", height: 23, type:"template", map_to:"execution"},
                 { name:"state", height: 23, type:"template", map_to:"state"},
             ];
            sections_action = [
                { name:"details", height: 245, type:"template", map_to:"details"},
                { name:"custom", height:23, type:"select", options:scheduler.serverList("sections"), map_to:"section_id" },
                { name:"time", height:72, type:"calendar_time", map_to:"auto"},
                { name:"execution", height: 23, type:"template", map_to:"execution"},
                { name:"state", height: 23, type:"template", map_to:"state"},
                { name:"actionState", height: 33, type:"select", map_to:"actionState", options: actionStateOptions},
                { name:"actionReason", height:70, map_to:"actionReason", type:"textarea" , focus:true},
            ];

            scheduler.config.lightbox.sections = sections_event;

 			//===============
            //Data loading
            //===============

			scheduler.attachEvent("onEventLoading", function(ev){
    			var s_list = scheduler.serverList("sections")

    			for (var i=0; i<s_list.length; i++) {
    				if(ev.section_id == s_list[i].key){
                    	return true;
                    }
    			}
    			return false;
			});

            scheduler.attachEvent("onBeforeViewChange", function(old_mode,old_date,mode,date){

            if (undefined != old_date){
            if(old_date.getTime() > date.getTime())
            {
                directUp = false;
            } else {
            	directUp = true;
            }

            }
            // if(miniCalendarEvent){
              //  directUp = false;
             //}
                return true;
            });

			scheduler.attachEvent("onViewChange", function (new_mode , new_date){
             $.jgrid.gridUnload('grid');
              initGrid();
            });


			scheduler.attachEvent("onXLE", function (){
 				scheduler.updateView();
			});

			scheduler.attachEvent("onXLS", function (){
				getNoWorkingPeriods(onComplete);
				miniCalendarEvent = false;
			});



            scheduler.init('scheduler_here', new Date(Date.now()),"timeline");
            scheduler.setLoadMode("week")
            scheduler.load("/rest/cmms/scheduler/events","json");
            var dp = new dataProcessor("/rest/cmms/scheduler/events");
           	dp.setTransactionMode("REST");
			dp.enableUTFencoding(true);
			dp.attachEvent("onFullSync", function(){
				//all saved
				var view = scheduler.getState().mode;
				scheduler.deleteMarkedTimespan();
				scheduler.clearAll();
                scheduler.setCurrentView(null, view);
           		scheduler.load("/rest/cmms/scheduler/events","json");
           		openFrameworkModal = false;
                 // scheduler.updateView();
			});

            dp.init(scheduler);

            updateSize();
	        $(window).resize(function(){
	            updateSize();
            });
 			scheduler.updateView();
 			firstLoading = false;
        }

 		function onComplete(a) {
 		//      		$.each(a, function(k, v) {
		///
 		//      		scheduler.deleteMarkedTimespan({
        //               start_date: new Date(v.start),
        //                               	end_date:  new Date(v.finish),
         //                              	type:  "dhx_time_block",
         //                             	 	sections: { timeline: v.workerId}
          //          });
            //        			});
              //                   scheduler.updateView();
      		$.each(a, function(k, v) {

				scheduler.addMarkedTimespan({
                	start_date: new Date(v.start),
                	end_date:  new Date(v.finish),
                	css: "shift_section",
               	 	sections: { timeline: v.workerId}
                });
			});
             scheduler.updateView();

  		}

  		function getNoWorkingPeriods(periods) {
      		 var mode = scheduler.getState();
             if (mode.mode == "timeline") {
			 	var start = scheduler.getState().min_date.getTime();
                var end = scheduler.getState().max_date.getTime();
				var jqxhr =  $.getJSON('/rest/cmms/scheduler/workingsPeriod?from='+start+'&to='+end+'&up='+directUp+'&miniCalendarEvent='+miniCalendarEvent+'&firstLoading='+firstLoading);
                jqxhr.done(function(data) {
                	periods(data);
                });

             }
  		}

  		function onCompleteGetWorkers(a) {
  			sections_list = a;
            $.each(a, function(k, v)
            {
                $(".chosen-select").append('<option value=' + v.key + '>' + v.label + '</option>');
            });
            $('.chosen-select').trigger("chosen:updated");
        }

  		function getWorkers(workers) {
      		var jqxhr =  $.getJSON('/rest/cmms/scheduler/workers');
            jqxhr.done(function(data) {
                workers(data);
            });
  		}

        //TODO more elegant way to set proper sizes will be welcome
        function updateSize() {
		    var h = $('#window_windowContent').height() - 20;
		    var w1 = $('#window_windowComponents').width() - 383;
		    var w = $('#scheduler_header').width();
		    var w2 = w1 - 18;
		    $('#scheduler_here').height(h);
		    $('#gridContainer').height(h);
		    $('#scheduler_here').width(w1);
            $('#scheduler_data').width(w2+7);
        }

        function updateSize1() {
		    var h = $('#window_windowContent').height() - 22;
		    var w1 = $('#window_windowComponents').width() - 462;
		    var w = $('#scheduler_header').width();
		    var filterHeight = $('#filterContainer').height();
		    var gridHeight = h - filterHeight;
		    $('#scheduler_here').height(h);
		    $('#gridContainer').height(h);
		    $('#jqGridContainer').height(gridHeight);
		    $('#scheduler_here').width(w1);
            $('#scheduler_data').width(w1);
        }

        function sleep(milliseconds) {
          var start = new Date().getTime();
          for (var i = 0; i < 1e7; i++) {
            if ((new Date().getTime() - start) > milliseconds){
              break;
            }
          }
        }

        function show_minical(){
        		if (scheduler.isCalendarVisible()){
        			scheduler.destroyCalendar();
        			scheduler.setCurrentView(date,"timeline");
				}
        		else
        			scheduler.renderCalendar({
        				position:"dhx_minical_icon",
        				date:scheduler._date,
        				navigation:true,
        				handler:function(date,calendar){
        				    miniCalendarEvent = true;
        					scheduler.setCurrentView(date,"timeline");
        					scheduler.destroyCalendar();

        				}
        			});
        	}
        scheduler.config.buttons_left = ["dhx_save_btn", "dhx_cancel_btn", 'start_button', 'transfer_button', 'confirm_button','refresh_button'];
        scheduler.config.buttons_right = ["event_button", "dhx_delete_btn"];

		scheduler.locale.labels["refresh_button"] = "Anuluj";
         scheduler.locale.labels["start_button"] = "Start";
         scheduler.locale.labels["event_button"] = "Zdarzenie";
         scheduler.locale.labels["transfer_button"] = "Przekaż";
         scheduler.locale.labels["confirm_button"] = "Potwierdź";

        setBlocked = function (blocked) {
            if (!blocked) {
                $("#window_windowComponents .blockUI").css("cursor", "default");
                QCD.components.elements.utils.LoadingIndicator.unblockElement($("#window_windowComponents"));
            }
            //this.setEnabled(!blocked);
            if (blocked) {
                QCD.components.elements.utils.LoadingIndicator.blockElement($("#window_windowComponents"));
            }
        };

        var messagesController = new QCD.MessagesController();

		function showMessage(type, title, content, autoClose, extraLargeClass) {
        	mainController.showMessage({
        		type : type,
        		title : title,
        		content : content,
        		autoClose : autoClose,
        		extraLarge : extraLargeClass
        	});
        }
        
        function getModalDimension(){
            var dimension = {width : 1000, height : 560 };
            var p = 0.8;
            
            var modalHeight = parseInt($(window).height()*p);
            var modelWidth = parseInt($(window).width()*p);
            
            if(modalHeight > dimension.height){
                dimension.height = modalHeight;
            }
            
            if(modelWidth > dimension.width){
                dimension.width = modelWidth;
            }
            
            return dimension;
        }

         scheduler.attachEvent("onLightboxButton", function(button_id, node, e){
             var ev = scheduler.getEvent(scheduler.getState().lightbox_id);
             if(button_id == "start_button"){
                setBlocked(true);

                QCDConnector.sendPost({}, function (response) {
                    var i = 0,
                        messagesLen = 0,
                        message = null;

                    if(response.status.toLowerCase() === "ok") {
                        showMessage('success', QCD.translate('qcadooView.notification.success'), QCD.translate('qcadooView.scheduler.ui.msg.changeStatusSuccess'), true, false);

                        ev.status = 'W realizacji';
                        ev.statusCode = '04inRealization';
                        scheduler.updateEvent(ev.id);
                    } else if(response.status.toLowerCase() === "error") {
                    	showMessage('failure', QCD.translate('qcadooView.notification.failure'), QCD.translate(response.message), false, true);
                    }
                    else {
                        showMessage('failure', QCD.translate('qcadooView.notification.failure'), QCD.translate('qcadooView.scheduler.ui.msg.changeStatusFail'), true, false);
                    }

                    scheduler.endLightbox( true, $('.dhx_cal_light_wide')[0]);
                    setBlocked(false);

                }, function () {
                    setBlocked(false);
                },
                "../../../rest/cmms/scheduler/changeStatus.html?event_id="+ev.event_id+"&operation=start&version="+ev.entityVersion);

             } else if(button_id == "event_button"){
             	openFrameworkModal = true;
				scheduler.hide_lightbox();
				scheduler.resetLightbox();
				scheduler.showLightbox(ev.id);
                mainController.openModal('body', 'cmmsMachineParts/plannedEventDetails.html?context={"form.id":'+ev.event_id+'}&iframe=true', null, null, null, getModalDimension());
             } else if(button_id == "transfer_button") {
 				setBlocked(true);
				var obj = {
					id: ev.id,
    				action_id: ev.action_id,
    				confirmed: ev.confirmed,
    				event_id: ev.event_id,
    				section_id: scheduler.formSection('custom').getValue(),
					start_date: scheduler.formSection('time').getValue().start_date.getTime(),
					end_date: scheduler.formSection('time').getValue().end_date.getTime()
					};
                QCDConnector.sendPost(JSON.stringify(obj), function (response) {
                    var i = 0,
                        messagesLen = 0,
                        message = null;

                    if(response.status.toLowerCase() === "ok") {
                        showMessage('success', QCD.translate('qcadooView.notification.success'), QCD.translate('qcadooView.scheduler.ui.msg.transferEventSuccess'), true, false);
                        scheduler.updateEvent(ev.id);
                    } else {
                        showMessage('failure', QCD.translate('qcadooView.notification.failure'), QCD.translate('qcadooView.scheduler.ui.msg.transferEventFail'), true, false);
                    }

                    scheduler.endLightbox( true, $('.dhx_cal_light_wide')[0]);
                    setBlocked(false);

                }, function () {
                    setBlocked(false);
                },
                "../../../rest/cmms/scheduler/transfer.html");

             } else if(button_id == "confirm_button") {
 				setBlocked(true);
				var obj = {
					id: ev.id,
    				action_id: ev.action_id,
    				confirmed: ev.confirmed,
    				event_id: ev.event_id,
    				section_id: scheduler.formSection('custom').getValue(),
					start_date: scheduler.formSection('time').getValue().start_date.getTime(),
					end_date: scheduler.formSection('time').getValue().end_date.getTime()
					};
                QCDConnector.sendPost(JSON.stringify(obj), function (response) {
                    var i = 0,
                        messagesLen = 0,
                        message = null;

                    if(response.status.toLowerCase() === "ok") {
                        showMessage('success', QCD.translate('qcadooView.notification.success'), QCD.translate('qcadooView.scheduler.ui.msg.confirmEventSuccess'), true, false);
                        scheduler.updateEvent(ev.id);
                    } else {
                        showMessage('failure', QCD.translate('qcadooView.notification.failure'), QCD.translate('qcadooView.scheduler.ui.msg.confirmEventFail'), true, false);
                    }

                    scheduler.endLightbox( true, $('.dhx_cal_light_wide')[0]);
                    setBlocked(false);

                }, function () {
                    setBlocked(false);
                },
                "../../../rest/cmms/scheduler/confirm.html");

             } else if(button_id == "refresh_button") {
             	if(openFrameworkModal){
             		scheduler.hide_lightbox();
                	scheduler.resetLightbox();
             		var view = scheduler.getState().mode;
                	scheduler.deleteMarkedTimespan();
                	scheduler.clearAll();
                     scheduler.setCurrentView(null, view);
                     scheduler.load("/rest/cmms/scheduler/events","json");
                     openFrameworkModal = false;

             	} else {
             		scheduler.endLightbox( true, $('.dhx_cal_light_wide')[0]);
             	}
             }


         });



         scheduler.locale.labels.section_actionState = 'Status czynności';
         scheduler.locale.labels.section_actionReason = 'Uzasadnienie';
         scheduler.locale.labels.section_details = 'Szczegóły';
         scheduler.locale.labels.section_execution = 'Czas trwania';
         scheduler.locale.labels.section_state = 'Status';

        scheduler.date.date_to_str(scheduler.config.hour_date);

        function formatDateDiff(diff){
            var seconds = diff/1000;
            var minuts = parseInt(seconds/60);
            var hours = parseInt(minuts/60);
            var days = parseInt(hours/24);

            var output = "";
            if(days > 0){
                output = days+' ';
            }
            output += addLeadingZeros(hours%24)+':';
            output += addLeadingZeros(minuts%60)+':';
            output += addLeadingZeros(seconds%60);

            return output;
        }

        function addLeadingZeros(number){
            number = ''+number;
            while(number.length < 2){
                number = '0'+number;
            }

            return number;
        }

		function getTimeZone() {
    		var offset = new Date().getTimezoneOffset(),
       			 o = Math.abs(offset);
    		return (offset < 0 ? "+" : "-") + ("00" + Math.floor(o / 60)).slice(-2) + ":" + ("00" + (o % 60)).slice(-2);
		}
        scheduler.attachEvent("onEventSave",function(id,ev){
            var persistedEvent = scheduler.getEvent(id);

            if(persistedEvent.action) {
                if(ev.actionState === '02incorrect' && !ev.actionReason){
                    showMessage('failure', QCD.translate('qcadooView.notification.failure'), QCD.translate('qcadooView.scheduler.ui.msg.reasonRequired'), true, false);
                    return false;
                }
            }

            return true;
        })

        scheduler.attachEvent("onTemplatesReady", function(){

            scheduler.attachEvent("onBeforeLightbox", function(id) {
                var ev = scheduler.getEvent(id);

                if(ev.action){
                    scheduler.config.lightbox.sections = sections_action;
                    
                } else {
                    scheduler.config.lightbox.sections = sections_event;
                }
        
                if(ev._readonly == true){
                    scheduler.config.buttons_left = ["dhx_cancel_btn"];
                    scheduler.config.buttons_right = ["event_button"];
                    
                } else {
                    if(ev.statusCode == '03planned'){

                   	 	if(ev.confirmed){
                        	scheduler.config.buttons_left = ["dhx_save_btn", "dhx_cancel_btn", 'start_button'];
                        } else {
                        	scheduler.config.buttons_left = ["dhx_save_btn", "dhx_cancel_btn", 'start_button'];
                        }

                        
                    } else {
                        scheduler.config.buttons_left = ["dhx_save_btn", "dhx_cancel_btn"];
                    }

                    if(ev.event_id){
                    	if(ev.confirmed){
                    		scheduler.config.buttons_right = ["event_button", "dhx_delete_btn", 'transfer_button'];
                    	} else {
                    		scheduler.config.buttons_right = ["event_button", "dhx_delete_btn", 'confirm_button'];
                    	}
                    } else {
                    	scheduler.config.buttons_right = ["dhx_delete_btn"];
                    }
                }
                
				if(openFrameworkModal){
					        scheduler.config.buttons_right = [];
                    		scheduler.config.buttons_left = ["refresh_button"];

				}
                
                scheduler.resetLightbox();

                ev.details = '<b>Zdarzenie: </b>'+ ev.event+'<br />';
                ev.details += '<b>Rodzaj: </b>'+ ev.type+'<br />';
                ev.details += '<b>Obiekt: </b>'+ ev.object+'<br />';
                if(ev.desc){
                    ev.details += '<b>Opis zdarzenia: </b><textarea style="resize: none; height: 50px" rows="2" readonly="true" disabled="disabled" cols="50">'+ ev.desc  +' </textarea><br />';

                }
                if(ev.action){
                    ev.details += '<b>Czynność: </b>'+ ev.action +'<br />';
                    ev.details += '<b>Opis czynności: </b><textarea style="resize: none; height: 50px" rows="2" readonly="true" disabled="disabled" cols="50">'+ ev.actionDesc  +' </textarea><br />';
                }

                var executionTime = formatDateDiff(ev.end_date.getTime() - ev.start_date.getTime());

                ev.execution = '<input type="text" value="'+executionTime+'" readonly="true" disabled="disabled" />';

                ev.state = '<input type="text" value="'+ev.status+'" readonly="true" disabled="disabled" />';

                return true;
            });
        });

    </script>

    <div id="parentContainer">
        <div id="scheduler_here" class="dhx_cal_container"">
            <div class="dhx_cal_navline">
                <div class="dhx_cal_prev_button" style="right: 64px;">&nbsp;</div>
                <div class="dhx_cal_next_button" style="right: 17px;">&nbsp;</div>
                <div class="dhx_cal_today_button"></div>
                <div class="dhx_cal_date"></div>
                <div class="dhx_minical_icon" id="dhx_minical_icon" onclick="show_minical()">&nbsp;</div>
                <div class="dhx_cal_tab" name="day_tab" style="right:218px;"></div>
                <div class="dhx_cal_tab" name="week_tab" style="right:154px;"></div>
                <div class="dhx_cal_tab" name="timeline_tab" style="right:294px;"></div>
                <div class="dhx_cal_tab" name="month_tab" style="right:90px;"></div>
            </div>
            <div class="dhx_cal_header" id="scheduler_header">
            </div>
            <div class="dhx_cal_data" id="scheduler_data">
            </div>
        </div>

        <div id="gridContainer">
            <div id="filterContainer">
                  <em> Filtr pracowników</em>
                  <input type="hidden" name="hidden_scheduler" value="hidden_scheduler">
                  <select id="workers-filter" data-placeholder="Wybierz pracownika..."  class="chosen-select" multiple style="width:427px;">
                        <option value=""></option>
                  </select>
                  <input type="button" id="filter-workers" value="Filtruj" />

             </div>
             <em>Bufor zdarzeń</em>
             <div id="jqGridContainer" class="gridContainer">
                <table id="grid"></table>
                <div id="jqGridPager"></div>
            </div>
        </div>
    </div>

<script type="text/JavaScript">
	var sections_list;

	jQuery(document).ready(function() {

          $(".chosen-select").chosen({
          	allow_single_deselect:true,
            disable_search_threshold: 10,
            no_results_text: "Uuups, nie znaleziono pracownika! ",
            width: "100%"
          });

        getWorkers(onCompleteGetWorkers);
        $("#workers-filter").trigger("chosen:updated");

		createScheduler();
		$(".chosen-select").chosen().change(function(e, params){

            var h = $('#window_windowContent').height() - 20;
            var filterHeight = $('#filterContainer').height();
            var gridHeight = h - filterHeight - 158;
            if(gridHeight < 70){
                gridHeight = 70;
                $('#grid').jqGrid('setGridHeight',gridHeight);
            }
            else {
                $('#grid').jqGrid('setGridHeight',gridHeight);
            }
        });

		jQuery("#filter-workers").on("click", function() {
                var values = $(".chosen-select").chosen().val();
                if(values != null){
                	var sectionsList = sections_list;
                    var filterList = [];
                         for (var i=0; i<values.length; i++) {
                            var display = sectionsList.slice();
                            for (var k=0; k < display.length; k++) {
                                if (values[i] == display[k].key) {
                                   filterList.push(display[k]);
                                }
                            }
                         }
                                        //update timeline reloads options and redraws the view
                         scheduler.updateCollection("sections", filterList);
                         var view = scheduler.getState().mode;
                         scheduler.deleteMarkedTimespan();
                         scheduler.clearAll();
                         scheduler.setCurrentView(null, view);
                         scheduler.load("/rest/cmms/scheduler/events","json");
                } else {
                	scheduler.updateCollection("sections", sections_list);
                	var view = scheduler.getState().mode;
                    scheduler.deleteMarkedTimespan();
                    scheduler.clearAll();
                    scheduler.setCurrentView(null, view);
                    scheduler.load("/rest/cmms/scheduler/events","json");
                }

        });
	});

</script>