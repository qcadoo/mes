var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.EntityComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));

	var mainController = _mainController;
	
	var element = _element;
	var options = this.options;
	var elementPath = this.elementPath;
	
	var select = $("#"+element.attr('id')+"_select");
	
	var messagesSpan = $("#"+element.attr('id')+"_messagesSpan");
	
	function constructor(_this) {
		select.change(onChange);
	}
	
	function onChange() {
		if (options.listeners.length > 0) {
			mainController.getUpdate(elementPath, select.val(), options.listeners);
		}
	}
	
	this.getComponentValue = function() {
		var selectedVal = select.val();
		if (!selectedVal || selectedVal.trim() == "") {
			selectedVal = null;
		}
		var value = {
			selectedValue: selectedVal
		};
		return value;
	}
	
	this.setComponentValue = function(value) {
		var previousSelectedVal = select.val();
		if (value.values != null) {
			select.children().remove();
			select.append("<option value=''></option>");
			for (var i in value.values) {
				var val = value.values[i];
				select.append("<option value='"+i+"'>"+val+"</option>");
			}
		}
		if (value.selectedValue != null) {
			select.val(value.selectedValue);
		} else if (value.emptySelected) {
			select.val('');
		} else {
			select.val(previousSelectedVal);
		}
	}
	
	this.setComponentEnabled = function(isEnabled) {
		if (isEnabled) {
			select.removeAttr('disabled');
		} else {
			select.attr('disabled', 'true');
		}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {

	}
	
	this.setMessages = function(messages) {
		var message = "";
		for (var i in messages.error) {
			if (message != "") {
				message += ", ";
			}
			message += messages.error[i];
		}
		for (var i in messages.info) {
			if (message != "") {
				message += ", ";
			}
			message += messages.info[i];
		}
		for (var i in messages.success) {
			if (message != "") {
				message += ", ";
			}
			message += messages.success[i];
		}
		messagesSpan.html(message);
	}
	
	constructor(this);
}