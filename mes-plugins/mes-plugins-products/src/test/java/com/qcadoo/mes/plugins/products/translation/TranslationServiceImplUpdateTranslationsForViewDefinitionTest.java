package com.qcadoo.mes.plugins.products.translation;

import org.junit.Test;

public class TranslationServiceImplUpdateTranslationsForViewDefinitionTest {

    @Test
    public void shouldTranslateViewDefinition() {
        // given
        // ViewDefinition viewDefinition = new ViewDefinition("testView");
        // viewDefinition.setHeader("view header");
        //
        // DataDefinition formDD = new DataDefinition("testEntity1");
        // formDD.addField(new DataFieldDefinition("f1"));
        // formDD.addField(new DataFieldDefinition("f2"));
        // FormDefinition formDef = new FormDefinition("testForm", formDD);
        // formDef.setHeader("form header");
        // formDef.addField(new FormFieldDefinition("f1"));
        // formDef.addField(new FormFieldDefinition("f2"));
        //
        // DataDefinition gridDD = new DataDefinition("testEntity2");
        // gridDD.addField(new DataFieldDefinition("g1"));
        // gridDD.addField(new DataFieldDefinition("g2"));
        // GridDefinition gridDef = new GridDefinition("testGrid", gridDD);
        // gridDef.setHeader("grid header");
        // ColumnDefinition c1 = new ColumnDefinition("c1");
        // c1.setFields(Arrays.asList(new DataFieldDefinition[] { gridDD.getField("g1") }));
        // ColumnDefinition c2 = new ColumnDefinition("c2");
        // c2.setFields(Arrays.asList(new DataFieldDefinition[] { gridDD.getField("g2") }));
        // gridDef.setColumns(Arrays.asList(new ColumnDefinition[] { c1, c2 }));
        //
        // viewDefinition.setElements(Arrays.asList(new ComponentDefinition[] { formDef, gridDef }));
        //
        // Locale locale = new Locale("test");
        //
        // MessageSource ms = mock(MessageSource.class);
        // TranslationServiceImpl translationService = new TranslationServiceImpl();
        // ReflectionTestUtils.setField(translationService, "messageSource", ms);
        //
        // Map<String, String> translations = new HashMap<String, String>();
        //
        // given(ms.getMessage("view header", null, "TO TRANSLATE: view header", locale)).willReturn("h1ok");
        // given(ms.getMessage("form header", null, "TO TRANSLATE: form header", locale)).willReturn("h2ok");
        // given(ms.getMessage("grid header", null, "TO TRANSLATE: grid header", locale)).willReturn("h3ok");
        //
        // given(ms.getMessage("testView.testForm.field.f1", null, locale)).willReturn("f1ok");
        // given(ms.getMessage("testView.testForm.field.f2", null, locale)).willThrow(new NoSuchMessageException(""));
        // given(ms.getMessage("entity.testEntity1.field.f2", null, "TO TRANSLATE: entity.testEntity1.field.f2", locale))
        // .willReturn("f2ok");
        //
        // given(ms.getMessage("testView.testGrid.column.c1", null, locale)).willReturn("c1ok");
        // given(ms.getMessage("testView.testGrid.column.c2", null, locale)).willThrow(new NoSuchMessageException(""));
        //
        // given(ms.getMessage("testView.testGrid.field.g1", null, locale)).willReturn("g1ok");
        // given(ms.getMessage("testView.testGrid.field.g2", null, locale)).willThrow(new NoSuchMessageException(""));
        // given(ms.getMessage("entity.testEntity2.field.g2", null, "TO TRANSLATE: entity.testEntity2.field.g2", locale))
        // .willReturn("g2ok");
        //
        // // when
        // translationService.updateTranslationsForViewDefinition(viewDefinition, translations, locale);
        //
        // // then
        // assertEquals(9, translations.size());
        // assertEquals("h1ok", translations.get("view header"));
        // assertEquals("h2ok", translations.get("form header"));
        // assertEquals("f1ok", translations.get("testView.testForm.field.f1"));
        // assertEquals("f2ok", translations.get("testView.testForm.field.f2"));
        // assertEquals("h3ok", translations.get("grid header"));
        // assertEquals("c1ok", translations.get("testView.testGrid.column.c1"));
        // assertEquals("g2ok", translations.get("testView.testGrid.column.c2"));
        // assertEquals("g1ok", translations.get("testView.testGrid.field.g1"));
        // assertEquals("g2ok", translations.get("testView.testGrid.field.g2"));

    }
}
