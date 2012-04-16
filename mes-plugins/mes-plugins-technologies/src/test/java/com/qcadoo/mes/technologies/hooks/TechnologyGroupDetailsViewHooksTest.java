package com.qcadoo.mes.technologies.hooks;

import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class TechnologyGroupDetailsViewHooksTest {

    private TechnologyGroupDetailsViewHooks technologyGroupDetailsViewHooks;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent technologyGroupForm;

    @Mock
    private Entity technologyGroup;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyGroupDetailsViewHooks = new TechnologyGroupDetailsViewHooks();

        ReflectionTestUtils.setField(technologyGroupDetailsViewHooks, "dataDefinitionService", dataDefinitionService);

        given(view.getComponentByReference("form")).willReturn(technologyGroupForm);
        given(technologyGroupForm.getEntity()).willReturn(technologyGroup);
    }

    // TODO lupo fix problem with tests
    @Ignore
    @Test
    public void shouldAddTechnologyGroupToProductIfProductIsntNull() {
        // given

        // when
        technologyGroupDetailsViewHooks.addTechnologyGroupToProduct(view, null, null);

        // then
    }
}
