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
package com.qcadoo.mes.materialFlowResources;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MaterialFlowResourcesService {

    List<Entity> getWarehouseLocationsFromDB();

    BigDecimal getResourcesQuantityForLocationAndProduct(final Entity location, final Entity product);

    List<Entity> getResourcesForLocationAndProduct(final Entity location, final Entity product);

    Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location);

    Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location,
                                                              final boolean withoutBlockedForQualityControl);

    Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location,
                                                              final boolean withoutBlockedForQualityControl,
                                                              final String fieldName);

    Map<Long, Map<Long, BigDecimal>> getQuantitiesForProductsAndLocations(final List<Entity> products,
                                                                          final List<Entity> locations);


    BigDecimal getBatchesQuantity(final Collection<Entity> batches, final Entity product,
                                  final Entity location);

    void fillUnitFieldValues(final ViewDefinitionState view);

    void fillCurrencyFieldValues(final ViewDefinitionState view);

    Optional<Entity> findStorageLocationForProduct(final Entity location, final Entity product);

    String getTypeOfPalletByPalletNumber(final Long locationId, final String palletNumberNumber);

}
