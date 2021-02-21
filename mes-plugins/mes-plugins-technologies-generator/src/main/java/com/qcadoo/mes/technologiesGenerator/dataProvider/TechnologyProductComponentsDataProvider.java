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
package com.qcadoo.mes.technologiesGenerator.dataProvider;

import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologiesGenerator.constants.ProductFieldsTG;
import com.qcadoo.mes.technologiesGenerator.domain.OutputProductComponentId;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class TechnologyProductComponentsDataProvider {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> findInputs(final TechnologyId technologyId, boolean generationMode) {
        SearchCriteriaBuilder scb = null;
        if (generationMode) {
            scb = createGenerationModeBaseCriteria(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT,
                    OperationProductInComponentFields.OPERATION_COMPONENT, OperationProductInComponentFields.PRODUCT,
                    technologyId);
        } else {
            scb = createBaseCriteria(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT,
                    OperationProductInComponentFields.OPERATION_COMPONENT, OperationProductInComponentFields.PRODUCT,
                    technologyId);
        }
        return scb.list().getEntities();
    }

    public List<Entity> findOutputs(final TechnologyId technologyId,
            final Optional<OutputProductComponentId> excludedComponentId, boolean generationMode) {
        SearchCriteriaBuilder scb = null;
        if (generationMode) {
            scb = createGenerationModeBaseCriteria(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT,
                    OperationProductOutComponentFields.OPERATION_COMPONENT, OperationProductOutComponentFields.PRODUCT,
                    technologyId);
        } else {
            scb = createBaseCriteria(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT,
                    OperationProductOutComponentFields.OPERATION_COMPONENT, OperationProductOutComponentFields.PRODUCT,
                    technologyId);
        }
        excludedComponentId.map(OutputProductComponentId::get).map(SearchRestrictions::idNe).ifPresent(scb::add);
        return scb.list().getEntities();
    }

    private SearchCriteriaBuilder createBaseCriteria(final String modelName, final String tocFieldName,
            final String productFieldName, final TechnologyId technologyId) {
        DataDefinition dd = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, modelName);
        SearchCriteriaBuilder opicCriteria = dd.find();
        opicCriteria.createCriteria(tocFieldName, "toc_alias", JoinType.INNER)
                .createCriteria(TechnologyOperationComponentFields.TECHNOLOGY, "tech_alias", JoinType.INNER)
                .add(idEq(technologyId.get()));
        opicCriteria.createCriteria(productFieldName, "prod_alias", JoinType.INNER).add(
                eq(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));
        return opicCriteria;
    }

    private SearchCriteriaBuilder createGenerationModeBaseCriteria(final String modelName, final String tocFieldName,
            final String productFieldName, final TechnologyId technologyId) {
        DataDefinition dd = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, modelName);
        SearchCriteriaBuilder opicCriteria = dd.find();
        opicCriteria.createCriteria(tocFieldName, "toc_alias", JoinType.INNER)
                .createCriteria(TechnologyOperationComponentFields.TECHNOLOGY, "tech_alias", JoinType.INNER)
                .add(idEq(technologyId.get()));
        opicCriteria.createCriteria(productFieldName, "prod_alias", JoinType.INNER).add(
                eq(ProductFieldsTG.FROM_GENERATOR, true));
        return opicCriteria;
    }

}
