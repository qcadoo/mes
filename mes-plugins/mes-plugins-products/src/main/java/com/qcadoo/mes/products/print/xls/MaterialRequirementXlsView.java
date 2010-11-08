package com.qcadoo.mes.products.print.xls;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.products.print.xls.util.XlsCopyUtil;

public final class MaterialRequirementXlsView extends AbstractExcelView {

    @Override
    protected void buildExcelDocument(final Map<String, Object> model, final HSSFWorkbook workbook,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");
        Object fileName = entity.getField("fileName");
        if (fileName != null && !"".equals(fileName.toString().trim())) {
            copyWorkbook(workbook, (String) fileName);
            String fileNameWithoutPath = ((String) fileName).substring(((String) fileName).lastIndexOf("/") + 1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileNameWithoutPath + XlsCopyUtil.XLS_EXTENSION);
        }
    }

    private void copyWorkbook(final HSSFWorkbook newWorkbook, final String existingWorkbookFileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(existingWorkbookFileName + XlsCopyUtil.XLS_EXTENSION);
        POIFSFileSystem fs = new POIFSFileSystem(fileInputStream);
        HSSFWorkbook existingWorkbook = new HSSFWorkbook(fs);
        fileInputStream.close();
        int n = existingWorkbook.getNumberOfSheets();
        for (int i = 0; i < n; i++) {
            HSSFSheet existingSheet = existingWorkbook.getSheetAt(i);
            HSSFSheet sheet = newWorkbook.createSheet(existingSheet.getSheetName());
            XlsCopyUtil.copySheets(sheet, existingSheet);
        }
    }
}
