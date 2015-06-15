package com.qcadoo.mes.warehouseMinimalState.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.warehouseMinimalState.constants.WarehouseMinimalStateConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service public class WarehouseMinimumStateListListener {

    private static final String L_FORM = "form";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    @Autowired private DataDefinitionService dataDefinitionService;

    @Autowired
    private ReportService reportService;

    public void redirectToAddManyMinimalState(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        Entity createMultiMinimalStateEntity = createMultiMinimalStateEntity();
        String url = "../page/warehouseMinimalState/warehouseMinimumStateAddMulti.html?context={\"form.id\":\""
                + createMultiMinimalStateEntity.getId() + "\"}";
        viewDefinitionState.openModal(url);
    }

    private Entity createMultiMinimalStateEntity() {
        Entity state = dataDefinitionService.get("warehouseMinimalState", "warehouseMinimumStateMulti").create();
        return state.getDataDefinition().save(state);
    }

    @Transactional public void createMultiMinimalStates(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity state = form.getPersistedEntityWithIncludedFormValues();

        if (state.getBelongsToField("location") == null) {
            LookupComponent location = (LookupComponent) view.getComponentByReference("location");
            location.addMessage(new ErrorMessage(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING));
            location.requestComponentUpdateState();
            return;
        }

        if (state.getManyToManyField("products") == null || state.getManyToManyField("products").isEmpty()) {
            view.addMessage(new ErrorMessage("warehouseMinimalState.warehouseMinimumStateAddMulti.error.productsEmpthy"));
            return;
        }
        state.getManyToManyField("products").forEach(p -> createMinimalStateEntity(state, p));
        componentState.addMessage("warehouseMinimalState.warehouseMinimumStateAddMulti.info.generated", ComponentState.MessageType.SUCCESS);

    }

    private void createMinimalStateEntity(Entity state, Entity product) {

        if (getLocationMinimumStateByProductAndLocation( dataDefinitionService.get("warehouseMinimalState", "warehouseMinimumState"), product, state.getBelongsToField("location"))
                == null) {

            Entity mstate = dataDefinitionService.get("warehouseMinimalState", "warehouseMinimumState").create();

            mstate.setField("product", product);
            mstate.setField("location", state.getBelongsToField("location"));
            mstate.setField("minimumState", state.getDecimalField("minimumState"));
            mstate.setField("optimalOrderQuantity", state.getDecimalField("optimalOrderQuantity"));
            mstate.getDataDefinition().save(mstate);
        }
    }

    private Entity getLocationMinimumStateByProductAndLocation(final DataDefinition locationMinimumStateDD, final Entity product,
            final Entity location) {
        return locationMinimumStateDD.find().add(SearchRestrictions.belongsTo("product", product))
                .add(SearchRestrictions.belongsTo("location", location)).setMaxResults(1).uniqueResult();
    }

    public void printDocument(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        viewDefinitionState.redirectTo("/" + WarehouseMinimalStateConstants.PLUGIN_IDENTIFIER + "/document." + args[0], true,
                false);
    }
}
