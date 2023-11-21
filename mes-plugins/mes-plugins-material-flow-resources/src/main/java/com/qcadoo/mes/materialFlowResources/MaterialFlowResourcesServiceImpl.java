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
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceStockDtoFields;
import com.qcadoo.mes.materialFlowResources.dto.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

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
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location) {
        return getQuantitiesForProductsAndLocation(products, location, false);
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location,
                                                                     final boolean withoutBlockedForQualityControl) {
        return getQuantitiesForProductsAndLocation(products, location, withoutBlockedForQualityControl,
                ResourceStockDtoFields.AVAILABLE_QUANTITY);
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location,
                                                                     final boolean withoutBlockedForQualityControl, final String fieldName) {
        Map<Long, BigDecimal> quantities = Maps.newHashMap();

        if (products.size() > 0) {
            List<Integer> productIds = products.stream().map(product -> product.getId().intValue()).collect(Collectors.toList());
            Integer locationId = location.getId().intValue();

            StringBuilder query = new StringBuilder();

            query.append("SELECT ");
            query.append("resourceStockDto.product_id AS product_id, resourceStockDto.quantity AS quantity, resourceStockDto.availableQuantity AS availableQuantity ");
            query.append("FROM #materialFlowResources_resourceStockDto resourceStockDto ");
            query.append("WHERE resourceStockDto.product_id IN (:productIds) ");
            query.append("AND resourceStockDto.location_id = :locationId ");

            if (withoutBlockedForQualityControl) {
                query.append("AND resourceStockDto.blockedForQualityControl = false ");
            }

            SearchQueryBuilder searchQueryBuilder = getResourceStockDtoDD().find(query.toString());

            searchQueryBuilder.setParameterList("productIds", productIds);
            searchQueryBuilder.setParameter("locationId", locationId);

            List<Entity> resourceStocks = searchQueryBuilder.list().getEntities();

            resourceStocks.forEach(resourceStock -> quantities.put(
                    Long.valueOf(resourceStock.getIntegerField(ResourceStockDtoFields.PRODUCT_ID).intValue()),
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

    @Override
    public List<QuantityDto> getQuantitiesForProductsAndLocationWMS(final List<String> productNumbers, final Long materialFlowLocationId) {
        List<QuantityDto> quantityDtoList = new ArrayList<>();
        Map<String, Object> params = Maps.newHashMap();

        if (!productNumbers.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("storageLocationDto.productNumber AS productNumber, storageLocationDto.resourceQuantity AS quantity, storageLocationDto.quantityInAdditionalUnit AS additionalQuantity ");
            prepareQuery.append("FROM materialFlowResources_storageLocationDto AS storageLocationDto ");
            prepareQuery.append("WHERE storageLocationDto.productNumber IN (:productNumbers) ");
            prepareQuery.append("AND storageLocationDto.location_id = :materialFlowLocationId ");

            params.put("productNumbers", productNumbers);
            params.put("materialFlowLocationId", materialFlowLocationId.intValue());

            quantityDtoList = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(QuantityDto.class));


            return quantityDtoList;
        }
        return quantityDtoList;
    }

    @Override
    public List<ResourcesQuantityDto> getResourceQuantities(final Long storageLocationId, final String productNumber) {
        List<ResourcesQuantityDto> resourcesQuantityDtoList = new ArrayList<>();
        Map<String, Object> params = Maps.newHashMap();

        if (storageLocationId != null || productNumber != null || !productNumber.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT DISTINCT ");
            prepareQuery.append("resourceDto.number AS resourceNumber, ");
            prepareQuery.append("resourceDto.quantity AS quantity, ");
            prepareQuery.append("resourceDto.quantityInAdditionalUnit AS additionalQuantity, ");
            prepareQuery.append("internal.productUnit AS productUnit, ");
            prepareQuery.append("internal.productAdditionalUnit AS productAdditionalUnit ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS resourceDto ");
            prepareQuery.append("JOIN materialFlowResources_storageLocationDto_internal AS internal ");
            prepareQuery.append("ON resourceDto.productNumber = internal.productNumber ");
            prepareQuery.append("WHERE resourceDto.productNumber = :productNumber ");
            prepareQuery.append("AND resourceDto.location_id = :storageLocationId");

            params.put("storageLocationId", storageLocationId);
            params.put("productNumber", productNumber);

            resourcesQuantityDtoList = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(ResourcesQuantityDto.class));

            return resourcesQuantityDtoList;
        }
        return resourcesQuantityDtoList;
    }


    @Override
    public List<PalletNumberProductDTO> getProductsForPalletNumber(String palletNumber, List<String> userLocationNumbers) {
        List<PalletNumberProductDTO> palletNumberProductDTOList = new ArrayList<>();
        Map<String, Object> params = Maps.newHashMap();

        if (palletNumber != null || !palletNumber.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("palletStorageDto.storageLocationNumber AS storageLocationNumber, ");
            prepareQuery.append("palletStorageDto.locationNumber AS locationNumber, ");
            prepareQuery.append("resourceStockDto.product_id AS productId, ");
            prepareQuery.append("resourceStockDto.productNumber AS productNumber, ");
            prepareQuery.append("resourceStockDto.productName AS productName, ");
            prepareQuery.append("resourceStockDto.productUnit AS productUnit, ");
            prepareQuery.append("storageLocationDto.productAdditionalUnit AS productAdditionalUnit, ");
            prepareQuery.append("resourceStockDto.quantity AS quantity, ");
            prepareQuery.append("resourceStockDto.quantityInAdditionalUnit AS quantityInAdditionalUnit, ");
            prepareQuery.append("resourceStockDto.location_id AS locationId ");
            prepareQuery.append("FROM materialFlowResources_resourceStockDto AS resourceStockDto ");
            prepareQuery.append("JOIN materialFlowResources_palletStorageStateDto AS palletStorageDto ");
            prepareQuery.append("ON palletStorageDto.location_id = resourceStockDto.location_id ");
            prepareQuery.append("JOIN materialFlowResources_storageLocationDto AS storageLocationDto ");
            prepareQuery.append("ON palletStorageDto.location_id = storageLocationDto.location_id ");
            prepareQuery.append("WHERE palletStorageDto.palletNumber = :palletNumber ");
            prepareQuery.append("AND resourceStockDto.locationNumber IN (:userLocationNumbers)");

            params.put("palletNumber", palletNumber);
            params.put("userLocationNumbers", userLocationNumbers);

            palletNumberProductDTOList = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(PalletNumberProductDTO.class));
            return palletNumberProductDTOList;
        }
        return palletNumberProductDTOList;
    }

    @Override
    public List<SumOfProductsDto> getSumOfProducts(String productNumber, List<String> locationNumbers) {
        List<SumOfProductsDto> list = new ArrayList<>();
        Map<String, Object> params = Maps.newHashMap();

        if (productNumber != null || !productNumber.isEmpty() || !locationNumbers.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("SUM (mfr.quantity) AS quantitySum, ");
            prepareQuery.append("SUM (mfr.quantityInAdditionalUnit) AS additionalQuantitySum ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS mfr ");
            prepareQuery.append("WHERE mfr.productNumber = :productNumber ");
            prepareQuery.append("AND mfr.locationNumber IN (:locationNumbers)");

            params.put("productNumber", productNumber);
            params.put("locationNumbers", locationNumbers);

            list = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(SumOfProductsDto.class));
            return list;
        }
        return list;
    }

    @Override
    public List<StorageLocationsForProductDto> getStoragesForProductNumber(String productNumber, List<String> locationNumbers) {
        List<StorageLocationsForProductDto> list = new ArrayList<>();
        Map<String, Object> params = Maps.newHashMap();

        if (productNumber != null || !productNumber.isEmpty() || !locationNumbers.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT DISTINCT ");
            prepareQuery.append("mfrs.location_id AS locationId, ");
            prepareQuery.append("mfrs.locationNumber AS locationNumber, ");
            prepareQuery.append("mfrs.locationName AS locationName, ");
            prepareQuery.append("mfrs.product_id AS productId, ");
            prepareQuery.append("mfrs.productName AS productName, ");
            prepareQuery.append("mfrs.productUnit AS unit, ");
            prepareQuery.append("mfrs.quantity AS quantity, ");
            prepareQuery.append("sl.productAdditionalUnit AS additionalUnit, ");
            prepareQuery.append("mfrs.quantityInAdditionalUnit AS additionalQuantity ");
            prepareQuery.append("FROM materialFlowResources_resourceStockDto AS mfrs ");
            prepareQuery.append("JOIN materialFlowResources_storageLocationDto AS sl ");
            prepareQuery.append("ON mfrs.productNumber = sl.productNumber ");
            prepareQuery.append("WHERE mfrs.productNumber = :productNumber ");
            prepareQuery.append("AND mfrs.locationNumber IN (:locationNumbers) ");


            params.put("productNumber", productNumber);
            params.put("locationNumbers", locationNumbers);

            list = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(StorageLocationsForProductDto.class));
            return list;
        }
        return list;
    }

    @Override
    public ResourceDetailsDto getResourceDetails(String resourceNumber) {

        Map<String, Object> params = Maps.newHashMap();

        if (resourceNumber != null || !resourceNumber.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("dto.location_id AS locationId, ");
            prepareQuery.append("dto.productName AS productName, ");
            prepareQuery.append("dto.productNumber AS productNumber, ");
            prepareQuery.append("dto.locationNumber AS locationNumber, ");
            prepareQuery.append("dto.storageLocationNumber AS storageLocationNumber, ");
            prepareQuery.append("dto.palletNumber AS palletNumber, ");
            prepareQuery.append("dto.batchNumber AS batchNumber, ");
            prepareQuery.append("dto.productionDate AS productionDate, ");
            prepareQuery.append("dto.expirationDate AS expirationDate, ");
            prepareQuery.append("dto.givenUnit AS additionalUnit, ");
            prepareQuery.append("dto.quantity AS quantity, ");
            prepareQuery.append("dto.productUnit AS unit, ");
            prepareQuery.append("dto.quantityInAdditionalUnit AS additionalQuantity, ");
            prepareQuery.append("dto.blockedForQualityControl AS blockedForQualityControl, ");
            prepareQuery.append("dto.qualityRating AS qualityRating ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS dto ");
            prepareQuery.append("WHERE dto.number = :resourceNumber");

            params.put("resourceNumber", resourceNumber);

            try {
                return jdbcTemplate.queryForObject(prepareQuery.toString(), params, BeanPropertyRowMapper.newInstance(ResourceDetailsDto.class));
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
        return null;
    }

    public ResourceToRepackDto getResourceDetailsToRepack(String resourceNumber, List<String> userLocations) {
        Map<String, Object> params = Maps.newHashMap();

        if (resourceNumber != null || !resourceNumber.isEmpty() || !userLocations.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("dto.location_id AS locationId, ");
            prepareQuery.append("dto.number AS resourceNumber, ");
            prepareQuery.append("dto.productName AS productName, ");
            prepareQuery.append("dto.productNumber AS productNumber, ");
            prepareQuery.append("dto.locationNumber AS locationNumber, ");
            prepareQuery.append("dto.storageLocationNumber AS storageLocationNumber, ");
            prepareQuery.append("dto.palletNumber AS palletNumber, ");
            prepareQuery.append("dto.typeOfPallet AS palletType, ");
            prepareQuery.append("dto.batchNumber AS batchNumber, ");
            prepareQuery.append("dto.productionDate AS productionDate, ");
            prepareQuery.append("dto.expirationDate AS expirationDate, ");
            prepareQuery.append("dto.givenUnit AS additionalUnit, ");
            prepareQuery.append("dto.quantity AS quantity, ");
            prepareQuery.append("dto.productUnit AS unit, ");
            prepareQuery.append("dto.quantityInAdditionalUnit AS additionalQuantity, ");
            prepareQuery.append("dto.conversion AS conversionValue, ");
            prepareQuery.append("dto.blockedForQualityControl AS blockedForQualityControl, ");
            prepareQuery.append("dto.qualityRating AS qualityRating ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS dto ");
            prepareQuery.append("WHERE dto.number = :resourceNumber ");
            prepareQuery.append("AND dto.locationNumber IN (:userLocations)");

            params.put("resourceNumber", resourceNumber);
            params.put("userLocations", userLocations);

            try {
                return jdbcTemplate.queryForObject(prepareQuery.toString(), params, BeanPropertyRowMapper.newInstance(ResourceToRepackDto.class));
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
        return null;
    }

    public StorageLocationNumberIdDto getLocationId(String storageLocation, String storageLocationNumber) {
        Map<String, Object> params = Maps.newHashMap();

        if (storageLocation != null && storageLocationNumber != null && !storageLocationNumber.isEmpty() && !storageLocation.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT DISTINCT ");
            prepareQuery.append("dto.location_id AS locationId ");
            prepareQuery.append("FROM materialFlowResources_storageLocationDto AS dto ");
            prepareQuery.append("WHERE dto.locationNumber = :storageLocation ");
            prepareQuery.append("AND dto.storageLocationNumber = :storageLocationNumber");

            params.put("storageLocation", storageLocation);
            params.put("storageLocationNumber", storageLocationNumber);

            try {
                return jdbcTemplate.queryForObject(prepareQuery.toString(), params, BeanPropertyRowMapper.newInstance(StorageLocationNumberIdDto.class));
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
        return null;
    }

    public StorageLocationNumberIdDto checkIfStorageLocationNumberExist(String storageLocationNumber) {
        Map<String, Object> params = Maps.newHashMap();

        if (storageLocationNumber != null && !storageLocationNumber.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("sl.id AS locationId ");
            prepareQuery.append("FROM materialFlowResources_storageLocation sl ");
            prepareQuery.append("WHERE sl.number = :storageLocationNumber");

            params.put("storageLocationNumber", storageLocationNumber);

            try {
                return jdbcTemplate.queryForObject(prepareQuery.toString(), params, BeanPropertyRowMapper.newInstance(StorageLocationNumberIdDto.class));
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
        return null;
    }

    public CheckProductDto checkProductByStorageLocationNumber(String storageLocationNumber) {
        Map<String, Object> params = Maps.newHashMap();

        if (storageLocationNumber != null && !storageLocationNumber.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("sl.location_id AS locationId, ");
            prepareQuery.append("psl.product_id AS productId, ");
            prepareQuery.append("sl.placeStorageLocation AS placeStorageLocation, ");
            prepareQuery.append("sl.maximumNumberOfPallets AS maximumNumberOfPallets ");
            prepareQuery.append("FROM materialFlowResources_storageLocation sl ");
            prepareQuery.append("LEFT JOIN jointable_product_storagelocation psl ");
            prepareQuery.append("ON psl.storagelocation_id = sl.id ");
            prepareQuery.append("LEFT JOIN basic_product p ");
            prepareQuery.append("ON p.id = psl.product_id ");
            prepareQuery.append("WHERE sl.number = :storageLocationNumber");

            params.put("storageLocationNumber", storageLocationNumber);

            try {
                return jdbcTemplate.queryForObject(prepareQuery.toString(), params, BeanPropertyRowMapper.newInstance(CheckProductDto.class));
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
        return null;
    }

    public List<PalletDto> checkPalletsForLocationNumber(final String storageLocationNumber) {
        Map<String, Object> params = Maps.newHashMap();
        List<PalletDto> list = new ArrayList<>();

        if (storageLocationNumber != null || !storageLocationNumber.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();
            prepareQuery.append("SELECT dto.palletNumber ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS dto ");
            prepareQuery.append("WHERE dto.storageLocationNumber = :storageLocationNumber ");
            prepareQuery.append("GROUP BY dto.palletNumber");

            params.put("storageLocationNumber", storageLocationNumber);

            list = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(PalletDto.class));
            return list;
        }
        return list;
    }

    public Optional<Entity> findStorageLocationForProduct(final Entity location, final Entity product) {
        SearchQueryBuilder scb = getStorageLocationDD().find("SELECT sl FROM #materialFlowResources_storageLocation AS sl JOIN sl.products p WHERE sl.location = :locationId AND p.id = :productId");

        scb.setLong("locationId", location.getId());
        scb.setLong("productId", product.getId());

        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

    public PalletDto checkIfPalletExist(String palletNumber) {
        Map<String, Object> params = Maps.newHashMap();

        if (palletNumber != null || !palletNumber.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("bpn.number ");
            prepareQuery.append("FROM basic_palletNumber AS bpn ");
            prepareQuery.append("WHERE bpn.number = :palletNumber");

            params.put("palletNumber", palletNumber);

            try {
                return jdbcTemplate.queryForObject(prepareQuery.toString(), params, BeanPropertyRowMapper.newInstance(PalletDto.class));
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
        return null;
    }

    public ResourceNumberDto checkIfPalletIsEmpty(String palletNumber) {
        Map<String, Object> params = Maps.newHashMap();
        if (palletNumber != null || !palletNumber.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("dto.number ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS dto ");
            prepareQuery.append("WHERE dto.palletNumber = :palletNumber");

            params.put("palletNumber", palletNumber);

            try {
                return jdbcTemplate.queryForObject(prepareQuery.toString(), params, BeanPropertyRowMapper.newInstance(ResourceNumberDto.class));
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public List<ResourceToRepackDto> getResourceListByStorageLocationNumber(String storageLocationNumber, List<String> userLocations) {
        Map<String, Object> params = Maps.newHashMap();
        List<ResourceToRepackDto> list = new ArrayList<>();

        if ((storageLocationNumber != null || !storageLocationNumber.isEmpty()) && !userLocations.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("dto.location_id AS locationId, ");
            prepareQuery.append("dto.number AS resourceNumber, ");
            prepareQuery.append("dto.productName AS productName, ");
            prepareQuery.append("dto.productNumber AS productNumber, ");
            prepareQuery.append("dto.locationNumber AS locationNumber, ");
            prepareQuery.append("dto.storageLocationNumber AS storageLocationNumber, ");
            prepareQuery.append("dto.palletNumber AS palletNumber, ");
            prepareQuery.append("dto.typeOfPallet AS palletType, ");
            prepareQuery.append("dto.batchNumber AS batchNumber, ");
            prepareQuery.append("dto.productionDate AS productionDate, ");
            prepareQuery.append("dto.expirationDate AS expirationDate, ");
            prepareQuery.append("dto.givenUnit AS additionalUnit, ");
            prepareQuery.append("dto.quantity AS quantity, ");
            prepareQuery.append("dto.productUnit AS unit, ");
            prepareQuery.append("dto.quantityInAdditionalUnit AS additionalQuantity, ");
            prepareQuery.append("dto.conversion AS conversionValue ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS dto ");
            prepareQuery.append("WHERE dto.storageLocationNumber = :storageLocationNumber ");
            prepareQuery.append("AND dto.locationNumber IN (:userLocations)");

            params.put("storageLocationNumber", storageLocationNumber);
            params.put("userLocations", userLocations);

            list = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(ResourceToRepackDto.class));
            return list;
        }
        return list;
    }

    @Override
    public List<ResourceToRepackDto> getResourceListByPalletNumber(String palletNumber, List<String> userLocations) {
        Map<String, Object> params = Maps.newHashMap();
        List<ResourceToRepackDto> list = new ArrayList<>();

        if ((palletNumber != null || !palletNumber.isEmpty()) && !userLocations.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("dto.location_id AS locationId, ");
            prepareQuery.append("dto.number AS resourceNumber, ");
            prepareQuery.append("dto.productName AS productName, ");
            prepareQuery.append("dto.productNumber AS productNumber, ");
            prepareQuery.append("dto.locationNumber AS locationNumber, ");
            prepareQuery.append("dto.storageLocationNumber AS storageLocationNumber, ");
            prepareQuery.append("dto.palletNumber AS palletNumber, ");
            prepareQuery.append("dto.typeOfPallet AS palletType, ");
            prepareQuery.append("dto.batchNumber AS batchNumber, ");
            prepareQuery.append("dto.productionDate AS productionDate, ");
            prepareQuery.append("dto.expirationDate AS expirationDate, ");
            prepareQuery.append("dto.givenUnit AS additionalUnit, ");
            prepareQuery.append("dto.quantity AS quantity, ");
            prepareQuery.append("dto.productUnit AS unit, ");
            prepareQuery.append("dto.quantityInAdditionalUnit AS additionalQuantity, ");
            prepareQuery.append("dto.conversion AS conversionValue ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS dto ");
            prepareQuery.append("WHERE dto.palletNumber = :palletNumber ");
            prepareQuery.append("AND dto.locationNumber IN (:userLocations)");

            params.put("palletNumber", palletNumber);
            params.put("userLocations", userLocations);

            list = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(ResourceToRepackDto.class));
            return list;
        }
        return list;
    }

    @Override
    public List<ResourceToRepackDto> getResourceListByPalletAndLocationNumber(String palletNumber, String storageLocationNumber, List<String> userLocations) {
        Map<String, Object> params = Maps.newHashMap();
        List<ResourceToRepackDto> list = new ArrayList<>();

        if ((palletNumber != null || !palletNumber.isEmpty())
                && (storageLocationNumber != null || !storageLocationNumber.isEmpty())
                && !userLocations.isEmpty()) {
            StringBuilder prepareQuery = new StringBuilder();

            prepareQuery.append("SELECT ");
            prepareQuery.append("dto.location_id AS locationId, ");
            prepareQuery.append("dto.number AS resourceNumber, ");
            prepareQuery.append("dto.productName AS productName, ");
            prepareQuery.append("dto.productNumber AS productNumber, ");
            prepareQuery.append("dto.locationNumber AS locationNumber, ");
            prepareQuery.append("dto.storageLocationNumber AS storageLocationNumber, ");
            prepareQuery.append("dto.palletNumber AS palletNumber, ");
            prepareQuery.append("dto.typeOfPallet AS palletType, ");
            prepareQuery.append("dto.batchNumber AS batchNumber, ");
            prepareQuery.append("dto.productionDate AS productionDate, ");
            prepareQuery.append("dto.expirationDate AS expirationDate, ");
            prepareQuery.append("dto.givenUnit AS additionalUnit, ");
            prepareQuery.append("dto.quantity AS quantity, ");
            prepareQuery.append("dto.productUnit AS unit, ");
            prepareQuery.append("dto.quantityInAdditionalUnit AS additionalQuantity, ");
            prepareQuery.append("dto.conversion AS conversionValue ");
            prepareQuery.append("FROM materialFlowResources_resourceDto AS dto ");
            prepareQuery.append("WHERE dto.palletNumber = :palletNumber ");
            prepareQuery.append("AND dto.storageLocationNumber = :storageLocationNumber ");
            prepareQuery.append("AND dto.locationNumber IN (:userLocations)");

            params.put("palletNumber", palletNumber);
            params.put("userLocations", userLocations);
            params.put("storageLocationNumber", storageLocationNumber);

            list = jdbcTemplate.query(prepareQuery.toString(), params, new BeanPropertyRowMapper(ResourceToRepackDto.class));
            return list;
        }
        return list;
    }

    public String getTypeOfPalletByPalletNumber(final Long locationId, final String palletNumberNumber) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT resource.typeofpallet ");
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
            return jdbcTemplate.queryForObject(query.toString(), params, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean isPlaceStorageLocation(final String storageLocationNumber) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT placestoragelocation ");
        query.append("FROM materialflowresources_storagelocation ");
        query.append("WHERE number = :storageLocationNumber");

        Map<String, Object> params = Maps.newHashMap();

        params.put("storageLocationNumber", storageLocationNumber);

        try {
            return jdbcTemplate.queryForObject(query.toString(), params, Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean checkIfExistsMorePalletsForStorageLocation(final Long locationId, final String storageLocationNumber) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("CASE ");
        query.append("WHEN COALESCE(MAX(storagelocation.maximumNumberOfPallets), 0) = 0 THEN FALSE ");
        query.append("ELSE COUNT(DISTINCT(resource.palletnumber_id)) >= COALESCE(MAX(storagelocation.maximumNumberOfPallets), 0) ");
        query.append("END AS exists ");
        query.append("FROM materialflowresources_resource resource ");
        query.append("RIGHT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON storagelocation.id = resource.storagelocation_id ");
        query.append("WHERE storagelocation.number = :storageLocationNumber ");
        query.append("AND storagelocation.placestoragelocation = true ");
        query.append("AND resource.location_id = :locationId");

        Map<String, Object> params = Maps.newHashMap();

        params.put("storageLocationNumber", storageLocationNumber);
        params.put("locationId", locationId);

        return jdbcTemplate.queryForObject(query.toString(), params, Boolean.class);
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
