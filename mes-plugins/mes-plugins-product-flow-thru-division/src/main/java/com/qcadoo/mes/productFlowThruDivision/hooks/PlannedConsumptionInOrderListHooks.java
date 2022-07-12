package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.productFlowThruDivision.criteriaModifiers.ProductsFlowInCriteriaModifiers;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class PlannedConsumptionInOrderListHooks {

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {

        JSONObject jsonObject = view.getJsonContext();
        jsonObject.keys();

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        FilterValueHolder gridProductsComponentInHolder = grid.getFilterValue();
        gridProductsComponentInHolder.put("productId", Integer.valueOf(jsonObject.getString("window.productId")));
        grid.setFilterValue(gridProductsComponentInHolder);
    }

}