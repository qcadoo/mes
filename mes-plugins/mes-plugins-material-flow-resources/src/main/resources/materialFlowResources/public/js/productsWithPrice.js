var thatObject = this;

QCD = QCD || {};

QCD.translate = function (key) {
	var msg = QCD.translations[key];
	return msg === undefined ? '[' + key + ']' : msg;
};

thatObject.enterPricesToPositions = function (eventPerformer, ribbonItemName, entityId) {
	QCD.components.elements.utils.LoadingIndicator.blockElement($("#window_windowComponents"));
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	const context = urlParams.get('context')
	var contextObject = JSON.parse(context);
	var formId = contextObject['form.id'];

	var pgrid = thatObject.getGrid();
	var ids = pgrid.jqGrid('getDataIDs');

	var positions = [];
	var invalid = false;
	var invalidPrecision = false;
	var invalidScale = false;
	$.each(ids, function (i, id) {
		var position = {};
		position.id = id;
		position.value = $("#price_" + id).val().replace(',', '.').replace(/\s/g, '');
		$("#price_" + id).removeClass('is-invalid');

        var validationAttrResult = validateDecimalWithPrecisionAndScale(position.value);
        if (!validationAttrResult.validScale) {
            invalidScale = true;
            invalid = true;
            $("#price_" + id).addClass('is-invalid');

        } else if (!validationAttrResult.validPrecision) {
            invalidPrecision = true;
            invalid = true;
            $("#price_" + id).addClass('is-invalid');
        } else {
            positions.push(position);
        }
	});

	if(invalid) {
			QCD.components.elements.utils.LoadingIndicator.unblockElement($("#window_windowComponents"));
    		if(invalidPrecision) {
    	        mainController.showMessage({
            	    type: "error",
            	    content: QCD.translate('materialFlowResources.productsWithPrice.invalidPrecision.max')
                });
    		}

    		if(invalidScale) {
        		mainController.showMessage({
        			type: "error",
        			content: QCD.translate('materialFlowResources.productsWithPrice.invalidScale.max')
        		});
    		}


    		return;
	}

	var request = {};
	request.entityId = formId;
	request.positions = positions;

	$.ajax({
		url: "../../rest/materialFlowResources/productsWithPrice",
		type: "POST",
		data: JSON.stringify(request),
		contentType: "application/json",
		beforeSend: function () {},
		success: function (data) {

			if (data.status == 'OK') {
				mainController.goBack(true);
			} else {
				mainController.showMessage({
					type: "error",
					content: data.message
				});
			}

		},
		error: function (data) {
			mainController.showMessage({
				type: "error",
				content: data.message
			});

		},
		complete: function () {
			QCD.components.elements.utils.LoadingIndicator.unblockElement($("#window_windowComponents"));
		}
	});
}


function validateDecimalWithPrecisionAndScale(value){
    var precision = 12;
    var scale = 5;

    var validPrecision = true;
    var validScale = true;
    var isScale = false;
    var parts;

    if (value.toString().indexOf('.') > 0) {
        parts = value.toString().split('.');
        isScale = true;
    } else if(value.toString().indexOf(',') > 0){
        parts = value.toString().split(',');
        isScale = true;
    }


    if(isScale && parts[1].length > scale){
        validScale = false;
    }

    var cleanValueLength = value.toString().length;
    if(isScale){
        cleanValueLength = cleanValueLength -1 - parts[1].length;
    }

    if(cleanValueLength > (precision - scale)){
        validPrecision = false;
    }

    return {
        validPrecision : validPrecision,
        validScale : validScale
    };

}

function priceChange(pgrid) {

}

var mutationObserver = new MutationObserver(function (mutations) {
	mutations.forEach(function (mutation) {
		$("input.grid-input").unbind();
		$('input.grid-input').on('input', function (e) {
			e.target.value = e.target.value.replace(/[^0-9.,]/g, '').replace(/(\..*)\./g, '$1').replace(/(\,.*)\,/g, '$1');
		});

	});
});
const deliveredProductsGroupNode = document.getElementById('window.mainTab.form.gridLayout.positions');

mutationObserver.observe(deliveredProductsGroupNode, {
	attributes: true,
	characterData: false,
	childList: false,
	subtree: true,
	attributeOldValue: false,
	characterDataOldValue: false
});
