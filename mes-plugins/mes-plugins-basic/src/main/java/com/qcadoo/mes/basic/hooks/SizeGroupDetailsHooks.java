package com.qcadoo.mes.basic.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SizeGroupDetailsHooks {

    public void fillCriteriaModifiers(final ViewDefinitionState view) {
        FormComponent sizeGroupForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent sizeLookup = (LookupComponent) view.getComponentByReference("sizeLookup");

        Long sizeGroupId = sizeGroupForm.getEntityId();

        if (Objects.nonNull(sizeGroupId)) {
            FilterValueHolder filter = sizeLookup.getFilterValue();

            filter.put("sizeGroupId", sizeGroupId);

            sizeLookup.setFilterValue(filter);
        }

        sizeLookup.requestComponentUpdateState();
    }

}
