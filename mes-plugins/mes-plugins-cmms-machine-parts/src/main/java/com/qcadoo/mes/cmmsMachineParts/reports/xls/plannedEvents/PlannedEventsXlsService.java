package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.MachinePartForEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventRealizationDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventStateChangeDTO;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.NumberService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service public class PlannedEventsXlsService {

    @Autowired private TranslationService translationService;

    @Autowired private PlannedEventsXLSDataProvider plannedEventsXLSDataProvider;

    @Autowired private NumberService numberService;

    private static final int REALIZATIONS_COLUMN_POSITION_START = 25;

    private static final int PARTS_COLUMN_POSITION_START = 21;

    public String getReportTitle(final Locale locale) {
        return translationService.translate(PlannedEventsXlsConstants.REPORT_TITLE, locale);
    }

    public void buildExcelContent(final HSSFWorkbook workbook, final HSSFSheet sheet, final Map<String, Object> filters, final Locale locale) {
        List<PlannedEventDTO> events = plannedEventsXLSDataProvider.getEvents(filters);
        fillHeaderRow(workbook, sheet, 0, locale);
        int rowCounter = 1;
        for (PlannedEventDTO plannedEventDTO : events) {
            rowCounter = fillEventsRows(workbook, sheet, plannedEventDTO, rowCounter++, locale);
        }
    }

    private void fillHeaderRow(final HSSFWorkbook workbook, final HSSFSheet sheet, Integer rowNum, final Locale locale) {
        HSSFRow headerLine = sheet.createRow(rowNum);
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        HSSFCellStyle style = workbook.createCellStyle();

            style.setFont(font);

        int colNumber = 0;
        for (String column : PlannedEventsXlsConstants.ALL_COLUMNS) {
            HSSFCell headerCell = headerLine.createCell(colNumber);
            headerCell.setCellValue(translationService.translate(column, locale));
            headerCell.setCellStyle(style);
            colNumber++;
        }
    }

    private int fillEventsRows(final HSSFWorkbook workbook, final HSSFSheet sheet, final PlannedEventDTO event, int rowCounter, final Locale locale) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        int rowCounterCopy = rowCounter;
        int partsCounter = rowCounterCopy;
        int realizationsCounter = rowCounterCopy;
        //dodanie wiersza ze zdarzeniem
        HSSFRow eventLine = sheet.createRow(rowCounterCopy);

        HSSFCell numberCell = eventLine.createCell(0);
        numberCell.setCellValue(event.getNumber());
        numberCell.getCellStyle().setFont(font);
        HSSFCell typeCell = eventLine.createCell(1);
        typeCell.setCellValue(translationService.translate(event.getType(), locale));

        HSSFCell factoryCell = eventLine.createCell(2);
        factoryCell.setCellValue(event.getFactoryNumber());

        HSSFCell divisionCell = eventLine.createCell(3);
        divisionCell.setCellValue(event.getDivisionNumber());

        HSSFCell productionLineCell = eventLine.createCell(4);
        productionLineCell.setCellValue(event.getProductionLineNumber());

        HSSFCell workstationCell = eventLine.createCell(5);
        workstationCell.setCellValue(event.getWorkstationNumber());

        HSSFCell subassemblyCell = eventLine.createCell(6);
        subassemblyCell.setCellValue(event.getSubassemblyNumber());

        HSSFCell descriptionCell = eventLine.createCell(7);
        descriptionCell.setCellValue(event.getDescription());

        HSSFCell ownerNameCell = eventLine.createCell(8);
        ownerNameCell.setCellValue(event.getOwnerName());

        HSSFCell plannedSeparatelyCell = eventLine.createCell(9);
        plannedSeparatelyCell.setCellValue(getValue(event.getPlannedSeparately()));

        HSSFCell requiresShutdownCell = eventLine.createCell(10);
        requiresShutdownCell.setCellValue(getValue(event.getRequiresShutdown()));

        HSSFCell basedOnCell = eventLine.createCell(11);
        basedOnCell.setCellValue(translationService.translate(event.getBasedOn(), locale));

        HSSFCell dateCell = eventLine.createCell(12);
        dateCell.setCellValue(getDateOnly(event.getDate()));

        HSSFCell counterCell = eventLine.createCell(13);
        counterCell.setCellValue(getDecimalValue(event.getCounter()));

        HSSFCell counterToleranceCell = eventLine.createCell(14);
        counterToleranceCell.setCellValue(getDecimalValue(event.getCounterTolerance()));

        HSSFCell sourceCostNumberCell = eventLine.createCell(15);
        sourceCostNumberCell.setCellValue(event.getSourceCostNumber());

        HSSFCell durationCell = eventLine.createCell(16);
        durationCell.setCellValue(getTime(event.getDuration()));

        HSSFCell effectiveCounterCell = eventLine.createCell(17);
        effectiveCounterCell.setCellValue(getDecimalValue(event.getEffectiveCounter()));

        HSSFCell startDateCell = eventLine.createCell(18);
        startDateCell.setCellValue(getDateValue(event.getStartDate()));

        HSSFCell finishDateCell = eventLine.createCell(19);
        finishDateCell.setCellValue(getDateValue(event.getFinishDate()));

        HSSFCell solutionDescriptionCell = eventLine.createCell(20);
        solutionDescriptionCell.setCellValue(event.getSolutionDescription());

        //dodanie sub wierszy
        int rowsToAdd = event.subListSize();
        for (int i = 1; i < rowsToAdd; i++) {
            int r = rowCounterCopy + i;
            HSSFRow subEventLine = sheet.createRow(r);
        }

        if (rowsToAdd > 0) {
            boolean first = true;
            for (PlannedEventRealizationDTO realization : event.getRealizations()) {

                if (first) {
                    HSSFCell realizationWorkerNameCell = eventLine.createCell(21);
                    realizationWorkerNameCell.setCellValue(
                            realization.getRealizationWorkerName() + " " + realization.getRealizationWorkerSurname());
                    HSSFCell realizationDurationNumberCell = eventLine.createCell(22);
                    realizationDurationNumberCell.setCellValue(getTime(realization.getRealizationDuration()));
                    first = false;

                } else {
                    realizationsCounter++;
                    HSSFRow subEventLine = sheet.getRow(realizationsCounter);
                    HSSFCell realizationWorkerNameCell = subEventLine.createCell(21);
                    realizationWorkerNameCell.setCellValue(
                            realization.getRealizationWorkerName() + " " + realization.getRealizationWorkerSurname());
                    HSSFCell realizationDurationNumberCell = subEventLine.createCell(22);
                    realizationDurationNumberCell.setCellValue(getTime(realization.getRealizationDuration()));
                }
            }

            first = true;
            for (MachinePartForEventDTO part : event.getParts()) {
                if (first) {
                    HSSFCell machinePartNumberCell = eventLine.createCell(23);
                    machinePartNumberCell.setCellValue(part.getMachinePartNumber());
                    HSSFCell machinePartNameCell = eventLine.createCell(24);
                    machinePartNameCell.setCellValue(part.getMachinePartName());
                    HSSFCell machinePartQuantityCell = eventLine.createCell(25);
                    machinePartQuantityCell.setCellValue(getDecimalValue(part.getMachinePartPlannedQuantity()));
                    HSSFCell machinePartUnitCell = eventLine.createCell(26);
                    machinePartUnitCell.setCellValue(part.getMachinePartUnit());
                    first = false;

                } else {
                    partsCounter++;
                    HSSFRow subEventLine = sheet.getRow(partsCounter);
                    HSSFCell machinePartNumberCell = subEventLine.createCell(23);
                    machinePartNumberCell.setCellValue(part.getMachinePartNumber());
                    HSSFCell machinePartNameCell = subEventLine.createCell(24);
                    machinePartNameCell.setCellValue(part.getMachinePartName());
                    HSSFCell machinePartQuantityCell = subEventLine.createCell(25);
                    machinePartQuantityCell.setCellValue(getDecimalValue(part.getMachinePartPlannedQuantity()));
                    HSSFCell machinePartUnitCell = subEventLine.createCell(26);
                    machinePartUnitCell.setCellValue(part.getMachinePartUnit());
                }
            }
        }

        fillStateChange(eventLine, event);

        if (rowsToAdd > 1) {
            return rowCounter + rowsToAdd - 1;
        } else {
            return rowCounter + 1;
        }
    }

    private void fillStateChange(HSSFRow eventLine, PlannedEventDTO event) {
        List<PlannedEventStateChangeDTO> states = event.getStateChanges();

        HSSFCell createDateCell = eventLine.createCell(27);
        createDateCell.setCellValue(getDateValue(event.getCreatedate()));

        HSSFCell stateAuthorCell = eventLine.createCell(28);
        stateAuthorCell.setCellValue(event.getCreateuser());

        HSSFCell stateStartDateCell = eventLine.createCell(29);
        stateStartDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.IN_PLAN, states));

        HSSFCell stateStopDateCell = eventLine.createCell(30);
        stateStopDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.PLANNED, states));

        HSSFCell stateInRealizationDateCell = eventLine.createCell(31);
        stateInRealizationDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.IN_REALIZATION, states));

        HSSFCell stateAcceptedDateDateCell = eventLine.createCell(32);
        stateAcceptedDateDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.ACCEPTED, states));

        HSSFCell stateRealizationDateCell = eventLine.createCell(33);
        stateRealizationDateCell.setCellValue(getDateForState(PlannedEventStateStringValues.REALIZED, states));

        HSSFCell stateCell = eventLine.createCell(34);
        stateCell.setCellValue(translationService.translate(event.getState(), LocaleContextHolder.getLocale()));
    }

    private String getDateForState(final String state, final List<PlannedEventStateChangeDTO> states) {
        Optional<PlannedEventStateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e1.getStateChangeDateAndTime().compareTo(e1.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return DateUtils.toDateTimeString(op.get().getStateChangeDateAndTime());
        }
        return "";
    }

    private void fillSubRows(final HSSFSheet sheet, final PlannedEventDTO event, Integer rowNum, final Locale locale) {

    }

    private String getIntValue(Integer value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private String getTime(Integer value) {
        if (value == null) {
            return "";
        }
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HHHH:mm:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(value*1000L*100L));

        return time;
    }

    private String getDecimalValue(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return numberService.formatWithMinimumFractionDigits(value,0);
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
