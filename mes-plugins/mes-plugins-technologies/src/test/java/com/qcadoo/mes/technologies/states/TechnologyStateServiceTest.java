package com.qcadoo.mes.technologies.states;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.logging.TechnologyLoggingService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Ignore
public class TechnologyStateServiceTest {

    private TechnologyStateService technologiesStateService;

    private ViewDefinitionState view;

    private FormComponent form;

    private Entity technology;

    private FieldComponent stateFieldComponent;

    @Before
    public void init() {
        technologiesStateService = new TechnologyStateService();

        TechnologyLoggingService technologyLoggingService = mock(TechnologyLoggingService.class);
        PluginAccessor pluginAccessor = mock(PluginAccessor.class);
        view = mock(ViewDefinitionState.class);
        form = mock(FormComponent.class);
        technology = mock(Entity.class);
        DataDefinition dataDefinition = mock(DataDefinition.class);
        stateFieldComponent = mock(FieldComponent.class);

        when(pluginAccessor.getPlugin(Mockito.anyString())).thenReturn(null);
        when(technology.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.get(Mockito.anyLong())).thenReturn(technology);
        when(view.getComponentByReference("state")).thenReturn(stateFieldComponent);
        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntity()).thenReturn(technology);

        setField(technologiesStateService, "pluginAccessor", pluginAccessor);
        setField(technologiesStateService, "technologyLoggingService", technologyLoggingService);
    }

    @Test
    public void shouldChangeStateFromDraftToAccepted() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("draft", "accepted");
        ArgumentCaptor<TechnologyState> fieldStateArgCaptor = ArgumentCaptor.forClass(TechnologyState.class);

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "accepted" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue("draft");
        Mockito.verify(stateFieldComponent, atLeastOnce()).setFieldValue(fieldStateArgCaptor.capture());
        Assert.assertEquals(TechnologyState.ACCEPTED.getStringValue(), fieldStateArgCaptor.getValue());

    }

    @Test
    public void shouldChangeStateFromDraftToDeclined() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("draft", "declined");
        ArgumentCaptor<TechnologyState> fieldStateArgCaptor = ArgumentCaptor.forClass(TechnologyState.class);

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "declined" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue("draft");
        Mockito.verify(stateFieldComponent, atLeastOnce()).setFieldValue(fieldStateArgCaptor.capture());
        Assert.assertEquals(TechnologyState.DECLINED.getStringValue(), fieldStateArgCaptor.getValue());
    }

    @Test
    public void shouldChangeStateFromAcceptedToOutdated() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("accepted", "outdated");
        ArgumentCaptor<TechnologyState> fieldStateArgCaptor = ArgumentCaptor.forClass(TechnologyState.class);

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "outdated" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue("accepted");
        Mockito.verify(stateFieldComponent, atLeastOnce()).setFieldValue(fieldStateArgCaptor.capture());
        Assert.assertEquals(TechnologyState.OUTDATED.getStringValue(), fieldStateArgCaptor.getValue());
    }

    @Test
    public void shouldNotChangeStateFromOutdatedToAccepted() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("outdated");

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "accepted" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldNotChangeStateFromAcceptedToDraft() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("accepted");

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "draft" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldNotChangeStateFromDeclinedToDraft() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("declined");

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "draft" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue(Mockito.anyString());
    }
}
