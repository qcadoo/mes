package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ReleaseOfMaterials;
import com.qcadoo.view.api.ViewDefinitionState;

import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DetailedProductionCountingAndProgressListHooksBPC {

    public static final String L_ORDER_FORM = "order";

    public static final String L_ISSUE = "issue";

    public static final String L_RESOURCE_ISSUE = "resourceIssue";

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        String releaseOfMaterials = parameterService.getParameter().getStringField(ParameterFieldsPC.RELEASE_OF_MATERIALS);
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup issueRibbonGroup = ribbon.getGroupByName(L_ISSUE);
        RibbonActionItem resourceIssueRibbonActionItem = issueRibbonGroup.getItemByName(L_RESOURCE_ISSUE);
        if (!ReleaseOfMaterials.MANUALLY_TO_ORDER_OR_GROUP.getStringValue().equals(releaseOfMaterials)) {
            resourceIssueRibbonActionItem.setEnabled(false);
            resourceIssueRibbonActionItem
                    .setMessage("basicProductionCounting.detailedProductionCountingAndProgressList.resourceIssue.description");
            resourceIssueRibbonActionItem.requestUpdate(true);
        }
    }

}
