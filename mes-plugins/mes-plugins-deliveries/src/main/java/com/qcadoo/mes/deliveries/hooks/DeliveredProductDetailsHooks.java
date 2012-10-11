package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.ORDERED_QUANTITY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class DeliveredProductDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillOrderedQuantities(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(PRODUCT);
        Entity product = productLookup.getEntity();
        FieldComponent orderedQuantity = (FieldComponent) view.getComponentByReference(ORDERED_QUANTITY);
        if (product == null) {
            orderedQuantity.setFieldValue(null);
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity delivery = form.getEntity().getBelongsToField(DELIVERY);
        Entity orderedProduct = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(DELIVERY, delivery)).uniqueResult();
        orderedQuantity.setFieldValue(orderedProduct.getDecimalField(ORDERED_QUANTITY));
        orderedQuantity.requestComponentUpdateState();
    }

    public void fillUnitsFields(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(PRODUCT);
        Entity product = productLookup.getEntity();
        String unit = "";
        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);
        }
        for (String reference : Lists.newArrayList("damagedQuantityUNIT", "deliveredQuantityUNIT", "orderedQuantityUNIT")) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(unit);
            field.requestComponentUpdateState();
        }
    }

    public void setDeliveredQuantityFieldRequired(final ViewDefinitionState view) {
        FieldComponent delivedQuantity = (FieldComponent) view.getComponentByReference(DELIVERED_QUANTITY);
        delivedQuantity.setRequired(true);
        delivedQuantity.requestComponentUpdateState();
    }
}
