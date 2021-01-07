package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologicalProcessListFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TOCProcessListDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        Long operationId = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM))
                .getPersistedEntityWithIncludedFormValues().getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                .getId();
        LookupComponent technologicalProcessList = (LookupComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.TECHNOLOGICAL_PROCESS_LIST);
        FilterValueHolder filterValueHolder = technologicalProcessList.getFilterValue();

        filterValueHolder.put(TechnologicalProcessListFields.OPERATION, operationId);

        technologicalProcessList.setFilterValue(filterValueHolder);
    }
}
