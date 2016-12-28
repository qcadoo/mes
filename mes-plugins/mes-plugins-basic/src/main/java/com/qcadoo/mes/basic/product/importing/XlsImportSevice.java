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

import org.apache.poi.POIXMLProperties;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

@Service
public class XlsImportSevice {

    private static final String SCHEMA_VERSION = "1.0.0";
    private static final String SCHEMA_VERSION_PROPERTY_NAME = "SchemaVersion";
    private static final int COLUMN_NUMBER = 13;

    @Autowired
    private RowProcessorFactory rowProcessorFactory;

    @Transactional(rollbackFor = ImportException.class)
    public ImportStatus importFrom(final XSSFWorkbook workbook) throws ImportException {

        assureSpreadsheetMatchesCurrentSchemaVersion(workbook);
        final int startRow = 1;
        ImportStatus importStatus = new ImportStatus();
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (int rowIndx = startRow; rowIndx < sheet.getLastRowNum(); rowIndx++) {

            Row row = sheet.getRow(rowIndx);
            if (row == null) { // This whole row is empty
                break;
            }
            final RowProcessor rowProcessor = rowProcessorFactory.create(importStatus, rowIndx);
            for (int colIndx = 0; colIndx < COLUMN_NUMBER; colIndx++) {
                rowProcessor.append(row.getCell(colIndx, Row.RETURN_BLANK_AS_NULL));
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


    private void assureSpreadsheetMatchesCurrentSchemaVersion(XSSFWorkbook workbook) throws ImportException {
        POIXMLProperties properties = workbook.getProperties();
        POIXMLProperties.CustomProperties customProperties = properties.getCustomProperties();
        CTProperties underlyingProperties = customProperties.getUnderlyingProperties();
        List<CTProperty> propertyList = underlyingProperties.getPropertyList();
        for (CTProperty ctProperty : propertyList) {
            if (SCHEMA_VERSION_PROPERTY_NAME.equals(ctProperty.getName()) && SCHEMA_VERSION.equals(ctProperty.getLpwstr())) {
                return;
            }
        }
        throw new ImportException("SchemaVersion metadata is either missing or has incorrect value");
    }
}
