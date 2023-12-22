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
package com.qcadoo.mes.materialRequirements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.DocumentException;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementProductFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementsConstants;
import com.qcadoo.mes.materialRequirements.print.MaterialRequirementDataService;
import com.qcadoo.mes.materialRequirements.print.MaterialRequirementEntry;
import com.qcadoo.mes.materialRequirements.print.WarehouseDateKey;
import com.qcadoo.mes.materialRequirements.print.pdf.MaterialRequirementPdfService;
import com.qcadoo.mes.materialRequirements.print.xls.MaterialRequirementXlsService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MaterialRequirementServiceImpl implements MaterialRequirementService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private FileService fileService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private MaterialRequirementDataService materialRequirementDataService;

    @Autowired
    private MaterialRequirementPdfService materialRequirementPdfService;

    @Autowired
    private MaterialRequirementXlsService materialRequirementXlsService;

    @Override
    public boolean checkIfInputProductsRequiredForTypeIsSelected(final DataDefinition entityDD, final Entity entity,
                                                                 final String fieldName, final String errorMessage) {
        String inputProductsRequiredForType = entity.getStringField(fieldName);

        if (Objects.isNull(inputProductsRequiredForType)) {
            entity.addError(entityDD.getField(fieldName), errorMessage);
            entity.addGlobalError(errorMessage);

            return false;
        }

        return true;
    }

    @Override
    public void setInputProductsRequiredForTypeDefaultValue(final Entity entity, final String fieldName, final String fieldValue) {
        String inputProductsRequiredForType = entity.getStringField(fieldName);

        if (Objects.isNull(inputProductsRequiredForType)) {
            entity.setField(fieldName, fieldValue);
        }
    }

    @Override
    public MrpAlgorithm getDefaultMrpAlgorithm() {
        return MrpAlgorithm.ONLY_COMPONENTS;
    }

    @Override
    public void generateMaterialRequirementDocuments(final ComponentState state, final Entity materialRequirement)
            throws IOException, DocumentException {
        Entity materialRequirementWithFileName = fileService.updateReportFileName(materialRequirement,
                MaterialRequirementFields.DATE, "materialRequirements.materialRequirement.report.fileName");

        createMaterialRequirementProducts(materialRequirementWithFileName);

        materialRequirementPdfService.generateDocument(materialRequirementWithFileName, state.getLocale());
        materialRequirementXlsService.generateDocument(materialRequirementWithFileName, state.getLocale());
    }

    private void createMaterialRequirementProducts(final Entity materialRequirement) {
        boolean includeWarehouse = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        boolean includeStartDateOrder = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);
        boolean showCurrentStockLevel = materialRequirement.getBooleanField(MaterialRequirementFields.SHOW_CURRENT_STOCK_LEVEL);

        Map<WarehouseDateKey, List<MaterialRequirementEntry>> materialRequirementEntriesMap = materialRequirementDataService
                .getGroupedData(materialRequirement);

        List<WarehouseDateKey> warehouseDateKeys = Lists.newArrayList(materialRequirementEntriesMap.keySet());

        Map<Long, Map<Long, BigDecimal>> quantitiesInStock = Maps.newHashMap();

        if (showCurrentStockLevel) {
            List<MaterialRequirementEntry> materialRequirementEntries = warehouseDateKeys.stream()
                    .filter(warehouseDateKey -> Objects.nonNull(warehouseDateKey.getWarehouseId()))
                    .flatMap(warehouseDateKey -> materialRequirementEntriesMap.get(warehouseDateKey).stream())
                    .collect(Collectors.toList());

            quantitiesInStock = materialRequirementDataService.getQuantitiesInStock(materialRequirementEntries);
        }

        List<Entity> materialRequirementProducts = Lists.newArrayList();

        for (WarehouseDateKey warehouseDateKey : warehouseDateKeys) {
            List<MaterialRequirementEntry> materialRequirementEntries = materialRequirementEntriesMap.get(warehouseDateKey);

            Map<String, MaterialRequirementEntry> neededProductQuantities = materialRequirementDataService.getNeededProductQuantities(materialRequirementEntries);

            List<String> materialKeys = Lists.newArrayList(neededProductQuantities.keySet());

            for (String materialKey : materialKeys) {
                MaterialRequirementEntry materialRequirementEntry = neededProductQuantities.get(materialKey);

                Long productId = materialRequirementEntry.getId();
                Long locationId = null;
                BigDecimal quantity = materialRequirementEntry.getPlannedQuantity();
                BigDecimal currentStock = null;
                Date orderStartDate = null;
                List<Entity> batches = materialRequirementEntry.getBatches();

                if (includeWarehouse) {
                    locationId = materialRequirementEntry.getWarehouseId();
                }

                if (showCurrentStockLevel) {
                    currentStock = Objects.nonNull(locationId) ?
                            BigDecimalUtils.convertNullToZero(materialRequirementDataService.getQuantity(quantitiesInStock, materialRequirementEntry)) : BigDecimal.ZERO;
                }

                if (includeStartDateOrder) {
                    orderStartDate = warehouseDateKey.getDate();
                }

                if (batches.isEmpty()) {
                    Entity materialRequirementProduct = createMaterialRequirementProduct(materialRequirement, productId, locationId, null, quantity, currentStock, null, orderStartDate);

                    materialRequirementProducts.add(materialRequirementProduct);
                } else {
                    Entity product = materialRequirementEntry.getProduct();
                    Entity location = materialRequirementEntry.getWarehouse();

                    for (Entity batch : batches) {
                        Long batchId = batch.getId();
                        BigDecimal batchStock = null;

                        if (showCurrentStockLevel) {
                            batchStock = Objects.nonNull(locationId) ?
                                    BigDecimalUtils.convertNullToZero(materialFlowResourcesService.getBatchesQuantity(Lists.newArrayList(batch), product, location)) : BigDecimal.ZERO;
                        }

                        Entity materialRequirementProduct = createMaterialRequirementProduct(materialRequirement, productId, locationId, batchId, quantity, currentStock, batchStock, orderStartDate);

                        materialRequirementProducts.add(materialRequirementProduct);
                    }
                }
            }
        }

        materialRequirement.setField(MaterialRequirementFields.MATERIAL_REQUIREMENT_PRODUCTS, materialRequirementProducts);
    }

    private Entity createMaterialRequirementProduct(final Entity materialRequirement, final Long productId, final Long locationId, final Long batchId,
                                                  final BigDecimal quantity, final BigDecimal currentStock, final BigDecimal batchStock,
                                                  final Date orderStartDate) {
        Entity materialRequirementProduct = getMaterialRequirementProductDD().create();

        materialRequirementProduct.setField(MaterialRequirementProductFields.MATERIAL_REQUIREMENT, materialRequirement);
        materialRequirementProduct.setField(MaterialRequirementProductFields.PRODUCT, productId);
        materialRequirementProduct.setField(MaterialRequirementProductFields.LOCATION, locationId);
        materialRequirementProduct.setField(MaterialRequirementProductFields.BATCH, batchId);
        materialRequirementProduct.setField(MaterialRequirementProductFields.QUANTITY,
                numberService.setScaleWithDefaultMathContext(quantity));
        materialRequirementProduct.setField(MaterialRequirementProductFields.CURRENT_STOCK,
                numberService.setScaleWithDefaultMathContext(currentStock));
        materialRequirementProduct.setField(MaterialRequirementProductFields.BATCH_STOCK,
                numberService.setScaleWithDefaultMathContext(batchStock));
        materialRequirementProduct.setField(MaterialRequirementProductFields.ORDER_START_DATE, orderStartDate);

        return materialRequirementProduct.getDataDefinition().save(materialRequirementProduct);
    }

    private DataDefinition getMaterialRequirementProductDD() {
        return dataDefinitionService.get(MaterialRequirementsConstants.PLUGIN_IDENTIFIER, MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT_PRODUCT);
    }

}
