package com.qcadoo.mes.costCalculation;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

public class CostCalculationModelValidatorsTest {

    private CostCalculationModelValidators costCalculationModelValidators;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity costCalculation, technology;

    @Mock
    private TechnologyService technologyService;

    private static EntityTree mockEntityTreeIterator(List<Entity> list) {
        EntityTree tree = mock(EntityTree.class);
        when(tree.isEmpty()).thenReturn(false);
        when(tree.iterator()).thenReturn(list.iterator());
        return tree;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        costCalculationModelValidators = new CostCalculationModelValidators();

        ReflectionTestUtils.setField(costCalculationModelValidators, "technologyService", technologyService);
    }

    @Test
    public void shouldNotAcceptNoTechnologyTree() {
        // given
        when(costCalculation.getBelongsToField("technology")).thenReturn(technology);
        Entity opComp = mock(Entity.class);
        EntityTree tree = mockEntityTreeIterator(asList(opComp));
        when(technology.getTreeField("operationComponents")).thenReturn(tree);
        when(technologyService.getProductCountForOperationComponent(opComp)).thenThrow(new IllegalStateException());

        // when
        boolean result = costCalculationModelValidators.checkIfTheTechnologyTreeIsntEmpty(dataDefinition, costCalculation);

        // then
        assertFalse(result);
    }
}
