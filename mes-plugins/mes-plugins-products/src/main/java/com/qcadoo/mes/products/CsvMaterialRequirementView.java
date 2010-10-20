package com.qcadoo.mes.products;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.qcadoo.mes.internal.DefaultEntity;

public final class CsvMaterialRequirementView extends AbstractExcelView {

    @Override
    protected void buildExcelDocument(final Map<String, Object> model, final HSSFWorkbook workbook,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");

        HSSFSheet sheet = workbook.createSheet("Title");

        HSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue("Numer");
        header.createCell(1).setCellValue("Nazwa");
        header.createCell(2).setCellValue("Ilosc");
        header.createCell(3).setCellValue("Jednostka");

        int rowNum = 1;
        for (int i = 0; i < 5; i++) {
            // create the row data
            HSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("<numer>");
            row.createCell(1).setCellValue("<nazwa>");
            row.createCell(2).setCellValue("<ilosc>");
            row.createCell(3).setCellValue("<jednostka>");
        }

    }
}
