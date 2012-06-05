package com.qcadoo.mes.materialFlow.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.MaterialFlowTransferService;
import com.qcadoo.mes.materialFlow.MaterialFlowTransferServiceImpl;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class MaterialFlowTransferServiceTest {

    private MaterialFlowTransferService materialFlowTransferService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private MaterialFlowService materialFlowService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity transfer, stockAreasFrom, stockAreasTo, product, staff;

    @Mock
    private Date time;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        materialFlowTransferService = new MaterialFlowTransferServiceImpl();

        ReflectionTestUtils.setField(materialFlowTransferService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(materialFlowTransferService, "materialFlowService", materialFlowService);
    }

    @Test
    public void shouldCreateAndSaveTransferEntity() {
        // given
        BigDecimal quantity = BigDecimal.valueOf(10);
        String type = "someType";
        String number = "abc123";

        given(dataDefinitionService.get("materialFlow", "transfer")).willReturn(dataDefinition);

        given(dataDefinition.create()).willReturn(transfer);
        given(dataDefinition.save(transfer)).willReturn(transfer);
        given(transfer.isValid()).willReturn(true);
        given(materialFlowService.generateNumberFromProduct(product, "transfer")).willReturn(number);

        // when
        materialFlowTransferService.createTransfer(type, stockAreasFrom, stockAreasTo, product, quantity, staff, time);

        // then
        verify(dataDefinition).create();
        verify(dataDefinition).save(transfer);

        verify(transfer).setField("number", number);
        verify(transfer).setField("type", type);
        verify(transfer).setField("product", product);
        verify(transfer).setField("stockAreasTo", stockAreasTo);
        verify(transfer).setField("stockAreasFrom", stockAreasFrom);
        verify(transfer).setField("time", time);
        verify(transfer).setField("staff", staff);
        verify(transfer).setField("quantity", quantity);
    }
}
