package com.qcadoo.mes.basicProductionCounting.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductionCountingReplacementHooks {


    public static final String L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT = "window.mainTab.form.basicProduct";
    private static final String WINDOW_MAIN_TAB_FORM_PCQ = "window.mainTab.form.productionCountingQuantity";

    public static final String BASIC_PRODUCT = "basicProduct";

    public static final String PRODUCT = "product";
    public static final String UNIT = "unit";
    public static final String REPLACES_QUANTITY_UNIT = "replacesQuantityUnit";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillFields(view);
        fillUnitField(view, BASIC_PRODUCT, REPLACES_QUANTITY_UNIT);
        fillUnitField(view, PRODUCT, UNIT);
    }

    private void fillFields(ViewDefinitionState view) {
        if (view.isViewAfterRedirect()) {
            JSONObject context = view.getJsonContext();
            LookupComponent basicProductComponent = (LookupComponent) view.getComponentByReference(BASIC_PRODUCT);
            LookupComponent productComponent = (LookupComponent) view.getComponentByReference(PRODUCT);

            if (context.has(L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT)) {
                try {
                    basicProductComponent.setFieldValue(context.getLong(L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT));
                    FilterValueHolder filterValueHolder = productComponent.getFilterValue();
                    filterValueHolder.put("BASIC_PRODUCT_ID", context.getLong(L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT));
                    productComponent.setFilterValue(filterValueHolder);
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }


        }
    }

    public void fillUnitField(final ViewDefinitionState view, String ref, String unitRef) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ref);
        Entity product = productLookup.getEntity();
        String unit = "";
        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);
        }
        FieldComponent field = (FieldComponent) view.getComponentByReference(unitRef);
        field.setFieldValue(unit);
        field.requestComponentUpdateState();
    }

}