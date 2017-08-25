package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.materialFlowResources.constants.WarehouseStockReportFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class WarehouseStockReportDetailsListeners {

    private static final String L_FORM = "form";

    public void generate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save", new String[0]);

        if (state.isHasError()) {
            return;
        }

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity report = form.getEntity();
        Entity reportDb = report.getDataDefinition().get(report.getId());
        reportDb.setField(WarehouseStockReportFields.GENERATED, Boolean.TRUE);
        reportDb.setField("generationDate", new Date());
        reportDb = reportDb.getDataDefinition().save(reportDb);
        state.performEvent(view, "reset", new String[0]);

    }

    public void print(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }
}
