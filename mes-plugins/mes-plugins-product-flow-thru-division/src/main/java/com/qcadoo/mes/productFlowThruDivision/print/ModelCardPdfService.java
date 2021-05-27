package com.qcadoo.mes.productFlowThruDivision.print;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.costCalculation.print.ProductsCostCalculationService;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.productFlowThruDivision.constants.ModelCardFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ModelCardProductFields;
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyInputProductTypeFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Service
public final class ModelCardPdfService extends PdfDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(ModelCardPdfService.class);

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final int[] defaultModelCardProductColumnWidth = new int[] { 10, 20, 5, 10, 10 };

    private static final int[] defaultModelCardMaterialsColumnWidth = new int[] { 9, 6, 15, 6, 6, 4, 4, 4, 4, 5, 7, 3 };

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("productFlowThruDivision.modelCard.report.title", locale);
    }

    @Override
    protected void buildPdfContent(Document document, Entity modelCard, Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("productFlowThruDivision.modelCard.report.header", locale,
                modelCard.getStringField(ModelCardFields.NAME));
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, StringUtils.EMPTY, documentTitle, documentAuthor,
                modelCard.getDateField(ModelCardFields.DATE));

        Entity parameter = parameterService.getParameter();
        Entity productAttribute = parameter.getBelongsToField(ParameterFieldsPFTD.PRODUCT_ATTRIBUTE);
        Entity materialAttribute = parameter.getBelongsToField(ParameterFieldsPFTD.MATERIAL_ATTRIBUTE);
        for (Entity modelCardProduct : modelCard.getHasManyField(ModelCardFields.MODEL_CARD_PRODUCTS)) {
            Entity product = modelCardProduct.getBelongsToField(ModelCardProductFields.PRODUCT);
            boolean forFamily = Objects.isNull(product.getBelongsToField(ProductFields.PARENT));
            Entity technology = modelCardProduct.getBelongsToField(ModelCardProductFields.TECHNOLOGY);
            BigDecimal quantity = modelCardProduct.getDecimalField(ModelCardProductFields.QUANTITY);

            Map<OperationProductComponentHolder, BigDecimal> neededQuantities = productQuantitiesService
                    .getNeededProductQuantities(technology, product, quantity);

            Map<Long, Map<Long, BigDecimal>> quantitiesInStock = getQuantitiesInStock(neededQuantities.keySet());

            List<ModelCardMaterialEntry> entries = newArrayList();

            for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : neededQuantities.entrySet()) {
                mapToModelCardMaterialEntry(modelCard, quantitiesInStock, entries, neededProductQuantity, forFamily, quantity,
                        locale);
            }

            List<ModelCardMaterialEntry> groupedEntries = groupMaterials(entries);
            BigDecimal materialUnitCostsSum = getMaterialUnitCostsSum(groupedEntries);
            List<ModelCardMaterialEntry> sortedEntries = groupedEntries.stream()
                    .sorted(Comparator
                            .comparing(ModelCardMaterialEntry::getTechnologyInputProductTypeId, nullsFirst(naturalOrder()))
                            .thenComparing(ModelCardMaterialEntry::getSizeGroupId, nullsFirst(naturalOrder())))
                    .collect(Collectors.toList());
            addProductTable(document, modelCardProduct, productAttribute, materialUnitCostsSum, locale);
            addMaterialsTable(document, sortedEntries, materialAttribute, locale);
            document.newPage();
        }
    }

    private BigDecimal getMaterialUnitCostsSum(List<ModelCardMaterialEntry> groupedEntries) {
        Map<Long, BigDecimal> productTypePricesSum = Maps.newHashMap();
        Map<Long, Integer> productTypePricesCount = Maps.newHashMap();
        for (ModelCardMaterialEntry modelCardMaterialEntry : groupedEntries) {
            if (!Objects.isNull(modelCardMaterialEntry.getSizeGroupId())) {
                if (productTypePricesSum.containsKey(modelCardMaterialEntry.getTechnologyInputProductTypeId())) {
                    productTypePricesSum.computeIfPresent(modelCardMaterialEntry.getTechnologyInputProductTypeId(),
                            (k, v) -> v.add(modelCardMaterialEntry.getPrice(), numberService.getMathContext()));
                    productTypePricesCount.computeIfPresent(modelCardMaterialEntry.getTechnologyInputProductTypeId(),
                            (k, v) -> v + 1);
                } else {
                    productTypePricesSum.put(modelCardMaterialEntry.getTechnologyInputProductTypeId(),
                            modelCardMaterialEntry.getPrice());
                    productTypePricesCount.put(modelCardMaterialEntry.getTechnologyInputProductTypeId(), 1);
                }
            }
        }
        BigDecimal materialUnitCostsSum = BigDecimal.ZERO;
        for (ModelCardMaterialEntry modelCardMaterialEntry : groupedEntries) {
            if (Objects.isNull(modelCardMaterialEntry.getId())) {
                BigDecimal price = numberService.setScaleWithDefaultMathContext(
                        productTypePricesSum.get(modelCardMaterialEntry.getTechnologyInputProductTypeId())
                                .divide(new BigDecimal(
                                        productTypePricesCount.get(modelCardMaterialEntry.getTechnologyInputProductTypeId())),
                                        numberService.getMathContext()),
                        2);
                modelCardMaterialEntry.setPrice(price);
                BigDecimal materialUnitCost = modelCardMaterialEntry.getNorm().multiply(price, numberService.getMathContext());
                modelCardMaterialEntry.setMaterialUnitCost(materialUnitCost);
            }
            if (Objects.isNull(modelCardMaterialEntry.getSizeGroupId())) {
                materialUnitCostsSum = materialUnitCostsSum.add(modelCardMaterialEntry.getMaterialUnitCost(),
                        numberService.getMathContext());
            }
        }
        return materialUnitCostsSum;
    }

    private void mapToModelCardMaterialEntry(Entity modelCard, Map<Long, Map<Long, BigDecimal>> quantitiesInStock,
            List<ModelCardMaterialEntry> entries, Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity,
            boolean forFamily, BigDecimal quantity, Locale locale) {
        ModelCardMaterialEntry modelCardMaterialEntry = new ModelCardMaterialEntry();
        Entity material = neededProductQuantity.getKey().getProduct();
        Entity operationProductComponent = neededProductQuantity.getKey().getOperationProductComponent();
        Entity warehouse = operationProductComponent.getBelongsToField(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION);

        BigDecimal neededQuantity = numberService.setScaleWithDefaultMathContext(neededProductQuantity.getValue(), 2);
        modelCardMaterialEntry.setNeededQuantity(neededQuantity);
        Entity technologyInputProductType = operationProductComponent
                .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);

        if (Objects.nonNull(technologyInputProductType)) {
            modelCardMaterialEntry.setTechnologyInputProductTypeId(technologyInputProductType.getId());
            modelCardMaterialEntry.setTechnologyInputProductTypeName(
                    technologyInputProductType.getStringField(TechnologyInputProductTypeFields.NAME));
        }

        modelCardMaterialEntry.setNorm(BigDecimal.ZERO);

        modelCardMaterialEntry
                .setDescription(operationProductComponent.getStringField(OperationProductInComponentFields.DESCRIPTION));
        if (forFamily && operationProductComponent
                .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES)) {
            modelCardMaterialEntry.setNumber(
                    translationService.translate("productFlowThruDivision.modelCard.report.productsBySize.label", locale));
            modelCardMaterialEntry
                    .setUnit(operationProductComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT));

            List<Entity> productBySizeGroups = getProductBySizeGroups(operationProductComponent.getId());

            for (Entity productBySizeGroup : productBySizeGroups) {
                ModelCardMaterialEntry modelCardMaterialBySizeEntry = new ModelCardMaterialEntry();
                Entity materialBySizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
                Entity sizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.SIZE_GROUP);
                modelCardMaterialBySizeEntry.setId(materialBySizeGroup.getId());
                modelCardMaterialBySizeEntry.setNumber(materialBySizeGroup.getStringField(ProductFields.NUMBER));
                modelCardMaterialBySizeEntry.setName(materialBySizeGroup.getStringField(ProductFields.NAME));
                modelCardMaterialBySizeEntry.setUnit(materialBySizeGroup.getStringField(ProductFields.UNIT));
                modelCardMaterialBySizeEntry.setWarehouseId(warehouse.getId());
                modelCardMaterialBySizeEntry.setCurrentStock(BigDecimalUtils
                        .convertNullToZero(quantitiesInStock.get(warehouse.getId()).get(materialBySizeGroup.getId())));
                if (Objects.nonNull(technologyInputProductType)) {
                    modelCardMaterialBySizeEntry.setTechnologyInputProductTypeId(technologyInputProductType.getId());
                }
                Entity parent = materialBySizeGroup.getBelongsToField(ProductFields.PARENT);
                if (!Objects.isNull(parent)) {
                    modelCardMaterialBySizeEntry.setParentId(parent.getId());
                }
                modelCardMaterialBySizeEntry.setSizeGroupId(sizeGroup.getId());
                modelCardMaterialBySizeEntry.setSizeGroupNumber(sizeGroup.getStringField(SizeGroupFields.NUMBER));

                BigDecimal norm = productBySizeGroup.getDecimalField(ProductBySizeGroupFields.QUANTITY);
                modelCardMaterialBySizeEntry.setNeededQuantity(numberService.setScaleWithDefaultMathContext(quantity.multiply(
                        productBySizeGroup.getDecimalField(ProductBySizeGroupFields.QUANTITY), numberService.getMathContext())));

                modelCardMaterialBySizeEntry.setNorm(norm);
                BigDecimal price = numberService.setScaleWithDefaultMathContext(
                        productsCostCalculationService.calculateProductCostPerUnit(materialBySizeGroup,
                                modelCard.getStringField(ModelCardFields.MATERIAL_COSTS_USED),
                                modelCard.getBooleanField(ModelCardFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED)),
                        2);
                modelCardMaterialBySizeEntry.setPrice(price);
                BigDecimal materialUnitCost = numberService
                        .setScaleWithDefaultMathContext(norm.multiply(price, numberService.getMathContext()), 2);
                modelCardMaterialBySizeEntry.setMaterialUnitCost(materialUnitCost);

                entries.add(modelCardMaterialBySizeEntry);
            }
        } else {
            BigDecimal norm = operationProductComponent.getDecimalField(OperationProductInComponentFields.QUANTITY);

            if (operationProductComponent
                    .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES)) {
                Entity pbs = operationProductComponent.getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS)
                        .stream()
                        .filter(prbs -> prbs.getBelongsToField(ProductBySizeGroupFields.PRODUCT).getId().equals(material.getId()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(pbs)) {
                    norm = pbs.getDecimalField(ProductBySizeGroupFields.QUANTITY);
                }
            }

            modelCardMaterialEntry.setNorm(norm);
            modelCardMaterialEntry.setId(material.getId());
            modelCardMaterialEntry.setNumber(material.getStringField(ProductFields.NUMBER));
            modelCardMaterialEntry.setName(material.getStringField(ProductFields.NAME));
            modelCardMaterialEntry.setUnit(material.getStringField(ProductFields.UNIT));
            modelCardMaterialEntry.setWarehouseId(warehouse.getId());
            modelCardMaterialEntry.setCurrentStock(
                    BigDecimalUtils.convertNullToZero(quantitiesInStock.get(warehouse.getId()).get(material.getId())));
            Entity parent = material.getBelongsToField(ProductFields.PARENT);
            if (!Objects.isNull(parent)) {
                modelCardMaterialEntry.setParentId(parent.getId());
            }

            BigDecimal price = numberService.setScaleWithDefaultMathContext(productsCostCalculationService
                    .calculateProductCostPerUnit(material, modelCard.getStringField(ModelCardFields.MATERIAL_COSTS_USED),
                            modelCard.getBooleanField(ModelCardFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED)),
                    2);
            modelCardMaterialEntry.setPrice(price);
            BigDecimal materialUnitCost = numberService
                    .setScaleWithDefaultMathContext(norm.multiply(price, numberService.getMathContext()), 2);
            modelCardMaterialEntry.setMaterialUnitCost(materialUnitCost);
        }
        entries.add(modelCardMaterialEntry);
    }

    private List<ModelCardMaterialEntry> groupMaterials(List<ModelCardMaterialEntry> entries) {
        Map<ModelCardMaterialEntry, QuantityNormDescriptions> quantityNormDescriptionsMap = com.google.common.collect.Maps
                .newHashMap();

        for (ModelCardMaterialEntry modelCardMaterialEntry : entries) {
            if (quantityNormDescriptionsMap.containsKey(modelCardMaterialEntry)) {
                updateGroup(quantityNormDescriptionsMap, modelCardMaterialEntry);
            } else {
                createNewGroup(quantityNormDescriptionsMap, modelCardMaterialEntry);
            }
        }

        quantityNormDescriptionsMap.forEach((k, v) -> {
            k.setNeededQuantity(v.getNeededQuantity());
            k.setNorm(v.getNorm());
            k.setMaterialUnitCost(v.getMaterialUnitCost());
            k.setCurrentStock(v.getCurrentStock());
            k.setDescription(String.join(",", v.getDescriptions()));
        });

        return com.google.common.collect.Lists.newArrayList(quantityNormDescriptionsMap.keySet());
    }

    private void updateGroup(Map<ModelCardMaterialEntry, QuantityNormDescriptions> quantityNormDescriptionsMap,
            ModelCardMaterialEntry modelCardMaterialEntry) {
        QuantityNormDescriptions quantityNormDescriptions = quantityNormDescriptionsMap.get(modelCardMaterialEntry);
        if (quantityNormDescriptions.getNeededQuantity() != null) {
            quantityNormDescriptions.setNeededQuantity(
                    quantityNormDescriptions.getNeededQuantity().add(modelCardMaterialEntry.getNeededQuantity()));
        }
        if (quantityNormDescriptions.getNorm() != null) {
            quantityNormDescriptions.setNorm(quantityNormDescriptions.getNorm().add(modelCardMaterialEntry.getNorm()));
        }
        if (quantityNormDescriptions.getMaterialUnitCost() != null) {
            quantityNormDescriptions.setMaterialUnitCost(
                    quantityNormDescriptions.getMaterialUnitCost().add(modelCardMaterialEntry.getMaterialUnitCost()));
        }
        if (quantityNormDescriptions.getCurrentStock() != null
                && !quantityNormDescriptions.getWarehouseIds().contains(modelCardMaterialEntry.getWarehouseId())) {
            quantityNormDescriptions
                    .setCurrentStock(quantityNormDescriptions.getCurrentStock().add(modelCardMaterialEntry.getCurrentStock()));
        }
        quantityNormDescriptions.getWarehouseIds().add(modelCardMaterialEntry.getWarehouseId());
        if (modelCardMaterialEntry.getDescription() != null) {
            quantityNormDescriptions.getDescriptions().add(modelCardMaterialEntry.getDescription());
        }
    }

    private void createNewGroup(Map<ModelCardMaterialEntry, QuantityNormDescriptions> quantityNormDescriptionsMap,
            ModelCardMaterialEntry modelCardMaterialEntry) {
        QuantityNormDescriptions quantityNormDescriptions = new QuantityNormDescriptions();
        quantityNormDescriptions.setNeededQuantity(modelCardMaterialEntry.getNeededQuantity());
        quantityNormDescriptions.setNorm(modelCardMaterialEntry.getNorm());
        quantityNormDescriptions.setMaterialUnitCost(modelCardMaterialEntry.getMaterialUnitCost());
        quantityNormDescriptions.setCurrentStock(modelCardMaterialEntry.getCurrentStock());
        Set<Long> warehouseIds = Sets.newHashSet();
        warehouseIds.add(modelCardMaterialEntry.getWarehouseId());
        quantityNormDescriptions.setWarehouseIds(warehouseIds);
        Set<String> descriptions = Sets.newHashSet();
        if (modelCardMaterialEntry.getDescription() != null) {
            descriptions.add(modelCardMaterialEntry.getDescription());
        }
        quantityNormDescriptions.setDescriptions(descriptions);
        quantityNormDescriptionsMap.put(modelCardMaterialEntry, quantityNormDescriptions);
    }

    private Map<Long, Map<Long, BigDecimal>> getQuantitiesInStock(
            Set<OperationProductComponentHolder> operationProductComponentHolders) {
        Map<Long, Entity> warehouses = com.google.common.collect.Maps.newHashMap();
        Map<Long, List<Entity>> warehouseProducts = com.google.common.collect.Maps.newHashMap();

        for (OperationProductComponentHolder operationProductComponentHolder : operationProductComponentHolders) {
            Entity product = operationProductComponentHolder.getProduct();
            Entity operationProductComponent = operationProductComponentHolder.getOperationProductComponent();
            Entity warehouse = operationProductComponent
                    .getBelongsToField(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION);

            if (!warehouses.containsKey(warehouse.getId())) {
                warehouses.put(warehouse.getId(), warehouse);
            }
            if (Objects.isNull(product)) {
                List<Entity> productBySizeGroups = getProductBySizeGroups(operationProductComponent.getId());
                for (Entity productBySizeGroup : productBySizeGroups) {
                    Entity materialBySizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
                    addProductToWarehouse(warehouseProducts, warehouse, materialBySizeGroup);
                }
            } else {
                addProductToWarehouse(warehouseProducts, warehouse, product);
            }
        }

        Map<Long, Map<Long, BigDecimal>> quantitiesInStock = com.google.common.collect.Maps.newHashMap();

        for (Map.Entry<Long, List<Entity>> entry : warehouseProducts.entrySet()) {
            quantitiesInStock.put(entry.getKey(), materialFlowResourcesService
                    .getQuantitiesForProductsAndLocation(entry.getValue(), warehouses.get(entry.getKey())));
        }

        return quantitiesInStock;
    }

    private void addProductToWarehouse(Map<Long, List<Entity>> warehouseProducts, Entity warehouse, Entity product) {
        if (warehouseProducts.containsKey(warehouse.getId())) {
            List<Entity> products = warehouseProducts.get(warehouse.getId());

            products.add(product);
        } else {
            warehouseProducts.put(warehouse.getId(), Lists.newArrayList(product));
        }
    }

    private void addMaterialsTable(Document document, List<ModelCardMaterialEntry> entries, Entity materialAttribute,
            Locale locale) throws DocumentException {
        Map<String, HeaderAlignment> headersWithAlignments = getMaterialHeaders(materialAttribute, locale);

        List<String> headers = com.google.common.collect.Lists.newLinkedList(headersWithAlignments.keySet());

        PdfPTable table = pdfHelper.createTableWithHeader(headersWithAlignments.size(), headers, true,
                defaultModelCardMaterialsColumnWidth, headersWithAlignments);

        for (ModelCardMaterialEntry modelCardMaterialEntry : entries) {
            if (Objects.isNull(modelCardMaterialEntry.getId())) {
                addProductInputTypeRow(table, modelCardMaterialEntry);
            } else if (!Objects.isNull(modelCardMaterialEntry.getSizeGroupId())) {
                addProductBySizeRow(table, modelCardMaterialEntry, materialAttribute);
            } else {
                addProductRow(table, modelCardMaterialEntry, materialAttribute);
            }
        }
        document.add(table);
    }

    private void addProductInputTypeRow(PdfPTable table, ModelCardMaterialEntry modelCardMaterialEntry) {
        table.getDefaultCell().disableBorderSide(PdfCell.BOTTOM);
        addTechnologyInputProductTypeToReport(table, modelCardMaterialEntry);
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(new Phrase(modelCardMaterialEntry.getNumber(), FontUtils.getDejavuItalic7Light()));
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(new Phrase(StringUtils.EMPTY));

        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(new Phrase(StringUtils.EMPTY));

        table.addCell(new Phrase(StringUtils.EMPTY));

        table.addCell(new Phrase(StringUtils.EMPTY));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(new Phrase(modelCardMaterialEntry.getUnit(), FontUtils.getDejavuRegular7Dark()));
        table.getDefaultCell().enableBorderSide(PdfCell.BOTTOM);
    }

    private void addProductRow(PdfPTable table, ModelCardMaterialEntry modelCardMaterialEntry, Entity materialAttribute) {
        addTechnologyInputProductTypeToReport(table, modelCardMaterialEntry);
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(new Phrase(modelCardMaterialEntry.getNumber() + ", " + modelCardMaterialEntry.getName(),
                FontUtils.getDejavuRegular7Dark()));
        table.addCell(new Phrase(getProductAttributeValue(materialAttribute, modelCardMaterialEntry.getId()),
                FontUtils.getDejavuRegular7Dark()));
        Entity supplier = null;
        BigDecimal minimumOrderQuantity = null;
        Entity supplierData = getSupplierData(modelCardMaterialEntry.getId(), modelCardMaterialEntry.getParentId());

        if (!Objects.isNull(supplierData)) {
            if (supplierData.getDataDefinition().getName().equals(DeliveriesConstants.MODEL_COMPANY_PRODUCT)) {
                supplier = supplierData.getBelongsToField(CompanyProductFields.COMPANY);
                minimumOrderQuantity = supplierData.getDecimalField(CompanyProductFields.MINIMUM_ORDER_QUANTITY);
            } else {
                supplier = supplierData.getBelongsToField(CompanyProductsFamilyFields.COMPANY);
                minimumOrderQuantity = supplierData.getDecimalField(CompanyProductsFamilyFields.MINIMUM_ORDER_QUANTITY);
            }
        }

        table.addCell(new Phrase(supplier != null ? supplier.getStringField(CompanyFields.NUMBER) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular7Dark()));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(new Phrase(minimumOrderQuantity != null ? numberService.format(minimumOrderQuantity) : StringUtils.EMPTY,
                FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(numberService.format(modelCardMaterialEntry.getNorm()), FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(numberService.format(modelCardMaterialEntry.getPrice()), FontUtils.getDejavuBold7Dark()));
        table.addCell(
                new Phrase(numberService.format(modelCardMaterialEntry.getMaterialUnitCost()), FontUtils.getDejavuBold7Dark()));
        table.addCell(
                new Phrase(numberService.format(modelCardMaterialEntry.getNeededQuantity()), FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(numberService.format(modelCardMaterialEntry.getCurrentStock()), FontUtils.getDejavuBold7Dark()));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(new Phrase(modelCardMaterialEntry.getUnit(), FontUtils.getDejavuRegular7Dark()));
    }

    private void addProductBySizeRow(PdfPTable table, ModelCardMaterialEntry modelCardMaterialEntry, Entity materialAttribute) {
        table.getDefaultCell().disableBorderSide(PdfCell.BOTTOM);
        table.getDefaultCell().disableBorderSide(PdfCell.TOP);
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(new Phrase(modelCardMaterialEntry.getSizeGroupNumber(), FontUtils.getDejavuRegular7Light()));
        table.addCell(new Phrase(modelCardMaterialEntry.getNumber() + ", " + modelCardMaterialEntry.getName(),
                FontUtils.getDejavuRegular7Light()));
        table.addCell(new Phrase(getProductAttributeValue(materialAttribute, modelCardMaterialEntry.getId()),
                FontUtils.getDejavuRegular7Light()));
        Entity supplier = null;
        BigDecimal minimumOrderQuantity = null;
        Entity supplierData = getSupplierData(modelCardMaterialEntry.getId(), modelCardMaterialEntry.getParentId());

        if (!Objects.isNull(supplierData)) {
            if (supplierData.getDataDefinition().getName().equals(DeliveriesConstants.MODEL_COMPANY_PRODUCT)) {
                supplier = supplierData.getBelongsToField(CompanyProductFields.COMPANY);
                minimumOrderQuantity = supplierData.getDecimalField(CompanyProductFields.MINIMUM_ORDER_QUANTITY);
            } else {
                supplier = supplierData.getBelongsToField(CompanyProductsFamilyFields.COMPANY);
                minimumOrderQuantity = supplierData.getDecimalField(CompanyProductsFamilyFields.MINIMUM_ORDER_QUANTITY);
            }
        }
        table.addCell(new Phrase(supplier != null ? supplier.getStringField(CompanyFields.NUMBER) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular7Light()));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(new Phrase(minimumOrderQuantity != null ? numberService.format(minimumOrderQuantity) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular7Light()));
        table.addCell(new Phrase(numberService.format(modelCardMaterialEntry.getNorm()), FontUtils.getDejavuRegular7Light()));
        table.addCell(new Phrase(numberService.format(modelCardMaterialEntry.getPrice()), FontUtils.getDejavuRegular7Light()));
        table.addCell(new Phrase(numberService.format(modelCardMaterialEntry.getMaterialUnitCost()),
                FontUtils.getDejavuRegular7Light()));
        table.addCell(new Phrase(new Phrase(numberService.format(modelCardMaterialEntry.getNeededQuantity()),
                FontUtils.getDejavuRegular7Light())));
        table.addCell(
                new Phrase(numberService.format(modelCardMaterialEntry.getCurrentStock()), FontUtils.getDejavuRegular7Light()));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(new Phrase(modelCardMaterialEntry.getUnit(), FontUtils.getDejavuRegular7Light()));
        table.getDefaultCell().enableBorderSide(PdfCell.BOTTOM);
        table.getDefaultCell().enableBorderSide(PdfCell.TOP);
    }

    private Map<String, HeaderAlignment> getMaterialHeaders(Entity materialAttribute, Locale locale) {
        Map<String, HeaderAlignment> headersWithAlignments = com.google.common.collect.Maps.newLinkedHashMap();
        headersWithAlignments.put(
                translationService.translate("productFlowThruDivision.modelCard.report.productType.label", locale),
                HeaderAlignment.LEFT);
        headersWithAlignments.put(
                translationService.translate("productFlowThruDivision.modelCard.report.sizeGroup.label", locale),
                HeaderAlignment.LEFT);
        headersWithAlignments.put(translationService.translate("productFlowThruDivision.modelCard.report.product.label", locale),
                HeaderAlignment.LEFT);
        headersWithAlignments.put(getAttributeText(materialAttribute), HeaderAlignment.LEFT);
        headersWithAlignments.put(translationService.translate("productFlowThruDivision.modelCard.report.supplier.label", locale),
                HeaderAlignment.LEFT);
        headersWithAlignments.put(
                translationService.translate("productFlowThruDivision.modelCard.report.minimumOrderQuantity.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("productFlowThruDivision.modelCard.report.norm.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("productFlowThruDivision.modelCard.report.price.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("productFlowThruDivision.modelCard.report.unitCost.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(
                translationService.translate("productFlowThruDivision.modelCard.report.demandQuantity.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(
                translationService.translate("productFlowThruDivision.modelCard.report.warehouseStock.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("productFlowThruDivision.modelCard.report.unit.label", locale),
                HeaderAlignment.LEFT);
        return headersWithAlignments;
    }

    private String getAttributeText(Entity attribute) {
        String attributeText = StringUtils.EMPTY;
        if (attribute != null) {
            attributeText = attribute.getStringField(AttributeFields.NUMBER);
            String unit = attribute.getStringField(AttributeFields.UNIT);
            if (!StringUtils.isEmpty(unit)) {
                attributeText = attributeText + " (" + unit + ")";
            }
        }
        return attributeText;
    }

    private void addTechnologyInputProductTypeToReport(PdfPTable table, ModelCardMaterialEntry modelCardMaterialEntry) {
        if (modelCardMaterialEntry.getTechnologyInputProductTypeId() != null) {
            if (!StringUtils.isEmpty(modelCardMaterialEntry.getDescription())) {
                table.addCell(new Phrase(modelCardMaterialEntry.getTechnologyInputProductTypeName() + " ("
                        + modelCardMaterialEntry.getDescription() + ")", FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase(modelCardMaterialEntry.getTechnologyInputProductTypeName(),
                        FontUtils.getDejavuRegular7Dark()));
            }
        } else {
            if (!StringUtils.isEmpty(modelCardMaterialEntry.getDescription())) {
                table.addCell(
                        new Phrase(" (" + modelCardMaterialEntry.getDescription() + ")", FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
            }
        }
    }

    private Entity getSupplierData(Long materialId, Long parentId) {
        Entity companyProduct = getCompanyProductDD().find()
                .createAlias(CompanyProductFields.PRODUCT, CompanyProductFields.PRODUCT, JoinType.LEFT)
                .add(SearchRestrictions.eq(CompanyProductFields.PRODUCT + L_DOT + L_ID, materialId))
                .add(SearchRestrictions.eq(CompanyProductFields.IS_DEFAULT, true)).uniqueResult();
        if (!Objects.isNull(companyProduct)) {
            return companyProduct;
        } else {
            if (!Objects.isNull(parentId)) {
                Entity companyProductFamily = getCompanyProductsFamilyDD().find()
                        .createAlias(CompanyProductsFamilyFields.PRODUCT, CompanyProductsFamilyFields.PRODUCT, JoinType.LEFT)
                        .add(SearchRestrictions.eq(CompanyProductsFamilyFields.PRODUCT + L_DOT + L_ID, parentId))
                        .add(SearchRestrictions.eq(CompanyProductsFamilyFields.IS_DEFAULT, true)).uniqueResult();
                if (!Objects.isNull(companyProductFamily)) {
                    return companyProductFamily;
                }
            }
        }
        return null;
    }

    private DataDefinition getCompanyProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COMPANY_PRODUCT);
    }

    private DataDefinition getCompanyProductsFamilyDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COMPANY_PRODUCTS_FAMILY);
    }

    private String getProductAttributeValue(Entity productAttribute, Long productId) {
        Set<String> attributeValues = new HashSet<>();
        if (productAttribute != null) {
            attributeValues = getProductAttributeValueDD().find()
                    .createAlias(ProductAttributeValueFields.PRODUCT, ProductAttributeValueFields.PRODUCT, JoinType.INNER)
                    .add(SearchRestrictions.eq(ProductAttributeValueFields.PRODUCT + L_DOT + L_ID, productId))
                    .createAlias(ProductAttributeValueFields.ATTRIBUTE, ProductAttributeValueFields.ATTRIBUTE, JoinType.INNER)
                    .add(SearchRestrictions.eq(ProductAttributeValueFields.ATTRIBUTE + L_DOT + L_ID, productAttribute.getId()))
                    .list().getEntities().stream().map(e -> e.getStringField(AttributeValueFields.VALUE))
                    .collect(Collectors.toSet());
        }
        return String.join(", ", attributeValues);
    }

    private DataDefinition getProductAttributeValueDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.PRODUCT_ATTRIBUTE_VALUE);
    }

    private List<Entity> getProductBySizeGroups(final Long operationProductComponentId) {
        return getProductBySizeGroupDD().find()
                .createAlias(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT,
                        ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT, JoinType.LEFT)
                .add(SearchRestrictions.eq(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT + L_DOT + L_ID,
                        operationProductComponentId))
                .list().getEntities();
    }

    private DataDefinition getProductBySizeGroupDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_BY_SIZE_GROUP);
    }

    private void addProductTable(Document document, Entity modelCardProduct, Entity productAttribute,
            BigDecimal materialUnitCostsSum, Locale locale) throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(5);
        panelTable.setWidths(defaultModelCardProductColumnWidth);
        panelTable.addCell(
                new Phrase(translationService.translate("productFlowThruDivision.modelCard.report.product.label", locale),
                        FontUtils.getDejavuBold7Dark()));
        Entity product = modelCardProduct.getBelongsToField(ModelCardProductFields.PRODUCT);
        PdfPCell cell = new PdfPCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular9Dark()));
        cell.setColspan(3);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(4.0f);
        panelTable.addCell(cell);

        panelTable.addCell(createImageCell(product));

        panelTable.addCell(new Phrase(StringUtils.EMPTY));
        cell = new PdfPCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular9Dark()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(4.0f);
        cell.setColspan(3);
        panelTable.addCell(cell);

        Entity productModel = product.getBelongsToField(ProductFields.MODEL);
        panelTable
                .addCell(new Phrase(translationService.translate("productFlowThruDivision.modelCard.report.model.label", locale),
                        FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(productModel != null ? productModel.getStringField(ModelFields.NAME) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular9Dark()));
        Entity form = null;
        if (productModel != null) {
            form = productModel.getBelongsToField(ModelFields.FORMS);
        }
        panelTable.addCell(new Phrase(translationService.translate("productFlowThruDivision.modelCard.report.form.label", locale),
                FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(form != null ? form.getStringField(FormsFields.NUMBER) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular9Dark()));

        panelTable.addCell(
                new Phrase(translationService.translate("productFlowThruDivision.modelCard.report.quantity.label", locale),
                        FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(
                numberService.formatWithMinimumFractionDigits(modelCardProduct.getDecimalField(ModelCardProductFields.QUANTITY),
                        0) + " " + product.getStringField(ProductFields.UNIT),
                FontUtils.getDejavuRegular9Dark()));
        Entity label = null;
        if (productModel != null) {
            label = productModel.getBelongsToField(ModelFields.LABEL);
        }
        panelTable
                .addCell(new Phrase(translationService.translate("productFlowThruDivision.modelCard.report.label.label", locale),
                        FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(label != null ? label.getStringField(LabelFields.NAME) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular9Dark()));

        panelTable.addCell(new Phrase(
                translationService.translate("productFlowThruDivision.modelCard.report.materialUnitCost.label", locale),
                FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(numberService.format(materialUnitCostsSum), FontUtils.getDejavuRegular9Dark()));
        panelTable.addCell(new Phrase(getAttributeText(productAttribute), FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(
                new Phrase(getProductAttributeValue(productAttribute, product.getId()), FontUtils.getDejavuRegular9Dark()));

        cell = new PdfPCell(new Phrase(StringUtils.EMPTY));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(4.0f);
        cell.setColspan(4);
        panelTable.addCell(cell);

        document.add(panelTable);
    }

    private PdfPCell createImageCell(Entity product) {
        PdfPCell cell;
        String fileName = null;
        for (Entity productAttachment : product.getHasManyField(ProductFields.PRODUCT_ATTACHMENTS)) {
            String ext = productAttachment.getStringField(ProductAttachmentFields.EXT);
            if ("JPG".equalsIgnoreCase(ext) || "JPEG".equalsIgnoreCase(ext) || "PNG".equalsIgnoreCase(ext)) {
                fileName = productAttachment.getStringField(ProductAttachmentFields.ATTACHMENT);
                break;
            }
        }
        if (fileName != null) {
            try {
                Image img = Image.getInstance(fileName);
                if (img.getWidth() > 130 || img.getHeight() > 90) {
                    img.scaleToFit(130, 90);
                }

                cell = new PdfPCell(img);
            } catch (IOException | DocumentException e) {
                LOG.error(e.getMessage(), e);
                cell = new PdfPCell(new Phrase(StringUtils.EMPTY));
            }
        } else {
            cell = new PdfPCell(new Phrase(StringUtils.EMPTY));
        }
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(4.0f);
        cell.setRowspan(6);
        return cell;
    }
}
