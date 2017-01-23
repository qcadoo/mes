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
package com.qcadoo.mes.deliveries.hooks;

import com.google.common.base.Strings;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DAMAGED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.ReservationService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Service
public class DeliveredProductHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onCreate(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        reservationService.createDefaultReservationsForDeliveredProduct(deliveredProduct);
    }

    public void onSave(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        reservationService.deleteReservationsForDeliveredProductIfChanged(deliveredProduct);
    }

    public void calculateDeliveredProductPricePerUnit(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        deliveriesService.calculatePricePerUnit(deliveredProduct, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public boolean validatesWith(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        return checkIfDeliveredProductAlreadyExists(deliveredProductDD, deliveredProduct)
                && checkIfDeliveredQuantityIsLessThanDamagedQuantity(deliveredProductDD, deliveredProduct)
                && checkIfDeliveredQuantityIsLessThanOrderedQuantity(deliveredProductDD, deliveredProduct) && validatePallet(deliveredProductDD, deliveredProduct);
    }

    public boolean checkIfDeliveredProductAlreadyExists(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = addSearchRestrictions(deliveredProductDD.find(), deliveredProduct);

        if (deliveredProduct.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", deliveredProduct.getId()));
        }

        Entity deliveredProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (deliveredProductFromDB == null) {
            return true;
        } else {
            deliveredProduct.addError(deliveredProductDD.getField(PRODUCT),
                    "deliveries.deliveredProduct.error.productAlreadyExists");

            return false;
        }
    }

    private SearchCriteriaBuilder addSearchRestrictions(SearchCriteriaBuilder scb, Entity deliveredProduct) {
        return scb.add(SearchRestrictions.belongsTo(DELIVERY, deliveredProduct.getBelongsToField(DELIVERY)))
                .add(SearchRestrictions.belongsTo(PRODUCT, deliveredProduct.getBelongsToField(PRODUCT)));
    }

    public boolean checkIfDeliveredQuantityIsLessThanDamagedQuantity(final DataDefinition deliveredProductDD,
            final Entity deliveredProduct) {
        BigDecimal damagedQuantity = deliveredProduct.getDecimalField(DAMAGED_QUANTITY);
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DELIVERED_QUANTITY);

        if ((damagedQuantity != null) && (deliveredQuantity != null) && (damagedQuantity.compareTo(deliveredQuantity) == 1)) {
            deliveredProduct.addError(deliveredProductDD.getField(DAMAGED_QUANTITY),
                    "deliveries.deliveredProduct.error.damagedQuantity.deliveredQuantityIsTooMuch");
            deliveredProduct.addError(deliveredProductDD.getField(DELIVERED_QUANTITY),
                    "deliveries.deliveredProduct.error.damagedQuantity.deliveredQuantityIsTooMuch");

            return false;
        }

        return true;
    }

    private boolean checkIfDeliveredQuantityIsLessThanOrderedQuantity(final DataDefinition deliveredProductDD,
            final Entity deliveredProduct) {
        if (isBiggerDeliveredQuantityAllowed()) {
            return true;
        }
        Optional<Entity> orderedProduct = getOrderedProductForDeliveredProduct(deliveredProduct);
        BigDecimal orderedQuantity = orderedProduct.isPresent()
                ? orderedProduct.get().getDecimalField(OrderedProductFields.ORDERED_QUANTITY) : BigDecimal.ZERO;
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
        if (deliveredQuantity != null && deliveredQuantity.compareTo(orderedQuantity) > 0) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.DELIVERED_QUANTITY),
                    "deliveries.deliveredProduct.error.deliveredQuantity.biggerThanOrderedQuantity");
            return false;
        }
        return true;
    }

    private Optional<Entity> getOrderedProductForDeliveredProduct(final Entity deliveredProduct) {
        DataDefinition orderedProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_ORDERED_PRODUCT);
        Entity orderedProduct = orderedProductDD.find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.ADDITIONAL_CODE,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE)))
                .setMaxResults(1).uniqueResult();
        return Optional.ofNullable(orderedProduct);

    }

    private boolean isBiggerDeliveredQuantityAllowed() {
        return parameterService.getParameter().getBooleanField(ParameterFieldsD.DELIVERED_BIGGER_THAN_ORDERED);
    }

    private boolean validatePallet(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        return (!existsOtherPositionForPalletAndStorageLocation(deliveredProductDD, deliveredProduct) || !existsOtherPositionForStorageLocationAndPallet(deliveredProductDD, deliveredProduct))
                && !existsOtherPositionForOtherPalletType(deliveredProductDD, deliveredProduct)
                && (!existsOtherResourceForPalletAndStorageLocation(deliveredProductDD, deliveredProduct) || !existsOtherResourceForStorageLocationAndPallet(deliveredProductDD, deliveredProduct))
                && !existsOtherResourceForOtherPalletType(deliveredProductDD, deliveredProduct);
    }

    private boolean existsOtherPositionForPalletAndStorageLocation(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        String query = "select count(*) from Deliveries_DeliveredProduct p"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER) == null) {
            query += "( p.palletnumber_id is null)";

        } else {
            Entity palletNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);
            params.put("palletNumberId", palletNumber.getId());
            query += "( p.palletnumber_id = :palletNumberId)";
        }

        if (deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION) == null) {
            query += " AND ( p.storagelocation_id is not null)";

        } else {
            Entity storageLocation = deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);
            params.put("storageLocationId", storageLocation.getId());
            query += " AND ( p.storagelocation_id <> :storageLocationId or p.storagelocation_id is null)";
        }

        if (deliveredProduct.getId() != null && deliveredProduct.getId() > 0) {
            query += " AND p.id <> " + deliveredProduct.getId();
        }
        Long count = jdbcTemplate.queryForObject(query, params, Long.class);

        boolean exists = count > 0L;
        if (exists) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.PALLET_NUMBER), "documentGrid.error.position.existsOtherPositionForPalletAndStorageLocation");
        }
        return exists;
    }

    private boolean existsOtherPositionForStorageLocationAndPallet(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        String query = "select count(*) from Deliveries_DeliveredProduct p"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION) == null) {
            query += "( p.storagelocation_id is null)";

        } else {
            Entity storageLocation = deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);
            params.put("storageLocationId", storageLocation.getId());
            query += "( p.storagelocation_id = :storageLocationId)";
        }

        if (deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER) == null) {
            query += " AND ( p.palletnumber_id is not null)";

        } else {
            Entity palletNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);
            params.put("palletNumberId", palletNumber.getId());
            query += " AND ( p.palletnumber_id <> :palletNumberId or p.palletnumber_id is null)";
        }

        if (deliveredProduct.getId() != null && deliveredProduct.getId() > 0) {
            query += " AND p.id <> " + deliveredProduct.getId();
        }
        Long count = jdbcTemplate.queryForObject(query, params, Long.class);
        boolean exists = count > 0L;
        if (exists) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.PALLET_NUMBER), "documentGrid.error.position.existsOtherPositionForPalletAndStorageLocation");
        }
        return exists;
    }

    private boolean existsOtherPositionForOtherPalletType(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        String query = "select count(dp) as cnt from DeliveriesDeliveredProduct dp JOIN dp.palletNumber as pallet"
                + "	where pallet.number = :palletNumber ";

        String palletNumber = "";
        Entity palletNumberEntity = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);
        if (palletNumberEntity != null) {
            palletNumber = palletNumberEntity.getStringField(PalletNumberFields.NUMBER);
        }

        String palletType = deliveredProduct.getStringField(DeliveredProductFields.PALLET_TYPE);
        if (Strings.isNullOrEmpty(palletType)) {
            palletType = "";
            query += "and ( dp.palletType <> :palletType)";
        } else {
            query += "and ( dp.palletType is null OR dp.palletType <> :palletType)";
        }

        if (deliveredProduct.getId() != null && deliveredProduct.getId() > 0) {
            query += " AND dp.id <> " + deliveredProduct.getId();
        }

        SearchQueryBuilder find = deliveredProductDD.find(query);
        find.setString("palletType", palletType);
        find.setString("palletNumber", palletNumber);
        Entity countResults = find.uniqueResult();

        boolean exists = ((Long) countResults.getField("cnt")) > 0L;
        if (exists) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.PALLET_NUMBER), "documentGrid.error.position.existsOtherPositionForOtherPalletType");
        }
        return exists;
    }

    private boolean existsOtherResourceForPalletAndStorageLocation(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        String query = "select count(*) from materialflowresources_resource r"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER) == null) {
            query += "( r.palletnumber_id is null)";

        } else {
            Entity palletNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);
            params.put("palletNumberId", palletNumber.getId());
            query += "( r.palletnumber_id = :palletNumberId)";
        }

        if (deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION) == null) {
            query += " AND ( r.storagelocation_id is not null)";

        } else {
            Entity storageLocation = deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);
            params.put("storageNumberId", storageLocation.getId());
            query += " AND ( r.storagelocation_id <> :storageNumberId or r.storagelocation_id is null)";
        }

        Long count = jdbcTemplate.queryForObject(query, params, Long.class);

        boolean exists = count > 0L;
        if (exists) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.PALLET_NUMBER), "documentGrid.error.position.existsOtherResourceForPalletAndStorageLocation");
        }
        return exists;
    }

    private boolean existsOtherResourceForStorageLocationAndPallet(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        String query = "select count(*) from materialflowresources_resource r"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION) == null) {
            query += "( r.storagelocation_id is null)";

        } else {
            Entity storageLocation = deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);
            params.put("storageId", storageLocation.getId());
            query += "( r.storagelocation_id = :storageId)";
        }

        if (deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER) == null) {
            query += " AND ( r.palletnumber_id is not null)";

        } else {
            Entity palletNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);
            params.put("palletNumberId", palletNumber.getId());
            query += " AND ( r.palletnumber_id <> :palletNumberId or r.palletnumber_id is null)";
        }

        Long count = jdbcTemplate.queryForObject(query, params, Long.class);

        boolean exists = count > 0L;
        if (exists) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.PALLET_NUMBER), "documentGrid.error.position.existsOtherResourceForPalletAndStorageLocation");
        }
        return exists;
    }

    private boolean existsOtherResourceForOtherPalletType(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        String query = "select count(resource) as cnt from MaterialFlowResourcesResource resource JOIN resource.palletNumber as pallet"
                + "	where pallet.number = :palletNumber and resource.typeOfPallet <> :palletType ";

        String palletNumber = "";
        Entity palletNumberEntity = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);
        if (palletNumberEntity != null) {
            palletNumber = palletNumberEntity.getStringField(PalletNumberFields.NUMBER);
        }

        String palletType = deliveredProduct.getStringField(DeliveredProductFields.PALLET_TYPE);
        if (Strings.isNullOrEmpty(palletType)) {
            palletType = "";
            query += "and ( resource.typeOfPallet <> :palletType)";
        } else {
            query += "and ( resource.typeOfPallet is null OR resource.typeOfPallet <> :palletType)";
        }

        SearchQueryBuilder find = deliveredProductDD.find(query);
        find.setString("palletType", palletType);
        find.setString("palletNumber", palletNumber);
        Entity countResults = find.uniqueResult();

        boolean exists = ((Long) countResults.getField("cnt")) > 0L;
        if (exists) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.PALLET_NUMBER), "documentGrid.error.position.existsOtherResourceForOtherPalletType");
        }
        return exists;
    }

}
