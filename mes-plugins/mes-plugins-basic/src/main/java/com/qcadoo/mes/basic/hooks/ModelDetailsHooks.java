package com.qcadoo.mes.basic.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ModelDetailsHooks {

    public void fillCriteriaModifiers(final ViewDefinitionState view) {
        FormComponent modelForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference("productLookup");

        Long modelId = modelForm.getEntityId();

        if (Objects.nonNull(modelId)) {
            FilterValueHolder filter = productLookup.getFilterValue();

            filter.put("modelId", modelId);

            productLookup.setFilterValue(filter);
        }

        productLookup.requestComponentUpdateState();
    }

}
