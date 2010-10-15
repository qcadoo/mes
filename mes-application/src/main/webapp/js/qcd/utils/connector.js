var QCDConnector = {};

QCDConnector.windowName = null;
QCDConnector.mainController = null;

QCDConnector.sendGet = function(type, parameters, responseFunction, errorFunction) {
	if (!QCDConnector.windowName) {
		throw("no window name defined in conector");
	}
	var url = QCDConnector.windowName+"/"+type+".html";
	
	if (parameters) {
		var first = true;
		for (var i in parameters) {
			if (first) {
				url+="?";
			} else {
				url += "&"
			}
			url += i+"="+parameters[i];
			first = false;
		}
	}
	
	$.ajax({
		url: url,
		type: 'GET',
		dataType: 'json',
		contentType: 'application/json; charset=utf-8',
		complete: function(XMLHttpRequest, textStatus) {
			if (XMLHttpRequest.status == 200) {
				var responseText = XMLHttpRequest.responseText.trim(); 
				if (responseText == "sessionExpired") {
					QCDConnector.mainController.onSessionExpired();
					return;
				}
				if (responseText.substring(0, 20) == "<![CDATA[ERROR PAGE:") {
					var message = responseText.substring(20, responseText.search("]]>"));
					QCDConnector.mainController.showMessage("error", "server error: "+message);
					if (errorFunction) {
						errorFunction(message);
					}
					return;
				}
				if (responseFunction) {
					if (responseText != "") {
						var response = JSON.parse(responseText);
						responseFunction(response);
					} else {
						responseFunction(null);
					}
				}
			} else {
				QCDConnector.mainController.showMessage("error", "connection error: "+XMLHttpRequest.statusText);
				if (errorFunction) {
					errorFunction(XMLHttpRequest.statusText);
				}
			}
		}
	});
}

QCDConnector.sendPost = function(type, parameters, responseFunction, errorFunction) {
	if (!QCDConnector.windowName) {
		throw("no window name defined in conector");
	}
	var url = QCDConnector.windowName+"/"+type+".html";
	
	$.ajax({
		url: url,
		type: 'POST',
		data: parameters,
		dataType: 'json',
		contentType: 'application/json; charset=utf-8',
		complete: function(XMLHttpRequest, textStatus) {
			if (XMLHttpRequest.status == 200) {
				var responseText = XMLHttpRequest.responseText.trim(); 
				if (responseText == "sessionExpired") {
					QCDConnector.mainController.onSessionExpired();
					return;
				}
				if (responseText.substring(0, 20) == "<![CDATA[ERROR PAGE:") {
					var message = responseText.substring(20, responseText.search("]]>"));
					QCDConnector.mainController.showMessage("error", "server error: "+message);
					if (errorFunction) {
						errorFunction(message);
					}
					return;
				}
				if (responseFunction) {
					if (responseText != "") {
						var response = JSON.parse(responseText);
						responseFunction(response);
					} else {
						responseFunction(null);
					}
				}
			} else {
				QCDConnector.mainController.showMessage("error", "connection error: "+XMLHttpRequest.statusText);
				if (errorFunction) {
					errorFunction(XMLHttpRequest.statusText);
				}
			}
		}
	});
}