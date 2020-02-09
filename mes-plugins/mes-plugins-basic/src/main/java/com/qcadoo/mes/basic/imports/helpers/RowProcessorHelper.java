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
package com.qcadoo.mes.basic.imports.helpers;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.imports.dtos.CellBinder;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.dtos.ImportError;
import com.qcadoo.mes.basic.imports.dtos.ImportStatus;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.model.constants.VersionableConstants;

public class RowProcessorHelper {

    private Entity entity;

    private CellBinderRegistry cellBinderRegistry;

    private ImportStatus importStatus;

    private int currentRow;

    private boolean finished = false;

    private int index = 0;

    private boolean empty = true;

    private List<ImportError> rowErrors = Lists.newArrayList();

    public RowProcessorHelper(final Entity entity, final CellBinderRegistry cellBinderRegistry, final ImportStatus importStatus,
            final int rowIndex) {
        this.entity = entity;
        this.cellBinderRegistry = cellBinderRegistry;
        this.importStatus = importStatus;
        this.currentRow = rowIndex;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void append(final Cell cell) {
        assureNotProcessedYet();

        if (Objects.nonNull(cell)) {
            empty = false;
        }

        final CellBinder binder = cellBinderRegistry.getCellBinder(index++);

        binder.bind(cell, entity, errorCode -> rowErrors.add(new ImportError(currentRow, binder.getFieldName(), errorCode)));
    }

    public void append(final String cellValue) {
        assureNotProcessedYet();

        if (Objects.nonNull(cellValue)) {
            empty = false;
        }

        final CellBinder binder = cellBinderRegistry.getCellBinder(index++);

        binder.bind(cellValue, entity, errorCode -> rowErrors.add(new ImportError(currentRow, binder.getFieldName(), errorCode)));
    }

    public void append(Cell cell, Cell dependentCell) {
        assureNotProcessedYet();

        if (Objects.nonNull(cell)) {
            empty = false;
        }

        final CellBinder binder = cellBinderRegistry.getCellBinder(index++);

        binder.bind(cell, dependentCell, entity,
                errorCode -> rowErrors.add(new ImportError(currentRow, binder.getFieldName(), errorCode)));
    }

    public void update(final Entity entityToUpdate, final Function<Entity, Boolean> checkOnUpdate) {
        if (!Objects.isNull(entityToUpdate)) {
            if (!Objects.isNull(checkOnUpdate)) {
                if (checkOnUpdate.apply(entityToUpdate)) {
                    setId(entityToUpdate.getId());
                    setVersion(entityToUpdate.getLongField(VersionableConstants.VERSION_FIELD_NAME));
                } else {
                    entityToUpdate.getGlobalErrors().forEach(this::addGlobalError);
                }
            } else {
                setId(entityToUpdate.getId());
                setVersion(entityToUpdate.getLongField(VersionableConstants.VERSION_FIELD_NAME));
            }
        }
    }

    private void setId(final Long entityId) {
        entity.setId(entityId);
    }

    private void setVersion(final Long entityVersion) {
        entity.setField(VersionableConstants.VERSION_FIELD_NAME, entityVersion);
    }

    private void addGlobalError(final ErrorMessage errorMessage) {
        entity.addGlobalError(errorMessage.getMessage(), errorMessage.getAutoClose(), errorMessage.getVars());
    }

    private Entity save() {
        return entity.getDataDefinition().save(entity);
    }

    public void process() {
        assureNotProcessedYet();

        finished = true;

        importStatus.incrementRowsProcessedCounter();

        populateImportStatusWithBindingErrors();

        if (rowErrors.isEmpty()) {
            Entity savedEntity = save();

            populateImportStatusWithEntityErrors(savedEntity);
        }
    }

    private void assureNotProcessedYet() {
        if (finished) {
            throw new IllegalStateException("Row already processed");
        }
    }

    private void populateImportStatusWithBindingErrors() {
        rowErrors.forEach(importStatus::addError);
    }

    private void populateImportStatusWithEntityErrors(final Entity entity) {
        if (!entity.isValid()) {
            entity.getErrors().entrySet().forEach(entry -> importStatus.addError(
                    new ImportError(currentRow, entry.getKey(), entry.getValue().getMessage(), entry.getValue().getVars())));

            entity.getGlobalErrors().forEach(errorMessage -> importStatus
                    .addError(new ImportError(currentRow, null, errorMessage.getMessage(), errorMessage.getVars())));
        }
    }

}
