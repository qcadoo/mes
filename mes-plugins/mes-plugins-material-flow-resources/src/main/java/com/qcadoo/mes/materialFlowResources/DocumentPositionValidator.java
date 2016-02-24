package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseAlgorithm;

@Service
public class DocumentPositionValidator {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public Map<String, Object> validateAndTryMapBeforeCreate(DocumentPositionDTO documentPositionDTO) {
        return validate(documentPositionDTO);
    }

    public Map<String, Object> validateAndTryMapBeforeUpdate(DocumentPositionDTO documentPositionDTO) {
        return validate(documentPositionDTO);
    }

    public void validateBeforeDelete(Long id) {
    }

    private Map<String, Object> validate(DocumentPositionDTO position) {
        Preconditions.checkNotNull(position, "qcadooView.required.documentPosition");
        Preconditions.checkNotNull(position.getDocument(), "qcadooView.required.documentPosition.document");

        DocumentDTO document = jdbcTemplate.queryForObject("SELECT * FROM materialflowresources_document WHERE id = :id",
                Collections.singletonMap("id", position.getDocument()), new BeanPropertyRowMapper<DocumentDTO>(DocumentDTO.class));

        List<String> errors = new ArrayList<>();
        Map<String, Object> params = null;

        if (isGridReadOnly(document)) {
            errors.add("qcadooView.error.position.documentAccepted");

        } else {

            if (Strings.isNullOrEmpty(position.getProduct())) {
                errors.add("qcadooView.error.position.product.required");
            }
            if (Strings.isNullOrEmpty(position.getUnit())) {
                errors.add("qcadooView.error.position.unit.required");
            }

            errors.addAll(validateConversion(position));
            errors.addAll(validatePrice(position));
            errors.addAll(validateQuantity(position));
            errors.addAll(validateGivenquantity(position));
            errors.addAll(validateDates(position));
            errors.addAll(checkAttributesRequirement(position, document));
            errors.addAll(validateResources(position, document));

            params = tryMapDocumentPositionVOToParams(position, errors);
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(errors.stream().collect(Collectors.joining("\n")));
        }

        return params;
    }

    private boolean isGridReadOnly(DocumentDTO document) {
        return DocumentState.parseString(document.getState()) == DocumentState.ACCEPTED;
    }

    private List<String> checkAttributesRequirement(final DocumentPositionDTO position, final DocumentDTO document) {
        DocumentType documentType = DocumentType.parseString(document.getType());
        DocumentState documentState = DocumentState.parseString(document.getState());

        if (documentState == DocumentState.ACCEPTED
                && (documentType == DocumentType.RECEIPT || documentType == DocumentType.INTERNAL_INBOUND)) {
            LocationDTO warehouseTo = getWarehouseById(document.getLocationTo_id());

            return validatePositionAttributes(position,
                    warehouseTo.isRequirePrice(),
                    warehouseTo.isRequirebatch(),
                    warehouseTo.isRequirEproductionDate(),
                    warehouseTo.isRequirEexpirationDate());
        }

        return Arrays.asList();
    }

    private List<String> validatePositionAttributes(DocumentPositionDTO position, boolean requirePrice,
            boolean requireBatch, boolean requireProductionDate, boolean requireExpirationDate) {

        List<String> errors = new ArrayList<>();

        if (requirePrice && (position.getPrice() == null || BigDecimal.ZERO.compareTo(position.getPrice()) == 0)) {
            errors.add("qcadooView.error.position.price.required");
        }
        if (requireBatch && position.getBatch() == null) {
            errors.add("qcadooView.error.position.batch.required");
        }
        if (requireProductionDate && position.getProductiondate() == null) {
            errors.add("qcadooView.error.position.productionDate.required");
        }

        if (requireExpirationDate && position.getExpirationdate() == null) {
            errors.add("qcadooView.error.position.expirationDate.required");
        }

        return errors;
    }

    private List<String> validateResources(final DocumentPositionDTO position, final DocumentDTO document) {
        if (DocumentState.parseString(document.getState()).compareTo(DocumentState.ACCEPTED) == 0) {
            return Arrays.asList();
        }

        DocumentType type = DocumentType.parseString(document.getType());

        if (DocumentType.TRANSFER.equals(type) || DocumentType.RELEASE.equals(type)
                || DocumentType.INTERNAL_OUTBOUND.equals(type)) {
            LocationDTO warehouseFrom = getWarehouseById(document.getLocationFrom_id());
            String algorithm = warehouseFrom.getAlgorithm();
            if (WarehouseAlgorithm.MANUAL.getStringValue().compareTo(algorithm) == 0) {
                boolean isValid = position.getResource() != null;
                if (!isValid) {
                    return Arrays.asList("qcadooView.error.position.batch.required");
                }
            }
        }

        return Arrays.asList();
    }

    private List<String> validateDates(final DocumentPositionDTO position) {
        Date productionDate = position.getProductiondate();
        Date expirationDate = position.getExpirationdate();
        if (productionDate != null && expirationDate != null && expirationDate.compareTo(productionDate) < 0) {
            return Arrays.asList("qcadooView.error.position.expirationDate.lessThenProductionDate");
        }

        return Arrays.asList();
    }

    private LocationDTO getWarehouseById(Long id) {
        BeanPropertyRowMapper<LocationDTO> x = new BeanPropertyRowMapper<>(LocationDTO.class);
        x.setPrimitivesDefaultedForNullValue(true);

        return jdbcTemplate.queryForObject("SELECT * FROM materialflow_location WHERE id = :id",
                Collections.singletonMap("id", id), x);
    }

    private Collection<? extends String> validateQuantity(DocumentPositionDTO position) {
        if (position.getQuantity() == null) {
            return Arrays.asList("qcadooView.error.position.quantity.required");

        } else if (BigDecimal.ZERO.compareTo(position.getQuantity()) >= 0) {
            return Arrays.asList("qcadooView.error.position.quantity.invalid");
        }

        return validateBigDecimal(position.getQuantity(), "quantity", 5, 9);
    }

    private Collection<? extends String> validateGivenquantity(DocumentPositionDTO position) {
        if (position.getGivenquantity() == null) {
            return Arrays.asList("qcadooView.error.position.givenquantity.required");

        } else if (BigDecimal.ZERO.compareTo(position.getGivenquantity()) >= 0) {
            return Arrays.asList("qcadooView.error.position.givenquantity.invalid");
        }

        return validateBigDecimal(position.getGivenquantity(), "givenquantity", 5, 9);
    }

    private Collection<? extends String> validatePrice(DocumentPositionDTO position) {
        if (position.getPrice() != null && BigDecimal.ZERO.compareTo(position.getPrice()) > 0) {
            return Arrays.asList("qcadooView.error.position.price.invalid");
        }

        if (position.getPrice() != null) {
            return validateBigDecimal(position.getPrice(), "price", 5, 7);
        }

        return Arrays.asList();
    }

    private Collection<? extends String> validateConversion(DocumentPositionDTO position) {
        if (position.getConversion() == null) {
            return Arrays.asList("qcadooView.error.position.conversion.required");

        } else {
            if (BigDecimal.ZERO.compareTo(position.getConversion()) >= 0) {
                return Arrays.asList("qcadooView.error.position.conversion.invalid");
            }
            
            return validateBigDecimal(position.getConversion(), "conversion", 5, 7);
        }
    }

    private Map<String, Object> tryMapDocumentPositionVOToParams(DocumentPositionDTO vo, List<String> errors) {
        Map<String, Object> params = new HashMap<>();

        params.put("id", vo.getId());
        params.put("product_id", tryGetProductIdByNumber(vo.getProduct(), errors));
        params.put("additionalcode_id", tryGetAdditionalCodeIdByCode(vo.getAdditionalCode(), errors));
        params.put("quantity", vo.getQuantity());
        params.put("givenquantity", vo.getGivenquantity());
        params.put("givenunit", vo.getGivenunit());
        params.put("conversion", vo.getConversion());
        params.put("expirationdate", vo.getExpirationdate());
        params.put("palletnumber_id", tryGetPalletNumberIdByNumber(vo.getPalletNumber(), errors));
        params.put("typeofpallet", vo.getTypeOfPallet());
        params.put("storagelocation_id", tryGetStorageLocationIdByNumber(vo.getStorageLocation(), errors));
        params.put("document_id", vo.getDocument());
        params.put("productiondate", vo.getProductiondate());
        params.put("price", vo.getPrice());
        params.put("resource_id", tryGetResourceIdByNumber(vo.getResource(), errors));
        params.put("batch", vo.getBatch());

        return params;
    }

    private Long tryGetProductIdByNumber(String productNumber, List<String> errors) {
        if (Strings.isNullOrEmpty(productNumber)) {
            return null;
        }

        try {
            Long productId = jdbcTemplate.queryForObject("SELECT product.id FROM basic_product product WHERE product.number = :number", Collections.singletonMap("number", productNumber), Long.class);

            return productId;

        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiego produktu: '%s'.", productNumber));
            return null;
        }
    }

    private Long tryGetAdditionalCodeIdByCode(String additionalCode, List<String> errors) {
        if (Strings.isNullOrEmpty(additionalCode)) {
            return null;
        }

        try {
            Long additionalCodeId = jdbcTemplate.queryForObject("SELECT additionalcode.id FROM basic_additionalcode additionalcode WHERE additionalcode.code = :code",
                    Collections.singletonMap("code", additionalCode), Long.class);

            return additionalCodeId;

        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiego dodatkowego kodu: '%s'.", additionalCode));
            return null;
        }
    }

    private Long tryGetPalletNumberIdByNumber(String palletNumber, List<String> errors) {
        if (Strings.isNullOrEmpty(palletNumber)) {
            return null;
        }

        try {
            Long palletNumberId = jdbcTemplate.queryForObject("SELECT palletnumber.id FROM basic_palletnumber palletnumber WHERE palletnumber.number = :number",
                    Collections.singletonMap("number", palletNumber), Long.class);

            return palletNumberId;

        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiego numeru palety: '%s'.", palletNumber));
            return null;
        }
    }

    private Long tryGetStorageLocationIdByNumber(String storageLocationNumber, List<String> errors) {
        if (Strings.isNullOrEmpty(storageLocationNumber)) {
            return null;
        }

        try {
            Long storageLocationId = jdbcTemplate.queryForObject("SELECT storagelocation.id FROM materialflowresources_storagelocation storagelocation WHERE storagelocation.number = :number",
                    Collections.singletonMap("number", storageLocationNumber), Long.class);

            return storageLocationId;

        } catch (EmptyResultDataAccessException e) {
            errors.add(String.format("Nie znaleziono takiego miejsca składowania: '%s'.", storageLocationNumber));
            return null;
        }
    }

    private Object tryGetResourceIdByNumber(String resource, List<String> errors) {
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

    private List<String> validateBigDecimal(BigDecimal value, String field, int maxScale, int maxPrecision) {
        List<String> errors = new ArrayList<>();
        
        BigDecimal noZero = value.stripTrailingZeros();
        int scale = noZero.scale();
        int precision = noZero.precision();
        if (scale < 0) {
            precision -= scale;
            scale = 0;
        }
        String fieldName = translationService.translate("qcadooView.gridColumn." + field, LocaleContextHolder.getLocale());

        if (scale > maxScale) {
            errors.add(String.format(translationService.translate("qcadooView.error.position.bigdecimal.invalidScale", LocaleContextHolder.getLocale()), fieldName, maxScale));
        }
        if ((precision - scale) > maxPrecision) {
            errors.add(String.format(translationService.translate("qcadooView.error.position.bigdecimal.invalidPrecision", LocaleContextHolder.getLocale()), fieldName, maxPrecision));
        }

        return errors;
    }
}
