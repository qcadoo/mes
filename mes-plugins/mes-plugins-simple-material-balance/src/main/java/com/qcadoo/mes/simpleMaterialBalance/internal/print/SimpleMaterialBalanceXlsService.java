package com.qcadoo.mes.simpleMaterialBalance.internal.print;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.inventory.InventoryService;
import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementReportDataServiceImpl;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsUtil;

@Service
public final class SimpleMaterialBalanceXlsService extends XlsDocumentService {

    @Autowired
    private MaterialRequirementReportDataServiceImpl materialRequirementReportDataService;

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number", locale));
        cell0.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name", locale));
        cell1.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.needed", locale));
        cell2.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.inStoch", locale));
        cell3.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell4 = header.createCell(4);
        cell4.setCellValue(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.balance", locale));
        cell4.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity simpleMaterialBalance) {
        int rowNum = 1;
        List<Entity> orders = simpleMaterialBalance.getHasManyField("orders");
        Boolean onlyComponents = (Boolean) simpleMaterialBalance.getField("onlyComponents");
        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForMaterialRequirementProducts(
                orders, onlyComponents);
        List<Entity> warehouses = simpleMaterialBalance.getHasManyField("warehouses");
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey().getField("number").toString());
            row.createCell(1).setCellValue(entry.getKey().getField("name").toString());
            row.createCell(2).setCellValue(getDecimalFormat().format(entry.getValue()));
            BigDecimal available = BigDecimal.ZERO;
            for (Entity warehouse : warehouses) {
                available = available.add(inventoryService.calculateShouldBe(warehouse.getBelongsToField("warehouse").getId()
                        .toString(), entry.getKey().getId().toString(), simpleMaterialBalance.getField("date").toString()));
            }
            row.createCell(3).setCellValue(getDecimalFormat().format(available));
            row.createCell(4).setCellValue(getDecimalFormat().format(available.subtract(entry.getValue())));
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
        sheet.autoSizeColumn((short) 4);
    }

    @Override
    protected String getSuffix() {
        return "";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
    }

}
