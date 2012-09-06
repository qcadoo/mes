package com.qcadoo.mes.techSubcontracting.hooks;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechInstOperCompHooksTSTest {

    private TechInstOperCompHooksTS techInstOperCompHooksTS;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, techOperComp;

    @Before
    public void init() {
        techInstOperCompHooksTS = new TechInstOperCompHooksTS();
        MockitoAnnotations.initMocks(this);
        when(entity.getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT)).thenReturn(techOperComp);
    }

    @Test
    public void shouldSetTrueFromLowerInstance() throws Exception {
        // given
        when(techOperComp.getBooleanField("isSubcontracting")).thenReturn(true);
        // when
        techInstOperCompHooksTS.copySubstractingFieldFromLowerInstance(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("isSubcontracting", true);
    }

    @Test
    public void shouldSetFalseFromLowerInstance() throws Exception {
        // given
        when(techOperComp.getBooleanField("isSubcontracting")).thenReturn(false);
        // when
        techInstOperCompHooksTS.copySubstractingFieldFromLowerInstance(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("isSubcontracting", false);
    }
}
