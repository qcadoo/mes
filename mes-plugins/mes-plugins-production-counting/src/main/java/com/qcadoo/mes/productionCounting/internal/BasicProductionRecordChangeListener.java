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
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BasicProductionRecordChangeListener extends RecordStateListener {

    private static final String FIELD_USED_QUANTITY = "usedQuantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    ProductionRecordService recordService;

    @Override
    public List<ChangeRecordStateMessage> onAccepted(Entity productionRecord, Entity prevState) {
        updateBasicProductionCounting(productionRecord, new Addition());
        return super.onAccepted(productionRecord, prevState);
    }

    @Override
    public List<ChangeRecordStateMessage> onDeclined(final Entity productionRecord, final Entity prevState) {
        if ("02accepted".equals(prevState.getField("state"))) {
            updateBasicProductionCounting(productionRecord, new Substraction());
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
            BigDecimal result;
            if (orginalValue == null && addition == null) {
                result = BigDecimal.ZERO;
            } else if (orginalValue == null && addition != null) {
                result = addition;
            } else if (addition == null && orginalValue != null) {
                result = orginalValue;
            } else {
                result = orginalValue.add(addition);
            }
            return result;
        }

    }

    private class Substraction implements Operation {

        @Override
        public BigDecimal perform(final BigDecimal orginalValue, final BigDecimal substrahend) {
            BigDecimal result;
            if (orginalValue == null && substrahend != null) {
                result = substrahend.negate();
            } else if (substrahend == null && orginalValue != null) {
                result = orginalValue;
            } else if (substrahend == null && orginalValue == null) {
                result = BigDecimal.ZERO;
            } else {
                result = orginalValue.subtract(substrahend);
            }
            return result;
        }

    }

    private void updateBasicProductionCounting(Entity productionRecord, Operation operation) {
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
