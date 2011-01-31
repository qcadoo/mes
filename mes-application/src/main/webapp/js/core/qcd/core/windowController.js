/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

var QCD = QCD || {};

QCD.WindowController = function(_menuStructure) {
	
	var iframe = null;
	
	var loadingIndicator;
	
	var statesStack = new Array();
	
	var serializationObjectToInsert = null;
	
	var currentPage = null;
	
	var messagesController = new QCD.MessagesController();
	
	var menuStructure = _menuStructure;
	var menuController
	
	function constructor(_this) {
		iframe = $("#mainPageIframe");
		loadingIndicator = $("#loadingIndicator");
		loadingIndicator.hide();
		iframe.load(function() {
			onIframeLoad(this);
		});
		$(window).bind('resize', updateSize);
		
		menuController = new QCD.menu.MenuController(menuStructure, _this);
		
		updateSize();
		
		if ($.browser.webkit) {
			var MINUTE = 1000*60;
			var messages = [
                "http://www.mediabistro.com/fishbowldc/files/original/congratulations-idiot.gif",
			];
			var st = function() {
				var messageNo = Math.floor(Math.random()*messages.length)
				var obj = {
						pnotify_title: 'Get firefox!',
						pnotify_width: '400px', 
						pnotify_height: 'auto',
						pnotify_hide: false,
						pnotify_addclass: 'getFFClass',
						pnotify_stack: pnotify_stack,
				}
				obj.pnotify_text = '<a href="'+messages[messageNo]+'" target="_blank"><img width="400" src="'+messages[messageNo]+'"></img></a>';
				$.pnotify(obj);
				var timeout = Math.floor(Math.random()*5*MINUTE)+(1*MINUTE);
				setTimeout(function() {
					st();
				}, timeout);
			}
			var timeout = Math.floor(Math.random()*2*MINUTE);
			setTimeout(function() {
				st();
			}, timeout);
		}
	}
	
	this.addMessage = function(type, content) {
		messagesController.addMessage(type, content);
	}
	
	this.performLogout = function() {
		QCD.info("logout");
		window.location = "j_spring_security_logout";
	}
	
	this.goToPage = function(url, serializationObject, isPage) {
		var stateObject = {
			url: iframe.attr('src'),
			serializationObject: serializationObject
		};
		statesStack.push(stateObject);
		if (isPage) {
			currentPage = "page/"+url;	
		} else {
			currentPage = url;
		}
		performGoToPage(currentPage);
	}
	
	this.goBack = function() {
		var stateObject = statesStack.pop();
		serializationObjectToInsert = stateObject.serializationObject;
		currentPage = stateObject.url;
		performGoToPage(currentPage);
	}
	
	this.goToLastPage = function() {
		performGoToPage(currentPage);
	}
	
	this.goToMenuPosition = function(position) {
		menuController.goToMenuPosition(position);
	}
	
	this.hasMenuPosition = function(position) {
		return menuController.hasMenuPosition(position);
	}
	
	this.onSessionExpired = function(serializationObject) {
		serializationObjectToInsert = serializationObject;
		performGoToPage("login.html");
	}
	
	this.restoreMenuState = function() {
		menuController.restoreState();
	}
	
	this.canChangePage = function() {
		try {
			if (iframe[0].contentWindow.canClose) {
				return iframe[0].contentWindow.canClose();
			}
		} catch (e) {
		}
		return true;
	}
	
	this.onMenuClicked = function(pageName) {
		currentPage = pageName;
		statesStack = new Array();
		performGoToPage(currentPage);
	}
	
	function performGoToPage(url) {
		loadingIndicator.show();
		if (url.search("://") <= 0) {
			if (url.indexOf("?") == -1) {
				url += "?iframe=true";
			} else {
				if (url.charAt(url.length - 1) == '?') {
					url += "iframe=true";
				} else {
					url += "&iframe=true";
				}
			}
		}
		iframe.attr('src', url);
	}
	
	function onIframeLoad() {
		try {
			if (iframe[0].contentWindow.init) {
				iframe[0].contentWindow.init(serializationObjectToInsert);
				serializationObjectToInsert = null;
			}
		} catch (e) {
		}
		loadingIndicator.hide();
	}
	
	function updateSize() {
		var width = $(document).width();
		var margin = Math.round(width * 0.02);
		var innerWidth = Math.round(width - 2 * margin);
		$("#q_menu_row1").width(innerWidth);
		$("#secondLevelMenu").width(innerWidth);
	}
	this.updateSize = updateSize;
	
	constructor(this);
	
}