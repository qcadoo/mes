package com.qcadoo.mes.productionLines;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class ProductionLinesServiceImplTest {

    private ProductionLinesServiceImpl productionLinesServiceImpl;

    @Mock
    private Entity opComp1, productionLine, workComp1, workComp2, work1, work2, work3, operation;

    private static EntityList mockEntityListIterator(final List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionLinesServiceImpl = new ProductionLinesServiceImpl();

        EntityList workstationComps = mockEntityListIterator(asList(workComp1, workComp2));
        given(productionLine.getHasManyField("workstationTypeComponents")).willReturn(workstationComps);
        given(workComp1.getBelongsToField("workstationType")).willReturn(work1);
        given(workComp2.getBelongsToField("workstationType")).willReturn(work2);
        given(work1.getId()).willReturn(1L);
        given(work2.getId()).willReturn(2L);
        given(work3.getId()).willReturn(3L);
        given(opComp1.getBelongsToField("operation")).willReturn(operation);
        given(workComp1.getField("quantity")).willReturn(123);
        given(workComp2.getField("quantity")).willReturn(234);
    }

    @Test
    public void shouldReturnCorrectWorkstationCount() {
        // given
        given(operation.getBelongsToField("workstationType")).willReturn(work2);

        // when
        Integer workstationCount = productionLinesServiceImpl.getWorkstationTypesCount(opComp1, productionLine);

        // then
        assertEquals(Integer.valueOf(234), workstationCount);
    }

    @Test
    public void shouldReturnSpecifiedQuantityIfNoWorkstationIsFound() {
        // given
        given(productionLine.getField("quantityForOtherWorkstationTypes")).willReturn(456);
        given(operation.getBelongsToField("workstationType")).willReturn(work3);

        // when
        Integer workstationCount = productionLinesServiceImpl.getWorkstationTypesCount(opComp1, productionLine);

        // then
        assertEquals(Integer.valueOf(456), workstationCount);
    }
}
