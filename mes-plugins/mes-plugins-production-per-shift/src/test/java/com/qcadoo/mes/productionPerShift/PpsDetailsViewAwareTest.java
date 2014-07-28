package com.qcadoo.mes.productionPerShift;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.utils.EntityUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

public abstract class PpsDetailsViewAwareTest {

    protected static final String WINDOW_REF = "window";

    protected static final String FORM_REF = "form";

    protected static final String PROGRESS_RIBBON_GROUP_NAME = "progress";

    protected static final String PROGRESS_TYPE_COMBO_REF = "plannedProgressType";

    protected static final String PROGRESS_FOR_DAYS_ADL_REF = "progressForDays";

    protected static final String ORDER_LOOKUP_REF = "order";

    protected static final String TECHNOLOGY_LOOKUP_REF = "technology";

    protected static final String OPERATION_LOOKUP_REF = "productionPerShiftOperation";

    protected static final String PRODUCED_PRODUCT_LOOKUP_REF = "produces";

    protected static final String VIEW_IS_INITIALIZED_CHECKBOX_REF = "viewIsInitialized";

    protected static final String UNIT_COMPONENT_NAME = "unit";

    protected static final String WAS_CORRECTED_CHECKBOX_REF = "wasItCorrected";

    protected static final String PLANNED_START_DATE_TIME_REF = "orderPlannedStartDate";

    protected static final String CORRECTED_START_DATE_TIME_REF = "orderCorrectedStartDate";

    protected static final String EFFECTIVE_START_DATE_TIME_REF = "orderEffectiveStartDate";

    protected static final String CORRECTION_CAUSE_TYPES_ADL_REF = "plannedProgressCorrectionTypes";

    protected static final String CORRECTION_COMMENT_TEXT_AREA_REF = "plannedProgressCorrectionComment";

    protected static final String DAY_NUMBER_INPUT_REF = "day";

    protected static final String DAILY_PROGRESS_ADL_REF = "dailyProgress";

    protected static final String SHIFT_LOOKUP_REF = "shift";

    protected static final String QUANTITY_FIELD_REF = "quantity";

    @Mock
    protected ViewDefinitionState view;

    @Mock
    protected FieldComponent progressTypeComboBox;

    @Mock
    private LookupComponent orderLookup;

    @Mock
    protected Entity order, technology;

    public void init() {
        MockitoAnnotations.initMocks(this);
        orderLookup = mockLookup(order);
        stubViewComponent(ORDER_LOOKUP_REF, orderLookup);
        stubViewComponent(PROGRESS_TYPE_COMBO_REF, progressTypeComboBox);
    }

    protected void stubViewComponent(final String referenceName, final ComponentState component) {
        given(view.getComponentByReference(referenceName)).willReturn(component);
        given(view.tryFindComponentByReference(referenceName)).willReturn(Optional.fromNullable(component));
    }

    protected void stubFormComponent(final FormComponent form, final String componentName, final FieldComponent component) {
        given(form.findFieldComponentByName(componentName)).willReturn(component);
    }

    protected FormComponent mockForm(final Entity underlyingEntity) {
        FormComponent form = mock(FormComponent.class);
        given(form.getEntity()).willReturn(underlyingEntity);
        given(form.getPersistedEntityWithIncludedFormValues()).willReturn(underlyingEntity);
        given(form.getEntityId()).willAnswer(new Answer<Long>() {

            @Override
            public Long answer(final InvocationOnMock invocation) throws Throwable {
                return FluentOptional.fromNullable(underlyingEntity).flatMap(EntityUtils.getSafeIdExtractor()).toOpt().orNull();
            }
        });
        return form;
    }

    protected LookupComponent mockLookup(final Entity underlyingEntity) {
        LookupComponent lookupComponent = mock(LookupComponent.class);
        given(lookupComponent.getEntity()).willReturn(underlyingEntity);
        given(lookupComponent.getFieldValue()).willAnswer(new Answer<Long>() {

            @Override
            public Long answer(final InvocationOnMock invocation) throws Throwable {
                return FluentOptional.fromNullable(underlyingEntity).flatMap(EntityUtils.getSafeIdExtractor()).toOpt().orNull();
            }
        });
        return lookupComponent;
    }

    protected FieldComponent mockFieldComponent(final Object componentValue) {
        FieldComponent fieldComponent = mock(FieldComponent.class);
        given(fieldComponent.getFieldValue()).willReturn(componentValue);
        return fieldComponent;
    }

    protected void stubProgressType(final ProgressType progressType) {
        given(progressTypeComboBox.getFieldValue()).willReturn(progressType.getStringValue());
    }

}
