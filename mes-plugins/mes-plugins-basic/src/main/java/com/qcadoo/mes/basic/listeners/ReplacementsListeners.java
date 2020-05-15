package com.qcadoo.mes.basic.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.SubstituteComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReplacementsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void openReplacements(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getEntity();
        String url = "/basic/productReplacements.html";
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.productId", product.getId());
        view.openModal(url, parameters);
    }

    public final void addManyReplacements(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        JSONObject obj = view.getJsonContext();
        if(obj.has("window.mainTab.product.productId")) {
            GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
            Long productId = obj.getLong("window.mainTab.product.productId");
            for(Long sub : grid.getSelectedEntitiesIds()) {
                Entity substituteComponent = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBSTITUTE_COMPONENT).create();
                substituteComponent.setField(SubstituteComponentFields.BASE_PRODUCT, productId);
                substituteComponent.setField(SubstituteComponentFields.PRODUCT, sub);
                substituteComponent.setField(SubstituteComponentFields.QUANTITY, 1);
                substituteComponent.getDataDefinition().save(substituteComponent);
            }
            CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
            generated.setChecked(true);
        }
    }
}
