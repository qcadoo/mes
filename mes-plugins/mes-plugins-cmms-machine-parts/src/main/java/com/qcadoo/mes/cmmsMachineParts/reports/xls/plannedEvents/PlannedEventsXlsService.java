package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.XlsDataType;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.MachinePartForEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventRealizationDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventStateChangeDTO;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.NumberService;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.qcadoo.model.api.BigDecimalUtils.*;

@Service
public class PlannedEventsXlsService {

    private static final Pattern TIME_SEPARATOR_PATTERN = Pattern.compile(":");

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
        DataFormat dataFormat = workbook.createDataFormat();
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(dataFormat.getFormat("0.00###"));

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(dataFormat.getFormat("yyyy-mm-dd"));

        CellStyle dateTimeStyle = workbook.createCellStyle();
        dateTimeStyle.setDataFormat(dataFormat.getFormat("yyyy-mm-dd hh:mm"));

        CellStyle timeStyle = workbook.createCellStyle();
        timeStyle.setDataFormat(dataFormat.getFormat("[HH]:MM:SS"));

        int rowCounter = 1;
        for (PlannedEventDTO plannedEventDTO : events) {
            rowCounter = fillEventsRows(workbook, sheet, plannedEventDTO, rowCounter++, numberStyle, dateStyle, dateTimeStyle,
                    timeStyle, locale);
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
            CellStyle numberStyle, CellStyle dateStyle, CellStyle dateTimeStyle, CellStyle timeStyle, final Locale locale) {
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
        if (event.getDate() != null) {
            dateCell.setCellValue(event.getDate());
            dateCell.setCellStyle(dateStyle);
        }

        XSSFCell counterCell = eventLine.createCell(13);
        counterCell.setCellStyle(numberStyle);
        counterCell.setCellType(Cell.CELL_TYPE_NUMERIC);
        if (event.getCounter() != null) {
            counterCell.setCellValue(event.getCounter().setScale(5).doubleValue());
        }

        XSSFCell counterToleranceCell = eventLine.createCell(14);
        counterToleranceCell.setCellStyle(numberStyle);
        counterToleranceCell.setCellType(Cell.CELL_TYPE_NUMERIC);
        if (event.getCounterTolerance() != null) {
            counterToleranceCell.setCellValue(event.getCounterTolerance().setScale(5).doubleValue());
        }

        XSSFCell sourceCostNumberCell = eventLine.createCell(15);
        sourceCostNumberCell.setCellValue(event.getSourceCostNumber());

        XSSFCell durationCell = eventLine.createCell(16);
        if (event.getDuration() != null) {
            durationCell.setCellStyle(timeStyle);
            durationCell.setCellValue(convertTimeInternal(XlsDataType.getValue(event.getDuration())));
            durationCell.setCellType(Cell.CELL_TYPE_NUMERIC);
        }
        XSSFCell effectiveCounterCell = eventLine.createCell(17);
        effectiveCounterCell.setCellStyle(numberStyle);
        effectiveCounterCell.setCellType(Cell.CELL_TYPE_NUMERIC);
        if (event.getEffectiveCounter() != null) {
            effectiveCounterCell.setCellValue(event.getEffectiveCounter().setScale(5).doubleValue());
        }
        XSSFCell startDateCell = eventLine.createCell(18);
        if (event.getStartDate() != null) {
            startDateCell.setCellValue(event.getStartDate());
            startDateCell.setCellStyle(dateTimeStyle);
        }

        XSSFCell finishDateCell = eventLine.createCell(19);
        if (event.getFinishDate() != null) {
            finishDateCell.setCellValue(event.getFinishDate());
            finishDateCell.setCellStyle(dateTimeStyle);
        }

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
                    realizationWorkerNameCell.setCellValue(realization.getRealizationWorkerName() + " "
                            + realization.getRealizationWorkerSurname());
                    XSSFCell realizationDurationNumberCell = eventLine.createCell(22);
                    if (realization.getRealizationDuration() != null) {
                        realizationDurationNumberCell.setCellStyle(timeStyle);
                        realizationDurationNumberCell.setCellValue(convertTimeInternal(XlsDataType.getValue(realization
                                .getRealizationDuration())));
                        realizationDurationNumberCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    }
                    first = false;

                } else {
                    realizationsCounter++;
                    XSSFRow subEventLine = sheet.getRow(realizationsCounter);
                    XSSFCell realizationWorkerNameCell = subEventLine.createCell(21);
                    realizationWorkerNameCell.setCellValue(realization.getRealizationWorkerName() + " "
                            + realization.getRealizationWorkerSurname());
                    XSSFCell realizationDurationNumberCell = subEventLine.createCell(22);
                    if (realization.getRealizationDuration() != null) {
                        realizationDurationNumberCell.setCellStyle(timeStyle);
                        realizationDurationNumberCell.setCellValue(convertTimeInternal(XlsDataType.getValue(realization
                                .getRealizationDuration())));
                        realizationDurationNumberCell.setCellValue(DateUtil.convertTime(DurationFormatUtils.formatDuration(
                                realization.getRealizationDuration() * 1000l, "HH:mm:ss")));
                        realizationDurationNumberCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    }
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
                    machinePartQuantityCell.setCellStyle(numberStyle);
                    machinePartQuantityCell.setCellType(Cell.CELL_TYPE_NUMERIC);

                    if (part.getMachinePartPlannedQuantity() != null) {
                        machinePartQuantityCell.setCellValue(part.getMachinePartPlannedQuantity().setScale(5).doubleValue());
                    }
                    XSSFCell machinePartUnitCell = eventLine.createCell(26);
                    machinePartUnitCell.setCellValue(part.getMachinePartUnit());

                    XSSFCell valueCell = eventLine.createCell(27);
                    valueCell.setCellStyle(numberStyle);
                    valueCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    if (part.getMachinePartPlannedQuantity() != null) {
                        valueCell.setCellValue(convertNullToZero(part.getValue()).setScale(5).doubleValue());
                    }
                    first = false;

                } else {
                    partsCounter++;
                    XSSFRow subEventLine = sheet.getRow(partsCounter);
                    XSSFCell machinePartNumberCell = subEventLine.createCell(23);
                    machinePartNumberCell.setCellValue(part.getMachinePartNumber());
                    XSSFCell machinePartNameCell = subEventLine.createCell(24);
                    machinePartNameCell.setCellValue(part.getMachinePartName());
                    XSSFCell machinePartQuantityCell = subEventLine.createCell(25);
                    machinePartQuantityCell.setCellStyle(numberStyle);
                    machinePartQuantityCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    if (part.getMachinePartPlannedQuantity() != null) {
                        machinePartQuantityCell.setCellValue(part.getMachinePartPlannedQuantity().setScale(5).doubleValue());
                    }
                    XSSFCell machinePartUnitCell = subEventLine.createCell(26);
                    machinePartUnitCell.setCellValue(part.getMachinePartUnit());
                    XSSFCell valueCell = subEventLine.createCell(27);
                    valueCell.setCellStyle(numberStyle);
                    valueCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    if (part.getMachinePartPlannedQuantity() != null) {
                        valueCell.setCellValue(convertNullToZero(part.getValue()).setScale(5).doubleValue());
                    }
                }
            }
        }

        fillStateChange(eventLine, event, dateTimeStyle);

        if (rowsToAdd > 1) {
            return rowCounter + rowsToAdd;
        } else {
            return rowCounter + 1;
        }
    }

    private void fillStateChange(XSSFRow eventLine, PlannedEventDTO event, CellStyle dateTimeStyle) {
        List<PlannedEventStateChangeDTO> states = event.getStateChanges();

        XSSFCell createDateCell = eventLine.createCell(28);
        if (event.getCreatedate() != null) {
            createDateCell.setCellValue(event.getCreatedate());
            createDateCell.setCellStyle(dateTimeStyle);
        }
        XSSFCell stateAuthorCell = eventLine.createCell(29);
        stateAuthorCell.setCellValue(event.getCreateuser());

        XSSFCell stateStartDateCell = eventLine.createCell(30);
        stateStartDateCell.setCellStyle(dateTimeStyle);
        Date stateStartDate = getDateForState(PlannedEventStateStringValues.IN_PLAN, states);
        if (stateStartDate != null) {
            stateStartDateCell.setCellValue(stateStartDate);
        }

        XSSFCell stateStartDateWCell = eventLine.createCell(31);
        stateStartDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.IN_PLAN, states));

        XSSFCell stateStopDateCell = eventLine.createCell(32);
        stateStopDateCell.setCellStyle(dateTimeStyle);
        Date stateStopDate = getDateForState(PlannedEventStateStringValues.PLANNED, states);
        if (stateStopDate != null) {
            stateStopDateCell.setCellValue(stateStopDate);
        }

        XSSFCell stateStopDateWCell = eventLine.createCell(33);
        stateStopDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.PLANNED, states));

        XSSFCell stateInRealizationDateCell = eventLine.createCell(34);
        stateInRealizationDateCell.setCellStyle(dateTimeStyle);
        Date stateInRealizationDate = getFirstDateForState(PlannedEventStateStringValues.IN_REALIZATION, states);
        if (stateInRealizationDate != null) {
            stateInRealizationDateCell.setCellValue(stateInRealizationDate);
        }

        XSSFCell stateInRealizationDateWCell = eventLine.createCell(35);
        stateInRealizationDateWCell.setCellValue(getFirstWorkerForState(PlannedEventStateStringValues.IN_REALIZATION, states));

        XSSFCell stateInEditingDateCell = eventLine.createCell(36);
        stateInEditingDateCell.setCellStyle(dateTimeStyle);
        Date stateInEditingDate = getDateForState(PlannedEventStateStringValues.IN_EDITING, states);
        if (stateInEditingDate != null) {
            stateInEditingDateCell.setCellValue(stateInEditingDate);
        }

        XSSFCell stateInEditingDateWCell = eventLine.createCell(37);
        stateInEditingDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.IN_EDITING, states));

        XSSFCell stateAcceptedDateDateCell = eventLine.createCell(38);
        stateAcceptedDateDateCell.setCellStyle(dateTimeStyle);
        Date stateAcceptedDateDate = getDateForState(PlannedEventStateStringValues.ACCEPTED, states);
        if (stateAcceptedDateDate != null) {
            stateAcceptedDateDateCell.setCellValue(stateAcceptedDateDate);
        }

        XSSFCell stateAcceptedDateWCell = eventLine.createCell(39);
        stateAcceptedDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.ACCEPTED, states));

        XSSFCell stateRealizationDateCell = eventLine.createCell(40);
        stateRealizationDateCell.setCellStyle(dateTimeStyle);
        Date stateRealizationDate = getDateForState(PlannedEventStateStringValues.REALIZED, states);
        if (stateRealizationDate != null) {
            stateRealizationDateCell.setCellValue(stateRealizationDate);
        }

        XSSFCell stateRealizationDateWCell = eventLine.createCell(41);
        stateRealizationDateWCell.setCellValue(getWorkerForState(PlannedEventStateStringValues.REALIZED, states));

        XSSFCell stateCell = eventLine.createCell(42);
        stateCell.setCellValue(translationService.translate(event.getState(), LocaleContextHolder.getLocale()));
    }

    private Date getDateForState(final String state, final List<PlannedEventStateChangeDTO> states) {
        Optional<PlannedEventStateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e1.getStateChangeDateAndTime().compareTo(e2.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return op.get().getStateChangeDateAndTime();
        }
        return null;
    }

    private Date getFirstDateForState(final String state, final List<PlannedEventStateChangeDTO> states) {
        Optional<PlannedEventStateChangeDTO> op = states.stream().filter(e -> state.equals(e.getStateChangeTargetState()))
                .sorted((e1, e2) -> e2.getStateChangeDateAndTime().compareTo(e1.getStateChangeDateAndTime())).reduce((a, b) -> b);
        if (op.isPresent()) {
            return op.get().getStateChangeDateAndTime();
        }
        return null;
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

        String timeString = String.format("%d:%02d:%02d", hours, minutes, seconds);

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

    private double convertTimeInternal(String timeStr) {
        int len = timeStr.length();
        String[] parts = TIME_SEPARATOR_PATTERN.split(timeStr);
        String secStr = parts[2];

        String hourStr = parts[0];
        String minStr = parts[1];
        int hours = Integer.parseInt(hourStr);
        int minutes = Integer.parseInt(minStr);
        int seconds = Integer.parseInt(secStr);
        double totalSeconds = (double) (seconds + (minutes + hours * 60) * 60);
        return totalSeconds / 86400.0D;
    }
}
