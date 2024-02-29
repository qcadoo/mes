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
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.dtos.ImportStatus;
import com.qcadoo.mes.basic.imports.helpers.RowProcessorHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
public class CsvImportService extends ImportService {

    @Transactional
    public ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
                                   final Boolean rollbackOnError, final String pluginIdentifier, final String modelName,
                                   final Entity belongsTo,
                                   final String belongsToName, final Boolean shouldUpdate,
                                   final Function<Entity, SearchCriterion> criteriaSupplier,
                                   final Function<Entity, Boolean> checkOnUpdate,
                                   final Boolean shouldSkip) throws IOException {
        ImportStatus importStatus = new ImportStatus();

        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(fis)).withCSVParser(parser).withSkipLines(1).build();

        List<String[]> rows = csvReader.readAll();

        int rowIndex = 0;

        for (String[] row : rows) {
            Entity entity = createEntity(pluginIdentifier, modelName);

            if (Objects.nonNull(belongsTo) && Objects.nonNull(belongsToName)) {
                entity.setField(belongsToName, belongsTo);
            }

            RowProcessorHelper rowProcessorService = new RowProcessorHelper(entity, cellBinderRegistry, importStatus, rowIndex);

            for (int columnIndex = 0; columnIndex < cellBinderRegistry.getSize(); columnIndex++) {
                rowProcessorService.append(row[columnIndex]);
            }

            if (rowProcessorService.isEmpty()) {
                break;
            }

            if (shouldSkip && !Objects.isNull(criteriaSupplier)) {
                Entity entityToSkip = getEntity(pluginIdentifier, modelName, criteriaSupplier.apply(entity));
                if (!Objects.isNull(entityToSkip)) {
                    continue;
                }
            }

            if (shouldUpdate && !Objects.isNull(criteriaSupplier)) {
                Entity entityToUpdate = getEntity(pluginIdentifier, modelName, criteriaSupplier.apply(entity));

                rowProcessorService.update(entityToUpdate, checkOnUpdate);
            }

            validateEntity(entity, entity.getDataDefinition());

            rowProcessorService.process();

            rowIndex++;
        }

        if (rollbackOnError && importStatus.hasErrors()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return importStatus;
    }

    public boolean checkFileExtension(final String filePath) {
        return Files.getFileExtension(filePath).equalsIgnoreCase(L_CSV);
    }

}
