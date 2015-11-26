package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import com.google.common.collect.Lists;

import java.util.List;

public final class PlannedEventsXlsConstants {

    private PlannedEventsXlsConstants() {

    }

    public static final String REPORT_TITLE = "cmmsMachineParts.plannedEventsList.report.title";

    public static final String COLUMN_number = "cmmsMachineParts.plannedEventsList.report.culumn.number";

    public static final String COLUMN_type = "cmmsMachineParts.plannedEventsList.report.culumn.type";

    public static final String COLUMN_description = "cmmsMachineParts.plannedEventsList.report.culumn.description";

    public static final String COLUMN_ownerName = "cmmsMachineParts.plannedEventsList.report.culumn.ownerName";

    public static final String COLUMN_state = "cmmsMachineParts.plannedEventsList.report.culumn.state";

    public static final String COLUMN_factoryNumber = "cmmsMachineParts.plannedEventsList.report.culumn.factoryNumber";

    public static final String COLUMN_divisionNumber = "cmmsMachineParts.plannedEventsList.report.culumn.divisionNumber";

    public static final String COLUMN_productionLineNumber = "cmmsMachineParts.plannedEventsList.report.culumn.productionLineNumber";

    public static final String COLUMN_workstationNumber = "cmmsMachineParts.plannedEventsList.report.culumn.workstationNumber";

    public static final String COLUMN_subassemblyNumber = "cmmsMachineParts.plannedEventsList.report.culumn.subassemblyNumber";

    public static final String COLUMN_companyNumber = "cmmsMachineParts.plannedEventsList.report.culumn.companyNumber";

    public static final String COLUMN_sourceCostNumber = "cmmsMachineParts.plannedEventsList.report.culumn.sourceCostNumber";

    public static final String COLUMN_plannedSeparately = "cmmsMachineParts.plannedEventsList.report.culumn.plannedSeparately";

    public static final String COLUMN_requiresShutdown = "cmmsMachineParts.plannedEventsList.report.culumn.requiresShutdown";

    public static final String COLUMN_basedOn = "cmmsMachineParts.plannedEventsList.report.culumn.basedOn";

    public static final String COLUMN_date = "cmmsMachineParts.plannedEventsList.report.culumn.date";

    public static final String COLUMN_counter = "cmmsMachineParts.plannedEventsList.report.culumn.counter";

    public static final String COLUMN_counterTolerance = "cmmsMachineParts.plannedEventsList.report.culumn.counterTolerance";

    public static final String COLUMN_duration = "cmmsMachineParts.plannedEventsList.report.culumn.duration";

    public static final String COLUMN_effectiveDuration = "cmmsMachineParts.plannedEventsList.report.culumn.effectiveDuration";

    public static final String COLUMN_effectiveCounter = "cmmsMachineParts.plannedEventsList.report.culumn.effectiveCounter";

    public static final String COLUMN_startDate = "cmmsMachineParts.plannedEventsList.report.culumn.startDate";

    public static final String COLUMN_finishDate = "cmmsMachineParts.plannedEventsList.report.culumn.finishDate";

    public static final String COLUMN_isDeadline = "cmmsMachineParts.plannedEventsList.report.culumn.isDeadline";

    public static final String COLUMN_machinePartName = "cmmsMachineParts.plannedEventsList.report.culumn.machinePartName";

    public static final String COLUMN_machinePartNumber = "cmmsMachineParts.plannedEventsList.report.culumn.machinePartNumber";

    public static final String COLUMN_machinePartUnit = "cmmsMachineParts.plannedEventsList.report.culumn.machinePartUnit";

    public static final String COLUMN_machinePartPlannedQuantity = "cmmsMachineParts.plannedEventsList.report.culumn.machinePartPlannedQuantity";

    public static final String COLUMN_realizationWorkerSurname = "cmmsMachineParts.plannedEventsList.report.culumn.realizationWorkerName";

    public static final String COLUMN_realizationDuration = "cmmsMachineParts.plannedEventsList.report.culumn.realizationDuration";

    public static final String COLUMN_stateAuthor = "cmmsMachineParts.plannedEventsList.report.culumn.stateAuthor";

    public static final String COLUMN_stateStartDate = "cmmsMachineParts.plannedEventsList.report.culumn.stateStartDate";

    public static final String COLUMN_stateStopDate = "cmmsMachineParts.plannedEventsList.report.culumn.stateStopDate";

    public static final String COLUMN_stateInRealizationDate = "cmmsMachineParts.plannedEventsList.report.culumn.stateInRealizationDate";

    public static final String COLUMN_stateAcceptedDate = "cmmsMachineParts.plannedEventsList.report.culumn.stateAcceptedDate";

    public static final String COLUMN_stateRealizationDate = "cmmsMachineParts.plannedEventsList.report.culumn.stateRealizationDate";

    public static final List<String> ALL_COLUMNS = Lists
            .newArrayList(COLUMN_number, COLUMN_type, COLUMN_factoryNumber, COLUMN_divisionNumber, COLUMN_productionLineNumber,
                    COLUMN_workstationNumber, COLUMN_subassemblyNumber, COLUMN_description, COLUMN_ownerName,
                    COLUMN_plannedSeparately, COLUMN_requiresShutdown,  COLUMN_basedOn, COLUMN_date, COLUMN_counter, COLUMN_counterTolerance,
                    COLUMN_sourceCostNumber, COLUMN_duration, COLUMN_effectiveDuration, COLUMN_effectiveCounter,
                    COLUMN_startDate, COLUMN_finishDate, COLUMN_machinePartName, COLUMN_machinePartNumber,
                    COLUMN_machinePartPlannedQuantity, COLUMN_machinePartUnit, COLUMN_realizationWorkerSurname,
                    COLUMN_realizationDuration, COLUMN_stateAuthor, COLUMN_stateStartDate, COLUMN_stateStopDate,
                    COLUMN_stateInRealizationDate, COLUMN_stateAcceptedDate, COLUMN_stateRealizationDate, COLUMN_state);
}
