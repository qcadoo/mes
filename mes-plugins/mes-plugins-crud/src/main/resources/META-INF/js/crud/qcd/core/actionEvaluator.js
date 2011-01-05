var QCD = QCD || {};

QCD.ActionEvaluator = function(_pageController) {
	
	var pageController = _pageController;
	
	this.performJsAction = function(jsBody, scope) {
		jsBody = " "+jsBody+" ";
		
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
	
	function printError(msg) {
		QCD.error("cannot evaluate script: "+msg);
	}
	
}