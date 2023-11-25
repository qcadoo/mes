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
package com.qcadoo.mes.deliveriesToMaterialFlow.states;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DocumentFieldsDTMF;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.PositionAttributeValueFields;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DeliveryStateServiceMF {

    private static final String L_QUALITY_RATING = "qualityRating";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private DocumentManagementService documentManagementService;

    public void createDocumentsForTheReceivedProducts(final StateChangeContext stateChangeContext) {
        final Entity delivery = stateChangeContext.getOwner();

        try {
            createDocumentForDeliveredProducts(stateChangeContext, delivery);

            if (!delivery.isValid()) {
                stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            }
        } catch (Exception ex) {
            stateChangeContext.addValidationError("productFlowThruDivision.deliveries.warehouseIssue.issuesCreated.error");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createDocumentForDeliveredProducts(final StateChangeContext stateChangeContext, final Entity delivery) {
        Entity location = getLocation(delivery);

        if (Objects.isNull(location)) {
            return;
        }

        Entity currency = getCurrency(delivery);

        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder();

        documentBuilder.receipt(location);
        documentBuilder.setField(DocumentFieldsDTMF.DELIVERY, delivery);
        documentBuilder.setField(DocumentFields.COMPANY, delivery.getField(DeliveryFields.SUPPLIER));

        for (Entity deliveredProduct : deliveredProducts) {
            BigDecimal quantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);

            Optional<BigDecimal> damagedQuantity = Optional
                    .ofNullable(deliveredProduct.getDecimalField(DeliveredProductFields.DAMAGED_QUANTITY));

            BigDecimal positionQuantity = quantity.subtract(damagedQuantity.orElse(BigDecimal.ZERO),
                    numberService.getMathContext());

            if (positionQuantity.compareTo(BigDecimal.ZERO) > 0) {
                Entity product = getProduct(deliveredProduct);
                String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
                BigDecimal givenQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY);
                BigDecimal conversion = deliveredProduct.getDecimalField(DeliveredProductFields.CONVERSION);

                if (StringUtils.isEmpty(additionalUnit)) {
                    additionalUnit = product.getStringField(ProductFields.UNIT);
                }

                List<Entity> attributes = prepareAttributes(deliveredProduct);

                documentBuilder.addPosition(product, positionQuantity,
                        numberService.setScaleWithDefaultMathContext(givenQuantity), additionalUnit, conversion,
                        currencyService.getConvertedValue(deliveredProduct.getDecimalField(DeliveredProductFields.PRICE_PER_UNIT),
                                currency),
                        getBatch(deliveredProduct), getProductionDate(deliveredProduct), getExpirationDate(deliveredProduct),
                        null, getStorageLocation(deliveredProduct), getPalletNumber(deliveredProduct),
                        getTypeOfPallet(deliveredProduct), isWaste(deliveredProduct),
                        deliveredProduct.getStringField(L_QUALITY_RATING), attributes);
            }
        }

        Entity createdDocument = documentBuilder.setAccepted().build();

        if (!createdDocument.isValid()) {
            delivery.addGlobalError("deliveriesToMaterialFlow.deliveryStateValidator.error.document", true);

            for (ErrorMessage error : createdDocument.getGlobalErrors()) {
                delivery.addGlobalError(error.getMessage(), error.getAutoClose());
            }
        }
    }

    private List<Entity> prepareAttributes(final Entity deliveredProduct) {
        List<Entity> attributes = Lists.newArrayList();

        deliveredProduct.getHasManyField(DeliveredProductFields.DELIVERED_PRODUCT_ATTRIBUTE_VALS).forEach(aVal -> {
            Entity deliveredProductAttributeVal = getDeliveredProductAttributeValDD()
                    .create();

            deliveredProductAttributeVal.setField(PositionAttributeValueFields.ATTRIBUTE,
                    aVal.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE).getId());

            if (Objects.nonNull(aVal.getBelongsToField(PositionAttributeValueFields.ATTRIBUTE_VALUE))) {
                deliveredProductAttributeVal.setField(PositionAttributeValueFields.ATTRIBUTE_VALUE,
                        aVal.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE_VALUE).getId());
            }

            deliveredProductAttributeVal.setField(PositionAttributeValueFields.VALUE,
                    aVal.getStringField(DeliveredProductAttributeValFields.VALUE));

            attributes.add(deliveredProductAttributeVal);
        });

        return attributes;
    }

    public void validateRequiredParameters(final StateChangeContext stateChangeContext) {
        final Entity delivery = stateChangeContext.getOwner();

        Entity location = getLocation(delivery);

        if (Objects.isNull(location)) {
            return;
        }

        boolean isBatchRequired = isRequired(location, LocationFieldsMFR.REQUIRE_BATCH);
        boolean isProductionDateRequired = isRequired(location, LocationFieldsMFR.REQUIRE_PRODUCTION_DATE);
        boolean isExpirationDateRequired = isRequired(location, LocationFieldsMFR.REQUIRE_EXPIRATION_DATE);
        boolean isPriceRequired = isRequired(location, LocationFieldsMFR.REQUIRE_PRICE);

        if (isBatchRequired || isExpirationDateRequired || isPriceRequired || isProductionDateRequired) {
            List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
            List<String> missingBatch = Lists.newArrayList();
            List<String> missingProductionDate = Lists.newArrayList();
            List<String> missingExpirationDate = Lists.newArrayList();
            List<String> missingPrice = Lists.newArrayList();

            for (Entity deliveredProduct : deliveredProducts) {
                String productName = getProductName(deliveredProduct);

                if (isBatchRequired && Objects.isNull(getBatch(deliveredProduct))) {
                    missingBatch.add(productName);
                }
                if (isProductionDateRequired && Objects.isNull(getProductionDate(deliveredProduct))) {
                    missingProductionDate.add(productName);
                }
                if (isExpirationDateRequired && Objects.isNull(getExpirationDate(deliveredProduct))) {
                    missingExpirationDate.add(productName);
                }
                if (isPriceRequired && Objects.isNull(currencyService.getConvertedValue(
                        deliveredProduct.getDecimalField(DeliveredProductFields.PRICE_PER_UNIT), getCurrency(delivery)))) {
                    missingPrice.add(productName);
                }
            }

            String locationName = getLocationName(location);
            addErrorMessage(stateChangeContext, missingBatch, locationName,
                    "deliveriesToMaterialFlow.deliveryStateValidator.missing.batch");
            addErrorMessage(stateChangeContext, missingProductionDate, locationName,
                    "deliveriesToMaterialFlow.deliveryStateValidator.missing.productionDate");
            addErrorMessage(stateChangeContext, missingExpirationDate, locationName,
                    "deliveriesToMaterialFlow.deliveryStateValidator.missing.expirationDate");
            addErrorMessage(stateChangeContext, missingPrice, locationName,
                    "deliveriesToMaterialFlow.deliveryStateValidator.missing.price");
        }
    }

    public void validateReceivedPackages(final StateChangeContext stateChangeContext) {
        final Entity delivery = stateChangeContext.getOwner();

        Entity packagingLocation = getPackagingLocation();

        List<Entity> deliveredPackages = delivery.getHasManyField(DeliveryFields.DELIVERED_PACKAGES);

        if (!deliveredPackages.isEmpty()) {
            if (Objects.isNull(packagingLocation)) {
                stateChangeContext.addValidationError("deliveriesToMaterialFlow.deliveryStateValidator.error.packagingLocationNotSet");
            }
        }
    }

    private void addErrorMessage(final StateChangeContext stateChangeContext, final List<String> message,
                                 final String locationName, final String translationKey) {
        if (message.size() != 0) {
            if (message.toString().length() < 255) {
                stateChangeContext.addValidationError(translationKey, false, locationName, message.toString());
            } else {
                stateChangeContext.addValidationError(translationKey + "Short", false, locationName);
            }
        }
    }

    public void createDocumentsForTheReceivedPackages(final StateChangeContext stateChangeContext) {
        final Entity delivery = stateChangeContext.getOwner();

        createDocumentForDeliveredPackages(delivery);

        if (!delivery.isValid()) {
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createDocumentForDeliveredPackages(final Entity delivery) {
        Entity packagingLocation = getPackagingLocation();

        List<Entity> deliveredPackages = delivery.getHasManyField(DeliveryFields.DELIVERED_PACKAGES);

        if (!deliveredPackages.isEmpty()) {
            if (Objects.nonNull(packagingLocation)) {
                DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder();

                documentBuilder.receipt(packagingLocation);
                documentBuilder.setField(DocumentFieldsDTMF.DELIVERY, delivery);
                documentBuilder.setField(DocumentFields.COMPANY, delivery.getField(DeliveryFields.SUPPLIER));
                documentBuilder.setField(DocumentFields.DESCRIPTION, translationService.translate("deliveriesToMaterialFlow.delivery.packagesReceipt", LocaleContextHolder.getLocale()));

                for (Entity deliveredPackage : deliveredPackages) {
                    Entity product = deliveredPackage.getBelongsToField(DeliveredPackageFields.PRODUCT);
                    BigDecimal deliveredQuantity = deliveredPackage.getDecimalField(DeliveredPackageFields.DELIVERED_QUANTITY);
                    String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

                    String unit = product.getStringField(ProductFields.UNIT);

                    if (StringUtils.isEmpty(additionalUnit)) {
                        additionalUnit = unit;
                    }

                    BigDecimal conversion = BigDecimal.ONE;

                    if (!unit.equals(additionalUnit)) {
                        conversion = unitConversionService
                                .getPossibleConversions(unit,
                                        searchCriteriaBuilder -> searchCriteriaBuilder
                                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)))
                                .asUnitToConversionMap().get(additionalUnit);
                    }

                    BigDecimal givenQuantity = deliveredQuantity.multiply(conversion, numberService.getMathContext());

                    documentBuilder.addPosition(product, numberService.setScaleWithDefaultMathContext(deliveredQuantity),
                            numberService.setScaleWithDefaultMathContext(givenQuantity), additionalUnit, conversion,
                            null, null, null, null, null);
                }

                Entity createdDocument = documentBuilder.setAccepted().build();

                if (!createdDocument.isValid()) {
                    delivery.addGlobalError("deliveriesToMaterialFlow.deliveryStateValidator.error.document", true);

                    for (ErrorMessage error : createdDocument.getGlobalErrors()) {
                        delivery.addGlobalError(error.getMessage(), error.getAutoClose());
                    }
                }
            }
        }
    }

    private Entity getCurrency(final Entity delivery) {
        Entity currency = delivery.getBelongsToField(DeliveryFields.CURRENCY);

        return Objects.nonNull(currency) ? currency : currencyFromParameter();
    }

    private Entity getLocation(final Entity delivery) {
        return delivery.getBelongsToField(DeliveryFields.LOCATION);
    }

    private Entity getPackagingLocation() {
        Entity parameter = parameterService.getParameter();

        return parameter.getBelongsToField(ParameterFieldsD.PACKAGING_LOCATION);
    }

    private Entity getProduct(final Entity deliveredProduct) {
        return deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
    }

    private Entity currencyFromParameter() {
        return parameterService.getParameter().getBelongsToField(ParameterFields.CURRENCY);
    }

    private Entity getBatch(final Entity deliveredProduct) {
        return deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);
    }

    private Date getExpirationDate(final Entity deliveredProduct) {
        return deliveredProduct.getDateField(DeliveredProductFields.EXPIRATION_DATE);
    }

    private String getTypeOfPallet(final Entity deliveredProduct) {
        return deliveredProduct.getStringField(DeliveredProductFields.PALLET_TYPE);
    }

    private Entity getPalletNumber(final Entity deliveredProduct) {
        return deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);
    }

    private Entity getStorageLocation(final Entity deliveredProduct) {
        return deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);
    }

    private boolean isWaste(final Entity deliveredProduct) {
        return deliveredProduct.getBooleanField(DeliveredProductFields.IS_WASTE);
    }

    private Date getProductionDate(final Entity deliveredProduct) {
        return deliveredProduct.getDateField(DeliveredProductFields.PRODUCTION_DATE);
    }

    private boolean isRequired(final Entity location, final String fieldName) {
        return location.getBooleanField(fieldName);
    }

    private String getProductName(final Entity deliveredProduct) {
        return getProduct(deliveredProduct).getStringField(ProductFields.NAME);
    }

    private String getLocationName(final Entity location) {
        return location.getStringField(LocationFields.NAME);
    }

    private DataDefinition getDeliveredProductAttributeValDD() {
        return dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT_ATTRIBUTE_VAL);
    }

}
