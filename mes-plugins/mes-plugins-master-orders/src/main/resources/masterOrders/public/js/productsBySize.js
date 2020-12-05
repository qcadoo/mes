var thatObject = this;

QCD = QCD || {};

QCD.translate = function (key) {
	var msg = QCD.translations[key];
	return msg === undefined ? '[' + key + ']' : msg;
};

thatObject.addPositionsToOrder = function (eventPerformer, ribbonItemName, entityId) {

	QCD.components.elements.utils.LoadingIndicator.blockElement($("#window_windowComponents"));
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	const context = urlParams.get('context')
	var contextObject = JSON.parse(context);
	var formId = contextObject['form.id'];

	var pgrid = thatObject.getGrid();
	var ids = pgrid.jqGrid('getDataIDs');

	var positions = [];
	$.each(ids, function (i, id) {
		var position = {};
		position.id = id;
		position.value = $("#quantity_" + id).val();
		if (position.value) {
			positions.push(position);
		}
	});

	if (positions.length === 0) {
		QCD.components.elements.utils.LoadingIndicator.unblockElement($("#window_windowComponents"));
		mainController.showMessage({
			type: "error",
			content: QCD.translate('masterOrders.productsBySize.productNotSelectedOrPositionsNotFilled')
		});
		return;
	}

	var request = {};
	request.entityId = formId;
	request.positions = positions;
	console.log(request);

	$.ajax({
		url: "../../rest/masterOrders/productsBySize",
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

function quantityChange(pgrid) {
	var ids = $("#" + "window_mainTab_masterOrderDefinitionDetails_gridLayout_productsBySizeEntryHelpers_grid").jqGrid('getDataIDs');
	var totalSum = 0;
	$.each(ids, function (i, id) {
		totalSum += parseFloat(nullToZeroValue($("#quantity_" + id).val().split(' ').join('').replace(',', '.'))) || 0;
	});

	document.getElementById('window.mainTab.masterOrderDefinitionDetails.gridLayout.totalQuantity_input').value = parseFloat(totalSum.toFixed(5));
}

function nullToZeroValue(value) {
	if (value) {
		return value;
	} else {
		return 0;
	}
}

var mutationObserver = new MutationObserver(function (mutations) {
	mutations.forEach(function (mutation) {
		$("input.grid-input").unbind();
		$('input.grid-input').on('input', function (e) {
			e.target.value = e.target.value.replace(/[^0-9.,]/g, '').replace(/(\..*)\./g, '$1').replace(/(\,.*)\,/g, '$1');
		});

	});
});
const materialsGroupNode = document.getElementById('window.mainTab.masterOrderDefinitionDetails.gridLayout.productsBySizeEntryHelpers');

mutationObserver.observe(materialsGroupNode, {
	attributes: true,
	characterData: false,
	childList: false,
	subtree: true,
	attributeOldValue: false,
	characterDataOldValue: false
});
