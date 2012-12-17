/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basic;

import static com.google.common.base.Preconditions.checkState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

/**
 * Service for accessing parameters
 * 
 * @since 1.1.5
 */
@Service
public class ParameterService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Returns basic parameter entity id for current user
     * 
     * @return parameter entity id
     * 
     */
    public Long getParameterId() {
        return getParameter().getId();
    }

    /**
     * Returns basic parameter entity for current user. If parameter does not exist the new parameter entity will be created,
     * saved and returned.
     * 
     * @return parameter entity
     * 
     */
    @Transactional
    public Entity getParameter() {
        DataDefinition dataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_PARAMETER);
        Entity parameter = dataDefinition.find().setMaxResults(1).uniqueResult();

        if (parameter == null) {
            parameter = createParameter(dataDefinition);
        }

        return parameter;
    }

    private Entity createParameter(final DataDefinition dataDefinition) {
        Entity parameter = dataDefinition.create();
        parameter = dataDefinition.save(parameter);
        checkState(parameter.isValid(), "Parameter entity has validation errors! " + parameter);

        return parameter;
    }
}
