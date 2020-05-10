package com.qcadoo.mes.productionScheduling.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.productionScheduling.criteriaModifiers.OperCompTimeCalculationsCM;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class OrderTimePredictionHooks {

    

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (form.getEntityId() == null && view.isViewAfterRedirect()) {
            CheckBoxComponent includeTpzField = (CheckBoxComponent) view.getComponentByReference(OrderFieldsPS.INCLUDE_TPZ);
            boolean checkIncludeTpzField = parameterService.getParameter().getBooleanField("includeTpzPS");
            includeTpzField.setChecked(checkIncludeTpzField);
            includeTpzField.requestComponentUpdateState();

            CheckBoxComponent includeAdditionalTimeField = (CheckBoxComponent) view.getComponentByReference(OrderFieldsPS.INCLUDE_ADDITIONAL_TIME);
            boolean checkIncludeAdditionalTimeField = parameterService.getParameter().getBooleanField("includeAdditionalTimePS");
            includeAdditionalTimeField.setChecked(checkIncludeAdditionalTimeField);
            includeAdditionalTimeField.requestComponentUpdateState();
        }
        setCriteriaModifierParameters(view);
    }

    private void setCriteriaModifierParameters(ViewDefinitionState view) {
        LookupComponent techComponent = (LookupComponent) view.getComponentByReference("technology");
        Entity tech = techComponent.getEntity();
        GridComponent grid = (GridComponent) view.getComponentByReference("operCompTimeCalculationsGrid");
        FilterValueHolder holder = grid.getFilterValue();
        if(Objects.nonNull(tech)){
            holder.put(OperCompTimeCalculationsCM.TECHNOLOGY_PARAMETER, tech.getId());
            grid.setFilterValue(holder);
        } else {
            if(holder.has(OperCompTimeCalculationsCM.TECHNOLOGY_PARAMETER)){
                holder.remove(OperCompTimeCalculationsCM.TECHNOLOGY_PARAMETER);
                grid.setFilterValue(holder);
            }
        }


    }
}
