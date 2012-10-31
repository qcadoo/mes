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
package com.qcadoo.mes.productionCountingWithCosts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.productionCounting.internal.ProductionCountingGenerateProductionBalance;
import com.qcadoo.plugin.api.Module;

@Component
public class ProductionCountingWithCostsOnStartupService extends Module {

    @Autowired
    private ProductionCountingGenerateProductionBalance productionCountingGenerateProductionBalance;

    @Autowired
    private GenerateProductionBalanceWithCosts generateProductionBalance;

    @Override
    public void enable() {
        productionCountingGenerateProductionBalance.addObserver(generateProductionBalance);
    }

    @Override
    public void enableOnStartup() {
        productionCountingGenerateProductionBalance.addObserver(generateProductionBalance);
    }

    @Override
    public void disable() {
        productionCountingGenerateProductionBalance.deleteObserver(generateProductionBalance);
    }

}
