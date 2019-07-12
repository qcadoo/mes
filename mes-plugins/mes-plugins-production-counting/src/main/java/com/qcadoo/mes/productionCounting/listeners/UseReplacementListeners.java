package com.qcadoo.mes.productionCounting.listeners;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

import java.math.BigDecimal;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UseReplacementListeners {

    public static final String QUANTITY = "quantity";

    private static final String L_FORM = "form";

    private static final String PRODUCT = "product";

    private static final String WINDOW_MAIN_TAB_FORM_PRODUCTION_TRACKING = "window.mainTab.form.productionTracking";

    private static final String L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT = "window.mainTab.form.basicProduct";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addReplacement(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
        generated.setChecked(false);

        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        entity = entity.getDataDefinition().validate(entity);
        if (!entity.isValid()) {
            form.setEntity(entity);
            return;
        }
        JSONObject context = view.getJsonContext();
        Long productionTrackingId = context.getLong(WINDOW_MAIN_TAB_FORM_PRODUCTION_TRACKING);
        Long basicProductId = context.getLong(L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT);
        Entity baseProduct = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                basicProductId);
        Entity productionTracking = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).get(productionTrackingId);

        Entity productIn = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,

                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT)
                .find()
                .add(SearchRestrictions.belongsTo(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING,
                        productionTracking))
                .add(SearchRestrictions.belongsTo(TrackingOperationProductInComponentFields.PRODUCT,
                        entity.getBelongsToField(PRODUCT))).setMaxResults(1).uniqueResult();
        if (Objects.isNull(productIn)) {
            Entity in = createInProductTracking(entity, productionTrackingId, basicProductId);
            if (!in.isValid()) {
                view.addMessage("productionCounting.useReplacement.error", ComponentState.MessageType.FAILURE);
            } else {
                Entity productionCountingQuantity = createProductionCountingQuantity(entity, baseProduct, productionTracking);
                if (productionCountingQuantity.isValid()) {
                    generated.setChecked(true);
                } else {
                    view.addMessage("productionCounting.useReplacement.error", ComponentState.MessageType.FAILURE);
                }
            }
        } else {
            view.addMessage("productionCounting.useReplacement.productExists", ComponentState.MessageType.FAILURE, false);
        }
    }

    private Entity createProductionCountingQuantity(Entity entity, Entity baseProduct, Entity productionTracking) {
        Entity productionCountingQuantity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).create();
        productionCountingQuantity.setField(ProductionCountingQuantityFields.ORDER,
                productionTracking.getBelongsToField(ProductionTrackingFields.ORDER).getId());
        if (Objects.nonNull(productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT))) {
            productionCountingQuantity.setField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                    productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT).getId());
        }
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY, BigDecimal.ZERO);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCT, entity.getBelongsToField(PRODUCT).getId());
        productionCountingQuantity.setField("role", ProductionCountingQuantityRole.USED.getStringValue());
        productionCountingQuantity
                .setField("typeOfMaterial", ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue());
        productionCountingQuantity.setField(ProductionCountingQuantityFields.FLOW_FILLED, Boolean.TRUE);
        Entity pCQBaseProduct = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER,
                        productionTracking.getBelongsToField(ProductionTrackingFields.ORDER)))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, baseProduct))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue())).setMaxResults(1).uniqueResult();
        if (Objects.nonNull(pCQBaseProduct)) {
            String type = pCQBaseProduct.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
            if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.COMPONENTS_LOCATION,
                        pCQBaseProduct.getField(ProductionCountingQuantityFields.COMPONENTS_LOCATION));
                productionCountingQuantity.setField(ProductionCountingQuantityFields.COMPONENTS_OUTPUT_LOCATION,
                        pCQBaseProduct.getField(ProductionCountingQuantityFields.COMPONENTS_OUTPUT_LOCATION));
            } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCTION_FLOW,
                        pCQBaseProduct.getField(ProductionCountingQuantityFields.PRODUCTION_FLOW));
                productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION,
                        pCQBaseProduct.getField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION));
            }
        }
        productionCountingQuantity = productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
        return productionCountingQuantity;
    }

    private Entity createInProductTracking(final Entity entity, final Long productionTrackingId, final Long basicProductId) {
        Entity in = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT).create();
        in.setField(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING, productionTrackingId);
        in.setField(TrackingOperationProductInComponentFields.PRODUCT, entity.getBelongsToField(PRODUCT).getId());
        in.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, entity.getDecimalField(QUANTITY));
        in.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, entity.getDecimalField(QUANTITY));
        in.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT,
                entity.getBelongsToField(PRODUCT).getStringField(ProductFields.UNIT));
        in.setField(TrackingOperationProductInComponentFields.REPLACEMENT_TO, basicProductId);
        in = in.getDataDefinition().save(in);
        return in;
    }

    public void fillUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(PRODUCT);
        Entity product = productLookup.getEntity();
        String unit = "";
        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);
        }
        FieldComponent field = (FieldComponent) view.getComponentByReference("unit");
        field.setFieldValue(unit);
        field.requestComponentUpdateState();
    }
}
