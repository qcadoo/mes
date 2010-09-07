package com.qcadoo.mes.core.data.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class DataAccessServiceDeleteTest extends DataAccessTest {

    @Test
    public void shouldProperlyDelete() throws Exception {
        // given
        SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        simpleDatabaseObject.setDeleted(false);

        given(session.get(SimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        dataAccessService.delete(dataDefinition, 1L);

        // then
        SimpleDatabaseObject simpleDatabaseObjectDeleted = new SimpleDatabaseObject();
        simpleDatabaseObjectDeleted.setId(1L);
        simpleDatabaseObjectDeleted.setName("Mr T");
        simpleDatabaseObjectDeleted.setAge(66);
        simpleDatabaseObjectDeleted.setDeleted(true);

        verify(session).update(simpleDatabaseObjectDeleted);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailIfEntityNotFound() throws Exception {
        // given
        given(session.get(SimpleDatabaseObject.class, 1L)).willReturn(null);

        // when
        dataAccessService.delete(dataDefinition, 1L);
    }
}
