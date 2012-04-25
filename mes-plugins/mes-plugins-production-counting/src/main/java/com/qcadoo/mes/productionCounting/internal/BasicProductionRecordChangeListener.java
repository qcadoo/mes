/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.states.ChangeRecordStateMessage;
import com.qcadoo.mes.productionCounting.internal.states.RecordStateListener;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BasicProductionRecordChangeListener extends RecordStateListener {

    private static final String PRODUCT_L = "product";

    private static final String ORDER_FIELD = "order";

    private static final String L_USED_QUANTITY = "usedQuantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Override
    public List<ChangeRecordStateMessage> onAccepted(final Entity productionRecord, final Entity prevState) {
        updateBasicProductionCounting(productionRecord, new Addition());
        setOrderDoneQuantity(productionRecord);
        return super.onAccepted(productionRecord, prevState);
    }

    private void setOrderDoneQuantity(final Entity productionRecord) {
        final Entity order = productionRecord.getBelongsToField(ORDER_FIELD);

        Entity product = order.getBelongsToField(PRODUCT_L);
        product = product.getDataDefinition().get(product.getId());

        final List<Entity> basicProductionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(ORDER_FIELD, order)).add(SearchRestrictions.belongsTo(PRODUCT_L, product))
                .list().getEntities();

        BigDecimal producedQuantity = BigDecimal.ZERO;

        for (Entity basicProductionCounting : basicProductionCountings) {
            BigDecimal qty = (BigDecimal) basicProductionCounting.getField("producedQuantity");
            if (qty == null) {
                qty = BigDecimal.ZERO;
            }
            producedQuantity = producedQuantity.add(qty, numberService.getMathContext());
        }

        order.setField("doneQuantity", producedQuantity);

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

    private Entity getBasicProductionCounting(final Entity productIn, final List<Entity> productionCountings) {
        Entity product = productIn.getBelongsToField(PRODUCT_L);

        for (Entity productionCounting : productionCountings) {
            if (productionCounting.getBelongsToField(PRODUCT_L).getId().equals(product.getId())) {
                return productionCounting;
            }
        }

        throw new IllegalStateException("No basic production counting found for product");
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

            return value.add(add, numberService.getMathContext());
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
            return value.subtract(sub, numberService.getMathContext());
        }

    }

    private void updateBasicProductionCounting(final Entity productionRecord, final Operation operation) {
        final Entity order = productionRecord.getBelongsToField(ORDER_FIELD);

        final List<Entity> basicProductionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(ORDER_FIELD, order)).list().getEntities();

        final List<Entity> productsIn = productionRecord.getHasManyField("recordOperationProductInComponents");
        final List<Entity> productsOut = productionRecord.getHasManyField("recordOperationProductOutComponents");

        for (Entity productIn : productsIn) {
            Entity basicProductionCounting;

            try {
                basicProductionCounting = getBasicProductionCounting(productIn, basicProductionCountings);
            } catch (IllegalStateException e) {
                continue;
            }

            final BigDecimal usedQuantity = (BigDecimal) basicProductionCounting.getField(L_USED_QUANTITY);
            final BigDecimal productQuantity = (BigDecimal) productIn.getField(L_USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);
            basicProductionCounting.setField(L_USED_QUANTITY, result);
            basicProductionCounting = basicProductionCounting.getDataDefinition().save(basicProductionCounting);
        }

        for (Entity productOut : productsOut) {
            Entity productionCounting;

            try {
                productionCounting = getBasicProductionCounting(productOut, basicProductionCountings);
            } catch (IllegalStateException e) {
                continue;
            }

            final BigDecimal usedQuantity = (BigDecimal) productionCounting.getField("producedQuantity");
            final BigDecimal productQuantity = (BigDecimal) productOut.getField(L_USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);
            productionCounting.setField("producedQuantity", result);
            productionCounting = productionCounting.getDataDefinition().save(productionCounting);
        }
    }
}
