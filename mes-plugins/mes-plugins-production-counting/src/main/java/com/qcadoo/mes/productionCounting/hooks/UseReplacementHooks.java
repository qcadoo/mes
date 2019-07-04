package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class UseReplacementHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        fillFields(view);
    }

    private void fillFields(ViewDefinitionState view) {
        if(view.isViewAfterRedirect()) {
            JSONObject context = view.getJsonContext();
            LookupComponent basicProductComponent = (LookupComponent) view.getComponentByReference("basicProduct");
            LookupComponent productComponent = (LookupComponent) view.getComponentByReference("product");
            if(context.has("window.mainTab.form.basicProduct")) {
                try {
                    basicProductComponent.setFieldValue(context.getLong("window.mainTab.form.basicProduct"));
                    FilterValueHolder filterValueHolder = productComponent.getFilterValue();
                    filterValueHolder.put("BASIC_PRODUCT_ID", context.getLong("window.mainTab.form.basicProduct"));
                    productComponent.setFilterValue(filterValueHolder);
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }
    }

}
