package com.qcadoo.mes.productionScheduling;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class NormOrderService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ShiftsService shiftsService;

    public boolean checkMachineRequiredFields(final DataDefinition dataDefinition, final Entity entity) {
        if ((Boolean) entity.getField("useDefaultValue")) {
            return true;
        }

        checkRequiredField(entity, "tpz");
        checkRequiredField(entity, "tj");
        checkRequiredField(entity, "parallel");

        return entity.isValid();
    }

    private void checkRequiredField(final Entity entity, final String field) {
        if (entity.getField(field) == null) {
            entity.addError(entity.getDataDefinition().getField(field), "qcadooView.validate.field.error.missing");
        }
    }

    public boolean checkOperationRequiredFields(final DataDefinition dataDefinition, final Entity entity) {
        if ((Boolean) entity.getField("useDefaultValue")) {
            return true;
        }

        checkRequiredField(entity, "timeNextOperation");

        if (!(Boolean) entity.getField("useMachineNorm")) {
            checkRequiredField(entity, "tpz");
            checkRequiredField(entity, "tj");
        }

        checkRequiredField(entity, "countRealized");

        if ("02specified".equals(entity.getStringField("countRealized"))) {
            checkRequiredField(entity, "countMachine");
        }

        return entity.isValid();
    }

    public boolean checkMachineInOrderOperationComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity machine = entity.getBelongsToField("machine");
        Entity operationComponent = entity.getBelongsToField("orderOperationComponent");

        if (operationComponent == null || machine == null) {
            return true;
        }

        SearchResult searchResult = dataDefinition.find().add(SearchRestrictions.belongsTo("machine", machine))
                .add(SearchRestrictions.belongsTo("orderOperationComponent", operationComponent)).list();

        if (searchResult.getTotalNumberOfEntities() == 1 && searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            return true;
        } else if (searchResult.getTotalNumberOfEntities() > 0) {
            entity.addError(dataDefinition.getField("machine"),
                    "productionScheduling.validate.global.error.machineInOperationDuplicated");
            return false;
        } else {
            return true;
        }
    }

    @Transactional
    public void createTechnologyInstanceForOrder(final DataDefinition dataDefinition, final Entity entity) {
        DataDefinition orderOperationComponentDD = dataDefinitionService.get("productionScheduling", "orderOperationComponent");
        DataDefinition machineInOrderOperationComponentDD = dataDefinitionService.get("productionScheduling",
                "machineInOrderOperationComponent");

        EntityTree orderOperationComponents = entity.getTreeField("orderOperationComponents");

        Entity technology = entity.getBelongsToField("technology");

        if (technology == null) {
            if (orderOperationComponents != null && orderOperationComponents.size() > 0) {
                orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
            }
            return;
        }

        if (orderOperationComponents != null && orderOperationComponents.size() > 0
                && orderOperationComponents.getRoot().getBelongsToField("technology").getId().equals(technology.getId())) {
            return;
        }

        if (orderOperationComponents != null && orderOperationComponents.size() > 0) {
            orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
        }

        EntityTree operationComponents = technology.getTreeField("operationComponents");

        entity.setField("orderOperationComponents", Collections.singletonList(createOrderOperationComponent(
                operationComponents.getRoot(), entity, technology, null, orderOperationComponentDD,
                machineInOrderOperationComponentDD)));
    }

    private Entity createOrderOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final Entity parent, final DataDefinition orderOperationComponentDD,
            final DataDefinition machineInOrderOperationComponentDD) {
        Entity orderOperationComponent = orderOperationComponentDD.create();

        orderOperationComponent.setField("order", order);
        orderOperationComponent.setField("technology", technology);
        orderOperationComponent.setField("parent", parent);

        if ("operation".equals(operationComponent.getField("entityType"))) {
            createOrCopyOrderOperationComponent(operationComponent, order, technology, orderOperationComponentDD,
                    machineInOrderOperationComponentDD, orderOperationComponent);
        } else {
            Entity referenceTechnology = operationComponent.getBelongsToField("referenceTechnology");
            createOrCopyOrderOperationComponent(referenceTechnology.getTreeField("operationComponents").getRoot(), order,
                    technology, orderOperationComponentDD, machineInOrderOperationComponentDD, orderOperationComponent);
        }

        return orderOperationComponent;
    }

    private void createOrCopyOrderOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final DataDefinition orderOperationComponentDD,
            final DataDefinition machineInOrderOperationComponentDD, final Entity orderOperationComponent) {
        orderOperationComponent.setField("operation", operationComponent.getBelongsToField("operation"));
        orderOperationComponent.setField("technologyOperationComponent", operationComponent);
        orderOperationComponent.setField("priority", operationComponent.getField("priority"));
        orderOperationComponent.setField("entityType", "operation");
        orderOperationComponent.setField("useDefaultValue", true);
        orderOperationComponent.setField("tpz", operationComponent.getField("tpz"));
        orderOperationComponent.setField("tj", operationComponent.getField("tj"));
        orderOperationComponent.setField("useMachineNorm", operationComponent.getField("useMachineNorm"));
        orderOperationComponent.setField("countRealized", operationComponent.getField("countRealizedNorm"));
        orderOperationComponent.setField("countMachine", operationComponent.getField("countMachineNorm"));
        orderOperationComponent.setField("timeNextOperation", operationComponent.getField("timeNextOperationNorm"));

        EntityList machineInOperationComponents = operationComponent.getHasManyField("machineInOperationComponent");

        List<Entity> newMachineInOrderOperationComponents = new ArrayList<Entity>();

        for (Entity machineInOperationComponent : machineInOperationComponents) {
            Entity machineInOrderOperationComponent = machineInOrderOperationComponentDD.create();
            machineInOrderOperationComponent.setField("orderOperationComponent", orderOperationComponent);
            machineInOrderOperationComponent.setField("machine", machineInOperationComponent.getBelongsToField("machine"));
            machineInOrderOperationComponent.setField("useDefaultValue", true);
            machineInOrderOperationComponent.setField("tpz", machineInOperationComponent.getField("tpz"));
            machineInOrderOperationComponent.setField("tj", machineInOperationComponent.getField("tj"));
            machineInOrderOperationComponent.setField("parallel", machineInOperationComponent.getField("parallel"));
            machineInOrderOperationComponent.setField("isActive", machineInOperationComponent.getField("activeMachine"));
            newMachineInOrderOperationComponents.add(machineInOrderOperationComponent);
        }

        orderOperationComponent.setField("machineInOrderOperationComponents", newMachineInOrderOperationComponents);

        List<Entity> newOrderOperationComponents = new ArrayList<Entity>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newOrderOperationComponents.add(createOrderOperationComponent(child, order, technology, orderOperationComponent,
                    orderOperationComponentDD, machineInOrderOperationComponentDD));
        }

        orderOperationComponent.setField("children", newOrderOperationComponents);
    }

    public void showOrderParameters(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/productionScheduling/orderOperationComponentList.html?context={\"form.id\":\"" + orderId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void showOperationsGantt(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        scheduleOrder(orderId);

        if (orderId != null) {
            String url = "../page/productionScheduling/ganttOrderOperationsCalendar.html?context={\"gantt.orderId\":\"" + orderId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    private void scheduleOrder(final Long orderId) {
        Entity order = dataDefinitionService.get("orders", "order").get(orderId);

        if (order == null) {
            return;
        }

        DataDefinition dataDefinition = dataDefinitionService.get("productionScheduling", "orderOperationComponent");

        List<Entity> operations = dataDefinition.find().add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        Date orderStartDate = null;

        if (order.getField("effectiveDateFrom") != null) {
            orderStartDate = (Date) order.getField("effectiveDateFrom");
        } else if (order.getField("dateFrom") != null) {
            orderStartDate = (Date) order.getField("dateFrom");
        } else {
            return;
        }

        for (Entity operation : operations) {
            Integer offset = (Integer) operation.getField("operationOffSet");
            Integer duration = (Integer) operation.getField("effectiveOperationRealizationTime");

            operation.setField("effectiveDateFrom", null);
            operation.setField("effectiveDateTo", null);

            if (offset == null || duration == null || duration.equals(0)) {
                continue;
            }

            if (offset == 0) {
                offset = 1;
            }

            Date dateFrom = shiftsService.findDateToForOrder(orderStartDate, offset);

            if (dateFrom == null) {
                continue;
            }

            Date dateTo = shiftsService.findDateToForOrder(orderStartDate, offset + duration);

            if (dateTo == null) {
                continue;
            }

            operation.setField("effectiveDateFrom", dateFrom);
            operation.setField("effectiveDateTo", dateTo);
        }

        for (Entity operation : operations) {
            dataDefinition.save(operation);
        }
    }

    public void changeUseDefaultValue(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        // ignore
    }

    public void changeCountRealized(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        // ignore
    }

    public void changeUseMachineNorm(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        // ignore
    }

    public void changeUseDefaultValueMachine(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        // ignore
    }

    public void selectMachine(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        setParentValuesMachine(viewDefinitionState, (FormComponent) viewDefinitionState.getComponentByReference("form"),
                (Long) ((FieldComponent) state).getFieldValue());
    }

    public void disableComponents(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent useDefaultValue = (FieldComponent) viewDefinitionState.getComponentByReference("useDefaultValue");
        FieldComponent tpz = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tj = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent useMachineNorm = (FieldComponent) viewDefinitionState.getComponentByReference("useMachineNorm");
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference("timeNextOperation");
        ComponentState parentTpz = viewDefinitionState.getComponentByReference("parentTpz");
        ComponentState parentTj = viewDefinitionState.getComponentByReference("parentTj");
        ComponentState parentUseMachineNorm = viewDefinitionState.getComponentByReference("parentUseMachineNorm");
        ComponentState parentCountRealized = viewDefinitionState.getComponentByReference("parentCountRealized");
        ComponentState parentCountMachine = viewDefinitionState.getComponentByReference("parentCountMachine");
        ComponentState parentTimeNextOperation = viewDefinitionState.getComponentByReference("parentTimeNextOperation");

        if (useDefaultValue.getFieldValue() == null || !"1".equals(useDefaultValue.getFieldValue())) {
            useDefaultValue.setFieldValue("0");
            useMachineNorm.setEnabled(true);

            if ("1".equals(useMachineNorm.getFieldValue())) {
                tpz.setEnabled(false);
                tpz.setRequired(false);
                tj.setEnabled(false);
                tj.setRequired(false);
            } else {
                tpz.setEnabled(true);
                tpz.setRequired(true);
                tj.setEnabled(true);
                tj.setRequired(true);
            }

            countRealized.setEnabled(true);
            countRealized.setRequired(true);

            if ("02specified".equals(countRealized.getFieldValue())) {
                countMachine.setEnabled(true);
                countMachine.setRequired(true);
                if (countMachine.getFieldValue() == null || !StringUtils.hasText(String.valueOf(countMachine.getFieldValue()))) {
                    countMachine.setFieldValue("1");
                }
            } else {
                countMachine.setEnabled(false);
                countMachine.setRequired(false);
            }

            timeNextOperation.setEnabled(true);
            timeNextOperation.setRequired(true);
        } else {
            tpz.setEnabled(false);
            tpz.setRequired(false);
            tj.setEnabled(false);
            tj.setRequired(false);
            useMachineNorm.setEnabled(false);
            useMachineNorm.setRequired(false);
            countRealized.setEnabled(false);
            countRealized.setRequired(false);
            countMachine.setEnabled(false);
            countMachine.setRequired(false);
            timeNextOperation.setEnabled(false);
            timeNextOperation.setRequired(false);
        }

        Entity technologyOperationComponent = dataDefinitionService.get("productionScheduling", "orderOperationComponent")
                .get(form.getEntityId()).getBelongsToField("technologyOperationComponent");

        if (technologyOperationComponent == null) {
            parentUseMachineNorm.setFieldValue(null);
            parentTpz.setFieldValue(null);
            parentTj.setFieldValue(null);
            parentCountRealized.setFieldValue(null);
            parentCountMachine.setFieldValue(null);
            parentTimeNextOperation.setFieldValue(null);
            return;
        }

        parentUseMachineNorm.setFieldValue(technologyOperationComponent.getField("useMachineNorm"));

        if ((Boolean) technologyOperationComponent.getField("useDefaultValue")) {
            Entity operation = technologyOperationComponent.getBelongsToField("operation");
            parentTpz.setFieldValue(operation.getField("tpz"));
            parentTj.setFieldValue(operation.getField("tj"));
            parentCountRealized.setFieldValue(operation.getField("countRealizedOperation"));
            parentCountMachine.setFieldValue(operation.getField("countMachineOperation"));
            parentTimeNextOperation.setFieldValue(operation.getField("timeNextOperation"));
        } else {
            parentTpz.setFieldValue(technologyOperationComponent.getField("tpz"));
            parentTj.setFieldValue(technologyOperationComponent.getField("tj"));
            parentCountRealized.setFieldValue(technologyOperationComponent.getField("countRealizedNorm"));
            parentCountMachine.setFieldValue(technologyOperationComponent.getField("countMachineNorm"));
            parentTimeNextOperation.setFieldValue(technologyOperationComponent.getField("timeNextOperationNorm"));
        }

        convertIntToStringTime(parentTpz);
        convertIntToStringTime(parentTj);
        convertIntToStringTime(parentTimeNextOperation);
        convertBooleanToString(parentUseMachineNorm);
        convertDecimalToString(parentCountMachine);
        convertEnumToString(parentCountRealized);
    }

    public void disableComponentsMachine(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent machine = (FieldComponent) viewDefinitionState.getComponentByReference("machine");
        FieldComponent useDefaultValue = (FieldComponent) viewDefinitionState.getComponentByReference("useDefaultValue");
        FieldComponent tpz = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tj = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent parallel = (FieldComponent) viewDefinitionState.getComponentByReference("parallel");
        FieldComponent isActive = (FieldComponent) viewDefinitionState.getComponentByReference("isActive");

        if (useDefaultValue.getFieldValue() == null || !"1".equals(useDefaultValue.getFieldValue())) {
            useDefaultValue.setFieldValue("0");
            tpz.setEnabled(true);
            tpz.setRequired(true);
            tj.setEnabled(true);
            tj.setRequired(true);
            parallel.setEnabled(true);
            parallel.setRequired(true);
            isActive.setEnabled(true);
            isActive.setRequired(true);
        } else {
            tpz.setEnabled(false);
            tpz.setRequired(false);
            tj.setEnabled(false);
            tj.setRequired(false);
            parallel.setEnabled(false);
            parallel.setRequired(false);
            isActive.setEnabled(false);
            isActive.setRequired(false);
        }

        setParentValuesMachine(viewDefinitionState, form, (Long) machine.getFieldValue());
    }

    private void setParentValuesMachine(final ViewDefinitionState viewDefinitionState, final FormComponent form,
            final Long machineId) {
        if (form.getEntityId() == null) {
            return;
        }

        ComponentState parentTpz = viewDefinitionState.getComponentByReference("parentTpz");
        ComponentState parentTj = viewDefinitionState.getComponentByReference("parentTj");
        ComponentState parentParallel = viewDefinitionState.getComponentByReference("parentParallel");
        ComponentState parentIsActive = viewDefinitionState.getComponentByReference("parentIsActive");

        Entity machine = null;

        if (machineId != null) {
            machine = dataDefinitionService.get("basic", "machine").get(machineId);
        }

        if (machine == null) {
            parentTpz.setFieldValue(null);
            parentTj.setFieldValue(null);
            parentParallel.setFieldValue(null);
            parentIsActive.setFieldValue(null);
        }

        Entity machineInOrderOperationComponent = dataDefinitionService.get("productionScheduling",
                "machineInOrderOperationComponent").get(form.getEntityId());
        Entity technologyOperationComponent = machineInOrderOperationComponent.getBelongsToField("orderOperationComponent")
                .getBelongsToField("technologyOperationComponent");
        Entity operation = machineInOrderOperationComponent.getBelongsToField("orderOperationComponent").getBelongsToField(
                "operation");
        Entity machineInOperationComponent = dataDefinitionService.get("productionScheduling", "machineInOperationComponent")
                .find().add(SearchRestrictions.belongsTo("operationComponent", technologyOperationComponent))
                .add(SearchRestrictions.belongsTo("machine", machine)).uniqueResult();
        Entity machineInOperation = dataDefinitionService.get("productionScheduling", "machineInOperation").find()
                .add(SearchRestrictions.belongsTo("operation", operation)).add(SearchRestrictions.belongsTo("machine", machine))
                .uniqueResult();

        if (machineInOperationComponent != null && !(Boolean) machineInOperationComponent.getField("useDefaultValue")) {
            parentTpz.setFieldValue(machineInOperationComponent.getField("tpz"));
            parentTj.setFieldValue(machineInOperationComponent.getField("tj"));
            parentParallel.setFieldValue(machineInOperationComponent.getField("parallel"));
            parentIsActive.setFieldValue(machineInOperationComponent.getField("activeMachine"));
        } else if (machineInOperation != null && !(Boolean) machineInOperation.getField("useDefaultValue")) {
            parentTpz.setFieldValue(machineInOperation.getField("tpz"));
            parentTj.setFieldValue(machineInOperation.getField("tj"));
            parentParallel.setFieldValue(machineInOperation.getField("parallel"));
            parentIsActive.setFieldValue(machineInOperation.getField("activeMachine"));
        } else if (machine != null) {
            parentTpz.setFieldValue(machine.getField("tpz"));
            parentTj.setFieldValue(machine.getField("tj"));
            parentParallel.setFieldValue(machine.getField("parallel"));
            parentIsActive.setFieldValue(machine.getField("activeMachine"));
        }

        convertIntToStringTime(parentTpz);
        convertIntToStringTime(parentTj);
        convertBooleanToString(parentIsActive);
        convertDecimalToString(parentParallel);
    }

    private void convertIntToStringTime(final ComponentState component) {
        if (component.getFieldValue() == null) {
            return;
        }

        int time = Integer.valueOf((String) component.getFieldValue());
        int hours = time / 3600;
        int minutes = time / 60 % 60;
        int seconds = time % 60;
        component.setFieldValue(String.format("(%02d:%02d:%02d)", hours, minutes, seconds));
    }

    private void convertBooleanToString(final ComponentState component) {
        if (component.getFieldValue() == null) {
            return;
        }

        if ("0".equals(component.getFieldValue())) {
            component.setFieldValue("(" + translationService.translate("qcadooView.false", component.getLocale()) + ")");
        } else {
            component.setFieldValue("(" + translationService.translate("qcadooView.true", component.getLocale()) + ")");
        }
    }

    private void convertDecimalToString(final ComponentState component) {
        if (component.getFieldValue() == null) {
            return;
        }

        NumberFormat format = NumberFormat.getNumberInstance(component.getLocale());
        format.setMinimumFractionDigits(3);
        format.setMaximumFractionDigits(3);

        component.setFieldValue("(" + format.format(new BigDecimal((String) component.getFieldValue())) + ")");
    }

    private void convertEnumToString(final ComponentState component) {
        if (component.getFieldValue() == null) {
            return;
        }

        String translated = null;

        if ("02specified".equals(component.getFieldValue())) {
            translated = translationService.translate(
                    "productionScheduling.orderOperationComponent.countRealized.value.02specified", component.getLocale());
        } else {
            translated = translationService.translate("productionScheduling.orderOperationComponent.countRealized.value.01all",
                    component.getLocale());
        }

        component.setFieldValue("(" + translated + ")");
    }

}
