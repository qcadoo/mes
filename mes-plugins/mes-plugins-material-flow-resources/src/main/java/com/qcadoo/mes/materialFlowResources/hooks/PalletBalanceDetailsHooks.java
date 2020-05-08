package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PalletBalanceFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PalletBalanceDetailsHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_PALLET_BALANCE, QcadooViewConstants.L_FORM, PalletBalanceFields.NUMBER);

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity palletBalance = form.getPersistedEntityWithIncludedFormValues();
        boolean generated = palletBalance.getBooleanField(PalletBalanceFields.GENERATED);

        Date dateTo = palletBalance.getDateField(PalletBalanceFields.DATE_TO);
        if (dateTo == null) {
            Date dateFrom = palletBalance.getDateField(PalletBalanceFields.DATE_FROM);
            if (dateFrom == null) {
                palletBalance.setField(PalletBalanceFields.DATE_FROM, new Date());
            }
            palletBalance.setField(PalletBalanceFields.DATE_TO, new Date());
            form.setEntity(palletBalance);
        }
        changeRibbonState(view, generated);
        form.setFormEnabled(!generated);
    }

    private void changeRibbonState(final ViewDefinitionState view, final boolean generated) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup reportGroup = window.getRibbon().getGroupByName("report");
        RibbonActionItem generate = reportGroup.getItemByName("generate");
        RibbonActionItem print = reportGroup.getItemByName("print");

        generate.setEnabled(!generated);
        generate.requestUpdate(true);
        print.setEnabled(generated);
        print.requestUpdate(true);
    }

}
