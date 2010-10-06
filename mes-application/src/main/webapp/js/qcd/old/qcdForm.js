var QCD = QCD || {};
QCD.elements = QCD.elements || {};

QCD.elements.FormElement = function(args, _mainController) {
	
	var parameters;
	
	var mainController = _mainController;
	
	var children = new Array();
	
	var contextEntityId = null;
	
	performSave = function() {
		sendSaveRequest(false);
	}
	performSaveAndExit = function() {
		sendSaveRequest(true);
	}
	performCancel = function() {
		redirectToCorrespondingPage();
	}
	
	sendSaveRequest = function(shouldRedirect) {
		clearErrorMessages();
		hideSaveInfo();
		
		if(!validateForm()) {
			return;
		}
		
		var formData = QCDSerializator.serializeForm($('#'+parameters.name+"_form"));
		var url = parameters.viewName+"/"+parameters.name+"/save.html";
		
		if (contextEntityId && parameters.parentField) {
			formData["fields["+parameters.parentField+"]"] = contextEntityId;
		}
		
		$.ajax({
			url: url,
			type: 'POST',
			data: formData,
			complete: function(XMLHttpRequest, textStatus) {
				if (XMLHttpRequest.status == 200) {
					if (XMLHttpRequest.responseText.trim() == "sessionExpired") {
						mainController.onSessionExpired();
						return;
					}
					var response = JSON.parse(XMLHttpRequest.responseText);
					if (response.valid) {
						if (shouldRedirect) {
							redirectToCorrespondingPage();
						} else {
							showSaveInfo();
							refreshForm(response.entity, true);
						}
					} else {
						for(var i in response.globalErrors) {
							addGlobalErrorMessage(response.globalErrors[i].message);
						}
						for (var field in response.errors) {
							addFieldErrorMessage(parameters.name+'_field_'+field, response.errors[field].message);
						}
					}
				} else {
					alert(XMLHttpRequest.statusText);
				}
			}

		});
	}
	
	redirectToCorrespondingPage = function() {
		mainController.goBack();
	}
	
	showSaveInfo = function() {
		$('#'+parameters.name+'_globalInfo').show().html(mainController.getTranslation('commons.message.save'));
	}
	
	hideSaveInfo = function() {
		$('#'+parameters.name+'_globalInfo').hide();
	}
	
	validateForm = function() {
		edition = $('#'+parameters.name+"_field_id").attr('value') != '';
		$('#'+parameters.name+'_form .required').filter(function() {
	        return $(this).attr('value').trim() == '';
	    }).each(function(index) {
	    	addFieldErrorMessage($(this).attr('id'), mainController.getTranslation('commons.validate.field.error.missing'));
		});
		if(!edition) {
			$('#'+parameters.name+'_form .required-on-create').filter(function() {
		        return $(this).attr('value').trim() == '';
		    }).each(function(index) {
		    	addFieldErrorMessage($(this).attr('id'), mainController.getTranslation('commons.validate.field.error.missing'));
			});
		}
		$('#'+parameters.name+'_form .type-integer').filter(function() {
	        return $(this).attr('value').trim() != '' && !$(this).attr('value').match(/^\-?\d+$/);
	    }).each(function(index) {
	    	addFieldErrorMessage($(this).attr('id'), mainController.getTranslation('commons.validate.field.error.invalidNumericFormat'));
		});
		$('#'+parameters.name+'_form .type-decimal').filter(function() {
	        return $(this).attr('value').trim() != '' && !$(this).attr('value').match(/^\-?\d+(\.\d*)?$/);
	    }).each(function(index) {
	    	addFieldErrorMessage($(this).attr('id'), mainController.getTranslation('commons.validate.field.error.invalidNumericFormat'));
		});
		$('#'+parameters.name+'_form .type-date').filter(function() {
			return $(this).attr('value').trim() != '' && !$(this).attr('value').match(/^\d{4}-\d{2}-\d{2}$/);
		}).each(function(index) {
			addFieldErrorMessage($(this).attr('id'), mainController.getTranslation('commons.validate.field.error.invalidDateFormat'));
		});
		$('#'+parameters.name+'_form .type-datetime').filter(function() {
			return $(this).attr('value').trim() != '' && !$(this).attr('value').match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/);
		}).each(function(index) {
			addFieldErrorMessage($(this).attr('id'), mainController.getTranslation('commons.validate.field.error.invalidDateTimeFormat'));
		});
		$('#'+parameters.name+'_form .confirmable').each(function(index) {
			valueConfirmation = $(this).attr('value').trim() || '';
			value = $('#'+($(this).attr('id').substring(0,$(this).attr('id').length - 13))).attr('value') || '';
			if(value != valueConfirmation) {
				addFieldErrorMessage($(this).attr('id'), mainController.getTranslation('commons.validate.field.error.notMatch'));
			}
		});
		return !hasErrors();
		
	}
	
	hasErrors = function() {
		return $('#'+parameters.name+'_globalErrors').html().trim() != '';
	}
	
	clearErrorMessages = function(error) {
		$('#'+parameters.name+"_form .errorMessage").html('');
	}
	
	addMainErrorMessage = function() {
		if(!hasErrors()) {
			$('#'+parameters.name+'_globalErrors').html('<p>'+mainController.getTranslation('commons.validate.global.error')+'</p>');
		}
	}
	
	addGlobalErrorMessage = function(error) {
		addMainErrorMessage();
		$('#'+parameters.name+'_globalErrors').append('<p>'+error+'</p>');
	}
	
	addFieldErrorMessage = function(fieldId, error) {
		if ($('#'+fieldId+'_error').html().trim() == '') {
			addMainErrorMessage();
			$('#'+fieldId+'_error').html(error);
		}
	}
	
	getEntityAndFillForm = function(entityId) {
		var url = parameters.viewName+"/"+parameters.name+"/entity.html?entityId="+entityId;
		$.ajax({
			url: url,
			type: 'GET',
			complete: function(XMLHttpRequest, textStatus) {
				if (XMLHttpRequest.status == 200) {
					if (XMLHttpRequest.responseText.trim() == "sessionExpired") {
						mainController.onSessionExpired();
						return;
					}
					var response = JSON.parse(XMLHttpRequest.responseText);
					if(response) {
						refreshForm(response, true);
					}
				} else {
					alert(XMLHttpRequest.statusText);
				}
			}
		});
	}
	
	refreshForm = function(entity, updateChildren) {
		$('#'+parameters.name+"_field_id").attr('value', entity["id"]);
		$('#'+parameters.name+'_form .readonly').attr('disabled', true);
		for(var i in entity["fields"]) {
			$('#'+parameters.name+"_field_"+i).attr('value', entity["fields"][i]);
		}
		if(entity["id"]) {
			$('#'+parameters.name+'_form .readonly-on-update').attr('disabled', true);
		}
		$('#'+parameters.name+'_form .confirmable').attr('value','');
		if (updateChildren) {
			for (var i in children) {
				children[i].insertParentId(entity["id"]);
			}
		}
	}
	
	function constructor(args) {
		parameters = args;
		$("#"+parameters.name+"_saveButton").click(performSave);
		$("#"+parameters.name+"_saveCloseButton").click(performSaveAndExit);
		$("#"+parameters.name+"_cancelButton").click(performCancel);
		$('#'+parameters.name+'_form .readonly').attr('disabled', true);
	}
	
	this.getParent = function() {
		return parameters.parent;
	}
	
	this.insertParentId = function(parentId) {
		getEntityAndFillForm(parentId);
	}
	
	this.insertContext = function(_contextEntityId) {
		contextEntityId = _contextEntityId;
	}
	
	this.addChild = function(child) {
		children.push(child);
	}
	
	this.serialize = function() {
		var serializationObject = new Object();
		serializationObject.formData = QCDSerializator.serializeForm($('#'+parameters.name+"_form"));
		return serializationObject;
	}
	
	this.deserialize = function(serializationObject) {
		dataFromSerialization = serializationObject;
		refreshForm(serializationObject.formData, false);
	}
	
	constructor(args);
	
}