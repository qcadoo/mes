/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionCounting.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

<<<<<<< HEAD:mes-plugins/mes-plugins-production-counting/src/main/java/com/qcadoo/mes/productionCounting/validators/ProductionBalanceValidators.java
import com.qcadoo.mes.productionCounting.ProductionCountingService;
=======
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.states.ProductionRecordStatesHelper;
>>>>>>> master:mes-plugins/mes-plugins-production-counting/src/main/java/com/qcadoo/mes/productionCounting/hooks/ProductionRecordModelHooks.java
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionBalanceValidators {

    @Autowired
<<<<<<< HEAD:mes-plugins/mes-plugins-production-counting/src/main/java/com/qcadoo/mes/productionCounting/validators/ProductionBalanceValidators.java
    private ProductionCountingService productionCountingService;

    public boolean validatesWith(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        return validateOrder(productionBalanceDD, productionBalance);
    }

    private boolean validateOrder(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        return productionCountingService.validateOrder(productionBalanceDD, productionBalance);
=======
    private ProductionRecordStatesHelper statesHelper;

    public void setInitialState(final DataDefinition productionRecordDD, final Entity productionRecord) {
        productionRecord.setField(ProductionRecordFields.IS_EXTERNAL_SYNCHRONIZED, true);
        statesHelper.setInitialState(productionRecord);
>>>>>>> master:mes-plugins/mes-plugins-production-counting/src/main/java/com/qcadoo/mes/productionCounting/hooks/ProductionRecordModelHooks.java
    }

}
