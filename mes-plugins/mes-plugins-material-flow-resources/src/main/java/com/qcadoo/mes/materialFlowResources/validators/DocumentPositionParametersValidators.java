package com.qcadoo.mes.materialFlowResources.validators;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class DocumentPositionParametersValidators {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity documentPositionParameters) {
        if (documentPositionParameters.getBooleanField("draftMakesReservation")) {
            return true;
        }
        boolean isValid = noReservationExists();
        if (!isValid) {
            documentPositionParameters.addError(dataDefinition.getField("draftMakesReservation"),
                    "materialFlowResources.materialFlowResourcesParameters.error.reservationsExist");
        }
        return isValid;
    }

    public boolean noReservationExists() {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION).find();
        scb.setProjection(alias(rowCount(), "cnt"));
        scb.addOrder(asc("cnt"));

        Entity countProjection = scb.setMaxResults(1).uniqueResult();

        return ((Long) countProjection.getField("cnt")).compareTo(0L) == 0;
    }
}
