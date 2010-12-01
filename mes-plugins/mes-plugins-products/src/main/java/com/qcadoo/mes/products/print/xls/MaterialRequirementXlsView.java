/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

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
