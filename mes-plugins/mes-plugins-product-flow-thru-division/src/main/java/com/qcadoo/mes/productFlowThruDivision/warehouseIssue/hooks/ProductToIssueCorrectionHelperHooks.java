package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductToIssueCorrectionFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.model.api.*;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

@Service
public class ProductToIssueCorrectionHelperHooks {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity helper = form.getEntity();
        FieldComponent locationFromLabel = (FieldComponent) view.getComponentByReference("locationFromLabel");
        Entity locationFrom = helper.getBelongsToField("locationFrom");
        if (locationFrom != null) {
            locationFromLabel.setFieldValue(translationService.translate(
                    "productFlowThruDivision.productToIssueCorrectionHelper.locationFrom.label", LocaleContextHolder.getLocale(),
                    locationFrom.getStringField(LocationFields.NUMBER)));
            locationFromLabel.setRequired(true);
        }
        if (helper.getHasManyField("corrections").isEmpty()) {
            String idsStr = helper.getStringField("productsToIssueIds");
            String[] split = idsStr.split(",");
            List<Long> ids = Lists.newArrayList(split).stream().map(Long::valueOf).collect(Collectors.toList());
            DataDefinition correctionDD = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                    ProductFlowThruDivisionConstants.MODEL_PRODUCT_TO_ISSUE_CORRECTION);
            DataDefinition productToIssueDD = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                    ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE);
            List<Entity> createdCorrections = Lists.newArrayList();
            for (Long id : ids) {
                Entity productToIssue = productToIssueDD.get(id);
                createdCorrections.add(createCorrections(correctionDD, productToIssue));
            }
            helper.setField("corrections", createdCorrections);
            form.setEntity(helper);
        } else {
            List<Entity> issues = helper.getHasManyField("corrections");
            if (issues.stream().allMatch(issue -> issue.getId() != null)) {
                WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
                Ribbon ribbon = window.getRibbon();
                RibbonGroup group = ribbon.getGroupByName("actions");
                RibbonActionItem saveItem = group.getItemByName("correct");
                saveItem.setEnabled(false);
                saveItem.requestUpdate(true);
            }
            fillQuantitiesInAdditionalUnit(view);
        }
        fillUnits(view);
        setWarehouseCriteriaModifier(view, helper);
    }

    private void setWarehouseCriteriaModifier(final ViewDefinitionState view, Entity helper) {
        LookupComponent warehouseLookup = (LookupComponent) view.getComponentByReference("locationTo");
        Entity placeOfIssue = helper.getBelongsToField("placeOfIssue");
        OptionalLong locationTo = helper.getHasManyField("corrections").stream()
                .mapToLong(correction -> correction.getBelongsToField(ProductToIssueCorrectionFields.LOCATION).getId()).findAny();

        FilterValueHolder filter = warehouseLookup.getFilterValue();
        filter.put("locationFrom", placeOfIssue.getId());

        if (locationTo.isPresent()) {
            filter.put("locationTo", locationTo.getAsLong());
        }
        warehouseLookup.setFilterValue(filter);
    }

    private Entity createCorrections(final DataDefinition correctionDD, Entity productToIssue) {

        Entity correction = correctionDD.create();
        BigDecimal demandQuantity = productToIssue.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
        BigDecimal correctionsSum = Optional.ofNullable(productToIssue.getDecimalField(ProductsToIssueFields.CORRECTION)).orElse(
                BigDecimal.ZERO);
        BigDecimal issuedQuantity = Optional.ofNullable(productToIssue.getDecimalField(ProductsToIssueFields.ISSUE_QUANTITY))
                .orElse(BigDecimal.ZERO);
        BigDecimal quantityToIssue = demandQuantity.subtract(correctionsSum).subtract(issuedQuantity);
        correction.setField(ProductToIssueCorrectionFields.PRODUCT,
                productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT));
        correction.setField(ProductToIssueCorrectionFields.WAREHOUSE_ISSUE,
                productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE));
        correction.setField(ProductToIssueCorrectionFields.PRODUCTS_TO_ISSUE, productToIssue);
        correction.setField(ProductToIssueCorrectionFields.DEMAND_QUANTITY, demandQuantity);
        correction.setField(ProductToIssueCorrectionFields.QUANTITY_TO_ISSUE, quantityToIssue);

        correction.setField(ProductToIssueCorrectionFields.CORRECTION_QUANTITY, quantityToIssue);

        BigDecimal conversion = productToIssue.getDecimalField(ProductsToIssueFields.CONVERSION);
        BigDecimal correctionInAdditionalUnit = quantityToIssue.multiply(conversion, numberService.getMathContext());
        correction.setField(ProductToIssueCorrectionFields.CORRECTION_QUANTITY_IN_ADDITIONAL_UNIT, correctionInAdditionalUnit);
        correction.setField(ProductToIssueCorrectionFields.CONVERSION, conversion);
        correction.setField(ProductToIssueCorrectionFields.LOCATION,
                productToIssue.getBelongsToField(ProductsToIssueFields.LOCATION));
        return correction;
    }

    public void fillQuantitiesInAdditionalUnit(final ViewDefinitionState view) {

        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("corrections");
        for (FormComponent form : adl.getFormComponents()) {
            Entity correction = form.getPersistedEntityWithIncludedFormValues();
            FieldComponent quantityField = form.findFieldComponentByName(ProductToIssueCorrectionFields.CORRECTION_QUANTITY);

            Either<Exception, com.google.common.base.Optional<BigDecimal>> maybeQuantity = BigDecimalUtils.tryParse(quantityField.getFieldValue()
                    .toString(), LocaleContextHolder.getLocale());
            BigDecimal quantityAdditionalUnit = BigDecimal.ZERO;
            BigDecimal conversion = correction.getDecimalField(IssueFields.CONVERSION);
            if (conversion != null && maybeQuantity.isRight() && maybeQuantity.getRight().isPresent()) {
                BigDecimal correctionQuantity = maybeQuantity.getRight().get();
                quantityAdditionalUnit = numberService.setScaleWithDefaultMathContext(correctionQuantity.multiply(conversion,
                        numberService.getMathContext()));

                correction.setField(ProductToIssueCorrectionFields.CORRECTION_QUANTITY_IN_ADDITIONAL_UNIT, quantityAdditionalUnit);
                form.setEntity(correction);
            }
        }
    }

    public void fillUnits(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("corrections");
        for (FormComponent form : adl.getFormComponents()) {
            Entity correction = form.getPersistedEntityWithIncludedFormValues();
            Entity product = correction.getBelongsToField(ProductToIssueCorrectionFields.PRODUCT);
            if (product != null) {
                FieldComponent unitField = form.findFieldComponentByName(ProductFields.UNIT);
                FieldComponent additionalUnitField = form.findFieldComponentByName(ProductFields.ADDITIONAL_UNIT);
                unitField.setFieldValue(product.getStringField(ProductFields.UNIT));
                String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
                if (StringUtils.isEmpty(additionalUnit)) {
                    additionalUnitField.setFieldValue(product.getStringField(ProductFields.UNIT));
                } else {
                    additionalUnitField.setFieldValue(additionalUnit);
                }
            }
        }
    }

}
