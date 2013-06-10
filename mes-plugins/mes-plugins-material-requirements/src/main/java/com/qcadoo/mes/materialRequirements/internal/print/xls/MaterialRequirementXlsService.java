/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.materialRequirements.internal.print.xls;

import static com.qcadoo.mes.materialRequirements.internal.constants.MaterialRequirementFields.MRP_ALGORITHM;

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

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsHelper;

@Service
public final class MaterialRequirementXlsService extends XlsDocumentService {

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private XlsHelper xlsHelper;

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale, final Entity entity) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(translationService.translate("basic.product.number.label", locale));
        xlsHelper.setCellStyle(sheet, cell0);
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(translationService.translate("basic.product.name.label", locale));
        xlsHelper.setCellStyle(sheet, cell1);
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(translationService.translate("technologies.technologyOperationComponent.quantity.label", locale));
        xlsHelper.setCellStyle(sheet, cell2);
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(translationService.translate("basic.product.unit.label", locale));
        xlsHelper.setCellStyle(sheet, cell3);
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {
        int rowNum = 1;
        List<Entity> orders = entity.getManyToManyField("orders");
        MrpAlgorithm algorithm = MrpAlgorithm.parseString(entity.getStringField(MRP_ALGORITHM));

        Map<Long, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(orders, algorithm,
                true);

        // TODO LUPO fix comparator
        // neededProductQuantities = SortUtil.sortMapUsingComparator(neededProductQuantities, new EntityNumberComparator());

        for (Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());

            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getField("number").toString());
            row.createCell(1).setCellValue(product.getField("name").toString());
            row.createCell(2).setCellValue(numberService.setScale(neededProductQuantity.getValue()).doubleValue());
            Object unit = product.getField("unit");
            if (unit == null) {
                row.createCell(3).setCellValue("");
            } else {
                row.createCell(3).setCellValue(unit.toString());
            }
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("materialRequirements.materialRequirement.report.title", locale);
    }
}
