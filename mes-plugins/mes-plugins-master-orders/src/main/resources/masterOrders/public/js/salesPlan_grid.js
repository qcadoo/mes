var thatObject = this;

this.addOnChangeListener({
    onChange: function (selectedEntitiesArray) {
        if (selectedEntitiesArray && selectedEntitiesArray.length > 0) {
            updateRibbonBySelectedEntitiesArray(selectedEntitiesArray);
        } else {
            thatObject.resetRibbon();
        }
        if (selectedEntitiesArray && selectedEntitiesArray.length === 1) {
            thatObject.copy.enable();
        } else {
            thatObject.copy.disable();
        }
    }
});

function updateRibbonBySelectedEntitiesArray(selectedEntitiesArray) {
    var differentStatesMessage = QCD.translate('masterOrders.salesPlan.status.differentStates');
    var differentStates = statesAreDifferent(selectedEntitiesArray);
   
    if (!differentStates) {
        var state = selectedEntitiesArray[0].fields.state;
    }

    if (!state) {
        thatObject.complete.disable(differentStatesMessage);
        thatObject.reject.disable(differentStatesMessage);
        thatObject.delete.disable(differentStatesMessage);

        return;
    }

    thatObject.updateRibbonByState(state);
}

function statesAreDifferent(selectedEntitiesArray) {
    if (selectedEntitiesArray.length < 2) {
        return false;
    }
    for (var i = 1; i < selectedEntitiesArray.length; i++) {
        if (selectedEntitiesArray[i - 1].fields.state !== selectedEntitiesArray[i].fields.state) {
            return true;
        }
    }
    return false;
}
