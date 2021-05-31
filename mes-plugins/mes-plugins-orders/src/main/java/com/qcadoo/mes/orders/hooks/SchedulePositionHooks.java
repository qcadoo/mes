package com.qcadoo.mes.orders.hooks;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class SchedulePositionHooks {

    public void onSave(final DataDefinition dataDefinition, final Entity schedulePosition) {
        if (schedulePosition.getId() != null) {
            Date startTime = schedulePosition.getDateField(SchedulePositionFields.START_TIME);
            Date endTime = schedulePosition.getDateField(SchedulePositionFields.END_TIME);
            if (endTime == null) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                        "qcadooView.validate.field.error.missing");
            }
            if (startTime == null) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                        "qcadooView.validate.field.error.missing");
            } else {
                validateChildrenDates(dataDefinition, schedulePosition);
            }
            if ((startTime != null) && (endTime != null) && endTime.before(startTime)) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                        "orders.validate.global.error.endTime");
            }
        }
    }

    private void validateChildrenDates(DataDefinition dataDefinition, Entity schedulePosition) {
        List<Entity> children = getChildren(dataDefinition, schedulePosition);
        Entity schedule = schedulePosition.getBelongsToField(SchedulePositionFields.SCHEDULE);
        for (Entity child : children) {
            Date childEndTime = child.getDateField(SchedulePositionFields.END_TIME);
            if (childEndTime != null) {
                if (!schedule.getBooleanField(ScheduleFields.ADDITIONAL_TIME_EXTENDS_OPERATION)) {
                    childEndTime = Date.from(
                            childEndTime.toInstant().plusSeconds(child.getIntegerField(SchedulePositionFields.ADDITIONAL_TIME)));
                }
                if (childEndTime.after(schedulePosition.getDateField(SchedulePositionFields.START_TIME))) {
                    schedulePosition.addGlobalMessage("orders.schedulePosition.message.linkedOperationStartTimeIncorrect");
                    break;
                }
            }
        }
    }

    private List<Entity> getChildren(final DataDefinition dataDefinition, final Entity schedulePosition) {
        return dataDefinition.find().createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, "toc", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("toc." + TechnologyOperationComponentFields.PARENT,
                        schedulePosition.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.ORDER,
                        schedulePosition.getBelongsToField(SchedulePositionFields.ORDER)))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE,
                        schedulePosition.getBelongsToField(SchedulePositionFields.SCHEDULE)))
                .list().getEntities();
    }
}
