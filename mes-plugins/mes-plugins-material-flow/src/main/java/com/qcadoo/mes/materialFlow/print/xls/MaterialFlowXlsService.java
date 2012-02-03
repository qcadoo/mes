/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.materialFlow.print.xls;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsUtil;

@Service
public final class MaterialFlowXlsService extends XlsDocumentService {

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(translationService.translate("materialFlow.materialFlow.report.columnHeader.number", locale));
        cell0.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(translationService.translate("materialFlow.materialFlow.report.columnHeader.name", locale));
        cell1.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(translationService.translate("materialFlow.materialFlow.report.columnHeader.quantity", locale));
        cell2.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(translationService.translate("materialFlow.materialFlow.report.columnHeader.unit", locale));
        cell3.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity materialsInStockAreas, final Locale locale) {
        Map<Entity, BigDecimal> reportData = materialFlowService.calculateMaterialQuantitiesInStockArea(materialsInStockAreas);

        int rowNum = 1;
        for (Map.Entry<Entity, BigDecimal> data : reportData.entrySet()) {
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getKey().getStringField("number"));
            row.createCell(1).setCellValue(data.getKey().getStringField("name"));
            row.createCell(2).setCellValue(numberService.getDecimalFormat(locale).format(data.getValue()));
            row.createCell(3).setCellValue(data.getKey().getStringField("unit"));
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return translationService.translate("materialFlow.materialFlow.report.title", locale);
    }
}
