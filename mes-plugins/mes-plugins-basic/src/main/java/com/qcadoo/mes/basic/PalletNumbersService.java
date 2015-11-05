/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.basic;

import java.util.List;
import java.util.Set;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public interface PalletNumbersService {

    /**
     * Creates pallet number
     *
     * @param number
     *            Number
     *
     * @return palletNumber
     *
     */
    Entity createPalletNumber(final String number);

    /**
     * Creates pallet numbers
     *
     * @param numbers
     *            Numbers
     *
     * @return palletNumbers
     *
     */
    List<Entity> createPalletNumbers(final List<String> numbers);

    /**
     * Creates pallet number helper
     *
     * @param quantity
     *            Quantity
     * 
     * @param temporary
     *            Temporary
     *
     * @param palletNumbers
     *            Pallet Numbers
     *
     * @return palletNumber
     *
     */
    Entity createPalletNumberHelper(final Integer quantity, final boolean temporary, final List<Entity> palletNumbers);

    /**
     * Gets pallet number entity
     *
     * @param palletNumberId
     *            Pallet Number Id
     *
     * @return palletNumber
     *
     */
    Entity getPalletNumber(final Long palletNumberId);

    /**
     * Gets pallet number entities
     *
     * @param palletNumberIds
     *            Pallet Number Ids
     *
     * @return palletNumbers
     *
     */
    List<Entity> getPalletNumbers(final Set<Long> palletNumberIds);

    /***
     * Gets numbers from pallet numbers
     *
     * @param palletNumbers
     *            Pallet Numbers
     *
     * @return numbers
     *
     */
    List<String> getNumbers(final List<Entity> palletNumbers);

    /**
     * Gets pallet number data definition
     *
     * @return palletNumberDD
     *
     */
    DataDefinition getPalletNumberDD();

    /**
     * Gets pallet number helper entity
     *
     * @param palletNumberHelperId
     *            Pallet Number Helper Id
     *
     * @return palletNumberHelper
     *
     */
    Entity getPalletNumberHelper(final Long palletNumberHelperId);

    /***
     * Gets pallet number helper data definition
     *
     * @return palletNumberHelperDD
     *
     */
    DataDefinition getPalletNumberHelperDD();


    /**
     * Deletes temporary pallet number helpers
     *
     */
    void deleteTemporaryPalletNumberHelpersTrigger();

}
