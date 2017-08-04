package com.qcadoo.mes.productionCounting.xls;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.xls.dto.ProducedQuantities;
import com.qcadoo.mes.productionCounting.xls.dto.ProductionCost;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;

@Service
public class ProductionBalanceXlsService extends XlsDocumentService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ProductionBalanceRepository productionBalanceRepository;

    @Override
    protected void addHeader(HSSFSheet sheet, Locale locale, Entity entity) {

    }

    @Override
    protected void addSeries(HSSFSheet sheet, Entity entity) {
        List<Long> ordersIds = getOrdersIds(entity);

        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        createProducedQuantitiesSheet(sheet, ordersIds, stylesContainer);
    }

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("productionCounting.productionBalance.report.xls.sheet.producedQuantities", locale);
    }

    @Override
    protected void addExtraSheets(final HSSFWorkbook workbook, Entity entity) {
        List<Long> ordersIds = getOrdersIds(entity);
        createProductionCostsSheet(createSheet(workbook, "..."));
    }

    private List<Long> getOrdersIds(final Entity productionBalance) {

        List<Entity> orders = productionBalance.getHasManyField(ProductionBalanceFields.ORDERS);
        return orders.stream().map(Entity::getId).collect(Collectors.toList());
    }

    private void createProducedQuantitiesSheet(HSSFSheet sheet, List<Long> ordersIds, StylesContainer stylesContainer) {
        List<ProducedQuantities> producedQuantities = productionBalanceRepository.getProducedQuantities(ordersIds);
    }

    private void createProductionCostsSheet(HSSFSheet sheet) {
        final int rowOffset = 1;
        List<ProductionCost> productionCosts = productionBalanceRepository.getCumulatedProductionCosts();
        int rowCounter = 0;
        for (ProductionCost productionCost : productionCosts) {
            HSSFRow row = sheet.createRow(rowOffset + rowCounter);
            row.createCell(0).setCellValue(productionCost.getOrderNumber());
            rowCounter++;
        }
    }

    private HSSFCell createRegularCell(StylesContainer stylesContainer, HSSFRow row, int column, String content) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue(content);
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.regularStyle, HSSFCellStyle.ALIGN_LEFT));
        return cell;
    }

    private HSSFCell createNumericCell(StylesContainer stylesContainer, HSSFRow row, int column, int value) {
        HSSFCell cell = row.createCell(column, HSSFCell.CELL_TYPE_NUMERIC);
        cell.setCellValue(value);
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.regularStyle, HSSFCellStyle.ALIGN_LEFT));
        return cell;
    }

    private HSSFCell createHeaderCell(StylesContainer stylesContainer, HSSFRow row, String content, int column, short align) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue(content);
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.headerStyle, align));
        return cell;
    }

    private static class StylesContainer {

        private final HSSFCellStyle regularStyle;

        private final HSSFCellStyle headerStyle;

        StylesContainer(HSSFWorkbook workbook, FontsContainer fontsContainer) {
            regularStyle = workbook.createCellStyle();
            regularStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

            headerStyle = workbook.createCellStyle();
            headerStyle.setFont(fontsContainer.headerFont);
        }

        private static HSSFCellStyle aligned(HSSFCellStyle style, short align) {
            style.setAlignment(align);
            return style;
        }

    }

    private static class FontsContainer {

        private final Font headerFont;

        FontsContainer(HSSFWorkbook workbook) {

            headerFont = workbook.createFont();
            headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        }
    }
}
