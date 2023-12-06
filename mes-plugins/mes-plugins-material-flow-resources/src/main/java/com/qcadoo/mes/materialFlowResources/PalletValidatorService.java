package com.qcadoo.mes.materialFlowResources;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class PalletValidatorService {

    private static final String L_VALIDATE_PALLET = "validatePallet";

    private static final String L_DELIVERY = "delivery";

    private static final String L_DELIVERED_PRODUCT = "deliveredProduct";

    private static final String L_LOCATION = "location";

    private static final String L_STORAGE_LOCATION = "storageLocation";

    private static final String L_PALLET_NUMBER = "palletNumber";

    private static final String L_PALLET_TYPE = "palletType";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public boolean validatePalletForResource(final Entity resource) {
        Entity location = resource.getBelongsToField(ResourceFields.LOCATION);
        Entity storageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
        Entity palletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
        String typeOfPallet = resource.getStringField(ResourceFields.TYPE_OF_PALLET);

        return validatePallet(location, storageLocation, palletNumber, typeOfPallet, resource);
    }

    public boolean validatePalletForDeliveredProduct(final Entity deliveredProduct) {
        Entity location = deliveredProduct.getBelongsToField(L_DELIVERY).getBelongsToField(L_LOCATION);
        Entity storageLocation = deliveredProduct.getBelongsToField(L_STORAGE_LOCATION);
        Entity palletNumber = deliveredProduct.getBelongsToField(L_PALLET_NUMBER);
        String palletType = deliveredProduct.getStringField(L_PALLET_TYPE);

        if (Objects.isNull(location)) {
            return true;
        }

        return validatePallet(location, storageLocation, palletNumber, palletType, deliveredProduct) &&
                notTooManyPalletsInStorageLocation(deliveredProduct.getDataDefinition(), deliveredProduct);
    }

    public boolean validatePallet(final Entity location, final Entity storageLocation, final Entity palletNumber, final String typeOfPallet, final Entity entity) {
        if (Objects.isNull(entity.getField(L_VALIDATE_PALLET)) || entity.getBooleanField(L_VALIDATE_PALLET)) {

            boolean isValid = validateRequiredFields(storageLocation, palletNumber, entity);

            isValid = isValid && validatePalletNumberAndTypeOfPallet(location, storageLocation, palletNumber, typeOfPallet, entity);

            return isValid;
        }

        return true;
    }

    public boolean validateRequiredFields(final Entity storageLocation, final Entity palletNumber, final Entity entity) {
        if (Objects.isNull(storageLocation) && Objects.nonNull(palletNumber)) {
            entity.addError(entity.getDataDefinition().getField(L_STORAGE_LOCATION), "qcadooView.validate.field.error.missing");

            return false;
        } else {
            if (Objects.nonNull(storageLocation)) {
                boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

                if (placeStorageLocation) {
                    if (Objects.isNull(palletNumber)) {
                        entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER), "qcadooView.validate.field.error.missing");

                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean validatePalletNumberAndTypeOfPallet(final Entity location, final Entity storageLocation, final Entity palletNumber, final String typeOfPallet, final Entity entity) {
        String palletNumberNumber = Objects.nonNull(palletNumber) ? palletNumber.getStringField(PalletNumberFields.NUMBER) : null;
        String storageLocationNumber = Objects.nonNull(storageLocation) ? storageLocation.getStringField(StorageLocationFields.NUMBER) : null;
        Long resourceId = getEntityId(entity, MaterialFlowResourcesConstants.MODEL_RESOURCE);
        Long deliveredProductId = getEntityId(entity, L_DELIVERED_PRODUCT);

        if (existsOtherResourceForPalletNumberOnOtherLocations(location.getId(), storageLocationNumber, palletNumberNumber, typeOfPallet, resourceId)) {
            entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER),
                    "documentGrid.error.position.existsOtherResourceForPallet");

            return false;
        }
        if (existsOtherResourceForPalletNumberOnSameLocation(location.getId(), storageLocationNumber, palletNumberNumber, typeOfPallet, resourceId)) {
            entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER),
                    "documentGrid.error.position.existsOtherResourceForPalletAndStorageLocation");

            return false;
        }
        if (existsOtherPositionForPalletNumber(location.getId(), storageLocationNumber, palletNumberNumber, typeOfPallet, null, null)) {
            entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER),
                    "documentGrid.error.position.existsOtherPositionForPalletAndStorageLocation");

            return false;
        }
        if (existsOtherDeliveredProductForPalletNumber(location.getId(), storageLocationNumber, palletNumberNumber, typeOfPallet, deliveredProductId)) {
            entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER),
                    "documentGrid.error.position.existsOtherDeliveredProductForPalletAndStorageLocation");

            return false;
        }

        return true;
    }

    private Long getEntityId(final Entity entity, final String modelName) {
        if (modelName.equals(entity.getDataDefinition().getName()) && Objects.nonNull(entity.getId())) {
            return entity.getId();
        }

        return null;
    }

    public boolean existsOtherResourceForPalletNumberOnOtherLocations(final Long locationId, final String storageLocationNumber,
                                                      final String palletNumberNumber, final String typeOfPallet,
                                                      final Long resourceId) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(*) FROM materialflowresources_resource resource ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = resource.palletnumber_id ");
        query.append("LEFT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON storagelocation.id = resource.storagelocation_id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND resource.location_id <> :locationId ");

        if (Objects.nonNull(resourceId)) {
            query.append("AND resource.id <> :resourceId ");
        }

        Map<String, Object> params = Maps.newHashMap();

        params.put("locationId", locationId);
        params.put("palletNumberNumber", palletNumberNumber);

        if (Objects.nonNull(resourceId)) {
            params.put("resourceId", resourceId);
        }

        return jdbcTemplate.queryForObject(query.toString(), params, Long.class) > 0;
    }

    public boolean existsOtherResourceForPalletNumberOnSameLocation(final Long locationId, final String storageLocationNumber,
                                                      final String palletNumberNumber, final String typeOfPallet,
                                                      final Long resourceId) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(*) FROM materialflowresources_resource resource ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = resource.palletnumber_id ");
        query.append("LEFT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON storagelocation.id = resource.storagelocation_id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND (");
        query.append("storageLocation.number <> :storageLocationNumber ");

        if (StringUtils.isNotEmpty(typeOfPallet)) {
            query.append("OR resource.typeOfPallet <> :typeOfPallet OR COALESCE(resource.typeOfPallet, '') = ''");
        } else {
            query.append("OR COALESCE(resource.typeOfPallet, '') <> ''");
        }

        query.append(") ");
        query.append("AND resource.location_id = :locationId ");

        if (Objects.nonNull(resourceId)) {
            query.append("AND resource.id <> :resourceId ");
        }

        Map<String, Object> params = Maps.newHashMap();

        params.put("locationId", locationId);
        params.put("storageLocationNumber", storageLocationNumber);
        params.put("palletNumberNumber", palletNumberNumber);
        params.put("typeOfPallet", typeOfPallet);

        if (Objects.nonNull(resourceId)) {
            params.put("resourceId", resourceId);
        }

        return jdbcTemplate.queryForObject(query.toString(), params, Long.class) > 0;
    }

    public boolean existsOtherPositionForPalletNumber(final Long locationId, final String storageLocationNumber,
                                                      final String palletNumberNumber, final String typeOfPallet,
                                                      final Long positionId, final Long documentId) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(position) FROM materialflowresources_position position ");
        query.append("JOIN materialflowresources_document document ");
        query.append("ON document.id = position.document_id ");
        query.append("LEFT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON storagelocation.id = position.storagelocation_id ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = position.palletnumber_id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND (");
        query.append("storageLocation.number <> :storageLocationNumber ");

        if (StringUtils.isNotEmpty(typeOfPallet)) {
            query.append("OR position.typeOfPallet <> :typeOfPallet OR COALESCE(position.typeOfPallet, '') = ''");
        } else {
            query.append("OR COALESCE(position.typeOfPallet, '') <> ''");
        }

        query.append(") ");
        query.append("AND document.state = '01draft' ");
        query.append("AND document.locationTo_id = :locationId ");

        if (Objects.nonNull(documentId)) {
            query.append("AND document.id = :documentId ");
        }

        if (Objects.nonNull(positionId)) {
            query.append("AND position.id <> :positionId ");
        }

        Map<String, Object> params = Maps.newHashMap();

        params.put("locationId", locationId);
        params.put("storageLocationNumber", storageLocationNumber);
        params.put("palletNumberNumber", palletNumberNumber);
        params.put("typeOfPallet", typeOfPallet);

        if (Objects.nonNull(documentId)) {
            params.put("documentId", documentId);
        }

        if (Objects.nonNull(positionId)) {
            params.put("positionId", positionId);
        }

        return jdbcTemplate.queryForObject(query.toString(), params, Long.class) > 0;
    }

    public boolean existsOtherDeliveredProductForPalletNumber(final Long locationId, final String storageLocationNumber,
                                                              final String palletNumberNumber, final String palletType,
                                                              final Long deliveredProductId) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(deliveredproduct) FROM deliveries_deliveredproduct deliveredproduct ");
        query.append("JOIN deliveries_delivery delivery ");
        query.append("ON delivery.id = deliveredproduct.delivery_id ");
        query.append("LEFT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON deliveredproduct.storagelocation_id = storagelocation.id ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON deliveredproduct.palletnumber_id = palletnumber.id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND (");
        query.append("storageLocation.number <> :storageLocationNumber ");

        if (StringUtils.isNotEmpty(palletType)) {
            query.append("OR deliveredproduct.palletType <> :palletType OR COALESCE(deliveredproduct.palletType, '') = ''");
        } else {
            query.append("OR COALESCE(deliveredproduct.palletType, '') <> ''");
        }

        query.append(") ");
        query.append("AND delivery.location_id = :locationId ");
        query.append("AND delivery.state NOT IN ('04declined', '06received') ");

        if (Objects.nonNull(deliveredProductId)) {
            query.append("AND deliveredproduct.id <> :deliveredProductId ");
        }

        Map<String, Object> params = Maps.newHashMap();

        params.put("locationId", locationId);
        params.put("storageLocationNumber", storageLocationNumber);
        params.put("palletNumberNumber", palletNumberNumber);
        params.put("palletType", palletType);

        if (Objects.nonNull(deliveredProductId)) {
            params.put("deliveredProductId", deliveredProductId);
        }

        return jdbcTemplate.queryForObject(query.toString(), params, Long.class) > 0;
    }

    public boolean checkIfExistsMorePalletsForStorageLocation(final Long locationId, final String storageLocationNumber,
                                                              final String palletNumberNumber) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");
        query.append("CASE ");
        query.append("WHEN COALESCE(MAX(storagelocation.maximumNumberOfPallets), 0) = 0 THEN FALSE ");
        query.append("ELSE COUNT(DISTINCT(resource.palletnumber_id)) >= COALESCE(MAX(storagelocation.maximumNumberOfPallets), 0) ");
        query.append("END AS exists ");
        query.append("FROM materialflowresources_resource resource ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = resource.palletnumber_id ");
        query.append("RIGHT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON storagelocation.id = resource.storagelocation_id ");
        query.append("WHERE palletnumber.number <> :palletNumberNumber ");
        query.append("AND storagelocation.number = :storageLocationNumber ");
        query.append("AND storagelocation.placestoragelocation = true ");
        query.append("AND resource.location_id = :locationId");

        Map<String, Object> params = Maps.newHashMap();

        params.put("locationId", locationId);
        params.put("storageLocationNumber", storageLocationNumber);
        params.put("palletNumberNumber", palletNumberNumber);

        return jdbcTemplate.queryForObject(query.toString(), params, Boolean.class);
    }

    public boolean notTooManyPalletsInStorageLocation(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        Entity storageLocation = deliveredProduct.getBelongsToField("storageLocation");

        final BigDecimal maxNumberOfPallets;

        if (Objects.nonNull(storageLocation) && Objects
                .nonNull(maxNumberOfPallets = storageLocation.getDecimalField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS))) {

            Entity palletNumber = deliveredProduct.getBelongsToField("palletNumber");

            if (Objects.nonNull(palletNumber)) {
                String query = "SELECT count(DISTINCT palletsInStorageLocation.palletnumber_id) AS palletsCount     "
                        + "   FROM (SELECT                                                                          "
                        + "           resource.palletnumber_id,                                                     "
                        + "           resource.storagelocation_id                                                   "
                        + "         FROM materialflowresources_resource resource                                    "
                        + "         UNION ALL SELECT                                                                "
                        + "                     deliveredproduct.palletnumber_id,                                   "
                        + "                     deliveredproduct.storagelocation_id                                 "
                        + "                   FROM deliveries_delivery delivery                                     "
                        + "                     JOIN deliveries_deliveredproduct deliveredproduct                   "
                        + "                       ON deliveredproduct.delivery_id = delivery.id                     "
                        + "                   WHERE                                                                 "
                        + "                     delivery.state not in ('06received','04declined') AND                                      "
                        + "                     deliveredproduct.id <> :deliveredProductId                          "
                        + "        ) palletsInStorageLocation                                                       "
                        + "   WHERE palletsInStorageLocation.storagelocation_id = :storageLocationId AND            "
                        + "         palletsInStorageLocation.palletnumber_id <> :palletNumberId";

                Long deliveredProductId = Optional.ofNullable(deliveredProduct.getId()).orElse(-1L);
                Long palletsCount = jdbcTemplate.queryForObject(query,
                        new MapSqlParameterSource().addValue("storageLocationId", storageLocation.getId())
                                .addValue("palletNumberId", palletNumber.getId())
                                .addValue("deliveredProductId", deliveredProductId),
                        Long.class);

                boolean valid = maxNumberOfPallets.compareTo(BigDecimal.valueOf(palletsCount)) > 0;

                if (!valid) {
                    deliveredProduct.addError(deliveredProductDD.getField("storageLocation"),
                            "deliveries.deliveredProduct.error.storageLocationPalletLimitExceeded");
                }

                return valid;
            }
        }

        return true;
    }

    public boolean checkMaximumNumberOfPallets(final Entity storageLocation, final Entity resource) {
        return checkMaximumNumberOfPallets(storageLocation, resource, 1);
    }

    public boolean checkMaximumNumberOfPallets(final Entity storageLocation, final long palletsCount) {
        return checkMaximumNumberOfPallets(storageLocation, null, palletsCount);
    }

    private boolean checkMaximumNumberOfPallets(final Entity storageLocation, final Entity resource, final long palletsCount) {
        if (Objects.nonNull(storageLocation) && storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION)) {
            BigDecimal maximumNumberOfPallets = storageLocation.getDecimalField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS);

            BigDecimal palletsInStorageLocation = BigDecimal.valueOf(getPalletsCountInStorageLocation(storageLocation, resource) + palletsCount);

            return (Objects.nonNull(maximumNumberOfPallets) && (palletsInStorageLocation.compareTo(maximumNumberOfPallets) > 0));
        }

        return false;
    }

    private Long getPalletsCountInStorageLocation(final Entity storageLocation, final Entity resource) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT COUNT(DISTINCT palletnumber.id) AS palletsCount ");
        query.append("FROM materialflowresources_resource resource ");
        query.append("LEFT JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = resource.palletnumber_id ");
        query.append("WHERE resource.storagelocation_id = :storageLocationId ");

        Map<String, Object> params = Maps.newHashMap();

        params.put("storageLocationId", storageLocation.getId());

        if (Objects.nonNull(resource)) {
            if (Objects.nonNull(resource.getId())) {
                query.append("AND resource.id != :resourceId ");

                params.put("resourceId", resource.getId());
            }

            Entity palletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);

            if (Objects.nonNull(palletNumber)) {
                query.append("AND palletnumber.id != :palletNumberId ");

                params.put("palletNumberId", palletNumber.getId());
            }
        }

        return jdbcTemplate.queryForObject(query.toString(), params, Long.class);
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

}
