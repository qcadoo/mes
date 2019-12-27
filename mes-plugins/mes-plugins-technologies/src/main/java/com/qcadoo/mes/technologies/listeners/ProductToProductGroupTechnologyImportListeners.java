package com.qcadoo.mes.technologies.listeners;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.imports.productToProductGroupTechnology.ProductToProductGroupTechnologyCellBinderRegistry;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductToProductGroupTechnologyImportListeners {

    @Autowired
    private XlsxImportService xlsxImportService;

    @Autowired
    private ProductToProductGroupTechnologyCellBinderRegistry productToProductGroupTechnologyCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        xlsxImportService.downloadImportSchema(view, TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_TO_PRODUCT_GROUP_TECHNOLOGY, XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        xlsxImportService.processImportFile(view, productToProductGroupTechnologyCellBinderRegistry.getCellBinderRegistry(), true,
                TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_TO_PRODUCT_GROUP_TECHNOLOGY);
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        xlsxImportService.redirectToLogs(view, TechnologiesConstants.MODEL_PRODUCT_TO_PRODUCT_GROUP_TECHNOLOGY);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        xlsxImportService.changeButtonsState(view, false);
    }
}
