var QCD = QCD || {};
QCD.utils = QCD.utils || {};
QCD.utils.Modal = {};

QCD.utils.Modal.createModal = function() {
	
	var dialog = $("<div>").addClass("jqmWindow").width(600);
	
	var container = $("<div>").css("border", "solid red 0px").width(600).height(400);
	dialog.append(container);
	
	var iframe = $('<iframe frameborder="0" src="" width="600" height="400">');
	container.append(iframe);
	
	$("body").append(dialog);
	dialog.jqm({
		modal: true
	});
	
	return {
		dialog: dialog,
		iframe: iframe,
		show: function(src) {
			this.dialog.jqmShow();
			this.iframe.attr("src", src);
		},
		hide: function() {
			this.iframe.unbind("load");
			this.dialog.jqmHide();
		}
	};
}
