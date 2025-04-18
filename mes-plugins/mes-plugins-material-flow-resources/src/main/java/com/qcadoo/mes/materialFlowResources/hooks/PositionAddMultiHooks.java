package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.criteriaModifiers.ResourceCriteriaModifiers;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class PositionAddMultiHooks {

    public final void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity helper = form.getPersistedEntityWithIncludedFormValues();
        JSONObject context = view.getJsonContext();
        if (context != null) {
            Long warehouseId = context.getLong("window.mainTab.helper.gridLayout.warehouseId");
            Long documentId = context.getLong("window.mainTab.helper.gridLayout.documentId");
            helper.setField("documentId", documentId);
            helper.setField("warehouseId", warehouseId);

            GridComponent grid = (GridComponent) view.getComponentByReference("resourceGrid");
            FilterValueHolder filter = grid.getFilterValue();
            filter.put(ResourceCriteriaModifiers.L_LOCATION_FROM, warehouseId);
            grid.setFilterValue(filter);

            form.setEntity(helper);
        }

    }

}
