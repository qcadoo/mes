var QCD = QCD || {};

QCD.MessagesController = function() {
	
	this.addMessage = function(type, content) { // type = [info|error|success]
		QCD.info(type+": "+content);
	}
	
}