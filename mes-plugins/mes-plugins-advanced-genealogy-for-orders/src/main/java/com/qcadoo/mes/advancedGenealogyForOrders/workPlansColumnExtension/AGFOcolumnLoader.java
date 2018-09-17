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
package com.qcadoo.mes.advancedGenealogyForOrders.workPlansColumnExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.advancedGenealogyForOrders.constants.AdvancedGenealogyForOrdersConstants;
import com.qcadoo.mes.workPlans.WorkPlansColumnLoaderService;

@Component
public class AGFOcolumnLoader {

    @Autowired
    private WorkPlansColumnLoaderService workPlansColumnLoaderService;

    private static final Logger LOG = LoggerFactory.getLogger(AGFOcolumnLoader.class);

    public void addAGFOcolumnsForOrders() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for orders table will be populated ...");
        }

        workPlansColumnLoaderService.fillColumnsForOrders(AdvancedGenealogyForOrdersConstants.PLUGIN_IDENTIFIER);
    }

    public void deleteAGFOcolumnsForOrders() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for orders table will be unpopulated ...");
        }

        workPlansColumnLoaderService.clearColumnsForOrders(AdvancedGenealogyForOrdersConstants.PLUGIN_IDENTIFIER);
    }

    public void addAGFOcolumnsForProducts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for input and output products table will be populated ...");
        }

        workPlansColumnLoaderService.fillColumnsForProducts(AdvancedGenealogyForOrdersConstants.PLUGIN_IDENTIFIER);
    }

    public void deleteAGFOcolumnsForProducts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for input and output products table will be unpopulated ...");
        }

        workPlansColumnLoaderService.clearColumnsForProducts(AdvancedGenealogyForOrdersConstants.PLUGIN_IDENTIFIER);
    }
}
