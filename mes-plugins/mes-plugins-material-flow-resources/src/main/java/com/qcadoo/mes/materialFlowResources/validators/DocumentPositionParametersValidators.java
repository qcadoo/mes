package com.qcadoo.mes.materialFlowResources.validators;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DocumentPositionParametersValidators {

    private static final List<String> DOCUMENT_TYPES = Lists.newArrayList(DocumentType.INTERNAL_OUTBOUND.getStringValue(),
            DocumentType.RELEASE.getStringValue(), DocumentType.TRANSFER.getStringValue());

    private static final String DRAFT_MAKES_RESERVATION = "draftMakesReservation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity documentPositionParameters) {
        if (draftMakesReservationChanged(dataDefinition, documentPositionParameters) && !noDraftDocumentExists()) {

            documentPositionParameters.addError(dataDefinition.getField(DRAFT_MAKES_RESERVATION),
                    "materialFlowResources.materialFlowResourcesParameters.error.draftDocumentsExist");
            return false;
        }
        if (documentPositionParameters.getBooleanField(DRAFT_MAKES_RESERVATION)) {
            return true;
        }
        boolean isValid = noReservationExists();
        if (!isValid) {
            documentPositionParameters.addError(dataDefinition.getField(DRAFT_MAKES_RESERVATION),
                    "materialFlowResources.materialFlowResourcesParameters.error.reservationsExist");
        }
        return isValid;
    }

    private boolean draftMakesReservationChanged(final DataDefinition dataDefinition, final Entity parameters) {
        if (parameters.getId() == null) {
            return false;
        }
        Entity parametersFromDb = dataDefinition.get(parameters.getId());
        return parametersFromDb.getBooleanField(DRAFT_MAKES_RESERVATION) != parameters.getBooleanField(DRAFT_MAKES_RESERVATION);
    }

    public boolean noReservationExists() {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION).find();
        scb.add(SearchRestrictions.isNotNull(ReservationFields.POSITION));
        scb.setProjection(alias(rowCount(), "cnt"));
        scb.addOrder(asc("cnt"));

        Entity countProjection = scb.setMaxResults(1).uniqueResult();

        return ((Long) countProjection.getField("cnt")).compareTo(0L) == 0;
    }

    public boolean noDraftDocumentExists() {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT).find();
        scb.add(SearchRestrictions.eq(DocumentFields.STATE, DocumentState.DRAFT.getStringValue()));
        scb.add(SearchRestrictions.in(DocumentFields.TYPE, DOCUMENT_TYPES));
        scb.setProjection(alias(rowCount(), "cnt"));
        scb.addOrder(asc("cnt"));

        Entity countProjection = scb.setMaxResults(1).uniqueResult();

        return ((Long) countProjection.getField("cnt")).compareTo(0L) == 0;
    }
}
