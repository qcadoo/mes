/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.ChangeoverType.FOR_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.CHANGEOVER_TYPE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.NUMBER;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class LineChangeoverNormsDetailsHooks {

    private static final List<String> TECHNOLOGY_FIELDS = Arrays.asList(FROM_TECHNOLOGY, TO_TECHNOLOGY);

    private static final List<String> TECHNOLOGY_GROUP_FIELDS = Arrays.asList(FROM_TECHNOLOGY_GROUP, TO_TECHNOLOGY_GROUP);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        setFieldsVisibleAndRequired(view);
    }

    public void setLineChangeoverNormsName(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent name = (FieldComponent) view.getComponentByReference(NUMBER);
        FieldComponent changeoverType = (FieldComponent) view.getComponentByReference(CHANGEOVER_TYPE);
        boolean selectForTechnology = FOR_TECHNOLOGY.getStringValue().equals(changeoverType.getFieldValue());
        FieldComponent productionLine = (FieldComponent) view.getComponentByReference(PRODUCTION_LINE);
        Entity productionLineEntity = null;
        if (productionLine.getFieldValue() != null) {
            productionLineEntity = getProductionLineById((Long) productionLine.getFieldValue());
        }
        if (selectForTechnology) {
            FieldComponent fromTechnology = (FieldComponent) view.getComponentByReference(FROM_TECHNOLOGY);
            FieldComponent toTechnology = (FieldComponent) view.getComponentByReference(TO_TECHNOLOGY);
            if (fromTechnology.getFieldValue() != null && toTechnology.getFieldValue() != null) {
                Entity fromTechnologyEntity = getTechnologyById((Long) fromTechnology.getFieldValue());
                Entity toTechnologyEntity = getTechnologyById((Long) toTechnology.getFieldValue());
                if (fromTechnologyEntity != null && toTechnologyEntity != null) {
                    String fromTechnologyNumber = fromTechnologyEntity.getStringField(TechnologyFields.NUMBER);
                    String toTechnologyNumber = toTechnologyEntity.getStringField(TechnologyFields.NUMBER);
                    name.setFieldValue(makeDefaultName(fromTechnologyNumber, toTechnologyNumber, productionLineEntity));
                    name.requestComponentUpdateState();
                }
            }
        } else {
            FieldComponent fromTechnologyGroup = (FieldComponent) view.getComponentByReference(FROM_TECHNOLOGY_GROUP);
            FieldComponent toTechnologyGroup = (FieldComponent) view.getComponentByReference(TO_TECHNOLOGY_GROUP);
            if (fromTechnologyGroup.getFieldValue() != null && toTechnologyGroup.getFieldValue() != null) {
                Entity fromTechnologyGroupEntity = getTechnologyGroupById((Long) fromTechnologyGroup.getFieldValue());
                Entity toTechnologyGroupEntity = getTechnologyGroupById((Long) toTechnologyGroup.getFieldValue());
                if (fromTechnologyGroupEntity != null && toTechnologyGroupEntity != null) {
                    String fromTechnologyGroupNumber = fromTechnologyGroupEntity.getStringField(TechnologyFields.NUMBER);
                    String toTechnologyGroupNumber = toTechnologyGroupEntity.getStringField(TechnologyFields.NUMBER);
                    name.setFieldValue(makeDefaultName(fromTechnologyGroupNumber, toTechnologyGroupNumber, productionLineEntity));
                    name.requestComponentUpdateState();
                }
            }
        }
    }

    private String makeDefaultName(final String from, final String to, final Entity productionLine) {
        StringBuilder defaultNameBuilder = new StringBuilder();
        defaultNameBuilder.append("{");
        defaultNameBuilder.append(from);
        defaultNameBuilder.append("}");
        defaultNameBuilder.append("-");
        defaultNameBuilder.append("{");
        defaultNameBuilder.append(to);
        defaultNameBuilder.append("}");

        if (productionLine != null) {
            String productionLineNumber = productionLine.getStringField(TechnologyFields.NUMBER);
            defaultNameBuilder.append("-");
            defaultNameBuilder.append("{");
            defaultNameBuilder.append(productionLineNumber);
            defaultNameBuilder.append("}");
        }

        return defaultNameBuilder.toString();
    }

    private Entity getTechnologyById(final Long id) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(id);
    }

    private Entity getTechnologyGroupById(final Long id) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_GROUP)
                .get(id);
    }

    private Entity getProductionLineById(final Long id) {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get(id);
    }

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        setFieldsVisibleAndRequired(view);
    }

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view) {
        FieldComponent changeoverType = (FieldComponent) view.getComponentByReference(CHANGEOVER_TYPE);

        boolean selectForTechnology = FOR_TECHNOLOGY.getStringValue().equals(changeoverType.getFieldValue());

        changeFieldsState(view, TECHNOLOGY_FIELDS, selectForTechnology);
        changeFieldsState(view, TECHNOLOGY_GROUP_FIELDS, !selectForTechnology);
    }

    private void changeFieldsState(final ViewDefinitionState view, final List<String> fieldNames,
            final boolean selectForTechnology) {
        for (String fieldName : fieldNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(fieldName);
            field.setVisible(selectForTechnology);
            field.setRequired(selectForTechnology);

            if (!selectForTechnology) {
                field.setFieldValue(null);
            }

            field.requestComponentUpdateState();
        }
    }

}
