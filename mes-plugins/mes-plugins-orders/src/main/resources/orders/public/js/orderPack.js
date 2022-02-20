var thatObject = this;

QCD = QCD || {};

QCD.translate = function (key) {
    var msg = QCD.translations[key];
    return msg === undefined ? '[' + key + ']' : msg;
};

thatObject.duringProduction = #{window}.getRibbonItem("status.duringProduction");
thatObject.finishedProduction = #{window}.getRibbonItem("status.finishedProduction");

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
    getRibbonItem("status.duringProduction").disable();
    getRibbonItem("status.finishedProduction").disable();
}

thatObject.resetRibbon = resetRibbon;

function getRibbonItem(ribbonPath) {
    return #{window}.getRibbonItem(ribbonPath);
}

thatObject.getRibbonItem = getRibbonItem;

function updateRibbonByState(state) {

    if (state == "01pending") {
        thatObject.duringProduction.enable();
        thatObject.finishedProduction.disable();
        thatObject.delete.disable();
    } else if (state == "02duringProduction") {
        thatObject.duringProduction.disable();
        thatObject.finishedProduction.enable();
        thatObject.delete.enable();
    } else if (state == "03finishedProduction") {
        thatObject.duringProduction.disable();
        thatObject.finishedProduction.disable();
        thatObject.delete.disable();
    }

    thatObject.duringProduction.confirmMessage = QCD.translate('orders.orderPack.status.confirm.duringProduction');
    thatObject.duringProduction.setLabel(QCD.translate('orders.orderPack.status.duringProduction'));
    thatObject.duringProduction.forState = "02duringProduction";
    
    thatObject.finishedProduction.confirmMessage = QCD.translate('orders.orderPack.status.confirm.finishedProduction');
    thatObject.finishedProduction.setLabel(QCD.translate('orders.orderPack.status.finishedProduction'));
    thatObject.finishedProduction.forState = "03finishedProduction";


}

thatObject.updateRibbonByState = updateRibbonByState;
