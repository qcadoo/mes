package com.qcadoo.mes.materialFlow.listeners;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.materialFlow.MaterialFlowResourceService;
import com.qcadoo.mes.materialFlow.MaterialFlowTransferService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.TimeConverterService;

public class MultitransferListenersTest {

    private MultitransferListeners multitransferListeners;

    @Mock
    private MaterialFlowTransferService materialFlowTransferService;

    @Mock
    private MaterialFlowResourceService materialFlowResourceService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private TimeConverterService timeConverterService;

    @Mock
    private DataDefinition staffDD, areasDD;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState state;

    @Mock
    private AwesomeDynamicListComponent adlc;

    @Mock
    private FormComponent formComponent;

    @Mock
    private Entity productQuantity, product, staff, stockAreasFrom, stockAreasTo;

    @Mock
    private FieldComponent timeComp, stockAreasFromComp, stockAreasToComp, typeComp, staffComp;

    @Mock
    private FormComponent form;

    @Mock
    private Date time;

    @Mock
    private Entity template;

    private String unit = "szt";

    private String type = "type";

    @Mock
    private DataDefinition productQuantityDD;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        multitransferListeners = new MultitransferListeners();

        ReflectionTestUtils.setField(multitransferListeners, "materialFlowTransferService", materialFlowTransferService);
        ReflectionTestUtils.setField(multitransferListeners, "materialFlowResourceService", materialFlowResourceService);
        ReflectionTestUtils.setField(multitransferListeners, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(multitransferListeners, "timeConverterService", timeConverterService);

        given(view.getComponentByReference("form")).willReturn(form);

        given(view.getComponentByReference("products")).willReturn(adlc);
        given(view.getComponentByReference("time")).willReturn(timeComp);
        given(view.getComponentByReference("stockAreasTo")).willReturn(stockAreasToComp);
        given(view.getComponentByReference("stockAreasFrom")).willReturn(stockAreasFromComp);
        given(view.getComponentByReference("type")).willReturn(typeComp);
        given(view.getComponentByReference("staff")).willReturn(staffComp);

        List<FormComponent> formComponents = Arrays.asList(formComponent);
        given(adlc.getFormComponents()).willReturn(formComponents);

        given(formComponent.getEntity()).willReturn(productQuantity);
        given(productQuantity.getBelongsToField("product")).willReturn(product);
        given(productQuantity.getDecimalField("quantity")).willReturn(BigDecimal.TEN);
        given(product.getStringField("unit")).willReturn(unit);

        given(typeComp.getFieldValue()).willReturn(type);
        given(staffComp.getFieldValue()).willReturn(1L);

        given(dataDefinitionService.get("basic", "staff")).willReturn(staffDD);
        given(staffDD.get(1L)).willReturn(staff);

        Long dateFieldValue = 123L;

        given(timeComp.getFieldValue()).willReturn(dateFieldValue);
        given(timeConverterService.getDateFromField(dateFieldValue)).willReturn(time);

        given(stockAreasFromComp.getFieldValue()).willReturn(0L);
        given(stockAreasToComp.getFieldValue()).willReturn(1L);

        given(dataDefinitionService.get("materialFlow", "stockAreas")).willReturn(areasDD);
        given(areasDD.get(0L)).willReturn(stockAreasFrom);
        given(areasDD.get(1L)).willReturn(stockAreasTo);

        given(materialFlowResourceService.areResourcesSufficient(stockAreasFrom, product, BigDecimal.TEN)).willReturn(true);
    }

    @Test
    public void shouldFillUnitsInADL() {
        // given

        // when
        multitransferListeners.fillUnitsInADL(view, state, null);

        // then
        verify(productQuantity).setField("unit", unit);
        verify(formComponent).setEntity(productQuantity);
    }

    @Test
    public void shouldCreateMultitransfer() {
        // given

        // when
        multitransferListeners.createMultitransfer(view, state, null);

        // then
        verify(materialFlowTransferService).createTransfer(type, stockAreasFrom, stockAreasTo, product, BigDecimal.TEN, staff,
                time);
        verify(form).addMessage("materialFlow.multitransfer.generate.success", MessageType.SUCCESS);
    }

    @Test
    public void shouldDownloadProductsFromTemplates() {
        // given
        List<Entity> templates = Arrays.asList(template);

        given(template.getBelongsToField("product")).willReturn(product);
        given(dataDefinitionService.get("materialFlow", "productQuantity")).willReturn(productQuantityDD);
        given(materialFlowTransferService.getTransferTemplates(stockAreasFrom, stockAreasTo)).willReturn(templates);
        given(productQuantityDD.create()).willReturn(productQuantity);

        // when
        multitransferListeners.getFromTemplate(view, state, null);

        // then
        verify(productQuantity).setField("product", product);
        verify(adlc).setFieldValue(Arrays.asList(productQuantity));
        verify(form).addMessage("materialFlow.multitransfer.template.success", MessageType.SUCCESS);
    }
}
