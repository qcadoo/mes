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
				var responseText = $.trim(XMLHttpRequest.responseText); 
				if (responseText == "sessionExpired") {
					QCDConnector.mainController.onSessionExpired();
					return;
				}
				if (responseText.substring(0, 20) == "<![CDATA[ERROR PAGE:") {
					var messageBody = responseText.substring(20, responseText.search("]]>"));
					QCDConnector.showErrorMessage(messageBody,errorFunction);
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
				QCDConnector.showErrorMessage("connection error: "+XMLHttpRequest.statusText);
				if (errorFunction) {
					errorFunction(XMLHttpRequest.statusText);
				}
			}
		}
	});
}

QCDConnector.sendPost = function(parameters, responseFunction, errorFunction) {
	if (!QCDConnector.windowName) {
		throw("no window name defined in conector");
	}
	var url = QCDConnector.windowName+".html";
	
	$.ajax({
		url: url,
		type: 'POST',
		data: parameters,
		dataType: 'json',
		contentType: 'application/json; charset=utf-8',
		complete: function(XMLHttpRequest, textStatus) {
			if (XMLHttpRequest.status == 200) {
				var responseText = $.trim(XMLHttpRequest.responseText); 
				if (responseText == "sessionExpired") {
					QCDConnector.mainController.onSessionExpired();
					return;
				}
				//alert(responseText);
				if (responseText.substring(0, 20) == "<![CDATA[ERROR PAGE:") {
					var messageBody = responseText.substring(20, responseText.search("]]>"));
					QCDConnector.showErrorMessage(messageBody,errorFunction);
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
				QCDConnector.showErrorMessage("connection error: "+XMLHttpRequest.statusText);
				if (errorFunction) {
					errorFunction(XMLHttpRequest.statusText);
				}
			}
		}
	});
}

QCDConnector.showErrorMessage = function(messageBody, errorFunction) {
	var messageBodyParts = messageBody.split("##");
	var message = {};
	if (messageBodyParts.length == 2) {
		message.title = messageBodyParts[0];
		message.content = messageBodyParts[1];
	} else {
		message.content = messageBody;
	}
	message.type = "failure";
	QCDConnector.mainController.showMessage(message);
	if (errorFunction) {
		errorFunction(message);
	}
}
