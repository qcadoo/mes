package com.qcadoo.mes.masterOrders.hooks;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

public class MasterOrderDetailsHooksTest {

    private MasterOrderDetailsHooks masterOrderDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent masterOrderTypeField, productField, technologyField, defaultTechnologyField, cumulatedQuantityField,
            masterOrderQuantityField;

    @Mock
    private GridComponent masterOrderProducts;

    @Mock
    private ComponentState borderLayoutProductQuantity;

    @Before
    public void init() {
        masterOrderDetailsHooks = new MasterOrderDetailsHooks();

        MockitoAnnotations.initMocks(this);

        when(view.getComponentByReference(MasterOrderFields.MASTER_ORDER_TYPE)).thenReturn(masterOrderTypeField);
        when(view.getComponentByReference(MasterOrderFields.PRODUCT)).thenReturn(productField);
        when(view.getComponentByReference(MasterOrderFields.TECHNOLOGY)).thenReturn(technologyField);
        when(view.getComponentByReference(MasterOrderFields.DEFAULT_TECHNOLOGY)).thenReturn(defaultTechnologyField);
        when(view.getComponentByReference(MasterOrderFields.CUMULATED_ORDER_QUANTITY)).thenReturn(cumulatedQuantityField);
        when(view.getComponentByReference(MasterOrderFields.MASTER_ORDER_QUANTITY)).thenReturn(masterOrderQuantityField);
        when(view.getComponentByReference("productsGrid")).thenReturn(masterOrderProducts);
        when(view.getComponentByReference("borderLayoutProductQuantity")).thenReturn(borderLayoutProductQuantity);
    }

    @Test
    public final void shouldInvisibleFieldWhenMasterOrderTypeValueIsEmty() {
        when(masterOrderTypeField.getFieldValue()).thenReturn(null);
        // when
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
        // then
        Mockito.verify(productField).setVisible(false);
        Mockito.verify(defaultTechnologyField).setVisible(false);
        Mockito.verify(cumulatedQuantityField).setVisible(false);
        Mockito.verify(technologyField).setVisible(false);
        Mockito.verify(masterOrderQuantityField).setVisible(false);
        Mockito.verify(borderLayoutProductQuantity).setVisible(false);
        Mockito.verify(masterOrderProducts).setVisible(false);
    }

    @Test
    public final void shouldInvisibleFieldWhenMasterOrderTypeIsManyProducts() {
        // given
        when(masterOrderTypeField.getFieldValue()).thenReturn(MasterOrderType.MANY_PRODUCTS.getStringValue());
        // when
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
        // then
        Mockito.verify(productField).setVisible(false);
        Mockito.verify(defaultTechnologyField).setVisible(false);
        Mockito.verify(cumulatedQuantityField).setVisible(false);
        Mockito.verify(technologyField).setVisible(false);
        Mockito.verify(masterOrderQuantityField).setVisible(false);
        Mockito.verify(borderLayoutProductQuantity).setVisible(false);
        Mockito.verify(masterOrderProducts).setVisible(true);
    }

    @Test
    public final void shouldInvisibleFieldWhenMasterOrderTypeIsUndefined() {
        // given
        when(masterOrderTypeField.getFieldValue()).thenReturn(MasterOrderType.UNDEFINED.getStringValue());
        // when
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
        // then
        Mockito.verify(productField).setVisible(false);
        Mockito.verify(defaultTechnologyField).setVisible(false);
        Mockito.verify(cumulatedQuantityField).setVisible(false);
        Mockito.verify(technologyField).setVisible(false);
        Mockito.verify(masterOrderQuantityField).setVisible(false);
        Mockito.verify(borderLayoutProductQuantity).setVisible(false);
        Mockito.verify(masterOrderProducts).setVisible(false);

    }

    @Test
    public final void shouldVisibleFieldWhenMasterOrderTypeIsOnProduct() {
        // given
        when(masterOrderTypeField.getFieldValue()).thenReturn(MasterOrderType.ONE_PRODUCT.getStringValue());
        // when
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
        // then
        Mockito.verify(productField).setVisible(true);
        Mockito.verify(defaultTechnologyField).setVisible(true);
        Mockito.verify(cumulatedQuantityField).setVisible(true);
        Mockito.verify(technologyField).setVisible(true);
        Mockito.verify(masterOrderQuantityField).setVisible(true);
        Mockito.verify(borderLayoutProductQuantity).setVisible(true);
        Mockito.verify(masterOrderProducts).setVisible(false);

    }
}
