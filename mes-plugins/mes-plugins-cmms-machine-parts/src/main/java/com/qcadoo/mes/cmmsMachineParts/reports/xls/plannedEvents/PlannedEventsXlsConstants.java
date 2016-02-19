package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import com.google.common.collect.Lists;

import java.util.List;

public final class PlannedEventsXlsConstants {

    private PlannedEventsXlsConstants() {

    }

    public static final String REPORT_TITLE = "cmmsMachineParts.plannedEventsList.report.title";

    public static final String COLUMN_number = "cmmsMachineParts.plannedEventsList.report.column.number";

    public static final String COLUMN_type = "cmmsMachineParts.plannedEventsList.report.column.type";

    public static final String COLUMN_description = "cmmsMachineParts.plannedEventsList.report.column.description";

    public static final String COLUMN_ownerName = "cmmsMachineParts.plannedEventsList.report.column.ownerName";

    public static final String COLUMN_state = "cmmsMachineParts.plannedEventsList.report.column.state";

    public static final String COLUMN_factoryNumber = "cmmsMachineParts.plannedEventsList.report.column.factoryNumber";

    public static final String COLUMN_divisionNumber = "cmmsMachineParts.plannedEventsList.report.column.divisionNumber";

    public static final String COLUMN_productionLineNumber = "cmmsMachineParts.plannedEventsList.report.column.productionLineNumber";

    public static final String COLUMN_workstationNumber = "cmmsMachineParts.plannedEventsList.report.column.workstationNumber";

    public static final String COLUMN_subassemblyNumber = "cmmsMachineParts.plannedEventsList.report.column.subassemblyNumber";

    public static final String COLUMN_companyNumber = "cmmsMachineParts.plannedEventsList.report.column.companyNumber";

    public static final String COLUMN_sourceCostNumber = "cmmsMachineParts.plannedEventsList.report.column.sourceCostNumber";

    public static final String COLUMN_plannedSeparately = "cmmsMachineParts.plannedEventsList.report.column.plannedSeparately";

    public static final String COLUMN_requiresShutdown = "cmmsMachineParts.plannedEventsList.report.column.requiresShutdown";

    public static final String COLUMN_basedOn = "cmmsMachineParts.plannedEventsList.report.column.basedOn";

    public static final String COLUMN_date = "cmmsMachineParts.plannedEventsList.report.column.date";

    public static final String COLUMN_counter = "cmmsMachineParts.plannedEventsList.report.column.counter";

    public static final String COLUMN_counterTolerance = "cmmsMachineParts.plannedEventsList.report.column.counterTolerance";

    public static final String COLUMN_duration = "cmmsMachineParts.plannedEventsList.report.column.duration";

    public static final String COLUMN_effectiveDuration = "cmmsMachineParts.plannedEventsList.report.column.effectiveDuration";

    public static final String COLUMN_effectiveCounter = "cmmsMachineParts.plannedEventsList.report.column.effectiveCounter";

    public static final String COLUMN_startDate = "cmmsMachineParts.plannedEventsList.report.column.startDate";

    public static final String COLUMN_finishDate = "cmmsMachineParts.plannedEventsList.report.column.finishDate";

    public static final String COLUMN_isDeadline = "cmmsMachineParts.plannedEventsList.report.column.isDeadline";

    public static final String COLUMN_machinePartName = "cmmsMachineParts.plannedEventsList.report.column.machinePartName";

    public static final String COLUMN_machinePartNumber = "cmmsMachineParts.plannedEventsList.report.column.machinePartNumber";

    public static final String COLUMN_machinePartUnit = "cmmsMachineParts.plannedEventsList.report.column.machinePartUnit";

    public static final String COLUMN_machinePartPlannedQuantity = "cmmsMachineParts.plannedEventsList.report.column.machinePartPlannedQuantity";

    public static final String COLUMN_machinePartValue = "cmmsMachineParts.plannedEventsList.report.column.value";

    public static final String COLUMN_realizationWorkerSurname = "cmmsMachineParts.plannedEventsList.report.column.realizationWorkerName";

    public static final String COLUMN_realizationDuration = "cmmsMachineParts.plannedEventsList.report.column.realizationDuration";

    public static final String COLUMN_stateAuthor = "cmmsMachineParts.plannedEventsList.report.column.stateAuthor";

    public static final String COLUMN_stateStartDate = "cmmsMachineParts.plannedEventsList.report.column.stateStartDate";

    public static final String COLUMN_stateStartDateWorker = "cmmsMachineParts.plannedEventsList.report.column.stateWorker";

    public static final String COLUMN_stateStopDate = "cmmsMachineParts.plannedEventsList.report.column.stateStopDate";

    public static final String COLUMN_stateStopDateWorker = "cmmsMachineParts.plannedEventsList.report.column.stateWorker";

    public static final String COLUMN_stateInRealizationDate = "cmmsMachineParts.plannedEventsList.report.column.stateInRealizationDate";

    public static final String COLUMN_stateInRealizationDateWorker = "cmmsMachineParts.plannedEventsList.report.column.stateWorker";

    public static final String COLUMN_stateEditDate = "cmmsMachineParts.plannedEventsList.report.column.stateEditDate";

    public static final String COLUMN_stateEditDateWorker = "cmmsMachineParts.plannedEventsList.report.column.stateWorker";

    public static final String COLUMN_stateAcceptedDate = "cmmsMachineParts.plannedEventsList.report.column.stateAcceptedDate";

    public static final String COLUMN_stateAcceptedDateWorker = "cmmsMachineParts.plannedEventsList.report.column.stateWorker";

    public static final String COLUMN_stateRealizationDate = "cmmsMachineParts.plannedEventsList.report.column.stateRealizationDate";

    public static final String COLUMN_stateRealizationDateWorker = "cmmsMachineParts.plannedEventsList.report.column.stateWorker";

    public static final String COLUMN_solutionDescription = "cmmsMachineParts.plannedEventsList.report.column.solutionDescription";

    public static final String COLUMN_createdate = "cmmsMachineParts.plannedEventsList.report.column.createdate";

    public static final List<String> ALL_COLUMNS = Lists.newArrayList(COLUMN_number, COLUMN_type, COLUMN_factoryNumber,
            COLUMN_divisionNumber, COLUMN_productionLineNumber, COLUMN_workstationNumber, COLUMN_subassemblyNumber,
            COLUMN_description, COLUMN_ownerName, COLUMN_plannedSeparately, COLUMN_requiresShutdown, COLUMN_basedOn, COLUMN_date,
            COLUMN_counter, COLUMN_counterTolerance, COLUMN_sourceCostNumber, COLUMN_duration, COLUMN_effectiveCounter,
            COLUMN_startDate, COLUMN_finishDate, COLUMN_solutionDescription, COLUMN_realizationWorkerSurname,
            COLUMN_realizationDuration, COLUMN_machinePartNumber, COLUMN_machinePartName, COLUMN_machinePartPlannedQuantity,
            COLUMN_machinePartUnit, COLUMN_machinePartValue, COLUMN_createdate, COLUMN_stateAuthor, COLUMN_stateStartDate,
            COLUMN_stateStartDateWorker, COLUMN_stateStopDate, COLUMN_stateStopDateWorker, COLUMN_stateInRealizationDate,
            COLUMN_stateInRealizationDateWorker, COLUMN_stateEditDate, COLUMN_stateEditDateWorker, COLUMN_stateAcceptedDate,
            COLUMN_stateAcceptedDateWorker, COLUMN_stateRealizationDate, COLUMN_stateRealizationDateWorker, COLUMN_state);
}
