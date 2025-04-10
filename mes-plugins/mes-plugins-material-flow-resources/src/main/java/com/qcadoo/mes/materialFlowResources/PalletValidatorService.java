package com.qcadoo.mes.materialFlowResources;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.TypeOfLoadUnitFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

    private static final String TYPE_OF_LOAD_UNIT = "typeOfLoadUnit";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public boolean validatePalletForResource(final Entity resource) {
        Entity location = resource.getBelongsToField(ResourceFields.LOCATION);
        Entity storageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
        Entity palletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
        Entity typeOfLoadUnit = resource.getBelongsToField(ResourceFields.TYPE_OF_LOAD_UNIT);

        return validatePalletForResource(location, storageLocation, palletNumber, typeOfLoadUnit, resource);
    }

    private boolean validatePalletForResource(final Entity location, final Entity storageLocation,
                                              final Entity palletNumber, final Entity typeOfLoadUnit,
                                              final Entity entity) {
        if (Objects.isNull(entity.getField(L_VALIDATE_PALLET)) || entity.getBooleanField(L_VALIDATE_PALLET)) {
            boolean isValid = validateRequiredFields(storageLocation, palletNumber, entity);

            isValid = isValid && validateResources(location, storageLocation, palletNumber, typeOfLoadUnit, entity);

            return isValid;
        }

        return true;
    }

    private boolean validateResources(final Entity location, final Entity storageLocation, final Entity palletNumber,
                                      final Entity typeOfLoadUnit, final Entity entity) {
        String palletNumberNumber = Objects.nonNull(palletNumber) ? palletNumber.getStringField(PalletNumberFields.NUMBER) : null;
        String storageLocationNumber = Objects.nonNull(storageLocation) ? storageLocation.getStringField(StorageLocationFields.NUMBER) : null;
        String typeOfLoadUnitName = Objects.nonNull(typeOfLoadUnit) ? typeOfLoadUnit.getStringField(TypeOfLoadUnitFields.NAME) : null;
        Long resourceId = getEntityId(entity, MaterialFlowResourcesConstants.MODEL_RESOURCE);

        return validateResources(location, storageLocationNumber, palletNumberNumber, typeOfLoadUnitName, entity, resourceId);
    }

    public boolean validatePalletForDeliveredProduct(final Entity deliveredProduct) {
        Entity location = deliveredProduct.getBelongsToField(L_DELIVERY).getBelongsToField(L_LOCATION);
        Entity storageLocation = deliveredProduct.getBelongsToField(L_STORAGE_LOCATION);
        Entity palletNumber = deliveredProduct.getBelongsToField(L_PALLET_NUMBER);
        Entity typeOfLoadUnit = deliveredProduct.getBelongsToField(TYPE_OF_LOAD_UNIT);

        if (Objects.isNull(location)) {
            return true;
        }

        return validatePalletForDeliveredProduct(location, storageLocation, palletNumber, typeOfLoadUnit, deliveredProduct) &&
                notTooManyPalletsInStorageLocationAndDeliveredProducts(deliveredProduct.getDataDefinition(), deliveredProduct);
    }

    private boolean validatePalletForDeliveredProduct(final Entity location, final Entity storageLocation,
                                                      final Entity palletNumber, final Entity typeOfLoadUnit,
                                                      final Entity entity) {
        if (Objects.isNull(entity.getField(L_VALIDATE_PALLET)) || entity.getBooleanField(L_VALIDATE_PALLET)) {
            boolean isValid = validateRequiredFields(storageLocation, palletNumber, entity);

            isValid = isValid && validatePalletNumberAndTypeOfLoadUnit(location, storageLocation, palletNumber, typeOfLoadUnit, entity);

            return isValid;
        }

        return true;
    }

    private boolean validateRequiredFields(final Entity storageLocation, final Entity palletNumber,
                                           final Entity entity) {
        if (Objects.isNull(storageLocation) && Objects.nonNull(palletNumber)) {
            entity.addError(entity.getDataDefinition().getField(L_STORAGE_LOCATION), "qcadooView.validate.field.error.missing");

            return false;
        } else if (Objects.nonNull(storageLocation)) {
            boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

            if (placeStorageLocation) {
                if (Objects.isNull(palletNumber)) {
                    entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER), "qcadooView.validate.field.error.missing");

                    return false;
                }
            }
        }

        return true;
    }

    public boolean validatePalletNumberAndTypeOfLoadUnit(final Entity location, final Entity storageLocation,
                                                         final Entity palletNumber, final Entity typeOfLoadUnit,
                                                         final Entity entity) {
        String palletNumberNumber = Objects.nonNull(palletNumber) ? palletNumber.getStringField(PalletNumberFields.NUMBER) : null;
        String storageLocationNumber = Objects.nonNull(storageLocation) ? storageLocation.getStringField(StorageLocationFields.NUMBER) : null;
        String typeOfLoadUnitName = Objects.nonNull(typeOfLoadUnit) ? typeOfLoadUnit.getStringField(TypeOfLoadUnitFields.NAME) : null;
        Long resourceId = getEntityId(entity, MaterialFlowResourcesConstants.MODEL_RESOURCE);
        Long deliveredProductId = getEntityId(entity, L_DELIVERED_PRODUCT);

        if (validateResources(location, storageLocationNumber, palletNumberNumber, typeOfLoadUnitName, entity, resourceId)) {
            return validatePositionsAndDeliveredProducts(location, storageLocationNumber, palletNumberNumber, typeOfLoadUnitName, entity, deliveredProductId);
        }

        return false;
    }

    private boolean validateResources(final Entity location, final String storageLocationNumber,
                                      final String palletNumberNumber, final String typeOfLoadUnitName,
                                      final Entity entity, final Long resourceId) {
        if (existsOtherResourceForPalletNumberOnOtherLocations(location.getId(), storageLocationNumber, resourceId)) {
            entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER),
                    "documentGrid.error.position.existsOtherResourceForPallet");

            return false;
        }

        if (existsOtherResourceForPalletNumberOnDifferentLocation(location.getId(), storageLocationNumber, palletNumberNumber, resourceId)) {
            entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER),
                    "documentGrid.error.position.existsOtherResourceForPalletAndStorageLocation", palletNumberNumber);

            return false;
        }

        if (existsOtherResourceForPalletNumberWithDifferentType(location.getId(), palletNumberNumber, typeOfLoadUnitName, resourceId)) {
            entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER),
                    "documentGrid.error.position.existsOtherResourceForLoadUnitAndTypeOfLoadUnit", palletNumberNumber);

            return false;
        }

        return true;
    }

    private boolean validatePositionsAndDeliveredProducts(final Entity location, final String storageLocationNumber,
                                                          final String palletNumberNumber,
                                                          final String typeOfLoadUnitName, final Entity entity,
                                                          final Long deliveredProductId) {
        if (existsOtherPositionForPalletNumber(location.getId(), storageLocationNumber, palletNumberNumber, typeOfLoadUnitName, null, null)) {
            entity.addError(entity.getDataDefinition().getField(L_PALLET_NUMBER),
                    "documentGrid.error.position.existsOtherPositionForPalletAndStorageLocation");

            return false;
        }

        if (existsOtherDeliveredProductForPalletNumber(location.getId(), storageLocationNumber, palletNumberNumber, typeOfLoadUnitName, deliveredProductId)) {
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

    public boolean existsOtherResourceForPalletNumberOnOtherLocations(final Long locationId,
                                                                      final String palletNumberNumber,
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

    public boolean existsOtherResourceForPalletNumberOnDifferentLocation(final Long locationId,
                                                                         final String storageLocationNumber,
                                                                         final String palletNumberNumber,
                                                                         final Long resourceId) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(*) FROM materialflowresources_resource resource ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = resource.palletnumber_id ");
        query.append("LEFT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON storagelocation.id = resource.storagelocation_id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND (storageLocation.number <> :storageLocationNumber) ");
        query.append("AND resource.location_id = :locationId ");

        if (Objects.nonNull(resourceId)) {
            query.append("AND resource.id <> :resourceId ");
        }

        Map<String, Object> params = Maps.newHashMap();

        params.put("locationId", locationId);
        params.put("storageLocationNumber", storageLocationNumber);
        params.put("palletNumberNumber", palletNumberNumber);

        if (Objects.nonNull(resourceId)) {
            params.put("resourceId", resourceId);
        }

        return jdbcTemplate.queryForObject(query.toString(), params, Long.class) > 0;
    }

    public boolean existsOtherResourceForPalletNumberWithDifferentType(final Long locationId,
                                                                       final String palletNumberNumber,
                                                                       final String typeOfLoadUnitName,
                                                                       final Long resourceId) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(*) FROM materialflowresources_resource resource ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = resource.palletnumber_id ");
        query.append("LEFT JOIN basic_typeofloadunit typeofloadunit ");
        query.append("ON typeofloadunit.id = resource.typeofloadunit_id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND (");
        if (StringUtils.isNotEmpty(typeOfLoadUnitName)) {
            query.append("typeofloadunit.name <> :typeOfLoadUnitName OR COALESCE(typeofloadunit.name, '') = ''");
        } else {
            query.append("COALESCE(typeofloadunit.name, '') <> ''");
        }

        query.append(") ");
        query.append("AND resource.location_id = :locationId ");

        if (Objects.nonNull(resourceId)) {
            query.append("AND resource.id <> :resourceId ");
        }

        Map<String, Object> params = Maps.newHashMap();

        params.put("locationId", locationId);
        params.put("palletNumberNumber", palletNumberNumber);
        params.put("typeOfLoadUnitName", typeOfLoadUnitName);

        if (Objects.nonNull(resourceId)) {
            params.put("resourceId", resourceId);
        }

        return jdbcTemplate.queryForObject(query.toString(), params, Long.class) > 0;
    }

    public boolean existsOtherPositionForPalletNumber(final Long locationId, final String storageLocationNumber,
                                                      final String palletNumberNumber, final String typeOfLoadUnitName,
                                                      final Long positionId, final Long documentId) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(position) FROM materialflowresources_position position ");
        query.append("JOIN materialflowresources_document document ");
        query.append("ON document.id = position.document_id ");
        query.append("LEFT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON storagelocation.id = position.storagelocation_id ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON palletnumber.id = position.palletnumber_id ");
        query.append("LEFT JOIN basic_typeofloadunit typeofloadunit ");
        query.append("ON typeofloadunit.id = position.typeofloadunit_id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND (");
        query.append("storageLocation.number <> :storageLocationNumber ");

        if (StringUtils.isNotEmpty(typeOfLoadUnitName)) {
            query.append("OR typeofloadunit.name <> :typeOfLoadUnitName OR COALESCE(typeofloadunit.name, '') = ''");
        } else {
            query.append("OR COALESCE(typeofloadunit.name, '') <> ''");
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
        params.put("typeOfLoadUnitName", typeOfLoadUnitName);

        if (Objects.nonNull(documentId)) {
            params.put("documentId", documentId);
        }

        if (Objects.nonNull(positionId)) {
            params.put("positionId", positionId);
        }

        return jdbcTemplate.queryForObject(query.toString(), params, Long.class) > 0;
    }

    public boolean existsOtherDeliveredProductForPalletNumber(final Long locationId, final String storageLocationNumber,
                                                              final String palletNumberNumber,
                                                              final String typeOfLoadUnitName,
                                                              final Long deliveredProductId) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(deliveredproduct) FROM deliveries_deliveredproduct deliveredproduct ");
        query.append("JOIN deliveries_delivery delivery ");
        query.append("ON delivery.id = deliveredproduct.delivery_id ");
        query.append("LEFT JOIN materialflowresources_storagelocation storagelocation ");
        query.append("ON deliveredproduct.storagelocation_id = storagelocation.id ");
        query.append("JOIN basic_palletnumber palletnumber ");
        query.append("ON deliveredproduct.palletnumber_id = palletnumber.id ");
        query.append("LEFT JOIN basic_typeofloadunit typeofloadunit ");
        query.append("ON typeofloadunit.id = deliveredproduct.typeofloadunit_id ");
        query.append("WHERE palletnumber.number = :palletNumberNumber ");
        query.append("AND (");
        query.append("storageLocation.number <> :storageLocationNumber ");

        if (StringUtils.isNotEmpty(typeOfLoadUnitName)) {
            query.append("OR typeofloadunit.name <> :typeOfLoadUnitName OR COALESCE(typeofloadunit.name, '') = ''");
        } else {
            query.append("OR COALESCE(typeofloadunit.name, '') <> ''");
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
        params.put("typeOfLoadUnitName", typeOfLoadUnitName);

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

    public boolean tooManyPalletsInStorageLocationAndPositions(final String storageLocationNumber,
                                                               final String palletNumberNumber,
                                                               final Long positionId, Long documentId) {
        if (Objects.nonNull(storageLocationNumber) && isPlaceStorageLocation(storageLocationNumber)) {
            if (Objects.nonNull(palletNumberNumber)) {
                StringBuilder query = new StringBuilder();

                query.append("SELECT ");
                query.append("COUNT(DISTINCT palletsInStorageLocation.palletnumber_id) AS palletsCount ");
                query.append("FROM (");
                query.append("SELECT ");
                query.append("resource.palletnumber_id, ");
                query.append("resource.storagelocation_id ");
                query.append("FROM materialflowresources_resource resource ");
                query.append("UNION ALL ");
                query.append("SELECT ");
                query.append("position.palletnumber_id, ");
                query.append("position.storagelocation_id ");
                query.append("FROM materialflowresources_position position ");
                query.append("WHERE position.id <> :positionId ");
                query.append("AND position.document_id = :documentId ");
                query.append(") palletsInStorageLocation ");
                query.append("JOIN materialflowresources_storagelocation storagelocation ");
                query.append("ON storagelocation.id = palletsInStorageLocation.storagelocation_id ");
                query.append("JOIN basic_palletnumber palletnumber ");
                query.append("ON palletnumber.id = palletsInStorageLocation.palletnumber_id ");
                query.append("WHERE storagelocation.number = :storageLocationNumber ");
                query.append("AND palletnumber.number <> :palletNumberNumber");

                Map<String, Object> params = Maps.newHashMap();

                params.put("storageLocationNumber", storageLocationNumber);
                params.put("palletNumberNumber", palletNumberNumber);
                params.put("positionId", Optional.ofNullable(positionId).orElse(-1L));
                params.put("documentId", documentId);

                Long palletsInStorageLocation = jdbcTemplate.queryForObject(query.toString(), params, Long.class);

                BigDecimal maximumNumberOfPallets = getMaximumNumberOfPallets(storageLocationNumber);

                return Objects.nonNull(maximumNumberOfPallets) && (BigDecimal.valueOf(palletsInStorageLocation + 1).compareTo(maximumNumberOfPallets) > 0);
            }
        }

        return false;
    }

    public boolean notTooManyPalletsInStorageLocationAndDeliveredProducts(final DataDefinition deliveredProductDD,
                                                                          final Entity deliveredProduct) {
        Entity storageLocation = deliveredProduct.getBelongsToField("storageLocation");

        if (Objects.nonNull(storageLocation) && storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION)) {
            Entity palletNumber = deliveredProduct.getBelongsToField("palletNumber");

            if (Objects.nonNull(palletNumber)) {
                StringBuilder query = new StringBuilder();

                query.append("SELECT ");
                query.append("COUNT(DISTINCT palletsInStorageLocation.palletnumber_id) AS palletsCount ");
                query.append("FROM (");
                query.append("SELECT ");
                query.append("resource.palletnumber_id, ");
                query.append("resource.storagelocation_id ");
                query.append("FROM materialflowresources_resource resource ");
                query.append("UNION ALL ");
                query.append("SELECT ");
                query.append("deliveredproduct.palletnumber_id, ");
                query.append("deliveredproduct.storagelocation_id ");
                query.append("FROM deliveries_delivery delivery ");
                query.append("JOIN deliveries_deliveredproduct deliveredproduct ");
                query.append("ON deliveredproduct.delivery_id = delivery.id ");
                query.append("WHERE delivery.id = :deliveryId ");
                query.append("AND deliveredproduct.id <> :deliveredProductId ");
                query.append(") palletsInStorageLocation ");
                query.append("WHERE palletsInStorageLocation.storagelocation_id = :storageLocationId ");
                query.append("AND palletsInStorageLocation.palletnumber_id <> :palletNumberId");

                Long deliveredProductId = Optional.ofNullable(deliveredProduct.getId()).orElse(-1L);

                Map<String, Object> params = Maps.newHashMap();

                params.put("storageLocationId", storageLocation.getId());
                params.put("palletNumberId", palletNumber.getId());
                params.put("deliveredProductId", deliveredProductId);
                params.put("deliveryId", deliveredProduct.getBelongsToField(L_DELIVERY).getId());

                Long palletsInStorageLocation = jdbcTemplate.queryForObject(query.toString(), params, Long.class);

                BigDecimal maximumNumberOfPallets = storageLocation.getDecimalField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS);

                if (Objects.nonNull(maximumNumberOfPallets) && (BigDecimal.valueOf(palletsInStorageLocation + 1).compareTo(maximumNumberOfPallets) > 0)) {
                    deliveredProduct.addError(deliveredProductDD.getField("storageLocation"),
                            "deliveries.deliveredProduct.error.storageLocationPalletLimitExceeded");

                    return false;
                }
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

    private boolean checkMaximumNumberOfPallets(final Entity storageLocation, final Entity resource,
                                                final long palletsCount) {
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

    public boolean checkPalletNumbersInStorageLocation(final Entity storageLocation) {
        if (Objects.nonNull(storageLocation) && storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION)) {
            BigDecimal palletsWithoutNumbersInStorageLocation = BigDecimal.valueOf(getPalletsWithoutNumbersCountInStorageLocation(storageLocation));

            return (palletsWithoutNumbersInStorageLocation.compareTo(BigDecimal.ZERO) > 0);
        }

        return false;
    }

    private Long getPalletsWithoutNumbersCountInStorageLocation(final Entity storageLocation) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT COUNT(*) AS palletsCount ");
        query.append("FROM materialflowresources_resource resource ");
        query.append("WHERE resource.storagelocation_id = :storageLocationId ");
        query.append("AND resource.palletnumber_id IS NULL");

        Map<String, Object> params = Maps.newHashMap();

        params.put("storageLocationId", storageLocation.getId());

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

    public BigDecimal getMaximumNumberOfPallets(final String storageLocationNumber) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT maximumnumberofpallets ");
        query.append("FROM materialflowresources_storagelocation ");
        query.append("WHERE number = :storageLocationNumber");

        Map<String, Object> params = Maps.newHashMap();

        params.put("storageLocationNumber", storageLocationNumber);

        try {
            return jdbcTemplate.queryForObject(query.toString(), params, BigDecimal.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
