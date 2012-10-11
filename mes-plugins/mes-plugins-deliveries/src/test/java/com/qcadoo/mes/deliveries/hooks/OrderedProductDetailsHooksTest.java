package com.qcadoo.mes.deliveries.hooks;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class OrderedProductDetailsHooksTest {

    private OrderedProductDetailsHooks orderedProductDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private LookupComponent productLookup;

    @Mock
    private Entity product;

    @Mock
    private FieldComponent unitField;

    @Before
    public void init() {
        orderedProductDetailsHooks = new OrderedProductDetailsHooks();
        MockitoAnnotations.initMocks(this);

        when(view.getComponentByReference("product")).thenReturn(productLookup);
        when(view.getComponentByReference("orderedQuantityUNIT")).thenReturn(unitField);
        when(productLookup.getEntity()).thenReturn(product);
    }

    @Test
    public void shouldSetProductUnitWhenProductIsSelected() throws Exception {
        // given
        String unit = "szt";
        when(productLookup.getEntity()).thenReturn(product);
        when(product.getStringField("unit")).thenReturn(unit);
        // when
        orderedProductDetailsHooks.fillUnitsFields(view);
        // then
        Mockito.verify(unitField).setFieldValue("szt");
    }

    @Test
    public void shouldReturnWhenProductIsNull() throws Exception {
        // given
        when(productLookup.getEntity()).thenReturn(null);
        // when
        orderedProductDetailsHooks.fillUnitsFields(view);
        // then
        Mockito.verify(unitField).setFieldValue("");
    }
}
