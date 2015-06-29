package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventCriteriaModifiersCMP {

    @Autowired
    private ParameterService parameterService;


    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void selectFactory(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
    }

    public void selectDivision(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(MaintenanceEventFields.FACTORY)) {
            DataDefinition factoryDataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_FACTORY);
            scb.add(SearchRestrictions.belongsTo(DivisionFields.FACTORY, factoryDataDefinition,  filterValue.getLong(MaintenanceEventFields.FACTORY)));
        }
    }


    public void selectWorkstation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(MaintenanceEventFields.PRODUCTION_LINE)) {
            Long productionLineId = filterValue.getLong(MaintenanceEventFields.PRODUCTION_LINE);
            scb.createAlias(WorkstationFieldsPL.PRODUCTION_LINE, WorkstationFieldsPL.PRODUCTION_LINE, JoinType.INNER).add(
                    SearchRestrictions.eq(WorkstationFieldsPL.PRODUCTION_LINE + ".id", productionLineId));
        }
    }

    public void selectSubassembly(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(MaintenanceEventFields.WORKSTATION)) {
            DataDefinition workstationDataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION);
            scb.add(SearchRestrictions.belongsTo(SubassemblyFields.WORKSTATION, workstationDataDefinition,  filterValue.getLong(MaintenanceEventFields.WORKSTATION)));
        }
    }
}
