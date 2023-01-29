/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.advancedGenealogy;

import java.util.Objects;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.advancedGenealogy.constants.ParameterFieldsAG;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.services.NumberPatternGeneratorService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AdvancedGenealogyService {

    public static final String L_PRODUCT_DELIVERY_BATCH_EVIDENCE = "productDeliveryBatchEvidence";
    public static final String L_PRODUCT_DELIVERY_BATCH_NUMBER_PATTERN = "productDeliveryBatchNumberPattern";
    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberPatternGeneratorService numberPatternGeneratorService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity createOrGetBatch(String number, final Entity product) {
        if (parameterService.getParameter().getBooleanField(
                ParameterFieldsAG.GENERATE_BATCH_FOR_ORDERED_PRODUCT)) {
            number = getNumberFromNumberPattern(product);
        }
        Entity batch = getBatch(number, product);

        if (Objects.isNull(batch)) {
            return createBatch(number, product);
        }

        return batch;
    }

    public Entity createOrGetBatch(String number, final Entity product, final Entity supplier) {
        if (Strings.isNullOrEmpty(number) && parameterService.getParameter().getBooleanField(L_PRODUCT_DELIVERY_BATCH_EVIDENCE)) {
            number = getNumberDeliveryProductFromNumberPattern(product);
        }
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

    public String getNumberFromNumberPattern(Entity product) {
        Entity numberPattern;
        Entity batchNumberPattern = product.getBelongsToField(ProductFields.BATCH_NUMBER_PATTERN);
        Entity parent = product.getBelongsToField(ProductFields.PARENT);
        if (batchNumberPattern != null) {
            numberPattern = batchNumberPattern;
        } else if (parent != null && parent.getBelongsToField(ProductFields.BATCH_NUMBER_PATTERN) != null) {
            numberPattern = parent.getBelongsToField(ProductFields.BATCH_NUMBER_PATTERN);
        } else {
            numberPattern = parameterService.getParameter().getBelongsToField(
                    ParameterFieldsAG.NUMBER_PATTERN);
        }
        return numberPatternGeneratorService.generateNumber(numberPattern);
    }

    public String getNumberDeliveryProductFromNumberPattern(Entity product) {
        Entity numberPattern;
        Entity batchNumberPattern = product.getBelongsToField(ProductFields.BATCH_NUMBER_PATTERN);
        Entity parent = product.getBelongsToField(ProductFields.PARENT);
        if (batchNumberPattern != null) {
            numberPattern = batchNumberPattern;
        } else if (parent != null && parent.getBelongsToField(ProductFields.BATCH_NUMBER_PATTERN) != null) {
            numberPattern = parent.getBelongsToField(ProductFields.BATCH_NUMBER_PATTERN);
        } else {
            numberPattern = parameterService.getParameter().getBelongsToField(
                    L_PRODUCT_DELIVERY_BATCH_NUMBER_PATTERN);
        }
        return numberPatternGeneratorService.generateNumber(numberPattern);
    }

}
