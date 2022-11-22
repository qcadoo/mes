package com.qcadoo.mes.costCalculation.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.TechnologyFieldsCC;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologyDetailsHooksCC {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent grid = (GridComponent) view.getComponentByReference(TechnologyFieldsCC.ADDITIONAL_DIRECT_COSTS);
        Entity technology = technologyForm.getEntity();

        String state = technology.getStringField(TechnologyFields.STATE);

        grid.setEnabled(TechnologyState.DRAFT.getStringValue().equals(state) || TechnologyState.CHECKED.getStringValue().equals(state)
                || TechnologyState.ACCEPTED.getStringValue().equals(state));
    }
}
