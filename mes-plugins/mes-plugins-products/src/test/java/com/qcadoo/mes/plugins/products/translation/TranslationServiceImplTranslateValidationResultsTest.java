package com.qcadoo.mes.plugins.products.translation;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Locale;

import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class TranslationServiceImplTranslateValidationResultsTest {

    @Test
    public void shouldTranslateValidationResult() {
        // given
        ValidationResults validationResults = new ValidationResults();
        validationResults.addGlobalError("globalMsg1", new String[] { "aa" });
        validationResults.addGlobalError("globalMsg2", new String[] {});

        validationResults.addError(new FieldDefinition("f1"), "f11", new String[] {});
        validationResults.addError(new FieldDefinition("f2"), "f22", new String[] { "1", "2", "3" });
        validationResults.addError(new FieldDefinition("f3"), "f33", new String[] {});

        Entity entity = new Entity((long) 21);
        validationResults.setEntity(entity);

        Locale locale = new Locale("test");

        MessageSource ms = mock(MessageSource.class);
        TranslationServiceImpl translationService = new TranslationServiceImpl();
        ReflectionTestUtils.setField(translationService, "messageSource", ms);

        given(ms.getMessage("globalMsg1", new String[] { "aa" }, "TO TRANSLATE: globalMsg1", locale)).willReturn("globalMsg1ok");
        given(ms.getMessage("globalMsg2", new String[] {}, "TO TRANSLATE: globalMsg2", locale)).willReturn("globalMsg2ok");
        given(ms.getMessage("f11", new String[] {}, "TO TRANSLATE: f11", locale)).willReturn("f1ok");
        given(ms.getMessage("f22", new String[] { "1", "2", "3" }, "TO TRANSLATE: f22", locale)).willReturn("f2ok");
        given(ms.getMessage("f33", new String[] {}, "TO TRANSLATE: f33", locale)).willReturn("f3ok");

        // when
        translationService.translateValidationResults(validationResults, locale);

        // then
        assertEquals(entity, validationResults.getEntity());
        assertEquals(2, validationResults.getGlobalErrors().size());
        assertEquals("globalMsg1ok", validationResults.getGlobalErrors().get(0).getMessage());
        assertEquals("globalMsg2ok", validationResults.getGlobalErrors().get(1).getMessage());
        assertEquals(3, validationResults.getErrors().size());
        assertEquals("f1ok", validationResults.getErrorForField("f1").getMessage());
        assertEquals("f2ok", validationResults.getErrorForField("f2").getMessage());
        assertEquals("f3ok", validationResults.getErrorForField("f3").getMessage());
    }
}
