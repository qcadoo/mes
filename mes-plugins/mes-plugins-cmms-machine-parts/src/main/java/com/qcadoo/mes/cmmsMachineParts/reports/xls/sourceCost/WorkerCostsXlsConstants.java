package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost;

import java.util.List;

import com.google.common.collect.Lists;

public final class WorkerCostsXlsConstants {

    private WorkerCostsXlsConstants() {

    }

    public static final String REPORT_TITLE = "cmmsMachineParts.workerCostsReport.report.title";

    public static final String COLUMN_sourceCost = "cmmsMachineParts.workerCostsReport.report.column.sourceCost";

    public static final String COLUMN_worker = "cmmsMachineParts.workerCostsReport.report.column.worker";

    public static final String COLUMN_event = "cmmsMachineParts.workerCostsReport.report.column.event";

    public static final String COLUMN_type = "cmmsMachineParts.workerCostsReport.report.column.type";

    public static final String COLUMN_workTime = "cmmsMachineParts.workerCostsReport.report.column.workTime";

    public static final String COLUMN_workerTimeSum = "cmmsMachineParts.workerCostsReport.report.column.workerTimeSum";

    public static final String COLUMN_costSourceTimeSum = "cmmsMachineParts.workerCostsReport.report.column.costSourceTimeSum";

    public static final List<String> ALL_COLUMNS = Lists
.newArrayList(COLUMN_sourceCost, COLUMN_worker, COLUMN_event,
            COLUMN_type, COLUMN_workTime, COLUMN_workerTimeSum, COLUMN_costSourceTimeSum);
}
