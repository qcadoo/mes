/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.constants.GroupFields;
import com.qcadoo.security.constants.PermissionType;
import com.qcadoo.security.constants.UserFields;

@Service
public class UserValidators {

    private static final String COUNT_ALIAS = "count";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private ParameterService parameterService;

    public boolean onValidate(final DataDefinition userDD, final Entity user) {
        Entity parameter = parameterService.getParameter();
        String permissionType = user.getBelongsToField(UserFields.GROUP).getStringField(GroupFields.PERMISSION_TYPE);
        if (PermissionType.OFFICE_LICENSE.getStringValue().equals(permissionType)
                && noFreeLicenses(userDD, PermissionType.OFFICE_LICENSE.getStringValue(), user.getId(), parameter.getIntegerField(ParameterFields.NUMBER_OFFICE_LICENSES))
                || PermissionType.TERMINAL_LICENSE.getStringValue().equals(permissionType)
                && noFreeLicenses(userDD, PermissionType.TERMINAL_LICENSE.getStringValue(), user.getId(), parameter.getIntegerField(ParameterFields.NUMBER_TERMINAL_LICENSES))) {
            user.addError(userDD.getField(UserFields.GROUP), "basic.user.error.group.thereAreNoFreeLicenses");
            return false;
        }
        return true;
    }

    private boolean noFreeLicenses(final DataDefinition userDD, final String permissionType, final Long id, final long allLicenses) {
        SearchCriteriaBuilder scb = userDD.find();

        if (id != null) {
            scb.add(SearchRestrictions.idNe(id));
        }
        scb.createAlias(UserFields.GROUP, UserFields.GROUP, JoinType.INNER);
        scb.add(SearchRestrictions.eq(UserFields.GROUP + L_DOT + GroupFields.PERMISSION_TYPE, permissionType));
        scb.setProjection(SearchProjections.alias(SearchProjections.countDistinct(L_ID), COUNT_ALIAS));
        scb.addOrder(SearchOrders.desc(COUNT_ALIAS));

        Entity projectionResult = scb.setMaxResults(1).uniqueResult();

        Long countValue = (Long) projectionResult.getField(COUNT_ALIAS);

        return countValue >= allLicenses;
    }

}
