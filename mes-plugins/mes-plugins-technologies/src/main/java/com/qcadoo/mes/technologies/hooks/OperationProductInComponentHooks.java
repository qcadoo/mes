/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologies.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.validators.TechnologyTreeValidators;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;

@Service
public class OperationProductInComponentHooks {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidators technologyTreeValidators;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition operationProductInComponentDD, final Entity operationProductInComponent) {
        setDifferentProductsInDifferentSizes(operationProductInComponent);
        clearProductBySizeGroups(operationProductInComponent);
        copyQuantityToProductsBySize(operationProductInComponentDD, operationProductInComponent);
    }

    public void onViewDto(final DataDefinition operationProductInComponentDD, final Entity operationProductInComponent) {
        if (Strings.isNullOrEmpty(operationProductInComponent.getStringField("productNumber"))
                && Strings.isNullOrEmpty(operationProductInComponent.getStringField("productName"))) {
            Entity opic = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).get(operationProductInComponent.getId());
            List<Entity> productsBySize = opic.getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS);
            if (!productsBySize.isEmpty()) {
                Entity product = productsBySize.get(0).getBelongsToField(ProductBySizeGroupFields.PRODUCT);

                boolean allSameName = productsBySize.stream().map(pg -> pg.getBelongsToField(ProductBySizeGroupFields.PRODUCT)
                                .getStringField(ProductFields.NUMBER))
                        .allMatch(x -> x.equals(product.getStringField(ProductFields.NUMBER)));
                if(allSameName) {
                    operationProductInComponent.setField("productNumber", product.getStringField(ProductFields.NUMBER));
                    operationProductInComponent.setField("productName", product.getStringField(ProductFields.NAME));
                }
            }
        }
    }

    private void copyQuantityToProductsBySize(final DataDefinition operationProductInComponentDD,
                                              final Entity operationProductInComponent) {
        if (Objects.isNull(
                operationProductInComponent.getField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE))) {
            operationProductInComponent.setField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE, false);
        }

        if (operationProductInComponent.getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES)
                && !operationProductInComponent
                .getBooleanField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE)
                && Objects.nonNull(operationProductInComponent.getId())) {
            Entity operationProductInComponentDb = operationProductInComponentDD.get(operationProductInComponent.getId());
            if (Objects.isNull(operationProductInComponentDb.getDecimalField(OperationProductInComponentFields.QUANTITY))
                    || !operationProductInComponentDb.getDecimalField(OperationProductInComponentFields.QUANTITY)
                    .equals(operationProductInComponent.getDecimalField(OperationProductInComponentFields.QUANTITY))) {

                operationProductInComponent.getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS)
                        .forEach(pbs -> {
                            String opicUnit = operationProductInComponent
                                    .getStringField(OperationProductInComponentFields.GIVEN_UNIT);
                            String productBySizeGroupUnit = pbs.getStringField(ProductBySizeGroupFields.GIVEN_UNIT);
                            if (opicUnit.equals(productBySizeGroupUnit)) {
                                pbs.setField(ProductBySizeGroupFields.GIVEN_QUANTITY, operationProductInComponent
                                        .getDecimalField(OperationProductInComponentFields.GIVEN_QUANTITY));
                                pbs.setField(ProductBySizeGroupFields.GIVEN_UNIT,
                                        operationProductInComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT));
                                calculateQuantityForPBSG(pbs);
                            } else {
                                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(opicUnit,
                                        searchCriteriaBuilder -> searchCriteriaBuilder
                                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT,
                                                        pbs.getBelongsToField(ProductBySizeGroupFields.PRODUCT))));
                                if (unitConversions.isDefinedFor(productBySizeGroupUnit)) {
                                    BigDecimal convertedQuantity = unitConversions
                                            .convertTo(
                                                    operationProductInComponent
                                                            .getDecimalField(OperationProductInComponentFields.GIVEN_QUANTITY),
                                                    productBySizeGroupUnit);
                                    pbs.setField(ProductBySizeGroupFields.GIVEN_QUANTITY, convertedQuantity);
                                    calculateQuantityForPBSG(pbs);
                                }
                            }
                            pbs.getDataDefinition().save(pbs);

                        });
            }
        }
    }

    private void calculateQuantityForPBSG(final Entity productBySizeGroup) {
        Entity product = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
        String givenUnit = productBySizeGroup.getStringField(ProductBySizeGroupFields.GIVEN_UNIT);

        BigDecimal givenQuantity = productBySizeGroup.getDecimalField(ProductBySizeGroupFields.GIVEN_QUANTITY);

        if (Objects.nonNull(product)) {
            String baseUnit = product.getStringField(ProductFields.UNIT);

            if (baseUnit.equals(givenUnit)) {
                productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, givenQuantity);
            } else {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(givenUnit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                if (unitConversions.isDefinedFor(baseUnit)) {
                    BigDecimal convertedQuantity = unitConversions.convertTo(givenQuantity, baseUnit);

                    productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, convertedQuantity);
                } else {
                    productBySizeGroup.addError(
                            productBySizeGroup.getDataDefinition().getField(ProductBySizeGroupFields.GIVEN_QUANTITY),
                            "technologies.operationProductInComponent.validate.error.missingUnitConversion");

                    productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, null);
                }
            }
        } else {
            productBySizeGroup.setField(ProductBySizeGroupFields.QUANTITY, givenQuantity);
            productBySizeGroup.setField(ProductBySizeGroupFields.UNIT, givenUnit);
        }

    }

    private void setDifferentProductsInDifferentSizes(final Entity operationProductInComponent) {
        if (Objects.isNull(
                operationProductInComponent.getField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES))) {
            operationProductInComponent.setField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES, false);
        }
    }

    private void clearProductBySizeGroups(final Entity operationProductInComponent) {
        boolean differentProductsInDifferentSizes = operationProductInComponent
                .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);
        List<Entity> productBySizeGroups = operationProductInComponent
                .getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS);

        if (!differentProductsInDifferentSizes && !productBySizeGroups.isEmpty()) {
            operationProductInComponent.setField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS, Lists.newArrayList());
        }
    }

    public boolean validatesWith(final DataDefinition operationProductInComponentDD, final Entity operationProductInComponent) {
        boolean isValid = true;

        isValid = isValid && checkIfTechnologyInputProductTypeOrProductIsSelected(operationProductInComponentDD,
                operationProductInComponent);
        isValid = isValid && checkIfOperationInputProductTypeIsUnique(operationProductInComponentDD, operationProductInComponent);
        isValid = isValid && technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(operationProductInComponentDD,
                operationProductInComponent);
        isValid = isValid
                && technologyTreeValidators.invalidateIfWrongFormula(operationProductInComponentDD, operationProductInComponent);
        isValid = isValid && technologyService.invalidateIfAlreadyInTheSameOperation(operationProductInComponentDD,
                operationProductInComponent);
        isValid = isValid && checkIfProductBySizeFilled(operationProductInComponentDD, operationProductInComponent);
        isValid = isValid && checkIfGivenUnitChanged(operationProductInComponentDD, operationProductInComponent);

        return isValid;
    }

    private boolean checkIfProductBySizeFilled(DataDefinition operationProductInComponentDD, Entity operationProductInComponent) {
        if (!operationProductInComponent.getBooleanField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE)
                && !operationProductInComponent.getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS)
                .isEmpty()
                && (Objects.isNull(operationProductInComponent.getDecimalField(OperationProductInComponentFields.GIVEN_QUANTITY))
                || StringUtils.isEmpty(
                operationProductInComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT)))) {
            operationProductInComponent
                    .addGlobalError("technologies.operationProductInComponent.error.productBySizeFilledAndQuantityIsNull");
            return false;
        }
        return true;
    }

    private boolean checkIfGivenUnitChanged(DataDefinition operationProductInComponentDD, Entity operationProductInComponent) {
        if (operationProductInComponent.getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES)
                && !operationProductInComponent
                .getBooleanField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE)
                && Objects.nonNull(operationProductInComponent.getId())) {
            Entity operationProductInComponentDb = operationProductInComponentDD.get(operationProductInComponent.getId());

            if (StringUtils.isEmpty(operationProductInComponentDb.getStringField(OperationProductInComponentFields.GIVEN_UNIT))
                    || !operationProductInComponentDb.getStringField(OperationProductInComponentFields.GIVEN_UNIT)
                    .equals(operationProductInComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT))) {

                String opicUnit = operationProductInComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT);

                for (Entity pbsg : operationProductInComponent
                        .getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS)) {
                    String productBySizeGroupUnit = pbsg.getBelongsToField(ProductBySizeGroupFields.PRODUCT)
                            .getStringField(ProductFields.UNIT);
                    if (StringUtils.isEmpty(opicUnit)) {
                        operationProductInComponent.addGlobalError(
                                "technologies.operationProductInComponent.error.unitChangedRemoveProductsBySizeGroup");

                        return false;
                    }
                    if (!opicUnit.equals(productBySizeGroupUnit)) {
                        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(
                                productBySizeGroupUnit,
                                searchCriteriaBuilder -> searchCriteriaBuilder
                                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT,
                                                pbsg.getBelongsToField(ProductBySizeGroupFields.PRODUCT))));

                        if (!unitConversions.isDefinedFor(opicUnit)) {
                            operationProductInComponent.addGlobalError(
                                    "technologies.operationProductInComponent.error.unitChangedRemoveProductsBySizeGroup");

                            return false;
                        }
                    }

                }
            }

        }
        return true;
    }

    private boolean checkIfOperationInputProductTypeIsUnique(final DataDefinition operationProductInComponentDD,
                                                             final Entity operationProductInComponent) {
        Entity operationComponent = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);

        Entity technologyInputProductType = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);

        if (Objects.isNull(technologyInputProductType)) {
            return true;
        }

        Long operationProductInComponentId = operationProductInComponent.getId();

        SearchCriteriaBuilder searchCriteriaBuilder = operationProductInComponentDD.find();

        searchCriteriaBuilder
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.OPERATION_COMPONENT, operationComponent));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE,
                technologyInputProductType));

        if (Objects.nonNull(operationProductInComponentId)) {
            searchCriteriaBuilder.add(SearchRestrictions.idNe(operationProductInComponentId));
        }

        searchCriteriaBuilder.setProjection(alias(id(), "id"));

        List<Entity> operationProductInComponents = searchCriteriaBuilder.list().getEntities();

        if (!operationProductInComponents.isEmpty()) {
            operationProductInComponent
                    .addGlobalError("technologies.operationProductInComponent.error.inputProductTypeNotUnique");

            return false;
        }

        return true;
    }

    private boolean checkIfTechnologyInputProductTypeOrProductIsSelected(final DataDefinition operationProductInComponentDD,
                                                                         final Entity operationProductInComponent) {
        boolean differentProductsInDifferentSizes = operationProductInComponent
                .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);
        Entity technologyInputProductType = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
        Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

        if ((differentProductsInDifferentSizes && Objects.isNull(technologyInputProductType))
                || (Objects.isNull(technologyInputProductType) && Objects.isNull(product))) {
            operationProductInComponent.addError(
                    operationProductInComponentDD.getField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE),
                    "technologies.operationProductInComponent.error.requiredFieldNotSelected");
            operationProductInComponent.addError(
                    operationProductInComponentDD.getField(OperationProductInComponentFields.PRODUCT),
                    "technologies.operationProductInComponent.error.requiredFieldNotSelected");

            return false;
        }

        return true;
    }

}
