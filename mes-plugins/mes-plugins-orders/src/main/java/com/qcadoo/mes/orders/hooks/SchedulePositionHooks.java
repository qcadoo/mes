package com.qcadoo.mes.orders.hooks;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

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
            if (schedulePosition.getDateField(SchedulePositionFields.END_TIME) == null) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                        "qcadooView.validate.field.error.missing");
            }
            if (schedulePosition.getDateField(SchedulePositionFields.START_TIME) == null) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                        "qcadooView.validate.field.error.missing");
            } else {
                List<Entity> children = dataDefinition.find()
                        .createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, "toc", JoinType.INNER)
                        .add(SearchRestrictions.belongsTo("toc." + TechnologyOperationComponentFields.PARENT,
                                schedulePosition.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)))
                        .list().getEntities();
                for (Entity child : children) {
                    if (child.getDateField(SchedulePositionFields.END_TIME) != null) {
                        Date childEndTimeWithAdditionalTime = Date.from(child.getDateField(SchedulePositionFields.END_TIME)
                                .toInstant().plusSeconds(child.getIntegerField(SchedulePositionFields.ADDITIONAL_TIME)));
                        if (childEndTimeWithAdditionalTime
                                .after(schedulePosition.getDateField(SchedulePositionFields.START_TIME))) {
                            schedulePosition
                                    .addGlobalMessage("orders.schedulePosition.message.linkedOperationStartTimeIncorrect");
                            break;
                        }
                    }
                }
            }
        }
    }
}
