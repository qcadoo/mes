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
package com.qcadoo.mes.supplyNegotiations;

public interface SupplyNegotiationsColumnLoaderService {

    /**
     * Fill columns for requests table
     * 
     * @param plugin
     *            plugin
     */
    void fillColumnsForRequests(final String plugin);

    /**
     * Clear columns for requests table
     * 
     * @param plugin
     *            plugin
     */
    void clearColumnsForRequests(final String plugin);

    /**
     * Fill columns for requests table
     * 
     * @param plugin
     *            plugin
     */
    void fillColumnsForOffers(final String plugin);

    /**
     * Clear columns for requests table
     * 
     * @param plugin
     *            plugin
     */
    void clearColumnsForOffers(final String plugin);

    /**
     * Check if columns for requests empty
     * 
     * @return boolean
     * 
     */
    boolean isColumnsForRequestsEmpty();

    /**
     * Check if columns for offers empty
     * 
     * @return boolean
     * 
     */
    boolean isColumnsForOffersEmpty();

}
