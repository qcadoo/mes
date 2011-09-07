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
package com.qcadoo.mes.inventory.print.xls;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.inventory.InventoryService;
import com.qcadoo.mes.inventory.constants.InventoryConstants;
import com.qcadoo.mes.inventory.print.utils.EntityTransferComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsUtil;

@Service
public final class InventoryXlsService extends XlsDocumentService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private InventoryService inventoryService;

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

        DataDefinition dataDefTransfer = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                InventoryConstants.MODEL_TRANSFER);
        List<Entity> transfers = dataDefTransfer
                .find("where warehouseTo.id = " + Long.toString(inventoryReport.getBelongsToField("warehouse").getId())).list()
                .getEntities();
        Collections.sort(transfers, new EntityTransferComparator());

        String warehouseNumber = inventoryReport.getBelongsToField("warehouse").getId().toString();
        String forDate = ((Date) inventoryReport.getField("inventoryForDate")).toString();

        String numberBefore = "";
        for (Entity e : transfers) {
            String numberNow = e.getBelongsToField("product").getStringField("number");

            if (!numberBefore.equals(numberNow)) {
                BigDecimal quantity = inventoryService.calculateShouldBe(warehouseNumber, e.getBelongsToField("product")
                        .getStringField("number"), forDate);

                HSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.getBelongsToField("product").getStringField("number"));
                row.createCell(1).setCellValue(e.getBelongsToField("product").getStringField("name"));
                row.createCell(2).setCellValue(quantity.toString());
                row.createCell(3).setCellValue(e.getBelongsToField("product").getStringField("unit"));
                numberBefore = numberNow;
            }
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
