package com.qcadoo.mes.assignmentToShift;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.START_DATE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_FROM;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_TO;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.STATE;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.OccupationTypeEnumStringValue;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftStateStringValue;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AssignmentToShiftReportHelper {

    private static final String EMPTY = "";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    public List<DateTime> getDaysFromGivenDate(final Entity entity) {
        List<DateTime> days = new LinkedList<DateTime>();
        DateTime dateFrom = new DateTime((Date) entity.getField(DATE_FROM));
        DateTime dateTo = new DateTime((Date) entity.getField(DATE_TO));
        DateTime nextDay = dateFrom;
        int dayQuantity = Days.daysBetween(dateFrom.toDateMidnight(), dateTo.toDateMidnight()).getDays();
        days.add(nextDay);
        int oneDay = 1;
        while (dayQuantity != 0) {
            nextDay = nextDay.plusDays(oneDay).toDateTime();
            days.add(nextDay);
            dayQuantity--;
        }
        return days;
    }

    public List<Entity> getAssignmentToShift(final Entity entity) {
        return dataDefinitionService
                .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT).find()
                .add(SearchRestrictions.belongsTo(SHIFT, entity.getBelongsToField(SHIFT))).list().getEntities();
    }

    public Entity getAssignmentToShift(final Entity shift, final Date date) {
        boolean shiftWorks = shiftsService.checkIfShiftWorkAtDate(date, shift);
        if (shiftWorks) {
            return dataDefinitionService
                    .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT)
                    .find().add(SearchRestrictions.belongsTo(SHIFT, shift)).add(SearchRestrictions.le(START_DATE, date))
                    .addOrder(SearchOrders.desc(START_DATE)).setMaxResults(1).uniqueResult();
        } else {
            return null;
        }
    }

    public String getListOfWorker(final List<Entity> staffs) {
        if (staffs.isEmpty()) {
            return EMPTY;
        }
        StringBuilder listOfWorker = new StringBuilder();
        for (Entity staff : staffs) {
            Entity worker = staff.getBelongsToField(StaffAssignmentToShiftFields.WORKER);
            listOfWorker.append(worker.getStringField("name"));
            listOfWorker.append(" ");
            listOfWorker.append(worker.getStringField("surname"));
            listOfWorker.append(", ");
        }
        return listOfWorker.toString();
    }

    public List<Entity> getStaffsList(final Entity assignmentToShift, final OccupationTypeEnumStringValue occupationTypeEnum,
            final Entity productionLine) {
        List<Entity> staffs = new ArrayList<Entity>();
        SearchCriterion criterion = SearchRestrictions.eq(StaffAssignmentToShiftFields.OCCUPATION_TYPE_ENUM,
                occupationTypeEnum.getStringValue());
        String assignmentState = assignmentToShift.getStringField(AssignmentToShiftFields.STATE);
        if ("04corrected".equals(assignmentState)) {
            staffs = assignmentToShift.getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS).find().add(criterion)
                    .add(SearchRestrictions.eq(STATE, StaffAssignmentToShiftStateStringValue.CORRECTED))
                    .add(SearchRestrictions.belongsTo(StaffAssignmentToShiftFields.PRODUCTION_LINE, productionLine)).list()
                    .getEntities();
        } else if (!"01draft".equals(assignmentState)) {
            staffs = assignmentToShift.getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS).find().add(criterion)
                    .add(SearchRestrictions.eq(STATE, StaffAssignmentToShiftStateStringValue.ACCEPTED))
                    .add(SearchRestrictions.belongsTo(StaffAssignmentToShiftFields.PRODUCTION_LINE, productionLine)).list()
                    .getEntities();
        }
        return staffs;
    }

    public List<Entity> getProductionLines() {
        return dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE).find().list()
                .getEntities();
    }

}
