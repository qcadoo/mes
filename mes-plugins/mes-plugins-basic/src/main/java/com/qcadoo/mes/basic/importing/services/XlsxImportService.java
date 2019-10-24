/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.importing.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.io.Files;
import com.qcadoo.mes.basic.importing.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.importing.dtos.ImportStatus;
import com.qcadoo.mes.basic.importing.helpers.RowProcessorHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;

@Service
public class XlsxImportService extends ImportService {

    public ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName) throws IOException {
        return importFile(fis, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, false, null, null);
    }

    @Transactional
    public ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName, final Boolean shouldUpdate,
            final Function<Entity, SearchCriterion> criteriaSupplier, final Function<Entity, Boolean> checkOnUpdate)
            throws IOException {
        ImportStatus importStatus = new ImportStatus();

        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (Objects.isNull(row)) {
                break;
            }

            Entity entity = createEntity(pluginIdentifier, modelName);

            RowProcessorHelper rowProcessorService = new RowProcessorHelper(entity, cellBinderRegistry, importStatus, rowIndex);

            for (int columnIndex = 0; columnIndex < cellBinderRegistry.getSize(); columnIndex++) {
                rowProcessorService.append(row.getCell(columnIndex, Row.RETURN_BLANK_AS_NULL));
            }

            if (rowProcessorService.isEmpty()) {
                break;
            }

            if (shouldUpdate && !Objects.isNull(criteriaSupplier)) {
                Entity entityToUpdate = getEntity(pluginIdentifier, modelName, criteriaSupplier.apply(entity));

                rowProcessorService.update(entityToUpdate, checkOnUpdate);
            }

            rowProcessorService.process();
        }

        if (rollbackOnError && importStatus.hasErrors()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return importStatus;
    }

    public boolean checkFileExtension(final String filePath) {
        return Files.getFileExtension(filePath).equalsIgnoreCase(L_XLSX);
    }

}
