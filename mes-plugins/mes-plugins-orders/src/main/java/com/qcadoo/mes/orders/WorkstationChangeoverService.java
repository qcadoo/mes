package com.qcadoo.mes.orders;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormChangeoverType;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class WorkstationChangeoverService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> findWorkstationChangeoverForOperationalTasks(final Entity operationalTask) {
        List<Entity> workstationChangeoverForOperationalTasks = Lists.newArrayList();

        return workstationChangeoverForOperationalTasks;
    }

    public List<Entity> findWorkstationChangeoverNorms(final Entity workstation, final Entity attribute) {
        return getWorkstationChangeoverNormDD().find()
                .add(SearchRestrictions.belongsTo(WorkstationChangeoverNormFields.WORKSTATION, workstation))
                .add(SearchRestrictions.belongsTo(WorkstationChangeoverNormFields.ATTRIBUTE, attribute))
                .add(SearchRestrictions.eq(WorkstationChangeoverNormFields.CHANGEOVER_TYPE, WorkstationChangeoverNormChangeoverType.ANY_CHANGE.getStringValue()))
                .list().getEntities();
    }

    public Optional<Entity> getOperationalTask(final String number) {
        return Optional.ofNullable(getOperationalTaskDD().find().add(SearchRestrictions.eq(OperationalTaskFields.NUMBER, number))
                .add(SearchRestrictions.not(SearchRestrictions.in(OperationalTaskFields.STATE, com.beust.jcommander.internal.Lists.newArrayList(OperationalTaskStateStringValues.FINISHED, OperationalTaskStateStringValues.REJECTED))))
                .setMaxResults(1).uniqueResult());
    }

    public Optional<Entity> findPreviousOperationalTask(final Entity operationalTask) {
        if (Objects.nonNull(operationalTask)) {
            Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
            Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);

            return Optional.ofNullable(getOperationalTaskDD().find()
                    .createAlias(OperationalTaskFields.WORKSTATION, OperationalTaskFields.WORKSTATION, JoinType.LEFT)
                    .add(SearchRestrictions.eq(OperationalTaskFields.WORKSTATION + ".id", workstation.getId()))
                    .add(SearchRestrictions.lt(OperationalTaskFields.FINISH_DATE, startDate))
                    .addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE)).setMaxResults(1).uniqueResult());
        } else {
            return Optional.empty();
        }
    }

    private DataDefinition getOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK);
    }

    private DataDefinition getWorkstationChangeoverForOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASK);
    }

    private DataDefinition getWorkstationChangeoverNormDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_WORKSTATION_CHANGEOVER_NORM);
    }

}
