var thatObject = this;

QCD = QCD || {};

QCD.translate = function (key) {
    var msg = QCD.translations[key];
    return msg === undefined ? '[' + key + ']' : msg;
};

thatObject.addOnChangeListener({
        onSetValue: function(newValue) {
                if (!newValue || !newValue.content) {
                        return;
                }

                updateRibbon(newValue.content.value);
        }
});


function updateRibbon(state) {
        var checkState = #{window}.getRibbonItem("status.checkTechnology");

        checkState.forState="05checked";
        checkState.confirmMessage = QCD.translate('technologies.technologyDetails.window.ribbon.status.confirm.checked');
        checkState.setLabel(QCD.translate('technologies.technologyDetails.window.ribbon.status.checkTechnology'));

        if (state === "05checked" || state === '02accepted') {
            checkState.enable();
            checkState.forState="01draft"
            checkState.confirmMessage = QCD.translate('technologies.technologyDetails.window.ribbon.status.confirm.draft');
            checkState.setLabel(QCD.translate('technologies.technologyDetails.window.ribbon.status.draftTechnology'));
        }
}

thatObject.updateRibbon = updateRibbon;
