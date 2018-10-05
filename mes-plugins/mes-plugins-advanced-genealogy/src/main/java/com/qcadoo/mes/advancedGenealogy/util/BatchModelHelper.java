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
package com.qcadoo.mes.advancedGenealogy.util;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;
import static com.qcadoo.model.api.search.SearchRestrictions.or;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class BatchModelHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity findByExternalNumber(final String externalNumber) {
        if (StringUtils.isBlank(externalNumber)) {
            return null;
        }
        SearchCriteriaBuilder scb = getBatchDataDef().find();
        scb.add(eq(BatchFields.EXTERNAL_NUMBER, externalNumber));
        return scb.setMaxResults(1).uniqueResult();
    }

    public Entity findFirstByNumberProductAndOptionallySupplier(final String number, final Entity product, final Entity supplier) {
        SearchCriteriaBuilder scb = getBatchDataDef().find();
        scb.add(eq(BatchFields.NUMBER, number));
        scb.add(belongsTo(BatchFields.PRODUCT, product));
        scb.add(or(belongsTo(BatchFields.SUPPLIER, supplier), isNull(BatchFields.SUPPLIER)));
        scb.addOrder(asc(BatchFields.SUPPLIER));
        scb.setMaxResults(1);
        return scb.uniqueResult();
    }

    private DataDefinition getBatchDataDef() {
        return dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH);
    }

}
