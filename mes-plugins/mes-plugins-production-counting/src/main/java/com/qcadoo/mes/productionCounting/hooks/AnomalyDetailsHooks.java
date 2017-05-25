package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AnomalyDetailsHooks {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnomalyDetailsHooks.class);

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillFields(view);
    }

    private void fillFields(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity anomaly = form.getEntity().getDataDefinition().get(form.getEntityId());

        FieldComponent productionTrackingNumberField = (FieldComponent) view.getComponentByReference("productionTrackingNumber");
        FieldComponent createDateField = (FieldComponent) view.getComponentByReference("createDate");
        createDateField.setEnabled(false);
        FieldComponent orderNumberField = (FieldComponent) view.getComponentByReference("orderNumber");
        FieldComponent masterProductNumberField = (FieldComponent) view.getComponentByReference("masterProductNumber");
        FieldComponent productionLineNumberField = (FieldComponent) view.getComponentByReference("productionLineNumber");
        FieldComponent productNumberField = (FieldComponent) view.getComponentByReference("productNumber");
        FieldComponent reasonsField = (FieldComponent) view.getComponentByReference("reasons");
        FieldComponent unitField = (FieldComponent) view.getComponentByReference("unit");

        Entity productionTracking = anomaly.getBelongsToField("productionTracking");

        productionTrackingNumberField.setFieldValue(productionTracking.getStringField(ProductionTrackingFields.NUMBER));
        orderNumberField.setFieldValue(
                productionTracking.getBelongsToField(ProductionTrackingFields.ORDER).getStringField(OrderFields.NUMBER));

        productionLineNumberField.setFieldValue(productionTracking.getBelongsToField(ProductionTrackingFields.ORDER)
                .getBelongsToField(OrderFields.PRODUCTION_LINE).getStringField(ProductionLineFields.NUMBER));
        masterProductNumberField.setFieldValue(anomaly.getBelongsToField("masterProduct").getStringField(ProductFields.NUMBER));
        productNumberField.setFieldValue(anomaly.getBelongsToField("product").getStringField(ProductFields.NUMBER));
        unitField.setFieldValue(anomaly.getBelongsToField("product").getStringField(ProductFields.UNIT));
        reasonsField.setFieldValue(anomaly.getHasManyField("anomalyReasons").stream().map(a -> a.getStringField("name"))
                .collect(Collectors.joining(", ")));

        productionTrackingNumberField.requestComponentUpdateState();
        orderNumberField.requestComponentUpdateState();
        productionLineNumberField.requestComponentUpdateState();
        masterProductNumberField.requestComponentUpdateState();
        productNumberField.requestComponentUpdateState();
        unitField.requestComponentUpdateState();
        reasonsField.requestComponentUpdateState();
    }

}
