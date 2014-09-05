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
package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.CommonReasonTypeFields;
import com.qcadoo.mes.orders.constants.deviationReasonTypes.DeviationModelDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class CommonReasonTypeModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateDate(final Entity reasonTypeEntity, final DeviationModelDescriber deviationModelDescriber) {
        if (reasonHasNotBeenSavedYet(reasonTypeEntity) || reasonHasChanged(reasonTypeEntity, deviationModelDescriber)) {
            reasonTypeEntity.setField(CommonReasonTypeFields.DATE, new Date());
        }
    }

    private boolean reasonHasNotBeenSavedYet(final Entity reasonTypeEntity) {
        return reasonTypeEntity.getId() == null;
    }

    private boolean reasonHasChanged(final Entity reasonTypeEntity, final DeviationModelDescriber deviationModelDescriber) {
        String reasonFieldName = deviationModelDescriber.getReasonTypeFieldName();
        String reason = reasonTypeEntity.getStringField(reasonFieldName);
        DataDefinition reasonDD = dataDefinitionService.get(deviationModelDescriber.getModelPlugin(),
                deviationModelDescriber.getModelName());
        SearchCriteriaBuilder scb = reasonDD.find();
        scb.add(idEq(reasonTypeEntity.getId()));
        scb.add(eq(reasonFieldName, reason));
        scb.setProjection(id());
        return scb.setMaxResults(1).list().getTotalNumberOfEntities() == 0;
    }

}
