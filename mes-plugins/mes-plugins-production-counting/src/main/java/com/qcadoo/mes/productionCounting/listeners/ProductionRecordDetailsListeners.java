package com.qcadoo.mes.productionCounting.listeners;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.constants.RecordOperationProductInComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionRecordDetailsListeners {

    private static final String L_FORM = "form";

    public void copyPlannedQuantity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        Entity productionRecord = form.getEntity().getDataDefinition().get(form.getEntityId());
        copyQuantity(productionRecord.getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS));
        copyQuantity(productionRecord.getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS));
    }

    private void copyQuantity(List<Entity> records) {
        for (Entity record : records) {
            BigDecimal plannedQuantity = record.getDecimalField(RecordOperationProductInComponentFields.PLANNED_QUANTITY);
            if (plannedQuantity == null) {
                plannedQuantity = BigDecimal.ZERO;
            }
            record.setField(RecordOperationProductInComponentFields.USED_QUANTITY, plannedQuantity);
            record.getDataDefinition().save(record);
        }
    }

}
