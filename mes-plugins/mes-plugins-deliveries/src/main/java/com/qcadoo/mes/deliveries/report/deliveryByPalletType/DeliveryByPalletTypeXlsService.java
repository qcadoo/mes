package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.model.constants.QcadooModelConstants;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
class DeliveryByPalletTypeXlsService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DeliveryByPalletTypeXlsDP dataProvider;

    public String getReportTitle(final Locale locale) {
        return translationService.translate(DeliveryByPalletTypeXlsConstants.REPORT_TITLE, locale);
    }

    public void buildExcelContent(final XSSFWorkbook workbook, final XSSFSheet sheet, final Map<String, Object> filters,
            final Locale locale) {
        List<Entity> pallets = getTypeOfPallets();
        fillHeaderRow(workbook, sheet, 0, locale, pallets);
        Map<DeliveryByPalletTypeKey, DeliveryByPalletTypeValue> deliveryByPalletType = dataProvider.findEntries(filters);
        fillRows(workbook, sheet, 1, locale, deliveryByPalletType, pallets);
        autoSizeColumn(sheet);
    }

    private void autoSizeColumn(XSSFSheet sheet) {
        Row row = sheet.getRow(0);
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            int columnIndex = cell.getColumnIndex();
            sheet.autoSizeColumn(columnIndex);
        }
    }

    private void fillRows(final XSSFWorkbook workbook, final XSSFSheet sheet, int rowNum, final Locale locale,
            Map<DeliveryByPalletTypeKey, DeliveryByPalletTypeValue> deliveryByPalletType, final List<Entity> pallets) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        DataFormat dataFormat = workbook.createDataFormat();

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(dataFormat.getFormat("dd.mm.yyyy"));

        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(dataFormat.getFormat("# ##0"));

        for (Map.Entry<DeliveryByPalletTypeKey, DeliveryByPalletTypeValue> entry : deliveryByPalletType.entrySet()) {
            DeliveryByPalletTypeKey key = entry.getKey();
            DeliveryByPalletTypeValue value = entry.getValue();
            XSSFRow rowLine = sheet.createRow(rowNum);

            XSSFCell dateCell = rowLine.createCell(0);
            dateCell.setCellValue(key.getDate());
            dateCell.getCellStyle().setFont(font);
            dateCell.setCellStyle(dateStyle);

            XSSFCell numberCell = rowLine.createCell(1);
            numberCell.setCellValue(key.getNumber());

            XSSFCell sumAllCell = rowLine.createCell(2);
            sumAllCell.setCellStyle(numberStyle);
            sumAllCell.setCellType(Cell.CELL_TYPE_NUMERIC);
            sumAllCell.setCellValue(value.sum());

            int number = 3;
            for (Entity pallet : pallets) {
                XSSFCell quantity = rowLine.createCell(number);
                quantity.setCellStyle(numberStyle);
                quantity.setCellType(Cell.CELL_TYPE_NUMERIC);
                quantity.setCellValue(nullToZero(value.getPalletQuantity().get(pallet.getStringField(DictionaryItemFields.NAME))));
                number++;
            }
            rowNum++;
        }
    }

    private void fillHeaderRow(final XSSFWorkbook workbook, final XSSFSheet sheet, final int rowNum, final Locale locale,
            final List<Entity> pallets) {
        XSSFRow headerLine = sheet.createRow(rowNum);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("ARIAL");
        font.setItalic(false);
        font.setBold(true);
        font.setBold(true);

        font.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle style = workbook.createCellStyle();

        style.setFont(font);

        XSSFCell dateCell = headerLine.createCell(0);
        dateCell.setCellValue(translationService.translate("deliveries.deliveryByPalletTypeReport.report.date", locale));
        dateCell.setCellStyle(style);
        XSSFCell numberCell = headerLine.createCell(1);
        numberCell.setCellValue(translationService.translate("deliveries.deliveryByPalletTypeReport.report.number", locale));
        numberCell.setCellStyle(style);

        XSSFCell sumOfPalletCell = headerLine.createCell(2);
        sumOfPalletCell.setCellValue(translationService.translate("deliveries.deliveryByPalletTypeReport.report.sumAllPallets",
                locale));
        sumOfPalletCell.setCellStyle(style);

        int number = 3;
        for (Entity pallet : pallets) {
            XSSFCell palletCell = headerLine.createCell(number);
            palletCell.setCellValue(translationService
                    .translate("deliveries.deliveryByPalletTypeReport.report.pallet", locale, pallet.getStringField(DictionaryItemFields.NAME)));
            palletCell.setCellStyle(style);
            number++;
        }

    }

    private List<Entity> getTypeOfPallets() {
        Entity typeOfPalletDictionary = dataDefinitionService
                .get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY).find()
                .add(SearchRestrictions.eq(DictionaryFields.NAME, "typeOfPallet")).uniqueResult();

        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM)
                .find().add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, typeOfPalletDictionary))
                .add(SearchRestrictions.eq("active", true)).list().getEntities();
    }

    private Integer nullToZero(Integer val) {
        if(Objects.isNull(val)){
            return 0;
        }
        return val;
    }

}
