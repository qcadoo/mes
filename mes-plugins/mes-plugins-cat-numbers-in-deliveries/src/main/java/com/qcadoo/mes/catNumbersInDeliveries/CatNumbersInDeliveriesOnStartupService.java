/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.catNumbersInDeliveries;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.catNumbersInDeliveries.deliveriesColumnExtension.CNIDcolumnLoader;
import com.qcadoo.plugin.api.Module;

@Component
public class CatNumbersInDeliveriesOnStartupService extends Module {

    @Autowired
    private CNIDcolumnLoader cNIDcolumnLoader;

    @Transactional
    @Override
    public void multiTenantEnable() {
        cNIDcolumnLoader.addCNIDcolumnsForDeliveries();
        cNIDcolumnLoader.addCNIDcolumnsForOrders();
    }

    @Transactional
    @Override
    public void multiTenantDisable() {
        cNIDcolumnLoader.deleteCNIDcolumnsForDeliveries();
        cNIDcolumnLoader.deleteCNIDcolumnsForOrders();
    }

}
