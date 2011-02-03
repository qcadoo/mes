/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.products.print.xls;

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

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.products.print.ReportDataService;
import com.qcadoo.mes.products.util.EntityNumberComparator;
import com.qcadoo.mes.products.util.SortUtil;
import com.qcadoo.mes.utils.xls.XlsUtil;

@Service
public final class MaterialRequirementXlsService extends XlsDocumentService {

    @Autowired
    private ReportDataService reportDataService;

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(getTranslationService().translate("products.product.number.label", locale));
        cell0.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(getTranslationService().translate("products.product.name.label", locale));
        cell1.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(getTranslationService().translate("products.technologyOperationComponent.quantity.label", locale));
        cell2.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(getTranslationService().translate("products.product.unit.label", locale));
        cell3.setCellStyle(XlsUtil.getHeaderStyle(sheet.getWorkbook()));
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {
        int rowNum = 1;
        List<Entity> orders = entity.getHasManyField("orders");
        Map<Entity, BigDecimal> products = reportDataService.getTechnologySeries(entity, orders);
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> entry : products.entrySet()) {
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey().getField("number").toString());
            row.createCell(1).setCellValue(entry.getKey().getField("name").toString());
            row.createCell(2).setCellValue(entry.getValue().doubleValue());
            Object unit = entry.getKey().getField("unit");
            if (unit != null) {
                row.createCell(3).setCellValue(unit.toString());
            } else {
                row.createCell(3).setCellValue("");
            }
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("products.materialRequirement.report.title", locale);
    }

    @Override
    protected String getSuffix() {
        return "";
    }
}
