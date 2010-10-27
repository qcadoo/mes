package com.qcadoo.mes.products.print.view.xls;

import java.io.FileInputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.products.print.view.xls.util.ExcelUtil;

public final class MaterialRequirementXlsView extends AbstractExcelView {

    private static final String XLS_EXTENSION = ".xls";

    @Override
    protected void buildExcelDocument(final Map<String, Object> model, final HSSFWorkbook workbook,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");
        FileInputStream fileInputStream = new FileInputStream((String) entity.getField("fileName") + XLS_EXTENSION);
        POIFSFileSystem fs = new POIFSFileSystem(fileInputStream);
        HSSFWorkbook existingWorkbook = new HSSFWorkbook(fs);
        fileInputStream.close();

        int n = existingWorkbook.getNumberOfSheets();
        for (int i = 0; i < n; i++) {
            HSSFSheet existingSheet = existingWorkbook.getSheetAt(i);
            HSSFSheet sheet = workbook.createSheet(existingSheet.getSheetName());
            ExcelUtil.copySheets(sheet, existingSheet);
        }
    }

}
