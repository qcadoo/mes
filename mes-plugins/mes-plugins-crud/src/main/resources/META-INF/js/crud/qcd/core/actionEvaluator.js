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

QCD.ActionEvaluator = function(_pageController) {
	
	var pageController = _pageController;
	
	this.performJsAction = function(jsBody, scope) {
		
		jsBody = " "+Encoder.htmlDecode(jsBody)+" ";
		
		var referenceObject = {};
		var thisObject = scope;
		
		if (/\WreferenceObject\W/.test(jsBody)) {
			printError("script contains forbidden keyword 'referenceObject'");
			return;
		}
		if (/\WthisObject\W/.test(jsBody)) {
			printError("script contains forbidden keyword 'thisObject'");
			return;
		}
		if (!scope) {
			if (/\Wthis\W/.test(jsBody)) {
				printError("script contains keyword 'this', but scope is not defined");
				return;
			}
		}
		
		var referencePatternRegexp = /#\{[^\}]+\}/g;
		var referencePatternMatches = jsBody.match(referencePatternRegexp);
		
		for (var i in referencePatternMatches) {
			var referencePattern = referencePatternMatches[i];
			referenceName = referencePattern.substring(2, referencePattern.length-1);
			if (! referenceObject[referenceName]) {
				var referenceValue = pageController.getComponentByReferenceName(referenceName);
				if (referenceValue == null) {
					printError("no component with referenceName '"+referenceName+"'");
					return;
				}
				referenceObject[referenceName] = referenceValue;
			}
		}
		
		for (var referenceName in referenceObject) {
			var referenveRegexp = new RegExp("#\{"+referenceName+"\}","g");
			jsBody = jsBody.replace(referenveRegexp, "referenceObject."+referenceName);
		}
		
		var thisPatternRegexp = /\Wthis\W/g;
		var thisPatternMatches = jsBody.match(thisPatternRegexp);
		for (var i in thisPatternMatches) {
			var thisPattern = thisPatternMatches[i];
			jsBody = jsBody.replace(thisPattern, thisPattern[0]+"thisObject"+thisPattern[thisPattern.length-1]);
		}
		
		try {
			eval(jsBody);
		} catch (e) {
			printError(e);
		}
		
	}
	
	this.performRibbonAction = function(ribbonAction) {
		var actionParts = ribbonAction.split(";");
		var actions = new Array();
		for (var actionIter in actionParts) {
			var action = $.trim(actionParts[actionIter]);
			if (action) {
				var elementBegin = action.search("{");
				var elementEnd = action.search("}");
				if (elementBegin<0 || elementEnd<0 || elementEnd<elementBegin) {
					QCD.error("action parse error in: "+action);
					return;
				}
				var elementPath = action.substring(elementBegin+1, elementEnd);
				var component = pageController.getComponent(elementPath);
				
				var elementAction = action.substring(elementEnd+1);
				if (elementAction[0] != ".") {
					QCD.error("action parse error in: "+action);
					return;
				}
				elementAction = elementAction.substring(1);

				var argumentsBegin = elementAction.indexOf("(");
				var argumentsEnd = elementAction.indexOf(")");
				var argumentsList = new Array();
				
				//(argumentsBegin < argumentsEnd-1) because it then means that there are no arguments
				//and only empty parenthesis ()
				if(argumentsBegin > 0 && argumentsEnd > 0 && argumentsBegin < argumentsEnd-1) {
					var args = elementAction.substring(argumentsBegin+1, argumentsEnd);
					argumentsList = args.split(",");
					elementAction = elementAction.substring(0, argumentsBegin);
				} else if(argumentsBegin == argumentsEnd-1) {
					//we need to get rid of the empty parenthesis
					elementAction = elementAction.substring(0, argumentsBegin);
				}

				var actionObject = {
					component: component,
					action: elementAction,
					arguments: argumentsList
				}
				
				actions.push(actionObject);
			}
		}
		var actionsPerformer = {
			actions: actions,
			actionIter: 0,
			performNext: function() {
				var actionObject = this.actions[this.actionIter];
				if (actionObject) {
					var func = actionObject.component[actionObject.action];
					if (!func) {
						QCD.error("no function in "+actionObject.component.elementPath+": "+actionObject.action);
						return;
					}
					this.actionIter++;
					
					var fullArgumentList = new Array(this);
					fullArgumentList = fullArgumentList.concat(actionObject.arguments[0]);
					fullArgumentList.push(actionObject.arguments.slice(1));
					
					func.apply(actionObject.component, fullArgumentList);
				}
			}
		}
		actionsPerformer.performNext();
	}
	
	function printError(msg) {
		QCD.error("cannot evaluate script: "+msg);
	}
	
}