/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.orderSupplies.columnExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.orderSupplies.OrderSuppliesColumnLoaderService;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;

@Component
public class OrderSuppliesColumnLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OrderSuppliesColumnLoader.class);

    @Autowired
    private OrderSuppliesColumnLoaderService orderSuppliesColumnLoaderService;

    public void addColumnsForCoverages() {
        if (!orderSuppliesColumnLoaderService.isColumnsForCoveragesEmpty()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for coverages table will be populated ...");
        }

        orderSuppliesColumnLoaderService.fillColumnsForCoverages(OrderSuppliesConstants.PLUGIN_IDENTIFIER);
    }

    public void deleteColumnsForCoverages() {
        if (orderSuppliesColumnLoaderService.isColumnsForCoveragesEmpty()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for coverages table will be unpopulated ...");
        }

        orderSuppliesColumnLoaderService.clearColumnsForCoverages(OrderSuppliesConstants.PLUGIN_IDENTIFIER);
    }

}
