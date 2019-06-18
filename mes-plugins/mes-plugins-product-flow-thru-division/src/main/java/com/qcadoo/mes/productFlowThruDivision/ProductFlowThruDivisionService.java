/**
 * ***************************************************************************
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
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ProductFlowThruDivisionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Set<Entity> getProductsLocations(final Long technologyId) {
        Set<Entity> locations = Sets.newHashSet();
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);
        List<Entity> tocs = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        for (Entity toc : tocs) {
            List<Entity> opics = toc.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
            for (Entity opic : opics) {
                Entity location = opic.getBelongsToField(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION);
                if (location != null) {
                    locations.add(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                            MaterialFlowConstants.MODEL_LOCATION).get(location.getId()));
                }
            }

        }
        return locations;
    }
}
