/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.constants.UnitConversionItemFields;

@Service
public class UnitConversionItemValidatorsB {

    public boolean validateUnitOnConversionWithProduct(final DataDefinition dataDefinition, final Entity unitConversionItem) {
        final Entity product = unitConversionItem.getBelongsToField(UnitConversionItemFieldsB.PRODUCT);
        final String unitFrom = unitConversionItem.getStringField(UnitConversionItemFields.UNIT_FROM);

        if (product == null || product.getStringField(ProductFields.UNIT).equals(unitFrom)) {
            return true;
        }

        final String errorMsg = "basic.product.validateError.unitFrom.doesNotMatchUnitConversionItem";
        unitConversionItem.addError(dataDefinition.getField(UnitConversionItemFields.UNIT_FROM), errorMsg);

        return false;
    }

}
