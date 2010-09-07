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
			success: function(response) {
				if (response.valid) {
					if (shouldRedirect) {
						redirectToCorrespondingPage();
					} else {
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
			},
			error: function(xhr, textStatus, errorThrown){
				alert(textStatus);
			}

		});
	}
	
	redirectToCorrespondingPage = function() {
		mainController.goBack();
	}
	
	validateForm = function() {
		edition = $('#'+parameters.name+"_field_id").attr('value') != '';
		$('#'+parameters.name+'_form .required').filter(function() {
	        return $(this).attr('value') == '';
	    }).each(function(index) {
	    	addFieldErrorMessage($(this).attr('id'), 'core.validation.error.missing');
		});
		if(!edition) {
			$('#'+parameters.name+'_form .required-on-creation').filter(function() {
		        return $(this).attr('value') == '';
		    }).each(function(index) {
		    	addFieldErrorMessage($(this).attr('id'), 'core.validation.error.missing');
			});
		}
		$('#'+parameters.name+'_form .type-integer').filter(function() {
	        return $(this).attr('value') != '' && !$(this).attr('value').match(/^\-?\d+$/);
	    }).each(function(index) {
	    	addFieldErrorMessage($(this).attr('id'), 'form.validate.errors.invalidNumericFormat');
		});
		$('#'+parameters.name+'_form .type-decimal').filter(function() {
	        return $(this).attr('value') != '' && !$(this).attr('value').match(/^\-?\d+(\.\d*)?$/);
	    }).each(function(index) {
	    	addFieldErrorMessage($(this).attr('id'), 'form.validate.errors.invalidNumericFormat');
		});
		$('#'+parameters.name+'_form .type-date').filter(function() {
			return $(this).attr('value') != '' && !$(this).attr('value').match(/^\d{4}-\d{2}-\d{2}$/);
		}).each(function(index) {
			addFieldErrorMessage($(this).attr('id'), 'form.validate.errors.invalidDateFormat');
		});
		$('#'+parameters.name+'_form .type-datetime').filter(function() {
			return $(this).attr('value') != '' && !$(this).attr('value').match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/);
		}).each(function(index) {
			addFieldErrorMessage($(this).attr('id'), 'form.validate.errors.invalidDateTimeFormat');
		});
		$('#'+parameters.name+'_form .confirmable').each(function(index) {
			value = $(this).attr('value') || '';
			valueConfirmation = $('#'+$(this).attr('id')+'_confirmation').attr('value') || '';
			if(value != valueConfirmation) {
				addFieldErrorMessage($(this).attr('id'), 'form.validate.errors.notMatch');
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
			$('#'+parameters.name+'_globalErrors').html('<p>core.validation.error.global</p>');
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
			success: function(response) {
				if(response) {
					refreshForm(response, true);
				}
			},
			error: function(xhr, textStatus, errorThrown){
				alert(textStatus);
			}
		});
	}
	
	refreshForm = function(entity, updateChildren) {
		$('#'+parameters.name+"_field_id").attr('value', entity["id"]);
		for(var i in entity["fields"]) {
			$('#'+parameters.name+"_field_"+i).attr('value', entity["fields"][i]);
		}
		if(entity["id"]) {
			$('#'+parameters.name+'_form .readonly').attr('readonly', 'readonly');
		}
		$('#'+parameters.name+'_form .confirmable').each(function(index) {
			$('#'+$(this).attr('id')+'_confirmation').attr('value','');
		});
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