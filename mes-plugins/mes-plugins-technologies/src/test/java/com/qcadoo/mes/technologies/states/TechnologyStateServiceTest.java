/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
        when(technology.getStringField("state")).thenReturn("01draft", "02accepted");
        ArgumentCaptor<TechnologyState> fieldStateArgCaptor = ArgumentCaptor.forClass(TechnologyState.class);

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "02accepted" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue("01draft");
        Mockito.verify(stateFieldComponent, atLeastOnce()).setFieldValue(fieldStateArgCaptor.capture());
        Assert.assertEquals(TechnologyState.ACCEPTED.getStringValue(), fieldStateArgCaptor.getValue());

    }

    @Test
    public void shouldChangeStateFromDraftToDeclined() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("01draft", "03declined");
        ArgumentCaptor<TechnologyState> fieldStateArgCaptor = ArgumentCaptor.forClass(TechnologyState.class);

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "03declined" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue("01draft");
        Mockito.verify(stateFieldComponent, atLeastOnce()).setFieldValue(fieldStateArgCaptor.capture());
        Assert.assertEquals(TechnologyState.DECLINED.getStringValue(), fieldStateArgCaptor.getValue());
    }

    @Test
    public void shouldChangeStateFromAcceptedToOutdated() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("02accepted", "04outdated");
        ArgumentCaptor<TechnologyState> fieldStateArgCaptor = ArgumentCaptor.forClass(TechnologyState.class);

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "04outdated" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue("02accepted");
        Mockito.verify(stateFieldComponent, atLeastOnce()).setFieldValue(fieldStateArgCaptor.capture());
        Assert.assertEquals(TechnologyState.OUTDATED.getStringValue(), fieldStateArgCaptor.getValue());
    }

    @Test
    public void shouldNotChangeStateFromOutdatedToAccepted() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("04outdated");

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "02accepted" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldNotChangeStateFromAcceptedToDraft() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("02accepted");

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "01draft" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldNotChangeStateFromDeclinedToDraft() throws Exception {
        // given
        when(technology.getStringField("state")).thenReturn("03declined");

        // when
        technologiesStateService.changeTechnologyState(view, form, new String[] { "01draft" });

        // then
        Mockito.verify(stateFieldComponent, never()).setFieldValue(Mockito.anyString());
    }
}
