package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.MachinePartForEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventRealizationDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventStateChangeDTO;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.NumberService;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PlannedEventsXlsService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PlannedEventsXLSDataProvider plannedEventsXLSDataProvider;

    @Autowired
    private NumberService numberService;

    public String getReportTitle(final Locale locale) {
        return translationService.translate(PlannedEventsXlsConstants.REPORT_TITLE, locale);
    }

    public void buildExcelContent(final XSSFWorkbook workbook, final XSSFSheet sheet, final Map<String, Object> filters,
            final Locale locale) {
        List<PlannedEventDTO> events = plannedEventsXLSDataProvider.getEvents(filters);
        fillHeaderRow(workbook, sheet, 0, locale);
        int rowCounter = 1;
        for (PlannedEventDTO plannedEventDTO : events) {
            rowCounter = fillEventsRows(workbook, sheet, plannedEventDTO, rowCounter++, locale);
        }
    }

    private void fillHeaderRow(final XSSFWorkbook workbook, final XSSFSheet sheet, Integer rowNum, final Locale locale) {
        XSSFRow headerLine = sheet.createRow(rowNum);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("ARIAL");
        font.setItalic(false);
        font.setBold(true);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);

        font.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle style = workbook.createCellStyle();

        style.setFont(font);

        int colNumber = 0;
        for (String column : PlannedEventsXlsConstants.ALL_COLUMNS) {
            XSSFCell headerCell = headerLine.createCell(colNumber);
            headerCell.setCellValue(translationService.translate(column, locale));
            headerCell.setCellStyle(style);
            colNumber++;
        }
    }

    private int fillEventsRows(final XSSFWorkbook workbook, final XSSFSheet sheet, final PlannedEventDTO event, int rowCounter,
            final Locale locale) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        int rowCounterCopy = rowCounter;
        int partsCounter = rowCounterCopy;
        int realizationsCounter = rowCounterCopy;
        XSSFRow eventLine = sheet.createRow(rowCounterCopy);

        XSSFCell numberCell = eventLine.createCell(0);
        numberCell.setCellValue(event.getNumber());
        numberCell.getCellStyle().setFont(font);
        XSSFCell typeCell = eventLine.createCell(1);
        typeCell.setCellValue(translationService.translate(event.getType(), locale));

        XSSFCell factoryCell = eventLine.createCell(2);
        factoryCell.setCellValue(event.getFactoryNumber());

        XSSFCell divisionCell = eventLine.createCell(3);
        divisionCell.setCellValue(event.getDivisionNumber());

        XSSFCell productionLineCell = eventLine.createCell(4);
        productionLineCell.setCellValue(event.getProductionLineNumber());

        XSSFCell workstationCell = eventLine.createCell(5);
        workstationCell.setCellValue(event.getWorkstationNumber());

        XSSFCell subassemblyCell = eventLine.createCell(6);
        subassemblyCell.setCellValue(event.getSubassemblyNumber());

        XSSFCell descriptionCell = eventLine.createCell(7);
        descriptionCell.setCellValue(event.getDescription());

        XSSFCell ownerNameCell = eventLine.createCell(8);
        ownerNameCell.setCellValue(event.getOwnerName());

        XSSFCell plannedSeparatelyCell = eventLine.createCell(9);
        plannedSeparatelyCell.setCellValue(getValue(event.getPlannedSeparately()));

        XSSFCell requiresShutdownCell = eventLine.createCell(10);
        requiresShutdownCell.setCellValue(getValue(event.getRequiresShutdown()));

        XSSFCell basedOnCell = eventLine.createCell(11);
        basedOnCell.setCellValue(translationService.translate(event.getBasedOn(), locale));

        XSSFCell dateCell = eventLine.createCell(12);
        dateCell.setCellValue(getDateOnly(event.getDate()));

        XSSFCell counterCell = eventLine.createCell(13);
        counterCell.setCellValue(getDecimalValue(event.getCounter()));

        XSSFCell counterToleranceCell = eventLine.createCell(14);
        counterToleranceCell.setCellValue(getDecimalValue(event.getCounterTolerance()));

        XSSFCell sourceCostNumberCell = eventLine.createCell(15);
        sourceCostNumberCell.setCellValue(event.getSourceCostNumber());

        XSSFCell durationCell = eventLine.createCell(16);
        durationCell.setCellValue(getTime(event.getDuration()));

        XSSFCell effectiveCounterCell = eventLine.createCell(17);
        effectiveCounterCell.setCellValue(getDecimalValue(event.getEffectiveCounter()));

        XSSFCell startDateCell = eventLine.createCell(18);
        startDateCell.setCellValue(getDateValue(event.getStartDate()));

        XSSFCell finishDateCell = eventLine.createCell(19);
        finishDateCell.setCellValue(getDateValue(event.getFinishDate()));

        XSSFCell solutionDescriptionCell = eventLine.createCell(20);
        solutionDescriptionCell.setCellValue(event.getSolutionDescription());

        // dodanie sub wierszy
        int rowsToAdd = event.subListSize();
        for (int i = 1; i < rowsToAdd; i++) {
            int r = rowCounterCopy + i;
            XSSFRow subEventLine = sheet.createRow(r);
            XSSFCell subEventLineNumberCell = subEventLine.createCell(0);
            subEventLineNumberCell.setCellValue(event.getNumber());
        }

        if (rowsToAdd > 0) {
            boolean first = true;
            for (PlannedEventRealizationDTO realization : event.getRealizations()) {

                if (first) {
                    XSSFCell realizationWorkerNameCell = eventLine.createCell(21);
                    realizationWorkerNameCell.setCellValue(
                            realization.getRealizationWorkerName() + " " + realization.getRealizationWorkerSurname());
                    XSSFCell realizationDurationNumberCell = eventLine.createCell(22);
                    realizationDurationNumberCell.setCellValue(getTime(realization.getRealizationDuration()));
                    first = false;

                } else {
                    realizationsCounter++;
                    XSSFRow subEventLine = sheet.getRow(realizationsCounter);
                    XSSFCell realizationWorkerNameCell = subEventLine.createCell(21);
                    realizationWorkerNameCell.setCellValue(
                            realization.getRealizationWorkerName() + " " + realization.getRealizationWorkerSurname());
                    XSSFCell realizationDurationNumberCell = subEventLine.createCell(22);
                    realizationDurationNumberCell.setCellValue(getTime(realization.getRealizationDuration()));
                }
            }

            first = true;
            for (MachinePartForEventDTO part : event.getParts()) {
                if (first) {
                    XSSFCell machinePartNumberCell = eventLine.createCell(23);
                    machinePartNumberCell.setCellValue(part.getMachinePartNumber());
                    XSSFCell machinePartNameCell = eventLine.createCell(24);
                    machinePartNameCell.setCellValue(part.getMachinePartName());
                    XSSFCell machinePartQuantityCell = eventLine.createCell(25);
                    machinePartQuantityCell.setCellValue(getDecimalValue(part.getMachinePartPlannedQuantity()));
                    XSSFCell machinePartUnitCell = eventLine.createCell(26);
                    machinePartUnitCell.setCellValue(part.getMachinePartUnit());
                    XSSFCell valueCell = eventLine.createCell(27);
                    valueCell.setCellValue(getDecimalValue(part.getValue()));
                    first = false;

                } else {
                    partsCounter++;
                    XSSFRow subEventLine = sheet.getRow(partsCounter);
                    XSSFCell machinePartNumberCell = subEventLine.createCell(23);
                    machinePartNumberCell.setCellValue(part.getMachinePartNumber());
                    XSSFCell machinePartNameCell = subEventLine.createCell(24);
                    machinePartNameCell.setCellValue(part.getMachinePartName());
                    XSSFCell machinePartQuantityCell = subEventLine.createCell(25);
                    machinePartQuantityCell.setCellValue(getDecimalValue(part.getMachinePartPlannedQuantity()));
                    XSSFCell machinePartUnitCell = subEventLine.createCell(26);
                    machinePartUnitCell.setCellValue(part.getMachinePartUnit());
                    XSSFCell valueCell = subEventLine.createCell(27);
                    valueCell.setCellValue(getDecimalValue(part.getValue()));
                }
            }
        }

        fillStateChange(eventLine, event);

        if (rowsToAdd > 1) {
            return rowCounter + rowsToAdd;
        } else {
            return rowCounter + 1;
        }
    }

    private void fillStateChange(XSSFRow eventLine, PlannedEventDTO event) {
        List<PlannedEventStateChangeDTO> states = event.getStateChanges();

        XSSFCell createDateCell = eventLine.createCell(28);
        createDateCell.setCellValue(getDateValue(event.getCreatedate()));

        XSSFCell stateAuthorCell = eventLine.createCell(29);
        stateAuthorCell.setCellValue(event.getCreateuser());

        XSSFCell stateStartDateCell = eventLine.createCell(30);
        stateStartDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.IN_PLAN, states));

        XSSFCell stateStartDateWCell = eventLine.createCell(31);
        stateStartDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.IN_PLAN, states));

        XSSFCell stateStopDateCell = eventLine.createCell(32);
        stateStopDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.PLANNED, states));

        XSSFCell stateStopDateWCell = eventLine.createCell(33);
        stateStopDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.PLANNED, states));

        XSSFCell stateInRealizationDateCell = eventLine.createCell(34);
        stateInRealizationDateCell.setCellValue(getFirstDateForState(PlannedEventStateStringValues.IN_REALIZATION, states));

        XSSFCell stateInRealizationDateWCell = eventLine.createCell(35);
        stateInRealizationDateWCell.setCellValue(getFirstWorkerForState(PlannedEventStateStringValues.IN_REALIZATION, states));

        XSSFCell stateInEditingDateCell = eventLine.createCell(36);
        stateInEditingDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.IN_EDITING, states));

        XSSFCell stateInEditingDateWCell = eventLine.createCell(37);
        stateInEditingDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.IN_EDITING, states));

        XSSFCell stateAcceptedDateDateCell = eventLine.createCell(38);
        stateAcceptedDateDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.ACCEPTED, states));

        XSSFCell stateAcceptedDateWCell = eventLine.createCell(39);
        stateAcceptedDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.ACCEPTED, states));

        XSSFCell stateRealizationDateCell = eventLine.createCell(40);
        stateRealizationDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.REALIZED, states));

        XSSFCell stateRealizationDateWCell = eventLine.createCell(41);
        stateRealizationDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.REALIZED, states));

        XSSFCell stateCell = eventLine.createCell(42);
        stateCell.setCellValue(translationService.translate(event.getState(), LocaleContextHolder.getLocale()));
    }

    private String getDateForState(final String state, final List<PlannedEventStateChangeDTO> states) {
        Optional<PlannedEventStateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e1.getStateChangeDateAndTime().compareTo(e2.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return DateUtils.toDateTimeString(op.get().getStateChangeDateAndTime());
        }
        return "";
    }

    private String getFirstDateForState(final String state, final List<PlannedEventStateChangeDTO> states) {
        Optional<PlannedEventStateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e2.getStateChangeDateAndTime().compareTo(e1.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return DateUtils.toDateTimeString(op.get().getStateChangeDateAndTime());
        }
        return "";
    }

    private String getWorkerForState(final String state, final List<PlannedEventStateChangeDTO> states) {
        Optional<PlannedEventStateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e1.getStateChangeDateAndTime().compareTo(e2.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return op.get().getStateWorker();
        }
        return "";
    }

    private String getFirstWorkerForState(final String state, final List<PlannedEventStateChangeDTO> states) {
        Optional<PlannedEventStateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e2.getStateChangeDateAndTime().compareTo(e1.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return op.get().getStateWorker();
        }
        return "";
    }

    private String getTime(Integer value) {
        if (value == null) {
            return "";
        }
        int hours = value / 3600;
        int minutes = (value % 3600) / 60;
        int seconds = value % 60;

        String timeString = String.format("%04d:%02d:%02d", hours, minutes, seconds);

        return timeString;
    }

    private String getDecimalValue(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return numberService.formatWithMinimumFractionDigits(value, 0);
    }

    private String getDateValue(Date date) {
        if (date == null) {
            return "";
        }
        return DateUtils.toDateTimeString(date);
    }

    private String getDateOnly(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.L_DATE_FORMAT);
        String time = df.format(date);

        return time;
    }

    private String getValue(Object value) {
        if (value == null) {
            return translationService.translate("qcadooView.false", LocaleContextHolder.getLocale());
        }
        if ((Boolean) value) {
            return translationService.translate("qcadooView.true", LocaleContextHolder.getLocale());
        } else {
            return translationService.translate("qcadooView.false", LocaleContextHolder.getLocale());
        }

    }
}
