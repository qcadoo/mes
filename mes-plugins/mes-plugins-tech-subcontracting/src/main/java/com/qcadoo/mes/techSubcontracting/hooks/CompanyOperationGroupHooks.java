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
package com.qcadoo.mes.techSubcontracting.hooks;

import com.qcadoo.mes.techSubcontracting.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyOperationGroupHooks {

    public boolean checkIfOperationGroupIsNotAlreadyUsed(final DataDefinition dataDefinition, final Entity entity) {
        if (checkIfOperationGroupIsUsed(entity)) {
            entity.addError(dataDefinition.getField(CompanyOperationGroupFields.OPERATION_GROUP),
                    "basic.company.message.operationGroupIsAlreadyUsed");

            return false;
        }

        return true;
    }

    public boolean checkIfOperationGroupHasDefaultSupplier(final DataDefinition dataDefinition, final Entity entity) {
        if (checkIfDefaultAlreadyExists(entity)) {
            entity.addError(dataDefinition.getField(CompanyOperationGroupFields.IS_DEFAULT),
                    "basic.company.message.defaultAlreadyExistsForOperationGroup");

            return false;
        }

        return true;
    }

    public boolean checkIfOperationGroupIsUsed(final Entity entity) {
        if (entity.getId() == null) {
            Entity operationGroup = entity.getBelongsToField(CompanyOperationGroupFields.OPERATION_GROUP);
            Entity company = entity.getBelongsToField(CompanyOperationGroupFields.COMPANY);

            if (operationGroup != null && company != null) {
                Entity operationGroupsCount = company.getHasManyField(CompanyFieldsTS.OPERATION_GROUPS).find()
                        .createAlias(CompanyOperationGroupFields.OPERATION_GROUP, CompanyOperationGroupFields.OPERATION_GROUP, JoinType.INNER)
                        .add(SearchRestrictions.eq(CompanyOperationGroupFields.OPERATION_GROUP + ".id", operationGroup.getId()))
                        .setProjection(SearchProjections.alias(SearchProjections.countDistinct("id"), "count"))
                        .addOrder(SearchOrders.desc("count")).setMaxResults(1).uniqueResult();

                return (Long) operationGroupsCount.getField("count") > 0;
            }
        }

        return false;
    }

    public boolean checkIfDefaultAlreadyExists(final Entity entity) {
        if (entity.getBooleanField(CompanyOperationGroupFields.IS_DEFAULT)) {
            Entity operationGroup = entity.getBelongsToField(CompanyOperationGroupFields.OPERATION_GROUP);

            if (operationGroup != null) {
                List<Entity> companiesForOperationGroup = operationGroup.getHasManyField(OperationGroupFieldsTS.COMPANIES);
                return companiesForOperationGroup.stream().anyMatch(
                        e -> e.getBooleanField(CompanyOperationGroupFields.IS_DEFAULT)
                                && !e.getId().equals(entity.getId()));
            }
        }

        return false;
    }
}