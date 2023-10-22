package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.BasicException;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.BigDecimalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentPositionValidator {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DocumentPositionService documentPositionService;

    public Map<String, Object> validateAndTryMapBeforeCreate(final DocumentPositionDTO documentPositionDTO) {
        return validateAndMap(documentPositionDTO);
    }

    public Map<String, Object> validateAndTryMapBeforeUpdate(final DocumentPositionDTO documentPositionDTO) {
        return validateAndMap(documentPositionDTO);
    }

    public void validateBeforeDelete(final Long id) {

    }

    private Map<String, Object> validateAndMap(final DocumentPositionDTO position) {
        Preconditions.checkNotNull(position, "documentGrid.required.documentPosition");
        Preconditions.checkNotNull(position.getDocument(), "documentGrid.required.documentPosition.document");

        DocumentDTO document = jdbcTemplate
                .queryForObject("SELECT * FROM materialflowresources_document WHERE id = :id", Collections.singletonMap("id",
                        position.getDocument()), new BeanPropertyRowMapper<DocumentDTO>(DocumentDTO.class));

        List<String> errors = Lists.newArrayList();

        Map<String, Object> params = null;

        if (isGridReadOnly(document)) {
            errors.add("documentGrid.error.position.documentAccepted");
        } else if (isAcceptationInProgress(document)) {
            errors.add("documentGrid.error.position.documentAcceptationInProgress");
        } else {
            if (Strings.isNullOrEmpty(position.getProduct())) {
                errors.add("documentGrid.error.position.product.required");
            }
            if (Strings.isNullOrEmpty(position.getUnit())) {
                errors.add("documentGrid.error.position.unit.required");
            }

            errors.addAll(validateConversion(position));
            errors.addAll(validatePrice(position));
            errors.addAll(validateSellingPrice(position));
            errors.addAll(validateQuantity(position));
            errors.addAll(validateGivenQuantity(position));
            errors.addAll(validateDates(position));
            errors.addAll(checkAttributesRequirement(position, document));
            errors.addAll(validateResources(position, document));
            errors.addAll(validatePallet(position, document));
            errors.addAll(validateAttributes(position, document));

            if (errors.isEmpty()) {
                errors.addAll(validateAvailableQuantity(position, document, errors));
            }
            params = tryMapDocumentPositionVOToParams(position, errors);
        }

        if (!errors.isEmpty()) {
            throw new BasicException(errors.stream().collect(Collectors.joining("\n")));
        }

        return params;
    }

    private boolean isGridReadOnly(final DocumentDTO document) {
        return DocumentState.parseString(document.getState()) == DocumentState.ACCEPTED;
    }

    private boolean isAcceptationInProgress(final DocumentDTO document) {
        return document.getAcceptationInProgress();
    }

    private List<String> checkAttributesRequirement(final DocumentPositionDTO position, final DocumentDTO document) {
        DocumentType documentType = DocumentType.parseString(document.getType());

        if (documentType == DocumentType.RECEIPT || documentType == DocumentType.INTERNAL_INBOUND) {
            LocationDTO warehouseTo = getWarehouseById(document.getLocationTo_id());

            return validatePositionAttributes(position, warehouseTo.isRequirePrice(), warehouseTo.isRequirebatch(),
                    warehouseTo.isRequirEproductionDate(), warehouseTo.isRequirEexpirationDate());
        }

        return Lists.newArrayList();
    }

    private List<String> validateAvailableQuantity(final DocumentPositionDTO position, final DocumentDTO document,
            final List<String> errors) {
        String type = document.getType();

        if (DocumentType.isOutbound(type)) {
            String query = "SELECT draftmakesreservation FROM materialflow_location WHERE id = :location_id";
            Boolean enabled = jdbcTemplate.queryForObject(query,
                    Collections.singletonMap("location_id", document.getLocationFrom_id()), Boolean.class);

            if (enabled) {
                BigDecimal availableQuantity = getAvailableQuantityForProductAndLocation(position,
                        tryGetProductIdByNumber(position.getProduct(), errors), document.getLocationFrom_id());
                BigDecimal quantity = position.getQuantity();

                if (availableQuantity == null || quantity.compareTo(availableQuantity) > 0) {
                    errors.add("documentGrid.error.position.quantity.notEnoughResources");
                } else {
                    if (!StringUtils.isEmpty(position.getResource())) {
                        BigDecimal resourceAvailableQuantity = getAvailableQuantityForResource(position,
                                tryGetProductIdByNumber(position.getProduct(), errors), document.getLocationFrom_id());

                        if (resourceAvailableQuantity == null || quantity.compareTo(resourceAvailableQuantity) > 0) {
                            errors.add("documentGrid.error.position.quantity.notEnoughResources");
                        }
                    }
                }
            }
        }

        return Lists.newArrayList();
    }

    private BigDecimal getAvailableQuantityForResource(final DocumentPositionDTO position, final Long productId,
            final Long locationId) {
        Long positionId = 0L;

        if (position != null) {
            positionId = position.getId();
        }

        Long resourceId = null;

        if (position != null && !StringUtils.isEmpty(position.getResource())) {
            ResourceDTO resource = documentPositionService.getResourceByNumber(position.getResource());

            if (resource != null) {
                resourceId = resource.getId();
            }
        }

        if (resourceId == null) {
            return BigDecimal.ZERO;
        }

        String query = "SELECT availableQuantity FROM materialflowresources_resource WHERE id = :resource_id";

        Map<String, Object> params = Maps.newHashMap();

        params.put("product_id", productId);
        params.put("location_id", locationId);
        params.put("position_id", positionId);
        params.put("resource_id", resourceId);

        BigDecimal availableQuantity = jdbcTemplate.query(query, params, new ResultSetExtractor<BigDecimal>() {

            @Override
            public BigDecimal extractData(ResultSet rs) throws SQLException, DataAccessException {
                return rs.next() ? rs.getBigDecimal("availableQuantity") : BigDecimal.ZERO;
            }
        });

        if (positionId != null && positionId != 0L) {
            String queryForOld = "SELECT product_id, quantity, resource_id FROM materialflowresources_position WHERE id = :position_id";

            Map<String, Object> oldPosition = jdbcTemplate.query(queryForOld, params,
                    new ResultSetExtractor<Map<String, Object>>() {

                        @Override
                        public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
                            Map<String, Object> result = Maps.newHashMap();
                            if (rs.next()) {
                                result.put("product_id", rs.getLong("product_id"));
                                result.put("quantity", rs.getBigDecimal("quantity"));
                                result.put("resource_id", rs.getLong("resource_id"));
                            }
                            return result;
                        }
                    });

            Long oldResource = (Long) oldPosition.get("resource_id");

            if (oldResource != null) {
                if (oldResource.compareTo(resourceId) == 0) {
                    availableQuantity = ((BigDecimal) oldPosition.get("quantity")).add(availableQuantity);
                }

            }
        }

        return availableQuantity;
    }

    private BigDecimal getAvailableQuantityForProductAndLocation(final DocumentPositionDTO position, final Long productId,
            final Long locationId) {
        Long positionId = 0L;

        if (position != null) {
            positionId = position.getId();
        }

        Long resourceId = null;

        if (position != null && !StringUtils.isEmpty(position.getResource())) {
            ResourceDTO resource = documentPositionService.getResourceByNumber(position.getResource());

            if (resource != null) {
                resourceId = resource.getId();
            }
        }

        String query = "SELECT availableQuantity FROM materialflowresources_resourcestockdto "
                + "WHERE product_id = :product_id AND location_id = :location_id";

        Map<String, Object> params = Maps.newHashMap();

        params.put("product_id", productId);
        params.put("location_id", locationId);
        params.put("position_id", positionId);
        params.put("resource_id", resourceId);

        BigDecimal availableQuantity = jdbcTemplate.query(query, params, new ResultSetExtractor<BigDecimal>() {

            @Override
            public BigDecimal extractData(ResultSet rs) throws SQLException, DataAccessException {
                return rs.next() ? rs.getBigDecimal("availableQuantity") : BigDecimal.ZERO;
            }
        });

        if (positionId != null && positionId != 0L) {
            String queryForOld = "SELECT product_id, quantity, resource_id FROM materialflowresources_position WHERE id = :position_id";

            Map<String, Object> oldPosition = jdbcTemplate.query(queryForOld, params,
                    new ResultSetExtractor<Map<String, Object>>() {

                        @Override
                        public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
                            Map<String, Object> result = Maps.newHashMap();

                            if (rs.next()) {
                                result.put("product_id", rs.getLong("product_id"));
                                result.put("quantity", rs.getBigDecimal("quantity"));
                                result.put("resource_id", rs.getLong("resource_id"));
                            }

                            return result;
                        }
                    });

            if (productId.compareTo((Long) oldPosition.get("product_id")) == 0) {
                availableQuantity = ((BigDecimal) oldPosition.get("quantity")).add(availableQuantity);
            }
        }

        return availableQuantity;
    }

    private List<String> validatePositionAttributes(final DocumentPositionDTO position, final boolean requirePrice,
            final boolean requireBatch, boolean requireProductionDate, boolean requireExpirationDate) {
        List<String> errors = Lists.newArrayList();

        if (requirePrice && (position.getPrice() == null || BigDecimal.ZERO.compareTo(position.getPrice()) == 0)) {
            errors.add("documentGrid.error.position.price.required");
        }
        if (requireBatch && (position.getBatch() == null || position.getBatch().trim().isEmpty())) {
            errors.add("documentGrid.error.position.batch.required");
        }
        if (requireProductionDate && position.getProductionDate() == null) {
            errors.add("documentGrid.error.position.productionDate.required");
        }

        if (requireExpirationDate && position.getExpirationDate() == null) {
            errors.add("documentGrid.error.position.expirationDate.required");
        }

        return errors;
    }

    private List<String> validateResources(final DocumentPositionDTO position, final DocumentDTO document) {
        if (DocumentState.parseString(document.getState()).compareTo(DocumentState.ACCEPTED) == 0) {
            return Lists.newArrayList();
        }

        if (!Strings.isNullOrEmpty(position.getResource())) {
            boolean find = false;

            DataResponse resourcesResponse = documentPositionService.getResourcesResponse(position.getDocument(), "",
                    position.getProduct(), position.getConversion(), position.getBatchId(), false);

            List<? extends AbstractDTO> resources = resourcesResponse.getEntities();

            for (AbstractDTO abstractDTO : resources) {
                ResourceDTO resourceDTO = (ResourceDTO) abstractDTO;

                if (position.getResource().equals(resourceDTO.getNumber())) {
                    find = true;

                    break;
                }
            }

            if (!find) {
                position.setResource(null);

                return Lists.newArrayList("documentGrid.error.position.resource.invalid");
            }
        }

        return Lists.newArrayList();
    }

    private List<String> validateDates(final DocumentPositionDTO position) {
        Date productionDate = position.getProductionDate();
        Date expirationDate = position.getExpirationDate();

        if (productionDate != null && expirationDate != null && expirationDate.compareTo(productionDate) < 0) {
            return Lists.newArrayList("documentGrid.error.position.expirationDate.lessThenProductionDate");
        }

        return Lists.newArrayList();
    }

    private LocationDTO getWarehouseById(final Long id) {
        BeanPropertyRowMapper<LocationDTO> x = new BeanPropertyRowMapper<>(LocationDTO.class);
        x.setPrimitivesDefaultedForNullValue(true);

        return jdbcTemplate.queryForObject("SELECT * FROM materialflow_location WHERE id = :id",
                Collections.singletonMap("id", id), x);
    }

    private Collection<? extends String> validateQuantity(final DocumentPositionDTO position) {
        if (position.getQuantity() == null) {
            return Lists.newArrayList("documentGrid.error.position.quantity.required");
        } else if (BigDecimal.ZERO.compareTo(position.getQuantity()) >= 0) {
            return Lists.newArrayList("documentGrid.error.position.quantity.invalid");
        }

        return validateBigDecimal(position.getQuantity(), "quantity", 5, 9);
    }

    private Collection<? extends String> validateGivenQuantity(final DocumentPositionDTO position) {
        if (position.getGivenquantity() == null) {
            return Lists.newArrayList("documentGrid.error.position.givenquantity.required");
        } else if (BigDecimal.ZERO.compareTo(position.getGivenquantity()) >= 0) {
            return Lists.newArrayList("documentGrid.error.position.givenquantity.invalid");
        }

        return validateBigDecimal(position.getGivenquantity(), "givenquantity", 5, 9);
    }

    private Collection<? extends String> validatePrice(final DocumentPositionDTO position) {
        if (position.getPrice() != null && BigDecimal.ZERO.compareTo(position.getPrice()) > 0) {
            return Lists.newArrayList("documentGrid.error.position.price.invalid");
        }

        if (position.getPrice() != null) {
            return validateBigDecimal(position.getPrice(), "price", 5, 7);
        }

        return Lists.newArrayList();
    }

    private Collection<? extends String> validateSellingPrice(final DocumentPositionDTO position) {
        if (position.getSellingPrice() != null && BigDecimal.ZERO.compareTo(position.getSellingPrice()) > 0) {
            return Lists.newArrayList("documentGrid.error.position.sellingPrice.invalid");
        }

        if (position.getSellingPrice() != null) {
            return validateBigDecimal(position.getSellingPrice(), "sellingPrice", 5, 7);
        }

        return Lists.newArrayList();
    }

    private Collection<? extends String> validateAttributes(DocumentPositionDTO position, DocumentDTO document) {
        List<String> errors = Lists.newArrayList();
        Map<String, Object> attrs = position.getAttrs();
        if (!attrs.isEmpty()) {
            String getAttributeValueByValue = "SELECT av.id FROM basic_attributevalue av WHERE av.attribute_id = :attrId AND value = :value";
            String getAttributes = "SELECT at.* FROM basic_attribute at WHERE at.number in (:numbers)";
            Map<String, Object> attDefinitionsParameters = Maps.newHashMap();
            attDefinitionsParameters.put("numbers", attrs.keySet());
            List<AttributeDto> attDefinitions = jdbcTemplate.query(getAttributes, attDefinitionsParameters,
                    new BeanPropertyRowMapper(AttributeDto.class));
            for (AttributeDto attributeDefinition : attDefinitions) {
                if (StringUtils.isNotEmpty((String) attrs.get(attributeDefinition.getNumber()))) {
                    if (attributeDefinition.getDataType().equals(AttributeDataType.CALCULATED.getStringValue())) {
                        Map<String, Object> parameters = Maps.newHashMap();
                        parameters.put("attrId", attributeDefinition.getId());
                        parameters.put("value", attrs.get(attributeDefinition.getNumber()));
                        List attrValues = jdbcTemplate.queryForList(getAttributeValueByValue.toString(), parameters);
                        if (attrValues.isEmpty()) {
                            errors.add(translationService.translate("documentGrid.error.position.attribiuteNotFound",
                                    LocaleContextHolder.getLocale(), attributeDefinition.getNumber()));
                        }
                    } else if (attributeDefinition.getValueType().equals(AttributeValueType.NUMERIC.getStringValue())) {
                        errors.addAll(validateBigDecimalFromAttribute((String) attrs.get(attributeDefinition.getNumber()),
                                attributeDefinition));
                    }
                }

            }

        }
        return errors;
    }

    private Collection<? extends String> validateConversion(final DocumentPositionDTO position) {
        if (position.getConversion() == null) {
            return Lists.newArrayList("documentGrid.error.position.conversion.required");
        } else {
            if (BigDecimal.ZERO.compareTo(position.getConversion()) >= 0) {
                return Lists.newArrayList("documentGrid.error.position.conversion.invalid");
            }

            return validateBigDecimal(position.getConversion(), "conversion", 5, 7);
        }
    }

    private Map<String, Object> tryMapDocumentPositionVOToParams(final DocumentPositionDTO vo, final List<String> errors) {
        Map<String, Object> params = Maps.newHashMap();

        params.put("id", vo.getId());
        params.put("product_id", tryGetProductIdByNumber(vo.getProduct(), errors));
        params.put("quantity", vo.getQuantity());
        params.put("givenquantity", vo.getGivenquantity());
        params.put("givenunit", vo.getGivenunit());
        params.put("conversion", vo.getUnit().equals(vo.getGivenunit()) ? 1 : vo.getConversion());
        params.put("expirationDate", vo.getExpirationDate());
        params.put("palletnumber_id", tryGetPalletNumberIdByNumber(vo.getPalletNumber(), errors));
        params.put("typeofpallet", vo.getTypeOfPallet());
        params.put("storagelocation_id", tryGetStorageLocationIdByNumber(vo.getStorageLocation(), errors));
        params.put("document_id", vo.getDocument());
        params.put("productionDate", vo.getProductionDate());
        params.put("price", vo.getPrice());
        params.put("resource_id", tryGetResourceIdByNumber(vo.getResource(), errors));
        params.put("batch_id", vo.getBatchId());
        params.put("waste", vo.isWaste());
        params.put("lastResource", vo.getLastResource());
        params.put("sellingPrice", vo.getSellingPrice());

        return params;
    }

    private Object tryGetBatchIdByNumber(final String batch, List<String> errors) {
        if (Strings.isNullOrEmpty(batch)) {
            return null;
        }

        try {
            Long productId = jdbcTemplate.queryForObject(
                    "SELECT _batch.id FROM advancedgenealogy_batch _batch WHERE _batch.number = :number",
                    Collections.singletonMap("number", batch), Long.class);

            return productId;
        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiej parti: '%s'.", batch));

            return null;
        }
    }

    private Long tryGetProductIdByNumber(final String productNumber, final List<String> errors) {
        if (Strings.isNullOrEmpty(productNumber)) {
            return null;
        }

        try {
            Long productId = jdbcTemplate.queryForObject(
                    "SELECT product.id FROM basic_product product WHERE product.number = :number",
                    Collections.singletonMap("number", productNumber), Long.class);

            return productId;
        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiego produktu: '%s'.", productNumber));

            return null;
        }
    }

    private Long tryGetPalletNumberIdByNumber(final String palletNumber, final List<String> errors) {
        if (Strings.isNullOrEmpty(palletNumber)) {
            return null;
        }

        try {
            Long palletNumberId = jdbcTemplate.queryForObject(
                    "SELECT palletnumber.id FROM basic_palletnumber palletnumber WHERE palletnumber.number = :number",
                    Collections.singletonMap("number", palletNumber), Long.class);

            return palletNumberId;
        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiego numeru palety: '%s'.", palletNumber));

            return null;
        }
    }

    private Long tryGetStorageLocationIdByNumber(final String storageLocationNumber, final List<String> errors) {
        if (Strings.isNullOrEmpty(storageLocationNumber)) {
            return null;
        }

        try {
            Long storageLocationId = jdbcTemplate
                    .queryForObject(
                            "SELECT storagelocation.id FROM materialflowresources_storagelocation storagelocation WHERE storagelocation.number = :number",
                            Collections.singletonMap("number", storageLocationNumber), Long.class);

            return storageLocationId;
        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiego miejsca składowania: '%s'.", storageLocationNumber));

            return null;
        }
    }

    private Object tryGetResourceIdByNumber(final String resource, final List<String> errors) {
        if (Strings.isNullOrEmpty(resource)) {
            return null;
        }

        try {
            Long resourceId = jdbcTemplate.queryForObject("SELECT id FROM materialflowresources_resource WHERE number = :number",
                    Collections.singletonMap("number", resource), Long.class);

            return resourceId;
        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiego zasobu: '%s'.", resource));

            return null;
        }
    }

    private List<String> validateBigDecimal(final BigDecimal value, final String field, final int maxScale, final int maxPrecision) {
        List<String> errors = Lists.newArrayList();

        BigDecimal noZero = value.stripTrailingZeros();

        int scale = noZero.scale();
        int precision = noZero.precision();

        if (scale < 0) {
            precision -= scale;
            scale = 0;
        }

        String fieldName = translationService.translate("documentGrid.gridColumn." + field, LocaleContextHolder.getLocale());

        if (scale > maxScale) {
            errors.add(String.format(
                    translationService.translate("documentGrid.error.position.bigdecimal.invalidScale",
                            LocaleContextHolder.getLocale()), fieldName, maxScale));
        }

        if ((precision - scale) > maxPrecision) {
            errors.add(String.format(
                    translationService.translate("documentGrid.error.position.bigdecimal.invalidPrecision",
                            LocaleContextHolder.getLocale()), fieldName, maxPrecision));
        }

        return errors;
    }

    private List<String> validateBigDecimalFromAttribute(final String value, final AttributeDto attribute) {
        List<String> errors = Lists.newArrayList();
        Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator((String) value,
                LocaleContextHolder.getLocale());

        if (eitherNumber.isLeft()) {
            errors.add(String.format(translationService.translate("documentGrid.error.position.bigdecimal.invalidNumericFormat",
                    LocaleContextHolder.getLocale()), attribute.getNumber()));
            return errors;
        }

        BigDecimal val = eitherNumber.getRight().get();
        int scale = attribute.getPrecision();
        int valueScale = val.scale();
        if (valueScale > scale) {
            errors.add(String.format(
                    translationService.translate("documentGrid.error.position.bigdecimal.invalidScale",
                            LocaleContextHolder.getLocale()), attribute.getNumber(), scale));
        }

        return errors;
    }

    private Collection<? extends String> validatePallet(final DocumentPositionDTO position, final DocumentDTO document) {
        List<String> errors = Lists.newArrayList();

        if (DocumentType.isInbound(document.getType())) {
            if (existsNotMatchingResourceForPalletNumber(position, document)) {
                errors.add(translationService.translate(
                        "documentGrid.error.position.existsOtherResourceForPalletAndStorageLocation",
                        LocaleContextHolder.getLocale()));
            } else if (existsNotMatchingPositionForPalletNumber(position, document)) {
                errors.add(translationService.translate(
                        "documentGrid.error.position.existsOtherPositionForPalletAndStorageLocation",
                        LocaleContextHolder.getLocale()));
            } else if (existsNotMatchingDeliveredProductForPalletNumber(position, document)) {
                errors.add(translationService.translate(
                        "documentGrid.error.position.existsOtherDeliveredProductForPalletAndStorageLocation",
                        LocaleContextHolder.getLocale()));
            }
        }

        return errors;
    }

    private boolean existsNotMatchingResourceForPalletNumber(final DocumentPositionDTO position, final DocumentDTO document) {
        if (Strings.isNullOrEmpty(position.getPalletNumber())) {
            return false;
        }

        StringBuilder query = new StringBuilder();

        query.append("SELECT count(*) FROM materialflowresources_resource resource ");
        query.append("JOIN basic_palletnumber pallet ON (resource.palletnumber_id = pallet.id) ");
        query.append("LEFT JOIN materialflowresources_storagelocation storage ON (resource.storagelocation_id = storage.id) ");
        query.append("WHERE pallet.number = :palletNumber AND (storage.number <> :storageNumber OR resource.typeofpallet <> :typeOfPallet) ");
        query.append("AND resource.location_id = :locationId");

        Map<String, Object> params = Maps.newHashMap();

        params.put("palletNumber", position.getPalletNumber());
        params.put("storageNumber", position.getStorageLocation());
        params.put("typeOfPallet", position.getTypeOfPallet());
        params.put("locationId", document.getLocationTo_id());

        Long count = jdbcTemplate.queryForObject(query.toString(), params, Long.class);

        return count > 0;
    }

    private boolean existsNotMatchingPositionForPalletNumber(final DocumentPositionDTO position, final DocumentDTO document) {
        if (Strings.isNullOrEmpty(position.getPalletNumber())) {
            return false;
        }

        StringBuilder query = new StringBuilder();

        query.append("SELECT count(*) FROM materialflowresources_position position ");
        query.append("JOIN basic_palletnumber pallet ON (position.palletnumber_id = pallet.id) ");
        query.append("LEFT JOIN materialflowresources_storagelocation storage ON (position.storagelocation_id = storage.id) ");
        query.append("WHERE pallet.number = :palletNumber AND (storage.number <> :storageNumber OR position.typeofpallet <> :typeOfPallet) ");
        query.append("AND position.document_id = :documentId AND position.id <> :positionId ");

        Map<String, Object> params = Maps.newHashMap();

        params.put("palletNumber", position.getPalletNumber());
        params.put("storageNumber", position.getStorageLocation());
        params.put("typeOfPallet", position.getTypeOfPallet());
        params.put("documentId", position.getDocument());
        params.put("positionId", position.getId());

        Long count = jdbcTemplate.queryForObject(query.toString(), params, Long.class);

        return count > 0;
    }

    private boolean existsNotMatchingDeliveredProductForPalletNumber(final DocumentPositionDTO position,
            final DocumentDTO document) {
        if (Strings.isNullOrEmpty(position.getPalletNumber())) {
            return false;
        }

        StringBuilder query = new StringBuilder();

        query.append("SELECT count(*) FROM deliveries_deliveredproduct position ");
        query.append("JOIN basic_palletnumber pallet ON (position.palletnumber_id = pallet.id) ");
        query.append("JOIN deliveries_delivery delivery ON position.delivery_id = delivery.id ");
        query.append("LEFT JOIN materialflowresources_storagelocation storage ON (position.storagelocation_id = storage.id) ");
        query.append("WHERE pallet.number = :palletNumber AND (storage.number <> :storageNumber OR position.pallettype <> :typeOfPallet) ");
        query.append("AND delivery.location_id = :locationId");

        Map<String, Object> params = Maps.newHashMap();

        params.put("palletNumber", position.getPalletNumber());
        params.put("storageNumber", position.getStorageLocation());
        params.put("typeOfPallet", position.getTypeOfPallet());
        params.put("locationId", document.getLocationTo_id());

        Long count = jdbcTemplate.queryForObject(query.toString(), params, Long.class);

        return count > 0;
    }

}
