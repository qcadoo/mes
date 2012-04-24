package com.qcadoo.mes.workPlans.hooks;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ParameterHooksWP {

    public void addFieldsForParameter(final DataDefinition dataDefinition, final Entity parameter) {
        parameter.setField("dontPrintOrdersInWorkPlans", false);
        parameter.setField("hideDescriptionInWorkPlans", false);
        parameter.setField("hideDetailsInWorkPlans", false);
        parameter.setField("hideTechnologyAndOrderInWorkPlans", false);
        parameter.setField("imageUrlInWorkPlan", false);
        parameter.setField("dontPrintInputProductsInWorkPlans", false);
        parameter.setField("dontPrintOutputProductsInWorkPlans", false);
    }
}
