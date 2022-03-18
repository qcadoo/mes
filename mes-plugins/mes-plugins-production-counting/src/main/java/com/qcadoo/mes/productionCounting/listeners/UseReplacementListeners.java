package com.qcadoo.mes.productionCounting.listeners;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
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
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UseReplacementListeners {

    public static final String QUANTITY = "quantity";

    private static final String PRODUCT = "product";

    private static final String WINDOW_MAIN_TAB_FORM_PRODUCTION_TRACKING = "window.mainTab.form.productionTracking";

    private static final String L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT = "window.mainTab.form.basicProduct";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderMaterialsCostDataGenerator orderMaterialsCostDataGenerator;

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
        Long productionTrackingId = context.getLong(WINDOW_MAIN_TAB_FORM_PRODUCTION_TRACKING);
        Long basicProductId = context.getLong(L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT);

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
            Entity in = createInProductTracking(entity, productionTracking, basicProductId);
            if (!in.isValid()) {
                view.addMessage("productionCounting.useReplacement.error", ComponentState.MessageType.FAILURE);
            } else {
                generated.setChecked(true);
            }
        } else {
            view.addMessage("productionCounting.useReplacement.productExists", ComponentState.MessageType.FAILURE, false);
        }
    }

    private Entity createInProductTracking(final Entity entity, final Entity productionTracking, final Long basicProductId) {
        Entity in = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT).create();
        in.setField(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING, productionTracking.getId());
        in.setField(TrackingOperationProductInComponentFields.PRODUCT, entity.getBelongsToField(PRODUCT).getId());
        in.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, entity.getDecimalField(QUANTITY));
        in.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, entity.getDecimalField(QUANTITY));
        in.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT,
                entity.getBelongsToField(PRODUCT).getStringField(ProductFields.UNIT));
        in.setField(TrackingOperationProductInComponentFields.REPLACEMENT_TO, basicProductId);
        in = in.getDataDefinition().save(in);
        updateMaterialCosts(productionTracking.getBelongsToField(ProductionTrackingFields.ORDER));
        return in;
    }

    public Entity updateMaterialCosts(Entity order) {
        List<Entity> orderMaterialsCosts = orderMaterialsCostDataGenerator.generateUpdatedMaterialsListFor(order);
        order.setField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, orderMaterialsCosts);
        return order.getDataDefinition().save(order);
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
