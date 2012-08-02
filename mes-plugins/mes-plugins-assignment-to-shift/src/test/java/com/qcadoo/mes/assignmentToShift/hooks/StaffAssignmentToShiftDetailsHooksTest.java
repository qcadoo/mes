package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.OccupationTypeEnumStringValue.WORK_ON_LINE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.PRODUCTION_LINE;
import static com.qcadoo.model.constants.DictionaryItemFields.NAME;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;
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
import com.qcadoo.view.api.components.FormComponent;

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
    private FormComponent staffAssignmentToShiftForm;

    @Mock
    private FieldComponent occupationType, productionLine, occupationTypeName;

    @Before
    public void init() {
        detailsHooks = new StaffAssignmentToShiftDetailsHooks();
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(detailsHooks, "dataDefinitionService", dataDefinitionService);

        when(view.getComponentByReference("form")).thenReturn(staffAssignmentToShiftForm);

        when(view.getComponentByReference(OCCUPATION_TYPE)).thenReturn(occupationType);
        when(view.getComponentByReference(PRODUCTION_LINE)).thenReturn(productionLine);
        when(view.getComponentByReference(OCCUPATION_TYPE_NAME)).thenReturn(occupationTypeName);

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
        when(dictionary.getStringField(TECHNICAL_CODE)).thenReturn(technicalCode);

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
        when(dictionary.getStringField(TECHNICAL_CODE)).thenReturn(technicalCode);

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
        when(dictionary.getStringField(TECHNICAL_CODE)).thenReturn(Mockito.anyString());

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);

        // then
        Mockito.verify(productionLine).setEnabled(false);
        Mockito.verify(occupationTypeName).setEnabled(false);
    }

    @Test
    public void shouldDisabledProductionLineAndOccupationTypeNameFieldsWhenEmptyIsSelected() throws Exception {
        // given
        String dictionaryName = "";

        when(occupationType.getFieldValue()).thenReturn(dictionaryName);
        SearchCriterion criterion = SearchRestrictions.eq(NAME, dictionaryName);
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.uniqueResult()).thenReturn(null);

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);

        // then
        Mockito.verify(productionLine).setEnabled(false);
        Mockito.verify(occupationTypeName).setEnabled(false);
    }

    @Test
    public void shouldntSetOccupationTypeToDefaultWhenFormIsSaved() {
        // given
        String dictionaryName = "Praca na linii";

        when(staffAssignmentToShiftForm.getEntityId()).thenReturn(1L);
        when(occupationType.getFieldValue()).thenReturn(dictionaryName);

        // when
        detailsHooks.setOccupationTypeToDefault(view);

        // then
        Mockito.verify(occupationType, Mockito.never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldntSetOccupationTypeToDefaultWhenDictionaryIsNull() {
        // given
        when(staffAssignmentToShiftForm.getEntityId()).thenReturn(null);
        when(occupationType.getFieldValue()).thenReturn(null);

        SearchCriterion criterion = SearchRestrictions.eq(TECHNICAL_CODE, WORK_ON_LINE.getStringValue());
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.uniqueResult()).thenReturn(null);

        // when
        detailsHooks.setOccupationTypeToDefault(view);

        // then
        Mockito.verify(occupationType, Mockito.never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldSetOccupationTypeToDefaultWhenDictionary() {
        // given
        when(staffAssignmentToShiftForm.getEntityId()).thenReturn(null);
        when(occupationType.getFieldValue()).thenReturn(null);

        SearchCriterion criterion = SearchRestrictions.eq(TECHNICAL_CODE, WORK_ON_LINE.getStringValue());
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.uniqueResult()).thenReturn(dictionary);
        when(dictionary.getStringField(NAME)).thenReturn(Mockito.anyString());

        // when
        detailsHooks.setOccupationTypeToDefault(view);

        // then
        Mockito.verify(occupationType).setFieldValue(Mockito.anyString());
    }

}
