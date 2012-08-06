package com.qcadoo.mes.assignmentToShift.hooks;

import static org.mockito.BDDMockito.given;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class StaffAssignmentToShiftHooksTest {

    private StaffAssignmentToShiftHooks hooks;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, dictionary, productionLine;

    @Mock
    private StaffAssignmentToShiftDetailsHooks staffAssignmentToShiftDetailsHooks;

    @Before
    public void init() {
        hooks = new StaffAssignmentToShiftHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(hooks, "assignmentToShiftDetailsHooks", staffAssignmentToShiftDetailsHooks);
    }

    @Test
    public void shouldSaveOccupationTypeForGridValueWhenProductionLineIsSelected() throws Exception {
        // given
        String technicalCode = "01workOnLine";
        String occupationType = "Praca na linii";
        String productionLineNumber = "00001";
        String occupationTypeForGridValue = "info";

        given(entity.getStringField("occupationType")).willReturn(occupationType);
        given(staffAssignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType)).willReturn(dictionary);
        given(dictionary.getStringField("technicalCode")).willReturn(technicalCode);
        given(entity.getBelongsToField("productionLine")).willReturn(productionLine);
        given(productionLine.getStringField("number")).willReturn(productionLineNumber);

        // when
        hooks.setOccupationTypeForGridValue(dataDefinition, entity);

        // then
        Assert.assertEquals("info", occupationTypeForGridValue);
    }

    @Test
    public void shouldAddErrorForEntityWhenProductionLineIsNull() throws Exception {
        // given
        String technicalCode = "01workOnLine";
        String occupationType = "Praca na linii";

        given(entity.getStringField("occupationType")).willReturn(occupationType);
        given(staffAssignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType)).willReturn(dictionary);
        given(dictionary.getStringField("technicalCode")).willReturn(technicalCode);
        given(entity.getBelongsToField("productionLine")).willReturn(null);

        // when
        hooks.setOccupationTypeForGridValue(dataDefinition, entity);

        // then
        Mockito.verify(entity).addError(dataDefinition.getField(StaffAssignmentToShiftFields.PRODUCTION_LINE),
                "assignmentToShift.staffAssignmentToShift.productionLine.isEmpty");
    }

}
