package com.qcadoo.mes.productionPerShift.services;

import com.google.common.collect.Lists;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateStringValues;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class WorkersOnLineService {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String L_staffAssignmentToShiftState = "staffAssignmentToShiftState";

    private static final String L_assignmentToShiftState = "assignmentToShiftState";
    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Get workers on line for shift and date
     * @param productionLine
     * @param shift
     * @param date
     * @return number of workers
     */
    public Integer getWorkersOnLine(final Entity productionLine, final Entity shift, final DateTime date) {
        Integer workersOnLine = 0;
        if(productionLine == null || shift == null || date == null){
            return workersOnLine;
        }
        List<Entity> factories = getFactories();
        for(Entity factory : factories){
            workersOnLine += getWorkersOnLineForFactory(factory, productionLine, shift, date);
        }

        return workersOnLine;
    }

    private Integer getWorkersOnLineForFactory(Entity factory, Entity productionLine, Entity shift, DateTime date) {
        String query = buildQuery();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        List<Entity> entities = dataDefinitionService
                .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_STAFF_ASSIGNMENT_TO_SHIFT)
                .find(query)
                .setParameter("shiftId", shift.getId())
                .setParameter("factoryId", factory.getId())
                .setParameter("productionLineId",productionLine.getId())
                .setDate("date", date.toDate())
                .list().getEntities();
        List<Entity> entitiesToReturn = Lists.newArrayList();
        filterForCorrectState(entities, entitiesToReturn);
        Integer workersOnLine = entitiesToReturn.size();
        return workersOnLine;
    }

    private void filterForCorrectState(List<Entity> entities, List<Entity> entitiesToReturn) {
        for (Entity entity : entities) {
            if (entity.getStringField(L_assignmentToShiftState).equals(AssignmentToShiftStateStringValues.ACCEPTED)
                    && entity.getStringField(L_staffAssignmentToShiftState).equals(
                    StaffAssignmentToShiftState.ACCEPTED.getStringValue())) {
                entitiesToReturn.add(entity);
            } else if (entity.getStringField(L_assignmentToShiftState).equals(AssignmentToShiftStateStringValues.CORRECTED)
                    && entity.getStringField(L_staffAssignmentToShiftState).equals(
                    StaffAssignmentToShiftState.CORRECTED.getStringValue())) {
                entitiesToReturn.add(entity);
            }
        }
    }

    private String buildQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT DISTINCT ");
        builder.append("assignmentToShift.startDate as date, ");
        builder.append("staffAssignmentToShift.id as staffId, ");
        builder.append("staffAssignmentToShift.state as staffAssignmentToShiftState,  ");
        builder.append("assignmentToShift.state as assignmentToShiftState, ");
        builder.append("staffAssignmentToShift.worker.id as workerId ");
        builder.append("FROM #assignmentToShift_staffAssignmentToShift staffAssignmentToShift ");
        builder.append("LEFT JOIN staffAssignmentToShift.assignmentToShift as assignmentToShift ");
        builder.append("WHERE ");
        builder.append("assignmentToShift.state in ('02accepted','04corrected') ");
        builder.append("and assignmentToShift.startDate = ");
            builder.append("(");
                builder.append("select max(startDate) from #assignmentToShift_assignmentToShift ");
                builder.append("where (startDate < :date or startDate = :date) and state in ('02accepted','04corrected') ");
                builder.append("and shift.id = :shiftId and factory.id = :factoryId");
            builder.append(") ");
        builder.append("and assignmentToShift.shift.id = :shiftId ");
        builder.append("and assignmentToShift.factory.id = :factoryId ");
        builder.append("and staffAssignmentToShift.productionLine.id = :productionLineId");
        return builder.toString();
    }

    private List<Entity> getFactories() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_FACTORY).find().list()
                .getEntities();
    }


}
