package com.qcadoo.mes.productionCounting.listeners;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
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

import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UseReplacementListeners {

    private static final String L_FORM = "form";

    private static final String PRODUCT = "product";

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
        Long productionTrackingId = context.getLong("window.mainTab.form.productionTracking");
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
        if(Objects.isNull(productIn)) {
            Entity in = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT).create();
            in.setField(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING, productionTrackingId);
            in.setField(TrackingOperationProductInComponentFields.PRODUCT, entity.getBelongsToField(PRODUCT).getId());
            in.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, entity.getDecimalField("quantity"));
            in.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, entity.getDecimalField("quantity"));
            in.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT, entity.getBelongsToField(PRODUCT).getStringField(ProductFields.UNIT));
            in = in.getDataDefinition().save(in);
            if (!in.isValid()) {
                view.addMessage("productionCounting.useReplacement.error", ComponentState.MessageType.FAILURE);
                generated.setChecked(false);
            } else {
                generated.setChecked(true);
            }
        } else {
            view.addMessage("productionCounting.useReplacement.productExists", ComponentState.MessageType.FAILURE, false);
            generated.setChecked(false);
        }
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
