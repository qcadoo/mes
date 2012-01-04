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
package com.qcadoo.mes.productionCounting.internal.orderStates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.orders.states.OrderStatesChangingService;
import com.qcadoo.mes.productionCounting.internal.BasicProductionRecordChangeListener;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStatesChangingService;
import com.qcadoo.plugin.api.Module;

@Component
public class ProductionCountingOnStartupService extends Module {

    @Autowired
    private OrderStatesChangingService orderStatesChangingService;

    @Autowired
    private ProductionCountingOrderStatesListener productionCountingOrderStatesListener;

    @Autowired
    private BasicProductionRecordChangeListener changeListener;

    @Autowired
    private ProductionCountingStatesChangingService changingService;

    @Override
    public void enable() {
        orderStatesChangingService.addOrderStateListener(productionCountingOrderStatesListener);
        changingService.addRecordStateListener(changeListener);
    }

    @Override
    public void disable() {
        orderStatesChangingService.removeOrderStateListener(productionCountingOrderStatesListener);
        changingService.removeRecordStateListener(changeListener);
    }
}
