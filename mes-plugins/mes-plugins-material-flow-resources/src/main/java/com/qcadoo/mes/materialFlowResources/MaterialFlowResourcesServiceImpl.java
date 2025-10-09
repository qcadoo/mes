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

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceStockDtoFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.*;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.*;

@Service
public class MaterialFlowResourcesServiceImpl implements MaterialFlowResourcesService {

    private static final String L_PRICE_CURRENCY = "priceCurrency";

    private static final String L_QUANTITY_UNIT = "quantityUNIT";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private UnitConversionService unitConversionService;

    @Override
    public List<Entity> getWarehouseLocationsFromDB() {
        return getLocationDD().find().list().getEntities();
    }

    @Override
    public BigDecimal getResourcesQuantityForLocationAndProduct(final Entity location, final Entity product) {
        List<Entity> resources = getResourcesForLocationAndProduct(location, product);

        if (Objects.isNull(resources)) {
            return null;
        } else {
            BigDecimal resourcesQuantity = BigDecimal.ZERO;

            for (Entity resource : resources) {
                resourcesQuantity = resourcesQuantity.add(resource.getDecimalField(ResourceFields.QUANTITY),
                        numberService.getMathContext());
            }

            return resourcesQuantity;
        }
    }

    @Override
    public List<Entity> getResourcesForLocationAndProduct(final Entity location, final Entity product) {
        return getResourceDD().find().add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, location))
                .add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, product))
                .addOrder(SearchOrders.asc(ResourceFields.TIME)).list().getEntities();
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products,
                                                                     final Entity location) {
        return getQuantitiesForProductsAndLocation(products, location, false, ResourceStockDtoFields.AVAILABLE_QUANTITY);
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location,
                                                                     final boolean includeReservedQuantities) {
        return getQuantitiesForProductsAndLocation(products, location, false, ResourceStockDtoFields.AVAILABLE_QUANTITY,
                includeReservedQuantities);
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location,
                                                                     final boolean withoutBlockedForQualityControl,
                                                                     final String fieldName) {
        return getQuantitiesForProductsAndLocation(products, location, withoutBlockedForQualityControl, fieldName, false);
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, Entity location,
                                                                     final boolean withoutBlockedForQualityControl,
                                                                     final String fieldName,
                                                                     final boolean includeReservedQuantities) {
        Map<Long, BigDecimal> quantities = Maps.newHashMap();

        if (!products.isEmpty()) {
            List<Integer> productIds = products.stream().map(product -> product.getId().intValue()).collect(Collectors.toList());
            Integer locationId = location.getId().intValue();

            StringBuilder query = new StringBuilder();

            query.append("SELECT resourceStockDto.product_id AS product_id, ");
            if (withoutBlockedForQualityControl) {
                query.append("(resourceStockDto.quantity - resourceStockDto.blockedQuantity) AS quantity, ");
            } else {
                query.append("resourceStockDto.quantity AS quantity, ");
            }
            query.append("resourceStockDto.availableQuantity AS availableQuantity ");
            query.append("FROM #materialFlowResources_resourceStockDto resourceStockDto ");
            query.append("WHERE resourceStockDto.product_id IN (:productIds) ");
            query.append("AND resourceStockDto.location_id = :locationId ");

            SearchQueryBuilder searchQueryBuilder = getResourceStockDtoDD().find(query.toString());

            searchQueryBuilder.setParameterList("productIds", productIds);
            searchQueryBuilder.setParameter("locationId", locationId);

            List<Entity> resourceStocks = searchQueryBuilder.list().getEntities();

            resourceStocks.forEach(resourceStock -> quantities.put(
                    (long) resourceStock.getIntegerField(ResourceStockDtoFields.PRODUCT_ID),
                    ResourceStockDtoFields.AVAILABLE_QUANTITY.equals(fieldName)
                            ? resourceStock.getDecimalField(ResourceStockDtoFields.AVAILABLE_QUANTITY)
                            : resourceStock.getDecimalField(ResourceStockDtoFields.QUANTITY)));
        }

        return quantities;
    }

    @Override
    public Map<Long, Map<Long, BigDecimal>> getQuantitiesForProductsAndLocations(final List<Entity> products,
                                                                                 final List<Entity> locations) {
        Map<Long, Map<Long, BigDecimal>> quantities = Maps.newHashMap();

        for (Entity location : locations) {
            quantities.put(location.getId(), getQuantitiesForProductsAndLocation(products, location));
        }

        return quantities;
    }

    @Override
    public BigDecimal getBatchesQuantity(final Collection<Entity> batches, final Entity product,
                                         final Entity location) {
        return getBatchesQuantity(batches, product, location, false);
    }

    @Override
    public BigDecimal getBatchesQuantity(final Collection<Entity> batches, final Entity product, final Entity location,
                                         final boolean includeReservedQuantities) {
        BigDecimal batchesQuantity = BigDecimal.ZERO;

        if (!batches.isEmpty()) {
            SearchCriteriaBuilder searchCriteriaBuilder =
                    getResourceDD().find()
                            .createAlias(ResourceFields.PRODUCT, ResourceFields.PRODUCT, JoinType.LEFT)
                            .createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.LEFT)
                            .createAlias(ResourceFields.BATCH, ResourceFields.BATCH, JoinType.LEFT)
                            .add(SearchRestrictions.eq(ResourceFields.PRODUCT + "." + "id", product.getId()))
                            .add(SearchRestrictions.eq(ResourceFields.LOCATION + "." + "id", location.getId()))
                            .add(SearchRestrictions.in(ResourceFields.BATCH + "." + "id", batches.stream().map(Entity::getId).collect(Collectors.toList())))
                            .setProjection(list().add(alias(sum(ResourceFields.AVAILABLE_QUANTITY), "sum")).add(rowCount()))
                            .addOrder(asc("sum"));

            Entity resource = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

            if (Objects.nonNull(resource)) {
                batchesQuantity = resource.getDecimalField("sum");
            }
        }

        return batchesQuantity;
    }

    public void fillUnitFieldValues(final ViewDefinitionState view) {
        Long productId = (Long) view.getComponentByReference(ResourceFields.PRODUCT).getFieldValue();

        if (Objects.isNull(productId)) {
            return;
        }

        Entity product = getProductDD().get(productId);
        String unit = product.getStringField(UNIT);

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_QUANTITY_UNIT);
        unitField.setFieldValue(unit);
        unitField.requestComponentUpdateState();
    }

    public void fillCurrencyFieldValues(final ViewDefinitionState view) {
        String currency = currencyService.getCurrencyAlphabeticCode();

        FieldComponent currencyField = (FieldComponent) view.getComponentByReference(L_PRICE_CURRENCY);
        currencyField.setFieldValue(currency);
        currencyField.requestComponentUpdateState();
    }

    public Optional<Entity> findStorageLocationForProduct(final Entity location, final Long productId) {
        SearchQueryBuilder scb = getStorageLocationDD().find("SELECT sl FROM #materialFlowResources_storageLocation AS sl JOIN sl.products p WHERE sl.location = :locationId AND p.id = :productId");

        scb.setLong("locationId", location.getId());
        scb.setLong("productId", productId);

        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

    public Long getTypeOfLoadUnitByPalletNumber(final Long locationId, final String palletNumberNumber) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT resource.typeofloadunit_id ");
        query.append("FROM materialflowresources_resource resource ");
        query.append("LEFT JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = resource.palletnumber_id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND resource.location_id = :locationId ");
        query.append("LIMIT 1");

        Map<String, Object> params = Maps.newHashMap();

        params.put("locationId", locationId);
        params.put("palletNumberNumber", palletNumberNumber);

        try {
            return jdbcTemplate.queryForObject(query.toString(), params, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public BigDecimal getConversion(final Entity product, String unit, String additionalUnit, BigDecimal dbConversion) {
        BigDecimal conversion = BigDecimal.ONE;
        if (!unit.equals(additionalUnit)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(additionalUnit)) {
                conversion = unitConversions.asUnitToConversionMap().get(additionalUnit);
            } else {
                conversion = BigDecimal.ZERO;
            }
            if (Objects.nonNull(dbConversion) && dbConversion.compareTo(numberService.setScaleWithDefaultMathContext(conversion)) != 0) {
                conversion = dbConversion;
            }
        }
        return conversion;
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

    private DataDefinition getStorageLocationDD() {
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    private DataDefinition getResourceStockDtoDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO);
    }

}
