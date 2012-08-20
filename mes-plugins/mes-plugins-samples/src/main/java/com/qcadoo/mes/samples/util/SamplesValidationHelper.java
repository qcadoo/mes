/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.samples.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public class SamplesValidationHelper {

    public void validateEntity(final Entity entity) {
        if (!entity.isValid()) {
            Map<String, ErrorMessage> errors = entity.getErrors();
            List<ErrorMessage> globalErrors = entity.getGlobalErrors();
            Set<String> keys = errors.keySet();
            StringBuilder stringError = new StringBuilder("Saved entity ");
            stringError.append(entity.getDataDefinition().getPluginIdentifier());
            stringError.append('.');
            stringError.append(entity.getDataDefinition().getName());
            stringError.append(" is invalid\n");
            stringError.append("Global errors:\n");
            for (ErrorMessage error : globalErrors) {
                stringError.append(error.getMessage()).append("\nError vars:\n");
                String[] vars = error.getVars();
                for (String errorVar : vars) {
                    stringError.append("\t").append(errorVar).append("\n");
                }
            }
            stringError.append("Errors:\n");
            for (String key : keys) {
                stringError.append("\t").append(key).append("  -  ").append(errors.get(key).getMessage()).append("\nError vars:");
                String[] vars = errors.get(key).getVars();
                for (String errorVar : vars) {
                    stringError.append("\t").append(errorVar).append("\n");
                }
            }
            stringError.append("Fields:\n");
            Map<String, Object> fields = entity.getFields();
            for (Entry<String, Object> entry : fields.entrySet()) {
                if (entry.getValue() == null) {
                    stringError.append("\t\t");
                }
                stringError.append(entry.getKey()).append(" - ").append(entry.getValue()).append("\n");
            }
            throw new IllegalStateException("Saved entity is invalid\n" + stringError.toString());
        }
    }

}
