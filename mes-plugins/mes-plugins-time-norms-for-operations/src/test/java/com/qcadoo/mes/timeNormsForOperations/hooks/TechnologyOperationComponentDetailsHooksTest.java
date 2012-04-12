package com.qcadoo.mes.timeNormsForOperations.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class TechnologyOperationComponentDetailsHooksTest {

    private TechnologyOperationComponentDetailsHooks technologyOperationComponentDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private TechnologyService technologyService;

    @Mock
    private TranslationService translationService;

    @Mock
    private NumberService numberService;

    @Mock
    private Entity opComp1, prodComp1, product1;

    @Mock
    private FormComponent form;

    @Mock
    private DataDefinition dd;

    @Mock
    private ComponentState productionInOneCycle;

    private DecimalFormat decimalFormat;

    private Locale locale;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyOperationComponentDetailsHooks = new TechnologyOperationComponentDetailsHooks();

        ReflectionTestUtils.setField(technologyOperationComponentDetailsHooks, "technologyService", technologyService);
        ReflectionTestUtils.setField(technologyOperationComponentDetailsHooks, "translationService", translationService);
        ReflectionTestUtils.setField(technologyOperationComponentDetailsHooks, "numberService", numberService);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);

        locale = LocaleContextHolder.getLocale();

        decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(3);
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);

        given(numberService.format(Mockito.any(BigDecimal.class))).willAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BigDecimal number = (BigDecimal) args[0];
                return decimalFormat.format(number);
            }
        });

        given(view.getComponentByReference("form")).willReturn(form);
        given(form.getEntity()).willReturn(opComp1);
        given(opComp1.getDataDefinition()).willReturn(dd);
        given(dd.get(0L)).willReturn(opComp1);
        given(technologyService.getMainOutputProductComponent(opComp1)).willReturn(prodComp1);
        given(prodComp1.getDecimalField("quantity")).willReturn(new BigDecimal(1.1f));
        given(opComp1.getDecimalField("productionInOneCycle")).willReturn(new BigDecimal(1.2f));
        given(view.getComponentByReference("productionInOneCycle")).willReturn(productionInOneCycle);
        given(prodComp1.getBelongsToField("product")).willReturn(product1);
        given(product1.getStringField("unit")).willReturn("unit");

    }

    @Test
    public void shouldNotFreakOutWhenTheOutputProductComponentCannotBeFound() {
        given(technologyService.getMainOutputProductComponent(opComp1)).willThrow(new IllegalStateException());

        // when
        technologyOperationComponentDetailsHooks.checkOperationOutputQuantities(view);
    }

    @Test
    public void shouldAttachCorrectErrorToTheRightComponentIfQuantitiesAreDifferent() {
        // given
        given(translationService.translate("technologies.technologyOperationComponent.validate.error.invalidQuantity1", locale))
                .willReturn("message1");
        given(translationService.translate("technologies.technologyOperationComponent.validate.error.invalidQuantity2", locale))
                .willReturn("message2");
        String number = decimalFormat.format(prodComp1.getDecimalField("quantity"));

        // when
        technologyOperationComponentDetailsHooks.checkOperationOutputQuantities(view);

        // then
        verify(productionInOneCycle).addMessage("message1 " + number + " unit message2", MessageType.FAILURE);
    }

    @Test
    public void shouldNotGiveAnyErrorIfQuantitiesAreTheSame() {
        // given
        given(prodComp1.getDecimalField("quantity")).willReturn(new BigDecimal(1.1f));
        given(opComp1.getDecimalField("productionInOneCycle")).willReturn(new BigDecimal(1.1f));

        // when
        technologyOperationComponentDetailsHooks.checkOperationOutputQuantities(view);

        // then
        verify(productionInOneCycle, Mockito.never()).addMessage(Mockito.anyString(), Mockito.eq(MessageType.FAILURE));
    }
}
