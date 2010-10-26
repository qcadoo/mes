package com.qcadoo.mes.products.print.service.xls;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.products.print.service.MaterialRequirementDocumentService;

@Service
public final class MaterialRequirementXlsService extends MaterialRequirementDocumentService {

    private static final String XLS_EXTENSION = ".xls";

    // TODO KRNA check method
    @Override
    public void generateDocument(final Entity entity, final Locale locale) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(translationService.translate("products.materialRequirement.report.title", locale));
        addHeader(sheet, locale);
        addSeries(sheet, entity);
        workbook.write(new FileOutputStream(getFileName() + XLS_EXTENSION));
        updateFileName(entity, getFileName());
    }

    private void addHeader(final HSSFSheet sheet, final Locale locale) {
        HSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue(translationService.translate("products.product.number.label", locale));
        header.createCell(1).setCellValue(translationService.translate("products.product.name.label", locale));
        header.createCell(2)
                .setCellValue(translationService.translate("products.instructionBomComponent.quantity.label", locale));
        header.createCell(3).setCellValue(translationService.translate("products.product.unit.label", locale));
    }

    private void addSeries(final HSSFSheet sheet, final Entity entity) {
        int rowNum = 1;
        Map<ProxyEntity, BigDecimal> products = getProductsSeries(entity);
        for (Entity product : products.keySet()) {
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getField("number").toString());
            row.createCell(1).setCellValue(product.getField("name").toString());
            row.createCell(2).setCellValue(products.get(product).longValueExact());
            row.createCell(3).setCellValue(product.getField("unit").toString());

        }
    }

    private Map<ProxyEntity, BigDecimal> getProductsSeries(final Entity entity) {
        List<Entity> orders = (List<Entity>) entity.getField("orders");
        List<Entity> instructions = new ArrayList<Entity>();
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity instruction = (Entity) order.getField("instruction");
            if (instruction != null) {
                instructions.add(instruction);
            }
        }
        return getBomSeries(entity, instructions);
    }

}
