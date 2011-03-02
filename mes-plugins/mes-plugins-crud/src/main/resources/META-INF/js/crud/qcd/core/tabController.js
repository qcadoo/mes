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

QCD.TabController = function() {
	
	this.updateTabObjects = function() {
		
		$('input, select, a').live('keydown',function(event) {
            if (event.keyCode == 9) { // TAB
            	var element = $(this);
            	if (element.hasClass("customTabIndex")) {
            		
//            		var nextElement = null;
//            		while (nextElement == null) {
//            			var children = element.find("*[tabindex]");
//            			if (children.length > 0) {
//            				nextElement = 0;
//            			}
//            				
//            		}
//            		
//            		var children = element.find("*[tabindex]");
//            		if (children.length == 0)
//            		
//            		QCD.info(children);
//            		QCD.info(this.nextTabElement);
//            		
//            		if (this.nextTabElement) {
//            			this.nextTabElement.focus();
//            			event.preventDefault();	
//            		}
            	}
            }   
        });

	}
	
};

