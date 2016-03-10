package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents;

import com.qcadoo.localization.api.TranslationService;

import java.util.Locale;

public enum MaintenanceEventsElementsReportEnum {

    NUMBER(0, "cmmsMachineParts.maintenanceEventsList.report.number"),
    TYPE(1, "cmmsMachineParts.maintenanceEventsList.report.type"),
    FACTORY_NUMBER(2, "cmmsMachineParts.maintenanceEventsList.report.factoryNumber"),
    DIVISION_NUMBER(3, "cmmsMachineParts.maintenanceEventsList.report.divisionNumber"),
    PRODUCTION_LINE_NUMBER(4, "cmmsMachineParts.maintenanceEventsList.report.productionLineNumber"),
    WORKSTATION_NUMBER(5, "cmmsMachineParts.maintenanceEventsList.report.workstationNumber"),
    SUNASSEMBY_NUMBER(6, "cmmsMachineParts.maintenanceEventsList.report.subassemblyNumber"),
    FAULT_TYPE_NAME(7, "cmmsMachineParts.maintenanceEventsList.report.faultTypeName"),
    DESCRIPTION(8, "cmmsMachineParts.maintenanceEventsList.report.description"),
    PERSON_RECEIVING(9, "cmmsMachineParts.maintenanceEventsList.report.personReceiving"),
    SOURCE_COST(10, "cmmsMachineParts.maintenanceEventsList.report.sourceCost"),
    STAFF_WORK_TIME_WORKER(11, "cmmsMachineParts.maintenanceEventsList.report.staffWorkTimeWorker"),
    STAFF_WORK_TIME_LABOR_TIME(12, "cmmsMachineParts.maintenanceEventsList.report.staffWorkTimeLaborTime"),
    PART_NUMBER(13, "cmmsMachineParts.maintenanceEventsList.report.partNumber"),
    PART_NAME(14, "cmmsMachineParts.maintenanceEventsList.report.partName"),
    WAREHOUSE_NUMBER(15, "cmmsMachineParts.maintenanceEventsList.report.warehouseNumber"),
    PART_PLANNED_QUANTITY(16, "cmmsMachineParts.maintenanceEventsList.report.partPlannedQuantity"),
    PART_UNIT(17, "cmmsMachineParts.maintenanceEventsList.report.partUnit"),
    VALUE(18, "cmmsMachineParts.maintenanceEventsList.report.value"),
    CREATE_DATE(19, "cmmsMachineParts.maintenanceEventsList.report.createDate"),
    CREATE_USER(20, "cmmsMachineParts.maintenanceEventsList.report.createUser"),
    DATE_BOOT(21, "cmmsMachineParts.maintenanceEventsList.report.dateBoot"),
    DATE_BOOT_USER(22, "cmmsMachineParts.maintenanceEventsList.report.user"),
    DATE_APPLICATION(23, "cmmsMachineParts.maintenanceEventsList.report.dateApplication"),
    DATE_APPLICATION_USER(24, "cmmsMachineParts.maintenanceEventsList.report.user"),
    DATE_ACCEPTANCE(25, "cmmsMachineParts.maintenanceEventsList.report.dateAcceptance"),
    DATE_ACCEPTANCE_USER(26, "cmmsMachineParts.maintenanceEventsList.report.user"),
    END_DATE(27, "cmmsMachineParts.maintenanceEventsList.report.endDate"),
    END_DATE_USER(28, "cmmsMachineParts.maintenanceEventsList.report.user"),
    STATE(29, "cmmsMachineParts.maintenanceEventsList.report.state"),
    SOLUTION_DESCRIPTION(30, "cmmsMachineParts.maintenanceEventsList.report.solutionDescription"),;

    private final int position;
    private final String label;

    MaintenanceEventsElementsReportEnum(int position, String label) {
        this.position = position;
        this.label = label;
    }

    public int getPosition() {
        return position;
    }

    public String getLabel(TranslationService translationService, Locale locale) {
        return translationService.translate(label, locale);
    }

}
