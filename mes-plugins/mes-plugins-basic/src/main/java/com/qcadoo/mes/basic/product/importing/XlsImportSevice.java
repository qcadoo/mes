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

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
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
import java.util.Map;

@Service
public class XlsImportSevice {

    public static final String SCHEMA_VERSION = "1.0.0";
    public static final String SCHEMA_VERSION_PROPERTY_NAME = "SchemaVersion";
    private static final int COLUMN_NUMBER = 13;


    private final DataDefinitionService dataDefinitionService;

    @Autowired
    public XlsImportSevice(DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    @Transactional(rollbackFor = ImportException.class)
    public ImportStatus importFrom(final XSSFWorkbook workbook) throws ImportException {

        assureSpreadsheetMatchesCurrentSchemaVersion(workbook);
        DataDefinition dataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        ProductRowBuilderFactory builderFactory = new ProductRowBuilderFactory(dataDefinition);
        ImportStatus importStatus = new ImportStatus();
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (int rowIndx = 1; rowIndx < sheet.getLastRowNum(); rowIndx++) {

            Row row = sheet.getRow(rowIndx);
            if (row == null) { // This whole row is empty
                break; // TODO verify if that's true
            }
            RowBuilder rowBuilder = builderFactory.builder();
            for (int colIndx = 0; colIndx < COLUMN_NUMBER; colIndx++) {
                rowBuilder.append(row.getCell(colIndx, Row.RETURN_BLANK_AS_NULL));
            }
            if (rowBuilder.isEmpty()) {
                break; // We are done. The whole row was empty so stop processing
            }
            Entity entity = rowBuilder.build(); // check the difference between entity vs savedEntity
            Entity savedEntity = dataDefinition.save(entity);
            if (!savedEntity.isValid()) {
                for (Map.Entry<String, ErrorMessage> entry : savedEntity.getErrors().entrySet()) {
                    importStatus.addError(
                            new ImportStatus.ImportError(
                                    entry.getKey(), rowIndx, entry.getValue().getMessage())
                    );
                }
            }
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
