/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.model.api.types.TreeType.NODE_NUMBER_FIELD;
import static java.lang.Long.valueOf;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.report.api.xls.ReportXlsView;
import com.qcadoo.report.api.xls.XlsHelper;

@Component(value = "technologiesTechnologyDetailsXlsView")
public class TechnologiesTechnologyDetailsXlsView extends ReportXlsView {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private XlsHelper xlsHelper;

    @Override
    protected String addContent(final Map<String, Object> model, final HSSFWorkbook workbook, final Locale locale) {
        HSSFSheet sheet = workbook.createSheet(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.title", locale));
        addOrderHeader(sheet, locale);
        addOrderSeries(model, sheet, locale);
        return translationService.translate("technologies.technologiesTechnologyDetails.report.fileName", locale);
    }

    private void addOrderHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        int columnCounter = 0;
        for (String headerText : newArrayList("level", "name", "direction", "product", "quantity", "unit")) {
            HSSFCell cell = header.createCell(columnCounter);
            cell.setCellValue(translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader."
                    + headerText, locale));
            xlsHelper.setCellStyle(sheet, cell);
            columnCounter++;
        }
    }

    private void addOrderSeries(final Map<String, Object> model, final HSSFSheet sheet, final Locale locale) {
        checkState(model.get("id") != null, "Unable to generate report for unsaved technology! (missing id)");

        DataDefinition technologyDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY);
        Entity technology = technologyDD.get(valueOf(model.get("id").toString()));
        EntityTree technologyTree = technology.getTreeField("operationComponents");
        treeNumberingService.generateTreeNumbers(technologyTree);

        List<Entity> technologyOperationsList = entityTreeUtilsService.getSortedEntities(technologyTree);

        int rowNum = 1;
        for (Entity technologyOperation : technologyOperationsList) {
            String nodeNumber = technologyOperation.getStringField(NODE_NUMBER_FIELD);
            String operationName = technologyOperation.getBelongsToField("operation").getStringField("name");
            List<Entity> technologyOperationProducts = Lists.newLinkedList();
            technologyOperationProducts.addAll(technologyOperation.getHasManyField("operationProductInComponents"));
            technologyOperationProducts.addAll(technologyOperation.getHasManyField("operationProductOutComponents"));

            for (Entity product : technologyOperationProducts) {
                HSSFRow row = sheet.createRow(rowNum++);
                String productType = "technologies.technologiesTechnologyDetails.report.direction.out";
                if (product.getDataDefinition().getName().equals("operationProductInComponent")) {
                    productType = "technologies.technologiesTechnologyDetails.report.direction.in";
                }
                row.createCell(0).setCellValue(nodeNumber);
                row.createCell(1).setCellValue(operationName);
                row.createCell(2).setCellValue(translationService.translate(productType, locale));
                row.createCell(3).setCellValue(product.getBelongsToField("product").getStringField("name"));
                row.createCell(4).setCellValue(product.getField("quantity").toString());
                row.createCell(5).setCellValue(product.getBelongsToField("product").getStringField("unit"));
            }

        }

        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
        sheet.autoSizeColumn((short) 4);
        sheet.autoSizeColumn((short) 5);
    }
}
