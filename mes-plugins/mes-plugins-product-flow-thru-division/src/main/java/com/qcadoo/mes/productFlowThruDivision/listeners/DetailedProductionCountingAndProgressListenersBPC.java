package com.qcadoo.mes.productFlowThruDivision.listeners;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.materialFlowResources.exceptions.DocumentBuildException;
import com.qcadoo.mes.productFlowThruDivision.service.ProductionCountingDocumentService;
import com.qcadoo.mes.productFlowThruDivision.states.ProductionTrackingListenerServicePFTD;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DetailedProductionCountingAndProgressListenersBPC {

    private static final String L_ORDER = "order";

    private static final String L_ERROR_NOT_ENOUGH_RESOURCES = "materialFlow.error.position.quantity.notEnoughResources";

    @Autowired
    private ProductionCountingDocumentService productionCountingDocumentService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionTrackingListenerServicePFTD productionTrackingListenerServicePFTD;

    public void resourceIssue(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_ORDER);
        Entity order = formComponent.getEntity().getDataDefinition().get(formComponent.getEntity().getId());
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Set<Long> ids = grid.getSelectedEntitiesIds();
        List<Entity> pcqs = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find().add(SearchRestrictions.in("id", ids)).list().getEntities();

        try {
            productionCountingDocumentService.createInternalOutboundDocument(order, pcqs, false);
            if (order.isValid()) {
                productionCountingDocumentService.updateProductionCountingQuantity(pcqs);
                productionTrackingListenerServicePFTD.updateCostsForOrder(order);
                view.addMessage("productFlowThruDivision.productionCountingQuantity.success.createInternalOutboundDocument",
                        ComponentState.MessageType.SUCCESS);
            }
        } catch (DocumentBuildException documentBuildException) {
            boolean errorsDisplayed = true;
            for (ErrorMessage error : documentBuildException.getEntity().getGlobalErrors()) {
                if (error.getMessage().equalsIgnoreCase(L_ERROR_NOT_ENOUGH_RESOURCES)) {
                    order.addGlobalError(error.getMessage(), false, error.getVars());
                } else {
                    errorsDisplayed = false;
                    order.addGlobalError(error.getMessage(), error.getVars());
                }
            }

            if (!errorsDisplayed) {
                order.addGlobalError(
                        "productFlowThruDivision.productionCountingQuantity.productionCountingQuantityError.createInternalOutboundDocument");
            }
            formComponent.setEntity(order);
        }

    }
}
