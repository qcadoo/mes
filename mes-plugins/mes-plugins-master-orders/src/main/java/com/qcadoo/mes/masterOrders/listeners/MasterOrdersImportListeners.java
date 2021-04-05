package com.qcadoo.mes.masterOrders.listeners;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.imports.masterOrder.MasterOrderCellBinderRegistry;
import com.qcadoo.mes.masterOrders.imports.masterOrder.MasterOrderXlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class MasterOrdersImportListeners {

    @Autowired
    private MasterOrderXlsxImportService masterOrderXlsxImportService;

    @Autowired
    private MasterOrderCellBinderRegistry masterOrderCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderXlsxImportService.downloadImportSchema(view, MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER, XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        masterOrderXlsxImportService.processImportFile(view, masterOrderCellBinderRegistry.getCellBinderRegistry(), true,
                MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER,
                MasterOrdersImportListeners::createRestrictionForMasterOrder);
    }

    public static SearchCriterion createRestrictionForMasterOrder(final Entity masterOrder) {
        return SearchRestrictions.eq(MasterOrderFields.NUMBER, masterOrder.getStringField(MasterOrderFields.NUMBER));
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderXlsxImportService.redirectToLogs(view, MasterOrdersConstants.MODEL_MASTER_ORDER);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderXlsxImportService.changeButtonsState(view, false);
    }

}
