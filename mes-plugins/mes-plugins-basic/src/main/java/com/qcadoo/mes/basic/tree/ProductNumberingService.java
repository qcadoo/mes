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
package com.qcadoo.mes.basic.tree;

import java.util.List;

import com.qcadoo.model.api.Entity;

public interface ProductNumberingService {

    /**
     * Gets product root
     * 
     * @param product
     *            product
     * 
     * @return root
     * 
     */
    Entity getRoot(final Entity entity);

    /**
     * Generate node number for product
     * 
     * @param product
     *            product
     * 
     */
    void generateNodeNumber(final Entity product);

    /**
     * Updated node number for product
     * 
     * @param product
     *            product
     * 
     */
    void updateNodeNumber(final Entity product);

    /**
     * Checks if product belongs to products family
     * 
     * @return boolean
     */
    boolean checkIfProductBelongsToProductsFamily(final Entity productsFamily, final Entity product);

    /**
     * Get product roots
     * 
     * @return boolean
     */
    List<Entity> getProductRoots(final Entity product);

}
