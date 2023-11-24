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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ResourceModelHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onSave(final DataDefinition resourceDD, final Entity resource) {
        if (StringUtils.isEmpty(resource.getStringField(ResourceFields.NUMBER))) {
            Date time = resource.getDateField(ResourceFields.TIME);

            if (Objects.isNull(time)) {
                time = new Date();
            }

            Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("date", time);

            String number = jdbcTemplate.queryForObject("select generate_and_set_resource_number(:date)", parameters,
                    String.class);

            resource.setField(ResourceFields.NUMBER, number);
        }

        setTypeOfPallet(resource);
    }

    private void setTypeOfPallet(final Entity resource) {
        Entity palletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);

        if (Objects.isNull(palletNumber)) {
            resource.setField(ResourceFields.TYPE_OF_PALLET, null);
        }
    }

    public void onCreate(final DataDefinition resourceDD, final Entity resource) {
        resource.setField(ResourceFields.IS_CORRECTED, false);

        if (Objects.isNull(resource.getField(ResourceFields.WASTE))) {
            resource.setField(ResourceFields.WASTE, false);
        }
        if (Objects.isNull(resource.getField(ResourceFields.BLOCKED_FOR_QUALITY_CONTROL))) {
            resource.setField(ResourceFields.BLOCKED_FOR_QUALITY_CONTROL, false);
        }
        if (Objects.isNull(resource.getDecimalField(ResourceFields.RESERVED_QUANTITY))) {
            resource.setField(ResourceFields.RESERVED_QUANTITY, BigDecimal.ZERO);
        }
        if (Objects.isNull(resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY))) {
            resource.setField(ResourceFields.AVAILABLE_QUANTITY, resource.getDecimalField(ResourceFields.QUANTITY));
        }
    }

}
