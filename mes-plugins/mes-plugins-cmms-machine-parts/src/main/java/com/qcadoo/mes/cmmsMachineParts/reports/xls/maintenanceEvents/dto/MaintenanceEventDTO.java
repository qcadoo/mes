package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto;

import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class MaintenanceEventDTO {

    private Long id;
    private String number;
    private String type;
    private String factoryNumber;
    private String divisionNumber;
    private String productionLineNumber;
    private String workstationNumber;
    private String subassemblyNumber;
    private String faultTypeName;
    private String description;
    private String personReceiving;
    private String sourceCost;
    private Date createDate;
    private String createUser;
    private String state;
    private String solutionDescription;

    //WorkTimeDTO
    private Long staffworkTimeId;
    private String staffWorkTimeWorker;
    private Integer staffWorkTimeLaborTime;
    private List<WorkTimeDTO> workTimes = Lists.newArrayList();

    //StateChangeDTO
    private Long stateChangeId;
    private Date stateChangeDateAndTime;
    private String stateChangeSourceState;
    private String stateChangeTargetState;
    private String stateStatus;
    private String stateWorker;
    private List<StateChangeDTO> stateChange = Lists.newArrayList();

    //MachinePartDTO
    private Long machinePartId;
    private String partNumber;
    private String partName;
    private String warehouseNumber;
    private BigDecimal partPlannedQuantity;
    private BigDecimal value;
    private BigDecimal lastPurchaseCost;
    private BigDecimal priceFromDocumentPosition;
    private BigDecimal priceFromPosition;
    private BigDecimal quantityFromPosition;
    private String partUnit;

    private List<MachinePartDTO> machineParts = Lists.newArrayList();

    public int subListSize() {
        return workTimes.size() > machineParts.size() ? workTimes.size() : machineParts.size();
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

    public String getFaultTypeName() {
        return faultTypeName;
    }

    public void setFaultTypeName(String faultTypeName) {
        this.faultTypeName = faultTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceCost() {
        return sourceCost;
    }

    public void setSourceCost(String sourceCost) {
        this.sourceCost = sourceCost;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSolutionDescription() {
        return solutionDescription;
    }

    public void setSolutionDescription(String solutionDescription) {
        this.solutionDescription = solutionDescription;
    }

    public Long getStaffworkTimeId() {
        return staffworkTimeId;
    }

    public void setStaffworkTimeId(Long staffworkTimeId) {
        this.staffworkTimeId = staffworkTimeId;
    }

    public String getStaffWorkTimeWorker() {
        return staffWorkTimeWorker;
    }

    public void setStaffWorkTimeWorker(String staffWorkTimeWorker) {
        this.staffWorkTimeWorker = staffWorkTimeWorker;
    }

    public Integer getStaffWorkTimeLaborTime() {
        return staffWorkTimeLaborTime;
    }

    public void setStaffWorkTimeLaborTime(Integer staffWorkTimeLaborTime) {
        this.staffWorkTimeLaborTime = staffWorkTimeLaborTime;
    }

    public List<WorkTimeDTO> getWorkTimes() {
        return workTimes;
    }

    public void setWorkTimes(List<WorkTimeDTO> workTimes) {
        this.workTimes = workTimes;
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

    public String getStateStatus() {
        return stateStatus;
    }

    public void setStateStatus(String stateStatus) {
        this.stateStatus = stateStatus;
    }

    public List<StateChangeDTO> getStateChange() {
        return stateChange;
    }

    public void setStateChange(List<StateChangeDTO> stateChange) {
        this.stateChange = stateChange;
    }

    public Long getMachinePartId() {
        return machinePartId;
    }

    public void setMachinePartId(Long machinePartId) {
        this.machinePartId = machinePartId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getWarehouseNumber() {
        return warehouseNumber;
    }

    public void setWarehouseNumber(String warehouseNumber) {
        this.warehouseNumber = warehouseNumber;
    }

    public BigDecimal getPartPlannedQuantity() {
        return partPlannedQuantity;
    }

    public void setPartPlannedQuantity(BigDecimal partPlannedQuantity) {
        this.partPlannedQuantity = partPlannedQuantity;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getPartUnit() {
        return partUnit;
    }

    public void setPartUnit(String partUnit) {
        this.partUnit = partUnit;
    }

    public List<MachinePartDTO> getMachineParts() {
        return machineParts;
    }

    public void setMachineParts(List<MachinePartDTO> machineParts) {
        this.machineParts = machineParts;
    }

    public String getPersonReceiving() {
        return personReceiving;
    }

    public void setPersonReceiving(String personReceiving) {
        this.personReceiving = personReceiving;
    }

    public String getStateWorker() {
        return stateWorker;
    }

    public void setStateWorker(String stateWorker) {
        this.stateWorker = stateWorker;
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
