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

