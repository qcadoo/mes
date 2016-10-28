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
package com.qcadoo.mes.productionCounting;

import com.google.common.collect.Lists;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.StaffWorkTimeFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.newstates.ProductionTrackingStateServiceMarker;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductionTrackingServiceImpl implements ProductionTrackingService {

    private static final String L_DONE_QUANTITY = "doneQuantity";

    private static final String L_AMOUNT_OF_PRODUCT_PRODUCED = "amountOfProductProduced";

    private static final String L_TIME_TAB = "timeTab";

    private static final String L_PIECEWORK_TAB = "pieceworkTab";

    private static final String L_WORK_TIME_RIBBON_GROUP = "workTime";

    private static final String L_CALC_LABOR_TOTAL_TIME_RIBBON_BUTTON = "calcTotalLaborTime";

    private static final String L_WINDOW = "window";

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private StateExecutorService stateExecutorService;
    
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
        final FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() != null) {
            WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

            RibbonActionItem calcTotalLaborTimeBtn = window.getRibbon().getGroupByName(L_WORK_TIME_RIBBON_GROUP)
                    .getItemByName(L_CALC_LABOR_TOTAL_TIME_RIBBON_BUTTON);
            calcTotalLaborTimeBtn.setEnabled(registerProductionTime && !recordingTypeEqualsBasic
                    && ProductionTrackingState.DRAFT.equals(recordState));
            calcTotalLaborTimeBtn.requestUpdate(true);
        }
        boolean registerPiecework = order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK);
        view.getComponentByReference(L_PIECEWORK_TAB).setVisible(registerPiecework && recordingTypeEqualsForEach);
    }

    @Override
    public ProductionTrackingState getTrackingState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity productionRecordFormEntity = form.getEntity();
        String stateStringValue = productionRecordFormEntity.getStringField(ProductionTrackingFields.STATE);
        if (StringUtils.isEmpty(stateStringValue)) {
            return ProductionTrackingState.DRAFT;
        }
        return ProductionTrackingState.parseString(stateStringValue);
    }

    @Override
    public void changeProducedQuantityFieldState(final ViewDefinitionState viewDefinitionState) {
        final FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        Entity order = null;
        if (form.getEntityId() != null) {
            order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    form.getEntityId());
        }

        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        ComponentState doneQuantity = viewDefinitionState.getComponentByReference(L_DONE_QUANTITY);
        ComponentState amountOfPP = viewDefinitionState.getComponentByReference(L_AMOUNT_OF_PRODUCT_PRODUCED);

        if (order == null || order.getStringField(OrderFields.STATE).equals(OrderState.PENDING.getStringValue())
                || order.getStringField(OrderFields.STATE).equals(OrderState.ACCEPTED.getStringValue())) {
            doneQuantity.setEnabled(false);
            amountOfPP.setEnabled(false);
        } else if ("".equals(typeOfProductionRecording.getFieldValue())
                || TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording.getFieldValue())) {
            doneQuantity.setEnabled(true);
            amountOfPP.setEnabled(true);
        } else {
            doneQuantity.setEnabled(false);
            amountOfPP.setEnabled(false);
        }
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
//        final StateChangeContext orderStateChangeContext = stateChangeContextBuilder.build(
//                productionTrackingStateChangeAspect.getChangeEntityDescriber(), productionTracking, state.getStringValue());
//        
//        productionTrackingStateChangeAspect.changeState(orderStateChangeContext);
        
        stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, productionTracking, state.getStringValue());
    }

    @Override
    public Entity correct(Entity productionTracking) {
        DataDefinition productionTrackingDD = productionTracking.getDataDefinition();
        boolean last = productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING);
        Entity clearedProductionTracking = clearLastProductionTracking(productionTracking);
        Entity correctingProductionTracking = productionTrackingDD.copy(clearedProductionTracking.getId()).get(0);

        copyOtherFields(clearedProductionTracking, correctingProductionTracking);
        clearedProductionTracking.setField(ProductionTrackingFields.CORRECTION, correctingProductionTracking);
        correctingProductionTracking.setField(ProductionTrackingFields.IS_CORRECTION, true);
        correctingProductionTracking.setField(ProductionTrackingFields.LAST_TRACKING, last);
        productionTrackingDD.save(correctingProductionTracking);

        changeState(clearedProductionTracking, ProductionTrackingState.CORRECTED);
        return correctingProductionTracking;
    }

    private Entity clearLastProductionTracking(Entity productionTracking) {
        if (productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING)) {
            productionTracking.setField(ProductionTrackingFields.LAST_TRACKING, false);
            productionTracking = productionTracking.getDataDefinition().save(productionTracking);
            return productionTracking;
        } else {
            return productionTracking;
        }

    }

    private void copyOtherFields(Entity productionTracking, Entity correctingProductionTracking) {
        correctingProductionTracking.setField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES,
                productionTracking.getDecimalField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES));

        copyStaffWorkTimes(productionTracking, correctingProductionTracking);
        copyTrackingOperationProductInComponents(productionTracking, correctingProductionTracking);
        copyTrackingOperationProductOutComponents(productionTracking, correctingProductionTracking);
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
        trackingOperationProductInComponents.forEach(t -> copiedTrackingOperationProductInComponents.add(t.getDataDefinition()
                .copy(t.getId()).get(0)));
        correctingProductionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS,
                copiedTrackingOperationProductInComponents);
    }

    private void copyTrackingOperationProductOutComponents(Entity productionTracking, Entity correctingProductionTracking) {
        EntityList trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
        List<Entity> copiedTrackingOperationProductOutComponents = Lists.newArrayList();
        trackingOperationProductOutComponents.forEach(t -> copiedTrackingOperationProductOutComponents.add(t.getDataDefinition()
                .copy(t.getId()).get(0)));
        correctingProductionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS,
                copiedTrackingOperationProductOutComponents);
    }

    @Override
    public void unCorrect(Entity correctingProductionTracking) {
        Entity correctedProductionTracking = correctingProductionTracking.getDataDefinition().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.CORRECTION, correctingProductionTracking))
                .uniqueResult();
        if (correctedProductionTracking != null) {
            correctedProductionTracking.setField(ProductionTrackingFields.CORRECTION, null);
            changeState(correctedProductionTracking, ProductionTrackingState.ACCEPTED);
            correctingProductionTracking.setField(ProductionTrackingFields.IS_CORRECTION, false);
            correctingProductionTracking.getDataDefinition().save(correctingProductionTracking);
        }
    }
}
