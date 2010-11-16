/*
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
	}
	
	this.addMessage = function(type, content) {
		messagesController.addMessage(type, content);
	}
	
	this.performLogout = function() {
		QCD.info("logout");
		window.location = "j_spring_security_logout";
	}
	
	this.goToPage = function(url, serializationObject) {
		var stateObject = {
			url: iframe.attr('src'),
			serializationObject: serializationObject
		};
		statesStack.push(stateObject);
		currentPage = "page/"+url;
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
		if (iframe[0].contentWindow.init) {
			iframe[0].contentWindow.init(serializationObjectToInsert);
			serializationObjectToInsert = null;
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