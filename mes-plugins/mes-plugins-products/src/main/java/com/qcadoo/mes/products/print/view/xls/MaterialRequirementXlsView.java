package com.qcadoo.mes.products.print.view.xls;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.qcadoo.mes.internal.DefaultEntity;

public final class MaterialRequirementXlsView extends AbstractExcelView {

    private static final String XLS_EXTENSION = ".xls";

    @Override
    protected void buildExcelDocument(final Map<String, Object> model, HSSFWorkbook workbook, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        DefaultEntity entity = (DefaultEntity) model.get("entity");

        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(new File((String) entity.getField("fileName")
                + XLS_EXTENSION)));
        workbook = new HSSFWorkbook(fs);

    }
}
