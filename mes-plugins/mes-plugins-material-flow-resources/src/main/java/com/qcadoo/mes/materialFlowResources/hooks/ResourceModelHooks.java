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

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants.MODEL_RESOURCE;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.BATCH;

@Service
public class ResourceModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onView(final DataDefinition resourceDD, final Entity resource) {
        BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);
        BigDecimal price = resource.getDecimalField(ResourceFields.PRICE);

        BigDecimal value = null;

        if (price != null) {
            value = quantity.multiply(price, numberService.getMathContext());
        }

        resource.setField(ResourceFields.VALUE, numberService.setScale(value));
    }

    public void onSave(final DataDefinition resourceDD, final Entity resource) {
        if (StringUtils.isEmpty(resource.getStringField(ResourceFields.NUMBER))) {
            Date time = resource.getDateField(ResourceFields.TIME);
            if (time == null) {
                time = new Date();
            }
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("date", time);
            String number = jdbcTemplate.queryForObject("select generate_and_set_resource_number(:date)", parameters,
                    String.class);
            resource.setField(ResourceFields.NUMBER, number);
        }
    }

    public void onCreate(final DataDefinition resourceDD, final Entity resource) {
        resource.setField(ResourceFields.IS_CORRECTED, false);
        if (resource.getField(ResourceFields.WASTE) == null) {
            resource.setField(ResourceFields.WASTE, false);
        }
        if (resource.getDecimalField(ResourceFields.RESERVED_QUANTITY) == null) {
            resource.setField(ResourceFields.RESERVED_QUANTITY, BigDecimal.ZERO);
        }
        if (resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY) == null) {
            resource.setField(ResourceFields.AVAILABLE_QUANTITY, resource.getDecimalField(ResourceFields.QUANTITY));
        }
    }

    public void generateBatch(final DataDefinition resourceDD, final Entity resource) {
        if (resource.getField(BATCH) == null) {
            resource.setField(BATCH, generateBatchForResource(MODEL_RESOURCE));
        }
    }

    private String generateBatchForResource(final String model) {
        Entity lastBatch = getLastBatch();

        String batch = null;

        if (lastBatch == null) {
            batch = "000001";
        } else {
            batch = lastBatch.getStringField(BATCH);

            Long parsedNumber = Long.parseLong(batch);

            do {
                parsedNumber++;

                batch = String.format("%06d", parsedNumber);
            } while (batchAlreadyExist(model, batch));
        }

        return batch;
    }

    private boolean batchAlreadyExist(final String model, final String batch) {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, model).find()
                .add(SearchRestrictions.eq(BATCH, batch)).setMaxResults(1).uniqueResult() != null;
    }

    private Entity getLastBatch() {
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .addOrder(SearchOrders.desc(BATCH)).setMaxResults(1).uniqueResult();
    }

}
