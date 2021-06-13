var thatObject = this;

this.addOnChangeListener({
    onSetValue: function (value) {
        thatObject.resetRibbon();

        if (!value || !value.content) {
            return;
        }
        if (!value.content.entityId) {
            return;
        } else {
            thatObject.updateRibbonByState(#{state}.getValue().content.value);
        }
    }
});
