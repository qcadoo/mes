package com.qcadoo.mes.orders.hooks;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

public class TechnologyInstOperCompDetailsHooksTest {

    private TechnologyOperCompHooksOrder technologyOperCompHooksorder;

    @Mock
    private ProductionLinesService productionLinesService;

    @Mock
    private Entity order, technology, techInstOperComp, productionLine;

    @Mock
    private EntityTreeNode root;

    @Mock
    private EntityTree tree;

    @Mock
    private DataDefinition dd, techInstDd;

    @Mock
    private DataDefinitionService ddService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        technologyOperCompHooksorder = new TechnologyOperCompHooksOrder();

        ReflectionTestUtils.setField(technologyOperCompHooksorder, "dataDefinitionService", ddService);
        ReflectionTestUtils.setField(technologyOperCompHooksorder, "productionLinesService", productionLinesService);
    }

    @Test
    public void shouldCreateANewTechnologyInstanceWhenCopying() {
        // given
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(technology.getTreeField("operationComponents")).willReturn(tree);
        given(tree.getRoot()).willReturn(root);
        given(ddService.get("technologies", "technologyInstanceOperationComponent")).willReturn(techInstDd);
        given(techInstDd.create()).willReturn(techInstOperComp);
        given(root.getStringField("entityType")).willReturn("operation");
        given(order.getBelongsToField("productionLine")).willReturn(productionLine);
        given(productionLinesService.getWorkstationTypesCount(root, productionLine)).willReturn(1);
        given(techInstDd.save(techInstOperComp)).willReturn(techInstOperComp);

        // when
        technologyOperCompHooksorder.createTechnologyInstanceFromScratch(dd, order);

        // then
        verify(order).setField("technologyInstanceOperationComponents", asList(techInstOperComp));
    }
}
