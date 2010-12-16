/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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
		itemElementSpan.append($('<div>').addClass('icon').css('backgroundImage', 'url(\'/img/core/icons/'+elementIcon+'\')'));
	}

	itemElementSpan.append(itemElementLabel);
	if (label == "") {
		itemElementLabel.css("paddingLeft", "0px");
		itemElementLabel.css("paddingRight", "3px");
	}
	itemElementButton.click(function() {
		itemElementButton.blur();
		clickAction.call();
	});
	
	var itemElementButtonWrapper = $("<div>").addClass("headerActionButton").append(itemElementButton);
	itemElementButtonWrapper.label = itemElementLabel;
	
	return itemElementButtonWrapper;
}