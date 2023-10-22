var thatObject = this;

this.addOnChangeListener({
    onSetValue: function (value) {

        if (!value || !value.content) {
            return;
        }
        if (!value.content.entityId) {
            return;
        } else {
            thatObject.resetRibbon();

            thatObject.updateRibbonByState(#{state}.getValue().content.value);

            thatObject.addProductsBySize = #{window}.getRibbonItemOrNull("products.addProductsBySize");
            thatObject.createOrderGroup = #{window}.getRibbonItemOrNull("orders.createOrderGroup");
            thatObject.createOrders = #{window}.getRibbonItemOrNull("orders.createOrders");
            thatObject.createSalesPlanMaterialRequirement = #{window}.getRibbonItemOrNull("salesPlanMaterialRequirement.createSalesPlanMaterialRequirement");

            var state = #{state}.getValue().content.value;
            if (state == "01draft") {
                thatObject.addProductsBySize.enable();
                thatObject.createOrderGroup.enable();
                thatObject.createOrders.enable();
                if (thatObject.createSalesPlanMaterialRequirement) {
                    thatObject.createSalesPlanMaterialRequirement.enable();
                }
            } else if (state == "02rejected") {
                thatObject.addProductsBySize.disable();
                thatObject.createOrderGroup.disable();
                thatObject.createOrders.disable();
                if (thatObject.createSalesPlanMaterialRequirement) {
                    thatObject.createSalesPlanMaterialRequirement.disable();
                }
            } else if (state == "03completed") {
                thatObject.addProductsBySize.disable();
                thatObject.createOrderGroup.disable();
                thatObject.createOrders.disable();
                if (thatObject.createSalesPlanMaterialRequirement) {
                    thatObject.createSalesPlanMaterialRequirement.disable();
                }
            }
        }
    }
});
