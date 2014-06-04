/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.orders;

import java.util.Locale;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface OrderService {

    /**
     * Gets order
     * 
     * @param orderId
     *            order id
     * 
     * @return order
     */
    Entity getOrder(final Long orderId);

    /**
     * Is order started
     * 
     * @param state
     *            state
     * 
     * @return boolean
     */
    boolean isOrderStarted(final String state);

    /**
     * Gets default production line
     * 
     * @return defaultProductionLine
     */
    Entity getDefaultProductionLine();

    /**
     * Makes default name
     * 
     * @param product
     *            product
     * @param technology
     *            technology
     * @param locale
     *            locale
     * 
     * @return defaultName
     */
    String makeDefaultName(final Entity product, Entity technology, final Locale locale);

    /**
     * Changes field state
     * 
     * @param view
     *            view
     * @param booleanFieldComponentName
     *            boolean field component name
     * @param fieldComponentName
     *            field component name
     */
    void changeFieldState(final ViewDefinitionState view, final String booleanFieldComponentName, final String fieldComponentName);

    /**
     * Checks component order has technology
     * 
     * @param dataDefinition
     *            data definition
     * 
     * @param entity
     *            entity
     * 
     * @return boolean
     */
    boolean checkComponentOrderHasTechnology(final DataDefinition dataDefinition, final Entity entity);

    /**
     * Checks autogenealogy required
     * 
     * @return boolean
     */
    boolean checkAutogenealogyRequired();

    /**
     * Checks required batch
     * 
     * @param order
     *            order
     * 
     * @return boolean
     */
    boolean checkRequiredBatch(final Entity order);

}
