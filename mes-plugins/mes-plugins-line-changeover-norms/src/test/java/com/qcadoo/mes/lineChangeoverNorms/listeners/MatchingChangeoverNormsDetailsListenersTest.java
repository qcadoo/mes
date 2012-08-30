/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.lineChangeoverNorms.listeners;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.CHANGEOVER_TYPE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.DURATION;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.NUMBER;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class MatchingChangeoverNormsDetailsListenersTest {

    private MatchingChangeoverNormsDetailsListeners listeners;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private DataDefinition technologyDataDefinition, productionLinesDataDefinition;

    @Mock
    private ChangeoverNormsService changeoverNormsService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private Entity fromTechnology, toTechnology, productionLine;

    @Mock
    private FieldComponent number, changeoverTypField, durationField;

    @Mock
    private LookupComponent fromTechLookup, toTechLookup, fromTechGpLookup, toTechGpLookup, productionLineLookup;

    @Mock
    private FormComponent form;

    @Mock
    private WindowComponentState window;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup matching;

    @Mock
    private RibbonActionItem edit;

    @Mock
    private ComponentState state;

    @Mock
    private LookupComponent fromTechnologyLookup, toTechnologyLookup, productionLinesLookup;

    private static final String MATCHING_FROM_TECHNOLOGY = "matchingFromTechnology";

    private static final String MATCHING_TO_TECHNOLOGY = "matchingToTechnology";

    private static final String MATCHING_PRODUCTION_LINE = "matchingProductionLine";

    @Before
    public void init() {
        listeners = new MatchingChangeoverNormsDetailsListeners();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(listeners, "changeoverNormsService", changeoverNormsService);

        Long toTechId = 1L;
        Long fromTechId = 1L;
        Long prodLinesId = 2L;

        when(view.getComponentByReference(MATCHING_FROM_TECHNOLOGY)).thenReturn(fromTechnologyLookup);
        when(fromTechnologyLookup.getFieldValue()).thenReturn(fromTechId);
        when(view.getComponentByReference(MATCHING_TO_TECHNOLOGY)).thenReturn(toTechnologyLookup);
        when(fromTechnologyLookup.getFieldValue()).thenReturn(toTechId);
        when(view.getComponentByReference(MATCHING_PRODUCTION_LINE)).thenReturn(productionLinesLookup);
        when(productionLinesLookup.getFieldValue()).thenReturn(prodLinesId);

        when(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY))
                .thenReturn(technologyDataDefinition);
        when(technologyDataDefinition.get(fromTechId)).thenReturn(fromTechnology);
        when(technologyDataDefinition.get(toTechId)).thenReturn(toTechnology);
        when(
                dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                        ProductionLinesConstants.MODEL_PRODUCTION_LINE)).thenReturn(productionLinesDataDefinition);
        when(productionLinesDataDefinition.get(prodLinesId)).thenReturn(productionLine);
    }

    @Test
    public void shouldClearFieldAndDisabledButtonWhenMatchingChangeoverNormWasnotFound() throws Exception {
        // given
        when(changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine)).thenReturn(null);
        when(view.getComponentByReference(NUMBER)).thenReturn(number);
        when(view.getComponentByReference(FROM_TECHNOLOGY)).thenReturn(fromTechLookup);
        when(view.getComponentByReference(TO_TECHNOLOGY)).thenReturn(toTechLookup);
        when(view.getComponentByReference(FROM_TECHNOLOGY_GROUP)).thenReturn(fromTechGpLookup);
        when(view.getComponentByReference(TO_TECHNOLOGY_GROUP)).thenReturn(toTechGpLookup);
        when(view.getComponentByReference(CHANGEOVER_TYPE)).thenReturn(changeoverTypField);
        when(view.getComponentByReference(DURATION)).thenReturn(durationField);
        when(view.getComponentByReference(PRODUCTION_LINE)).thenReturn(productionLineLookup);
        when(view.getComponentByReference("form")).thenReturn(form);
        when(view.getComponentByReference("window")).thenReturn((ComponentState) window);
        when(window.getRibbon()).thenReturn(ribbon);
        when(ribbon.getGroupByName("matching")).thenReturn(matching);
        when(matching.getItemByName("edit")).thenReturn(edit);

        // when
        listeners.matchingChangeoverNorm(view, state, null);

        // then
        Mockito.verify(fromTechLookup).setFieldValue(null);
        Mockito.verify(edit).setEnabled(false);
    }

}
