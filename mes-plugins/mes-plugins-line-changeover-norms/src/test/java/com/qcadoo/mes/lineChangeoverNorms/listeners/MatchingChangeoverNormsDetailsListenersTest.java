package com.qcadoo.mes.lineChangeoverNorms.listeners;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
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
    private FieldComponent number, fromTechField, toTechField, fromTechGpField, toTechGpField, changeoverTypField, durationField,
            productionLineField;

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
    private ComponentState state, fromTechnologyLookup, toTechnologyLookup, productionLinesLookup;

    private static final String MATCHING_FROM_TECHNOLOGY = "matchingFromTechnology";

    private static final String MATCHING_TO_TECHNOLOGY = "matchingToTechnology";

    private static final String MATCHING_PRODUCTION_LINE = "matchingProductionLine";

    @Before
    public void init() {
        listeners = new MatchingChangeoverNormsDetailsListeners();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(listeners, "dataDefinitionService", dataDefinitionService);
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
        when(view.getComponentByReference(LineChangeoverNormsFields.NUMBER)).thenReturn(number);
        when(view.getComponentByReference(LineChangeoverNormsFields.FROM_TECHNOLOGY)).thenReturn(fromTechField);
        when(view.getComponentByReference(LineChangeoverNormsFields.TO_TECHNOLOGY)).thenReturn(toTechField);
        when(view.getComponentByReference(LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP)).thenReturn(fromTechGpField);
        when(view.getComponentByReference(LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP)).thenReturn(toTechGpField);
        when(view.getComponentByReference(LineChangeoverNormsFields.CHANGEOVER_TYPE)).thenReturn(changeoverTypField);
        when(view.getComponentByReference(LineChangeoverNormsFields.DURATION)).thenReturn(durationField);
        when(view.getComponentByReference(LineChangeoverNormsFields.PRODUCTION_LINE)).thenReturn(productionLineField);
        when(view.getComponentByReference("form")).thenReturn(form);
        when(view.getComponentByReference("window")).thenReturn((ComponentState) window);
        when(window.getRibbon()).thenReturn(ribbon);
        when(ribbon.getGroupByName("matching")).thenReturn(matching);
        when(matching.getItemByName("edit")).thenReturn(edit);

        // when
        listeners.matchingChangeoverNorm(view, state, null);

        // then
        Mockito.verify(fromTechField).setFieldValue(null);
        Mockito.verify(edit).setEnabled(false);
    }

}
