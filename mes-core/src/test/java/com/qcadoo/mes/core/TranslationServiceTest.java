package com.qcadoo.mes.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Locale;

import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.TranslationServiceImpl;
import com.qcadoo.mes.model.internal.FieldDefinitionImpl;

public class TranslationServiceTest {

    @Test
    public void shouldTranslateValidationResult() {
        // given
        Entity entity = new DefaultEntity((long) 21);

        entity.addGlobalError("globalMsg1", new String[] { "aa" });
        entity.addGlobalError("globalMsg2", new String[] {});

        entity.addError(new FieldDefinitionImpl("f1"), "f11", new String[] {});
        entity.addError(new FieldDefinitionImpl("f2"), "f22", new String[] { "1", "2", "3" });
        entity.addError(new FieldDefinitionImpl("f3"), "f33", new String[] {});

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
        translationService.translateEntity(entity, locale);

        // then
        assertEquals(2, entity.getGlobalErrors().size());
        assertEquals("globalMsg1ok", entity.getGlobalErrors().get(0).getMessage());
        assertEquals("globalMsg2ok", entity.getGlobalErrors().get(1).getMessage());
        assertEquals("f1ok", entity.getError("f1").getMessage());
        assertEquals("f2ok", entity.getError("f2").getMessage());
        assertEquals("f3ok", entity.getError("f3").getMessage());
    }
}
