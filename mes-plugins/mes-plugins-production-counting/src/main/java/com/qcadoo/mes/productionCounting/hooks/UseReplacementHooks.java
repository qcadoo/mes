package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class UseReplacementHooks {

    public static final String L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT = "window.mainTab.form.basicProduct";

    public static final String BASIC_PRODUCT = "basicProduct";

    public static final String PRODUCT = "product";

    public void onBeforeRender(final ViewDefinitionState view) {
        fillFields(view);
    }

    private void fillFields(ViewDefinitionState view) {
        if(view.isViewAfterRedirect()) {
            JSONObject context = view.getJsonContext();
            LookupComponent basicProductComponent = (LookupComponent) view.getComponentByReference(BASIC_PRODUCT);
            LookupComponent productComponent = (LookupComponent) view.getComponentByReference(PRODUCT);
            if(context.has(L_WINDOW_MAIN_TAB_FORM_BASIC_PRODUCT)) {
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

}
