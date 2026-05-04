package com.qcadoo.mes.productionCounting.listeners;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.imports.productionTracking.ProductionTrackingCellBinderRegistry;
import com.qcadoo.mes.productionCounting.imports.productionTracking.ProductionTrackingXlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TrackingsImportListeners {

    @Autowired
    private ProductionTrackingXlsxImportService productionTrackingXlsxImportService;

    @Autowired
    private ProductionTrackingCellBinderRegistry productionTrackingCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productionTrackingXlsxImportService.downloadImportSchema(view, ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING,
                XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        productionTrackingXlsxImportService.processImportFile(view, productionTrackingCellBinderRegistry.getCellBinderRegistry(), true,
                ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING, TrackingsImportListeners::createRestrictionForProductionTracking);
    }

    private static SearchCriterion createRestrictionForProductionTracking(final Entity productionTracking) {
        return SearchRestrictions.eq(ProductionTrackingFields.NUMBER, productionTracking.getStringField(ProductionTrackingFields.NUMBER));
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productionTrackingXlsxImportService.redirectToLogs(view, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productionTrackingXlsxImportService.changeButtonsState(view, false);
    }

}
