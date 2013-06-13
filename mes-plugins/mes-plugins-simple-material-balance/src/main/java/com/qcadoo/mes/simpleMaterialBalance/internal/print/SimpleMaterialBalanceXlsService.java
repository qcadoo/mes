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
package com.qcadoo.mes.simpleMaterialBalance.internal.print;

import java.math.BigDecimal;
import java.util.Date;
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
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsHelper;

@Service
public final class SimpleMaterialBalanceXlsService extends XlsDocumentService {

    private static final String L_NUMBER = "number";

    private static final String L_NAME = "name";

    private static final String L_DATE = "date";

    private static final String L_SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS = "simpleMaterialBalanceOrdersComponents";

    private static final String L_SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS = "simpleMaterialBalanceLocationsComponents";

    private static final String L_LOCATION = "location";

    private static final String L_UNIT = "unit";

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private XlsHelper xlsHelper;

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale, final Entity entity) {
        HSSFRow header = sheet.createRow(0);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.number",
                locale));
        xlsHelper.setCellStyle(sheet, cell0);
        HSSFCell cell1 = header.createCell(1);
        cell1.setCellValue(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.name",
                locale));
        xlsHelper.setCellStyle(sheet, cell1);
        HSSFCell cell2 = header.createCell(2);
        cell2.setCellValue(translationService.translate("basic.product.unit.label", locale));
        xlsHelper.setCellStyle(sheet, cell2);
        HSSFCell cell3 = header.createCell(3);
        cell3.setCellValue(translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.needed",
                locale));
        xlsHelper.setCellStyle(sheet, cell3);
        HSSFCell cell4 = header.createCell(4);
        cell4.setCellValue(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.inLocation", locale));
        xlsHelper.setCellStyle(sheet, cell4);
        HSSFCell cell5 = header.createCell(5);
        cell5.setCellValue(translationService.translate(
                "simpleMaterialBalance.simpleMaterialBalance.report.columnHeader.balance", locale));
        xlsHelper.setCellStyle(sheet, cell5);
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity simpleMaterialBalance) {
        int rowNum = 1;
        List<Entity> simpleMaterialBalanceOrdersComponents = simpleMaterialBalance
                .getHasManyField(L_SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS);
        MrpAlgorithm mrpAlgorithm = MrpAlgorithm.parseString(simpleMaterialBalance.getStringField("mrpAlgorithm"));

        Map<Long, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantitiesForComponents(
                simpleMaterialBalanceOrdersComponents, mrpAlgorithm);

        List<Entity> simpleMaterialBalanceLocationComponents = simpleMaterialBalance
                .getHasManyField(L_SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS);

        // TODO LUPO fix comparator
        // neededProductQuantities = SortUtil.sortMapUsingComparator(neededProductQuantities, new EntityNumberComparator());

        for (Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());

            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getField(L_NUMBER).toString());
            row.createCell(1).setCellValue(product.getField(L_NAME).toString());
            row.createCell(2).setCellValue(product.getField(L_UNIT).toString());
            row.createCell(3).setCellValue(numberService.format(neededProductQuantity.getValue()));
            BigDecimal available = BigDecimal.ZERO;
            for (Entity simpleMaterialBalanceLocationComponent : simpleMaterialBalanceLocationComponents) {
                available = available.add(materialFlowService.calculateShouldBeInLocation(simpleMaterialBalanceLocationComponent
                        .getBelongsToField(L_LOCATION).getId(), product.getId(), (Date) simpleMaterialBalance.getField(L_DATE)),
                        numberService.getMathContext());
            }
            row.createCell(4).setCellValue(numberService.format(available));
            row.createCell(5)
                    .setCellValue(
                            numberService.format(available.subtract(neededProductQuantity.getValue(),
                                    numberService.getMathContext())));
        }
        sheet.autoSizeColumn((short) 0);
        sheet.autoSizeColumn((short) 1);
        sheet.autoSizeColumn((short) 2);
        sheet.autoSizeColumn((short) 3);
        sheet.autoSizeColumn((short) 4);
        sheet.autoSizeColumn((short) 5);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
    }

}
