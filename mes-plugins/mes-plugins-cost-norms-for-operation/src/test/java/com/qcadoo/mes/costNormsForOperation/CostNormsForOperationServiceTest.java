package com.qcadoo.mes.costNormsForOperation;

import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.FIELDS;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class CostNormsForOperationServiceTest {

    private ViewDefinitionState viewDefinitionState;

    private CostNormsForOperationService costNormsForOperationService;

    private DataDefinition dd;

    private FormComponent form;

    private Entity emptyEntity;

    private Entity entityWitchCosts;

    private FieldComponent fieldComponent;

    @Before
    public void init() {
        costNormsForOperationService = new CostNormsForOperationService();

        viewDefinitionState = mock(ViewDefinitionState.class);
        dd = mock(DataDefinition.class);
        form = mock(FormComponent.class);
        fieldComponent = mock(FieldComponent.class);
        DataDefinitionService ddService = mock(DataDefinitionService.class);
        emptyEntity = mock(Entity.class);
        entityWitchCosts = mock(Entity.class);

        when(ddService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT))
                .thenReturn(dd);
        when(
                ddService.get(ProductionSchedulingConstants.PLUGIN_IDENTIFIER,
                        ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT)).thenReturn(dd);

        when(viewDefinitionState.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(3L);
        when(dd.get(anyLong())).thenReturn(emptyEntity);
        // when(dd.save(emptyEntity)).thenReturn(entityWitchCosts);
        when(dd.save(entityWitchCosts)).thenReturn(entityWitchCosts);
        when(emptyEntity.getBelongsToField("operation")).thenReturn(entityWitchCosts);
        when(emptyEntity.getDataDefinition()).thenReturn(dd);
        when(fieldComponent.getFieldValue()).thenReturn("");

        for (String fieldName : FIELDS) {
            when(emptyEntity.getField(fieldName)).thenReturn(null);
            when(entityWitchCosts.getField(fieldName)).thenReturn(fieldName);
            // when(emptyEntity.getField(fieldName)).thenReturn(null);
            when(viewDefinitionState.getComponentByReference(fieldName)).thenReturn(fieldComponent);
        }

        setField(costNormsForOperationService, "dataDefinitionService", ddService);

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSourceEntityIsIncompatible() throws Exception {
        // given
        when(form.getName()).thenReturn("form");
        when(emptyEntity.getBelongsToField("operation")).thenReturn(null); // set source to null

        // when
        costNormsForOperationService.inheritOperationCostValues(viewDefinitionState);
    }

    @Test
    public void shouldDoNothingWhenFormComponentIsUnsupported() throws Exception {
        // given
        when(form.getName()).thenReturn("some incompatible form component");

        // when
        costNormsForOperationService.inheritOperationCostValues(viewDefinitionState);

        // then
        verify(emptyEntity, never()).setField(anyString(), anyObject());
    }

    @Test
    public void shouldInheritValuesFromOperationToTechnology() throws Exception {
        // given
        when(form.getName()).thenReturn("form");

        // when
        costNormsForOperationService.inheritOperationCostValues(viewDefinitionState);

        // then
        verify(emptyEntity, atLeastOnce()).setField(anyString(), anyObject());
        verify(entityWitchCosts, atLeastOnce()).getField(anyString());

    }
    /*
     * @Test public void shouldInheritValuesFromTechnologyToInstanceOfTechnologyInOrder() throws Exception { // given
     * when(form.getName()).thenReturn("orderOperationComponent");
     * when(emptyEntity.getBelongsToField("technologyOperationComponent")).thenReturn(entityWitchCosts); // when
     * costNormsForOperationService.inheritOperationCostValues(viewDefinitionState); // then verify(emptyEntity,
     * atLeastOnce()).setField(anyString(), anyObject()); verify(entityWitchCosts, atLeastOnce()).getField(anyString()); }
     * @Test public void shouldInheritValuesFromOperationToInstanceOfTechnologyInOrder() throws Exception { //given
     * when(form.getName()).thenReturn("orderOperationComponent");
     * when(emptyEntity.getBelongsToField("technologyOperationComponent")).thenReturn(emptyEntity);
     * when(emptyEntity.getBelongsToField("operation")).thenReturn(entityWitchCosts); //when
     * costNormsForOperationService.inheritOperationCostValues(viewDefinitionState); //then verify(emptyEntity,
     * atLeastOnce()).setField(anyString(), anyObject()); verify(entityWitchCosts, atLeastOnce()).getField(anyString());
     * verify(fieldComponent, atLeastOnce()).setFieldValue(anyObject()); }
     */
}
