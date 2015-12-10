package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseAlgorithm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentPositionValidator {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void validateBeforeCreate(DocumentPositionDTO documentPositionDTO) {
        validate(documentPositionDTO);
    }

    public void validateBeforeUpdate(DocumentPositionDTO documentPositionDTO) {
        validate(documentPositionDTO);
    }

    public void validateBeforeDelete(Long id) {
    }

    private void validate(DocumentPositionDTO position) {
        Preconditions.checkNotNull(position, "qcadooView.required.documentPosition");
        Preconditions.checkNotNull(position.getDocument(), "qcadooView.required.documentPosition.document");

        DocumentDTO document = jdbcTemplate.queryForObject("SELECT * FROM materialflowresources_document WHERE id = :id",
                Collections.singletonMap("id", position.getDocument()), new BeanPropertyRowMapper<DocumentDTO>(DocumentDTO.class));

        List<String> errors = new ArrayList<>();
        errors.addAll(validateDates(position));
        errors.addAll(checkAttributesRequirement(position, document));
        errors.addAll(validateResources(position, document));
        
        if(!errors.isEmpty()){
            throw new RuntimeException(errors.stream().collect(Collectors.joining("\n")));
        }
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

        boolean result = true;
        if (requirePrice && position.getPrice() == null) {
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
}
