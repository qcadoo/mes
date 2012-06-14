package com.qcadoo.mes.productionPerShift.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.PENDING;
import static com.qcadoo.mes.productionPerShift.constants.PlannedProgressType.PLANNED;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPE;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_TYPE;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFields.PROGRESS_FOR_DAYS;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class ProductionPerShiftDetailsHooks {

    private static final String PRODUCTION_PER_SHIFT_OPERATION = "productionPerShiftOperation";

    @Autowired
    private PPSHelper helper;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TimeConverterService timeConverterService;

    public void addRootForOperation(final ViewDefinitionState view) {
        FieldComponent setRootField = (FieldComponent) view.getComponentByReference("setRoot");
        if (setRootField.getFieldValue() != null && setRootField.getFieldValue().equals("1")) {
            return;
        }
        Entity order = helper.getOrderFromLookup(view);
        EntityTree techInstOperComps = order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
        if (techInstOperComps.isEmpty()) {
            return;
        }
        Entity root = techInstOperComps.getRoot();
        FieldComponent operation = (FieldComponent) view.getComponentByReference(PRODUCTION_PER_SHIFT_OPERATION);

        if (operation.getFieldValue() == null) {
            operation.setFieldValue(root.getId());
            setRootField.setFieldValue(true);
        }
        operation.requestComponentUpdateState();
        setRootField.requestComponentUpdateState();
    }

    public void disablePlannedProgressTypeForPendingOrder(final ViewDefinitionState view) {
        Entity order = helper.getOrderFromLookup(view);
        FieldComponent plannedProgressType = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        if (plannedProgressType.getFieldValue().equals("") || isPlanned(plannedProgressType.getFieldValue())) {
            plannedProgressType.setFieldValue(PlannedProgressType.PLANNED.getStringValue());
        } else {
            plannedProgressType.setFieldValue(PlannedProgressType.CORRECTED.getStringValue());
        }
        if (order.getStringField(OrderFields.STATE).equals(PENDING.getStringValue())) {
            plannedProgressType.setEnabled(false);
        } else {
            plannedProgressType.setEnabled(true);
        }
    }

    public void fillProducedField(final ViewDefinitionState viewState) {
        ComponentState producesInput = viewState.getComponentByReference("produces");
        if (!producesInput.getFieldValue().equals("")) {
            return;
        }
        Entity tioc = helper.getTiocFromOperationLookup(viewState);
        if (tioc == null) {
            return;
        }
        String producedProduct = null;
        Entity toc = tioc.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT);
        Entity prodComp = technologyService.getMainOutputProductComponent(toc);
        Entity prod = prodComp.getBelongsToField("product");
        producedProduct = prod.getStringField("name");
        producesInput.setFieldValue(producedProduct);
        fillUnitFields(viewState, prod);

    }

    private void fillUnitFields(final ViewDefinitionState viewState, final Entity product) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference("progressForDaysADL");
        for (FormComponent form : progressForDaysADL.getFormComponents()) {
            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) form
                    .findFieldComponentByName("dailyProgressADL");
            for (FormComponent formComponent : dailyProgressADL.getFormComponents()) {
                FieldComponent unit = formComponent.findFieldComponentByName("unit");
                unit.setFieldValue(product.getStringField("unit"));
            }
        }
    }

    public void setOrderStartDate(final ViewDefinitionState view) {
        Entity order = helper.getOrderFromLookup(view);
        FieldComponent orderPlannedStartDate = (FieldComponent) view.getComponentByReference("orderPlannedStartDate");
        FieldComponent orderCorrectedStartDate = (FieldComponent) view.getComponentByReference("orderCorrectedStartDate");
        if (order.getField(OrderFields.DATE_FROM) != null) {
            orderPlannedStartDate
                    .setFieldValue(timeConverterService.setDateToField((Date) order.getField(OrderFields.DATE_FROM)));
        }
        if (order.getField(OrderFields.CORRECTED_DATE_FROM) != null) {
            orderCorrectedStartDate.setFieldValue(timeConverterService.setDateToField((Date) order
                    .getField(OrderFields.CORRECTED_DATE_FROM)));
        }
        orderPlannedStartDate.requestComponentUpdateState();
        orderCorrectedStartDate.requestComponentUpdateState();
    }

    public void disableReasonOfCorrection(final ViewDefinitionState view) {
        FieldComponent progressType = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        FieldComponent plannedProgressCorrectionType = (FieldComponent) view
                .getComponentByReference(PLANNED_PROGRESS_CORRECTION_TYPE);
        FieldComponent plannedProgressCorrectionComment = (FieldComponent) view
                .getComponentByReference(PLANNED_PROGRESS_CORRECTION_COMMENT);
        if (isPlanned(progressType.getFieldValue())) {
            plannedProgressCorrectionType.setEnabled(false);
            plannedProgressCorrectionComment.setEnabled(false);
        } else {
            plannedProgressCorrectionType.setEnabled(true);
            plannedProgressCorrectionComment.setEnabled(true);
        }
    }

    public void fillProgressForDays(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference("progressForDaysADL");
        Entity tioc = helper.getTiocFromOperationLookup(viewState);
        Entity order = helper.getOrderFromLookup(viewState);

        if (tioc == null) {
            progressForDaysADL.setFieldValue(null);
        } else {
            FieldComponent plannedProgressType = ((FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE));
            List<Entity> progressForDays = tioc.getHasManyField(PROGRESS_FOR_DAYS).find()
                    .add(SearchRestrictions.eq(CORRECTED, !isPlanned(plannedProgressType.getFieldValue()))).list().getEntities();
            progressForDaysADL.setFieldValue(progressForDays);
        }
        if (shouldHasCorrections(viewState) || order.getStringField(STATE).equals(OrderState.PENDING.getStringValue())) {
            disabledComponent(progressForDaysADL.getFormComponents(), true);
        } else {
            disabledComponent(progressForDaysADL.getFormComponents(), false);
        }
        progressForDaysADL.requestComponentUpdateState();
    }

    public void refreshProgressForDaysADL(final ViewDefinitionState viewState) {
        if (!progressTypeWasChange(viewState) || !tiocWasChanged(viewState)) {
            return;
        }
        fillProgressForDays(viewState);
    }

    private boolean tiocWasChanged(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference("progressForDaysADL");
        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();
        if (progressForDays.isEmpty()) {
            return true;
        }
        Entity tioc = helper.getTiocFromOperationLookup(viewState);
        Entity tiocFromPfdays = progressForDays.get(0).getDataDefinition().get(progressForDays.get(0).getId())
                .getBelongsToField(ProgressForDayFields.TECH_INST_OPER_COMP);
        if (!tioc.getId().equals(tiocFromPfdays.getId())) {
            return true;
        }
        return false;
    }

    private boolean progressTypeWasChange(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference("progressForDaysADL");
        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();
        if (progressForDays.isEmpty()) {
            return true;
        }
        FieldComponent progressType = (FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE);
        boolean corrected = progressForDays.get(0).getBooleanField(CORRECTED);
        if ((isPlanned(progressType.getFieldValue()) && !corrected)) {
            return true;
        }
        return false;
    }

    private boolean isPlanned(final Object progressType) {
        return PLANNED.getStringValue().equals(progressType);
    }

    private void disabledComponent(final List<FormComponent> components, final boolean shouldDisabled) {
        for (FormComponent form : components) {
            form.setFormEnabled(shouldDisabled);
            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) form
                    .findFieldComponentByName("dailyProgressADL");
            dailyProgressADL.setEnabled(shouldDisabled);
        }
    }

    public boolean shouldHasCorrections(final ViewDefinitionState viewState) {
        return ((FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE)).getFieldValue().equals(
                PlannedProgressType.CORRECTED.getStringValue());
    }

    public void changedButtonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup progressSelectedOperation = (RibbonGroup) window.getRibbon().getGroupByName("progress");
        RibbonActionItem clearButton = (RibbonActionItem) progressSelectedOperation.getItemByName("clear");
        RibbonActionItem copyButton = (RibbonActionItem) progressSelectedOperation.getItemByName("copyFromPlanned");
        FieldComponent plannedProgressType = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        if (isPlanned(plannedProgressType.getFieldValue())) {
            clearButton.setEnabled(false);
            copyButton.setEnabled(false);
        } else {
            clearButton.setEnabled(true);
            copyButton.setEnabled(true);
        }
        clearButton.requestUpdate(true);
        copyButton.requestUpdate(true);
    }

}
