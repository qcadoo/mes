package com.qcadoo.mes.productionScheduling.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OperationalTaskHooksPS {

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private NormService normService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void setStaff(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Entity technologyOperationComponent = operationalTask
                .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

        int optimalStaff = getOptimalStaff(technologyOperationComponent);

        Integer actualStaff = operationalTask.getIntegerField(OperationalTaskFields.ACTUAL_STAFF);

        if (Objects.isNull(actualStaff)) {
            actualStaff = optimalStaff;
            operationalTask.setField(OperationalTaskFields.ACTUAL_STAFF, actualStaff);
        }

        List<Entity> workers = operationalTask.getManyToManyField(OperationalTaskFields.WORKERS);

        Entity staff = operationalTask.getBelongsToField(OperationalTaskFields.STAFF);
        Entity operationalTaskDB = null;

        if (Objects.nonNull(operationalTask.getId())) {
            operationalTaskDB = operationalTaskDD.get(operationalTask.getId());
        }

        if (workers.size() > 1 || workers.isEmpty() && Objects.nonNull(operationalTaskDB)
                && !operationalTaskDB.getManyToManyField(OperationalTaskFields.WORKERS).isEmpty()) {
            operationalTask.setField(OperationalTaskFields.STAFF, null);
        } else if (Objects.nonNull(staff) && workers.size() <= 1) {
            operationalTask.setField(OperationalTaskFields.WORKERS, Collections.singletonList(staff));
        } else if (Objects.isNull(staff) && workers.size() == 1) {
            if (Objects.nonNull(operationalTaskDB) && operationalTaskDB.getManyToManyField(OperationalTaskFields.WORKERS).size() != 1) {
                operationalTask.setField(OperationalTaskFields.STAFF, workers.get(0));
            } else {
                operationalTask.setField(OperationalTaskFields.WORKERS, Collections.emptyList());
            }
        }

        updateFinishDate(operationalTask, technologyOperationComponent, actualStaff, operationalTaskDB);

        if (actualStaff != operationalTask.getManyToManyField(OperationalTaskFields.WORKERS).size()) {
            operationalTask.addGlobalMessage(
                    "orders.operationalTask.error.workersQuantityDifferentThanActualStaff");
        }
    }

    private int getOptimalStaff(final Entity technologyOperationComponent) {
        if (!Objects.isNull(technologyOperationComponent)) {
            return technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF);
        } else {
            return 1;
        }
    }

    private void updateFinishDate(final Entity operationalTask, final Entity technologyOperationComponent, final Integer actualStaff, final Entity operationalTaskDB) {
        if (!Objects.isNull(technologyOperationComponent) && technologyOperationComponent
                .getBooleanField(TechnologyOperationComponentFieldsTNFO.TJ_DECREASES_FOR_ENLARGED_STAFF) &&
                (Objects.isNull(operationalTask.getId()) && !actualStaff.equals(technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF))
                        || Objects.nonNull(operationalTaskDB) && actualStaff != operationalTaskDB.getIntegerField(OperationalTaskFields.ACTUAL_STAFF).intValue())) {
            operationalTask.setField(OperationalTaskFields.FINISH_DATE,
                    getFinishDate(operationalTask, technologyOperationComponent));
        }
    }

    private Date getFinishDate(final Entity operationalTask, final Entity technologyOperationComponent) {
        Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
        Entity parameter = parameterService.getParameter();
        boolean includeTpz = parameter.getBooleanField("includeTpzSG");

        BigDecimal operationComponentRuns = basicProductionCountingService.getOperationComponentRuns(order, technologyOperationComponent);
        Optional<Entity> techOperCompWorkstationTime = normService.getTechOperCompWorkstationTime(technologyOperationComponent, workstation);
        BigDecimal staffFactor = normService.getStaffFactor(technologyOperationComponent, operationalTask.getIntegerField(OperationalTaskFields.ACTUAL_STAFF));

        Integer machineWorkTime;
        Integer additionalTime;

        if (techOperCompWorkstationTime.isPresent()) {
            OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTechOperationWorkTimeForWorkstation(
                    technologyOperationComponent, operationComponentRuns, includeTpz, false,
                    techOperCompWorkstationTime.get(), staffFactor);

            machineWorkTime = operationWorkTime.getMachineWorkTime();
            additionalTime = techOperCompWorkstationTime.get()
                    .getIntegerField(TechOperCompWorkstationTimeFields.TIME_NEXT_OPERATION);
        } else {
            OperationWorkTime operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(null,
                    technologyOperationComponent, operationComponentRuns, includeTpz, false,
                    false, staffFactor);

            machineWorkTime = operationWorkTime.getMachineWorkTime();
            additionalTime = technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION);
        }

        Entity productionLine = null;

        if (Objects.nonNull(workstation)) {
            productionLine = workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE);
        }

        Date finishDate = shiftsService.findDateToForProductionLine(startDate, machineWorkTime, productionLine);

        if (parameter.getBooleanField("includeAdditionalTimeSG")) {
            finishDate = Date.from(finishDate.toInstant().plusSeconds(additionalTime));
        }

        return finishDate;
    }
}
