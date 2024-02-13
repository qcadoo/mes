package com.qcadoo.mes.orders.listeners;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.util.AdditionalUnitService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.CopyException;
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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SplitOrdersListeners {

    private static final DecimalFormat L_THREE_CHARACTER_NUMBER = new DecimalFormat("000");

    private static final String L_PARENTS = "parents";

    private static final String L_CHILDES = "childes";

    private static final String L_GENERATED = "generated";

    private static final String L_ORDERS_GROUP = "ordersGroup";

    private static final String L_REGENERATE_PQC = "regeneratePQC";

    @Autowired
    private NumberService numberService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private AdditionalUnitService additionalUnitService;

    public void divideOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent splitOrderHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        state.performEvent(view, "save", args);

        if (state.isHasError()) {
            return;
        }

        boolean isGenerated = true;

        try {
            Entity splitOrderHelper = splitOrderHelperForm.getEntity().getDataDefinition().get(splitOrderHelperForm.getEntityId());

            Integer parts = splitOrderHelper.getIntegerField(SplitOrderHelperFields.PARTS);
            List<Entity> parents = splitOrderHelper.getHasManyField(SplitOrderHelperFields.PARENTS);

            for (Entity parent : parents) {
                BigDecimal quantity = parent.getDecimalField(OrderFields.PLANNED_QUANTITY);

                BigDecimal plannedQuantity = quantity.divide(new BigDecimal(parts), MathContext.DECIMAL64).setScale(0,
                        RoundingMode.DOWN);

                if (BigDecimal.ZERO.compareTo(plannedQuantity) == 0) {
                    throw new IllegalStateException("Error");
                }

                BigDecimal rest = quantity.subtract(plannedQuantity.multiply(new BigDecimal(parts), MathContext.DECIMAL64));

                Entity order = parent.getBelongsToField(SplitOrderParentFields.ORDER);
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);

                String numberPrefix = parent.getStringField(SplitOrderParentFields.NUMBER) + "-";
                String baseNumber = numberGeneratorService.generateNumberWithPrefix(OrdersConstants.PLUGIN_IDENTIFIER,
                        OrdersConstants.MODEL_ORDER, 3, numberPrefix);
                String suffix = baseNumber.replace(numberPrefix, "");
                Integer counter = Integer.parseInt(suffix.replaceFirst("^0+(?!$)", ""));

                for (int i = 0; i < parts; i++) {
                    Entity orderPart;

                    if (i == parts - 1) {
                        plannedQuantity = plannedQuantity.add(rest);

                        orderPart = order;
                    } else {
                        orderPart = order.getDataDefinition().copy(order.getId()).get(0);
                    }

                    String number = order.getStringField(SplitOrderParentFields.NUMBER) + "-" + L_THREE_CHARACTER_NUMBER.format(counter);
                    BigDecimal plannedQuantityForAdditionalUnit = additionalUnitService.getQuantityAfterConversion(order,
                            additionalUnitService.getAdditionalUnit(product),
                            numberService.setScaleWithDefaultMathContext(plannedQuantity),
                            product.getStringField(ProductFields.UNIT));

                    orderPart.setField(OrderFields.NUMBER, number);
                    orderPart.setField(OrderFields.PLANNED_QUANTITY,
                            numberService.setScaleWithDefaultMathContext(plannedQuantity));
                    orderPart.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT,
                            numberService.setScaleWithDefaultMathContext(plannedQuantityForAdditionalUnit));
                    orderPart.setField(L_ORDERS_GROUP, order.getBelongsToField(L_ORDERS_GROUP));
                    orderPart.setField(L_REGENERATE_PQC, true);

                    orderPart = orderPart.getDataDefinition().save(orderPart);

                    if (!orderPart.isValid()) {
                        throw new IllegalStateException("Undone split orders");
                    }

                    counter++;
                }
            }
        } catch (CopyException ce) {
            ce.getEntity().getErrors().values().forEach(view::addMessage);

            isGenerated = false;
        } catch (Exception ex) {
            isGenerated = false;
        }

        if (isGenerated) {
            view.addMessage("orders.splitOrdersDetails.messages.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("orders.splitOrdersDetails.messages.errors.splitOrdersError", ComponentState.MessageType.FAILURE);
        }

        generatedCheckBox.setChecked(isGenerated);
    }

    public void splitOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent splitOrderHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        Entity splitOrderHelper = splitOrderHelperForm.getPersistedEntityWithIncludedFormValues();

        List<Entity> parents = splitOrderHelper.getHasManyField(SplitOrderHelperFields.PARENTS);

        int count = 0;

        for (Entity parent : parents) {
            count = count + parent.getHasManyField(SplitOrderParentFields.CHILDES).size();
        }

        if (count == 0) {
            view.addMessage("orders.splitOrdersDetails.messages.info.splitOrdersEmpty", ComponentState.MessageType.INFO);

            return;
        }

        boolean isGenerated = true;

        try {
            if (!validate(splitOrderHelper)) {
                splitOrderHelperForm.setEntity(splitOrderHelper);

                generatedCheckBox.setChecked(false);
                view.addMessage("orders.splitOrdersDetails.messages.errors.splitOrdersError", ComponentState.MessageType.FAILURE);

                return;
            }

            trySplitOrders(splitOrderHelper);
        } catch (CopyException ce) {
            ce.getEntity().getErrors().values().forEach(view::addMessage);

            isGenerated = false;
        } catch (Exception ex) {
            splitOrderHelperForm.setEntity(splitOrderHelper);

            isGenerated = false;
        }

        if (isGenerated) {
            view.addMessage("orders.splitOrdersDetails.messages.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("orders.splitOrdersDetails.messages.errors.splitOrdersError", ComponentState.MessageType.FAILURE);
        }

        generatedCheckBox.setChecked(isGenerated);
    }

    private boolean validate(final Entity splitOrderHelper) {
        boolean isValid = true;

        List<Entity> parents = splitOrderHelper.getHasManyField(SplitOrderHelperFields.PARENTS);

        for (Entity parent : parents) {
            if (parent.getDecimalField(SplitOrderParentFields.PLANNED_QUANTITY).compareTo(BigDecimal.ZERO) < 1) {
                parent.addError(parent.getDataDefinition().getField(SplitOrderParentFields.PLANNED_QUANTITY), "orders.splitOrdersDetails.messages.errors.parentOrderPlannedQuantity");

                isValid = false;
            }

            for (Entity child : parent.getHasManyField(SplitOrderParentFields.CHILDES)) {
                Date dateFrom = child.getDateField(SplitOrderChildFields.DATE_FROM);
                Date dateTo = child.getDateField(SplitOrderChildFields.DATE_TO);

                if (Objects.nonNull(dateFrom) && Objects.nonNull(dateTo) && (dateFrom.after(dateTo) || dateFrom.equals(dateTo))) {
                    child.addError(child.getDataDefinition().getField(SplitOrderChildFields.DATE_TO), "orders.validate.global.error.datesOrder");

                    isValid = false;
                } else {
                    Entity validate = child.getDataDefinition().validate(child);

                    if (!validate.isValid()) {
                        child = validate;

                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }

    @Transactional
    public void trySplitOrders(final Entity splitOrderHelper) {
        List<Entity> parents = splitOrderHelper.getHasManyField(SplitOrderHelperFields.PARENTS);

        for (Entity parent : parents) {
            Entity order = parent.getDataDefinition().get(parent.getId()).getBelongsToField(SplitOrderParentFields.ORDER);

            Entity product = order.getBelongsToField(OrderFields.PRODUCT);

            for (Entity child : parent.getHasManyField(SplitOrderParentFields.CHILDES)) {
                BigDecimal plannedQuantityForAdditionalUnit = additionalUnitService.getQuantityAfterConversion(order,
                        additionalUnitService.getAdditionalUnit(product),
                        numberService.setScaleWithDefaultMathContext(child.getDecimalField(SplitOrderChildFields.PLANNED_QUANTITY)),
                        product.getStringField(ProductFields.UNIT));

                Entity orderPart = order.getDataDefinition().copy(order.getId()).get(0);

                orderPart.setField(OrderFields.START_DATE, null);
                orderPart.setField(OrderFields.FINISH_DATE, null);
                orderPart.setField(OrderFields.DATE_FROM, null);
                orderPart.setField(OrderFields.DATE_TO, null);
                orderPart.setField(OrderFields.NUMBER, child.getStringField(SplitOrderChildFields.NUMBER));
                orderPart.setField(OrderFields.PLANNED_QUANTITY,
                        numberService.setScaleWithDefaultMathContext(child.getDecimalField(SplitOrderChildFields.PLANNED_QUANTITY)));
                orderPart.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT,
                        numberService.setScaleWithDefaultMathContext(plannedQuantityForAdditionalUnit));
                orderPart.setField(OrderFields.DATE_FROM, child.getDateField(SplitOrderChildFields.DATE_FROM));
                orderPart.setField(OrderFields.DATE_TO, child.getDateField(SplitOrderChildFields.DATE_TO));
                orderPart.setField(L_ORDERS_GROUP, order.getBelongsToField(L_ORDERS_GROUP));
                orderPart.setField(L_REGENERATE_PQC, true);

                orderPart = orderPart.getDataDefinition().save(orderPart);

                if (!orderPart.isValid()) {
                    throw new IllegalStateException("Undone split orders");
                }
            }

            BigDecimal plannedQuantityForAdditionalUnit = additionalUnitService.getQuantityAfterConversion(order,
                    additionalUnitService.getAdditionalUnit(product),
                    numberService.setScaleWithDefaultMathContext(parent.getDecimalField(SplitOrderChildFields.PLANNED_QUANTITY)),
                    product.getStringField(ProductFields.UNIT));

            order.setField(OrderFields.START_DATE, null);
            order.setField(OrderFields.FINISH_DATE, null);
            order.setField(OrderFields.DATE_FROM, null);
            order.setField(OrderFields.DATE_TO, null);
            order.setField(OrderFields.DATE_FROM, parent.getDateField(SplitOrderParentFields.DATE_FROM));
            order.setField(OrderFields.DATE_TO, parent.getDateField(SplitOrderParentFields.DATE_TO));
            order.setField(OrderFields.PLANNED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(parent.getDecimalField(SplitOrderChildFields.PLANNED_QUANTITY)));
            order.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT,
                    numberService.setScaleWithDefaultMathContext(plannedQuantityForAdditionalUnit));

            order = order.getDataDefinition().save(order);

            if (!order.isValid()) {
                throw new IllegalStateException("Undone split orders");
            }
        }
    }

    public void fillDates(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent splitOrderHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity splitOrderHelper = splitOrderHelperForm.getPersistedEntityWithIncludedFormValues();

        Date dateFrom = splitOrderHelper.getDateField(SplitOrderHelperFields.DATE_FROM);
        Date dateTo = splitOrderHelper.getDateField(SplitOrderHelperFields.DATE_TO);

        if (Objects.nonNull(dateFrom) && Objects.nonNull(dateTo) && (dateFrom.after(dateTo) || dateFrom.equals(dateTo))) {
            splitOrderHelper.addError(splitOrderHelper.getDataDefinition().getField(SplitOrderHelperFields.DATE_TO), "orders.validate.global.error.datesOrder");

            view.addMessage("qcadooView.validate.global.error.custom", ComponentState.MessageType.FAILURE);

            splitOrderHelperForm.setEntity(splitOrderHelper);

            return;
        }

        AwesomeDynamicListComponent parentsADL = (AwesomeDynamicListComponent) view.getComponentByReference(L_PARENTS);

        for (FormComponent parentFormComponent : parentsADL.getFormComponents()) {
            AwesomeDynamicListComponent childesADL = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

            Entity parent = parentFormComponent.getEntity();

            parent.setField(SplitOrderChildFields.DATE_FROM, splitOrderHelper.getDateField(SplitOrderHelperFields.DATE_FROM));
            parent.setField(SplitOrderChildFields.DATE_TO, splitOrderHelper.getDateField(SplitOrderHelperFields.DATE_TO));

            parentFormComponent.setEntity(parent);

            for (FormComponent formComponent : childesADL.getFormComponents()) {
                Entity child = formComponent.getEntity();

                child.setField(SplitOrderChildFields.DATE_FROM, splitOrderHelper.getDateField(SplitOrderHelperFields.DATE_FROM));
                child.setField(SplitOrderChildFields.DATE_TO, splitOrderHelper.getDateField(SplitOrderHelperFields.DATE_TO));

                formComponent.setEntity(child);
            }
        }

        view.addMessage("orders.splitOrdersDetails.messages.fillDates.success", ComponentState.MessageType.SUCCESS);
    }

    public void onPlannedQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent parentsADL = (AwesomeDynamicListComponent) view.getComponentByReference(L_PARENTS);

        for (FormComponent parentFormComponent : parentsADL.getFormComponents()) {
            Entity parent = parentFormComponent.getEntity();

            AwesomeDynamicListComponent childesADL = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

            for (FormComponent formComponent : childesADL.getFormComponents()) {
                FieldComponent plannedQuantityField = formComponent.findFieldComponentByName(SplitOrderChildFields.PLANNED_QUANTITY);

                if (plannedQuantityField.getUuid().equals(state.getUuid())) {
                    setParentPlannedQuantity(parentFormComponent, parent.getDataDefinition().get(parent.getId()), parent);
                }
            }
        }
    }

    public void onAddOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent parentsADL = (AwesomeDynamicListComponent) view.getComponentByReference(L_PARENTS);

        for (FormComponent parentFormComponent : parentsADL.getFormComponents()) {
            Entity parent = parentFormComponent.getEntity();

            AwesomeDynamicListComponent childesADL = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

            if (childesADL.getUuid().equals(state.getUuid())) {
                FormComponent formComponent = childesADL.getFormComponents().get(childesADL.getFormComponents().size() - 1);

                Entity newChild = formComponent.getEntity();

                newChild.setField(SplitOrderChildFields.NUMBER, parent.getStringField(OrderFields.NUMBER));
                newChild.setField(SplitOrderChildFields.NAME, parent.getStringField(OrderFields.NAME));
                newChild.setField(SplitOrderChildFields.DATE_FROM, parent.getDateField(OrderFields.DATE_FROM));
                newChild.setField(SplitOrderChildFields.DATE_TO, parent.getDateField(OrderFields.DATE_TO));
                newChild.setField(SplitOrderChildFields.UNIT, parent.getStringField(ProductFields.UNIT));

                formComponent.setEntity(newChild);

                updateChildesNumbers(parentFormComponent);
            }
        }
    }

    private void updateChildesNumbers(final FormComponent parentFormComponent) {
        Entity parent = parentFormComponent.getEntity();

        String numberPrefix = parent.getStringField(SplitOrderParentFields.NUMBER) + "-";
        String baseNumber = numberGeneratorService.generateNumberWithPrefix(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER, 3, numberPrefix);
        String suffix = baseNumber.replace(numberPrefix, "");
        Integer counter = Integer.parseInt(suffix.replaceFirst("^0+(?!$)", ""));

        AwesomeDynamicListComponent childesADL = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

        for (FormComponent formComponent : childesADL.getFormComponents()) {
            Entity entity = formComponent.getEntity();

            String number = parent.getStringField(SplitOrderParentFields.NUMBER) + "-" + L_THREE_CHARACTER_NUMBER.format(counter);

            entity.setField(SplitOrderChildFields.NUMBER, number);

            formComponent.setEntity(entity);

            counter++;
        }
    }

    public void onDeleteRow(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent parentsADL = (AwesomeDynamicListComponent) view.getComponentByReference(L_PARENTS);

        for (FormComponent parentFormComponent : parentsADL.getFormComponents()) {
            Entity parent = parentFormComponent.getEntity();

            AwesomeDynamicListComponent childesADL = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

            if (childesADL.getUuid().equals(state.getUuid())) {
                setParentPlannedQuantity(parentFormComponent, parent.getDataDefinition().get(parent.getId()), parent);

                updateChildesNumbers(parentFormComponent);
            }
        }
    }

    private void setParentPlannedQuantity(final FormComponent parentFormComponent, final Entity parent, final Entity formParent) {
        AwesomeDynamicListComponent childesADL = (AwesomeDynamicListComponent) parentFormComponent.findFieldComponentByName(L_CHILDES);

        for (FormComponent formComponent : childesADL.getFormComponents()) {
            Entity persistedEntityWithIncludedFormValues = formComponent.getPersistedEntityWithIncludedFormValues();

            Entity validate = persistedEntityWithIncludedFormValues.getDataDefinition().validate(persistedEntityWithIncludedFormValues);

            if (!validate.isValid()) {
                formComponent.setEntity(persistedEntityWithIncludedFormValues);

                return;
            }
        }

        Optional<BigDecimal> plannedQuantity = childesADL.getEntities().stream()
                .map(child -> BigDecimalUtils.convertNullToZero(child.getDecimalField(SplitOrderChildFields.PLANNED_QUANTITY)))
                .reduce(BigDecimal::add);

        if (plannedQuantity.isPresent()) {
            Entity order = parent.getBelongsToField(SplitOrderParentFields.ORDER);

            BigDecimal parentPlannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
            BigDecimal plannedQuantityAfterSplit = parentPlannedQuantity.subtract(plannedQuantity.get(), numberService.getMathContext());

            formParent.setField(SplitOrderParentFields.PLANNED_QUANTITY, plannedQuantityAfterSplit);

            parentFormComponent.setEntity(formParent);
        }
    }

}
