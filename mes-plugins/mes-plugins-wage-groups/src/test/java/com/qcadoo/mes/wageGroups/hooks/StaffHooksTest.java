package com.qcadoo.mes.wageGroups.hooks;

import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.DETERMINED_INDIVIDUAL;
import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.INDIVIDUAL_LABOR_COST;
import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.WAGE_GROUP;
import static com.qcadoo.mes.wageGroups.constants.WageGroupFields.LABOR_HOURLY_COST;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class StaffHooksTest {

    private StaffHooks hooks;

    @Mock
    private Entity entity, wageGroup;

    @Mock
    private DataDefinition dataDefinition;

    @Before
    public void init() {
        hooks = new StaffHooks();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveIndividualCost() throws Exception {
        // given
        when(entity.getBooleanField(DETERMINED_INDIVIDUAL)).thenReturn(true);
        when(entity.getField(INDIVIDUAL_LABOR_COST)).thenReturn(BigDecimal.ONE);
        // when
        hooks.saveLaborHourlyCost(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("laborHourlyCost", BigDecimal.ONE);
    }

    @Test
    public void shouldReturnWhenWageDoesnotExists() throws Exception {
        // given
        when(entity.getBooleanField(DETERMINED_INDIVIDUAL)).thenReturn(false);
        when(entity.getBelongsToField(WAGE_GROUP)).thenReturn(null);
        // when
        hooks.saveLaborHourlyCost(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("laborHourlyCost", null);
        // then
    }

    @Test
    public void shouldSaveCostFromWageGroup() throws Exception {
        // given
        when(entity.getBooleanField(DETERMINED_INDIVIDUAL)).thenReturn(false);
        when(entity.getBelongsToField(WAGE_GROUP)).thenReturn(wageGroup);
        when(wageGroup.getField(LABOR_HOURLY_COST)).thenReturn(BigDecimal.TEN);
        // when
        hooks.saveLaborHourlyCost(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("laborHourlyCost", BigDecimal.TEN);
        // then
    }
}
