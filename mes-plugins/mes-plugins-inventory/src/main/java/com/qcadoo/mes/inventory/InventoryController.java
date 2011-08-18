package com.qcadoo.mes.inventory;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.inventory.constants.InventoryConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportUtil;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.xls.XlsUtil;

@Controller
public class InventoryController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "inventory/inventoryReport.pdf", method = RequestMethod.GET)
    public void inventoryReportPdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                InventoryConstants.MODEL_INVENTORY_REPORT);
        Entity inventoryReport = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName(inventoryReport,
                translationService.translate("inventory.inventory.report.fileName", locale), "", PdfUtil.PDF_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(inventoryReport.getStringField("fileName") + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "inventory/inventoryReport.xls", method = RequestMethod.GET)
    public void inventoryReportXls(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                InventoryConstants.MODEL_INVENTORY_REPORT);
        Entity inventory = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName(inventory, translationService.translate("inventory.inventory.report.fileName", locale),
                "", XlsUtil.XLS_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(inventory.getStringField("fileName") + XlsUtil.XLS_EXTENSION,
                ReportUtil.XLS_CONTENT_TYPE, response);
    }
}
