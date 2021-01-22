package com.qcadoo.mes.costCalculation.print;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costCalculation.constants.CalculationResultFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.print.utils.CostCalculationMaterial;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.xls.XlsDocumentService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CostCalculationXlsService extends XlsDocumentService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private CostCalculationMaterialsService costCalculationMaterialsService;

    @Autowired
    private CostCalculationComponentsService costCalculationComponentsService;

    private static final List<String> CALCULATION_RESULTS_HEADERS = Lists.newArrayList("technologyNumber", "technologyName",
            "productNumber", "quantity", "unit", "materialCosts", "labourCost", "productionCosts", "materialCostMargin",
            "materialCostMarginValue", "labourCostMargin", "labourCostMarginValue", "additionalOverhead", "totalCost",
            "registrationPrice", "registrationPriceOverhead", "registrationPriceOverheadValue", "technicalProductionCost",
            "profit", "profitValue", "sellingPrice", "containsComponents");

    @Override
    protected void addHeader(HSSFSheet sheet, Locale locale, Entity entity) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        HSSFRow headerRow = sheet.createRow(0);
        int columnIndex = 0;
        for (String key : CALCULATION_RESULTS_HEADERS) {
            createHeaderCell(stylesContainer, headerRow,
                    translationService.translate("costCalculation.costCalculation.report.xls.header." + key, locale),
                    columnIndex);
            columnIndex++;
        }
    }

    @Override
    protected void addSeries(HSSFSheet sheet, Entity entity) {
    }

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("costCalculation.costCalculation.report.xls.sheet.calculationResults", locale);
    }

    @Override
    protected void addExtraSheets(final HSSFWorkbook workbook, Entity entity, Locale locale) {
        List<ComponentsCalculationHolder> componentCosts = Lists.newArrayList();
        Map<Long, Boolean> hasComponents = Maps.newHashMap();
        boolean includeComponents = entity.getBooleanField(CostCalculationFields.INCLUDE_COMPONENTS);

        HSSFSheet sheet = workbook.getSheetAt(0);
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        if (includeComponents) {
            for (Entity technology : entity.getHasManyField(CostCalculationFields.TECHNOLOGIES)) {
                List<ComponentsCalculationHolder> technologyComponentCosts = costCalculationComponentsService
                        .getComponentCosts(entity, technology);
                componentCosts.addAll(technologyComponentCosts);
                hasComponents.put(technology.getId(), !technologyComponentCosts.isEmpty());
            }
        }
        createCalculationResultsSheet(sheet, entity, hasComponents, stylesContainer, locale);
        createMaterialCostsSheet(entity,
                createSheet(workbook,
                        translationService.translate("costCalculation.costCalculation.report.xls.sheet.materialCosts", locale)),
                locale);
        createMaterialsBySizeSheet(entity,
                createSheet(workbook,
                        translationService.translate("costCalculation.costCalculation.report.xls.sheet.materialsBySize", locale)),
                locale);
        createLabourCostSheet(entity,
                createSheet(workbook,
                        translationService.translate("costCalculation.costCalculation.report.xls.sheet.labourCost", locale)),
                locale);
        if (includeComponents) {
            createComponentCostsSheet(componentCosts, createSheet(workbook,
                    translationService.translate("costCalculation.costCalculation.report.xls.sheet.componentCosts", locale)),
                    locale);
        }
    }

    private void createCalculationResultsSheet(HSSFSheet sheet, Entity costCalculation, Map<Long, Boolean> hasComponents,
            StylesContainer stylesContainer, Locale locale) {
        int rowIndex = 1;
        for (Entity calculationResult : costCalculation.getHasManyField(CostCalculationFields.CALCULATION_RESULTS)) {
            HSSFRow row = sheet.createRow(rowIndex);
            Entity technology = calculationResult.getBelongsToField(CalculationResultFields.TECHNOLOGY);
            Entity product = calculationResult.getBelongsToField(CalculationResultFields.PRODUCT);
            boolean containsComponents = hasComponents.get(technology.getId()) != null && hasComponents.get(technology.getId());
            createRegularCell(stylesContainer, row, 0, technology.getStringField(TechnologyFields.NUMBER));
            createRegularCell(stylesContainer, row, 1, technology.getStringField(TechnologyFields.NAME));
            createRegularCell(stylesContainer, row, 2, product.getStringField(ProductFields.NUMBER));
            createNumericCell(stylesContainer, row, 3, costCalculation.getDecimalField(CostCalculationFields.QUANTITY));
            createRegularCell(stylesContainer, row, 4, product.getStringField(ProductFields.UNIT));
            createNumericCell(stylesContainer, row, 5, calculationResult.getDecimalField(CalculationResultFields.MATERIAL_COSTS));
            createNumericCell(stylesContainer, row, 6, calculationResult.getDecimalField(CalculationResultFields.LABOUR_COST));
            createNumericCell(stylesContainer, row, 7,
                    calculationResult.getDecimalField(CalculationResultFields.PRODUCTION_COSTS));
            createNumericCell(stylesContainer, row, 8,
                    costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN));
            createNumericCell(stylesContainer, row, 9,
                    calculationResult.getDecimalField(CalculationResultFields.MATERIAL_COST_MARGIN_VALUE));
            createNumericCell(stylesContainer, row, 10,
                    costCalculation.getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN));
            createNumericCell(stylesContainer, row, 11,
                    calculationResult.getDecimalField(CalculationResultFields.LABOUR_COST_MARGIN_VALUE));
            createNumericCell(stylesContainer, row, 12,
                    costCalculation.getDecimalField(CostCalculationFields.ADDITIONAL_OVERHEAD));
            createNumericCell(stylesContainer, row, 13, calculationResult.getDecimalField(CalculationResultFields.TOTAL_COST));
            createNumericCell(stylesContainer, row, 14,
                    calculationResult.getDecimalField(CalculationResultFields.REGISTRATION_PRICE));
            createNumericCell(stylesContainer, row, 15,
                    costCalculation.getDecimalField(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD));
            createNumericCell(stylesContainer, row, 16,
                    calculationResult.getDecimalField(CalculationResultFields.REGISTRATION_PRICE_OVERHEAD_VALUE));
            createNumericCell(stylesContainer, row, 17,
                    calculationResult.getDecimalField(CalculationResultFields.TECHNICAL_PRODUCTION_COST));
            createNumericCell(stylesContainer, row, 18, costCalculation.getDecimalField(CostCalculationFields.PROFIT));
            createNumericCell(stylesContainer, row, 19, calculationResult.getDecimalField(CalculationResultFields.PROFIT_VALUE));
            createNumericCell(stylesContainer, row, 20, calculationResult.getDecimalField(CalculationResultFields.SELLING_PRICE));
            createRegularCell(stylesContainer, row, 21,
                    containsComponents ? translationService.translate("qcadooView.true", locale)
                            : translationService.translate("qcadooView.false", locale));
            rowIndex++;
        }

        for (int i = 0; i < CALCULATION_RESULTS_HEADERS.size(); i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createMaterialCostsSheet(Entity entity, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.materialCosts.technologyNumber", locale), 0);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.materialCosts.productNumber", locale), 1);
        createHeaderCell(stylesContainer, row, translationService.translate(
                "costCalculation.costCalculation.report.xls.sheet.materialCosts.technologyInputProductType", locale), 2);
        createHeaderCell(stylesContainer, row, translationService.translate(
                "costCalculation.costCalculation.report.xls.sheet.materialCosts.differentProductsInDifferentSizes", locale), 3);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.materialCosts.componentNumber", locale), 4);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.materialCosts.componentName", locale), 5);
        createHeaderCell(stylesContainer, row,
                translationService.translate("costCalculation.costCalculation.report.xls.sheet.materialCosts.quantity", locale),
                6);
        createHeaderCell(stylesContainer, row,
                translationService.translate("costCalculation.costCalculation.report.xls.sheet.materialCosts.unit", locale), 7);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.materialCosts.costPerUnit", locale), 8);
        createHeaderCell(stylesContainer, row,
                translationService.translate("costCalculation.costCalculation.report.xls.sheet.materialCosts.cost", locale), 9);

        int rowCounter = 0;
        for (Entity technology : entity.getHasManyField(CostCalculationFields.TECHNOLOGIES)) {
            List<CostCalculationMaterial> sortedMaterials = costCalculationMaterialsService
                    .getSortedMaterialsFromProductQuantities(entity, technology);

            for (CostCalculationMaterial materialCost : sortedMaterials) {
                row = sheet.createRow(rowOffset + rowCounter);
                createRegularCell(stylesContainer, row, 0, technology.getStringField(TechnologyFields.NUMBER));
                createRegularCell(stylesContainer, row, 1,
                        technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(ProductFields.NUMBER));
                createRegularCell(stylesContainer, row, 2, "");
                createRegularCell(stylesContainer, row, 3, "");
                createRegularCell(stylesContainer, row, 4, materialCost.getProductNumber());
                createRegularCell(stylesContainer, row, 5, materialCost.getProductName());
                createNumericCell(stylesContainer, row, 6, materialCost.getProductQuantity());
                createRegularCell(stylesContainer, row, 7, materialCost.getUnit());
                createNumericCell(stylesContainer, row, 8, materialCost.getCostPerUnit());
                createNumericCell(stylesContainer, row, 9, materialCost.getCostForGivenQuantity());
                rowCounter++;
            }
        }
        for (int i = 0; i <= 9; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createMaterialsBySizeSheet(Entity entity, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.materialsBySize.technologyNumber", locale), 0);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.materialsBySize.productNumber", locale), 1);

        int rowCounter = 0;
        for (Entity technology : entity.getHasManyField(CostCalculationFields.TECHNOLOGIES)) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, technology.getStringField(TechnologyFields.NUMBER));
            createRegularCell(stylesContainer, row, 1,
                    technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(ProductFields.NUMBER));
            rowCounter++;
        }
        for (int i = 0; i <= 1; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createLabourCostSheet(Entity entity, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.labourCost.technologyNumber", locale), 0);
        createHeaderCell(stylesContainer, row,
                translationService.translate("costCalculation.costCalculation.report.xls.sheet.labourCost.productNumber", locale),
                1);

        int rowCounter = 0;
        for (Entity technology : entity.getHasManyField(CostCalculationFields.TECHNOLOGIES)) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, technology.getStringField(TechnologyFields.NUMBER));
            createRegularCell(stylesContainer, row, 1,
                    technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(ProductFields.NUMBER));
            rowCounter++;
        }
        for (int i = 0; i <= 1; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createComponentCostsSheet(List<ComponentsCalculationHolder> componentCosts, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.technologyNumber", locale), 0);
        createHeaderCell(stylesContainer, row, translationService.translate(
                "costCalculation.costCalculation.report.xls.sheet.componentCosts.technologyInputProductType", locale), 1);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.componentNumber", locale), 2);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.componentName", locale), 3);
        createHeaderCell(stylesContainer, row,
                translationService.translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.quantity", locale),
                4);
        createHeaderCell(stylesContainer, row,
                translationService.translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.unit", locale), 5);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.materialCost", locale), 6);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.labourCost", locale), 7);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.sumOfCosts", locale), 8);
        createHeaderCell(stylesContainer, row, translationService
                .translate("costCalculation.costCalculation.report.xls.sheet.componentCosts.costPerUnit", locale), 9);

        int rowCounter = 0;

        for (ComponentsCalculationHolder componentCost : componentCosts) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, componentCost.getToc()
                    .getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY).getStringField(TechnologyFields.NUMBER));
            createRegularCell(stylesContainer, row, 1, componentCost.getTechnologyInputProductType());
            createRegularCell(stylesContainer, row, 2, componentCost.getProduct().getStringField(ProductFields.NUMBER));
            createRegularCell(stylesContainer, row, 3, componentCost.getProduct().getStringField(ProductFields.NAME));
            createNumericCell(stylesContainer, row, 4, componentCost.getQuantity());
            createRegularCell(stylesContainer, row, 5, componentCost.getProduct().getStringField(ProductFields.UNIT));
            createNumericCell(stylesContainer, row, 6, componentCost.getMaterialCost());
            createNumericCell(stylesContainer, row, 7, componentCost.getLaborCost());
            createNumericCell(stylesContainer, row, 8, componentCost.getSumOfCost());
            createNumericCell(stylesContainer, row, 9, componentCost.getCostPerUnit());
            rowCounter++;
        }

        for (int i = 0; i <= 9; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private HSSFCell createRegularCell(StylesContainer stylesContainer, HSSFRow row, int column, String content) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue(content);
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.regularStyle, HSSFCellStyle.ALIGN_LEFT));
        return cell;
    }

    private HSSFCell createNumericCell(StylesContainer stylesContainer, HSSFRow row, int column, BigDecimal value) {
        HSSFCell cell = row.createCell(column, HSSFCell.CELL_TYPE_NUMERIC);
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        cell.setCellValue(numberService.setScaleWithDefaultMathContext(value, 2).doubleValue());
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.numberStyle, HSSFCellStyle.ALIGN_RIGHT));
        return cell;
    }

    private HSSFCell createHeaderCell(StylesContainer stylesContainer, HSSFRow row, String content, int column) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue(content);
        cell.setCellStyle(stylesContainer.headerStyle);
        return cell;
    }

    private static class StylesContainer {

        private final HSSFCellStyle regularStyle;

        private final HSSFCellStyle headerStyle;

        private final HSSFCellStyle numberStyle;

        StylesContainer(HSSFWorkbook workbook, FontsContainer fontsContainer) {
            regularStyle = workbook.createCellStyle();
            regularStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

            headerStyle = workbook.createCellStyle();
            headerStyle.setFont(fontsContainer.boldFont);
            headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);
            headerStyle.setWrapText(true);

            numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00###"));
        }

        private static HSSFCellStyle aligned(HSSFCellStyle style, short align) {
            style.setAlignment(align);
            return style;
        }
    }

    private static class FontsContainer {

        private final Font boldFont;

        FontsContainer(HSSFWorkbook workbook) {
            boldFont = workbook.createFont();
            boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        }
    }

}