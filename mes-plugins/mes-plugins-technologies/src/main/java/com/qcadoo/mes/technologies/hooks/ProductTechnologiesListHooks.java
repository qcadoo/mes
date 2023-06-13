package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.criteriaModifiers.ProductTechnologiesCriteriaModifiers;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProductTechnologiesListHooks {

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        JSONObject jsonContext = view.getJsonContext();
        jsonContext.length();

        Long productId = jsonContext.getLong("window.productId");

        if(Objects.nonNull(productId)) {

            GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

            FilterValueHolder filterValue = grid.getFilterValue();
            filterValue.put(ProductTechnologiesCriteriaModifiers.L_PRODUCT_ID, productId);
            grid.setFilterValue(filterValue);

        }

    }
}