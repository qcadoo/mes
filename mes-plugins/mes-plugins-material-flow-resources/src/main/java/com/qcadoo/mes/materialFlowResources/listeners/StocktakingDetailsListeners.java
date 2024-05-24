package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.print.StocktakingReportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StocktakingDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(StocktakingDetailsListeners.class);

    @Autowired
    private StocktakingReportService reportService;

    public void generate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save");

        if (state.isHasError()) {
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity report = form.getEntity();
        Entity reportDb = report.getDataDefinition().get(report.getId());
        reportDb.setField(StocktakingFields.GENERATED, Boolean.TRUE);
        reportDb.setField("generationDate", new Date());
        reportDb = reportDb.getDataDefinition().save(reportDb);
        try {
            reportService.generateReport(state, reportDb);
        } catch (Exception e) {
            LOG.error("Error when generate stocktaking report", e);
            throw new IllegalStateException(e.getMessage(), e);
        }
        state.performEvent(view, "reset", new String[0]);

    }

    public void print(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printReport(view, state);
    }

}
