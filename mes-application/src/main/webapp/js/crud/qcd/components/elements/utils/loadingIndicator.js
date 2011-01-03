var QCD = QCD || {};
QCD.components = QCD.components || {};
QCD.components.elements = QCD.components.elements || {};
QCD.components.elements.utils = QCD.components.elements.utils || {};

QCD.components.elements.utils.LoadingIndicator = {};

QCD.components.elements.utils.LoadingIndicator.blockElement = function(element) {
	element.block({ message: '<div class="loading_div">'+""+'</div>', showOverlay: true,  fadeOut: 0, fadeIn: 0,
		css: { 
	        border: 'none', 
	        padding: '15px', 
	        backgroundColor: '#000', 
	        '-webkit-border-radius': '10px', 
	        '-moz-border-radius': '10px', 
	        opacity: .5, 
	        color: '#fff',
	        width: '50px'
        },
        overlayCSS:  { 
            backgroundColor: '#000', 
            opacity:         0.1 
        }, 
	});
};

QCD.components.elements.utils.LoadingIndicator.unblockElement = function(element) {
	element.unblock();
};

