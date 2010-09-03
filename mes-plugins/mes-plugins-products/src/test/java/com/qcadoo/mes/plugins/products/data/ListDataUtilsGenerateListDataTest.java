package com.qcadoo.mes.plugins.products.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Test;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.internal.search.SearchResultImpl;

public class ListDataUtilsGenerateListDataTest {

    @Test
    public void shouldReturnValidListData() {
        // given
        SearchResultImpl sr = new SearchResultImpl();
        Entity e1 = new Entity((long) 0);
        Entity e2 = new Entity((long) 1);
        Entity e3 = new Entity((long) 2);
        sr.setResults(Arrays.asList(new Entity[] { e1, e2, e3 }));
        sr.setTotalNumberOfEntities(10);

        GridDefinition gd = new GridDefinition(null, null);
        ColumnDefinition c1 = mock(ColumnDefinition.class);
        ColumnDefinition c2 = mock(ColumnDefinition.class);
        gd.setColumns(Arrays.asList(new ColumnDefinition[] { c1, c2 }));
        given(c1.getName()).willReturn("f1");
        given(c1.getValue(e1)).willReturn("fv10");
        given(c1.getValue(e2)).willReturn("fv11");
        given(c1.getValue(e3)).willReturn("fv12");
        given(c2.getName()).willReturn("f2");
        given(c2.getValue(e1)).willReturn("fv20");
        given(c2.getValue(e2)).willReturn("fv21");
        given(c2.getValue(e3)).willReturn("fv22");

        // when
        ListData listData = ListDataUtils.generateListData(sr, gd);

        // then
        assertEquals(10, listData.getTotalNumberOfEntities());
        assertEquals(3, listData.getEntities().size());
        for (int i = 0; i < 3; i++) {
            assertEquals(new Long(i), listData.getEntities().get(i).getId());
            assertEquals(2, listData.getEntities().get(i).getFields().size());
            assertEquals("fv1" + i, listData.getEntities().get(i).getField("f1"));
            assertEquals("fv2" + i, listData.getEntities().get(i).getField("f2"));
        }
    }
}
