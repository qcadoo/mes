package com.qcadoo.mes.technologies.tree.builder;

import java.math.BigDecimal;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.tree.builder.api.InternalOperationProductComponent;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationProductComponentImpl implements InternalOperationProductComponent {

    private final Entity entity;

    private final OperationCompType operationType;

    public OperationProductComponentImpl(final OperationCompType operationType, final DataDefinition opcDataDef) {
        this.entity = opcDataDef.create();
        this.operationType = operationType;
    }

    @Override
    public void setField(final String name, final Object value) {
        entity.setField(name, value);
    }

    @Override
    public Entity getWrappedEntity() {
        return entity.copy();
    }

    @Override
    public void setProduct(final Entity product) throws IllegalArgumentException {
        Preconditions.checkArgument(hasCorrectProductDataDefinition(product));
        entity.setField(operationType.getProductFieldName(), product);
    }

    private boolean hasCorrectProductDataDefinition(final Entity product) {
        DataDefinition dataDef = product.getDataDefinition();
        return BasicConstants.MODEL_PRODUCT.equals(dataDef.getName())
                && BasicConstants.PLUGIN_IDENTIFIER.equals(dataDef.getPluginIdentifier());
    }

    @Override
    public void setQuantity(final BigDecimal quantity) {
        entity.setField(operationType.getQuantityFieldName(), quantity);
    }

}
