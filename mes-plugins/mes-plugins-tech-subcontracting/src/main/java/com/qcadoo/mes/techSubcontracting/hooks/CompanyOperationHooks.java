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

import com.qcadoo.mes.techSubcontracting.constants.CompanyFieldsTS;
import com.qcadoo.mes.techSubcontracting.constants.CompanyOperationFields;
import com.qcadoo.mes.techSubcontracting.constants.OperationFieldsTS;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyOperationHooks {

    public boolean checkIfOperationIsNotAlreadyUsed(final DataDefinition dataDefinition, final Entity entity) {
        if (checkIfOperationIsUsed(entity)) {
            entity.addError(dataDefinition.getField(CompanyOperationFields.OPERATION),
                    "basic.company.message.operationIsAlreadyUsed");

            return false;
        }

        return true;
    }

    public boolean checkIfOperationHasDefaultSupplier(final DataDefinition dataDefinition, final Entity entity) {
        if (checkIfDefaultAlreadyExists(entity)) {
            entity.addError(dataDefinition.getField(CompanyOperationFields.IS_DEFAULT),
                    "basic.company.message.defaultAlreadyExistsForOperation");

            return false;
        }

        return true;
    }

    public boolean checkIfOperationIsUsed(final Entity entity) {
        if (entity.getId() == null) {
            Entity operation = entity.getBelongsToField(CompanyOperationFields.OPERATION);
            Entity company = entity.getBelongsToField(CompanyOperationFields.COMPANY);

            if (operation != null && company != null) {
                Entity operationsCount = company.getHasManyField(CompanyFieldsTS.OPERATIONS).find()
                        .createAlias(CompanyOperationFields.OPERATION, CompanyOperationFields.OPERATION, JoinType.INNER)
                        .add(SearchRestrictions.eq(CompanyOperationFields.OPERATION + ".id", operation.getId()))
                        .setProjection(SearchProjections.alias(SearchProjections.countDistinct("id"), "count"))
                        .addOrder(SearchOrders.desc("count")).setMaxResults(1).uniqueResult();

                return (Long) operationsCount.getField("count") > 0;
            }
        }

        return false;
    }

    public boolean checkIfDefaultAlreadyExists(final Entity entity) {
        if (entity.getBooleanField(CompanyOperationFields.IS_DEFAULT)) {
            Entity operation = entity.getBelongsToField(CompanyOperationFields.OPERATION);

            if (operation != null) {
                List<Entity> companiesForOperation = operation.getHasManyField(OperationFieldsTS.COMPANIES);
                return companiesForOperation.stream().anyMatch(
                        e -> e.getBooleanField(CompanyOperationFields.IS_DEFAULT)
                                && !e.getId().equals(entity.getId()));
            }
        }

        return false;
    }
}