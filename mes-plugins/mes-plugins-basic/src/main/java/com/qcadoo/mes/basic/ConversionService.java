/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ConversionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public Long getParameterId() {

        DataDefinition dataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_CONVERSION);
        // Entity conversion = dataDefinition.find().add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        // Long unit = dataDefinition.find().add(SearchRestrictions.eq("name", "units")).setMaxResults(1).uniqueResult().getId();
        Entity conversion = dataDefinition.find().setMaxResults(1).uniqueResult();

        if (conversion == null) {
            Entity newConversion = dataDefinition.create();
            // newConversion.setField("units", unit);
            Entity savedConversion = dataDefinition.save(newConversion);
            return savedConversion.getId();

        } else {
            return conversion.getId();
        }

    }

}
