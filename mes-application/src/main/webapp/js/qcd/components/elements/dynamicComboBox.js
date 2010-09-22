var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};

QCD.components.elements.DynamicComboBox = function(_element, _mainController) {
	$.extend(this, new QCD.components.Component(_element, _mainController));

	var mainController = _mainController;
	
	var element = _element;
	
	//var input = $("#"+element.attr('id')+"_input");
	
	this.insterData = function(data) {
		//input.val(data);
		QCD.info("QCD.components.elements.DynamicComboBox.insterData()");
	}
	
	this.getComponentValue = function() {
		//if (input.val() && input.val().trim() != "") {
			//return input.val();
		//}
		//return null;
	}
	
	this.setComponentValue = function(value) {
		//input.val(value);
	}
	
	this.setComponentEnabled = function(isEnabled) {
		//if (isEnabled) {
			//input.removeAttr('disabled');
		//} else {
			//input.attr('disabled', 'true');
		//}
	}
	
	this.setComponentLoading = function(isLoadingVisible) {
//		if (isLoadingVisible) {
//			input.val("loading");
//		} else {
//			if (input.val() == "loading")
//				input.val("");
//		}
	}
}