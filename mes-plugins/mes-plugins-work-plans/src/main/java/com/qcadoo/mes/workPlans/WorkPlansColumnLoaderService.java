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
package com.qcadoo.mes.workPlans;

public interface WorkPlansColumnLoaderService {

    /**
     * Sets parameter default values
     */
    void setParameterDefaultValues();

    /**
     * Sets operation default values
     */
    void setOperationDefaultValues();

    /**
     * Sets technology operation component default values
     */
    void setTechnologyOperationComponentDefaultValues();

    /**
     * Fills columns for orders
     * 
     * @param plugin
     *            plugin
     */
    void fillColumnsForOrders(final String plugin);

    /**
     * Clears columns for orders
     * 
     * @param plugin
     *            plugin
     */
    void clearColumnsForOrders(final String plugin);

    /**
     * Fills columns for products
     * 
     * @param plugin
     *            plugin
     */
    void fillColumnsForProducts(final String plugin);

    /**
     * Clears columns for products
     * 
     * @param plugin
     *            plugin
     */
    void clearColumnsForProducts(final String plugin);

    /**
     * Is columns for orders empty
     * 
     * @return boolean
     */
    boolean isColumnsForOrdersEmpty();

    /**
     * Is columns for products empty
     * 
     * @return
     */
    boolean isColumnsForProductsEmpty();

}
