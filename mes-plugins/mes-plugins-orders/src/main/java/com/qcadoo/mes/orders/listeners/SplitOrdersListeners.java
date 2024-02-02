package com.qcadoo.mes.orders.listeners;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.util.AdditionalUnitService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SplitOrdersListeners {

    private static final String L_PARENTS = "parents";

    private static final String L_CHILDES = "childes";

    private static final String L_GENERATED = "generated";

    private static final DecimalFormat threeCharacterNumber = new DecimalFormat("000");
    public static final String ORDERS_GROUP = "ordersGroup";


    @Autowired
    private NumberService numberService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private AdditionalUnitService additionalUnitService;

    public void divideOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent splitForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);
        state.performEvent(view, "save", args);

        if (state.isHasError()) {
            return;
        }

        try {

            Entity helper = splitForm.getEntity().getDataDefinition().get(splitForm.getEntityId());
            Integer parts = helper.getIntegerField(SplitOrderHelperConstants.PARTS);
            List<Entity> parents = helper.getHasManyField(SplitOrderHelperConstants.PARENTS);

            for (Entity parent : parents) {
                BigDecimal quantity = parent.getDecimalField(OrderFields.PLANNED_QUANTITY);

                BigDecimal newPlannedQuantity = quantity.divide(new BigDecimal(parts), MathContext.DECIMAL64).setScale(0,
                        BigDecimal.ROUND_DOWN);

                if (BigDecimal.ZERO.compareTo(newPlannedQuantity) == 0) {
                    throw new IllegalStateException("Error");
                }

                BigDecimal rest = quantity.subtract(newPlannedQuantity.multiply(new BigDecimal(parts), MathContext.DECIMAL64));

                Entity order = parent.getBelongsToField(SplitOrderParentConstants.ORDER);
                Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);

                String numberPrefix = parent.getStringField(SplitOrderParentConstants.NUMBER) + "-";
                String baseNumber = numberGeneratorService.generateNumberWithPrefix(OrdersConstants.PLUGIN_IDENTIFIER,
                        OrdersConstants.MODEL_ORDER, 3, numberPrefix);
                String suffix = baseNumber.replace(numberPrefix, "");
                Integer counter = Integer.parseInt(suffix.replaceFirst("^0+(?!$)", ""));

                for (Integer i = 0; i < parts; i++) {

                    Entity entity = null;
                    if (i == parts - 1) {
                        newPlannedQuantity = newPlannedQuantity.add(rest);
                        entity = order;
                    } else {
                        entity = order.getDataDefinition().copy(order.getId()).get(0);

                    }
                    String number = order.getStringField(SplitOrderParentConstants.NUMBER) + "-" + threeCharacterNumber.format(counter);
                    entity.setField(OrderFields.NUMBER, number);
                    entity.setField(OrderFields.PLANNED_QUANTITY, newPlannedQuantity);
                    entity.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT,
                            numberService.setScaleWithDefaultMathContext(additionalUnitService.getQuantityAfterConversion(order,
                                    additionalUnitService.getAdditionalUnit(product),
                                    numberService.setScaleWithDefaultMathContext(newPlannedQuantity),
                                    product.getStringField(ProductFields.UNIT))));
                    entity.setField(ORDERS_GROUP, order.getBelongsToField(ORDERS_GROUP));
                    entity.setField("regeneratePQC", true);

                    entity = entity.getDataDefinition().save(entity);
                    if (!entity.isValid()) {
                        throw new IllegalStateException("Undone split orders");
                    }
                    counter++;
                }
            }


        } catch (Exception ex) {
            generatedCheckBox.setChecked(false);
            view.addMessage("orders.splitOrdersDetails.messages.errors.splitOrdersError", ComponentState.MessageType.FAILURE);
            return;
        }
        view.addMessage("orders.splitOrdersDetails.messages.success", ComponentState.MessageType.SUCCESS);

        generatedCheckBox.setChecked(true);
    }

    public void splitOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent splitForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity splitEntity = splitForm.getPersistedEntityWithIncludedFormValues();

        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        List<Entity> parents = splitEntity
                .getHasManyField(SplitOrderHelperConstants.PARENTS);
        int count = 0;
        for (Entity parent : parents) {
            count = count + parent.getHasManyField(SplitOrderParentConstants.CHILDES).size();
        }

        if (count == 0) {
            view.addMessage("orders.splitOrdersDetails.messages.info.splitOrdersEmpty", ComponentState.MessageType.INFO);
            return;
        }

        try {

            if (!validate(splitEntity)) {
                splitForm.setEntity(splitEntity);
                generatedCheckBox.setChecked(false);
                view.addMessage("orders.splitOrdersDetails.messages.errors.splitOrdersError", ComponentState.MessageType.FAILURE);
                return;
            }

            trySplitOrders(splitEntity);

        } catch (Exception ex) {
            generatedCheckBox.setChecked(false);
            splitForm.setEntity(splitEntity);
            view.addMessage("orders.splitOrdersDetails.messages.errors.splitOrdersError", ComponentState.MessageType.FAILURE);
            return;
        }
        view.addMessage("orders.splitOrdersDetails.messages.success", ComponentState.MessageType.SUCCESS);

        generatedCheckBox.setChecked(true);
    }

    @Transactional
    public void trySplitOrders(Entity splitEntity) {
        List<Entity> parents = splitEntity
                .getHasManyField(SplitOrderHelperConstants.PARENTS);
        for (Entity parent : parents) {
            Entity order = parent.getDataDefinition().get(parent.getId()).getBelongsToField(SplitOrderParentConstants.ORDER);
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);

            for (Entity child : parent.getHasManyField(SplitOrderParentConstants.CHILDES)) {

                Entity entity = order.getDataDefinition().copy(order.getId()).get(0);

                entity.setField(OrderFields.START_DATE, null);
                entity.setField(OrderFields.FINISH_DATE, null);
                entity.setField(OrderFields.DATE_FROM, null);
                entity.setField(OrderFields.DATE_TO, null);

                entity.setField(OrderFields.NUMBER, child.getStringField(SplitOrderChildConstants.NUMBER));
                entity.setField(OrderFields.PLANNED_QUANTITY, child.getDecimalField(SplitOrderChildConstants.PLANNED_QUANTITY));
                entity.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT,
                        numberService.setScaleWithDefaultMathContext(additionalUnitService.getQuantityAfterConversion(order,
                                additionalUnitService.getAdditionalUnit(product),
                                numberService.setScaleWithDefaultMathContext(child.getDecimalField(SplitOrderChildConstants.PLANNED_QUANTITY)),
                                product.getStringField(ProductFields.UNIT))));
                entity.setField(OrderFields.DATE_FROM, child.getDateField(SplitOrderChildConstants.DATE_FROM));
                entity.setField(OrderFields.DATE_TO, child.getDateField(SplitOrderChildConstants.DATE_TO));
                entity.setField(ORDERS_GROUP, order.getBelongsToField(ORDERS_GROUP));
                entity.setField("regeneratePQC", true);
                entity = entity.getDataDefinition().save(entity);
                if (!entity.isValid()) {
                    throw new IllegalStateException("Undone split orders");
                }
            }
            order.setField(OrderFields.START_DATE, null);
            order.setField(OrderFields.FINISH_DATE, null);
            order.setField(OrderFields.DATE_FROM, null);
            order.setField(OrderFields.DATE_TO, null);
            order.setField(OrderFields.DATE_FROM, parent.getDateField(SplitOrderParentConstants.DATE_FROM));
            order.setField(OrderFields.DATE_TO, parent.getDateField(SplitOrderParentConstants.DATE_TO));
            order.setField(OrderFields.PLANNED_QUANTITY, parent.getDecimalField(SplitOrderChildConstants.PLANNED_QUANTITY));
            order.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT,
                    numberService.setScaleWithDefaultMathContext(additionalUnitService.getQuantityAfterConversion(order,
                            additionalUnitService.getAdditionalUnit(product),
                            numberService.setScaleWithDefaultMathContext(parent.getDecimalField(SplitOrderChildConstants.PLANNED_QUANTITY)),
                            product.getStringField(ProductFields.UNIT))));
            order = order.getDataDefinition().save(order);
            if (!order.isValid()) {
                throw new IllegalStateException("Undone split orders");
            }
        }

    }

    private boolean validate(Entity splitEntity) {
        boolean isValid = true;
        List<Entity> parents = splitEntity
                .getHasManyField(SplitOrderHelperConstants.PARENTS);

        for (Entity parent : parents) {
            if (parent.getDecimalField(SplitOrderParentConstants.PLANNED_QUANTITY).compareTo(BigDecimal.ZERO) < 1) {
                parent.addError(parent.getDataDefinition().getField(SplitOrderParentConstants.PLANNED_QUANTITY), "orders.splitOrdersDetails.messages.errors.parentOrderPlannedQuantity");
                isValid = false;
            }

            for (Entity child : parent.getHasManyField(SplitOrderParentConstants.CHILDES)) {
                Date dateFrom = child.getDateField(SplitOrderChildConstants.DATE_FROM);
                Date dateTo = child.getDateField(SplitOrderChildConstants.DATE_TO);

                if (Objects.nonNull(dateFrom) && Objects.nonNull(dateTo) && (dateFrom.after(dateTo) || dateFrom.equals(dateTo))) {
                    child.addError(child.getDataDefinition().getField(SplitOrderChildConstants.DATE_TO), "orders.validate.global.error.datesOrder");
                    isValid = false;
                } else {
                    Entity validate = child.getDataDefinition().validate(child);
                    if(!validate.isValid()) {
                        child = validate;
                        isValid = false;
                    }
                }


            }
        }
        return isValid;
    }

    public void fillDates(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent splitForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity splitEntity = splitForm.getPersistedEntityWithIncludedFormValues();
        Date dateFrom = splitEntity.getDateField(SplitOrderHelperConstants.DATE_FROM);
        Date dateTo = splitEntity.getDateField(SplitOrderHelperConstants.DATE_TO);

        if (Objects.nonNull(dateFrom) && Objects.nonNull(dateTo) && (dateFrom.after(dateTo) || dateFrom.equals(dateTo))) {
            splitEntity.addError(splitEntity.getDataDefinition().getField(SplitOrderHelperConstants.DATE_TO), "orders.validate.global.error.datesOrder");
            view.addMessage("qcadooView.validate.global.error.custom", ComponentState.MessageType.FAILURE);
            splitForm.setEntity(splitEntity);
            return;
        }

        AwesomeDynamicListComponent parents = (AwesomeDynamicListComponent) view.getComponentByReference(L_PARENTS);
        List<FormComponent> parentsFormComponents = parents.getFormComponents();

        for (FormComponent parentFormComponent : parentsFormComponents) {
            AwesomeDynamicListComponent childADL = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);
            Entity parent = parentFormComponent.getEntity();
            parent.setField(SplitOrderChildConstants.DATE_FROM, splitEntity.getDateField(SplitOrderHelperConstants.DATE_FROM));
            parent.setField(SplitOrderChildConstants.DATE_TO, splitEntity.getDateField(SplitOrderHelperConstants.DATE_TO));
            parentFormComponent.setEntity(parent);
            for (FormComponent form : childADL.getFormComponents()) {

                Entity child = form.getEntity();
                child.setField(SplitOrderChildConstants.DATE_FROM, splitEntity.getDateField(SplitOrderHelperConstants.DATE_FROM));
                child.setField(SplitOrderChildConstants.DATE_TO, splitEntity.getDateField(SplitOrderHelperConstants.DATE_TO));
                form.setEntity(child);
            }
        }

        view.addMessage("orders.splitOrdersDetails.messages.fillDates.success", ComponentState.MessageType.SUCCESS);
    }

    public void onPlannedQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        AwesomeDynamicListComponent parents = (AwesomeDynamicListComponent) view.getComponentByReference(L_PARENTS);
        List<FormComponent> parentsFormComponents = parents.getFormComponents();

        for (FormComponent parentFormComponent : parentsFormComponents) {
            Entity entity = parentFormComponent.getEntity();

            AwesomeDynamicListComponent child = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

            for (FormComponent form : child.getFormComponents()) {

                FieldComponent plannedQuantityCmp = form.findFieldComponentByName(SplitOrderChildConstants.PLANNED_QUANTITY);
                if (plannedQuantityCmp.getUuid().equals(state.getUuid())) {
                    setParentPlannedQuantity(parentFormComponent, entity.getDataDefinition().get(entity.getId()), entity);
                }

            }
        }
    }

    public void onAddOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent parents = (AwesomeDynamicListComponent) view.getComponentByReference(L_PARENTS);

        List<FormComponent> parentsFormComponents = parents.getFormComponents();

        for (FormComponent parentFormComponent : parentsFormComponents) {
            Entity entity = parentFormComponent.getEntity();

            AwesomeDynamicListComponent child = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

            if (child.getUuid().equals(state.getUuid())) {

                FormComponent formComponent = child.getFormComponents().get(child.getFormComponents().size() - 1);

                Entity newChild = formComponent.getEntity();
                newChild.setField(SplitOrderChildConstants.NUMBER, entity.getStringField(OrderFields.NUMBER));
                newChild.setField(SplitOrderChildConstants.NAME, entity.getStringField(OrderFields.NAME));
                newChild.setField(SplitOrderChildConstants.DATE_FROM, entity.getDateField(OrderFields.DATE_FROM));
                newChild.setField(SplitOrderChildConstants.DATE_TO, entity.getDateField(OrderFields.DATE_TO));
                newChild.setField(SplitOrderChildConstants.UNIT, entity.getStringField(ProductFields.UNIT));

                formComponent.setEntity(newChild);

                updateChildesNumbers(parentFormComponent);
            }
        }


    }

    private void updateChildesNumbers(FormComponent parentFormComponent) {
        Entity parent = parentFormComponent.getEntity();

        AwesomeDynamicListComponent child = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);
        String numberPrefix = parent.getStringField(SplitOrderParentConstants.NUMBER) + "-";
        String baseNumber = numberGeneratorService.generateNumberWithPrefix(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER, 3, numberPrefix);
        String suffix = baseNumber.replace(numberPrefix, "");
        Integer counter = Integer.parseInt(suffix.replaceFirst("^0+(?!$)", ""));

        for (FormComponent form : child.getFormComponents()) {
            Entity entity = form.getEntity();
            String number = parent.getStringField(SplitOrderParentConstants.NUMBER) + "-" + threeCharacterNumber.format(counter);
            entity.setField(SplitOrderChildConstants.NUMBER, number);
            form.setEntity(entity);
            counter++;
        }
    }

    public void onDeleteRow(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent parents = (AwesomeDynamicListComponent) view.getComponentByReference(L_PARENTS);
        List<FormComponent> parentsFormComponents = parents.getFormComponents();

        for (FormComponent parentFormComponent : parentsFormComponents) {
            Entity entity = parentFormComponent.getEntity();
            AwesomeDynamicListComponent child = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

            if (child.getUuid().equals(state.getUuid())) {
                setParentPlannedQuantity(parentFormComponent, entity.getDataDefinition().get(entity.getId()), entity);
                updateChildesNumbers(parentFormComponent);
            }
        }

    }


    private void setParentPlannedQuantity(FormComponent parentFormComponent, Entity parent, Entity formParentEntity) {
        AwesomeDynamicListComponent childs = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);
        for (FormComponent formComponent : childs.getFormComponents()) {
            Entity persistedEntityWithIncludedFormValues = formComponent.getPersistedEntityWithIncludedFormValues();
            Entity validate = persistedEntityWithIncludedFormValues.getDataDefinition().validate(persistedEntityWithIncludedFormValues);
            if(!validate.isValid()){
                formComponent.setEntity(persistedEntityWithIncludedFormValues);
                return;
            }
        }
        Optional<BigDecimal> plannedQuantity = childs.getEntities().stream().map(c -> BigDecimalUtils.convertNullToZero(c.getDecimalField("plannedQuantity"))).reduce(BigDecimal::add);
        if (plannedQuantity.isPresent()) {
            Entity order = parent.getBelongsToField(SplitOrderParentConstants.ORDER);

            BigDecimal parentPlannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
            BigDecimal plannedQuantityAfterSplit = parentPlannedQuantity.subtract(plannedQuantity.get(), numberService.getMathContext());

            formParentEntity.setField(SplitOrderParentConstants.PLANNED_QUANTITY, plannedQuantityAfterSplit);
            parentFormComponent.setEntity(formParentEntity);
        }
    }

}
