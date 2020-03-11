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

import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productFlowThruDivision.constants.Range;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qcadoo.model.api.search.SearchRestrictions.eq;

@Service
public class TechnologyHooksPFTD {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCreate(final DataDefinition technologyDD, final Entity technology) {
        if (technology.getField(TechnologyFieldsPFTD.RANGE) == null) {
            technology.setField(TechnologyFieldsPFTD.RANGE, Range.MANY_DIVISIONS.getStringValue());
        }
        fillProductionFlow(technologyDD, technology);
    }

    public void fillProductionFlow(final DataDefinition technologyDD, final Entity technology) {
        if (technology.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW) == null) {
            technology
                    .setField(TechnologyFieldsPFTD.PRODUCTION_FLOW, ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
        }
    }

    public void onSave(final DataDefinition technologyDD, final Entity technology) {
        cleanUpOnRangeChange(technologyDD, technology);
        cleanUpOnProductionRecordingTypeChangeToCumulated(technologyDD, technology);
        fillDivision(technologyDD, technology);
        fillProductionLine(technologyDD, technology);
    }

    private void cleanUpOnProductionRecordingTypeChangeToCumulated(DataDefinition technologyDD, Entity technology) {
        if (technology.getId() == null) {
            return;
        }
        Entity technologyDB = technologyDD.get(technology.getId());

        if (TypeOfProductionRecording.CUMULATED.getStringValue()
                .equals(technology.getStringField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING))
                && !technology.getStringField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING)
                        .equals(technologyDB.getStringField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
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

    private void cleanOperationProductProductionFlow(Entity op) {
        op.setField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW,
                ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
        op.setField(OperationProductInComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION, null);
        op.getDataDefinition().fastSave(op);
    }

    private void fillProductionLine(DataDefinition technologyDD, Entity technology) {
        if (technology.getId() != null) {
            if (technology.getField(TechnologyFieldsPFTD.RANGE).equals(Range.ONE_DIVISION.getStringValue())) {
                Entity technologyDB = technologyDD.get(technology.getId());
                Entity productionLineDb = technologyDB.getBelongsToField("productionLine");
                if (technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTION_LINE) == null) {
                    List<Entity> tocs = dataDefinitionService
                            .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                            .find().add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                            .list().getEntities();
                    for (Entity toc : tocs) {
                        if (!toc.getBooleanField("productionLineChange")) {
                            toc.setField("productionLine", null);
                            toc.getDataDefinition().save(toc);
                        }
                    }
                } else if (productionLineDb == null
                        || !technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTION_LINE).getId()
                                .equals(productionLineDb.getId())) {
                    List<Entity> tocs = dataDefinitionService
                            .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                            .find().add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                            .list().getEntities();
                    for (Entity toc : tocs) {
                        toc.setField("productionLine", technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTION_LINE));
                        toc.getDataDefinition().save(toc);
                    }

                }
            } else {
                technology.setField("productionLine", null);
            }
        }
    }

    private void fillDivision(DataDefinition technologyDD, Entity technology) {
        if (technology.getId() != null) {
            if (technology.getField(TechnologyFieldsPFTD.RANGE).equals(Range.ONE_DIVISION.getStringValue())) {
                List<Entity> tocs = dataDefinitionService
                        .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                        .find().add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                        .getEntities();
                for (Entity toc : tocs) {
                    toc.setField("division", technology.getBelongsToField(TechnologyFieldsPFTD.DIVISION));
                    toc.getDataDefinition().save(toc);
                }

            } else {
                technology.setField("division", null);
            }
        }
    }

    private void cleanUpOnRangeChange(final DataDefinition technologyDD, final Entity technology) {
        if (technology.getId() == null) {
            return;
        }
        Entity technologyDB = technologyDD.get(technology.getId());

        if (!technology.getStringField(TechnologyFieldsPFTD.RANGE)
                .equals(technologyDB.getStringField(TechnologyFieldsPFTD.RANGE))) {
            cleanLocations(technology);
            if (technology.getField(TechnologyFieldsPFTD.RANGE).equals(Range.MANY_DIVISIONS.getStringValue())) {
                technology.setField("componentsLocation", null);
                technology.setField("componentsOutputLocation", null);
                technology.setField("productsInputLocation", null);
                technology.setField("productionFlow", null);
                technology.setField("productsFlowLocation", null);
            }
        }
    }

    private void cleanLocations(Entity technology) {
        List<Entity> opocs = findOPOCs(technology.getId());
        for (Entity opoc : opocs) {
            cleanOperationProduct(opoc);
        }
        List<Entity> opics = findOPICs(technology.getId());
        for (Entity opic : opics) {
            cleanOperationProduct(opic);
        }
    }

    public List<Entity> findOPOCs(Long technologyId) {
        SearchCriteriaBuilder scb = getOpocDD().find();
        scb.createAlias(OperationProductOutComponentFields.OPERATION_COMPONENT, "toc", JoinType.INNER);
        scb.createAlias("toc." + TechnologyOperationComponentFields.TECHNOLOGY, "tech", JoinType.INNER);
        scb.add(eq("tech.id", technologyId));
        return scb.list().getEntities();
    }

    private DataDefinition getOpocDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
    }

    public List<Entity> findOPICs(Long technologyId) {
        SearchCriteriaBuilder scb = getOpicDD().find();
        scb.createAlias(OperationProductOutComponentFields.OPERATION_COMPONENT, "toc", JoinType.INNER);
        scb.createAlias("toc." + TechnologyOperationComponentFields.TECHNOLOGY, "tech", JoinType.INNER);
        scb.add(eq("tech.id", technologyId));
        return scb.list().getEntities();
    }

    private DataDefinition getOpicDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    private void cleanOperationProduct(Entity op) {
        op.setField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW, ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
        op.setField(OperationProductInComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION, null);
        op.setField(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION, null);
        op.setField(OperationProductInComponentFieldsPFTD.COMPONENTS_OUTPUT_LOCATION, null);
        op.setField(OperationProductInComponentFieldsPFTD.PRODUCTS_INPUT_LOCATION, null);
        op.getDataDefinition().fastSave(op);
    }

    public Entity getDivisionForOperation(final Entity toc) {
        return toc.getBelongsToField(TechnologyOperationComponentFields.DIVISION);
    }
}
