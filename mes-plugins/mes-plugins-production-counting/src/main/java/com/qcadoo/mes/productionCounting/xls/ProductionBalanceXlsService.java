package com.qcadoo.mes.productionCounting.xls;

import java.math.BigDecimal;
import java.util.Date;
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
import com.qcadoo.mes.productionCounting.xls.dto.LaborTime;
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
        List<LaborTimeDetails> laborTimeDetailsList = productionBalanceRepository.getLaborTimeDetails(entity, ordersIds);
        createLaborTimeDetailsSheet(laborTimeDetailsList, createSheet(workbook,
                translationService.translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails", locale)),
                locale);
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
        List<OrderBalance> componentsBalance = productionBalanceRepository.getComponentsBalance(entity, ordersIds, ordersBalance);
        createOrdersBalanceSheet(componentsBalance, createSheet(workbook,
                translationService.translate("productionCounting.productionBalance.report.xls.sheet.componentsBalance", locale)),
                locale);
        List<OrderBalance> productsBalance = productionBalanceRepository.getProductsBalance(entity, ordersIds, componentsBalance);
        createProductsBalanceSheet(productsBalance, createSheet(workbook,
                translationService.translate("productionCounting.productionBalance.report.xls.sheet.productsBalance", locale)),
                locale);
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

        List<LaborTime> laborTimeList = productionBalanceRepository.getLaborTime(ordersIds);
        int rowCounter = 0;
        for (LaborTime laborTime : laborTimeList) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, laborTime.getOrderNumber());
            createRegularCell(stylesContainer, row, 1, laborTime.getOperationNumber());
            createRegularCell(stylesContainer, row, 2, laborTime.getStaffNumber());
            createRegularCell(stylesContainer, row, 3, laborTime.getStaffName());
            createRegularCell(stylesContainer, row, 4, laborTime.getStaffSurname());
            createTimeCell(stylesContainer, row, 5, laborTime.getLaborTime());
            rowCounter++;
        }
        for (int i = 0; i <= 5; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private void createLaborTimeDetailsSheet(List<LaborTimeDetails> laborTimeDetailsList, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.divisionNumber", locale),
                0, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.productionLineNumber", locale),
                1, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.orderNumber", locale),
                2, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.orderState", locale),
                3, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.plannedDateFrom", locale),
                4, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.effectiveDateFrom", locale),
                5, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.plannedDateTo", locale),
                6, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.effectiveDateTo", locale),
                7, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.productNumber", locale),
                8, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.orderName", locale),
                9, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.plannedQuantity", locale),
                10, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.amountOfProductProduced", locale),
                11, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.staffNumber", locale),
                12, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.staffName", locale),
                13, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.staffSurname", locale),
                14, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.operationNumber", locale),
                15, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.timeRangeFrom", locale),
                16, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.timeRangeTo", locale),
                17, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.shiftName", locale),
                18, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.createDate", locale),
                19, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.laborTime", locale),
                20, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.plannedLaborTime", locale),
                21, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.laborTimeDeviation", locale),
                22, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.machineTime", locale),
                23, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.plannedMachineTime", locale),
                24, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.laborTimeDetails.machineTimeDeviation", locale),
                25, CellStyle.ALIGN_LEFT);

        int rowCounter = 0;
        for (LaborTimeDetails laborTimeDetails : laborTimeDetailsList) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, laborTimeDetails.getDivisionNumber());
            createRegularCell(stylesContainer, row, 1, laborTimeDetails.getProductionLineNumber());
            createRegularCell(stylesContainer, row, 2, laborTimeDetails.getOrderNumber());
            createRegularCell(stylesContainer, row, 3, translationService.translate(
                    "orders.order.state.value." + laborTimeDetails.getOrderState(), locale));
            createDateTimeCell(stylesContainer, row, 4, laborTimeDetails.getPlannedDateFrom());
            createDateTimeCell(stylesContainer, row, 5, laborTimeDetails.getEffectiveDateFrom());
            createDateTimeCell(stylesContainer, row, 6, laborTimeDetails.getPlannedDateTo());
            createDateTimeCell(stylesContainer, row, 7, laborTimeDetails.getEffectiveDateTo());
            createRegularCell(stylesContainer, row, 8, laborTimeDetails.getProductNumber());
            createRegularCell(stylesContainer, row, 9, laborTimeDetails.getOrderName());
            createNumericCell(stylesContainer, row, 10, laborTimeDetails.getPlannedQuantity());
            createNumericCell(stylesContainer, row, 11, laborTimeDetails.getAmountOfProductProduced());
            createRegularCell(stylesContainer, row, 12, laborTimeDetails.getStaffNumber());
            createRegularCell(stylesContainer, row, 13, laborTimeDetails.getStaffName());
            createRegularCell(stylesContainer, row, 14, laborTimeDetails.getStaffSurname());
            createRegularCell(stylesContainer, row, 15, laborTimeDetails.getOperationNumber());
            createDateTimeCell(stylesContainer, row, 16, laborTimeDetails.getTimeRangeFrom());
            createDateTimeCell(stylesContainer, row, 17, laborTimeDetails.getTimeRangeTo());
            createRegularCell(stylesContainer, row, 18, laborTimeDetails.getShiftName());
            createDateTimeCell(stylesContainer, row, 19, laborTimeDetails.getCreateDate());
            createTimeCell(stylesContainer, row, 20, laborTimeDetails.getLaborTime());
            createTimeCell(stylesContainer, row, 21, laborTimeDetails.getPlannedLaborTime());
            createTimeCell(stylesContainer, row, 22, laborTimeDetails.getLaborTimeDeviation());
            createTimeCell(stylesContainer, row, 23, laborTimeDetails.getMachineTime());
            createTimeCell(stylesContainer, row, 24, laborTimeDetails.getPlannedMachineTime());
            createTimeCell(stylesContainer, row, 25, laborTimeDetails.getMachineTimeDeviation());
            rowCounter++;
        }
        for (int i = 0; i <= 25; i++) {
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

    private void createProductsBalanceSheet(List<OrderBalance> productsBalance, HSSFSheet sheet, Locale locale) {
        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);
        final int rowOffset = 1;
        HSSFRow row = sheet.createRow(0);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productNumber", locale),
                0, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productName", locale),
                1, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.producedQuantity", locale),
                2, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCosts", locale),
                3, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCosts", locale),
                4, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.technicalProductionCosts", locale),
                5, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCostMargin", locale),
                6, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.materialCostMarginValue", locale),
                7, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCostMargin", locale),
                8, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.productionCostMarginValue", locale),
                9, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.additionalOverhead", locale),
                10, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.directAdditionalCost", locale),
                11, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.totalCosts", locale),
                12, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPrice", locale),
                13, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPriceOverhead", locale),
                14, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.registrationPriceOverheadValue",
                        locale),
                15, CellStyle.ALIGN_LEFT);
        createHeaderCell(stylesContainer, row,
                translationService.translate(
                        "productionCounting.productionBalance.report.xls.sheet.ordersBalance.realProductionCosts", locale),
                16, CellStyle.ALIGN_LEFT);
        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.profit", locale),
                17, CellStyle.ALIGN_LEFT);

        createHeaderCell(stylesContainer,
                row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.profitValue", locale),
                18, CellStyle.ALIGN_LEFT);

        createHeaderCell(
                stylesContainer, row, translationService
                        .translate("productionCounting.productionBalance.report.xls.sheet.ordersBalance.sellPrice", locale),
                19, CellStyle.ALIGN_LEFT);

        int rowCounter = 0;
        for (OrderBalance orderBalance : productsBalance) {
            row = sheet.createRow(rowOffset + rowCounter);
            createRegularCell(stylesContainer, row, 0, orderBalance.getProductNumber());
            createRegularCell(stylesContainer, row, 1, orderBalance.getProductName());
            createNumericCell(stylesContainer, row, 2, orderBalance.getProducedQuantity());
            createNumericCell(stylesContainer, row, 3, orderBalance.getMaterialCosts());
            createNumericCell(stylesContainer, row, 4, orderBalance.getProductionCosts());
            createNumericCell(stylesContainer, row, 5, orderBalance.getTechnicalProductionCosts());
            createNumericCell(stylesContainer, row, 6, orderBalance.getMaterialCostMargin());
            createNumericCell(stylesContainer, row, 7, orderBalance.getMaterialCostMarginValue());
            createNumericCell(stylesContainer, row, 8, orderBalance.getProductionCostMargin());
            createNumericCell(stylesContainer, row, 9, orderBalance.getProductionCostMarginValue());
            createNumericCell(stylesContainer, row, 10, orderBalance.getAdditionalOverhead());
            createNumericCell(stylesContainer, row, 11, orderBalance.getDirectAdditionalCost());
            createNumericCell(stylesContainer, row, 12, orderBalance.getTotalCosts());
            createNumericCell(stylesContainer, row, 13, orderBalance.getRegistrationPrice());
            createNumericCell(stylesContainer, row, 14, orderBalance.getRegistrationPriceOverhead());
            createNumericCell(stylesContainer, row, 15, orderBalance.getRegistrationPriceOverheadValue());
            createNumericCell(stylesContainer, row, 16, orderBalance.getRealProductionCosts());
            createNumericCell(stylesContainer, row, 17, orderBalance.getProfit());
            createNumericCell(stylesContainer, row, 18, orderBalance.getProfitValue());
            createNumericCell(stylesContainer, row, 19, orderBalance.getSellPrice());
            rowCounter++;
        }
        for (int i = 0; i <= 19; i++) {
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
        cell.setCellValue(numberService.setScaleWithDefaultMathContext(value, 2).doubleValue());
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.numberStyle, HSSFCellStyle.ALIGN_RIGHT));
        return cell;
    }

    private HSSFCell createTimeCell(StylesContainer stylesContainer, HSSFRow row, int column, Integer value) {
        HSSFCell cell = row.createCell(column, HSSFCell.CELL_TYPE_NUMERIC);
        if (value == null) {
            value = 0;
        }
        cell.setCellValue(Math.abs(value) / 86400d);
        if (value >= 0) {
            cell.setCellStyle(StylesContainer.aligned(stylesContainer.timeStyle, HSSFCellStyle.ALIGN_RIGHT));
        } else {
            cell.setCellStyle(StylesContainer.aligned(stylesContainer.negativeTimeStyle, HSSFCellStyle.ALIGN_RIGHT));
        }
        return cell;
    }

    private HSSFCell createDateTimeCell(StylesContainer stylesContainer, HSSFRow row, int column, Date value) {
        HSSFCell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
            cell.setCellStyle(StylesContainer.aligned(stylesContainer.dateTimeStyle, HSSFCellStyle.ALIGN_RIGHT));
        }
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

        private final HSSFCellStyle negativeTimeStyle;

        private final HSSFCellStyle numberStyle;

        private final HSSFCellStyle dateTimeStyle;

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

            negativeTimeStyle = workbook.createCellStyle();
            negativeTimeStyle.setDataFormat(workbook.createDataFormat().getFormat("-[HH]:MM:SS"));

            numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00###"));

            dateTimeStyle = workbook.createCellStyle();
            dateTimeStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
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
