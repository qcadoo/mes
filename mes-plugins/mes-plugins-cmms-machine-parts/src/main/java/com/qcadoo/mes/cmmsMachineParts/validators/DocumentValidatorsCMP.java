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
package com.qcadoo.mes.cmmsMachineParts.validators;

import com.qcadoo.mes.cmmsMachineParts.constants.DocumentFieldsCMP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service public class DocumentValidatorsCMP {

    public boolean checkSelectedEvents(final DataDefinition documentDD, final Entity document) {
        boolean valid = true;
        if (document.getBelongsToField(DocumentFieldsCMP.MAINTENANCE_EVENT) != null
                && document.getBelongsToField(DocumentFieldsCMP.PLANNED_EVENT) != null) {
            document.addError(documentDD.getField(DocumentFieldsCMP.PLANNED_EVENT), "materialFlowResources.document.error.canSelectOnlyOneEvent");
            valid = false;
        }
        return valid;
    }

}
