/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
package com.qcadoo.mes.productionCounting.internal;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.states.ChangeRecordStateMessage;
import com.qcadoo.mes.productionCounting.internal.states.RecordStateListener;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BasicProductionRecordChangeListener extends RecordStateListener {

    private static final String FIELD_USED_QUANTITY = "usedQuantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<ChangeRecordStateMessage> onAccepted(final Entity productionRecord, final Entity prevState) {
        updateBasicProductionCounting(productionRecord, new Addition());
        setOrderDoneQuantity(productionRecord);
        return super.onAccepted(productionRecord, prevState);
    }

    private void setOrderDoneQuantity(Entity productionRecord) {
        final Entity order = productionRecord.getBelongsToField("order");
        final Entity product = order.getBelongsToField("product");

        final List<Entity> productionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.belongsTo("product", product)).list()
                .getEntities();

        Preconditions.checkArgument(productionCountings.size() == 1,
                "There is more than one production counting for same order and product");

        final Entity productionCounting = productionCountings.get(0);

        order.setField("doneQuantity", productionCounting.getField("producedQuantity"));

        order.getDataDefinition().save(order);

    }

    @Override
    public List<ChangeRecordStateMessage> onDeclined(final Entity productionRecord, final Entity prevState) {
        if ("02accepted".equals(prevState.getField("state"))) {
            updateBasicProductionCounting(productionRecord, new Substraction());
            setOrderDoneQuantity(productionRecord);
        }
        return super.onDeclined(productionRecord, prevState);
    }

    private Entity getProductCount(final Entity productIn, final List<Entity> productionCountings) {
        Entity product = productIn.getBelongsToField("product");

        product = product.getDataDefinition().get(product.getId());

        for (Entity productionCounting : productionCountings) {
            if (productionCounting.getBelongsToField("product").getId().equals(product.getId())) {
                return productionCounting;
            }
        }
        throw new IllegalStateException("No material requirement found for product");
    }

    private interface Operation {

        BigDecimal perform(BigDecimal argument1, BigDecimal argument2);
    }

    private class Addition implements Operation {

        @Override
        public BigDecimal perform(final BigDecimal orginalValue, final BigDecimal addition) {
            BigDecimal value;
            BigDecimal add;
            if (orginalValue == null) {
                value = BigDecimal.ZERO;
            } else {
                value = orginalValue;
            }
            if (addition == null) {
                add = BigDecimal.ZERO;
            } else {
                add = addition;
            }

            return value.add(add);
        }

    }

    private class Substraction implements Operation {

        @Override
        public BigDecimal perform(final BigDecimal orginalValue, final BigDecimal substrahend) {
            BigDecimal value;
            BigDecimal sub;
            if (orginalValue == null) {
                value = BigDecimal.ZERO;
            } else {
                value = orginalValue;
            }

            if (substrahend == null) {
                sub = BigDecimal.ZERO;
            } else {
                sub = substrahend;
            }
            return value.subtract(sub);

        }

    }

    private void updateBasicProductionCounting(final Entity productionRecord, final Operation operation) {
        final Entity order = productionRecord.getBelongsToField("order");

        final List<Entity> productionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        final List<Entity> productsIn = productionRecord.getHasManyField("recordOperationProductInComponents");
        final List<Entity> productsOut = productionRecord.getHasManyField("recordOperationProductOutComponents");

        for (Entity productIn : productsIn) {
            Entity productionCounting = getProductCount(productIn, productionCountings);
            final BigDecimal usedQuantity = (BigDecimal) productionCounting.getField(FIELD_USED_QUANTITY);
            final BigDecimal productQuantity = (BigDecimal) productIn.getField(FIELD_USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);
            productionCounting.setField(FIELD_USED_QUANTITY, result);
            productionCounting = productionCounting.getDataDefinition().save(productionCounting);
            if (!productionCounting.isValid()) {
                throw new IllegalStateException("Saved entity is invalid");
            }
        }

        for (Entity productOut : productsOut) {
            Entity productionCounting = getProductCount(productOut, productionCountings);
            final BigDecimal usedQuantity = (BigDecimal) productionCounting.getField("producedQuantity");
            final BigDecimal productQuantity = (BigDecimal) productOut.getField(FIELD_USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);
            productionCounting.setField("producedQuantity", result);
            productionCounting = productionCounting.getDataDefinition().save(productionCounting);
            if (!productionCounting.isValid()) {
                throw new IllegalStateException("Saved entity is invalid");
            }
        }
    }
}
