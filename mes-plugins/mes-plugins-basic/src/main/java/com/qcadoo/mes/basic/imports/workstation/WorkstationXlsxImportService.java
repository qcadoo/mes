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
package com.qcadoo.mes.basic.imports.workstation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class WorkstationXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_PRODUCTION_LINE = "productionLine";

    private static final String L_PRODUCTION_LINES = "productionLines";

    @Override
    public void validateEntity(final Entity workstation, final DataDefinition workstationDD) {
        validateProductionLine(workstation, workstationDD);
    }

    private void validateProductionLine(final Entity workstation, final DataDefinition workstationDD) {
        Entity productionLine = workstation.getBelongsToField(L_PRODUCTION_LINE);
        Entity division = workstation.getBelongsToField(WorkstationFields.DIVISION);

        if (Objects.nonNull(productionLine)) {
            if (Objects.isNull(division)) {
                workstation.addError(workstationDD.getField(WorkstationFields.DIVISION),
                        L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            } else {
                List<Entity> divisionProductionLines = division.getHasManyField(L_PRODUCTION_LINES);

                Optional<Entity> mayBeProductionLine = divisionProductionLines.stream()
                        .filter(divisionProductionLine -> divisionProductionLine.getId().equals(productionLine.getId()))
                        .findAny();

                if (!mayBeProductionLine.isPresent()) {
                    workstation.addError(workstationDD.getField(L_PRODUCTION_LINE), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
                }
            }
        }
    }

}
