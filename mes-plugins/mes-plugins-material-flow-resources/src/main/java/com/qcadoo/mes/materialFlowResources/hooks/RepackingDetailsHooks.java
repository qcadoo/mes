package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.UserFieldsB;
import com.qcadoo.mes.materialFlowResources.constants.RepackingFields;
import com.qcadoo.mes.materialFlowResources.states.constants.RepackingState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class RepackingDetailsHooks {

    @Autowired
    private UserService userService;

    public void onBeforeRender(final ViewDefinitionState view) {
        changeFieldsEnabledDependOnState(view);
        toggleButtons(view);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent timeField = (FieldComponent) view.getComponentByReference(RepackingFields.TIME);
        LookupComponent staffLookup = (LookupComponent) view.getComponentByReference(RepackingFields.STAFF);
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(RepackingFields.LOCATION);
        Entity repacking = form.getPersistedEntityWithIncludedFormValues();
        if (Objects.isNull(timeField.getFieldValue())) {
            timeField.setFieldValue(DateUtils.toDateTimeString(new Date()));
            Entity worker = userService.getCurrentUserEntity().getBelongsToField(UserFieldsB.STAFF);
            if (Objects.nonNull(worker)) {
                staffLookup.setFieldValue(worker.getId());
                staffLookup.requestComponentUpdateState();
            }
        }
        setStorageLocationLookupFilterValue(view, repacking);
        setPalletNumberLookupFilterValue(view, repacking);
        if (!repacking.getHasManyField(RepackingFields.POSITIONS).isEmpty()) {
            locationLookup.setEnabled(false);
            locationLookup.requestComponentUpdateState();
        }
    }

    private void toggleButtons(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(RepackingFields.STATE);
        RibbonGroup actionsRibbonGroup = window.getRibbon().getGroupByName("actions");
        RibbonGroup stateRibbonGroup = window.getRibbon().getGroupByName("state");
        RibbonActionItem deleteRibbonActionItem = actionsRibbonGroup.getItemByName("delete");
        RibbonActionItem saveRibbonActionItem = actionsRibbonGroup.getItemByName("save");
        RibbonActionItem saveBackRibbonActionItem = actionsRibbonGroup.getItemByName("saveBack");
        RibbonActionItem saveNewRibbonActionItem = actionsRibbonGroup.getItemByName("saveNew");
        RibbonActionItem acceptRibbonActionItem = stateRibbonGroup.getItemByName("accept");
        RibbonActionItem rejectRibbonActionItem = stateRibbonGroup.getItemByName("reject");

        String state = stateField.getFieldValue().toString();
        boolean enabled = !RepackingState.REJECTED.getStringValue().equals(state) && !RepackingState.ACCEPTED.getStringValue().equals(state);

        deleteRibbonActionItem.setEnabled(!RepackingState.ACCEPTED.getStringValue().equals(state) && !Objects.isNull(form.getEntityId()));
        deleteRibbonActionItem.requestUpdate(true);
        acceptRibbonActionItem.setEnabled(enabled && !Objects.isNull(form.getEntityId()));
        acceptRibbonActionItem.requestUpdate(true);
        rejectRibbonActionItem.setEnabled(enabled && !Objects.isNull(form.getEntityId()));
        rejectRibbonActionItem.requestUpdate(true);
        saveRibbonActionItem.setEnabled(enabled);
        saveRibbonActionItem.requestUpdate(true);
        saveBackRibbonActionItem.setEnabled(enabled);
        saveBackRibbonActionItem.requestUpdate(true);
        saveNewRibbonActionItem.setEnabled(enabled);
        saveNewRibbonActionItem.requestUpdate(true);
    }

    public void changeFieldsEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(RepackingFields.STATE);
        GridComponent positionsGrid = (GridComponent) view.getComponentByReference(RepackingFields.POSITIONS);

        String state = stateField.getFieldValue().toString();

        if (Objects.isNull(form.getEntityId())) {
            form.setFormEnabled(true);
            positionsGrid.setEnabled(false);
        } else {
            if (RepackingState.REJECTED.getStringValue().equals(state) || RepackingState.ACCEPTED.getStringValue().equals(state)) {
                form.setFormEnabled(false);
                positionsGrid.setEnabled(false);
            } else {
                form.setFormEnabled(true);
                positionsGrid.setEnabled(true);
            }
        }
    }

    private void setStorageLocationLookupFilterValue(final ViewDefinitionState view, final Entity repacking) {
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(RepackingFields.STORAGE_LOCATION);

        FilterValueHolder filter = storageLocationLookup.getFilterValue();

        Entity warehouse = repacking.getBelongsToField(RepackingFields.LOCATION);

        if (Objects.nonNull(warehouse)) {
            filter.put(RepackingFields.LOCATION, warehouse.getId());
        } else {
            filter.remove(RepackingFields.LOCATION);
        }

        storageLocationLookup.setFilterValue(filter);
    }

    private void setPalletNumberLookupFilterValue(final ViewDefinitionState view, final Entity repacking) {
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(RepackingFields.PALLET_NUMBER);

        FilterValueHolder filter = palletNumberLookup.getFilterValue();

        Entity warehouse = repacking.getBelongsToField(RepackingFields.LOCATION);

        if (Objects.nonNull(warehouse)) {
            filter.put(RepackingFields.LOCATION, warehouse.getId());
        } else {
            filter.remove(RepackingFields.LOCATION);
        }

        palletNumberLookup.setFilterValue(filter);
    }

}
