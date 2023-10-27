package com.qcadoo.mes.productFlowThruDivision.warehouseIssue;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.CollectionProducts;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductToIssueCorrectionFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductToIssueCorrectionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private WarehouseIssueGenerator warehouseIssueGenerator;

    @Transactional
    public List<Entity> correctProductsToIssue(final Entity helper, final List<Entity> corrections) {
        Entity newWarehouseIssue = createCorrectedWarehouseIssue(helper);
        Entity locationTo = helper.getBelongsToField("locationTo");

        List<Entity> correctedProductsToIssue = Lists.newArrayList();
        List<Entity> createdWarehouseIssues = Lists.newArrayList();

        for (Entity correction : corrections) {
            Entity productsToIssue = correction.getBelongsToField(ProductToIssueCorrectionFields.PRODUCTS_TO_ISSUE);

            BigDecimal correctionQuantity = correction.getDecimalField(ProductToIssueCorrectionFields.CORRECTION_QUANTITY);
            BigDecimal previousQuantity = BigDecimalUtils
                    .convertNullToZero(productsToIssue.getDecimalField(ProductsToIssueFields.CORRECTION));

            productsToIssue.setField(ProductsToIssueFields.CORRECTION, correctionQuantity.add(previousQuantity));
            productsToIssue.getDataDefinition().save(productsToIssue);

            if (shouldCreateNewWarehouseIssue(correctedProductsToIssue, correction)) {
                newWarehouseIssue.setField(WarehouseIssueFields.PRODUCTS_TO_ISSUES, correctedProductsToIssue);
                Entity saved = newWarehouseIssue.getDataDefinition().save(newWarehouseIssue);

                if (!saved.isValid()) {
                    throw new IllegalStateException("Warehouse issue correction failed.");
                }

                createdWarehouseIssues.add(saved);

                newWarehouseIssue = createCorrectedWarehouseIssue(helper);

                correctedProductsToIssue = Lists.newArrayList();
            }

            createOrUpdateCorrectedProductToIssue(correctedProductsToIssue, locationTo, newWarehouseIssue, correction);
        }

        newWarehouseIssue.setField(WarehouseIssueFields.PRODUCTS_TO_ISSUES, correctedProductsToIssue);

        Entity saved = newWarehouseIssue.getDataDefinition().save(newWarehouseIssue);

        createdWarehouseIssues.add(saved);

        if (saved.isValid()) {
            return createdWarehouseIssues;
        }

        throw new IllegalStateException("Warehouse issue correction failed.");
    }

    private boolean shouldCreateNewWarehouseIssue(final List<Entity> correctedProductsToIssue, final Entity correction) {
        return correctedProductsToIssue.stream()
                .anyMatch(productToIssue -> correction.getBelongsToField(ProductToIssueCorrectionFields.PRODUCT).getId()
                        .equals(productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT).getId())
                        && !correction.getDecimalField(ProductToIssueCorrectionFields.CONVERSION)
                        .equals(productToIssue.getDecimalField(ProductsToIssueFields.CONVERSION)));
    }

    private Entity createCorrectedWarehouseIssue(final Entity helper) {
        Entity newWarehouseIssue = getWarehouseIssueDD().create();

        newWarehouseIssue.setField(WarehouseIssueFields.NUMBER, warehouseIssueGenerator.setNumberFromSequence());
        newWarehouseIssue.setField(WarehouseIssueFields.PLACE_OF_ISSUE, helper.getBelongsToField("placeOfIssue"));
        newWarehouseIssue.setField(WarehouseIssueFields.DESCRIPTION,
                translationService.translate(
                        "productFlowThruDivision.productToIssueCorrectionHelperDetails.correction.description",
                        LocaleContextHolder.getLocale()));
        newWarehouseIssue.setField(WarehouseIssueFields.COLLECTION_PRODUCTS, CollectionProducts.ON_ORDER.getStringValue());

        return newWarehouseIssue;
    }

    private void createOrUpdateCorrectedProductToIssue(final List<Entity> correctedProductsToIssue, final Entity locationTo,
                                                       final Entity warehouseIssue, final Entity correction) {
        Entity product = correction.getBelongsToField(ProductToIssueCorrectionFields.PRODUCT);

        BigDecimal conversion = correction.getDecimalField(ProductToIssueCorrectionFields.CONVERSION);
        BigDecimal correctionQuantity = correction.getDecimalField(ProductToIssueCorrectionFields.CORRECTION_QUANTITY);
        BigDecimal correctionQuantityInAdditionalUnit = correction
                .getDecimalField(ProductToIssueCorrectionFields.CORRECTION_QUANTITY_IN_ADDITIONAL_UNIT);

        Optional<Entity> existingProductToIssue = correctedProductsToIssue.stream().filter(productToIssue -> productToIssue
                        .getBelongsToField(ProductsToIssueFields.PRODUCT).getId().equals(product.getId())
                        && productToIssue.getDecimalField(ProductsToIssueFields.CONVERSION).compareTo(conversion) == 0)
                .findAny();

        if (existingProductToIssue.isPresent()) {
            Entity existing = existingProductToIssue.get();

            BigDecimal oldQuantity = existing.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
            BigDecimal oldQuantityAdditionalUnit = existing.getDecimalField(ProductsToIssueFields.ADDITIONAL_DEMAND_QUANTITY);

            existing.setField(ProductsToIssueFields.DEMAND_QUANTITY, oldQuantity.add(correctionQuantity));
            existing.setField(ProductsToIssueFields.ADDITIONAL_DEMAND_QUANTITY,
                    oldQuantityAdditionalUnit.add(correctionQuantityInAdditionalUnit));
        } else {
            Entity productToIssue = getProductsToIssueDD().create();

            productToIssue.setField(ProductsToIssueFields.PRODUCT, product);
            productToIssue.setField(ProductsToIssueFields.WAREHOUSE_ISSUE, warehouseIssue);
            productToIssue.setField(ProductsToIssueFields.DEMAND_QUANTITY, correctionQuantity);
            productToIssue.setField(ProductsToIssueFields.ADDITIONAL_DEMAND_QUANTITY, correctionQuantityInAdditionalUnit);
            productToIssue.setField(ProductsToIssueFields.LOCATION, locationTo);
            productToIssue.setField(ProductsToIssueFields.CONVERSION, conversion);

            correctedProductsToIssue.add(productToIssue);
        }
    }

    private DataDefinition getWarehouseIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_WAREHOUSE_ISSUE);
    }

    private DataDefinition getProductsToIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE);
    }

}
