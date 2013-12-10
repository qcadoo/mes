package com.qcadoo.mes.productionCounting.listeners;

import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.NUMBER;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class RecordOperationProductComponentListeners {

    private static final Set<String> UNIT_COMPONENT_REFERENCES = Sets.newHashSet("plannedQuantityUNIT", "usedQuantityUNIT");

    private static final String L_FORM = "form";

    private static final String L_PRODUCT = "product";

    private static final String L_NAME = "name";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity componentEntity = form.getPersistedEntityWithIncludedFormValues();
        Entity productEntity = componentEntity.getBelongsToField(L_PRODUCT);

        fillUnits(view, productEntity);
        fillFieldFromProduct(view, productEntity);
    }

    private void fillUnits(final ViewDefinitionState view, final Entity productEntity) {
        String unit = productEntity.getStringField(ProductFields.UNIT);
        for (String componentReferenceName : UNIT_COMPONENT_REFERENCES) {
            FieldComponent unitComponent = (FieldComponent) view.getComponentByReference(componentReferenceName);
            if (unitComponent != null) {
                unitComponent.setFieldValue(unit);
                unitComponent.requestComponentUpdateState();
            }
        }
    }

    public void fillFieldFromProduct(final ViewDefinitionState view, final Entity productEntity) {
        view.getComponentByReference(NUMBER).setFieldValue(productEntity.getField(NUMBER));
        view.getComponentByReference(L_NAME).setFieldValue(productEntity.getField(L_NAME));
    }

}
