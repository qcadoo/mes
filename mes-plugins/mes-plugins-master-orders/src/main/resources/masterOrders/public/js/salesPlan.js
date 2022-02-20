var thatObject = this;

QCD = QCD || {};

QCD.translate = function (key) {
    var msg = QCD.translations[key];
    return msg === undefined ? '[' + key + ']' : msg;
};

thatObject.complete = #{window}.getRibbonItem("status.complete");
thatObject.reject = #{window}.getRibbonItem("status.reject");

thatObject.delete = #{window}.getRibbonItem("actions.delete");
thatObject.copy = #{window}.getRibbonItem("actions.copy");

thatObject.changeState = function (eventPerformer, ribbonItemName, entityId) {
    var ribbonItem = thatObject.getRibbonItem("status." + ribbonItemName);
    var newState = ribbonItem.forState;
    if (window.canClose()) {
        if (window.confirm(ribbonItem.confirmMessage)) {
            this.fireEvent(eventPerformer, 'changeState', [newState]);
        }
    }
}

function resetRibbon() {
    getRibbonItem("status.complete").disable();
    getRibbonItem("status.reject").disable();
}

thatObject.resetRibbon = resetRibbon;

function getRibbonItem(ribbonPath) {
    return #{window}.getRibbonItem(ribbonPath);
}

thatObject.getRibbonItem = getRibbonItem;

function updateRibbonByState(state) {

    if (state == "01draft") {
        thatObject.complete.enable();
        thatObject.reject.enable();
        thatObject.delete.enable();
    } else if (state == "02rejected") {
        thatObject.complete.disable();
        thatObject.reject.disable();
        thatObject.delete.enable();
    } else if (state == "03completed") {
        thatObject.complete.disable();
        thatObject.reject.disable();
        thatObject.delete.disable();
    }

    thatObject.complete.confirmMessage = QCD.translate('masterOrders.salesPlan.status.confirm.complete');
    thatObject.complete.setLabel(QCD.translate('masterOrders.salesPlan.status.complete'));
    thatObject.complete.forState = "03completed";
    
    thatObject.reject.confirmMessage = QCD.translate('masterOrders.salesPlan.status.confirm.reject');
    thatObject.reject.setLabel(QCD.translate('masterOrders.salesPlan.status.reject'));
    thatObject.reject.forState = "02rejected";


}

thatObject.updateRibbonByState = updateRibbonByState;
