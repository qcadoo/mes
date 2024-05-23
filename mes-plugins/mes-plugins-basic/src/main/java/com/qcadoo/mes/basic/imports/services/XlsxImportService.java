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
package com.qcadoo.mes.basic.imports.services;

import com.google.common.io.Files;
import com.qcadoo.mes.basic.imports.dtos.CellBinder;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.dtos.ImportStatus;
import com.qcadoo.mes.basic.imports.helpers.RowProcessorHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

@Service
public class XlsxImportService extends ImportService {

    @Transactional
    public ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
                                   final Boolean rollbackOnError, final String pluginIdentifier, final String modelName,
                                   final Entity belongsTo,
                                   final String belongsToName, final Boolean shouldUpdate,
                                   final Function<Entity, SearchCriterion> criteriaSupplier,
                                   final Function<Entity, Boolean> checkOnUpdate,
                                   final Boolean shouldSkip) throws IOException {
        ImportStatus importStatus = new ImportStatus();

        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (Objects.isNull(row)) {
                break;
            }

            Entity entity = createEntity(pluginIdentifier, modelName);

            if (Objects.nonNull(belongsTo) && Objects.nonNull(belongsToName)) {
                entity.setField(belongsToName, belongsTo);
            }

            RowProcessorHelper rowProcessorHelper = new RowProcessorHelper(entity, cellBinderRegistry, importStatus, rowIndex);

            for (int columnIndex = 0; columnIndex < cellBinderRegistry.getSize(); columnIndex++) {
                CellBinder cell = cellBinderRegistry.getCellBinder(columnIndex);
                String dependentFieldName = cell.getDependentFieldName();
                if (StringUtils.isEmpty(dependentFieldName)) {
                    rowProcessorHelper.append(row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                } else {
                    int dependentIndex = getDependentIndex(dependentFieldName, cellBinderRegistry);
                    rowProcessorHelper.append(row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL),
                            row.getCell(dependentIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                }
            }

            if (rowProcessorHelper.isEmpty()) {
                break;
            }

            if (shouldSkip && !Objects.isNull(criteriaSupplier)) {
                Entity entityToSkip = getEntityToSkip(pluginIdentifier, modelName, criteriaSupplier, entity);
                if (!Objects.isNull(entityToSkip)) {
                    continue;
                }
            }

            if (shouldUpdate && !Objects.isNull(criteriaSupplier)) {
                Entity entityToUpdate = getEntity(pluginIdentifier, modelName, criteriaSupplier.apply(entity));

                rowProcessorHelper.update(entityToUpdate, checkOnUpdate);
            }

            validateEntity(entity, entity.getDataDefinition());

            rowProcessorHelper.process();
        }

        if (rollbackOnError && importStatus.hasErrors()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return importStatus;
    }

    protected Entity getEntityToSkip(String pluginIdentifier, String modelName,
                                     Function<Entity, SearchCriterion> criteriaSupplier, Entity entity) {
        return getEntity(pluginIdentifier, modelName, criteriaSupplier.apply(entity));
    }

    private int getDependentIndex(final String dependentFieldName, final CellBinderRegistry cellBinderRegistry) {
        for (int columnIndex = 0; columnIndex < cellBinderRegistry.getSize(); columnIndex++) {
            if (cellBinderRegistry.getCellBinder(columnIndex).getFieldName().equals(dependentFieldName)) {
                return columnIndex;
            }
        }
        return -1;
    }

    public boolean checkFileExtension(final String filePath) {
        return Files.getFileExtension(filePath).equalsIgnoreCase(L_XLSX);
    }

}
