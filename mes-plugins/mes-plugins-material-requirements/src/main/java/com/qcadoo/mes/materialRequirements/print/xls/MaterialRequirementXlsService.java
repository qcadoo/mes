/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.materialRequirements.print.xls;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.materialRequirements.print.MaterialRequirementDataService;
import com.qcadoo.mes.materialRequirements.print.MaterialRequirementEntry;
import com.qcadoo.mes.materialRequirements.print.WarehouseDateKey;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsHelper;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public final class MaterialRequirementXlsService extends XlsDocumentService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private XlsHelper xlsHelper;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialRequirementDataService materialRequirementDataService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale, final Entity materialRequirement) {
        boolean includeWarehouse = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        boolean includeStartDateOrder = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);
        boolean showCurrentStockLevel = materialRequirement.getBooleanField(MaterialRequirementFields.SHOW_CURRENT_STOCK_LEVEL);

        int column = 0;

        HSSFRow header = sheet.createRow(0);

        if (includeWarehouse) {
            HSSFCell cellWarehouse = header.createCell(column);
            cellWarehouse.setCellValue(
                    translationService.translate("materialRequirements.materialRequirement.report.warehouse", locale));
            xlsHelper.setCellStyle(sheet, cellWarehouse);
            column += 1;
        }

        if (includeStartDateOrder) {
            HSSFCell cellStartDateOrder = header.createCell(column);
            cellStartDateOrder.setCellValue(
                    translationService.translate("materialRequirements.materialRequirement.report.startDateOrder", locale));
            xlsHelper.setCellStyle(sheet, cellStartDateOrder);
            column += 1;
        }

        HSSFCell cell0 = header.createCell(column);
        cell0.setCellValue(translationService.translate("basic.product.number.label", locale));
        xlsHelper.setCellStyle(sheet, cell0);
        column += 1;

        HSSFCell cell1 = header.createCell(column);
        cell1.setCellValue(translationService.translate("basic.product.name.label", locale));
        xlsHelper.setCellStyle(sheet, cell1);
        column += 1;

        HSSFCell cell2 = header.createCell(column);
        cell2.setCellValue(translationService.translate("technologies.technologyOperationComponent.quantity.label", locale));
        xlsHelper.setCellStyle(sheet, cell2);
        column += 1;

        HSSFCell cell3 = header.createCell(column);
        cell3.setCellValue(translationService.translate("basic.product.unit.label", locale));
        xlsHelper.setCellStyle(sheet, cell3);
        column += 1;

        if (showCurrentStockLevel) {
            HSSFCell cellStockLevel = header.createCell(column);
            cellStockLevel.setCellValue(
                    translationService.translate("materialRequirements.materialRequirement.report.currentStock", locale));
            xlsHelper.setCellStyle(sheet, cellStockLevel);
        }
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity materialRequirement) {
        boolean includeWarehouse = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        boolean includeStartDateOrder = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);

        if (includeWarehouse || includeStartDateOrder) {
            addGroupedDataSeries(sheet, materialRequirement);
        } else {
            addDataSeries(sheet, materialRequirement);
        }
    }

    private void addGroupedDataSeries(final HSSFSheet sheet, final Entity materialRequirement) {
        Map<WarehouseDateKey, List<MaterialRequirementEntry>> materialRequirementEntriesMap = materialRequirementDataService
                .getGroupedData(materialRequirement);

        boolean includeWarehouse = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        boolean includeStartDateOrder = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);
        boolean showCurrentStockLevel = materialRequirement.getBooleanField(MaterialRequirementFields.SHOW_CURRENT_STOCK_LEVEL);

        int rowNum = 1;

        List<WarehouseDateKey> warehouseDateKeys = Lists.newArrayList(materialRequirementEntriesMap.keySet());

        if (includeWarehouse) {
            warehouseDateKeys.sort(Comparator.comparing(WarehouseDateKey::getWarehouseNumber).thenComparing(WarehouseDateKey::getDate));
        } else {
            warehouseDateKeys.sort(Comparator.comparing(WarehouseDateKey::getDate));
        }

        Map<Long, Map<Long, BigDecimal>> quantitiesInStock = Maps.newHashMap();

        if (showCurrentStockLevel) {
            List<MaterialRequirementEntry> materialRequirementEntries = Lists.newArrayList();

            for (WarehouseDateKey warehouseDateKey : warehouseDateKeys) {
                if (Objects.nonNull(warehouseDateKey.getWarehouseId())) {
                    materialRequirementEntries.addAll(materialRequirementEntriesMap.get(warehouseDateKey));
                }
            }

            quantitiesInStock = materialRequirementDataService.getQuantitiesInStock(materialRequirementEntries);
        }

        String actualWarehouse = "";
        Date actualDate = null;

        int column = 0;
        for (WarehouseDateKey warehouseDateKey : warehouseDateKeys) {
            List<MaterialRequirementEntry> materialRequirementEntries = materialRequirementEntriesMap.get(warehouseDateKey);
            Map<String, MaterialRequirementEntry> neededProductQuantities = materialRequirementDataService.getNeededProductQuantities(materialRequirementEntries);
            List<String> materialKeys = Lists.newArrayList(neededProductQuantities.keySet());

            materialKeys.sort(Comparator.naturalOrder());

            for (String materialKey : materialKeys) {
                HSSFRow row = sheet.createRow(rowNum++);

                column = 0;

                MaterialRequirementEntry materialRequirementEntry = neededProductQuantities.get(materialKey);

                boolean fillDateIfWarehouseChanged = false;

                if (includeWarehouse) {
                    if (!actualWarehouse.equals(warehouseDateKey.getWarehouseNumber())) {
                        row.createCell(column).setCellValue(warehouseDateKey.getWarehouseNumber());

                        actualWarehouse = warehouseDateKey.getWarehouseNumber();

                        fillDateIfWarehouseChanged = true;
                    } else {
                        row.createCell(column).setCellValue("");
                    }

                    column += 1;
                }

                if (includeStartDateOrder) {
                    Date date = warehouseDateKey.getDate();

                    if (Objects.isNull(actualDate) || !actualDate.equals(date) || fillDateIfWarehouseChanged) {
                        if (Objects.isNull(date)) {
                            actualDate = null;

                            row.createCell(column).setCellValue("");
                        } else {
                            actualDate = new Date(date.getTime());

                            row.createCell(column).setCellValue(DateUtils.toDateString(actualDate));
                        }
                    } else {
                        row.createCell(column).setCellValue("");
                    }

                    column += 1;
                }

                row.createCell(column).setCellValue(materialRequirementEntry.getNumber());
                column += 1;
                row.createCell(column).setCellValue(materialRequirementEntry.getName());
                column += 1;

                row.createCell(column)
                        .setCellValue(numberService.setScaleWithDefaultMathContext(materialRequirementEntry.getPlannedQuantity()).doubleValue());
                column += 1;

                String unit = materialRequirementEntry.getUnit();
                if (Objects.isNull(unit)) {
                    row.createCell(column).setCellValue("");
                } else {
                    row.createCell(column).setCellValue(unit);
                }

                column += 1;

                if (showCurrentStockLevel) {
                    if (Objects.nonNull(warehouseDateKey.getWarehouseId())) {
                        row.createCell(column).setCellValue(
                                numberService.format(materialRequirementDataService.getQuantity(quantitiesInStock, materialRequirementEntry)));
                    } else {
                        row.createCell(column).setCellValue(numberService.format(BigDecimal.ZERO));
                    }
                }
            }
        }

        for (int i = 0; i < column; i++) {
            sheet.autoSizeColumn((short) i);
        }
    }

    private void addDataSeries(final HSSFSheet sheet, final Entity materialRequirement) {
        int rowNum = 1;

        List<Entity> orders = materialRequirement.getManyToManyField(MaterialRequirementFields.ORDERS);

        MrpAlgorithm algorithm = MrpAlgorithm
                .parseString(materialRequirement.getStringField(MaterialRequirementFields.MRP_ALGORITHM));

        Map<Long, BigDecimal> neededProductQuantities = basicProductionCountingService.getNeededProductQuantities(orders,
                algorithm);

        List<Entity> products = getProductDD().find().add(SearchRestrictions.in("id", neededProductQuantities.keySet()))
                .list().getEntities();

        products.sort(Comparator.comparing(product -> product.getStringField(ProductFields.NUMBER)));

        for (Entity product : products) {
            HSSFRow row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(product.getStringField(ProductFields.NUMBER));
            row.createCell(1).setCellValue(product.getStringField(ProductFields.NAME));
            row.createCell(2).setCellValue(
                    numberService.setScaleWithDefaultMathContext(neededProductQuantities.get(product.getId())).doubleValue());

            String unit = product.getStringField(ProductFields.UNIT);
            if (Objects.isNull(unit)) {
                row.createCell(3).setCellValue("");
            } else {
                row.createCell(3).setCellValue(unit);
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

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
