package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_FROM;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_TO;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.FILE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.GENERATED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.print.xls.AssignmentToShiftXlsHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class AssignmentToShiftReportHooks {

    @Autowired
    private AssignmentToShiftXlsHelper assignmentToShiftXlsHelper;

    public boolean checkIfIsMoreThatFiveDays(final DataDefinition assignmentToShiftReportDD, final Entity assignmentToShiftReport) {
        int days = assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport);

        if (days > 5) {
            assignmentToShiftReport.addError(assignmentToShiftReportDD.getField(DATE_FROM),
                    "assignmentToShift.assignmentToShift.report.onlyFiveDays");
            assignmentToShiftReport.addError(assignmentToShiftReportDD.getField(DATE_TO),
                    "assignmentToShift.assignmentToShift.report.onlyFiveDays");

            return false;
        }

        return true;
    }

    public void clearGenerated(final DataDefinition assignmentToShiftReportDD, final Entity assignmentToShiftReport) {
        assignmentToShiftReport.setField(GENERATED, false);
        assignmentToShiftReport.setField(FILE_NAME, null);
    }

}
