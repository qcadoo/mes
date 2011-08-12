package com.qcadoo.mes.inventory.print.xls;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.inventory.constants.InventoryConstants;
import com.qcadoo.mes.inventory.print.utils.EntityInventoryComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsUtil;

@Service
public final class InventoryXlsService extends XlsDocumentService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(getTranslationService().translate("inventory.inventory.report.columnHeader.number", locale));
        cell0.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(getTranslationService().translate("inventory.inventory.report.columnHeader.name", locale));
        cell1.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(getTranslationService().translate("inventory.inventory.report.columnHeader.quantity", locale));
        cell2.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(getTranslationService().translate("inventory.inventory.report.columnHeader.unit", locale));
        cell3.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity inventoryReport) {
        int rowNum = 1;
        DataDefinition dataDefInventory = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                InventoryConstants.MODEL_INVENTORY);
        List<Entity> inventories = dataDefInventory
                .find("where warehouse.id = " + Long.toString(inventoryReport.getBelongsToField("warehouse").getId())).list()
                .getEntities();
        Collections.sort(inventories, new EntityInventoryComparator());
        for (Entity e : inventories) {
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(e.getBelongsToField("product").getStringField("number"));
            row.createCell(1).setCellValue(e.getBelongsToField("product").getStringField("name"));
            row.createCell(2).setCellValue(((BigDecimal) e.getField("quantity")).toString());
            row.createCell(3).setCellValue(e.getBelongsToField("product").getStringField("unit"));
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("inventory.inventory.report.title", locale);
    }

    @Override
    protected String getSuffix() {
        return "";
    }
}
