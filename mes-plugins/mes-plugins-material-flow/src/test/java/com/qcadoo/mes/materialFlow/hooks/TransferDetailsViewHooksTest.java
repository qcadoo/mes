package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.TransferFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class TransferDetailsViewHooksTest {

    private TransferDetailsViewHooks transferDetailsViewHooks;

    private static final String L_NUMBER = "000001";

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent numberField, typeField, timeField, locationFromField, locationToField, staffField;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition transferDD;

    @Mock
    private Entity transfer, transformations;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        transferDetailsViewHooks = new TransferDetailsViewHooks();

        setField(transferDetailsViewHooks, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldReturnWhenCheckIfTransferHasTransformationsAndNumberIsNull() {
        // given
        given(view.getComponentByReference(NUMBER)).willReturn(numberField);
        given(view.getComponentByReference(TYPE)).willReturn(typeField);
        given(view.getComponentByReference(TIME)).willReturn(typeField);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromField);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToField);
        given(view.getComponentByReference(STAFF)).willReturn(staffField);

        given(view.getComponentByReference(TransferFields.NUMBER)).willReturn(numberField);

        given(numberField.getFieldValue()).willReturn(null);

        // when
        transferDetailsViewHooks.checkIfTransferHasTransformations(view);

        // then
        verify(typeField, never()).setEnabled(false);
        verify(timeField, never()).setEnabled(false);
        verify(locationFromField, never()).setEnabled(false);
        verify(locationToField, never()).setEnabled(false);
        verify(staffField, never()).setEnabled(false);

    }

    @Test
    public void shouldReturnWhenCheckIfTransferHasTransformationsAndTransferIsNull() {
        // given
        given(view.getComponentByReference(NUMBER)).willReturn(numberField);
        given(view.getComponentByReference(TYPE)).willReturn(typeField);
        given(view.getComponentByReference(TIME)).willReturn(timeField);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromField);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToField);
        given(view.getComponentByReference(STAFF)).willReturn(staffField);

        given(view.getComponentByReference(TransferFields.NUMBER)).willReturn(numberField);

        given(numberField.getFieldValue()).willReturn(L_NUMBER);

        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER))
                .willReturn(transferDD);
        given(transferDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(null);

        // when
        transferDetailsViewHooks.checkIfTransferHasTransformations(view);

        // then
        verify(typeField, never()).setEnabled(false);
        verify(timeField, never()).setEnabled(false);
        verify(locationFromField, never()).setEnabled(false);
        verify(locationToField, never()).setEnabled(false);
        verify(staffField, never()).setEnabled(false);

    }

    @Test
    public void shouldReturnWhenCheckIfTransferHasTransformationsAndTransformationsAreNull() {
        // given
        given(view.getComponentByReference(NUMBER)).willReturn(numberField);
        given(view.getComponentByReference(TYPE)).willReturn(typeField);
        given(view.getComponentByReference(TIME)).willReturn(timeField);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromField);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToField);
        given(view.getComponentByReference(STAFF)).willReturn(staffField);

        given(view.getComponentByReference(TransferFields.NUMBER)).willReturn(numberField);

        given(numberField.getFieldValue()).willReturn(L_NUMBER);

        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER))
                .willReturn(transferDD);
        given(transferDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(transfer);

        given(transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION)).willReturn(null);
        given(transfer.getBelongsToField(TRANSFORMATIONS_PRODUCTION)).willReturn(null);

        // when
        transferDetailsViewHooks.checkIfTransferHasTransformations(view);

        // then
        verify(typeField, never()).setEnabled(false);
        verify(timeField, never()).setEnabled(false);
        verify(locationFromField, never()).setEnabled(false);
        verify(locationToField, never()).setEnabled(false);
        verify(staffField, never()).setEnabled(false);

    }

    @Test
    public void shouldDisableFieldsWhenCheckIfTransferHasTransformationsAndTransformationsAreNull() {
        // given
        given(view.getComponentByReference(NUMBER)).willReturn(numberField);
        given(view.getComponentByReference(TYPE)).willReturn(typeField);
        given(view.getComponentByReference(TIME)).willReturn(timeField);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromField);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToField);
        given(view.getComponentByReference(STAFF)).willReturn(staffField);

        given(view.getComponentByReference(TransferFields.NUMBER)).willReturn(numberField);

        given(numberField.getFieldValue()).willReturn(L_NUMBER);

        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER))
                .willReturn(transferDD);
        given(transferDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(transfer);

        given(transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION)).willReturn(transformations);
        given(transfer.getBelongsToField(TRANSFORMATIONS_PRODUCTION)).willReturn(transformations);

        // when
        transferDetailsViewHooks.checkIfTransferHasTransformations(view);

        // then
        verify(typeField).setEnabled(false);
        verify(timeField).setEnabled(false);
        verify(locationFromField).setEnabled(false);
        verify(locationToField).setEnabled(false);
        verify(staffField).setEnabled(false);

    }

}
