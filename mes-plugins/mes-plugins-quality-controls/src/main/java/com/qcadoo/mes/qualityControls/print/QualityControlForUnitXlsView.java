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
package com.qcadoo.mes.qualityControls.print;

import java.math.BigDecimal;
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

import com.qcadoo.mes.qualityControls.print.utils.EntityNumberComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.xls.ReportXlsView;
import com.qcadoo.report.api.xls.XlsUtil;

@Component(value = "qualityControlForUnitXlsView")
public class QualityControlForUnitXlsView extends ReportXlsView {

    @Autowired
    private QualityControlsReportService qualityControlsReportService;

    @Autowired
    private NumberService numberService;

    @Override
    protected final String addContent(final Map<String, Object> model, final HSSFWorkbook workbook, final Locale locale) {
        HSSFSheet sheet = workbook.createSheet(getTranslationService().translate(
                "qualityControls.qualityControlForUnit.report.title", locale));
        sheet.setZoom(4, 3);
        addOrderHeader(sheet, locale);
        addOrderSeries(model, sheet);
        return getTranslationService().translate("qualityControls.qualityControlForUnit.report.fileName", locale);
    }

    private void addOrderHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(getTranslationService().translate("qualityControls.qualityControl.report.product.number", locale));
        cell0.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(getTranslationService().translate(
                "qualityControls.qualityControlForUnitDetails.window.mainTab.qualityControlForUnit.number.label", locale));
        cell1.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(getTranslationService().translate(
                "qualityControls.qualityControlForUnitDetails.window.mainTab.qualityControlForUnit.controlledQuantity.label",
                locale));
        cell2.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(getTranslationService().translate(
                "qualityControls.qualityControlForUnitDetails.window.mainTab.qualityControlForUnit.rejectedQuantity.label",
                locale));
        cell3.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell4 = header.createCell(4);
        cell4.setCellValue(getTranslationService()
                .translate(
                        "qualityControls.qualityControlForUnitDetails.window.mainTab.qualityControlForUnit.acceptedDefectsQuantity.label",
                        locale));
        cell4.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
    }

    private void addOrderSeries(final Map<String, Object> model, final HSSFSheet sheet) {
        int rowNum = 1;
        Map<Entity, List<Entity>> productOrders = qualityControlsReportService
                .getQualityOrdersForProduct(qualityControlsReportService.getOrderSeries(model, "qualityControlsForUnit"));
        productOrders = SortUtil.sortMapUsingComparator(productOrders, new EntityNumberComparator());
        for (Entry<Entity, List<Entity>> entry : productOrders.entrySet()) {
            List<Entity> orders = entry.getValue();
            Collections.sort(orders, new EntityNumberComparator());
            for (Entity order : orders) {
                HSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey() == null ? "" : entry.getKey().getField("number").toString());
                row.createCell(1).setCellValue(order.getField("number").toString());
                row.createCell(2).setCellValue(
                        numberService.setScale((BigDecimal) order.getField("controlledQuantity")).doubleValue());
                row.createCell(3).setCellValue(
                        numberService.setScale((BigDecimal) order.getField("rejectedQuantity")).doubleValue());
                row.createCell(4).setCellValue(
                        numberService.setScale((BigDecimal) order.getField("acceptedDefectsQuantity")).doubleValue());
            }
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
        sheet.autoSizeColumn((short) 4);
    }

}
