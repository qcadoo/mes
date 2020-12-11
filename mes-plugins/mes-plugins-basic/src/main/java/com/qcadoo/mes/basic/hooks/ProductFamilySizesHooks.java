package com.qcadoo.mes.basic.hooks;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.criteriaModifiers.ProductFamilySizesCriteriaModifiers;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductFamilySizesHooks {

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        JSONObject obj = view.getJsonContext();
        if (obj.has("window.mainTab.product.id")) {
            GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
            FilterValueHolder holder = grid.getFilterValue();
            holder.put(ProductFamilySizesCriteriaModifiers.L_PRODUCT_ID, obj.getLong("window.mainTab.product.id"));
            grid.setFilterValue(holder);
        }
    }

    public void addOnBeforeRender(final ViewDefinitionState view) throws JSONException {
        JSONObject obj = view.getJsonContext();
        if (obj.has("window.mainTab.product.productId")) {
            GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
            FilterValueHolder holder = grid.getFilterValue();
            holder.put(ProductFamilySizesCriteriaModifiers.L_PRODUCT_ID, obj.getLong("window.mainTab.product.productId"));
            grid.setFilterValue(holder);
        }
    }
}
