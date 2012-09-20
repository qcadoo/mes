package com.qcadoo.mes.operationalTasksForOrders.hooks;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTasksListOTFOHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        addDiscriminatorRestrictionToGrid(view);
    }

    public final void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view) {
        LookupComponent productIn = (LookupComponent) view.getComponentByReference(OperationalTasksFields.PRODUCT_IN);
        LookupComponent productOut = (LookupComponent) view.getComponentByReference(OperationalTasksFields.PRODUCT_OUT);
        Entity productInEntity = productIn.getEntity();
        Entity productOutEntity = productOut.getEntity();
        if (productInEntity == null && productOutEntity == null) {
            return;
        }
        List<Entity> operations = new ArrayList<Entity>();
        if (productInEntity != null) {
            operations = getOperation(getProductInCompList(productInEntity));
        }
        if (productOutEntity != null) {
            operations.addAll(getOperation(getProductOutCompList(productOutEntity)));
        }
        List<Entity> tiocs = getTiocList(operations);

        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        grid.setEntities(getTasks(tiocs));
        grid.performEvent(view, "refresh");
    }

    private List<Entity> getTasks(final List<Entity> tiocs) {
        List<Entity> tasks = new ArrayList<Entity>();
        for (Entity tioc : tiocs) {
            List<Entity> tasksForTioc = dataDefinitionService
                    .get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK).find()
                    .add(SearchRestrictions.belongsTo(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT, tioc))
                    .list().getEntities();
            for (Entity taskForTioc : tasksForTioc) {
                if (!tasks.contains(taskForTioc)) {
                    tasks.add(taskForTioc);
                }
            }
        }
        return tasks;
    }

    private List<Entity> getTiocList(final List<Entity> operations) {
        List<Entity> tiocs = new ArrayList<Entity>();
        for (Entity operation : operations) {
            List<Entity> techInstOperComps = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                            TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                    .find()
                    .add(SearchRestrictions.belongsTo(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT, operation))
                    .list().getEntities();
            for (Entity techInstOperComp : techInstOperComps) {
                if (!tiocs.contains(techInstOperComp)) {
                    tiocs.add(techInstOperComp);
                }
            }
        }
        return tiocs;
    }

    private List<Entity> getOperation(final List<Entity> products) {
        List<Entity> operations = new ArrayList<Entity>();
        for (Entity product : products) {
            Entity operComp = product.getBelongsToField("operationComponent");
            if (!operations.contains(operComp)) {
                operations.add(operComp);
            }
        }
        return operations;
    }

    private List<Entity> getProductInCompList(final Entity productIn) {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).find()
                .add(SearchRestrictions.belongsTo("product", productIn)).list().getEntities();
    }

    private List<Entity> getProductOutCompList(final Entity productOut) {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT).find()
                .add(SearchRestrictions.belongsTo("product", productOut)).list().getEntities();
    }

}
