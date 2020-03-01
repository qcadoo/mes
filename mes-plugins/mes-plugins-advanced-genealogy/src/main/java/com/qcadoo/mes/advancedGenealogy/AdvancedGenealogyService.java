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
package com.qcadoo.mes.advancedGenealogy;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AdvancedGenealogyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity createOrGetBatch(final String number, final Entity product) {
        Entity batch = getBatch(number, product);

        if (Objects.isNull(batch)) {
            return createBatch(number, product);
        }

        return batch;
    }

    public Entity createOrGetBatch(final String number, final Entity product, final Entity supplier) {
        Entity batch = getBatch(number, product, supplier);

        if (Objects.isNull(batch)) {
            return createBatch(number, product, supplier);
        }

        return batch;
    }

    public Entity createBatch(final String number, final Entity product) {
        return createBatch(number, product, null);
    }

    public Entity createBatch(final String number, final Entity product, final Entity supplier) {
        Entity batch = getBatchDD().create();

        batch.setField(BatchFields.NUMBER, number);

        if (Objects.nonNull(product)) {
            batch.setField(BatchFields.PRODUCT, product.getId());
        }
        if (Objects.nonNull(supplier)) {
            batch.setField(BatchFields.SUPPLIER, supplier.getId());
        }

        return batch.getDataDefinition().save(batch);
    }

    private Entity getBatch(final String number, final Entity product) {
        return getBatch(number, product, null);
    }

    private Entity getBatch(final String number, final Entity product, final Entity supplier) {
        SearchCriteriaBuilder searchCriteriaBuilder = getBatchDD().find().add(SearchRestrictions.eq(BatchFields.NUMBER, number));

        if (Objects.nonNull(product)) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(BatchFields.PRODUCT, product));
        }
        if (Objects.nonNull(supplier)) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(BatchFields.SUPPLIER, supplier));
        }

        return searchCriteriaBuilder.setMaxResults(1).uniqueResult();
    }

    public final Entity getTrackingRecord(final Long trackingRecordId) {
        return getTrackingRecordDD().get(trackingRecordId);
    }

    private DataDefinition getBatchDD() {
        return dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH);
    }

    public DataDefinition getTrackingRecordDD() {
        return dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                AdvancedGenealogyConstants.MODEL_TRACKING_RECORD);
    }

}
