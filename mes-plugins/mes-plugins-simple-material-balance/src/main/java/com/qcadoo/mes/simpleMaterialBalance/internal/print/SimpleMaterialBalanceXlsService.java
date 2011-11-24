/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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

import com.qcadoo.mes.materialFlow.MaterialFlowService;
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
    private MaterialFlowService materialFlowService;

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
        cell2.setCellValue(getTranslationService().translate("basic.product.unit.label", locale));
        cell2.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.needed", locale));
        cell3.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell4 = header.createCell(4);
        cell4.setCellValue(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.inStoch", locale));
        cell4.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell5 = header.createCell(5);
        cell5.setCellValue(getTranslationService().translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.balance", locale));
        cell5.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity simpleMaterialBalance) {
        int rowNum = 1;
        List<Entity> orders = simpleMaterialBalance.getHasManyField("orders");
        Boolean onlyComponents = (Boolean) simpleMaterialBalance.getField("onlyComponents");
        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForMaterialRequirementProducts(
                orders, onlyComponents);
        List<Entity> stockAreass = simpleMaterialBalance.getHasManyField("stockAreas");
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getKey().getField("number").toString());
            row.createCell(1).setCellValue(product.getKey().getField("name").toString());
            row.createCell(2).setCellValue(product.getKey().getField("unit").toString());
            row.createCell(3).setCellValue(getDecimalFormat().format(product.getValue()));
            BigDecimal available = BigDecimal.ZERO;
            for (Entity stockAreas : stockAreass) {
                available = available.add(materialFlowService.calculateShouldBeInStockArea(
                        stockAreas.getBelongsToField("stockAreas").getId(), product.getKey().getId().toString(),
                        simpleMaterialBalance.getField("date").toString()));
            }
            row.createCell(4).setCellValue(getDecimalFormat().format(available));
            row.createCell(5).setCellValue(getDecimalFormat().format(available.subtract(product.getValue())));
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
        sheet.autoSizeColumn((short) 4);
        sheet.autoSizeColumn((short) 5);
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
