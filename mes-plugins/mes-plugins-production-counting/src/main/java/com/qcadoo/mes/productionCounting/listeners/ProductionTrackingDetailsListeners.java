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
package com.qcadoo.mes.productionCounting.listeners;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.ParameterFieldsAG;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.newstates.ProductionTrackingStateServiceMarker;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.mes.productionCounting.utils.StaffTimeCalculator;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductionTrackingDetailsListeners {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionTrackingDetailsListeners.class);

    public static final String L_BATCH = "batch";

    public static final String L_ORDER = "order";

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private StaffTimeCalculator staffTimeCalculator;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductionTrackingDocumentsHelper productionTrackingDocumentsHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationalTasksService operationalTasksService;

    public void copyProductionTracking(final ViewDefinitionState view, final ComponentState state,
                                       final String[] args) {
        if (parameterService.getParameter().getBooleanField(ParameterFieldsPC.JUST_ONE)) {
            view.addMessage("productionCounting.productionTracking.messages.error.canExistOnlyOneProductionTrackingRecord", ComponentState.MessageType.FAILURE, false);
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        form.performEvent(view, "copy");
    }

    public void copyProductionTrackings(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        if (parameterService.getParameter().getBooleanField(ParameterFieldsPC.JUST_ONE)) {
            view.addMessage("productionCounting.productionTracking.messages.error.canExistOnlyOneProductionTrackingRecord", ComponentState.MessageType.FAILURE, false);
            return;
        }
        state.performEvent(view, "copy");
    }

    public void useReplacement(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent trackingOperationProductInComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        if (!trackingOperationProductInComponentsGrid.getSelectedEntitiesIds().isEmpty()
                && trackingOperationProductInComponentsGrid.getSelectedEntitiesIds().size() == 1) {
            FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

            Long productionTrackingId = productionTrackingForm.getEntityId();

            Entity trackingOperationProductInComponent = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT)
                    .get(trackingOperationProductInComponentsGrid.getSelectedEntitiesIds().stream().findFirst().get());

            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.productionTracking", productionTrackingId);
            parameters.put("form.basicProduct", trackingOperationProductInComponent
                    .getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId());

            String url = "/productionCounting/useReplacement.html";
            view.openModal(url, parameters);
        }
    }

    public void addToAnomaliesList(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent trackingOperationProductInComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        if (!trackingOperationProductInComponentsGrid.getSelectedEntitiesIds().isEmpty()) {
            FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

            Long productionTrackingId = productionTrackingForm.getEntityId();

            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("form.productionTrackingId", productionTrackingId);
            parameters.put("form.selectedTOPICs", trackingOperationProductInComponentsGrid.getSelectedEntitiesIds().stream()
                    .map(String::valueOf).collect(Collectors.joining(",")));
            parameters.put("form.performAndAccept", Boolean.FALSE);

            String url = "/productionCounting/anomalyProductionTrackingDetails.html";
            view.openModal(url, parameters);
        }
    }

    public void goToProductionCountingQuantities(final ViewDefinitionState view, final ComponentState state,
                                                 final String[] args) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long productionTrackingId = productionTrackingForm.getEntityId();
        Entity order = productionTrackingForm.getEntity().getBelongsToField(ProductionTrackingFields.ORDER);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("window.mainTab.order.id", order.getId());
        parameters.put("form.productionTrackingId", productionTrackingId);

        String url = "/page/basicProductionCounting/detailedProductionCountingAndProgressList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void changeTrackingState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Optional<FormComponent> maybeForm = view.tryFindComponentByReference(QcadooViewConstants.L_FORM);

        if (maybeForm.isPresent() && parameterService.getParameter()
                .getBooleanField(ParameterFieldsPC.ALLOW_ANOMALY_CREATION_ON_ACCEPTANCE_RECORD)) {
            FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

            Entity productionTracking = productionTrackingForm.getEntity();

            productionTracking = productionTracking.getDataDefinition().get(productionTracking.getId());

            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
            Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

            List<Entity> trackingOperationProductInComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
            List<Entity> trackingOperationProductOutComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

            Multimap<Long, Entity> groupedRecordInProducts = productionTrackingDocumentsHelper
                    .fillFromBPCProductIn(trackingOperationProductInComponents, order, toc, true, true);

            List<Long> productIds = productionTrackingDocumentsHelper
                    .findProductsWithInsufficientQuantity(groupedRecordInProducts, trackingOperationProductOutComponents);

            if (productIds.isEmpty()) {
                stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, view, args);
            } else {
                Long productionTrackingId = productionTrackingForm.getEntityId();

                Map<String, Object> parameters = Maps.newHashMap();

                parameters.put("form.productionTrackingId", productionTrackingId);
                parameters.put("form.selectedTOPICs", trackingOperationProductInComponents.stream()
                        .filter(ip -> productIds
                                .contains(ip.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId()))
                        .map(Entity::getId).map(String::valueOf).collect(Collectors.joining(",")));
                parameters.put("form.performAndAccept", Boolean.TRUE);

                String url = "/productionCounting/anomalyProductionTrackingDetails.html";
                view.openModal(url, parameters);
            }
        } else {
            stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, view, args);
        }
    }

    public void calcTotalLaborTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long productionTrackingId = productionTrackingForm.getEntityId();

        if (Objects.isNull(productionTrackingId)) {
            return;
        }

        Long totalLabor = staffTimeCalculator.countTotalLaborTime(productionTrackingId);

        FieldComponent laborTimeField = (FieldComponent) view.getComponentByReference(ProductionTrackingFields.LABOR_TIME);

        laborTimeField.setFieldValue(totalLabor);
    }

    public void copyPlannedQuantityToUsedQuantity(final ViewDefinitionState view, final ComponentState state,
                                                  final String[] args) {
        FormComponent productionRecordForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long productionRecordId = productionRecordForm.getEntityId();

        if (Objects.isNull(productionRecordId)) {
            return;
        }

        Entity productionRecord = productionRecordForm.getEntity().getDataDefinition().get(productionRecordId);

        EntityList trackingOperationProductInComponents = productionRecord
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        clearWasteUsedDetails(trackingOperationProductInComponents);

        copyPlannedQuantityToUsedQuantity(trackingOperationProductInComponents);
        copyPlannedQuantityToUsedQuantity(
                productionRecord.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS));
    }

    private void copyPlannedQuantityToUsedQuantity(final List<Entity> trackingOperationProductComponents) {
        for (Entity trackingOperationProductComponent : trackingOperationProductComponents) {
            Entity product = trackingOperationProductComponent
                    .getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

            Entity recordOperationProductComponentDto;

            if (ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT
                    .equals(trackingOperationProductComponent.getDataDefinition().getName())) {
                recordOperationProductComponentDto = dataDefinitionService
                        .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT_DTO)
                        .get(trackingOperationProductComponent.getId());
            } else {
                recordOperationProductComponentDto = dataDefinitionService
                        .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT_DTO)
                        .get(trackingOperationProductComponent.getId());
            }

            BigDecimal plannedQuantity = BigDecimalUtils.convertNullToZero(recordOperationProductComponentDto
                    .getDecimalField(TrackingOperationProductInComponentDtoFields.PLANNED_QUANTITY));
            trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(plannedQuantity));

            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            String baseUnit = product.getStringField(ProductFields.UNIT);

            if (StringUtils.isEmpty(additionalUnit)) {
                trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                        numberService.setScaleWithDefaultMathContext(plannedQuantity));
                trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT, baseUnit);
            } else {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(baseUnit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));
                if (unitConversions.isDefinedFor(additionalUnit)) {
                    BigDecimal convertedQuantity = unitConversions.convertTo(plannedQuantity, additionalUnit);
                    trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                            convertedQuantity);
                } else {
                    trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                            numberService.setScaleWithDefaultMathContext(plannedQuantity));
                }

                trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT, additionalUnit);
            }

            trackingOperationProductComponent.getDataDefinition().save(trackingOperationProductComponent);
        }
    }

    private void clearWasteUsedDetails(final EntityList trackingOperationProductInComponents) {
        for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
            trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.WASTE_USED, Boolean.FALSE);
            trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.WASTE_USED_ONLY,
                    Boolean.FALSE);
            trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.WASTE_USED_QUANTITY, null);
            trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.WASTE_UNIT, null);
        }
    }

    public void enableOrDisableFields(final ViewDefinitionState view, final ComponentState componentState,
                                      final String[] args) {
        Entity order = getOrderFromLookup(view);

        if (Objects.isNull(order)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("order is null");
            }

            return;
        }

        productionTrackingService.setTimeAndPieceworkComponentsVisible(view, order);
    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.ORDER);

        return orderLookup.getEntity();
    }

    public void onOrderChange(final ViewDefinitionState view, final ComponentState componentState,
                              final String[] args) {
        clearFields(view, componentState, args);
        enableOrDisableFields(view, componentState, args);
        checkJustOne(view, componentState, args);
        fillDivisionFieldFromOrder(view);
        fillOrderedProductBatch(view);
    }

    private void fillOrderedProductBatch(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.ORDER);
        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(L_BATCH);

        Entity order = orderLookup.getEntity();

        if (Objects.nonNull(order) && PluginUtils.isEnabled("advancedGenealogyForOrders")) {
            List<Entity> records = dataDefinitionService
                    .get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_TRACKING_RECORD).find()
                    .add(SearchRestrictions.belongsTo(L_ORDER, order)).list().getEntities();

            if (records.size() == 1) {
                Entity record = records.get(0);

                batchLookup.setFieldValue(record.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH).getId());
            } else {
                batchLookup.setFieldValue(null);
            }
        } else {
            batchLookup.setFieldValue(null);
        }

        batchLookup.requestComponentUpdateState();
    }

    private void fillDivisionFieldFromOrder(final ViewDefinitionState view) {
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.ORDER);

        Entity order = orderLookup.getEntity();

        if (Objects.nonNull(order) && Objects.nonNull(order.getBelongsToField(OrderFields.DIVISION))) {
            divisionLookup.setFieldValue(order.getBelongsToField(OrderFields.DIVISION).getId());
            divisionLookup.requestComponentUpdateState();
        }
    }

    public void checkJustOne(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity productionTracking = productionTrackingForm.getEntity();
        String state = productionTracking.getStringField(ProductionTrackingFields.STATE);

        boolean isDraft = (ProductionTrackingStateStringValues.DRAFT.equals(state));
        FieldComponent lastTrackingField = (FieldComponent) view.getComponentByReference(ProductionTrackingFields.LAST_TRACKING);

        if (isDraft) {
            Entity parameter = parameterService.getParameter();
            boolean justOne = parameter.getBooleanField(ParameterFieldsPC.JUST_ONE);

            if (justOne) {
                lastTrackingField.setFieldValue(true);
                lastTrackingField.setEnabled(false);
            } else {
                lastTrackingField.setEnabled(true);
            }
        } else {
            lastTrackingField.setEnabled(false);
        }

        lastTrackingField.requestComponentUpdateState();
    }

    public void clearFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FieldComponent technologyOperationComponentField = (FieldComponent) view
                .getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        technologyOperationComponentField.setFieldValue("");

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.isNull(form.getEntityId())) {
            return;
        }

        GridComponent trackingOperationProductInComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        GridComponent trackingOperationProductOutComponentsGrid = (GridComponent) view
                .getComponentByReference(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        trackingOperationProductInComponentsGrid.setEntities(Lists.newArrayList());
        trackingOperationProductOutComponentsGrid.setEntities(Lists.newArrayList());
    }

    public void onTechnologyOperationComponentChange(final ViewDefinitionState view,
                                                     final ComponentState componentState,
                                                     final String[] args) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();
        Entity order = getOrderFromLookup(view);

        Entity operationalTask = operationalTasksService.findOperationalTasks(order, technologyOperationComponent);

        LookupComponent staffLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.STAFF);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.WORKSTATION);

        if (Objects.nonNull(operationalTask)) {
            if (Objects.nonNull(operationalTask.getBelongsToField(OperationalTaskFields.STAFF))) {
                staffLookup.setFieldValue(operationalTask.getBelongsToField(OperationalTaskFields.STAFF).getId());
            } else {
                staffLookup.setFieldValue(null);
            }
            if (Objects.nonNull(operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION))) {
                workstationLookup.setFieldValue(operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION).getId());
            } else {
                workstationLookup.setFieldValue(null);
            }
        } else {
            staffLookup.setFieldValue(null);
            workstationLookup.setFieldValue(null);
        }
    }

    public void onStaffChange(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        LookupComponent staffLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.STAFF);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);

        Entity staff = staffLookup.getEntity();

        if (Objects.isNull(staff)) {
            return;
        }

        Entity division = staff.getBelongsToField(StaffFields.DIVISION);

        if (Objects.isNull(division)) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    public void fillDivisionField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.WORKSTATION);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);

        Entity workstation = workstationLookup.getEntity();

        if (Objects.isNull(workstation)) {
            divisionLookup.setFieldValue(null);

            return;
        }

        Entity division = workstation.getBelongsToField(WorkstationFields.DIVISION);

        if (Objects.isNull(division)) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    public void onAddBatchChange(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        if (!parameterService.getParameter().getBooleanField(ParameterFieldsAG.GENERATE_BATCH_FOR_ORDERED_PRODUCT)) {
            FieldComponent batchNumber = (FieldComponent) view.getComponentByReference(ProductionTrackingFields.BATCH_NUMBER);
            batchNumber.setEnabled(true);
        }
    }

    public void correct(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity productionTracking = productionTrackingForm.getPersistedEntityWithIncludedFormValues();

        if (Objects.nonNull(productionTracking)) {
            Long id = productionTrackingService.correct(productionTracking).getId();

            String url = "../page/productionCounting/productionTrackingDetails.html";
            view.redirectTo(url, false, true, ImmutableMap.of("form.id", id));
        }
    }

}
