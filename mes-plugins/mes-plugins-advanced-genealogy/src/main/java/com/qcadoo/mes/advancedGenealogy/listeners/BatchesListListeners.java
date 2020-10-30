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
package com.qcadoo.mes.advancedGenealogy.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class BatchesListListeners {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void deactivateBatches(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deactivateBatches();

        view.addMessage("advancedGenealogy.batchesList.deactivateBatches.info", MessageType.INFO);
    }

    private void deactivateBatches() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("UPDATE advancedgenealogy_batch batch ");
        queryBuilder.append("SET active = false ");
        queryBuilder.append("WHERE active = true ");
        queryBuilder.append("AND NOT EXISTS ( ");
        queryBuilder.append("        SELECT batch_id FROM materialflowresources_resource resource ");
        queryBuilder.append("        WHERE resource.batch_id = batch.id ");
        queryBuilder.append(") ");
        queryBuilder.append("AND NOT EXISTS ( ");
        queryBuilder.append("        SELECT batch_id FROM deliveries_orderedproduct orderedproduct ");
        queryBuilder.append("        JOIN deliveries_delivery delivery ");
        queryBuilder.append("        ON delivery.id = orderedproduct.delivery_id ");
        queryBuilder.append("        WHERE orderedproduct.batch_id = batch.id ");
        queryBuilder.append("        AND delivery.state NOT IN ('04declined', '06received') ");
        queryBuilder.append(") ");
        queryBuilder.append("AND NOT EXISTS ( ");
        queryBuilder.append("        SELECT batch_id FROM deliveries_deliveredproduct deliveredproduct ");
        queryBuilder.append("        JOIN deliveries_delivery delivery ");
        queryBuilder.append("        ON delivery.id = deliveredproduct.delivery_id ");
        queryBuilder.append("        WHERE deliveredproduct.batch_id = batch.id ");
        queryBuilder.append("        AND delivery.state NOT IN ('04declined', '06received') ");
        queryBuilder.append(") ");
        queryBuilder.append("AND NOT EXISTS ( ");
        queryBuilder.append("        SELECT producedbatch_id FROM advancedgenealogy_trackingrecord trackingrecord ");
        queryBuilder.append("        JOIN orders_order ordersorder ");
        queryBuilder.append("        ON ordersorder.id = trackingrecord.order_id ");
        queryBuilder.append("        WHERE trackingrecord.producedbatch_id = batch.id ");
        queryBuilder.append("        AND ordersorder.state NOT IN ('04completed', '05declined', '07abandoned') ");
        queryBuilder.append(") ");

        jdbcTemplate.update(queryBuilder.toString(), Maps.newHashMap());
    }

}