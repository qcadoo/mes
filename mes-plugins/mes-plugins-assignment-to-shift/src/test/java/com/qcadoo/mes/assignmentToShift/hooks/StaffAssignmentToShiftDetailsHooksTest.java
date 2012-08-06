package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.OccupationTypeEnumStringValue.WORK_ON_LINE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.PRODUCTION_LINE;
import static com.qcadoo.model.constants.DictionaryItemFields.NAME;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;
import static org.mockito.BDDMockito.given;

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

        given(view.getComponentByReference("form")).willReturn(staffAssignmentToShiftForm);

        given(view.getComponentByReference(OCCUPATION_TYPE)).willReturn(occupationType);
        given(view.getComponentByReference(PRODUCTION_LINE)).willReturn(productionLine);
        given(view.getComponentByReference(OCCUPATION_TYPE_NAME)).willReturn(occupationTypeName);

        given(dataDefinitionService.get("qcadooModel", "dictionaryItem")).willReturn(dataDefinition);
        given(dataDefinition.find()).willReturn(builder);

    }

    @Test
    public void shouldEnabledProductionLineAndDisableOccupationTypeNameFieldsWhenWorkOnLineIsSelected() throws Exception {
        // given
        String dictionaryName = "Praca na linii";
        String technicalCode = "01workOnLine";

        given(occupationType.getFieldValue()).willReturn(dictionaryName);
        SearchCriterion criterion = SearchRestrictions.eq(NAME, dictionaryName);
        given(builder.add(criterion)).willReturn(builder);
        given(builder.uniqueResult()).willReturn(dictionary);
        given(dictionary.getStringField(TECHNICAL_CODE)).willReturn(technicalCode);

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);

        // then
        Mockito.verify(productionLine).setVisible(true);
        Mockito.verify(occupationTypeName).setVisible(false);
    }

    @Test
    public void shouldDisabledProductionLineAndEnableOccupationTypeNameFieldsWhenOtherCaseIsSelected() throws Exception {
        // given
        String dictionaryName = "Inne zadania";
        String technicalCode = "02otherCase";

        given(occupationType.getFieldValue()).willReturn(dictionaryName);
        SearchCriterion criterion = SearchRestrictions.eq(NAME, dictionaryName);
        given(builder.add(criterion)).willReturn(builder);
        given(builder.uniqueResult()).willReturn(dictionary);
        given(dictionary.getStringField(TECHNICAL_CODE)).willReturn(technicalCode);

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);

        // then
        Mockito.verify(productionLine).setVisible(false);
        Mockito.verify(occupationTypeName).setVisible(true);
    }

    @Test
    public void shouldDisabledProductionLineAndOccupationTypeNameFieldsWhenMixDictionaryIsSelected() throws Exception {
        // given
        String dictionaryName = "MIX";

        given(occupationType.getFieldValue()).willReturn(dictionaryName);
        SearchCriterion criterion = SearchRestrictions.eq(NAME, dictionaryName);
        given(builder.add(criterion)).willReturn(builder);
        given(builder.uniqueResult()).willReturn(dictionary);
        given(dictionary.getStringField(TECHNICAL_CODE)).willReturn(Mockito.anyString());

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);

        // then
        Mockito.verify(productionLine).setVisible(false);
        Mockito.verify(occupationTypeName).setVisible(false);
    }

    @Test
    public void shouldDisabledProductionLineAndOccupationTypeNameFieldsWhenEmptyIsSelected() throws Exception {
        // given
        String dictionaryName = "";

        given(occupationType.getFieldValue()).willReturn(dictionaryName);
        SearchCriterion criterion = SearchRestrictions.eq(NAME, dictionaryName);
        given(builder.add(criterion)).willReturn(builder);
        given(builder.uniqueResult()).willReturn(null);

        // when
        detailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);

        // then
        Mockito.verify(productionLine).setVisible(false);
        Mockito.verify(occupationTypeName).setVisible(false);
    }

    @Test
    public void shouldntSetOccupationTypeToDefaultWhenFormIsSaved() {
        // given
        String dictionaryName = "Praca na linii";

        given(staffAssignmentToShiftForm.getEntityId()).willReturn(1L);
        given(occupationType.getFieldValue()).willReturn(dictionaryName);

        // when
        detailsHooks.setOccupationTypeToDefault(view);

        // then
        Mockito.verify(occupationType, Mockito.never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldntSetOccupationTypeToDefaultWhenDictionaryIsNull() {
        // given
        given(staffAssignmentToShiftForm.getEntityId()).willReturn(null);
        given(occupationType.getFieldValue()).willReturn(null);

        SearchCriterion criterion = SearchRestrictions.eq(TECHNICAL_CODE, WORK_ON_LINE.getStringValue());
        given(builder.add(criterion)).willReturn(builder);
        given(builder.uniqueResult()).willReturn(null);

        // when
        detailsHooks.setOccupationTypeToDefault(view);

        // then
        Mockito.verify(occupationType, Mockito.never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldSetOccupationTypeToDefaultWhenDictionary() {
        // given
        given(staffAssignmentToShiftForm.getEntityId()).willReturn(null);
        given(occupationType.getFieldValue()).willReturn(null);

        SearchCriterion criterion = SearchRestrictions.eq(TECHNICAL_CODE, WORK_ON_LINE.getStringValue());
        given(builder.add(criterion)).willReturn(builder);
        given(builder.uniqueResult()).willReturn(dictionary);
        given(dictionary.getStringField(NAME)).willReturn(Mockito.anyString());

        // when
        detailsHooks.setOccupationTypeToDefault(view);

        // then
        Mockito.verify(occupationType).setFieldValue(Mockito.anyString());
    }

}
