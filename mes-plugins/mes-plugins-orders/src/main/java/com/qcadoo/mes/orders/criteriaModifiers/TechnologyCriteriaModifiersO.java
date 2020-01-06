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
package com.qcadoo.mes.orders.criteriaModifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TechnologyCriteriaModifiersO {

    public static final String PRODUCT_PARAMETER = "product";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showTechnologyForProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        Long productId = filterValue.getLong(PRODUCT_PARAMETER);
        scb.add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_PRODUCT, productId));
        scb.add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE));
    }

    public void showAcceptedPatternTechnologyForProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(PRODUCT_PARAMETER)) {
            Long productId = filterValue.getLong(PRODUCT_PARAMETER);
            Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)
                    .get(productId);
            Entity parent = product.getBelongsToField(ProductFields.PARENT);
            if (parent != null) {
                scb.add(SearchRestrictions.or(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product),
                        SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, parent)));
            } else {
                scb.add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product));
            }
        } else {
            scb.add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_PRODUCT, 0L));
        }

        scb.add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE));
        scb.add(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyStateStringValues.ACCEPTED));
    }

}
