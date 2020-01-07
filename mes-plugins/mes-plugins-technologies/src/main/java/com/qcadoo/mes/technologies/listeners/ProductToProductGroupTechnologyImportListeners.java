package com.qcadoo.mes.technologies.listeners;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.imports.productToProductGroupTechnology.ProductToProductGroupTechnologyCellBinderRegistry;
import com.qcadoo.mes.technologies.imports.productToProductGroupTechnology.ProductToProductGroupTechnologyXlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductToProductGroupTechnologyImportListeners {

    @Autowired
    private ProductToProductGroupTechnologyXlsxImportService productToProductGroupTechnologyXlsxImportService;

    @Autowired
    private ProductToProductGroupTechnologyCellBinderRegistry productToProductGroupTechnologyCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productToProductGroupTechnologyXlsxImportService.downloadImportSchema(view, TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_TO_PRODUCT_GROUP_TECHNOLOGY, XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        productToProductGroupTechnologyXlsxImportService.processImportFile(view,
                productToProductGroupTechnologyCellBinderRegistry.getCellBinderRegistry(), true,
                TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_TO_PRODUCT_GROUP_TECHNOLOGY);
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productToProductGroupTechnologyXlsxImportService.redirectToLogs(view,
                TechnologiesConstants.MODEL_PRODUCT_TO_PRODUCT_GROUP_TECHNOLOGY);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productToProductGroupTechnologyXlsxImportService.changeButtonsState(view, false);
    }
}
