/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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
		
		show: function(src, onLoadFunction) {
			this.iframe.hide();
			this.dialog.jqmShow();
			QCD.components.elements.utils.LoadingIndicator.blockElement(this.dialog);
			this.iframe.load(function() {
				iframe.show();
				onLoadFunction.call(this);
				QCD.components.elements.utils.LoadingIndicator.unblockElement(dialog);
			});
			this.iframe.attr("src", src);
		},
		
		refresh: function() {
			this.iframe.attr("src", this.iframe.attr("src"));
		},
		
		hide: function() {
			this.iframe.unbind("load");
			this.dialog.jqmHide();
		}
	};
}
