/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.costCalculation.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TechnologyCriteriaModifiersCC {

    public void restrictTechnologyToOrderTechnologyOrProductTechnology(final SearchCriteriaBuilder scb,
            final FilterValueHolder filterValue) {
        Long productId = null;

        if (filterValue.has(CostCalculationFields.PRODUCT)) {
            productId = filterValue.getLong(CostCalculationFields.PRODUCT);
        }

        Long technologyId = null;

        if (filterValue.has(CostCalculationFields.TECHNOLOGY)) {
            technologyId = filterValue.getLong(CostCalculationFields.TECHNOLOGY);
        }

        if ((productId != null) && (technologyId != null)) {
            scb.add(SearchRestrictions.or(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT,
                    BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT, productId), SearchRestrictions
                    .idEq(technologyId)));
        } else if (productId != null) {
            scb.add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_PRODUCT, productId));
            scb.add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE));
        } else if (technologyId != null) {
            scb.add(SearchRestrictions.idEq(technologyId));
        } else {
            scb.add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE));
        }
    }

}
