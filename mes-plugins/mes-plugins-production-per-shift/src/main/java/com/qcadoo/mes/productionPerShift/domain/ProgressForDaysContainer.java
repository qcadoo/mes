package com.qcadoo.mes.productionPerShift.domain;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

public class ProgressForDaysContainer {

    private List<Entity> progressForDays = Lists.newArrayList();

    private List<ErrorMessage> errors = Lists.newArrayList();

    private List<PpsMessage> messages = Lists.newArrayList();

    private boolean calculationError;

    private boolean partCalculation;

    private DateTime partCalculationToDate;

    private Entity operationComponent;

    private Entity order;

    private boolean shouldBeCorrected = false;

    private BigDecimal plannedQuantity = BigDecimal.ZERO;

    private BigDecimal alreadyRegisteredQuantity = BigDecimal.ZERO;

    public void addError(ErrorMessage errorMessage) {
        errors.add(errorMessage);
    }

    public void addErrors(List<ErrorMessage> errorsMessage) {
        errors.addAll(errorsMessage);
    }

    public void addMessage(PpsMessage message){
        messages.add(message);
    }

    public List<PpsMessage> getMessages() {
        return messages;
    }

    public List<ErrorMessage> getErrors() {
        return errors;
    }

    public List<Entity> getProgressForDays() {
        return progressForDays;
    }

    public void setProgressForDays(List<Entity> progressForDays) {
        this.progressForDays = progressForDays;
    }

    public boolean isCalculationError() {
        return calculationError;
    }

    public void setCalculationError(boolean calculationError) {
        this.calculationError = calculationError;
    }

    public boolean isPartCalculation() {
        return partCalculation;
    }

    public void setPartCalculation(boolean partCalculation) {
        this.partCalculation = partCalculation;
    }

    public DateTime getPartCalculationToDate() {
        return partCalculationToDate;
    }

    public void setPartCalculationToDate(DateTime partCalculationToDate) {
        this.partCalculationToDate = partCalculationToDate;
    }

    public Entity getOperationComponent() {
        return operationComponent;
    }

    public void setOperationComponent(Entity operationComponent) {
        this.operationComponent = operationComponent;
    }

    public boolean isShouldBeCorrected() {
        return shouldBeCorrected;
    }

    public void setShouldBeCorrected(boolean shouldBeCorrected) {
        this.shouldBeCorrected = shouldBeCorrected;
    }

    public Entity getOrder() {
        return order;
    }

    public void setOrder(Entity order) {
        this.order = order;
    }

    public BigDecimal getAlreadyRegisteredQuantity() {
        return alreadyRegisteredQuantity;
    }

    public void setAlreadyRegisteredQuantity(BigDecimal alreadyRegisteredQuantity) {
        this.alreadyRegisteredQuantity = alreadyRegisteredQuantity;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }
}
