package com.qcadoo.mes.products;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;

public final class XlsMaterialRequirementView extends AbstractExcelView {

    @Autowired
    private TranslationService translationService;

    @Override
    protected void buildExcelDocument(final Map<String, Object> model, final HSSFWorkbook workbook,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");

        HSSFSheet sheet = workbook.createSheet(translationService.translate("products.materialRequirement.report.title",
                request.getLocale()));
        addHeader(sheet, request.getLocale());
        addSeries(sheet, entity);
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
        List<Entity> orders = (List<Entity>) entity.getField("orders");
        List<Entity> instructions = new ArrayList<Entity>();
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity instruction = (Entity) order.getField("instruction");
            if (instruction != null) {
                instructions.add(instruction);
            }
        }
        for (Entity instruction : instructions) {
            List<Entity> bomComponents = (List<Entity>) instruction.getField("bomComponents");
            for (Entity bomComponent : bomComponents) {
                Entity product = (Entity) bomComponent.getField("product");
                if (!(Boolean) entity.getField("onlyComponents") || "component".equals(product.getField("typeOfMaterial"))) {
                    HSSFRow row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(product.getField("number").toString());
                    row.createCell(1).setCellValue(product.getField("name").toString());
                    row.createCell(2).setCellValue(bomComponent.getField("quantity").toString());
                    row.createCell(3).setCellValue(product.getField("unit").toString());
                }
            }
        }
    }
}
