package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents;

import org.springframework.stereotype.Service;

@Service
public class MaintenanceEventsFilterUtils {

    private static final String FACTORY_FROM = "maintenanceevent.factoryNumber";

    private static final String FACTORY_TO = "factory.number";

    private static final String DIVISION_FROM = "maintenanceevent.divisionNumber";

    private static final String DIVISION_TO = "division.number";

    private static final String PRODUCTION_LINE_FROM = "maintenanceevent.productionLineNumber";

    private static final String PRODUCTION_LINE_TO = "productionline.number";

    private static final String WORKSTATION_FROM = "maintenanceevent.workstationNumber";

    private static final String WORKSTATION_TO = "workstation.number";

    private static final String SUBASSEMBLY_FROM = "maintenanceevent.subassemblyNumber";

    private static final String SUBASSEMBLY_TO = "subassembly.number";

    private static final String FAULT_FROM = "maintenanceevent.faultTypeName";

    private static final String FAULT_TO = "faulttype.name";

    private MaintenanceEventsFilterUtils() {
    }

    public static String processFilter(String filter) {
        if (filter.contains(FACTORY_FROM)) {
            filter = filter.replace(FACTORY_FROM, FACTORY_TO);
        }
        if (filter.contains(DIVISION_FROM)) {
            filter = filter.replace(DIVISION_FROM, DIVISION_TO);
        }
        if (filter.contains(PRODUCTION_LINE_FROM)) {
            filter = filter.replace(PRODUCTION_LINE_FROM, PRODUCTION_LINE_TO);
        }
        if (filter.contains(WORKSTATION_FROM)) {
            filter = filter.replace(WORKSTATION_FROM, WORKSTATION_TO);
        }
        if (filter.contains(SUBASSEMBLY_FROM)) {
            filter = filter.replace(SUBASSEMBLY_FROM, SUBASSEMBLY_TO);
        }
        if (filter.contains(FAULT_FROM)) {
            filter = filter.replace(FAULT_FROM, FAULT_TO);
        }
        return filter;
    }

}
