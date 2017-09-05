package com.qcadoo.mes.productionCounting.xls;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.xls.dto.LaborTimeDetails;
import com.qcadoo.mes.productionCounting.xls.dto.MaterialCost;
import com.qcadoo.mes.productionCounting.xls.dto.OrderBalance;
import com.qcadoo.mes.productionCounting.xls.dto.PieceworkDetails;
import com.qcadoo.mes.productionCounting.xls.dto.ProducedQuantity;
import com.qcadoo.mes.productionCounting.xls.dto.ProductionCost;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.xls.XlsDocumentService;

@Service
public class ProductionBalanceXlsService extends XlsDocumentService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionBalanceRepository productionBalanceRepository;

    private static final List<String> PRODUCTION_QUANTITIES_HEADERS = Lists.newArrayList("orderNumber", "productNumber",
            "productName", "plannedQuantity", "producedQuantity", "wastesQuantity", "producedWastes", "deviation", "productUnit");

    @Override
    protected void addHeader(HSSFSheet sheet, Locale locale, Entity entity) {

        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        HSSFRow headerRow = sheet.createRow(0);
        int columnIndex = 0;
        for (String key : PRODUCTION_QUANTITIES_HEADERS) {
            createHeaderCell(stylesContainer, headerRow,
                    translationService.translate("productionCounting.productionBalance.report.xls.header." + key, locale),
                    columnIndex, HSSFCellStyle.ALIGN_LEFT);
            columnIndex++;
        }
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
    protected void addExtraSheets(final HSSFWorkbook workbook, Entity entity, Locale locale) {
        List<Long> ordersIds = getOrdersIds(entity);
        List<MaterialCost> materialCosts = productionBalanceRepository.getMaterialCosts(entity, ordersIds);
        createMaterialCostsSheet(materialCosts, createSheet(workbook,
                translationService.translate("productionCounting.productionBalance.report.xls.sheet.materialCosts", locale)),
                locale);
        createLaborTimeSheet(createSheet(workbook, translationService.translate(LaborTimeSheetConstants.SHEET_TITLE, locale)),
                ordersIds, locale);
        createPieceworkSheet(createSheet(workbook, translationService.translate(PieceworkSheetConstants.SHEET_TITLE, locale)),
                ordersIds, locale);
        List<ProductionCost> productionCosts = productionBalanceRepository.getProductionCosts(entity, ordersIds);
        createProductionCostsSheet(productionCosts, createSheet(workbook,
                translationService.translate("productionCounting.productionBalance.report.xls.sheet.productionCosts", locale)),
                locale);
        List<OrderBalance> ordersBalance = productionBalanceRepository.getOrdersBalance(entity, ordersIds, materialCosts,
                productionCosts);
        createOrdersBalanceSheet(ordersBalance, createSheet(workbook,
                translationService.translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance", locale)),
                locale);
        createComponentsBalanceSheet(entity, ordersBalance,
                createSheet(workbook, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.componentsBalance", locale)),
                ordersIds, locale);
    }

    private List<Long> getOrdersIds(final Entity productionBalance) {

        List<Entity> orders = productionBalance.getHasManyField(ProductionBalanceFields.ORDERS);
        return orders.stream().map(Entity::getId).collect(Collectors.toList());
    }

    private void createProducedQuantitiesSheet(HSSFSheet sheet, List<Long> ordersIds, StylesContainer stylesContainer) {
        List<ProducedQuantity> producedQuantities = productionBalanceRepository.getProducedQuantities(ordersIds);
        int rowIndex = 1;
        for (ProducedQuantity producedQuantity : producedQuantities) {
            HSSFRow row = sheet.createRow(rowIndex);
            createRegularCell(stylesContainer, row, 0, producedQuantity.getOrderNumber());
            createRegularCell(stylesContainer, row, 1, producedQuantity.getProductNumber());
            createRegularCell(stylesContainer, row, 2, producedQuantity.getProductName());
            createNumericCell(stylesContainer, row, 3, producedQuantity.getPlannedQuantity());
            createNumericCell(stylesContainer, row, 4, producedQuantity.getProducedQuantity());
            createNumericCell(stylesContainer, row, 5, producedQuantity.getWastesQuantity());
            createNumericCell(stylesContainer, row, 6, producedQuantity.getProducedWastes());
            createNumericCell(stylesContainer, row, 7, producedQuantity.getDeviation());
            createRegularCell(stylesContainer, row, 8, producedQuantity.getProductUnit());
            rowIndex++;
        }

        for (int i = 0; i < PRODUCTION_QUANTITIES_HEADERS.size(); i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createMaterialCostsSheet(List<MaterialCost> materialCosts, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.orderNumber", locale),
                0, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.operationNumber", locale),
                1, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.productNumber", locale),
                2, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.productName", locale),
                3, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.plannedQuantity", locale),
                4, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.usedQuantity", locale),
                5, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.materialCosts.quantitativeDeviation", locale),
                6, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.unit", locale),
                7, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.plannedCost", locale),
                8, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.realCost", locale),
                9, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.valueDeviation", locale),
                10, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.materialCosts.usedWasteQuantity", locale),
                11, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate("productionCounting.productionBalance.report.xls.sheet.materialCosts.unit", locale),
                12, CellStyle.ALIGN_LEFT);

        int rowCounter = 0;
        for (MaterialCost materialCost : materialCosts) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, materialCost.getOrderNumber());
            createRegularCell(stylesContainer, row, 1, materialCost.getOperationNumber());
            createRegularCell(stylesContainer, row, 2, materialCost.getProductNumber());
            createRegularCell(stylesContainer, row, 3, materialCost.getProductName());
            createNumericCell(stylesContainer, row, 4, materialCost.getPlannedQuantity());
            createNumericCell(stylesContainer, row, 5, materialCost.getUsedQuantity());
            createNumericCell(stylesContainer, row, 6, materialCost.getQuantitativeDeviation());
            createRegularCell(stylesContainer, row, 7, materialCost.getProductUnit());
            createNumericCell(stylesContainer, row, 8, materialCost.getPlannedCost());
            createNumericCell(stylesContainer, row, 9, materialCost.getRealCost());
            createNumericCell(stylesContainer, row, 10, materialCost.getValueDeviation());
            createNumericCell(stylesContainer, row, 11, materialCost.getUsedWasteQuantity());
            createRegularCell(stylesContainer, row, 12, materialCost.getUsedWasteUnit());
            rowCounter++;
        }
        for (int i = 0; i <= 12; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createPieceworkSheet(HSSFSheet sheet, List<Long> ordersIds, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer, row, translationService.translate(PieceworkSheetConstants.ORDER_NUMBER, locale), 0,
                CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row, translationService.translate(PieceworkSheetConstants.OPERATION_NUMBER, locale), 1,
                CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(PieceworkSheetConstants.TOTAL_EXECUTED_OPERATION_CYCLES, locale), 2,
                CellStyle.ALIGN_LEFT);

        List<PieceworkDetails> pieceworkDetailsList = productionBalanceRepository.getPieceworkDetails(ordersIds);
        int rowCounter = 0;
        for (PieceworkDetails pieceworkDetails : pieceworkDetailsList) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, pieceworkDetails.getOrderNumber());
            createRegularCell(stylesContainer, row, 1, pieceworkDetails.getOperationNumber());
            createNumericCell(stylesContainer, row, 2, pieceworkDetails.getTotalExecutedOperationCycles());
            rowCounter++;
        }
        for (int i = 0; i <= 2; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createLaborTimeSheet(HSSFSheet sheet, List<Long> ordersIds, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer, row, translationService.translate(LaborTimeSheetConstants.ORDER_NUMBER, locale), 0,
                CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row, translationService.translate(LaborTimeSheetConstants.OPERATION_NUMBER, locale), 1,
                CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row, translationService.translate(LaborTimeSheetConstants.STAFF_NUMBER, locale), 2,
                CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row, translationService.translate(LaborTimeSheetConstants.STAFF_NAME, locale), 3,
                CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row, translationService.translate(LaborTimeSheetConstants.STAFF_SURNAME, locale), 4,
                CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row, translationService.translate(LaborTimeSheetConstants.LABOR_TIME, locale), 5,
                CellStyle.ALIGN_LEFT);

        List<LaborTimeDetails> laborTimeDetailsList = productionBalanceRepository.getLaborTimeDetails(ordersIds);
        int rowCounter = 0;
        for (LaborTimeDetails laborTimeDetails : laborTimeDetailsList) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, laborTimeDetails.getOrderNumber());
            createRegularCell(stylesContainer, row, 1, laborTimeDetails.getOperationNumber());
            createRegularCell(stylesContainer, row, 2, laborTimeDetails.getStaffNumber());
            createRegularCell(stylesContainer, row, 3, laborTimeDetails.getStaffName());
            createRegularCell(stylesContainer, row, 4, laborTimeDetails.getStaffSurname());
            createTimeCell(stylesContainer, row, 5, laborTimeDetails.getLaborTime());
            rowCounter++;
        }
        for (int i = 0; i <= 5; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createProductionCostsSheet(List<ProductionCost> productionCosts, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.productionCosts.orderNumber", locale),
                0, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.operationNumber", locale),
                1, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.plannedStaffTime", locale),
                2, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.productionCosts.realStaffTime", locale),
                3, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.plannedMachineTime", locale),
                4, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.realMachineTime", locale),
                5, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.plannedStaffCosts", locale),
                6, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.realStaffCosts", locale),
                7, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.staffCostsDeviation", locale),
                8, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.plannedMachineCosts", locale),
                9, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.realMachineCosts", locale),
                10, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.machineCostsDeviation", locale),
                11, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.plannedPieceworkCosts", locale),
                12, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.realPieceworkCosts", locale),
                13, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.plannedCostsSum", locale),
                14, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.productionCosts.realCostsSum", locale),
                15, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.productionCosts.sumCostsDeviation", locale),
                16, CellStyle.ALIGN_LEFT);

        int rowCounter = 0;
        for (ProductionCost productionCost : productionCosts) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, productionCost.getOrderNumber());
            createRegularCell(stylesContainer, row, 1, productionCost.getOperationNumber());
            createTimeCell(stylesContainer, row, 2, productionCost.getPlannedStaffTime());
            createTimeCell(stylesContainer, row, 3, productionCost.getRealStaffTime());
            createTimeCell(stylesContainer, row, 4, productionCost.getPlannedMachineTime());
            createTimeCell(stylesContainer, row, 5, productionCost.getRealMachineTime());
            createNumericCell(stylesContainer, row, 6, productionCost.getPlannedStaffCosts());
            createNumericCell(stylesContainer, row, 7, productionCost.getRealStaffCosts());
            createNumericCell(stylesContainer, row, 8, productionCost.getStaffCostsDeviation());
            createNumericCell(stylesContainer, row, 9, productionCost.getPlannedMachineCosts());
            createNumericCell(stylesContainer, row, 10, productionCost.getRealMachineCosts());
            createNumericCell(stylesContainer, row, 11, productionCost.getMachineCostsDeviation());
            createNumericCell(stylesContainer, row, 12, productionCost.getPlannedPieceworkCosts());
            createNumericCell(stylesContainer, row, 13, productionCost.getRealPieceworkCosts());
            createNumericCell(stylesContainer, row, 14, productionCost.getPlannedCostsSum());
            createNumericCell(stylesContainer, row, 15, productionCost.getRealCostsSum());
            createNumericCell(stylesContainer, row, 16, productionCost.getSumCostsDeviation());
            rowCounter++;
        }
        for (int i = 0; i <= 16; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createOrdersBalanceSheet(List<OrderBalance> ordersBalance, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.orderNumber", locale),
                0, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productNumber", locale),
                1, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productName", locale),
                2, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.producedQuantity", locale),
                3, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCosts", locale),
                4, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCosts", locale),
                5, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.technicalProductionCosts", locale),
                6, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCostMargin", locale),
                7, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCostMarginValue", locale),
                8, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCostMargin", locale),
                9, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCostMarginValue", locale),
                10, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.additionalOverhead", locale),
                11, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.directAdditionalCost", locale),
                12, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.totalCosts", locale),
                13, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPrice", locale),
                14, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPriceOverhead", locale),
                15, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPriceOverheadValue",
                        locale),
                16, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.realProductionCosts", locale),
                17, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.profit", locale),
                18, CellStyle.ALIGN_LEFT);

        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.profitValue", locale),
                19, CellStyle.ALIGN_LEFT);

        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.sellPrice", locale),
                20, CellStyle.ALIGN_LEFT);

        int rowCounter = 0;
        for (OrderBalance orderBalance : ordersBalance) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, orderBalance.getOrderNumber());
            createRegularCell(stylesContainer, row, 1, orderBalance.getProductNumber());
            createRegularCell(stylesContainer, row, 2, orderBalance.getProductName());
            createNumericCell(stylesContainer, row, 3, orderBalance.getProducedQuantity());
            createNumericCell(stylesContainer, row, 4, orderBalance.getMaterialCosts());
            createNumericCell(stylesContainer, row, 5, orderBalance.getProductionCosts());
            createNumericCell(stylesContainer, row, 6, orderBalance.getTechnicalProductionCosts());
            createNumericCell(stylesContainer, row, 7, orderBalance.getMaterialCostMargin());
            createNumericCell(stylesContainer, row, 8, orderBalance.getMaterialCostMarginValue());
            createNumericCell(stylesContainer, row, 9, orderBalance.getProductionCostMargin());
            createNumericCell(stylesContainer, row, 10, orderBalance.getProductionCostMarginValue());
            createNumericCell(stylesContainer, row, 11, orderBalance.getAdditionalOverhead());
            createNumericCell(stylesContainer, row, 12, orderBalance.getDirectAdditionalCost());
            createNumericCell(stylesContainer, row, 13, orderBalance.getTotalCosts());
            createNumericCell(stylesContainer, row, 14, orderBalance.getRegistrationPrice());
            createNumericCell(stylesContainer, row, 15, orderBalance.getRegistrationPriceOverhead());
            createNumericCell(stylesContainer, row, 16, orderBalance.getRegistrationPriceOverheadValue());
            createNumericCell(stylesContainer, row, 17, orderBalance.getRealProductionCosts());
            createNumericCell(stylesContainer, row, 18, orderBalance.getProfit());
            createNumericCell(stylesContainer, row, 19, orderBalance.getProfitValue());
            createNumericCell(stylesContainer, row, 20, orderBalance.getSellPrice());
            rowCounter++;
        }
        for (int i = 0; i <= 20; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createComponentsBalanceSheet(Entity entity, List<OrderBalance> ordersBalance, HSSFSheet sheet,
            List<Long> ordersIds, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.orderNumber", locale),
                0, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productNumber", locale),
                1, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productName", locale),
                2, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.producedQuantity", locale),
                3, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCosts", locale),
                4, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCosts", locale),
                5, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.technicalProductionCosts", locale),
                6, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCostMargin", locale),
                7, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCostMarginValue", locale),
                8, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCostMargin", locale),
                9, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCostMarginValue", locale),
                10, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.additionalOverhead", locale),
                11, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.directAdditionalCost", locale),
                12, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.totalCosts", locale),
                13, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPrice", locale),
                14, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPriceOverhead", locale),
                15, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPriceOverheadValue",
                        locale),
                16, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.realProductionCosts", locale),
                17, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.profit", locale),
                18, CellStyle.ALIGN_LEFT);

        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.profitValue", locale),
                19, CellStyle.ALIGN_LEFT);

        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.sellPrice", locale),
                20, CellStyle.ALIGN_LEFT);

        List<OrderBalance> componentsBalance = productionBalanceRepository.getComponentsBalance(entity, ordersIds, ordersBalance);
        int rowCounter = 0;
        for (OrderBalance orderBalance : componentsBalance) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, orderBalance.getOrderNumber());
            createRegularCell(stylesContainer, row, 1, orderBalance.getProductNumber());
            createRegularCell(stylesContainer, row, 2, orderBalance.getProductName());
            createNumericCell(stylesContainer, row, 3, orderBalance.getProducedQuantity());
            createNumericCell(stylesContainer, row, 4, orderBalance.getMaterialCosts());
            createNumericCell(stylesContainer, row, 5, orderBalance.getProductionCosts());
            createNumericCell(stylesContainer, row, 6, orderBalance.getTechnicalProductionCosts());
            createNumericCell(stylesContainer, row, 7, orderBalance.getMaterialCostMargin());
            createNumericCell(stylesContainer, row, 8, orderBalance.getMaterialCostMarginValue());
            createNumericCell(stylesContainer, row, 9, orderBalance.getProductionCostMargin());
            createNumericCell(stylesContainer, row, 10, orderBalance.getProductionCostMarginValue());
            createNumericCell(stylesContainer, row, 11, orderBalance.getAdditionalOverhead());
            createNumericCell(stylesContainer, row, 12, orderBalance.getDirectAdditionalCost());
            createNumericCell(stylesContainer, row, 13, orderBalance.getTotalCosts());
            createNumericCell(stylesContainer, row, 14, orderBalance.getRegistrationPrice());
            createNumericCell(stylesContainer, row, 15, orderBalance.getRegistrationPriceOverhead());
            createNumericCell(stylesContainer, row, 16, orderBalance.getRegistrationPriceOverheadValue());
            createNumericCell(stylesContainer, row, 17, orderBalance.getRealProductionCosts());
            createNumericCell(stylesContainer, row, 18, orderBalance.getProfit());
            createNumericCell(stylesContainer, row, 19, orderBalance.getProfitValue());
            createNumericCell(stylesContainer, row, 20, orderBalance.getSellPrice());
            rowCounter++;
        }
        for (int i = 0; i <= 20; i++) {
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
        cell.setCellValue(numberService.setScale(value, 2).doubleValue());
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.numberStyle, HSSFCellStyle.ALIGN_RIGHT));
        return cell;
    }

    private HSSFCell createTimeCell(StylesContainer stylesContainer, HSSFRow row, int column, Integer value) {
        HSSFCell cell = row.createCell(column, HSSFCell.CELL_TYPE_NUMERIC);
        cell.setCellValue(value == null ? 0d : value / 86400d);
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.timeStyle, HSSFCellStyle.ALIGN_RIGHT));
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

        private final HSSFCellStyle timeStyle;

        private final HSSFCellStyle numberStyle;

        StylesContainer(HSSFWorkbook workbook, FontsContainer fontsContainer) {
            regularStyle = workbook.createCellStyle();
            regularStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

            headerStyle = workbook.createCellStyle();
            headerStyle.setFont(fontsContainer.headerFont);
            headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);
            headerStyle.setWrapText(true);

            timeStyle = workbook.createCellStyle();
            timeStyle.setDataFormat(workbook.createDataFormat().getFormat("[HH]:MM:SS"));

            numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00###"));
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
