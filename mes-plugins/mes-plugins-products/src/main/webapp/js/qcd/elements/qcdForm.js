var QCD = QCD || {};
QCD.elements = QCD.elements || {};

QCD.elements.FormElement = function(args) {
	
	var parameters;
	
	var children = new Array();
	
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
		var formData = $('#'+parameters.name+"_form").serializeObject();
		var url = parameters.viewName+"/"+parameters.name+"/save.html";
		
		$("."+parameters.name+"_validatorGlobalMessage").html('');
		$("."+parameters.name+"_fieldValidatorMessage").html('');
		
		$.ajax({
			url: url,
			type: 'POST',
			data: formData,
			success: function(response) {
				if (response.valid) {
					if (shouldRedirect) {
						redirectToCorrespondingPage();
					}
				} else {
					$("."+parameters.name+"_validatorGlobalMessage").html(response.globalMessage);
					for (var field in response.fieldMessages) {
						$("#"+parameters.name+"_"+field+"_validateMessage").html(response.fieldMessages[field]);
					}
				}
			},
			error: function(xhr, textStatus, errorThrown){
				alert(textStatus);
			}

		});
	}
	
	redirectToCorrespondingPage = function() {
		window.location = parameters.correspondingView + ".html";
	}
	
	getEntityAndFillForm = function(entityId) {
		var url = parameters.viewName+"/"+parameters.name+"/entity.html?entityId="+entityId;
		$.ajax({
			url: url,
			type: 'GET',
			success: function(response) {
				if(response) {
					refreshForm(response);
				}
			},
			error: function(xhr, textStatus, errorThrown){
				alert(textStatus);
			}
		});
	}
	
	refreshForm = function(entity) {
		$('#'+parameters.name+"_field_id").value = entity["id"];
		for(var i in entity["fields"]) {
			$('#'+parameters.name+"_field_"+i).attr('value', entity["fields"][i]);
		}
		for (var i in children) {
			children[i].insertParentId(entity["id"]);
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
	
	this.addChild = function(child) {
		children.push(child);
	}
	
	constructor(args);
	
}