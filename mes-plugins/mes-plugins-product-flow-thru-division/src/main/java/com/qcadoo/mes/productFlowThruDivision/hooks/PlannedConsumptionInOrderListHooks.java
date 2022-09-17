package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productFlowThruDivision.criteriaModifiers.ProductsFlowInCriteriaModifiers;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlannedConsumptionInOrderListHooks {

    public static final String L_PRODUCT_NAME = "productName";
    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {

        JSONObject jsonObject = view.getJsonContext();
        if(view.isViewAfterRedirect()) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(L_PRODUCT_NAME);
            fillProductName(jsonObject, component);
        }
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        FilterValueHolder gridProductsComponentInHolder = grid.getFilterValue();
        gridProductsComponentInHolder.put("productId", Integer.valueOf(jsonObject.getString("window.productId")));
        grid.setFilterValue(gridProductsComponentInHolder);
    }

    private void fillProductName(JSONObject jsonObject, FieldComponent component) throws JSONException {
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)
                .get(Long.valueOf(jsonObject.getString("window.productId")));
        component.setFieldValue(product.getStringField(ProductFields.NUMBER));
        component.requestComponentUpdateState();
    }

}