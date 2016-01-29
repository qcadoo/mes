package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage;

import java.util.List;

import com.google.common.collect.Lists;

public final class TimeUsageXlsConstants {

    private TimeUsageXlsConstants() {

    }

    public static final String REPORT_TITLE = "cmmsMachineParts.timeUsageReport.report.title";

    public static final String COLUMN_worker = "cmmsMachineParts.timeUsageReport.report.column.worker";

    public static final String COLUMN_startDate = "cmmsMachineParts.timeUsageReport.report.column.startDate";

    public static final String COLUMN_number = "cmmsMachineParts.timeUsageReport.report.column.number";

    public static final String COLUMN_type = "cmmsMachineParts.timeUsageReport.report.column.type";

    public static final String COLUMN_state = "cmmsMachineParts.timeUsageReport.report.column.state";

    public static final String COLUMN_object = "cmmsMachineParts.timeUsageReport.report.column.object";

    public static final String COLUMN_parts = "cmmsMachineParts.timeUsageReport.report.column.parts";

    public static final String COLUMN_description = "cmmsMachineParts.timeUsageReport.report.column.description";

    public static final String COLUMN_duration = "cmmsMachineParts.timeUsageReport.report.column.duration";

    public static final String COLUMN_registeredTime = "cmmsMachineParts.timeUsageReport.report.column.registeredTime";

    public static final String COLUMN_durationSum = "cmmsMachineParts.timeUsageReport.report.column.durationSum";

    public static final String COLUMN_registeredTimeSum = "cmmsMachineParts.timeUsageReport.report.column.registeredTimeSum";

    public static final List<String> ALL_COLUMNS = Lists
.newArrayList(COLUMN_worker, COLUMN_startDate, COLUMN_number,
            COLUMN_type, COLUMN_state, COLUMN_object, COLUMN_parts, COLUMN_description, COLUMN_duration, COLUMN_registeredTime,
            COLUMN_durationSum, COLUMN_registeredTimeSum);
}
