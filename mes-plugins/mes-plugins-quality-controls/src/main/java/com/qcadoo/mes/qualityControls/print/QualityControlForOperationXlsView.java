/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.qualityControls.print;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.qualityControls.print.utils.EntityNumberComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.xls.ReportXlsView;
import com.qcadoo.report.api.xls.XlsHelper;

@Component(value = "qualityControlForOperationXlsView")
public class QualityControlForOperationXlsView extends ReportXlsView {

    @Autowired
    private QualityControlsReportService qualityControlsReportService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private XlsHelper xlsHelper;

    @Override
    protected final String addContent(final Map<String, Object> model, final HSSFWorkbook workbook, final Locale locale) {
        HSSFSheet sheet = workbook.createSheet(translationService.translate(
                "qualityControls.qualityControlForOperation.report.title", locale));
        sheet.setZoom(4, 3);
        addOrderHeader(sheet, locale);
        addOrderSeries(model, sheet, locale);
        return translationService.translate("qualityControls.qualityControlForOperation.report.fileName", locale);
    }

    private void addOrderHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(translationService.translate("qualityControls.qualityControl.report.operation.number", locale));
        xlsHelper.setCellStyle(sheet, cell0);
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(translationService
                .translate(
                        "qualityControlsForOperation.qualityControlForOperationDetails.window.mainTab.qualityControlForOperation.number.label",
                        locale));
        xlsHelper.setCellStyle(sheet, cell1);
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(translationService
                .translate(
                        "qualityControlsForOperation.qualityControlForOperationDetails.window.mainTab.qualityControlForOperation.controlResult.label",
                        locale));
        xlsHelper.setCellStyle(sheet, cell2);
    }

    private void addOrderSeries(final Map<String, Object> model, final HSSFSheet sheet, final Locale locale) {
        int rowNum = 1;
        Map<Entity, List<Entity>> operationOrders = qualityControlsReportService
                .getQualityOrdersForOperation(qualityControlsReportService.getOrderSeries(model, "qualityControlsForOperation"));
        operationOrders = SortUtil.sortMapUsingComparator(operationOrders, new EntityNumberComparator());
        for (Entry<Entity, List<Entity>> entry : operationOrders.entrySet()) {
            List<Entity> orders = entry.getValue();
            Collections.sort(orders, new EntityNumberComparator());
            for (Entity order : orders) {
                HSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey() == null ? "" : entry.getKey().getField("nodeNumber").toString());
                row.createCell(1).setCellValue(order.getField("number").toString());
                String result = "";
                if ("01correct".equals(order.getField("controlResult"))) {
                    result = translationService.translate("qualityControls.qualityControl.controlResult.value.01correct", locale);
                } else if ("02incorrect".equals(order.getField("controlResult"))) {
                    result = translationService.translate("qualityControls.qualityControl.controlResult.value.02incorrect",
                            locale);
                } else if ("03objection".equals(order.getField("controlResult"))) {
                    result = translationService.translate("qualityControls.qualityControl.controlResult.value.03objection",
                            locale);
                }
                row.createCell(2).setCellValue(result);
            }
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
    }

}
