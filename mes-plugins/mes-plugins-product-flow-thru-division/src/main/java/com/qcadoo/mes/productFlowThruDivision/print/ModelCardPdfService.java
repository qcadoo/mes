package com.qcadoo.mes.productFlowThruDivision.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.FormsFields;
import com.qcadoo.mes.basic.constants.LabelFields;
import com.qcadoo.mes.basic.constants.ModelFields;
import com.qcadoo.mes.basic.constants.ProductAttachmentFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SizeGroupFields;
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
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public final class ModelCardPdfService extends PdfDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(ModelCardPdfService.class);

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final int[] defaultModelCardProductColumnWidth = new int[] { 10, 20, 5, 10, 10 };

    private static final int[] defaultModelCardMaterialsColumnWidth = new int[] { 9, 6, 15, 6, 6, 4, 4, 4, 4, 5, 4, 3 };

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
            Entity technology = modelCardProduct.getBelongsToField(ModelCardProductFields.TECHNOLOGY);
            BigDecimal quantity = modelCardProduct.getDecimalField(ModelCardProductFields.QUANTITY);
            Map<OperationProductComponentHolder, BigDecimal> neededQuantities = productQuantitiesService
                    .getNeededProductQuantities(technology, product, quantity);
            Map<Long, BigDecimal> norms = com.google.common.collect.Maps.newHashMap();
            Map<Long, BigDecimal> prices = com.google.common.collect.Maps.newHashMap();
            Map<Long, BigDecimal> productBySizeGroupPrices = Maps.newHashMap();
            Map<Long, BigDecimal> materialUnitCosts = Maps.newHashMap();

            BigDecimal materialUnitCostsSum = getMaterialUnitCostsSum(modelCard, neededQuantities, norms, prices,
                    productBySizeGroupPrices, materialUnitCosts);
            addProductTable(document, modelCardProduct, productAttribute, materialUnitCostsSum, locale);
            addMaterialsTable(document, product, materialAttribute, neededQuantities, norms, prices, productBySizeGroupPrices,
                    materialUnitCosts, locale);
            document.newPage();
        }
    }

    private BigDecimal getMaterialUnitCostsSum(Entity modelCard,
            Map<OperationProductComponentHolder, BigDecimal> neededQuantities, Map<Long, BigDecimal> norms,
            Map<Long, BigDecimal> prices, Map<Long, BigDecimal> productBySizeGroupPrices,
            Map<Long, BigDecimal> materialUnitCosts) {
        BigDecimal materialUnitCostsSum = BigDecimal.ZERO;
        for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : neededQuantities.entrySet()) {
            Entity material = neededProductQuantity.getKey().getProduct();
            Entity operationProductComponent = neededProductQuantity.getKey().getOperationProductComponent();
            BigDecimal norm = operationProductComponent.getDecimalField(OperationProductInComponentFields.QUANTITY);
            norms.put(operationProductComponent.getId(), norm);
            if (Objects.isNull(material)) {
                List<Entity> productBySizeGroups = getProductBySizeGroups(operationProductComponent.getId());
                BigDecimal productBySizeGroupPricesSum = getProductBySizeGroupPricesSum(modelCard, productBySizeGroups,
                        productBySizeGroupPrices);
                BigDecimal price = numberService.setScaleWithDefaultMathContext(productBySizeGroupPricesSum
                        .divide(new BigDecimal(productBySizeGroups.size()), numberService.getMathContext()), 2);
                prices.put(operationProductComponent.getId(), price);
                BigDecimal materialUnitCost = norm.multiply(price, numberService.getMathContext());
                materialUnitCosts.put(operationProductComponent.getId(), materialUnitCost);
                materialUnitCostsSum = materialUnitCostsSum.add(materialUnitCost, numberService.getMathContext());
            } else {
                BigDecimal price = numberService.setScaleWithDefaultMathContext(productsCostCalculationService
                        .calculateProductCostPerUnit(material, modelCard.getStringField(ModelCardFields.MATERIAL_COSTS_USED),
                                modelCard.getBooleanField(ModelCardFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED)),
                        2);
                prices.put(operationProductComponent.getId(), price);
                BigDecimal materialUnitCost = norm.multiply(price, numberService.getMathContext());
                materialUnitCosts.put(operationProductComponent.getId(), materialUnitCost);
                materialUnitCostsSum = materialUnitCostsSum.add(materialUnitCost, numberService.getMathContext());
            }
        }
        return materialUnitCostsSum;
    }

    private void addMaterialsTable(Document document, Entity product, Entity materialAttribute,
            Map<OperationProductComponentHolder, BigDecimal> neededQuantities, Map<Long, BigDecimal> norms,
            Map<Long, BigDecimal> prices, Map<Long, BigDecimal> productBySizeGroupPrices, Map<Long, BigDecimal> materialUnitCosts,
            Locale locale) throws DocumentException {
        Map<String, HeaderAlignment> headersWithAlignments = getMaterialHeaders(materialAttribute, locale);

        List<String> headers = com.google.common.collect.Lists.newLinkedList(headersWithAlignments.keySet());

        PdfPTable table = pdfHelper.createTableWithHeader(headersWithAlignments.size(), headers, true,
                defaultModelCardMaterialsColumnWidth, headersWithAlignments);

        Map<Long, Map<Long, BigDecimal>> quantitiesInStock = getQuantitiesInStock(neededQuantities.keySet());

        for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : neededQuantities.entrySet()) {
            Entity material = neededProductQuantity.getKey().getProduct();
            Entity operationProductComponent = neededProductQuantity.getKey().getOperationProductComponent();
            Entity warehouse = operationProductComponent
                    .getBelongsToField(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION);
            BigDecimal neededQuantity = numberService.setScaleWithDefaultMathContext(neededProductQuantity.getValue(), 2);

            if (Objects.isNull(material)) {
                List<Entity> productBySizeGroups = getProductBySizeGroups(operationProductComponent.getId());

                addProductInputTypeRow(locale, table, operationProductComponent, neededQuantity, norms, prices,
                        materialUnitCosts);

                addProductBySizeGroupsRows(materialAttribute, table, quantitiesInStock, warehouse, productBySizeGroups,
                        productBySizeGroupPrices);
            } else {
                Entity materialAttributeValue = getProductAttributeValue(materialAttribute, product);
                addProductRow(materialAttributeValue, table, quantitiesInStock, material, operationProductComponent, warehouse,
                        neededQuantity, norms, prices, materialUnitCosts);
            }
        }
        document.add(table);
    }

    private void addProductInputTypeRow(Locale locale, PdfPTable table, Entity operationProductComponent,
            BigDecimal neededQuantity, Map<Long, BigDecimal> norms, Map<Long, BigDecimal> prices,
            Map<Long, BigDecimal> materialUnitCosts) {
        table.getDefaultCell().disableBorderSide(PdfCell.BOTTOM);
        addTechnologyInputProductTypeToReport(table, operationProductComponent);
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(
                new Phrase(translationService.translate("productFlowThruDivision.modelCard.report.productsBySize.label", locale),
                        FontUtils.getDejavuItalic7Light()));
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(
                new Phrase(numberService.format(norms.get(operationProductComponent.getId())), FontUtils.getDejavuBold7Dark()));

        table.addCell(
                new Phrase(numberService.format(prices.get(operationProductComponent.getId())), FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(numberService.format(materialUnitCosts.get(operationProductComponent.getId())),
                FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(numberService.format(neededQuantity), FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(new Phrase(operationProductComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT),
                FontUtils.getDejavuRegular7Dark()));
    }

    private void addProductRow(Entity materialAttributeValue, PdfPTable table, Map<Long, Map<Long, BigDecimal>> quantitiesInStock,
            Entity material, Entity operationProductComponent, Entity warehouse, BigDecimal neededQuantity,
            Map<Long, BigDecimal> norms, Map<Long, BigDecimal> prices, Map<Long, BigDecimal> materialUnitCosts) {
        addTechnologyInputProductTypeToReport(table, operationProductComponent);
        table.addCell(new Phrase(StringUtils.EMPTY));
        table.addCell(
                new Phrase(material.getStringField(ProductFields.NUMBER) + ", " + material.getStringField(ProductFields.NAME),
                        FontUtils.getDejavuRegular7Dark()));
        table.addCell(
                new Phrase(materialAttributeValue != null ? materialAttributeValue.getStringField(AttributeValueFields.VALUE)
                        : StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
        Entity supplier = null;
        BigDecimal minimumOrderQuantity = null;
        Entity supplierData = getSupplierData(material);

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
        table.addCell(
                new Phrase(numberService.format(norms.get(operationProductComponent.getId())), FontUtils.getDejavuBold7Dark()));
        table.addCell(
                new Phrase(numberService.format(prices.get(operationProductComponent.getId())), FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(numberService.format(materialUnitCosts.get(operationProductComponent.getId())),
                FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(numberService.format(neededQuantity), FontUtils.getDejavuBold7Dark()));
        table.addCell(new Phrase(
                numberService.format(
                        BigDecimalUtils.convertNullToZero(quantitiesInStock.get(warehouse.getId()).get(material.getId()))),
                FontUtils.getDejavuBold7Dark()));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(new Phrase(material.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Dark()));
    }

    private void addProductBySizeGroupsRows(Entity materialAttribute, PdfPTable table,
            Map<Long, Map<Long, BigDecimal>> quantitiesInStock, Entity warehouse, List<Entity> productBySizeGroups,
            Map<Long, BigDecimal> productBySizeGroupPrices) {
        table.getDefaultCell().disableBorderSide(PdfCell.TOP);
        for (Entity productBySizeGroup : productBySizeGroups) {
            Entity materialBySizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
            Entity sizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.SIZE_GROUP);
            table.addCell(new Phrase(StringUtils.EMPTY));
            table.addCell(new Phrase(sizeGroup.getStringField(SizeGroupFields.NUMBER), FontUtils.getDejavuRegular7Light()));
            table.addCell(new Phrase(materialBySizeGroup.getStringField(ProductFields.NUMBER) + ", "
                    + materialBySizeGroup.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Light()));
            Entity materialAttributeValue = getProductAttributeValue(materialAttribute, materialBySizeGroup);
            table.addCell(
                    new Phrase(materialAttributeValue != null ? materialAttributeValue.getStringField(AttributeValueFields.VALUE)
                            : StringUtils.EMPTY, FontUtils.getDejavuRegular7Light()));
            Entity supplier = null;
            BigDecimal minimumOrderQuantity = null;
            Entity supplierData = getSupplierData(materialBySizeGroup);

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
            table.addCell(
                    new Phrase(minimumOrderQuantity != null ? numberService.format(minimumOrderQuantity) : StringUtils.EMPTY,
                            FontUtils.getDejavuRegular7Light()));
            table.addCell(new Phrase(StringUtils.EMPTY));
            table.addCell(new Phrase(numberService.format(productBySizeGroupPrices.get(productBySizeGroup.getId())),
                    FontUtils.getDejavuRegular7Light()));
            table.addCell(new Phrase(StringUtils.EMPTY));
            table.addCell(new Phrase(StringUtils.EMPTY));
            table.addCell(new Phrase(
                    numberService.format(BigDecimalUtils
                            .convertNullToZero(quantitiesInStock.get(warehouse.getId()).get(materialBySizeGroup.getId()))),
                    FontUtils.getDejavuRegular7Light()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(new Phrase(materialBySizeGroup.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Light()));
        }
        table.getDefaultCell().enableBorderSide(PdfCell.BOTTOM);
        table.getDefaultCell().enableBorderSide(PdfCell.TOP);
    }

    private BigDecimal getProductBySizeGroupPricesSum(Entity modelCard, List<Entity> productBySizeGroups,
            Map<Long, BigDecimal> productBySizeGroupPrices) {
        BigDecimal productBySizeGroupPricesSum = BigDecimal.ZERO;
        for (Entity productBySizeGroup : productBySizeGroups) {
            Entity materialBySizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
            BigDecimal materialBySizeGroupPrice = productsCostCalculationService.calculateProductCostPerUnit(materialBySizeGroup,
                    modelCard.getStringField(ModelCardFields.MATERIAL_COSTS_USED),
                    modelCard.getBooleanField(ModelCardFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED));
            productBySizeGroupPrices.put(productBySizeGroup.getId(),
                    numberService.setScaleWithDefaultMathContext(materialBySizeGroupPrice, 2));
            productBySizeGroupPricesSum = productBySizeGroupPricesSum.add(materialBySizeGroupPrice,
                    numberService.getMathContext());
        }
        return productBySizeGroupPricesSum;
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
        headersWithAlignments.put(
                materialAttribute != null ? materialAttribute.getStringField(AttributeFields.NUMBER) : StringUtils.EMPTY,
                HeaderAlignment.LEFT);
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

    private Entity getSupplierData(Entity material) {
        Entity companyProduct = getCompanyProductDD().find()
                .createAlias(CompanyProductFields.PRODUCT, CompanyProductFields.PRODUCT, JoinType.LEFT)
                .add(SearchRestrictions.eq(CompanyProductFields.PRODUCT + L_DOT + L_ID, material.getId()))
                .add(SearchRestrictions.eq(CompanyProductFields.IS_DEFAULT, true)).uniqueResult();
        if (!Objects.isNull(companyProduct)) {
            return companyProduct;
        } else {
            Entity parent = material.getBelongsToField(ProductFields.PARENT);
            if (!Objects.isNull(parent)) {
                Entity companyProductFamily = getCompanyProductsFamilyDD().find()
                        .createAlias(CompanyProductsFamilyFields.PRODUCT, CompanyProductsFamilyFields.PRODUCT, JoinType.LEFT)
                        .add(SearchRestrictions.eq(CompanyProductsFamilyFields.PRODUCT + L_DOT + L_ID, parent.getId()))
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

    private Entity getProductAttributeValue(Entity productAttribute, Entity product) {
        Entity productAttributeValue = null;
        if (productAttribute != null) {
            for (Entity pav : product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES)) {
                if (pav.getBelongsToField(AttributeValueFields.ATTRIBUTE).getId().equals(productAttribute.getId())) {
                    productAttributeValue = pav;
                    break;
                }
            }
        }
        return productAttributeValue;
    }

    private void addTechnologyInputProductTypeToReport(PdfPTable table, Entity operationProductComponent) {
        Entity technologyInputProductType = operationProductComponent
                .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
        String description = operationProductComponent.getStringField(OperationProductInComponentFields.DESCRIPTION);
        if (technologyInputProductType != null) {
            if (!StringUtils.isEmpty(description)) {
                table.addCell(new Phrase(technologyInputProductType.getStringField(TechnologyInputProductTypeFields.NAME) + " ("
                        + description + ")", FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase(technologyInputProductType.getStringField(TechnologyInputProductTypeFields.NAME),
                        FontUtils.getDejavuRegular7Dark()));
            }
        } else {
            if (!StringUtils.isEmpty(description)) {
                table.addCell(new Phrase(" (" + description + ")", FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
            }
        }
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
        Entity productAttributeValue = getProductAttributeValue(productAttribute, product);
        panelTable.addCell(
                new Phrase(productAttribute != null ? productAttribute.getStringField(AttributeFields.NUMBER) : StringUtils.EMPTY,
                        FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(
                new Phrase(productAttributeValue != null ? productAttributeValue.getStringField(AttributeValueFields.VALUE)
                        : StringUtils.EMPTY, FontUtils.getDejavuRegular9Dark()));

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
