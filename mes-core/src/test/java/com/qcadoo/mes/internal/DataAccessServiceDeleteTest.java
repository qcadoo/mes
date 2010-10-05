package com.qcadoo.mes.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;

public class DataAccessServiceDeleteTest extends DataAccessTest {

    @Test
    public void shouldProperlyDelete() throws Exception {
        // given
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        simpleDatabaseObject.setDeleted(false);

        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        dataDefinition.delete(1L);

        // then
        SampleSimpleDatabaseObject simpleDatabaseObjectDeleted = new SampleSimpleDatabaseObject();
        simpleDatabaseObjectDeleted.setId(1L);
        simpleDatabaseObjectDeleted.setName("Mr T");
        simpleDatabaseObjectDeleted.setAge(66);
        simpleDatabaseObjectDeleted.setDeleted(true);

        verify(session).update(simpleDatabaseObjectDeleted);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailIfEntityNotFound() throws Exception {
        // given
        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(null);

        // when
        dataDefinition.delete(1L);
    }
}
