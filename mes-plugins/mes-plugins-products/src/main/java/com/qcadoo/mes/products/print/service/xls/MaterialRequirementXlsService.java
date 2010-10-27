package com.qcadoo.mes.products.print.service.xls;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.products.print.service.MaterialRequirementDocumentService;

@Service
public final class MaterialRequirementXlsService extends MaterialRequirementDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(MaterialRequirementXlsService.class);

    private static final String XLS_EXTENSION = ".xls";

    @Override
    public void generateDocument(final Entity entity, final Locale locale) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(translationService.translate("products.materialRequirement.report.title", locale));
        addHeader(sheet, locale);
        addSeries(sheet, entity);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(getFileName((Date) entity.getField("date")) + XLS_EXTENSION);
            workbook.write(outputStream);
        } catch (IOException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
            if (outputStream != null) {
                outputStream.close();
            }
            throw e;
        }
        outputStream.close();
        updateFileName(entity, getFileName((Date) entity.getField("date")));
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
        for (Entry<ProxyEntity, BigDecimal> entry : products.entrySet()) {
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey().getField("number").toString());
            row.createCell(1).setCellValue(entry.getKey().getField("name").toString());
            row.createCell(2).setCellValue(entry.getValue().longValueExact());
            row.createCell(3).setCellValue(entry.getKey().getField("unit").toString());

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
