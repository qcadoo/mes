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
package com.qcadoo.mes.masterOrders.hooks;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class SalesPlanMaterialRequirementHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onSave(final DataDefinition salesPlanMaterialRequirementDD, final Entity salesPlanMaterialRequirement) {
        if (checkIfShouldInsertNumber(salesPlanMaterialRequirement)) {
            salesPlanMaterialRequirement.setField(SalesPlanMaterialRequirementFields.NUMBER, setNumberFromSequence());
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity salesPlanMaterialRequirement) {
        if (Objects.nonNull(salesPlanMaterialRequirement.getId())) {
            return false;
        }

        return StringUtils.isEmpty(salesPlanMaterialRequirement.getStringField(SalesPlanMaterialRequirementFields.NUMBER));
    }

    private String setNumberFromSequence() {
        return jdbcTemplate.queryForObject("SELECT generate_salesplanmaterialrequirement_number()", Maps.newHashMap(),
                String.class);
    }

}
