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

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

class RowProcessor {

    private final CellBinder[] parsers;
    private final ImportStatus importStatus;
    private final DataDefinition dataDefinition;
    private final Entity entity;
    private boolean finished;
    private int index;
    private boolean empty = true;
    private int currentRow;

    RowProcessor(ImportStatus importStatus, DataDefinition dataDefinition, Entity entity, int rowIndx, CellBinder[] cellBinders) {
        this.importStatus = importStatus;
        this.dataDefinition = dataDefinition;
        this.entity = entity;
        this.currentRow = rowIndx;
        this.parsers = cellBinders;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void append(final Cell cell) {
        assureNotProcessedYet();
        if (null != cell) {
            empty = false;
        }
        final CellBinder binder = parsers[index++];
        binder.bind(
                cell,
                entity,
                errorCode -> {
                    importStatus.addError(new ImportStatus.ImportError(binder.getFieldName(), currentRow, errorCode));
                }
        );
    }

    private void assureNotProcessedYet() {
        if (finished) {
            throw new IllegalStateException("Row already processed");
        }
    }

    public void process() {
        assureNotProcessedYet();
        finished = true;
        Entity savedEntity = dataDefinition.save(entity);
        populateImportStatusWithEntityErrors(importStatus, savedEntity, currentRow);
    }

    private void populateImportStatusWithEntityErrors(ImportStatus importStatus, Entity entity, int rowIndx) {
        if (!entity.isValid()) {
            for (Map.Entry<String, ErrorMessage> entry : entity.getErrors().entrySet()) {
                importStatus.addError(
                        new ImportStatus.ImportError(
                                entry.getKey(), rowIndx, entry.getValue().getMessage())
                );
            }
        }
    }
}
