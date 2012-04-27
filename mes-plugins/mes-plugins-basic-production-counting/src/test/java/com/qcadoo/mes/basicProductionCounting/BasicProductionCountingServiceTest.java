package com.qcadoo.mes.basicProductionCounting;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class BasicProductionCountingServiceTest {

    private BasicProductionCountingService basicProductionCountingService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent productField;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    Entity basicPC, product;

    @Before
    public void init() {
        basicProductionCountingService = new BasicProductionCountingService();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(basicProductionCountingService, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldSetProductNameToFieldFromCounting() throws Exception {
        // given
        Long bpcId = 1L;
        String productName = "Product name";
        when(view.getComponentByReference("product")).thenReturn(productField);
        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(bpcId);
        when(dataDefinitionService.get("basicProductionCounting", "basicProductionCounting")).thenReturn(dataDefinition);
        when(dataDefinition.get(bpcId)).thenReturn(basicPC);
        when(basicPC.getBelongsToField("product")).thenReturn(product);
        when(product.getField("name")).thenReturn(productName);
        // when
        basicProductionCountingService.getProductNameFromCounting(view);
        // then
        Mockito.verify(productField).setFieldValue(productName);
    }
}
