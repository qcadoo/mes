package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto.MachinePartDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto.MaintenanceEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto.StateChangeDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto.WorkTimeDTO;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateStringValues;
import com.qcadoo.model.api.NumberService;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class MaintenanceEventsXlsService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private MaintenanceEventsXLSDataProvider dataProvider;

    public void buildExcelDocument(final XSSFWorkbook xssfWorkbook, final Map<String, Object> filters, final Locale locale) {
        XSSFSheet sheet = xssfWorkbook.createSheet(translationService.translate("cmmsMachineParts.eventsList.report.title",
                locale));
        fillHeaderRow(xssfWorkbook, sheet, 0, locale);

        List<MaintenanceEventDTO> events = dataProvider.getEvents(filters);
        int rowCounter = 1;
        for (MaintenanceEventDTO maintenanceEventDTO : events) {
            rowCounter = fillEventsRows(xssfWorkbook, sheet, maintenanceEventDTO, rowCounter, locale);
        }

    }

    private int fillEventsRows(XSSFWorkbook xssfWorkbook, XSSFSheet sheet, MaintenanceEventDTO event, int rowCounter,
            Locale locale) {
        int rowCounterCopy = rowCounter;
        int partsCounter = rowCounterCopy;
        int realizationsCounter = rowCounterCopy;

        XSSFFont font = xssfWorkbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("ARIAL");
        font.setItalic(false);
        font.setBold(false);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        font.setColor(HSSFColor.BLACK.index);

        XSSFRow eventLine = sheet.createRow(rowCounterCopy);

        XSSFCell number = eventLine.createCell(MaintenanceEventsElementsReportEnum.NUMBER.getPosition());
        number.setCellValue(event.getNumber());
        number.getCellStyle().setFont(font);

        XSSFCell type = eventLine.createCell(MaintenanceEventsElementsReportEnum.TYPE.getPosition());
        type.setCellValue(XlsDataType.getValue(translationService, locale, event.getType()));

        XSSFCell factoryNumber = eventLine.createCell(MaintenanceEventsElementsReportEnum.FACTORY_NUMBER.getPosition());
        factoryNumber.setCellValue(event.getFactoryNumber());

        XSSFCell divisionNumber = eventLine.createCell(MaintenanceEventsElementsReportEnum.DIVISION_NUMBER.getPosition());
        divisionNumber.setCellValue(event.getDivisionNumber());

        XSSFCell productionLineNumber = eventLine.createCell(MaintenanceEventsElementsReportEnum.PRODUCTION_LINE_NUMBER
                .getPosition());
        productionLineNumber.setCellValue(event.getProductionLineNumber());

        XSSFCell workstationNumber = eventLine.createCell(MaintenanceEventsElementsReportEnum.WORKSTATION_NUMBER.getPosition());
        workstationNumber.setCellValue(event.getWorkstationNumber());

        XSSFCell subassemblyNumber = eventLine.createCell(MaintenanceEventsElementsReportEnum.SUNASSEMBY_NUMBER.getPosition());
        subassemblyNumber.setCellValue(event.getSubassemblyNumber());

        XSSFCell faultTypeName = eventLine.createCell(MaintenanceEventsElementsReportEnum.FAULT_TYPE_NAME.getPosition());
        faultTypeName.setCellValue(event.getFaultTypeName());

        XSSFCell description = eventLine.createCell(MaintenanceEventsElementsReportEnum.DESCRIPTION.getPosition());
        description.setCellValue(event.getDescription());

        XSSFCell personReceiving = eventLine.createCell(MaintenanceEventsElementsReportEnum.PERSON_RECEIVING.getPosition());
        personReceiving.setCellValue(event.getPersonReceiving());

        XSSFCell sourceCost = eventLine.createCell(MaintenanceEventsElementsReportEnum.SOURCE_COST.getPosition());
        sourceCost.setCellValue(event.getSourceCost());

        int rowsToAdd = event.subListSize();
        if (rowsToAdd > 0) {
            for (int i = 1; i < rowsToAdd; i++) {
                int r = rowCounterCopy + i;
                XSSFRow subEventLine = sheet.createRow(r);
                XSSFCell subnumber = subEventLine.createCell(MaintenanceEventsElementsReportEnum.NUMBER.getPosition());
                subnumber.setCellValue(event.getNumber());
            }
        }

        boolean first = true;
        if (rowsToAdd > 0) {
            for (WorkTimeDTO workTime : event.getWorkTimes()) {

                if (first) {
                    XSSFCell staffWorkTimeWorker = eventLine
                            .createCell(MaintenanceEventsElementsReportEnum.STAFF_WORK_TIME_WORKER.getPosition());
                    staffWorkTimeWorker.setCellValue(workTime.getStaffWorkTimeWorker());

                    XSSFCell staffWorkTimeLaborTime = eventLine
                            .createCell(MaintenanceEventsElementsReportEnum.STAFF_WORK_TIME_LABOR_TIME.getPosition());
                    staffWorkTimeLaborTime.setCellValue(XlsDataType.getValue(workTime.getStaffWorkTimeLaborTime()));
                    first = false;

                } else {
                    realizationsCounter++;
                    XSSFRow subEventLine = sheet.getRow(realizationsCounter);

                    XSSFCell staffWorkTimeWorker = subEventLine
                            .createCell(MaintenanceEventsElementsReportEnum.STAFF_WORK_TIME_WORKER.getPosition());
                    staffWorkTimeWorker.setCellValue(workTime.getStaffWorkTimeWorker());

                    XSSFCell staffWorkTimeLaborTime = subEventLine
                            .createCell(MaintenanceEventsElementsReportEnum.STAFF_WORK_TIME_LABOR_TIME.getPosition());
                    staffWorkTimeLaborTime.setCellValue(XlsDataType.getValue(workTime.getStaffWorkTimeLaborTime()));
                }
            }

            first = true;
            for (MachinePartDTO part : event.getMachineParts()) {
                if (first) {
                    XSSFCell partNumber = eventLine.createCell(MaintenanceEventsElementsReportEnum.PART_NUMBER.getPosition());
                    partNumber.setCellValue(part.getPartNumber());

                    XSSFCell partName = eventLine.createCell(MaintenanceEventsElementsReportEnum.PART_NAME.getPosition());
                    partName.setCellValue(part.getPartName());

                    XSSFCell warehouseNumber = eventLine
                            .createCell(MaintenanceEventsElementsReportEnum.WAREHOUSE_NUMBER.getPosition());
                    warehouseNumber.setCellValue(part.getWarehouseNumber());

                    XSSFCell partPlannedQuantity = eventLine
                            .createCell(MaintenanceEventsElementsReportEnum.PART_PLANNED_QUANTITY.getPosition());
                    partPlannedQuantity.setCellValue(XlsDataType.getValue(numberService, part.getPartPlannedQuantity()));

                    XSSFCell partUnit = eventLine.createCell(MaintenanceEventsElementsReportEnum.PART_UNIT.getPosition());
                    partUnit.setCellValue(part.getPartUnit());

                    XSSFCell value = eventLine.createCell(MaintenanceEventsElementsReportEnum.VALUE.getPosition());
                    value.setCellValue(XlsDataType.getValue(numberService, part.getValue()));
                    first = false;

                } else {
                    partsCounter++;
                    XSSFRow subEventLine = sheet.getRow(partsCounter);

                    XSSFCell partNumber = subEventLine.createCell(MaintenanceEventsElementsReportEnum.PART_NUMBER.getPosition());
                    partNumber.setCellValue(part.getPartNumber());

                    XSSFCell partName = subEventLine.createCell(MaintenanceEventsElementsReportEnum.PART_NAME.getPosition());
                    partName.setCellValue(part.getPartName());

                    XSSFCell warehouseNumber = subEventLine
                            .createCell(MaintenanceEventsElementsReportEnum.WAREHOUSE_NUMBER.getPosition());
                    warehouseNumber.setCellValue(part.getWarehouseNumber());

                    XSSFCell partPlannedQuantity = subEventLine
                            .createCell(MaintenanceEventsElementsReportEnum.PART_PLANNED_QUANTITY.getPosition());
                    partPlannedQuantity.setCellValue(XlsDataType.getValue(numberService, part.getPartPlannedQuantity()));

                    XSSFCell partUnit = subEventLine.createCell(MaintenanceEventsElementsReportEnum.PART_UNIT.getPosition());
                    partUnit.setCellValue(part.getPartUnit());

                    XSSFCell value = subEventLine.createCell(MaintenanceEventsElementsReportEnum.VALUE.getPosition());
                    value.setCellValue(XlsDataType.getValue(numberService, part.getValue()));
                }
            }
        }

        fillStateChanges(event, eventLine, locale);

        XSSFCell solutionDescription = eventLine.createCell(MaintenanceEventsElementsReportEnum.SOLUTION_DESCRIPTION
                .getPosition());
        solutionDescription.setCellValue(event.getSolutionDescription());

        if (rowsToAdd > 1) {
            return rowCounter + rowsToAdd;
        } else {
            return rowCounter + 1;
        }

    }

    private void fillStateChanges(MaintenanceEventDTO event, XSSFRow eventLine, Locale locale) {

        XSSFCell createDate = eventLine.createCell(MaintenanceEventsElementsReportEnum.CREATE_DATE.getPosition());
        createDate.setCellValue(XlsDataType.getValue(event.getCreateDate(), false));

        XSSFCell createUser = eventLine.createCell(MaintenanceEventsElementsReportEnum.CREATE_USER.getPosition());
        createUser.setCellValue(event.getCreateUser());

        XSSFCell dateBoot = eventLine.createCell(MaintenanceEventsElementsReportEnum.DATE_BOOT.getPosition());
        dateBoot.setCellValue(getDateForState(MaintenanceEventStateStringValues.IN_PROGRESS, event.getStateChange()));

        XSSFCell dateBootUser = eventLine.createCell(MaintenanceEventsElementsReportEnum.DATE_BOOT_USER.getPosition());
        dateBootUser.setCellValue(getWorkerForState(MaintenanceEventStateStringValues.IN_PROGRESS, event.getStateChange()));

        XSSFCell dateApplication = eventLine.createCell(MaintenanceEventsElementsReportEnum.DATE_APPLICATION.getPosition());
        dateApplication.setCellValue(getDateForState(MaintenanceEventStateStringValues.EDITED, event.getStateChange()));

        XSSFCell dateApplicationUser = eventLine.createCell(MaintenanceEventsElementsReportEnum.DATE_APPLICATION_USER
                .getPosition());
        dateApplicationUser.setCellValue(getWorkerForState(MaintenanceEventStateStringValues.EDITED, event.getStateChange()));

        XSSFCell dateAcceptance = eventLine.createCell(MaintenanceEventsElementsReportEnum.DATE_ACCEPTANCE.getPosition());
        dateAcceptance.setCellValue(getDateForState(MaintenanceEventStateStringValues.ACCEPTED, event.getStateChange()));

        XSSFCell dateAcceptanceUser = eventLine
                .createCell(MaintenanceEventsElementsReportEnum.DATE_ACCEPTANCE_USER.getPosition());
        dateAcceptanceUser.setCellValue(getWorkerForState(MaintenanceEventStateStringValues.ACCEPTED, event.getStateChange()));

        XSSFCell endDate = eventLine.createCell(MaintenanceEventsElementsReportEnum.END_DATE.getPosition());
        endDate.setCellValue(getDateForState(MaintenanceEventStateStringValues.CLOSED, event.getStateChange()));

        XSSFCell endDateUser = eventLine.createCell(MaintenanceEventsElementsReportEnum.END_DATE_USER.getPosition());
        endDateUser.setCellValue(getWorkerForState(MaintenanceEventStateStringValues.CLOSED, event.getStateChange()));

        XSSFCell state = eventLine.createCell(MaintenanceEventsElementsReportEnum.STATE.getPosition());
        state.setCellValue(XlsDataType.getValue(translationService, locale, event.getState()));
    }

    private String getDateForState(final String state, final List<StateChangeDTO> states) {
        Optional<StateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e1.getStateChangeDateAndTime().compareTo(e1.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return DateUtils.toDateTimeString(op.get().getStateChangeDateAndTime());
        }
        return "";
    }

    private String getWorkerForState(final String state, final List<StateChangeDTO> states) {
        Optional<StateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e1.getStateChangeDateAndTime().compareTo(e1.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return op.get().getStateWorker();
        }
        return "";
    }

    private void fillHeaderRow(XSSFWorkbook xssfWorkbook, XSSFSheet sheet, Integer rowNum, Locale locale) {
        XSSFRow headerLine = sheet.createRow(rowNum);

        XSSFFont font = xssfWorkbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("ARIAL");
        font.setItalic(false);
        font.setBold(true);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);

        font.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle style = xssfWorkbook.createCellStyle();
        style.setFont(font);

        Lists.newArrayList(MaintenanceEventsElementsReportEnum.values()).forEach(
                e -> createHeaderCell(e, headerLine, style, locale));
    }

    private void createHeaderCell(MaintenanceEventsElementsReportEnum e, XSSFRow headerLine, XSSFCellStyle style, Locale locale) {
        XSSFCell headerCell = headerLine.createCell(e.getPosition());
        headerCell.setCellValue(e.getLabel(translationService, locale));
        headerCell.setCellStyle(style);
    }
}
