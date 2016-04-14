package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import org.springframework.stereotype.Service;

@Service
public class PlannedEventsFilterUtils {

    private PlannedEventsFilterUtils(){}
    
    private static final String FACTORY_FROM = "event.factoryNumber";
    private static final String FACTORY_TO = "factory.number";
    private static final String DIVISION_FROM = "event.divisionNumber";
    private static final String DIVISION_TO = "division.number";
    private static final String PRODUCTION_LINE_FROM = "event.productionLineNumber";
    private static final String PRODUCTION_LINE_TO = "productionline.number";
    private static final String WORKSTATION_FROM = "event.workstationNumber";
    private static final String WORKSTATION_TO = "workstation.number";
    private static final String SUBASSEMBLY_FROM = "event.subassemblyNumber";
    private static final String SUBASSEMBLY_TO = "subassembly.number";


    public static String processFilter(String filter){
        if(filter.contains(FACTORY_FROM)){
            filter = filter.replace(FACTORY_FROM, FACTORY_TO);
        }
        if(filter.contains(DIVISION_FROM)){
            filter = filter.replace(DIVISION_FROM, DIVISION_TO);
        }
        if(filter.contains(PRODUCTION_LINE_FROM)){
            filter = filter.replace(PRODUCTION_LINE_FROM, PRODUCTION_LINE_TO);
        }
        if(filter.contains(WORKSTATION_FROM)){
            filter = filter.replace(WORKSTATION_FROM, WORKSTATION_TO);
        }
        if(filter.contains(SUBASSEMBLY_FROM)){
            filter = filter.replace(SUBASSEMBLY_FROM, SUBASSEMBLY_TO);
        }
        return filter;
    }
}
