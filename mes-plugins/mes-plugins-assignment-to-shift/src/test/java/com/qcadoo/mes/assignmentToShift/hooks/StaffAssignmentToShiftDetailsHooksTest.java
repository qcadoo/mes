package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.model.constants.DictionaryItemFields.NAME;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class StaffAssignmentToShiftDetailsHooksTest {

    private StaffAssignmentToShiftDetailsHooks detailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    SearchCriteriaBuilder builder;

    @Mock
    private Entity dictionary;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private FieldComponent occupationType, productionLine, occupationTypeName;

    @Before
    public void init() {
        detailsHooks = new StaffAssignmentToShiftDetailsHooks();
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(detailsHooks, "dataDefinitionService", dataDefinitionService);

        when(view.getComponentByReference("occupationType")).thenReturn(occupationType);
        when(view.getComponentByReference("productionLine")).thenReturn(productionLine);
        when(view.getComponentByReference("occupationTypeName")).thenReturn(occupationTypeName);

        when(dataDefinitionService.get("qcadooModel", "dictionaryItem")).thenReturn(dataDefinition);
        when(dataDefinition.find()).thenReturn(builder);

    }

    @Test
    public void shouldEnabledProductionLineAndDisableOccupationTypeNameFieldsWhenWorkOnLineIsSelected() throws Exception {
        // given
        String dictionaryName = "Praca na linii";
        String technicalCode = "01workOnLine";

        when(occupationType.getFieldValue()).thenReturn(dictionaryName);
        SearchCriterion criterion = SearchRestrictions.eq(NAME, dictionaryName);
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.uniqueResult()).thenReturn(dictionary);
        when(dictionary.getStringField("technicalCode")).thenReturn(technicalCode);

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);
        // then
        Mockito.verify(productionLine).setEnabled(true);
        Mockito.verify(occupationTypeName).setEnabled(false);
    }

    @Test
    public void shouldDisabledProductionLineAndEnableOccupationTypeNameFieldsWhenOtherCaseIsSelected() throws Exception {
        // given
        String dictionaryName = "Inne zadania";
        String technicalCode = "02otherCase";

        when(occupationType.getFieldValue()).thenReturn(dictionaryName);
        SearchCriterion criterion = SearchRestrictions.eq(NAME, dictionaryName);
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.uniqueResult()).thenReturn(dictionary);
        when(dictionary.getStringField("technicalCode")).thenReturn(technicalCode);

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);
        // then
        Mockito.verify(productionLine).setEnabled(false);
        Mockito.verify(occupationTypeName).setEnabled(true);
    }

    @Test
    public void shouldDisabledProductionLineAndOccupationTypeNameFieldsWhenMixDictionaryIsSelected() throws Exception {
        // given
        String dictionaryName = "MIX";

        when(occupationType.getFieldValue()).thenReturn(dictionaryName);
        SearchCriterion criterion = SearchRestrictions.eq(NAME, dictionaryName);
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.uniqueResult()).thenReturn(dictionary);
        when(dictionary.getStringField("technicalCode")).thenReturn(Mockito.anyString());

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);
        // then
        Mockito.verify(productionLine).setEnabled(false);
        Mockito.verify(occupationTypeName).setEnabled(false);
    }
}
