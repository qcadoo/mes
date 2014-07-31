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
package com.qcadoo.mes.timeNormsForOperations.validators;

import static com.qcadoo.mes.technologies.tree.traversing.MainTocOutputProductCriteriaBuilder.Aliases;
import static com.qcadoo.model.api.search.SearchOrders.desc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;
import static com.qcadoo.model.api.search.SearchRestrictions.in;
import static com.qcadoo.model.api.search.SearchRestrictions.neField;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.mes.technologies.tree.traversing.MainTocOutputProductCriteriaBuilder;
import com.qcadoo.mes.technologies.tree.traversing.MasterOutputProductCriteria;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.utils.EntityUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductValidatorsTNFO {

    private static final String CORRUPTED_TECHNOLOGIES_MESSAGE = "technologies.technology.validate.product.OperationTreeNotValid";

    @Autowired
    private MainTocOutputProductCriteriaBuilder mainTocOutputProductCriteriaBuilder;

    public void checkIfUnitChangeDoesNotCorruptAnyTechnology(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Long productId = form.getEntityId();

        if (productId == null) {
            return;
        }

        List<Entity> corruptedTechnologyNumbers = findCorruptedTechnologyNumbers(productId);
        if (!corruptedTechnologyNumbers.isEmpty()) {
            showCorruptedTechnologiesNotification(form, corruptedTechnologyNumbers);
        }
    }

    private void showCorruptedTechnologiesNotification(final FormComponent form, final List<Entity> wrongTechnologies) {
        Collection<String> techNumbers = Collections2.transform(wrongTechnologies,
                EntityUtils.<String> getFieldExtractor("techNumber"));
        String techNumbersAsString = StringUtils.join(techNumbers, ", ");
        form.addMessage(CORRUPTED_TECHNOLOGIES_MESSAGE, MessageType.INFO, false, techNumbersAsString);
    }

    // HQL form:
    // -----------------------
    // select
    // toc.id as id,
    // t.number as techNumber
    //
    // from
    // #technologies_technologyOperationComponent toc
    // inner join toc.operationProductOutComponents opoc
    // inner join opoc.product as p
    // inner join toc.technology t
    // left join toc.parent parentToc
    // left join parentToc.operationProductInComponents opic
    //
    // where
    // (opoc.product.id = opic.product.id or (toc.parent is null and t.product.id = p.id))
    // and t.state in ('02accepted', '05checked')
    // and p.unit <> toc.productionInOneCycleUNIT
    // and p.id = :productId
    //
    // order by toc.id desc
    private List<Entity> findCorruptedTechnologyNumbers(final Long productID) {
        MasterOutputProductCriteria criteria = MasterOutputProductCriteria
                .empty()
                .withProdCriteria(idEq(productID))
                .withTechCriteria(
                        in(TechnologyFields.STATE,
                                Lists.newArrayList(TechnologyStateStringValues.ACCEPTED, TechnologyStateStringValues.CHECKED)));
        SearchCriteriaBuilder scb = mainTocOutputProductCriteriaBuilder.create(criteria);
        scb.add(neField(Aliases.OPERATION_OUTPUT_PRODUCT + "." + ProductFields.UNIT, Aliases.TOC + "."
                + TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE_UNIT));

        scb.addOrder(desc("id"));

        SearchProjection techNumProjection = alias(field(Aliases.TECHNOLOGY + "." + TechnologyFields.NUMBER), "techNumber");
        SearchProjection projection = SearchProjections.list().add(techNumProjection).add(alias(id(), "id"));
        scb.setProjection(SearchProjections.distinct(projection));
        return scb.list().getEntities();
    }
}
