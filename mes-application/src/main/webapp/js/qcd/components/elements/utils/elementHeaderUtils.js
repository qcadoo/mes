var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.utils = QCD.components.elements.utils || {};

QCD.components.elements.utils.HeaderUtils = {};

QCD.components.elements.utils.HeaderUtils.createHeaderButton = function(label, clickAction, icon) {
	var elementIcon = (icon && $.trim(icon) != "") ? $.trim(icon) : null;
	
	var itemElementLabel = $('<div>');
	itemElementLabel.html(label);
	
	var itemElementSpan = $('<span>');
	
	var itemElementButton = $("<a>").attr('href','#').append(itemElementSpan);
	
	if (icon && $.trim(icon) != "") {
		itemElementLabel.addClass('hasIcon');
		itemElementSpan.append($('<div>').addClass('icon').css('background', 'url(\'../../images/icons/'+elementIcon+'\')'));
	}

	itemElementSpan.append(itemElementLabel);
	itemElementButton.click(function() {
		itemElementButton.blur();
		clickAction.call();
	});
	
	var itemElementButtonWrapper = $("<div>").addClass("headerActionButton").append(itemElementButton);
	itemElementButtonWrapper.label = itemElementLabel;
	
	return itemElementButtonWrapper;
}