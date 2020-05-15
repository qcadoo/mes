package com.qcadoo.mes.materialFlowResources.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductDetailsListenersMFR {

    public void showStorageLocationHistory(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long id = form.getEntityId();
        String url = "../page/materialFlowResources/productStorageLocationHistoryDetails.html?context={\"form.id\":\"" + id
                + "\"}";
        view.openModal(url);
    }

}
