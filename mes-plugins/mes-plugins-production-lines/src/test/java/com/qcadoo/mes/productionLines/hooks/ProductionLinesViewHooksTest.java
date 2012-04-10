package com.qcadoo.mes.productionLines.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class ProductionLinesViewHooksTest {

    private ProductionLinesViewHooks productionLinesViewHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState supportsAllTechnologies, groupsGrid, technologiesGrid;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionLinesViewHooks = new ProductionLinesViewHooks();

        given(view.getComponentByReference("supportsAllTechnologies")).willReturn(supportsAllTechnologies);
        given(view.getComponentByReference("technologies")).willReturn(technologiesGrid);
        given(view.getComponentByReference("groups")).willReturn(groupsGrid);
    }

    @Test
    public void shouldDisableBothGridsIfTheCheckboxSupportAllTechnoogiesIsSet() {
        // given
        given(supportsAllTechnologies.getFieldValue()).willReturn("1");

        // when
        productionLinesViewHooks.disableSupportedTechnologiesGrids(view, null, null);

        // then
        verify(technologiesGrid).setEnabled(false);
        verify(groupsGrid).setEnabled(false);
    }

    @Test
    public void shouldEnableBothGridsIfTheCheckboxSupportAllTechnoogiesIsntSet() {
        // given
        given(supportsAllTechnologies.getFieldValue()).willReturn("0");

        // when
        productionLinesViewHooks.disableSupportedTechnologiesGrids(view, null, null);

        // then
        verify(technologiesGrid).setEnabled(true);
        verify(groupsGrid).setEnabled(true);
    }
}
