package com.qcadoo.mes.basicProductionCounting.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class ProductionCountingReplacementListeners {


    private static final String WINDOW_MAIN_TAB_FORM_PCQ = "window.mainTab.form.productionCountingQuantity";

    private static final String L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT = "window.mainTab.form.basicProduct";

    private static final String PRODUCT = "product";
    public static final String L_PLANNED_QUANTITY = "plannedQuantity";
    public static final String L_REPLACES_QUANTITY = "replacesQuantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void onPlannedQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent plannedQuantity = (FieldComponent) view.getComponentByReference(L_PLANNED_QUANTITY);
        FieldComponent replacesQuantity = (FieldComponent) view.getComponentByReference(L_REPLACES_QUANTITY);
        replacesQuantity.setFieldValue(plannedQuantity.getFieldValue());
    }

    public void addReplacement(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
        generated.setChecked(false);

        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        entity = entity.getDataDefinition().validate(entity);
        if (!entity.isValid()) {
            form.setEntity(entity);
            return;
        }
        JSONObject context = view.getJsonContext();
        Long productionCountingQuantityId = context.getLong(WINDOW_MAIN_TAB_FORM_PCQ);
        Long basicProductId = context.getLong(L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT);

        Entity productionCountingQuantity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).get(productionCountingQuantityId);

        try {
            createProductionCountingQuantity(view, entity, productionCountingQuantity, basicProductId);
            generated.setChecked(true);
        } catch (EntityRuntimeException ere) {
            ere.getGlobalErrors().forEach(errorMessage -> {
                view.addMessage(errorMessage.getMessage(), ComponentState.MessageType.FAILURE, errorMessage.getVars());
            });
            view.addMessage("productionCounting.useReplacement.error", ComponentState.MessageType.FAILURE);
        }
    }

    @Transactional
    private void createProductionCountingQuantity(ViewDefinitionState view, Entity entity,
                                                    Entity productionCountingQuantity, Long basicProductId) {
        BigDecimal plannedQuantity = entity.getDecimalField(L_PLANNED_QUANTITY);
        BigDecimal replacesQuantity = entity.getDecimalField(L_REPLACES_QUANTITY);

        BigDecimal pcqPlannedQuantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

        BigDecimal newPcqPlannedQuantity = pcqPlannedQuantity.subtract(replacesQuantity, numberService.getMathContext());
        if (BigDecimal.ZERO.compareTo(newPcqPlannedQuantity) >= 0) {
            entity.addGlobalError("basicProductionCounting.useReplacement.error.replacesQuantityToBig");
            throw new EntityRuntimeException(entity);
        }

        Entity in = productionCountingQuantity.copy();
        in.setId(null);
        in.setField(ProductionCountingQuantityFields.BATCHES, Lists.newArrayList());
        in.setField(ProductionCountingQuantityFields.USED_QUANTITY, BigDecimal.ZERO);
        in.setField(ProductionCountingQuantityFields.PRODUCTION_COUNTING_ATTRIBUTE_VALUES, Lists.newArrayList());
        in.setField(ProductionCountingQuantityFields.PRODUCT, entity.getBelongsToField(PRODUCT).getId());
        in.setField(ProductionCountingQuantityFields.REPLACEMENT_TO, productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId());
        in.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY, plannedQuantity);
        in = in.getDataDefinition().save(in);
        if(!in.isValid()) {
            throw new EntityRuntimeException(in);
        }
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY, newPcqPlannedQuantity);
        productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
    }
}
