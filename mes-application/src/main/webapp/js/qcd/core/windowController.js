var QCD = QCD || {};

QCD.WindowController = function() {
	
	var iframe = null;
	
	var loadingIndicator;
	
	var statesStack = new Array();
	
	var serializationObjectToInsert = null;
	
	var currentPage = null;
	
	var messagesController = new QCD.MessagesController();
	
	function constructor() {
		QCD.info("init main page");
		iframe = $("#mainPageIframe");
		loadingIndicator = $("#loadingIndicator");
		loadingIndicator.hide();
		iframe.load(function() {
			onIframeLoad(this);
		});

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
	
	this.onSessionExpired = function(serializationObject) {
		serializationObjectToInsert = serializationObject;
		performGoToPage("login.html");
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
	
	constructor();
	
}