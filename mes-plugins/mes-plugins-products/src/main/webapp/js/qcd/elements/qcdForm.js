var QCD = QCD || {};
QCD.elements = QCD.elements || {};

QCD.elements.FormElement = function(args) {
	
	var parameters;
	
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
	
	function constructor(args) {
		parameters = args;
		$("#"+parameters.name+"_saveButton").click(performSave);
		$("#"+parameters.name+"_saveCloseButton").click(performSaveAndExit);
		$("#"+parameters.name+"_cancelButton").click(performCancel);
	}
	
	this.insertParentId = function(parentId) {
		
	}
	
	constructor(args);
	
}