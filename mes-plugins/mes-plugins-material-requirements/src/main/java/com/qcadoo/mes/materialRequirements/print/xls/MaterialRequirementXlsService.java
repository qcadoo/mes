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
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementProductFields;
import com.qcadoo.mes.materialRequirements.print.MaterialRequirementDataService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsHelper;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

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
                    translationService.translate("materialRequirements.materialRequirement.report.locationNumber", locale));
            xlsHelper.setCellStyle(sheet, cellWarehouse);
            column += 1;
        }

        if (includeStartDateOrder) {
            HSSFCell cellStartDateOrder = header.createCell(column);
            cellStartDateOrder.setCellValue(
                    translationService.translate("materialRequirements.materialRequirement.report.orderStartDate", locale));
            xlsHelper.setCellStyle(sheet, cellStartDateOrder);
            column += 1;
        }

        HSSFCell cellProductNumber = header.createCell(column);
        cellProductNumber.setCellValue(translationService.translate("materialRequirements.materialRequirement.report.productNumber", locale));
        xlsHelper.setCellStyle(sheet, cellProductNumber);
        column += 1;

        HSSFCell cellProductName = header.createCell(column);
        cellProductName.setCellValue(translationService.translate("materialRequirements.materialRequirement.report.productName", locale));
        xlsHelper.setCellStyle(sheet, cellProductName);
        column += 1;

        HSSFCell cellQuantity = header.createCell(column);
        cellQuantity.setCellValue(translationService.translate("materialRequirements.materialRequirement.report.quantity", locale));
        xlsHelper.setCellStyle(sheet, cellQuantity);
        column += 1;

        HSSFCell cell3 = header.createCell(column);
        cell3.setCellValue(translationService.translate("materialRequirements.materialRequirement.report.product.unit", locale));
        xlsHelper.setCellStyle(sheet, cell3);
        column += 1;

        if (showCurrentStockLevel) {
            HSSFCell cellStockLevel = header.createCell(column);
            cellStockLevel.setCellValue(
                    translationService.translate("materialRequirements.materialRequirement.report.currentStock", locale));
            xlsHelper.setCellStyle(sheet, cellStockLevel);
            column += 1;
        }

        HSSFCell cellBatch = header.createCell(column);
        cellBatch.setCellValue(translationService.translate("materialRequirements.materialRequirement.report.batch", locale));
        xlsHelper.setCellStyle(sheet, cellBatch);
        column += 1;

        if (showCurrentStockLevel) {
            HSSFCell cellBatchStock = header.createCell(column);
            cellBatchStock.setCellValue(translationService.translate("materialRequirements.materialRequirement.report.batchStock", locale));
            xlsHelper.setCellStyle(sheet, cellBatchStock);
        }
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity materialRequirement) {
        boolean includeWarehouse = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        boolean includeStartDateOrder = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);
        boolean showCurrentStockLevel = materialRequirement.getBooleanField(MaterialRequirementFields.SHOW_CURRENT_STOCK_LEVEL);

        List<Entity> materialRequirementProducts = Lists.newArrayList(materialRequirement.getHasManyField(MaterialRequirementFields.MATERIAL_REQUIREMENT_PRODUCTS));

        if (includeWarehouse) {
            materialRequirementProducts.sort(Comparator.comparing(MaterialRequirementXlsService::extractLocationNumber, nullsFirst(naturalOrder()))
                    .thenComparing(MaterialRequirementXlsService::extractOrderStartDate, nullsFirst(naturalOrder()))
                    .thenComparing(MaterialRequirementXlsService::extractProductNumber)
            );
        } else {
            materialRequirementProducts.sort(Comparator.comparing(MaterialRequirementXlsService::extractOrderStartDate, nullsFirst(naturalOrder()))
                    .thenComparing(MaterialRequirementXlsService::extractProductNumber));
        }

        String actualProduct = null;
        String actualLocation = "";
        Date actualDate = null;

        int rowNum = 1;
        int column = 0;

        for (Entity materialRequirementProduct : materialRequirementProducts) {
            Entity product = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.PRODUCT);
            Entity location = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.LOCATION);
            Entity batch = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.BATCH);
            BigDecimal quantity = materialRequirementProduct.getDecimalField(MaterialRequirementProductFields.QUANTITY);
            BigDecimal currentStock = materialRequirementProduct.getDecimalField(MaterialRequirementProductFields.CURRENT_STOCK);
            BigDecimal batchStock = materialRequirementProduct.getDecimalField(MaterialRequirementProductFields.BATCH_STOCK);
            Date orderStartDate = materialRequirementProduct.getDateField(MaterialRequirementProductFields.ORDER_START_DATE);
            String productNumber = product.getStringField(ProductFields.NUMBER);
            String productName = product.getStringField(ProductFields.NAME);
            String unit = product.getStringField(ProductFields.UNIT);

            HSSFRow row = sheet.createRow(rowNum++);

            column = 0;

            boolean fillDateIfWarehouseChanged = false;

            if (includeWarehouse) {
                String locationNumber = "";

                if (Objects.nonNull(location)) {
                    locationNumber = location.getStringField(LocationFields.NUMBER);
                }

                if (!actualLocation.equals(locationNumber)) {
                    row.createCell(column).setCellValue(locationNumber);

                    actualLocation = locationNumber;

                    fillDateIfWarehouseChanged = true;
                } else {
                    row.createCell(column).setCellValue("");
                }

                column += 1;
            }

            if (includeStartDateOrder) {
                if (Objects.isNull(actualDate) || !actualDate.equals(orderStartDate) || fillDateIfWarehouseChanged) {
                    if (Objects.nonNull(orderStartDate)) {
                        row.createCell(column).setCellValue(DateUtils.toDateString(orderStartDate));

                        actualDate = new Date(orderStartDate.getTime());
                    } else {
                        row.createCell(column).setCellValue("");

                        actualDate = null;
                    }
                } else {
                    row.createCell(column).setCellValue("");
                }

                column += 1;
            }

            if (Objects.isNull(actualProduct) || !actualProduct.equals(productNumber)) {
                row.createCell(column).setCellValue(productNumber);
                column += 1;
                row.createCell(column).setCellValue(productName);
                column += 1;
                row.createCell(column).setCellValue(numberService.format(quantity));
                column += 1;
                row.createCell(column).setCellValue(unit);
                column += 1;

                if (showCurrentStockLevel) {
                    row.createCell(column).setCellValue(numberService.format(currentStock));
                    column += 1;
                }

                actualProduct = productNumber;
            } else {
                row.createCell(column).setCellValue("");
                column += 1;
                row.createCell(column).setCellValue("");
                column += 1;
                row.createCell(column).setCellValue("");
                column += 1;
                row.createCell(column).setCellValue("");
                column += 1;

                if (showCurrentStockLevel) {
                    row.createCell(column).setCellValue("");
                    column += 1;
                }
            }

            if (Objects.nonNull(batch)) {
                row.createCell(column).setCellValue(batch.getStringField(BatchFields.NUMBER));
                column += 1;
            } else {
                row.createCell(column).setCellValue("");
                column += 1;
            }

            if (showCurrentStockLevel) {
                row.createCell(column).setCellValue(numberService.format(batchStock));
                column += 1;
            }
        }

        for (int i = 0; i < column; i++) {
            sheet.autoSizeColumn((short) i);
        }
    }

    private static String extractProductNumber(final Entity materialRequirementProduct) {
        Entity product = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.PRODUCT);

        return Objects.nonNull(product) ? product.getStringField(ProductFields.NUMBER) : null;
    }

    private static String extractLocationNumber(final Entity materialRequirementProduct) {
        Entity location = materialRequirementProduct.getBelongsToField(MaterialRequirementProductFields.LOCATION);

        return Objects.nonNull(location) ? location.getStringField(LocationFields.NUMBER) : null;
    }

    private static Date extractOrderStartDate(final Entity materialRequirementProduct) {
        return materialRequirementProduct.getDateField(MaterialRequirementProductFields.ORDER_START_DATE);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("materialRequirements.materialRequirement.report.title", locale);
    }

}
