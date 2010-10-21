var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.utils = QCD.components.elements.utils || {};

QCD.components.elements.utils.HeaderUtils = {};

QCD.components.elements.utils.HeaderUtils.createHeaderButton = function(label, clickAction, icon) {
	var elementIcon = (icon && $.trim(icon) != "") ? $.trim(icon) : null;
	if (icon && $.trim(icon) != "") {
		var itemElementButton = $("<a href='#'>").html("<span><div class='icon' style=\"background-image:url(\'../../images/icons/"+elementIcon+"\')\"></div><div class='hasIcon'>"+label+"</div></span>");	
	} else {
		var itemElementButton = $("<a href='#'>").html("<span><div>"+label+"</div></span>");
	}
	itemElementButton.click(clickAction);
	var itemElementButtonWrapper = $("<div>").addClass("headerActionButton").append(itemElementButton);
	return itemElementButtonWrapper;
}