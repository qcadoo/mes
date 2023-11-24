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
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementProductFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementsConstants;
import com.qcadoo.mes.materialRequirements.print.MaterialRequirementDataService;
import com.qcadoo.mes.materialRequirements.print.MaterialRequirementEntry;
import com.qcadoo.mes.materialRequirements.print.WarehouseDateKey;
import com.qcadoo.mes.materialRequirements.print.pdf.MaterialRequirementPdfService;
import com.qcadoo.mes.materialRequirements.print.xls.MaterialRequirementXlsService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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

        createMaterialRequirementProducts(materialRequirement);

        materialRequirementPdfService.generateDocument(materialRequirementWithFileName, state.getLocale());
        materialRequirementXlsService.generateDocument(materialRequirementWithFileName, state.getLocale());
    }

    private void createMaterialRequirementProducts(final Entity materialRequirement) {
        boolean includeWarehouse = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        boolean includeStartDateOrder = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);
        boolean showCurrentStockLevel = materialRequirement.getBooleanField(MaterialRequirementFields.SHOW_CURRENT_STOCK_LEVEL);

        if (includeWarehouse || includeStartDateOrder) {
            Map<WarehouseDateKey, List<MaterialRequirementEntry>> materialRequirementEntriesMap = materialRequirementDataService
                    .getGroupedData(materialRequirement);

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

            for (WarehouseDateKey warehouseDateKey : warehouseDateKeys) {
                List<MaterialRequirementEntry> materialRequirementEntries = materialRequirementEntriesMap.get(warehouseDateKey);
                Map<String, MaterialRequirementEntry> neededProductQuantities = materialRequirementDataService.getNeededProductQuantities(materialRequirementEntries);
                List<String> materialKeys = Lists.newArrayList(neededProductQuantities.keySet());

                materialKeys.sort(Comparator.naturalOrder());

                for (String materialKey : materialKeys) {
                    MaterialRequirementEntry materialRequirementEntry = neededProductQuantities.get(materialKey);

                    Entity product = materialRequirementEntry.getProduct();
                    Entity location = null;
                    BigDecimal quantity = materialRequirementEntry.getPlannedQuantity();
                    BigDecimal currentStock = null;
                    Date orderStartDate = null;

                    if (includeWarehouse) {
                        location = materialRequirementEntry.getWarehouse();
                    }

                    if (showCurrentStockLevel) {
                        if (Objects.nonNull(warehouseDateKey.getWarehouseId())) {
                            currentStock = materialRequirementDataService.getQuantity(quantitiesInStock, materialRequirementEntry);
                        } else {
                            currentStock = BigDecimal.ZERO;
                        }
                    }

                    if (includeStartDateOrder) {
                        orderStartDate = warehouseDateKey.getDate();
                    }

                    createMaterialRequirementProduct(materialRequirement, product, location, quantity, currentStock, orderStartDate);
                }
            }
        } else {
            List<Entity> orders = materialRequirement.getManyToManyField(MaterialRequirementFields.ORDERS);

            MrpAlgorithm algorithm = MrpAlgorithm
                    .parseString(materialRequirement.getStringField(MaterialRequirementFields.MRP_ALGORITHM));

            Map<Long, BigDecimal> neededProductQuantities = basicProductionCountingService.getNeededProductQuantities(orders,
                    algorithm);

            List<Entity> products = getProductDD().find().add(SearchRestrictions.in("id", neededProductQuantities.keySet()))
                    .list().getEntities();

            products.sort(Comparator.comparing(product -> product.getStringField(ProductFields.NUMBER)));

            for (Entity product : products) {
                BigDecimal quantity = neededProductQuantities.get(product.getId());

                createMaterialRequirementProduct(materialRequirement, product, null, quantity, null, null);
            }
        }
    }

    private void createMaterialRequirementProduct(final Entity materialRequirement, final Entity product, final Entity location,
                                                  final BigDecimal quantity, final BigDecimal currentStock, final Date orderStartDate) {
        Entity materialRequirementProduct = getMaterialRequirementProductDD().create();

        materialRequirementProduct.setField(MaterialRequirementProductFields.MATERIAL_REQUIREMENT, materialRequirement);
        materialRequirementProduct.setField(MaterialRequirementProductFields.PRODUCT, product);
        materialRequirementProduct.setField(MaterialRequirementProductFields.LOCATION, location);
        materialRequirementProduct.setField(MaterialRequirementProductFields.QUANTITY,
                numberService.setScaleWithDefaultMathContext(quantity));
        materialRequirementProduct.setField(MaterialRequirementProductFields.CURRENT_STOCK,
                numberService.setScaleWithDefaultMathContext(currentStock));
        materialRequirementProduct.setField(MaterialRequirementProductFields.ORDER_START_DATE, orderStartDate);

        materialRequirementProduct.getDataDefinition().save(materialRequirementProduct);
    }

    private DataDefinition getMaterialRequirementProductDD() {
        return dataDefinitionService.get(MaterialRequirementsConstants.PLUGIN_IDENTIFIER, MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT_PRODUCT);
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
