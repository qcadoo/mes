package com.qcadoo.mes.materialFlowResources.print;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WarehouseStockReportService {

    public static final String GENERATION_DATE = "generationDate";

    @Autowired
    private WarehouseStockPdfReportService warehouseStockPdfReportService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    public void generateReport(final ComponentState state, final Entity report) throws IOException, DocumentException {
        warehouseStockPdfReportService.generateDocument(fileService.updateReportFileName(report, GENERATION_DATE, "materialFlowResources.warehouseStockReport.report.fileName"), state.getLocale());
    }

    public void printReport(final ViewDefinitionState view, final ComponentState state) {
        reportService.printGeneratedReport(view, state, new String[] { "pdf", MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_WAREHOUSE_STOCK_REPORT});
    }
}
