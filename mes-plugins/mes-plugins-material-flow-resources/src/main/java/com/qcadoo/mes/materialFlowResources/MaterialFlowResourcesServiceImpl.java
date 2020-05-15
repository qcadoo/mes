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
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.LocationType;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.TransferFields;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaterialFlowResourcesServiceImpl implements MaterialFlowResourcesService {



    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Override
    public boolean areResourcesSufficient(final Entity location, final Entity product, final BigDecimal quantity) {
        String type = location.getStringField(LocationFields.TYPE);

        if (isTypeWarehouse(type)) {
            BigDecimal resourcesQuantity = getResourcesQuantityForLocationAndProduct(location, product);

            return Objects.nonNull(resourcesQuantity) && (resourcesQuantity.compareTo(quantity) >= 0);
        } else {
            return true;
        }
    }

    @Override
    public BigDecimal getResourcesQuantityForLocationAndProduct(final Entity location, final Entity product) {
        String type = location.getStringField(LocationFields.TYPE);

        if (isTypeWarehouse(type)) {
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
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public void manageResources(final Entity transfer) {
        if (Objects.isNull(transfer)) {
            return;
        }

        Entity locationFrom = transfer.getBelongsToField(TransferFields.LOCATION_FROM);
        Entity locationTo = transfer.getBelongsToField(TransferFields.LOCATION_TO);
        Entity product = transfer.getBelongsToField(TransferFields.PRODUCT);
        BigDecimal quantity = transfer.getDecimalField(TransferFields.QUANTITY);
        Date time = (Date) transfer.getField(TransferFields.TIME);
        BigDecimal price = transfer.getDecimalField(TransferFieldsMFR.PRICE);

        if (Objects.nonNull(locationFrom) && isTypeWarehouse(locationFrom.getStringField(LocationFields.TYPE))
                && Objects.nonNull(locationTo) && isTypeWarehouse(locationTo.getStringField(LocationFields.TYPE))) {
            moveResource(locationFrom, locationTo, product, quantity, time, price);
        } else if (Objects.nonNull(locationFrom) && isTypeWarehouse(locationFrom.getStringField(LocationFields.TYPE))) {
            updateResource(locationFrom, product, quantity);
        } else if (Objects.nonNull(locationTo) && isTypeWarehouse(locationTo.getStringField(LocationFields.TYPE))) {
            addResource(locationTo, product, quantity, time, price);
        }
    }

    private boolean isTypeWarehouse(final String type) {
        return (Objects.nonNull(type) && LocationType.WAREHOUSE.getStringValue().equals(type));
    }

    @Override
    public void addResource(final Entity locationTo, final Entity product, final BigDecimal quantity, final Date time,
            final BigDecimal price) {
        addResource(locationTo, product, quantity, time, price, null);
    }

    @Override
    public void addResource(final Entity locationTo, final Entity product, final BigDecimal quantity, final Date time,
            final BigDecimal price, final Entity batch) {
        Entity resource = getResourceDD().create();

        resource.setField(ResourceFields.LOCATION, locationTo);
        resource.setField(ResourceFields.PRODUCT, product);
        resource.setField(ResourceFields.QUANTITY, numberService.setScaleWithDefaultMathContext(quantity));
        resource.setField(ResourceFields.TIME, time);
        resource.setField(ResourceFields.BATCH, batch);
        resource.setField(ResourceFields.PRICE, Objects.isNull(price) ? null : numberService.setScaleWithDefaultMathContext(price));

        resource.getDataDefinition().save(resource);
    }

    @Override
    public void updateResource(final Entity locationFrom, final Entity product, BigDecimal quantity) {
        List<Entity> resources = getResourcesForLocationAndProduct(locationFrom, product);

        if (Objects.nonNull(resources)) {
            for (Entity resource : resources) {
                BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.QUANTITY);

                if (quantity.compareTo(resourceQuantity) >= 0) {
                    quantity = quantity.subtract(resourceQuantity, numberService.getMathContext());

                    resource.getDataDefinition().delete(resource.getId());

                    if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                        return;
                    }
                } else {
                    resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());

                    resource.setField(ResourceFields.QUANTITY, numberService.setScaleWithDefaultMathContext(resourceQuantity));

                    resource.getDataDefinition().save(resource);

                    return;
                }
            }
        }
    }

    @Override
    public void moveResource(final Entity locationFrom, final Entity locationTo, final Entity product, BigDecimal quantity,
            final Date time, final BigDecimal price) {
        List<Entity> resources = getResourcesForLocationAndProduct(locationFrom, product);

        if (Objects.nonNull(resources)) {
            for (Entity resource : resources) {
                BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.QUANTITY);
                BigDecimal resourcePrice = Objects.isNull(price) ? resource.getDecimalField(ResourceFields.PRICE) : price;
                Entity resourceBatch = Objects.isNull(price) ? resource.getBelongsToField(ResourceFields.BATCH) : null;

                if (quantity.compareTo(resourceQuantity) >= 0) {
                    quantity = quantity.subtract(resourceQuantity, numberService.getMathContext());

                    resource.getDataDefinition().delete(resource.getId());

                    addResource(locationTo, product, resourceQuantity, time, resourcePrice, resourceBatch);

                    if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                        return;
                    }
                } else {
                    resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());

                    resource.setField(ResourceFields.QUANTITY, numberService.setScaleWithDefaultMathContext(resourceQuantity));

                    resource.getDataDefinition().save(resource);

                    addResource(locationTo, product, quantity, time, resourcePrice, resourceBatch);

                    return;
                }
            }
        }
    }

    @Override
    public List<Entity> getWarehouseLocationsFromDB() {
        return getLocationDD().find().add(SearchRestrictions.eq(LocationFields.TYPE, LocationType.WAREHOUSE.getStringValue()))
                .list().getEntities();
    }

    @Override
    public List<Entity> getResourcesForLocationAndProduct(final Entity location, final Entity product) {
        List<Entity> resources = getResourceDD().find().add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, location))
                .add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, product))
                .addOrder(SearchOrders.asc(ResourceFields.TIME)).list().getEntities();

        return resources;
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location) {
        return getQuantitiesForProductsAndLocation(products, location, false);
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location,
            final boolean withoutBlockedForQualityControl) {
        Map<Long, BigDecimal> quantities = Maps.newHashMap();

        if (products.size() > 0) {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT p.id AS product, SUM(r.quantity) AS quantity ");
            sb.append("FROM #materialFlowResources_resource AS r ");
            sb.append("JOIN r.product AS p ");
            sb.append("JOIN r.location AS l ");
            if (withoutBlockedForQualityControl) {
                sb.append("WHERE r.blockedForQualityControl = false ");
            }
            sb.append("GROUP BY p.id, l.id ");
            sb.append("HAVING p.id IN (:productIds) ");
            sb.append("AND l.id = :locationId ");

            SearchQueryBuilder sqb = getResourceDD().find(sb.toString());

            sqb.setParameter("locationId", location.getId());
            sqb.setParameterList("productIds", products.stream().map(product -> product.getId()).collect(Collectors.toList()));

            List<Entity> productsAndQuantities = sqb.list().getEntities();

            productsAndQuantities.stream().forEach(productAndQuantity -> quantities
                    .put((Long) productAndQuantity.getField("product"), productAndQuantity.getDecimalField("quantity")));
        }

        return quantities;
    }

    @Override
    public Map<Entity, BigDecimal> groupResourcesByProduct(final Entity location) {
        Map<Entity, BigDecimal> productsAndQuantities = new LinkedHashMap<Entity, BigDecimal>();

        List<Entity> resources = getResourcesForLocation(location);

        if (Objects.nonNull(resources)) {
            for (Entity resource : resources) {
                Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
                BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);

                if (productsAndQuantities.containsKey(product)) {
                    productsAndQuantities.put(product,
                            productsAndQuantities.get(product).add(quantity, numberService.getMathContext()));
                } else {
                    productsAndQuantities.put(product, quantity);
                }
            }
        }

        return productsAndQuantities;
    }

    private List<Entity> getResourcesForLocation(final Entity location) {
        List<Entity> resources = getResourceDD().find().createAlias(ResourceFields.PRODUCT, ResourceFields.PRODUCT)
                .add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, location))
                .addOrder(SearchOrders.asc(ResourceFields.PRODUCT + "." + ProductFields.NAME)).list().getEntities();

        return resources;
    }

    @Override
    public BigDecimal calculatePrice(final Entity location, final Entity product) {
        if (Objects.nonNull(location) && Objects.nonNull(product)) {
            List<Entity> resources = getResourcesForLocationAndProduct(location, product);

            if (Objects.nonNull(resources)) {
                BigDecimal avgPrice = BigDecimal.ZERO;
                BigDecimal avgQuantity = BigDecimal.ZERO;

                for (Entity resource : resources) {
                    BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);
                    BigDecimal price = resource.getDecimalField(ResourceFields.PRICE);

                    if (Objects.nonNull(price)) {
                        avgPrice = avgPrice.add(quantity.multiply(price, numberService.getMathContext()),
                                numberService.getMathContext());
                        avgQuantity = avgQuantity.add(quantity, numberService.getMathContext());
                    }
                }

                if (!BigDecimal.ZERO.equals(avgPrice) && !BigDecimal.ZERO.equals(avgQuantity)) {
                    avgPrice = avgPrice.divide(avgQuantity, numberService.getMathContext());

                    return avgPrice;
                }
            }
        }

        return null;
    }

    @Override
    public boolean canChangeDateWhenTransferToWarehouse() {
        Entity documentPositionParameters = parameterService.getParameter()
                .getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS);
        String changeDateWhenTransferToWarehouseType = documentPositionParameters
                .getStringField(ParameterFieldsMFR.CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE);

        return !ChangeDateWhenTransferToWarehouseType.NEVER.getStringValue().equals(changeDateWhenTransferToWarehouseType);
    }

    @Override
    public boolean shouldValidateDateWhenTransferToWarehouse() {
        Entity documentPositionParameters = parameterService.getParameter()
                .getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS);
        String changeDateWhenTransferToWarehouseType = documentPositionParameters
                .getStringField(ParameterFieldsMFR.CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE);

        return ChangeDateWhenTransferToWarehouseType.VALIDATE_WITH_RESOURCES.getStringValue()
                .equals(changeDateWhenTransferToWarehouseType);
    }

    @Override
    public boolean isDateGraterThanResourcesDate(final Date time) {
        SearchResult searchResult = getResourceDD().find().add(SearchRestrictions.gt(ResourceFields.TIME, time)).list();

        return searchResult.getEntities().isEmpty();
    }

    @Override
    public void disableDateField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        FieldComponent dateField = (FieldComponent) view.getComponentByReference(TransferFields.TIME);

        LookupComponent locationFromField = (LookupComponent) view.getComponentByReference(TransferFields.LOCATION_FROM);
        LookupComponent locationToField = (LookupComponent) view.getComponentByReference(TransferFields.LOCATION_TO);

        Entity locationFrom = locationFromField.getEntity();
        Entity locationTo = locationToField.getEntity();

        if (Objects.isNull(form.getEntityId())) {
            if (areLocationsWarehouses(locationFrom, locationTo) && !canChangeDateWhenTransferToWarehouse()) {
                String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                dateField.setFieldValue(currentDate);
                dateField.setEnabled(false);
            } else {
                dateField.setEnabled(true);
            }
        }
    }

    @Override
    public boolean isLocationIsWarehouse(final Entity location) {
        return (Objects.nonNull(location)
                && LocationType.WAREHOUSE.getStringValue().equals(location.getStringField(LocationFields.TYPE)));
    }

    @Override
    public boolean areLocationsWarehouses(final Entity locationFrom, final Entity locationTo) {
        return (isLocationIsWarehouse(locationFrom) || isLocationIsWarehouse(locationTo));
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

}
