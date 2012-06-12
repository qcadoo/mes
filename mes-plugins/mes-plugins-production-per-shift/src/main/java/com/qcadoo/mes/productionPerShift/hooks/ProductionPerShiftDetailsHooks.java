package com.qcadoo.mes.productionPerShift.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
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
import com.qcadoo.mes.orders.constants.OrderState;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
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
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TimeConverterService timeConverterService;

    public void addRootForOperation(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        Entity order = getOrderFromLookup(view);
        EntityTree techInstOperComps = order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
        if (techInstOperComps.isEmpty()) {
            return;
        }
        Entity root = techInstOperComps.getRoot();
        FieldComponent operation = (FieldComponent) view.getComponentByReference(PRODUCTION_PER_SHIFT_OPERATION);
        if (operation.getFieldValue() == null) {
            operation.setFieldValue(root.getId());
        }
        fillProgressForDays(view);
    }

    public void disablePlannedProgressTypeForPendingOrder(final ViewDefinitionState view) {
        Entity order = getOrderFromLookup(view);
        FieldComponent plannedProgressType = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        if (plannedProgressType.getFieldValue().equals("")
                || plannedProgressType.getFieldValue().equals(PlannedProgressType.PLANNED.getStringValue())) {
            plannedProgressType.setFieldValue(PlannedProgressType.PLANNED.getStringValue());
        } else {
            plannedProgressType.setFieldValue(PlannedProgressType.CORRECTED.getStringValue());
        }
        if (order.getStringField(OrderFields.STATE).equals(OrderState.PENDING.getStringValue())) {
            plannedProgressType.setEnabled(false);
        } else {
            plannedProgressType.setEnabled(true);
        }
    }

    public void fillProducedField(final ViewDefinitionState viewState) {
        Entity tioc = getTiocFromOperationLookup(viewState);
        String producedProduct = null;

        if (tioc != null) {
            Entity toc = tioc.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT);
            Entity prodComp = technologyService.getMainOutputProductComponent(toc);
            Entity prod = prodComp.getBelongsToField("product");
            producedProduct = prod.getStringField("name");
        }

        ComponentState producesInput = viewState.getComponentByReference("produces");
        producesInput.setFieldValue(producedProduct);
    }

    public Entity getTiocFromOperationLookup(final ViewDefinitionState viewState) {
        ComponentState operationLookup = viewState.getComponentByReference("productionPerShiftOperation");
        Long id = (Long) operationLookup.getFieldValue();
        Entity tioc = null;
        if (id != null) {
            tioc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(id);
        }
        return tioc;
    }

    public void setOrderStartDate(final ViewDefinitionState view) {
        Entity order = getOrderFromLookup(view);
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
        FieldComponent plannedProgressType = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        FieldComponent plannedProgressCorrectionType = (FieldComponent) view
                .getComponentByReference(PLANNED_PROGRESS_CORRECTION_TYPE);
        FieldComponent plannedProgressCorrectionComment = (FieldComponent) view
                .getComponentByReference(PLANNED_PROGRESS_CORRECTION_COMMENT);
        if (plannedProgressType.getFieldValue().equals(PlannedProgressType.PLANNED.getStringValue())) {
            plannedProgressCorrectionType.setEnabled(false);
            plannedProgressCorrectionComment.setEnabled(false);
        } else {
            plannedProgressCorrectionType.setEnabled(true);
            plannedProgressCorrectionComment.setEnabled(true);
        }
    }

    public Entity getOrderFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference("order");
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) lookup.getFieldValue());
    }

    public void fillProgressForDays(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference("progressForDaysADL");
        Entity tioc = getTiocFromOperationLookup(viewState);
        Entity order = getOrderFromLookup(viewState);

        if (tioc == null) {
            progressForDaysADL.setFieldValue(null);
        } else {
            String plannedProgressType = ((FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE))
                    .getFieldValue().toString();
            List<Entity> progressForDays = tioc
                    .getHasManyField(PROGRESS_FOR_DAYS)
                    .find()
                    .add(SearchRestrictions.eq(CORRECTED,
                            plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()))).list().getEntities();
            progressForDaysADL.setFieldValue(progressForDays);
        }
        if (!shouldHasCorrections(viewState) && !order.getStringField(STATE).equals(OrderState.PENDING.getStringValue())) {
            disabledComponent(progressForDaysADL.getFormComponents(), false);
        } else {
            disabledComponent(progressForDaysADL.getFormComponents(), true);
        }
        progressForDaysADL.requestComponentUpdateState();
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
        if (plannedProgressType.getFieldValue().equals(PlannedProgressType.PLANNED.getStringValue())) {
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
