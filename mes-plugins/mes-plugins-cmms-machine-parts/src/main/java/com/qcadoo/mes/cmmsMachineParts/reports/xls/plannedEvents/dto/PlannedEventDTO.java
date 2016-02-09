package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto;

import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class PlannedEventDTO {

    private Long id;

    private String number;

    private String type;

    private String description;

    private String ownerName;

    private String state;

    private String factoryNumber;

    private String divisionNumber;

    private String productionLineNumber;

    private String workstationNumber;

    private String subassemblyNumber;

    private String companyNumber;

    private String sourceCostNumber;

    private Boolean plannedSeparately;

    private Boolean requiresShutdown;

    private String basedOn;

    private Date date;

    private BigDecimal counter;

    private BigDecimal counterTolerance;

    private Integer duration;

    private Integer effectiveDuration;

    private BigDecimal effectiveCounter;

    private Date startDate;

    private Date finishDate;

    private Boolean isDeadline;

    private String solutionDescription;

    private Date createdate;

    private List<PlannedEventStateChangeDTO> stateChanges = Lists.newArrayList();

    private List<PlannedEventRealizationDTO> realizations = Lists.newArrayList();

    private List<MachinePartForEventDTO> parts = Lists.newArrayList();

    //-----
    private Long machinePartId;

    private String machinePartName;

    private String machinePartNumber;

    private String machinePartUnit;

    private BigDecimal machinePartPlannedQuantity;
    private BigDecimal value;
    private BigDecimal lastPurchaseCost;
    private BigDecimal priceFromDocumentPosition;
    private BigDecimal priceFromPosition;
    private BigDecimal quantityFromPosition;
    private String createuser;
    //------
    private Long realizationId;

    private String realizationWorkerName;

    private String realizationWorkerSurname;

    private Integer realizationDuration;

    //------
    private Long stateChangeId;

    private Date stateChangeDateAndTime;

    private String stateChangeSourceState;

    private String stateChangeTargetState;

    private String stateStatus;

    private String stateWorker;

    public int subListSize() {
        return realizations.size() > parts.size() ? realizations.size() : parts.size();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFactoryNumber() {
        return factoryNumber;
    }

    public void setFactoryNumber(String factoryNumber) {
        this.factoryNumber = factoryNumber;
    }

    public String getDivisionNumber() {
        return divisionNumber;
    }

    public void setDivisionNumber(String divisionNumber) {
        this.divisionNumber = divisionNumber;
    }

    public String getProductionLineNumber() {
        return productionLineNumber;
    }

    public void setProductionLineNumber(String productionLineNumber) {
        this.productionLineNumber = productionLineNumber;
    }

    public String getWorkstationNumber() {
        return workstationNumber;
    }

    public void setWorkstationNumber(String workstationNumber) {
        this.workstationNumber = workstationNumber;
    }

    public String getSubassemblyNumber() {
        return subassemblyNumber;
    }

    public void setSubassemblyNumber(String subassemblyNumber) {
        this.subassemblyNumber = subassemblyNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getSourceCostNumber() {
        return sourceCostNumber;
    }

    public void setSourceCostNumber(String sourceCostNumber) {
        this.sourceCostNumber = sourceCostNumber;
    }

    public Boolean getPlannedSeparately() {
        return plannedSeparately;
    }

    public void setPlannedSeparately(Boolean plannedSeparately) {
        this.plannedSeparately = plannedSeparately;
    }

    public Boolean getRequiresShutdown() {
        return requiresShutdown;
    }

    public void setRequiresShutdown(Boolean requiresShutdown) {
        this.requiresShutdown = requiresShutdown;
    }

    public String getBasedOn() {
        return basedOn;
    }

    public void setBasedOn(String basedOn) {
        this.basedOn = basedOn;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getCounter() {
        return counter;
    }

    public void setCounter(BigDecimal counter) {
        this.counter = counter;
    }

    public BigDecimal getCounterTolerance() {
        return counterTolerance;
    }

    public void setCounterTolerance(BigDecimal counterTolerance) {
        this.counterTolerance = counterTolerance;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getEffectiveDuration() {
        return effectiveDuration;
    }

    public void setEffectiveDuration(Integer effectiveDuration) {
        this.effectiveDuration = effectiveDuration;
    }

    public BigDecimal getEffectiveCounter() {
        return effectiveCounter;
    }

    public void setEffectiveCounter(BigDecimal effectiveCounter) {
        this.effectiveCounter = effectiveCounter;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Boolean getIsDeadline() {
        return isDeadline;
    }

    public void setIsDeadline(Boolean isDeadline) {
        this.isDeadline = isDeadline;
    }

    public List<PlannedEventStateChangeDTO> getStateChanges() {
        return stateChanges;
    }

    public void setStateChanges(List<PlannedEventStateChangeDTO> stateChanges) {
        this.stateChanges = stateChanges;
    }

    public List<PlannedEventRealizationDTO> getRealizations() {
        return realizations;
    }

    public void setRealizations(List<PlannedEventRealizationDTO> realizations) {
        this.realizations = realizations;
    }

    public List<MachinePartForEventDTO> getParts() {
        return parts;
    }

    public void setParts(List<MachinePartForEventDTO> parts) {
        this.parts = parts;
    }

    public Long getMachinePartId() {
        return machinePartId;
    }

    public void setMachinePartId(Long machinePartId) {
        this.machinePartId = machinePartId;
    }

    public String getMachinePartName() {
        return machinePartName;
    }

    public void setMachinePartName(String machinePartName) {
        this.machinePartName = machinePartName;
    }

    public String getMachinePartNumber() {
        return machinePartNumber;
    }

    public void setMachinePartNumber(String machinePartNumber) {
        this.machinePartNumber = machinePartNumber;
    }

    public String getMachinePartUnit() {
        return machinePartUnit;
    }

    public void setMachinePartUnit(String machinePartUnit) {
        this.machinePartUnit = machinePartUnit;
    }

    public BigDecimal getMachinePartPlannedQuantity() {
        return machinePartPlannedQuantity;
    }

    public void setMachinePartPlannedQuantity(BigDecimal machinePartPlannedQuantity) {
        this.machinePartPlannedQuantity = machinePartPlannedQuantity;
    }

    public Long getRealizationId() {
        return realizationId;
    }

    public void setRealizationId(Long realizationId) {
        this.realizationId = realizationId;
    }

    public String getRealizationWorkerName() {
        return realizationWorkerName;
    }

    public void setRealizationWorkerName(String realizationWorkerName) {
        this.realizationWorkerName = realizationWorkerName;
    }

    public String getRealizationWorkerSurname() {
        return realizationWorkerSurname;
    }

    public void setRealizationWorkerSurname(String realizationWorkerSurname) {
        this.realizationWorkerSurname = realizationWorkerSurname;
    }

    public Integer getRealizationDuration() {
        return realizationDuration;
    }

    public void setRealizationDuration(Integer realizationDuration) {
        this.realizationDuration = realizationDuration;
    }

    public Long getStateChangeId() {
        return stateChangeId;
    }

    public void setStateChangeId(Long stateChangeId) {
        this.stateChangeId = stateChangeId;
    }

    public Date getStateChangeDateAndTime() {
        return stateChangeDateAndTime;
    }

    public void setStateChangeDateAndTime(Date stateChangeDateAndTime) {
        this.stateChangeDateAndTime = stateChangeDateAndTime;
    }

    public String getStateChangeSourceState() {
        return stateChangeSourceState;
    }

    public void setStateChangeSourceState(String stateChangeSourceState) {
        this.stateChangeSourceState = stateChangeSourceState;
    }

    public String getStateChangeTargetState() {
        return stateChangeTargetState;
    }

    public void setStateChangeTargetState(String stateChangeTargetState) {
        this.stateChangeTargetState = stateChangeTargetState;
    }

    public String getCreateuser() {
        return createuser;
    }

    public void setCreateuser(String createuser) {
        this.createuser = createuser;
    }

    public String getStateStatus() {
        return stateStatus;
    }

    public void setStateStatus(String stateStatus) {
        this.stateStatus = stateStatus;
    }

    public String getSolutionDescription() {
        return solutionDescription;
    }

    public void setSolutionDescription(String solutionDescription) {
        this.solutionDescription = solutionDescription;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }

    public String getStateWorker() {
        return stateWorker;
    }

    public void setStateWorker(String stateWorker) {
        this.stateWorker = stateWorker;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getLastPurchaseCost() {
        return lastPurchaseCost;
    }

    public void setLastPurchaseCost(BigDecimal lastPurchaseCost) {
        this.lastPurchaseCost = lastPurchaseCost;
    }

    public BigDecimal getPriceFromDocumentPosition() {
        return priceFromDocumentPosition;
    }

    public void setPriceFromDocumentPosition(BigDecimal priceFromDocumentPosition) {
        this.priceFromDocumentPosition = priceFromDocumentPosition;
    }

    public BigDecimal getPriceFromPosition() {
        return priceFromPosition;
    }

    public void setPriceFromPosition(BigDecimal priceFromPosition) {
        this.priceFromPosition = priceFromPosition;
    }

    public BigDecimal getQuantityFromPosition() {
        return quantityFromPosition;
    }

    public void setQuantityFromPosition(BigDecimal quantityFromPosition) {
        this.quantityFromPosition = quantityFromPosition;
    }
}
