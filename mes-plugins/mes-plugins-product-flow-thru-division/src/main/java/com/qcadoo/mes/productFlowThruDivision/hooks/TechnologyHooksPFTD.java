/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.productFlowThruDivision.constants.*;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.qcadoo.model.api.search.SearchRestrictions.eq;

@Service
public class TechnologyHooksPFTD {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCreate(final DataDefinition technologyDD, final Entity technology) {
        fillProductionFlow(technologyDD, technology);
    }

    public void fillProductionFlow(final DataDefinition technologyDD, final Entity technology) {
        if (Objects.isNull(technology.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW))) {
            technology.setField(TechnologyFieldsPFTD.PRODUCTION_FLOW,
                    ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
        }
    }

    public void onSave(final DataDefinition technologyDD, final Entity technology) {
        cleanUpOnRangeChange(technologyDD, technology);
        cleanUpOnProductionRecordingTypeChange(technologyDD, technology);
    }

    private void cleanUpOnProductionRecordingTypeChange(final DataDefinition technologyDD, final Entity technology) {
        if (Objects.isNull(technology.getId())) {
            return;
        }

        Entity technologyDB = technologyDD.get(technology.getId());

        String typeOfProductionRecording = technology.getStringField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        String typeOfProductionRecordingDB = technologyDB.getStringField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        if (Objects.isNull(typeOfProductionRecordingDB) && Objects.nonNull(typeOfProductionRecording)
                || Objects.nonNull(typeOfProductionRecordingDB) && Objects.isNull(typeOfProductionRecording)
                || Objects.nonNull(typeOfProductionRecording) && !typeOfProductionRecording.equals(typeOfProductionRecordingDB)) {
            List<Entity> tocs = getTechnologyOperationComponents(technology);

            clearWorkstations(tocs);
        }

        if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(typeOfProductionRecording)
                && !typeOfProductionRecording.equals(typeOfProductionRecordingDB)) {
            List<Entity> opocs = findOPOCs(technology.getId());

            for (Entity opoc : opocs) {
                cleanOperationProductProductionFlow(opoc);
            }

            List<Entity> opics = findOPICs(technology.getId());

            for (Entity opic : opics) {
                cleanOperationProductProductionFlow(opic);
            }
        }
    }

    private void clearWorkstations(List<Entity> tocs) {
        for (Entity toc : tocs) {
            toc.setField(TechnologyOperationComponentFields.WORKSTATIONS, null);
            toc.getDataDefinition().save(toc);
        }
    }

    private void cleanOperationProductProductionFlow(final Entity operationProduct) {
        operationProduct.setField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW,
                ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
        operationProduct.setField(OperationProductInComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION, null);

        operationProduct.getDataDefinition().fastSave(operationProduct);
    }

    private void cleanUpOnRangeChange(final DataDefinition technologyDD, final Entity technology) {
        if (Objects.isNull(technology.getId())) {
            return;
        }

        Entity technologyDB = technologyDD.get(technology.getId());

        if (!technology.getStringField(TechnologyFields.RANGE)
                .equals(technologyDB.getStringField(TechnologyFields.RANGE))) {
            cleanLocations(technology);

            if (technology.getField(TechnologyFields.RANGE).equals(Range.MANY_DIVISIONS.getStringValue())) {
                technology.setField(TechnologyFieldsPFTD.COMPONENTS_LOCATION, null);
                technology.setField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION, null);
                technology.setField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION, null);
                technology.setField(TechnologyFieldsPFTD.PRODUCTION_FLOW, null);
                technology.setField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION, null);
                technology.setField(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE, null);
            }
        }
    }

    private void cleanLocations(final Entity technology) {
        List<Entity> opocs = findOPOCs(technology.getId());

        for (Entity opoc : opocs) {
            cleanOperationProductOut(opoc);
        }

        List<Entity> opics = findOPICs(technology.getId());

        for (Entity opic : opics) {
            cleanOperationProductIn(opic);
        }
    }

    public List<Entity> findOPOCs(final Long technologyId) {
        SearchCriteriaBuilder scb = getOpocDD().find();

        scb.createAlias(OperationProductOutComponentFields.OPERATION_COMPONENT, "toc", JoinType.INNER);
        scb.createAlias("toc." + TechnologyOperationComponentFields.TECHNOLOGY, "tech", JoinType.INNER);

        scb.add(eq("tech.id", technologyId));

        return scb.list().getEntities();
    }

    public List<Entity> findOPICs(final Long technologyId) {
        SearchCriteriaBuilder scb = getOpicDD().find();

        scb.createAlias(OperationProductOutComponentFields.OPERATION_COMPONENT, "toc", JoinType.INNER);
        scb.createAlias("toc." + TechnologyOperationComponentFields.TECHNOLOGY, "tech", JoinType.INNER);

        scb.add(eq("tech.id", technologyId));

        return scb.list().getEntities();
    }

    private void cleanOperationProductIn(final Entity operationProduct) {
        operationProduct.setField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW,
                ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
        operationProduct.setField(OperationProductInComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION, null);
        operationProduct.setField(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION, null);
        operationProduct.setField(OperationProductInComponentFieldsPFTD.COMPONENTS_OUTPUT_LOCATION, null);
        operationProduct.setField(OperationProductInComponentFieldsPFTD.PRODUCTS_INPUT_LOCATION, null);

        operationProduct.getDataDefinition().fastSave(operationProduct);
    }

    private void cleanOperationProductOut(final Entity operationProduct) {
        operationProduct.setField(OperationProductOutComponentFieldsPFTD.PRODUCTION_FLOW,
                ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
        operationProduct.setField(OperationProductOutComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION, null);
        operationProduct.setField(OperationProductOutComponentFieldsPFTD.COMPONENTS_LOCATION, null);
        operationProduct.setField(OperationProductOutComponentFieldsPFTD.COMPONENTS_OUTPUT_LOCATION, null);
        operationProduct.setField(OperationProductOutComponentFieldsPFTD.PRODUCTS_INPUT_LOCATION, null);
        operationProduct.setField(OperationProductOutComponentFieldsPFTD.WASTE_RECEPTION_WAREHOUSE, null);

        operationProduct.getDataDefinition().fastSave(operationProduct);
    }

    public Entity getDivisionForOperation(final Entity technologyOperationComponent) {
        return technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.DIVISION);
    }

    private List<Entity> getTechnologyOperationComponents(final Entity technology) {
        return getTechnologyOperationComponentDD().find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

    private DataDefinition getOpicDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    private DataDefinition getOpocDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
    }

}
