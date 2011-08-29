package com.qcadoo.mes.costNormsForProduct;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class CostNormsForProductServiceTest {

    private ViewDefinitionState viewDefinitionState;

    private CostNormsForProductService costNormsForProductService;

    private DataDefinition dataDefinition;

    private FormComponent form;

    private Entity entity, product;

    @Before
    public void init() {
        costNormsForProductService = new CostNormsForProductService();
        viewDefinitionState = mock(ViewDefinitionState.class);
        dataDefinition = mock(DataDefinition.class);
        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        form = mock(FormComponent.class);

        when(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT))
                .thenReturn(dataDefinition);

        when(viewDefinitionState.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(3L);
        when(dataDefinition.get(anyLong())).thenReturn(entity);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldReturnExceptionWhenViewDefinitionStateIsNull() throws Exception {
        costNormsForProductService.fillInProductsGrid(null);
        costNormsForProductService.fillCostTabCurrency(null);
        costNormsForProductService.fillCostTabUnit(null);
    }

}
