package com.qcadoo.mes.avgLaborCostCalcForOrder.listeners;

import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.WORKER;
import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.AVERAGE_LABOR_HOURLY_COST;
import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.BASED_ON;
import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.FINISH_DATE;
import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.ORDER;
import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.PRODUCTION_LINE;
import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.START_DATE;
import static com.qcadoo.mes.costNormsForOperation.constants.TechnologyInstanceOperCompFieldsCNFO.LABOR_HOURLY_COST;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AssignmentWorkerToShiftFields;
import com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderConstants;
import com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.wageGroups.constants.StaffFieldsWG;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class AvgLaborCostCalcForOrderDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TimeConverterService timeConverterService;

    @Autowired
    private ShiftsService shiftsService;

    public void setAvgLaborCostCalcForGivenOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (args.length < 2) {
            return;
        }
        Long sourceId = Long.valueOf(args[1]);
        DataDefinition avgLaborCostCalcForOrderDD = dataDefinitionService.get(
                AvgLaborCostCalcForOrderConstants.PLUGIN_IDENTIFIER,
                AvgLaborCostCalcForOrderConstants.MODEL_AVG_LABOR_COST_CALC_FOR_ORDER);
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(sourceId);
        FormComponent avgLaborCostCalcForOrderForm = (FormComponent) view.getComponentByReference("form");
        Entity avgLaborCostCalcForOrder = avgLaborCostCalcForOrderDD.find().add(SearchRestrictions.belongsTo("order", order))
                .uniqueResult();
        if (avgLaborCostCalcForOrder == null) {
            avgLaborCostCalcForOrder = createAverageWithDataFromOrder(view, order, avgLaborCostCalcForOrderDD);
        }
        avgLaborCostCalcForOrderForm.setEntity(avgLaborCostCalcForOrder);
        avgLaborCostCalcForOrderForm.performEvent(view, "refresh", args);
    }

    private Entity createAverageWithDataFromOrder(final ViewDefinitionState view, final Entity order,
            final DataDefinition avgLaborCostCalcForOrderDD) {
        Entity avgLaborCostCalcForOrder = avgLaborCostCalcForOrderDD.create();
        avgLaborCostCalcForOrder.setField(ORDER, order);
        avgLaborCostCalcForOrder.setField(START_DATE, order.getField(OrderFields.START_DATE));
        avgLaborCostCalcForOrder.setField(FINISH_DATE, order.getField(OrderFields.FINISH_DATE));
        avgLaborCostCalcForOrder.setField(PRODUCTION_LINE, order.getBelongsToField(OrderFields.PRODUCTION_LINE));
        avgLaborCostCalcForOrder.setField(BASED_ON, "01assignment");
        return avgLaborCostCalcForOrder.getDataDefinition().save(avgLaborCostCalcForOrder);

    }

    public void generateAssignmentWorkerToShiftAndAverageCost(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        state.performEvent(view, "save", args);
        FieldComponent startDate = (FieldComponent) view.getComponentByReference(START_DATE);
        FieldComponent finishDate = (FieldComponent) view.getComponentByReference(FINISH_DATE);
        FieldComponent averageLaborHourlyCost = (FieldComponent) view.getComponentByReference(AVERAGE_LABOR_HOURLY_COST);

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity avgLaborCostCalcForOrder = form.getEntity().getDataDefinition().get(form.getEntityId());
        Entity shiftWorkAtStartDate = shiftsService.getShiftFromDateWithTime(timeConverterService.getDateFromField(startDate
                .getFieldValue()));
        Entity shiftWorkAtFinishDate = shiftsService.getShiftFromDateWithTime(timeConverterService.getDateFromField(finishDate
                .getFieldValue()));
        Entity productionLine = getProductionLinesFromLookup(view);

        avgLaborCostCalcForOrder.setField(
                AvgLaborCostCalcForOrderFields.ASSIGNMENT_WORKER_TO_SHIFTS,
                generateAssignmentWorkerToShift(getAssignmentForGivenShift(shiftWorkAtStartDate, shiftWorkAtFinishDate),
                        productionLine));
        avgLaborCostCalcForOrder.setField(AVERAGE_LABOR_HOURLY_COST, generateAverageCost(avgLaborCostCalcForOrder));
        avgLaborCostCalcForOrder.getDataDefinition().save(avgLaborCostCalcForOrder);
        // TODO ALBR why refresh actions on field or form doesn't work?
        averageLaborHourlyCost.setFieldValue(avgLaborCostCalcForOrder.getDecimalField(AVERAGE_LABOR_HOURLY_COST));
        averageLaborHourlyCost.requestComponentUpdateState();
    }

    public void copyToOperationsNorms(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity avgLaborCostCalcForOrder = form.getEntity().getDataDefinition().get(form.getEntityId());
        Entity order = avgLaborCostCalcForOrder.getBelongsToField(AvgLaborCostCalcForOrderFields.ORDER);
        List<Entity> tiocs = order.getHasManyField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
        for (Entity tioc : tiocs) {
            tioc.setField(LABOR_HOURLY_COST, avgLaborCostCalcForOrder.getDecimalField(AVERAGE_LABOR_HOURLY_COST));
            tioc.getDataDefinition().save(tioc);
        }
    }

    private BigDecimal generateAverageCost(final Entity avgLaborCostCalcForOrder) {
        BigDecimal averageCost = BigDecimal.ZERO;
        List<Entity> assignmentWorkerToShifts = avgLaborCostCalcForOrder.getHasManyField("assignmentWorkerToShifts");
        for (Entity assignmentWorkerToShift : assignmentWorkerToShifts) {
            Entity worker = assignmentWorkerToShift.getBelongsToField(AssignmentWorkerToShiftFields.WORKER);
            averageCost = averageCost.add(worker.getDecimalField(StaffFieldsWG.LABOR_HOURLY_COST));
        }
        averageCost = averageCost.divide(BigDecimal.valueOf(assignmentWorkerToShifts.size()));
        return averageCost;
    }

    private List<Entity> getAssignmentForGivenShift(final Entity shiftWorkAtStartDate, final Entity shiftWorkAtFinishDate) {
        return dataDefinitionService
                .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT)
                .find()
                .add(SearchRestrictions.or(SearchRestrictions.belongsTo("shift", shiftWorkAtStartDate),
                        SearchRestrictions.belongsTo("shift", shiftWorkAtFinishDate))).list().getEntities();
    }

    private List<Entity> generateAssignmentWorkerToShift(final List<Entity> assignmentToShifts, final Entity productionLine) {
        List<Entity> assignmentWorkerToShifts = new ArrayList<Entity>();
        for (Entity assignmentToShift : assignmentToShifts) {
            String assignmentState = assignmentToShift.getStringField(AssignmentToShiftFields.STATE);
            for (Entity staffAssignmentToShift : getStaffAssignmentToShiftDependOnAssignmentToShiftState(assignmentToShift,
                    assignmentState, productionLine)) {
                assignmentWorkerToShifts.add(createAssignmentWorkerToShift(assignmentToShift, staffAssignmentToShift));
            }
        }
        return assignmentWorkerToShifts;
    }

    private List<Entity> getStaffAssignmentToShiftDependOnAssignmentToShiftState(final Entity assignmentToShift,
            final String state, final Entity productionLine) {
        List<Entity> staffAssignmentToShifts = new ArrayList<Entity>();
        SearchCriteriaBuilder searchCriteriaBuilder = assignmentToShift.getHasManyField("staffAssignmentToShifts").find()
                .add(SearchRestrictions.eq("occupationTypeEnum", "01workOnLine"))
                .add(SearchRestrictions.belongsTo(StaffAssignmentToShiftFields.PRODUCTION_LINE, productionLine));
        if (state.equals(AssignmentToShiftState.CORRECTED.getStringValue())) {
            staffAssignmentToShifts = searchCriteriaBuilder
                    .add(SearchRestrictions.eq("state", StaffAssignmentToShiftState.CORRECTED)).list().getEntities();
        } else if (state.equals(AssignmentToShiftState.ACCEPTED.getStringValue())
                || state.equals(AssignmentToShiftState.DURING_CORRECTION.getStringValue())) {
            staffAssignmentToShifts = searchCriteriaBuilder
                    .add(SearchRestrictions.eq("state", StaffAssignmentToShiftState.ACCEPTED)).list().getEntities();
        }
        return staffAssignmentToShifts;
    }

    private Entity createAssignmentWorkerToShift(final Entity assignmentToShift, final Entity staffAssignmentToShift) {
        Entity assignmentWorkerToShift = dataDefinitionService.get(AvgLaborCostCalcForOrderConstants.PLUGIN_IDENTIFIER,
                AvgLaborCostCalcForOrderConstants.MODEL_ASSIGNMENT_WORKER_TO_SHIFT).create();
        assignmentWorkerToShift.setField(AssignmentWorkerToShiftFields.ASSIGNMENT_TO_SHIFT, assignmentToShift);
        assignmentWorkerToShift.setField(AssignmentWorkerToShiftFields.WORKER, staffAssignmentToShift.getBelongsToField(WORKER));
        return assignmentWorkerToShift;
    }

    private Entity getProductionLinesFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference("productionLine");
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get((Long) lookup.getFieldValue());
    }

}
