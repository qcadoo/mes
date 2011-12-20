package com.qcadoo.mes.workPlans.print;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.Entity;

public class WorkPlanPdfServiceTest {

    private WorkPlanPdfService2 workPlanPdfService;

    private TranslationService translationService;

    @Before
    public void init() {
        workPlanPdfService = new WorkPlanPdfService2();

        translationService = mock(TranslationService.class);

        ReflectionTestUtils.setField(workPlanPdfService, "translationService", translationService);
    }

    @Test
    public void shouldHasCorrectReportTitle() {
        // given
        String correctTitle = "title";
        Locale locale = Locale.getDefault();
        when(translationService.translate("workPlans.workPlan.report.title", locale)).thenReturn(correctTitle);

        // when
        String title = workPlanPdfService.getReportTitle(locale);

        // then
        assertEquals(correctTitle, title);
    }

    @Test
    public void shouldHasCorrectHeader() throws DocumentException {
        // given
        Locale locale = Locale.getDefault();
        Document document = mock(Document.class);
        Entity entity = mock(Entity.class);

        // when
        workPlanPdfService.buildPdfContent(document, entity, locale);

        // then
    }
}
