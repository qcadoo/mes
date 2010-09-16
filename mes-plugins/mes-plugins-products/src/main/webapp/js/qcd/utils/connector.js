var QCDConnector = {};

QCDConnector.windowName = null;

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
		//data: parameters,
		dataType: 'json',
		contentType: 'application/json; charset=utf-8',
		complete: function(XMLHttpRequest, textStatus) {
			if (XMLHttpRequest.status == 200) {
				if (XMLHttpRequest.responseText.trim() == "sessionExpired") {
					mainController.onSessionExpired();
					return;
				}
				if (responseFunction) {
					if (XMLHttpRequest.responseText && XMLHttpRequest.responseText.trim != "") {
						var response = JSON.parse(XMLHttpRequest.responseText);
						responseFunction(response);
					} else {
						responseFunction(null);
					}
				}
			} else {
				alert(XMLHttpRequest.statusText);
				if (errorFunction) {
					errorFunction(XMLHttpRequest.statusText);
				}
			}
		}
	});
}