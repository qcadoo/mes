/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.6
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
package com.qcadoo.mes.technologies.print;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.print.utils.EntityOperationProductInOutComponentComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.ReportXlsView;
import com.qcadoo.report.api.xls.XlsUtil;

public class TechnologiesTechnologyDetailsXlsView extends ReportXlsView {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected String addContent(final Map<String, Object> model, final HSSFWorkbook workbook, final Locale locale) {
        HSSFSheet sheet = workbook.createSheet(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.title", locale));
        addOrderHeader(sheet, locale);
        addOrderSeries(model, sheet, locale);
        return getTranslationService().translate("technologies.technologiesTechnologyDetails.report.fileName", locale);
    }

    private void addOrderHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.name", locale));
        cell0.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.level", locale));
        cell1.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.direction", locale));
        cell2.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.product", locale));
        cell3.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell4 = header.createCell(4);
        cell4.setCellValue(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.quantity", locale));
        cell4.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell5 = header.createCell(5);
        cell5.setCellValue(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.unit", locale));
        cell5.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
    }

    private void addOrderSeries(final Map<String, Object> model, final HSSFSheet sheet, final Locale locale) {
        DataDefinition dataDefOperationProductInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
        DataDefinition dataDefOperationProductOutComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
        List<Entity> technologyDetailsTableContent = new ArrayList<Entity>();
        technologyDetailsTableContent.addAll(dataDefOperationProductInComponent
                .find("where operationComponent.technology.id = " + model.get("id").toString()).list().getEntities());
        technologyDetailsTableContent.addAll(technologyDetailsTableContent.size(),
                dataDefOperationProductOutComponent
                        .find("where operationComponent.technology.id = " + model.get("id").toString()).list().getEntities());
        Collections.sort(technologyDetailsTableContent, new EntityOperationProductInOutComponentComparator());

        int rowNum = 1;
        for (Entity e : technologyDetailsTableContent) {
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(
                    e.getBelongsToField("operationComponent").getBelongsToField("operation").getStringField("name"));
            row.createCell(1).setCellValue(
                    e.getBelongsToField("operationComponent").getBelongsToField("operation").getId().toString());
            if (e.getDataDefinition().getName().toString().equals("operationProductInComponent"))
                row.createCell(2).setCellValue(
                        getTranslationService().translate("technologies.technologiesTechnologyDetails.report.direction.in",
                                locale));
            else
                row.createCell(2).setCellValue(
                        getTranslationService().translate("technologies.technologiesTechnologyDetails.report.direction.out",
                                locale));
            row.createCell(3).setCellValue(e.getBelongsToField("product").getStringField("name"));
            row.createCell(4).setCellValue(e.getField("quantity").toString());
            row.createCell(5).setCellValue(e.getBelongsToField("product").getStringField("unit"));
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
        sheet.autoSizeColumn((short) 4);
        sheet.autoSizeColumn((short) 5);
    }
}
