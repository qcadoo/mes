package com.qcadoo.mes.deliveries.hooks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class DeliveredProductDetailsHooksTest {

    private DeliveredProductDetailsHooks deliveredProductDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private LookupComponent productLookup;

    @Mock
    private Entity product;

    @Mock
    private FieldComponent deliveredUnitField, damagedUnitField, orderedUnitField, deliveredQuantityField;

    @Before
    public void init() {
        deliveredProductDetailsHooks = new DeliveredProductDetailsHooks();
        MockitoAnnotations.initMocks(this);

        when(view.getComponentByReference("product")).thenReturn(productLookup);
        when(view.getComponentByReference("deliveredQuantityUNIT")).thenReturn(deliveredUnitField);
        when(view.getComponentByReference("damagedQuantityUNIT")).thenReturn(damagedUnitField);
        when(view.getComponentByReference("orderedQuantityUNIT")).thenReturn(orderedUnitField);
        when(view.getComponentByReference("deliveredQuantity")).thenReturn(deliveredQuantityField);
        when(productLookup.getEntity()).thenReturn(product);
    }

    @Test
    public void shouldSetProductUnitWhenProductIsSelected() throws Exception {
        // given
        String unit = "szt";
        when(productLookup.getEntity()).thenReturn(product);
        when(product.getStringField("unit")).thenReturn(unit);
        // when
        deliveredProductDetailsHooks.fillUnitsFields(view);
        // then
        verify(deliveredUnitField).setFieldValue("szt");
        verify(damagedUnitField).setFieldValue("szt");
        verify(orderedUnitField).setFieldValue("szt");
    }

    @Test
    public void shouldReturnWhenProductIsNull() throws Exception {
        // given
        when(productLookup.getEntity()).thenReturn(null);
        // when
        deliveredProductDetailsHooks.fillUnitsFields(view);
        // then
        verify(deliveredUnitField).setFieldValue("");
        verify(damagedUnitField).setFieldValue("");
        verify(orderedUnitField).setFieldValue("");
    }

    @Test
    public void shouldSetRequiredOnDeliveredQuantityField() throws Exception {
        // when
        deliveredProductDetailsHooks.setDeliveredQuantityFieldRequired(view);
        // then
        verify(deliveredQuantityField).setRequired(true);
    }
}
