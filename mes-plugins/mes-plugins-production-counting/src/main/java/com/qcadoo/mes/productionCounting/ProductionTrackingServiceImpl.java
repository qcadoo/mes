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
package com.qcadoo.mes.productionCounting;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.productionCounting.constants.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.ProductUnitsConversionService;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.newstates.ProductionTrackingStateServiceMarker;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.states.listener.ProductionTrackingListenerService;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionTrackingServiceImpl implements ProductionTrackingService {

    private static final String L_TIME_TAB = "timeTab";

    private static final String L_WORK_TIME_RIBBON_GROUP = "workTime";

    private static final String L_CALC_LABOR_TOTAL_TIME_RIBBON_BUTTON = "calcTotalLaborTime";

    private static final String USER_CHANGE_STATE = "user";

    private static final String L_ID = ".id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductUnitsConversionService productUnitsConversionService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionTrackingListenerService productionTrackingListenerService;

    @Autowired
    private ParameterService parameterService;

    @Override
    public void setTimeAndPieceworkComponentsVisible(final ViewDefinitionState view, final Entity order) {
        String recordingType = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        boolean recordingTypeEqualsForEach = TypeOfProductionRecording.FOR_EACH.getStringValue().equals(recordingType);
        boolean recordingTypeEqualsBasic = TypeOfProductionRecording.BASIC.getStringValue().equals(recordingType);

        LookupComponent tocComponent = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        tocComponent.setVisible(recordingTypeEqualsForEach);
        tocComponent.setRequired(recordingTypeEqualsForEach);

        boolean registerProductionTime = order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME);
        view.getComponentByReference(L_TIME_TAB).setVisible(registerProductionTime && !recordingTypeEqualsBasic);

        ProductionTrackingState recordState = getTrackingState(view);
        final FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form.getEntityId() != null) {
            WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

            RibbonActionItem calcTotalLaborTimeBtn = window.getRibbon().getGroupByName(L_WORK_TIME_RIBBON_GROUP)
                    .getItemByName(L_CALC_LABOR_TOTAL_TIME_RIBBON_BUTTON);
            calcTotalLaborTimeBtn.setEnabled(registerProductionTime && !recordingTypeEqualsBasic
                    && ProductionTrackingState.DRAFT.equals(recordState));
            calcTotalLaborTimeBtn.requestUpdate(true);
        }
    }

    @Override
    public ProductionTrackingState getTrackingState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity productionRecordFormEntity = form.getEntity();
        String stateStringValue = productionRecordFormEntity.getStringField(ProductionTrackingFields.STATE);
        if (StringUtils.isEmpty(stateStringValue)) {
            return ProductionTrackingState.DRAFT;
        }
        return ProductionTrackingState.parseString(stateStringValue);
    }

    @Override
    public void fillProductionLineLookup(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference("order");
        Entity order = orderLookup.getEntity();
        Long productionLineId = null;
        if (order != null) {
            productionLineId = order.getBelongsToField(OrderFields.PRODUCTION_LINE).getId();
        }
        LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference("productionLine");
        productionLineLookup.setFieldValue(productionLineId);
    }

    @Override
    public void changeState(Entity productionTracking, ProductionTrackingState state) {
        Long userId = securityService.getCurrentUserId();
        productionTracking.setField(USER_CHANGE_STATE, userId);
        String userLogin = securityService.getCurrentUserName();
        stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, productionTracking, userLogin,
                state.getStringValue());
    }

    @Override
    public Entity correct(Entity productionTracking) {
        DataDefinition productionTrackingDD = productionTracking.getDataDefinition();
        boolean last = productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING);
        productionTracking.setField(ProductionTrackingFields.IS_CORRECTED, true);
        clearLastProductionTracking(productionTracking);
        productionTracking.setField(ProductionTrackingFields.UNDERGOING_CORRECTION, true);

        Entity clearedProductionTracking = productionTracking.getDataDefinition().save(productionTracking);
        Entity correctingProductionTracking = productionTrackingDD.copy(clearedProductionTracking.getId()).get(0);

        copyOtherFields(clearedProductionTracking, correctingProductionTracking);
        clearedProductionTracking.setField(ProductionTrackingFields.CORRECTION, correctingProductionTracking);
        correctingProductionTracking.setField(ProductionTrackingFields.IS_CORRECTION, true);
        correctingProductionTracking.setField(ProductionTrackingFields.IS_CORRECTED, false);
        correctingProductionTracking.setField(ProductionTrackingFields.LAST_TRACKING, last);
        correctingProductionTracking.setField(ProductionTrackingFields.UNDERGOING_CORRECTION, true);

        productionTrackingDD.save(correctingProductionTracking);
        clearedProductionTracking.setField(ProductionTrackingFields.UNDERGOING_CORRECTION, false);

        changeState(clearedProductionTracking, ProductionTrackingState.CORRECTED);
        return correctingProductionTracking;
    }

    private void clearLastProductionTracking(Entity productionTracking) {
        if (productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING)) {
            productionTracking.setField(ProductionTrackingFields.LAST_TRACKING, false);
        }
    }

    private void copyOtherFields(Entity productionTracking, Entity correctingProductionTracking) {
        copyStaffWorkTimes(productionTracking, correctingProductionTracking);
        copyTrackingOperationProductInComponents(productionTracking, correctingProductionTracking);
        copyTrackingOperationProductOutComponents(productionTracking, correctingProductionTracking);
    }

    private void copyTrackingOperationProductOutComponents(Entity productionTracking, Entity correctingProductionTracking) {
        EntityList trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
        List<Entity> copiedTrackingOperationProductOutComponents = Lists.newArrayList();
        trackingOperationProductOutComponents.forEach(t -> {

            Entity operationProductOutComponent = t.getDataDefinition()
                    .copy(t.getId()).get(0);

            List<Entity> copiedLacks = Lists.newArrayList();
            t.getHasManyField(TrackingOperationProductOutComponentFields.LACKS).forEach(l -> {
                copiedLacks.add(l.getDataDefinition().copy(l.getId()).get(0));
            });
            operationProductOutComponent.setField(TrackingOperationProductOutComponentFields.LACKS, copiedLacks);
            copiedTrackingOperationProductOutComponents.add(operationProductOutComponent);
        });
        correctingProductionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS,
                copiedTrackingOperationProductOutComponents);
    }

    private void copyStaffWorkTimes(Entity productionTracking, Entity correctingProductionTracking) {
        EntityList staffWorkTimes = productionTracking.getHasManyField(ProductionTrackingFields.STAFF_WORK_TIMES);
        List<Entity> copiedStaffWorkTimes = Lists.newArrayList();
        for (Entity staffWorkTime : staffWorkTimes) {
            Entity newStaffWorkTime = staffWorkTime.getDataDefinition().create();
            newStaffWorkTime.setField(StaffWorkTimeFields.PRODUCTION_RECORD, correctingProductionTracking);
            newStaffWorkTime.setField(StaffWorkTimeFields.WORKER, staffWorkTime.getField(StaffWorkTimeFields.WORKER));
            newStaffWorkTime.setField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END,
                    staffWorkTime.getField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END));
            newStaffWorkTime.setField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START,
                    staffWorkTime.getField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START));
            newStaffWorkTime.setField(StaffWorkTimeFields.LABOR_TIME,
                    staffWorkTime.getIntegerField(StaffWorkTimeFields.LABOR_TIME));
            copiedStaffWorkTimes.add(newStaffWorkTime);
        }
        correctingProductionTracking.setField(ProductionTrackingFields.STAFF_WORK_TIMES, copiedStaffWorkTimes);

        correctingProductionTracking.setField(ProductionTrackingFields.MACHINE_TIME,
                productionTracking.getField(ProductionTrackingFields.MACHINE_TIME));
        correctingProductionTracking.setField(ProductionTrackingFields.LABOR_TIME,
                productionTracking.getField(ProductionTrackingFields.LABOR_TIME));
    }

    private void copyTrackingOperationProductInComponents(Entity productionTracking, Entity correctingProductionTracking) {
        EntityList trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        List<Entity> copiedTrackingOperationProductInComponents = Lists.newArrayList();
        trackingOperationProductInComponents
                .forEach(t -> {
                    Entity operationProductInComponent = t.getDataDefinition().copy(t.getId()).get(0);
                    List<Entity> batches = t.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);
                    List<Entity> copiedBatches = Lists.newArrayList();
                    batches.forEach(batch -> {
                        Entity copiedBatch = batch.getDataDefinition().create();
                        copiedBatch.setField(UsedBatchFields.QUANTITY, batch.getDecimalField(UsedBatchFields.QUANTITY));
                        copiedBatch.setField(UsedBatchFields.BATCH, batch.getBelongsToField(UsedBatchFields.BATCH).getId());
                        copiedBatch.setField(UsedBatchFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENT,
                                operationProductInComponent.getId());
                        copiedBatch = copiedBatch.getDataDefinition().save(copiedBatch);
                        copiedBatches.add(copiedBatch);
                    });
                    operationProductInComponent.setField(TrackingOperationProductInComponentFields.USED_BATCHES, copiedBatches);
                    copiedTrackingOperationProductInComponents.add(operationProductInComponent);
                });
        correctingProductionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS,
                copiedTrackingOperationProductInComponents);
    }

    @Override
    public void unCorrect(Entity correctingProductionTracking, boolean updateOrderReportedQuantity) {
        Entity correctedProductionTracking = correctingProductionTracking.getDataDefinition().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.CORRECTION, correctingProductionTracking))
                .uniqueResult();
        if (correctedProductionTracking != null) {
            correctedProductionTracking.setField(ProductionTrackingFields.CORRECTION, null);
            correctedProductionTracking.setField(ProductionTrackingFields.ON_UNCORRECTION_PROCESS, true);
            if(parameterService.getParameter().getBooleanField(ParameterFieldsPC.JUST_ONE)) {
                correctedProductionTracking.setField(ProductionTrackingFields.LAST_TRACKING, true);
            }
            changeState(correctedProductionTracking, ProductionTrackingState.ACCEPTED);
            correctingProductionTracking.setField(ProductionTrackingFields.IS_CORRECTION, false);
            correctingProductionTracking = correctingProductionTracking.getDataDefinition().save(correctingProductionTracking);
            if(updateOrderReportedQuantity) {
                productionTrackingListenerService.updateOrderReportedQuantityAfterRemoveCorrection(correctedProductionTracking);
            }
        }
    }

    @Override
    public Optional<BigDecimal> calculateGivenQuantity(final Entity trackingOperationProductInComponent,
                                                       final BigDecimal usedQuantity) {

        Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

        String givenUnit = trackingOperationProductInComponent
                .getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);

        if (givenUnit == null) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            if (StringUtils.isNotEmpty(additionalUnit)) {
                givenUnit = additionalUnit;
            } else {
                givenUnit = product.getStringField(ProductFields.UNIT);
            }
            trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT, givenUnit);
        }
        return productUnitsConversionService.forProduct(product).fromPrimaryUnit().to(givenUnit).convertValue(usedQuantity);

    }

    @Override
    public Either<Boolean, Optional<Date>> findExpirationDate(final Entity productionTracking, final Entity order,
                                                              final Entity toc, final Entity batch) {
        if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {

            List<Entity> productionTracingsForOrder = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING)
                    .find().add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                    .add(SearchRestrictions.belongsTo(ProductionTrackingFields.BATCH, batch)).list().getEntities();

            if (Objects.nonNull(productionTracking) && Objects.nonNull(productionTracking.getId())) {
                productionTracingsForOrder = productionTracingsForOrder.stream()
                        .filter(pt -> !pt.getId().equals(productionTracking.getId())).collect(Collectors.toList());
            }

            boolean nullDate = productionTracingsForOrder.stream().anyMatch(
                    pt -> Objects.isNull(pt.getDateField(ProductionTrackingFields.EXPIRATION_DATE)));
            if (nullDate) {
                return Either.left(true);
            }
            Optional<Date> maybeDate = productionTracingsForOrder.stream()
                    .filter(pt -> Objects.nonNull(pt.getDateField(ProductionTrackingFields.EXPIRATION_DATE)))
                    .map(pt -> pt.getDateField(ProductionTrackingFields.EXPIRATION_DATE)).findFirst();
            return Either.right(maybeDate);
        } else {
            Entity bpcq = basicProductionCountingService
                    .getProductionCountingQuantityDD()
                    .find()
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ORDER + L_ID, order.getId()))
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                            ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT + L_ID,
                            toc.getId())).setMaxResults(1).uniqueResult();

            if (Objects.nonNull(bpcq)
                    && bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId()
                    .equals(order.getBelongsToField(OrderFields.PRODUCT).getId())) {
                List<Entity> productionTracingsForOrder = dataDefinitionService
                        .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING)
                        .find().add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                        .add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                        .add(SearchRestrictions.belongsTo(ProductionTrackingFields.BATCH, batch)).list().getEntities();

                if (Objects.nonNull(productionTracking) && Objects.nonNull(productionTracking.getId())) {
                    productionTracingsForOrder = productionTracingsForOrder.stream()
                            .filter(pt -> !pt.getId().equals(productionTracking.getId())).collect(Collectors.toList());
                }

                boolean nullDate = productionTracingsForOrder.stream().anyMatch(
                        pt -> Objects.isNull(pt.getDateField(ProductionTrackingFields.EXPIRATION_DATE)));
                if (nullDate) {
                    return Either.left(true);
                }
                Optional<Date> maybeDate = productionTracingsForOrder.stream()
                        .filter(pt -> Objects.nonNull(pt.getDateField(ProductionTrackingFields.EXPIRATION_DATE)))
                        .map(pt -> pt.getDateField(ProductionTrackingFields.EXPIRATION_DATE)).findFirst();
                return Either.right(maybeDate);
            }
        }
        return null;
    }

    @Override
    public BigDecimal getTrackedQuantity(Entity trackingOperationProductOutComponent, List<Entity> trackings, boolean useTracking) {
        BigDecimal trackedQuantity = BigDecimal.ZERO;

        for (Entity trackingProduct : trackings) {
            if (!trackingProduct.getId().equals(trackingOperationProductOutComponent.getId())) {
                trackedQuantity = trackedQuantity.add(BigDecimalUtils.convertNullToZero(trackingProduct
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)), numberService
                        .getMathContext());
            }

        }
        if (useTracking) {
            trackedQuantity = trackedQuantity.add(BigDecimalUtils.convertNullToZero(trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)), numberService.getMathContext());
        }
        return trackedQuantity;
    }

    @Override
    public List<Entity> findTrackingOperationProductOutComponents(Entity order, Entity toc, Entity product) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT)
                .find()
                .createAlias(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING, "pTracking", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("pTracking." + ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.in("pTracking." + ProductionTrackingFields.STATE, Lists.newArrayList(
                        ProductionTrackingStateStringValues.ACCEPTED, ProductionTrackingStateStringValues.DRAFT)))
                .add(SearchRestrictions.or(SearchRestrictions.eq("pTracking." + ProductionTrackingFields.IS_CORRECTED, false),
                        SearchRestrictions.isNull("pTracking." + ProductionTrackingFields.IS_CORRECTED)))
                .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, product));

        if (Objects.nonNull(toc)) {
            scb.add(SearchRestrictions.belongsTo("pTracking." + ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
        }

        return scb.list().getEntities();
    }
}
