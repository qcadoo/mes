package com.qcadoo.mes.techSubcontracting.hooks;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechnologyOperationHooksTSTest {

    private TechnologyOperationHooksTS technologyOperationHooksTS;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, operation;

    @Before
    public void init() {
        technologyOperationHooksTS = new TechnologyOperationHooksTS();
        MockitoAnnotations.initMocks(this);
        when(entity.getBelongsToField(TechnologyOperationComponentFields.OPERATION)).thenReturn(operation);

    }

    @Test
    public void shouldSetTrueFromLowerInstance() throws Exception {
        // given
        when(operation.getBooleanField("isSubcontracting")).thenReturn(true);
        // when
        technologyOperationHooksTS.copySubstractingFieldFromLowerInstance(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("isSubcontracting", true);
    }

    @Test
    public void shouldSetFalseFromLowerInstance() throws Exception {
        // given
        when(operation.getBooleanField("isSubcontracting")).thenReturn(false);
        // when
        technologyOperationHooksTS.copySubstractingFieldFromLowerInstance(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("isSubcontracting", false);
    }
}
