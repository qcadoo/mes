/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
    public void setProduct(final Entity product) {
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
