package com.qcadoo.mes.productFlowThruDivision.listeners;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.productFlowThruDivision.service.ProductionCountingDocumentService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
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

    public static final String L_ORDER = "order";

    @Autowired
    private ProductionCountingDocumentService productionCountingDocumentService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void resourceIssue(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_ORDER);
        Entity order = formComponent.getEntity().getDataDefinition().get(formComponent.getEntity().getId());
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Set<Long> ids = grid.getSelectedEntitiesIds();
        List<Entity> pcqs = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find().add(SearchRestrictions.in("id", ids)).list().getEntities();
        productionCountingDocumentService.createInternalOutboundDocument(order, pcqs, false);
        if (order.isValid()) {
            view.addMessage("utworzono dokument", ComponentState.MessageType.SUCCESS);
        } else {
            formComponent.setEntity(order);
        }
    }
}
