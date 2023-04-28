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
package com.qcadoo.mes.productionCounting.hooks;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.AdvancedGenealogyService;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.ParameterFieldsAG;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordType;
import com.qcadoo.mes.basic.LogService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ExpiryDateValidityUnit;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.services.NumberPatternGeneratorService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.hooks.helpers.OperationProductsExtractor;
import com.qcadoo.mes.productionCounting.states.ProductionTrackingStatesHelper;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.states.listener.ProductionTrackingListenerService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.security.constants.UserFields;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionTrackingHooks {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionTrackingHooks.class);

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ProductionTrackingStatesHelper productionTrackingStatesHelper;

    @Autowired
    private OperationProductsExtractor operationProductsExtractor;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogService logService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdvancedGenealogyService advancedGenealogyService;

    @Autowired
    private ProductionTrackingListenerService productionTrackingListenerService;

    public void onCreate(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        setInitialState(productionTracking);
        fillExpirationDate(productionTracking, order);
    }

    public void onCopy(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        setInitialState(productionTracking);
        productionTracking.setField(ProductionTrackingFields.IS_CORRECTION, false);
    }

    public void onSave(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        willOrderAcceptOneMore(productionTrackingDD, productionTracking, order);
        generateNumberIfNeeded(productionTracking);
        setTimesToZeroIfEmpty(productionTracking);
        copyProducts(productionTracking);

        if (productionTracking.getBooleanField(ProductionTrackingFields.ADD_BATCH)
                && (StringUtils.isNoneEmpty(productionTracking.getStringField(ProductionTrackingFields.BATCH_NUMBER)) || parameterService.getParameter().getBooleanField(
                ParameterFieldsAG.GENERATE_BATCH_FOR_ORDERED_PRODUCT))) {
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);
            String number = productionTracking.getStringField(ProductionTrackingFields.BATCH_NUMBER);

            Entity batch = advancedGenealogyService.createOrGetBatch(number, product);

            if (!batch.isValid()) {
                productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.BATCH_NUMBER),
                        "productionCounting.productionTracking.messages.error.batchCreation");
            } else {
                productionTracking.setField(ProductionTrackingFields.BATCH, batch.getId());

                List<Entity> trackingRecords = order.getHasManyField("trackingRecords");
                List<Entity> filteredTrackingRecords = trackingRecords.stream()
                        .filter(tr -> tr.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH).getId().equals(batch.getId()))
                        .collect(Collectors.toList());

                if (filteredTrackingRecords.isEmpty()) {
                    Entity trackingRecord = advancedGenealogyService.getTrackingRecordDD().create();

                    trackingRecord.setField(TrackingRecordFields.NUMBER, numberGeneratorService.generateNumber(
                            AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_TRACKING_RECORD));
                    trackingRecord.setField("order", order.getId());
                    trackingRecord.setField(TrackingRecordFields.ENTITY_TYPE, TrackingRecordType.FOR_ORDER);
                    trackingRecord.setField(TrackingRecordFields.PRODUCED_BATCH, batch.getId());
                    trackingRecord = trackingRecord.getDataDefinition().save(trackingRecord);

                    if (!trackingRecord.isValid()) {
                        Optional<ErrorMessage> msg = trackingRecord.getGlobalErrors().stream()
                                .filter(em -> em.getMessage().equals("advancedGenealogyForOrders.trackingRecord.validation.error.onlyOneBatchTrackingForOrder"))
                                .findFirst();
                        if (msg.isPresent()) {
                            productionTracking.addGlobalError("advancedGenealogyForOrders.trackingRecord.validation.error.onlyOneBatchTrackingForOrder");
                        } else {
                            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.BATCH_NUMBER),
                                    "productionCounting.productionTracking.messages.error.batchCreation");
                        }

                    }
                }
            }
        }

        if (Objects.isNull(productionTracking.getId())) {
            if(parameterService.getParameter().getBooleanField("justOne")) {
                productionTracking.setField(ProductionTrackingFields.LAST_TRACKING, Boolean.TRUE);
            }

            Entity user = userService.find(productionTracking.getStringField("createUser"));
            String worker = StringUtils.EMPTY;

            if (Objects.nonNull(user)) {
                worker = user.getStringField(UserFields.FIRST_NAME) + " " + user.getStringField(UserFields.LAST_NAME);
            }

            String number = productionTracking.getStringField(ProductionTrackingFields.NUMBER);
            String orderNumber = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER).getStringField(
                    OrderFields.NUMBER);
            Date createDate = productionTracking.getDateField("createDate");

            logService.add(LogService.Builder
                    .activity(
                            "productionTracking",
                            translationService.translate("productionCounting.productionTracking.activity.created.action",
                                    LocaleContextHolder.getLocale()))
                    .withMessage(
                            translationService.translate("productionCounting.productionTracking.activity.created.message",
                                    LocaleContextHolder.getLocale(), worker, number,
                                    generateOrderDetailsUrl(orderNumber, order.getId()))).withCreateTime(createDate));
        }
    }

    private void fillExpirationDate(final Entity productionTracking, final Entity order) {
        if (Objects.nonNull(order) && Objects.isNull(productionTracking.getDateField(ProductionTrackingFields.EXPIRATION_DATE))) {
            if (Objects.nonNull(order.getDateField(OrderFields.EXPIRATION_DATE))) {
                productionTracking.setField(ProductionTrackingFields.EXPIRATION_DATE, order.getDateField(OrderFields.EXPIRATION_DATE));
            } else {
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);
                Integer expiryDateValidity = product.getIntegerField(ProductFields.EXPIRY_DATE_VALIDITY);
                String expiryDateValidityUnit = product.getStringField(ProductFields.EXPIRY_DATE_VALIDITY_UNIT);

                if (Objects.nonNull(expiryDateValidity)) {
                    Date expirationDate;

                    if (ExpiryDateValidityUnit.DAYS.getStringValue().equals(expiryDateValidityUnit)) {
                        expirationDate = new DateTime(order.getDateField(OrderFields.START_DATE)).plusDays(expiryDateValidity).toDate();
                    } else {
                        expirationDate = new DateTime(order.getDateField(OrderFields.START_DATE)).plusMonths(expiryDateValidity).toDate();
                    }

                    productionTracking.setField(ProductionTrackingFields.EXPIRATION_DATE, expirationDate);
                }
            }
        }
    }

    private String generateOrderDetailsUrl(final String number, final Long id) {
        return "<a href=\"" + OrdersConstants.orderDetailsUrl(id) + "\" target=\"_blank\">" + number + "</a>";
    }

    public void onDelete(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        Entity correctedProductionTracking = productionTracking.getDataDefinition().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.CORRECTION, productionTracking))
                .uniqueResult();
        if (correctedProductionTracking != null) {
            productionTrackingListenerService.updateOrderReportedQuantityOnRemove(productionTracking);
            productionTrackingService.unCorrect(productionTracking, true);

        } else {
            productionTrackingListenerService.updateOrderReportedQuantityOnRemove(productionTracking);
        }

        logPerformDelete(productionTracking);
    }

    private void logPerformDelete(final Entity productionTracking) {
        String username = securityService.getCurrentUserName();

        LOGGER.info(String.format("Delete production tracking. Number : %S id : %d. User : %S",
                productionTracking.getStringField(ProductionTrackingFields.NUMBER), productionTracking.getId(), username));

        logService.add(LogService.Builder
                .info("productionTracking",
                        translationService.translate("productionCounting.productionTracking.delete",
                                LocaleContextHolder.getLocale())).withItem1("ID: " + productionTracking.getId().toString())
                .withItem2("Number: " + productionTracking.getStringField(ProductionTrackingFields.NUMBER))
                .withItem3("User: " + username));
    }

    private boolean willOrderAcceptOneMore(final DataDefinition productionTrackingDD, final Entity productionTracking,
                                           final Entity order) {
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        final List<Entity> productionTrackings = productionTrackingDD
                .find()
                .add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.ACCEPTED))
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent)).list().getEntities();

        return willOrderAcceptOneMoreValidator(productionTrackingDD, productionTracking, productionTrackings);
    }

    private boolean willOrderAcceptOneMoreValidator(final DataDefinition productionTrackingDD, final Entity productionTracking,
                                                    final List<Entity> productionTrackings) {
        for (Entity tracking : productionTrackings) {
            if (Objects.nonNull(productionTracking.getId()) && productionTracking.getId().equals(tracking.getId())) {
                if (checkLastProductionTracking(productionTrackingDD, productionTracking)) {
                    return false;
                }
            } else {
                if (checkLastProductionTracking(productionTrackingDD, tracking)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkLastProductionTracking(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        if (productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING)
                && !productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTION)) {

            if (Objects.isNull(productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT))) {
                productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.ORDER),
                        "productionCounting.productionTracking.messages.error.final");
            } else {
                productionTracking.addError(
                        productionTrackingDD.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT),
                        "productionCounting.productionTracking.messages.error.operationFinal");
            }

            return true;
        }

        return false;
    }

    private void copyProducts(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        final boolean registerQuantityInProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        final boolean registerQuantityOutProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        if (!(registerQuantityInProduct || registerQuantityOutProduct)
                || StringUtils.isEmpty(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))
                || !shouldCopyProducts(productionTracking)) {
            return;
        }

        OperationProductsExtractor.TrackingOperationProducts operationProducts = operationProductsExtractor
                .getProductsByModelName(productionTracking);

        List<Entity> inputs = Collections.emptyList();

        if (registerQuantityInProduct) {
            inputs = operationProducts.getInputComponents();
        }

        List<Entity> outputs = Collections.emptyList();

        if (registerQuantityOutProduct) {
            outputs = operationProducts.getOutputComponents();
        }

        if (registerQuantityInProduct) {
            productionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS, inputs);
        }

        if (registerQuantityOutProduct) {
            productionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS, outputs);
        }
    }

    private boolean shouldCopyProducts(final Entity productionTracking) {
        if (Objects.isNull(productionTracking.getId())) {
            List<Entity> inputProduct = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
            List<Entity> outputProduct = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

            return inputProduct.isEmpty() && outputProduct.isEmpty();
        }

        Entity existingProductionTracking = productionTracking.getDataDefinition().get(productionTracking.getId());

        Object oldTocValue = existingProductionTracking.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        Object newTocValue = productionTracking.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Object oldOrderValue = existingProductionTracking.getField(ProductionTrackingFields.ORDER);
        Object newOrderValue = productionTracking.getField(ProductionTrackingFields.ORDER);

        return !ObjectUtils.equals(oldOrderValue, newOrderValue) || !ObjectUtils.equals(oldTocValue, newTocValue);
    }

    private void setTimesToZeroIfEmpty(final Entity productionTracking) {
        setTimeToZeroIfNull(productionTracking, ProductionTrackingFields.LABOR_TIME);
        setTimeToZeroIfNull(productionTracking, ProductionTrackingFields.MACHINE_TIME);
    }

    private void setTimeToZeroIfNull(final Entity productionTracking, final String timeFieldName) {
        Integer time = productionTracking.getIntegerField(timeFieldName);

        productionTracking.setField(timeFieldName, ObjectUtils.defaultIfNull(time, 0));
    }

    private void setInitialState(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.IS_EXTERNAL_SYNCHRONIZED, true);
        productionTracking.setField(ProductionTrackingFields.CORRECTION, null);

        if (Objects.isNull(productionTracking.getField(ProductionTrackingFields.LAST_TRACKING))) {
            productionTracking.setField(ProductionTrackingFields.LAST_TRACKING, false);
        }

        productionTrackingStatesHelper.setInitialState(productionTracking);
    }

    private void generateNumberIfNeeded(final Entity productionTracking) {
        if (Objects.isNull(productionTracking.getField(ProductionTrackingFields.NUMBER))) {
            productionTracking.setField(ProductionTrackingFields.NUMBER, setNumberFromSequence());
        }
    }

    private String setNumberFromSequence() {
        return jdbcTemplate.queryForObject("select generate_productiontracking_number()", Maps.newHashMap(), String.class);
    }

}
