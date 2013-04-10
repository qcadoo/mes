package com.qcadoo.mes.masterOrders.hooks;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.CUMULATED_ORDER_QUANTITY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.MASTER_ORDER_QUANTITY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields.PRODUCT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class MasterOrderProductDetailsHooksTest {

    private MasterOrderProductDetailsHooks masterOrderProductDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent cumulatedOrderQuantityUnitField, masterOrderQuantityUnitField;

    @Mock
    private LookupComponent productField;

    @Mock
    private Entity product, masterOrderProductEntity, masterOrder;

    @Before
    public void init() {
        masterOrderProductDetailsHooks = new MasterOrderProductDetailsHooks();
        MockitoAnnotations.initMocks(this);

        given(view.getComponentByReference(PRODUCT)).willReturn(productField);
        given(view.getComponentByReference("form")).willReturn(form);
        given(view.getComponentByReference("cumulatedOrderQuantityUnit")).willReturn(cumulatedOrderQuantityUnitField);
        given(view.getComponentByReference("masterOrderQuantityUnit")).willReturn(masterOrderQuantityUnitField);

        given(masterOrderProductEntity.getBelongsToField(MasterOrderProductFields.MASTER_ORDER)).willReturn(masterOrder);
    }

    @Test
    public final void shouldSetNullWhenProductDoesnotExists() {
        // given
        given(productField.getEntity()).willReturn(null);
        // when
        masterOrderProductDetailsHooks.fillUnitField(view);
        // then
        verify(cumulatedOrderQuantityUnitField).setFieldValue(null);
        verify(masterOrderQuantityUnitField).setFieldValue(null);
    }

    @Test
    public final void shouldSetUnitFromProduct() {
        // given
        String unit = "szt";
        given(productField.getEntity()).willReturn(product);
        given(product.getStringField(ProductFields.UNIT)).willReturn(unit);
        // when
        masterOrderProductDetailsHooks.fillUnitField(view);
        // then
        verify(cumulatedOrderQuantityUnitField).setFieldValue(unit);
        verify(masterOrderQuantityUnitField).setFieldValue(unit);
    }

    @Test
    public final void shouldShowMessageError() {
        // given
        BigDecimal cumulatedQuantity = BigDecimal.ONE;
        BigDecimal masterQuantity = BigDecimal.TEN;
        String masterOrderType = "03manyProducts";
        given(form.getEntity()).willReturn(masterOrderProductEntity);
        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn(masterOrderType);
        given(masterOrderProductEntity.getDecimalField(MASTER_ORDER_QUANTITY)).willReturn(masterQuantity);
        given(masterOrderProductEntity.getDecimalField(CUMULATED_ORDER_QUANTITY)).willReturn(cumulatedQuantity);
        // when
        masterOrderProductDetailsHooks.showErrorWhenCumulatedQuantity(view);
        // then
        verify(form).addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity", MessageType.INFO,
                false);
    }

    @Test
    public final void shouldDonotShowMessageError() {
        // given
        BigDecimal cumulatedQuantity = BigDecimal.TEN;
        BigDecimal masterQuantity = BigDecimal.ONE;
        String masterOrderType = "03manyProducts";
        given(form.getEntity()).willReturn(masterOrderProductEntity);
        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn(masterOrderType);
        given(masterOrderProductEntity.getDecimalField(MASTER_ORDER_QUANTITY)).willReturn(masterQuantity);
        given(masterOrderProductEntity.getDecimalField(CUMULATED_ORDER_QUANTITY)).willReturn(cumulatedQuantity);
        // when
        masterOrderProductDetailsHooks.showErrorWhenCumulatedQuantity(view);
        // then
        verify(form, Mockito.never()).addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity",
                MessageType.INFO, false);
    }
}
