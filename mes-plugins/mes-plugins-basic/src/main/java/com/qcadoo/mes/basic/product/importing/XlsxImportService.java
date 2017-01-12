/*
 * **************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
 * **************************************************************************
 */
package com.qcadoo.mes.basic.product.importing;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class XlsxImportService {

    private final RowProcessorFactory rowProcessorFactory;

    @Autowired
    public XlsxImportService(RowProcessorFactory rowProcessorFactory) {
        this.rowProcessorFactory = rowProcessorFactory;
    }

    @Transactional
    public ImportStatus importFrom(final XSSFWorkbook workbook) {

        ImportStatus importStatus = new ImportStatus();
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (int rowIndex = SpreadsheetSchemaInfo.START_ROW_INDEX; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

            Row row = sheet.getRow(rowIndex);
            if (row == null) { // This whole row is empty
                break;
            }
            final RowProcessor rowProcessor = rowProcessorFactory.create(importStatus, rowIndex);
            for (int colIndex = 0; colIndex < SpreadsheetSchemaInfo.COLUMN_NUMBER; colIndex++) {
                rowProcessor.append(row.getCell(colIndex, Row.RETURN_BLANK_AS_NULL));
            }
            if (rowProcessor.isEmpty()) {
                break; // We are done. The whole row was empty so stop processing
            }
            rowProcessor.process();
        }

        if (importStatus.hasErrors()) { // We have to rollback transaction here
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return importStatus;
    }

}
