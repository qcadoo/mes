package com.qcadoo.mes.assignmentToShift.hooks;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.AssignmentToShiftReportHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class AssignmentToShiftReportHooks {

    @Autowired
    private AssignmentToShiftReportHelper assignmentToShiftReportHelper;

    public boolean checkIfIsMoreThatFiveDays(final DataDefinition dataDefinition, final Entity entity) {
        List<DateTime> days = assignmentToShiftReportHelper.getDaysFromGivenDate(entity);
        if (days.size() > 5) {
            entity.addError(entity.getDataDefinition().getField("dateFrom"),
                    "assignmentToShift.assignmentToShift.report.onlyFiveDays");
            entity.addError(entity.getDataDefinition().getField("dateTo"),
                    "assignmentToShift.assignmentToShift.report.onlyFiveDays");
            return false;
        }
        return true;
    }
}
