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
package com.qcadoo.mes.orderSupplies;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.List;

public interface OrderSuppliesService {

    /**
     * Gets material requirement coverage
     * 
     * @param materialRequirementCoverageId
     * 
     * @return material requirement coverage
     */
    Entity getMaterialRequirementCoverage(final Long materialRequirementCoverageId);

    /**
     * Gets coverage location
     * 
     * @param coverageLocationId
     * 
     * @return coverage location
     */
    Entity getCoverageLocation(final Long coverageLocationId);

    /**
     * Gets coverage product
     * 
     * @param coverageProductId
     * 
     * @return coverage product
     */
    Entity getCoverageProduct(final Long coverageProductId);

    /**
     * Gets coverage product logging
     * 
     * @param coverageProductLoggingId
     * 
     * @return coverage product logging
     */
    Entity getCoverageProductLogging(final Long coverageProductLoggingId);

    /**
     * Gets material requirement coverage data definition
     * 
     * @return material requirement coverage data definition
     */
    DataDefinition getMaterialRequirementCoverageDD();

    /**
     * Gets coverage location data definition
     * 
     * @return coverage location data definition
     */
    DataDefinition getCoverageLocationDD();

    /**
     * Gets coverage location data definition
     *
     * @return coverage location data definition
     */
    DataDefinition getCoverageOrderStateDD();

    /**
     * Gets coverage product data definition
     * 
     * @return coverage product data definition
     */
    DataDefinition getCoverageProductDD();

    /**
     * Gets coverage product logging data definition
     * 
     * @return coverage product logging data definition
     */
    DataDefinition getCoverageProductLoggingDD();

    /**
     * Gets list of columns for coverages
     * 
     * @return list of columns for coverages
     */
    List<Entity> getColumnsForCoverages();

    /**
     * Gets column for coverages data definition
     * 
     * @return column for coverages data definition
     */
    DataDefinition getColumnForCoveragesDD();

    /**
     * Checks if material requirement coverage is saved
     * 
     * @param materialRequirementCoverageId
     * 
     * @return boolean
     */
    boolean checkIfMaterialRequirementCoverageIsSaved(final Long materialRequirementCoverageId);

    /**
     * Deletes unsaved material requirement coverages if not modified since 24 h
     * 
     */
    void deleteUnsavedMaterialRequirementCoveragesTrigger();

    void deleteMaterialRequirementCoverageAndReferences(final List<Long> idsList);

    void clearMaterialRequirementCoverage(final Long id);

}
