package com.qcadoo.mes.deviationCausesReporting.print;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.commons.functional.Fold;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.deviationCausesReporting.DeviationsReportCriteria;
import com.qcadoo.mes.deviationCausesReporting.dataProvider.DeviationSummariesDataProvider;
import com.qcadoo.mes.deviationCausesReporting.dataProvider.DeviationWithOccurrencesDataProvider;
import com.qcadoo.mes.deviationCausesReporting.domain.DeviationSummary;
import com.qcadoo.mes.deviationCausesReporting.domain.DeviationWithOccurrencesCount;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.report.api.pdf.elements.Headers;
import com.qcadoo.report.api.pdf.elements.Phrases;
import com.qcadoo.report.api.pdf.layout.VerticalLayout;
import com.qcadoo.security.api.SecurityService;

@Component("deviationProtocolPdf")
public class DeviationsProtocolPdf extends ReportPdfView {

    private static final ImmutableList<String> OCCURRENCES_TABLE_HEADERS = ImmutableList.of(
            "deviationCausesReporting.report.deviationsShortSummary.table.header.no.label",
            "deviationCausesReporting.report.deviationsShortSummary.table.header.deviationType.label",
            "deviationCausesReporting.report.deviationsShortSummary.table.header.numberOfOccurrences.label");

    private static final ImmutableList<String> SUMMARIES_TABLE_HEADERS = ImmutableList.of(
            "deviationCausesReporting.report.deviationDetailsByType.table.header.no.label",
            "deviationCausesReporting.report.deviationDetailsByType.table.header.date.label",
            "deviationCausesReporting.report.deviationDetailsByType.table.header.orderNumber.label",
            "deviationCausesReporting.report.deviationDetailsByType.table.header.productNumber.label",
            "deviationCausesReporting.report.deviationDetailsByType.table.header.comment.label");

    private static final Function<Map.Entry<String, PdfPTable>, VerticalLayout> CAUSE_AND_SUMMARY_TABLE_IN_VERTICAL_LAYOUT = new Function<Map.Entry<String, PdfPTable>, VerticalLayout>() {

        @Override
        public VerticalLayout apply(final Map.Entry<String, PdfPTable> causeWithSummaryTable) {
            String cause = causeWithSummaryTable.getKey();
            PdfPTable summaryTableForCause = causeWithSummaryTable.getValue();
            return VerticalLayout.create().append(Headers.small(cause)).append(summaryTableForCause);
        }
    };

    private final Function<String, String> getTranslationFunction(final Locale locale) {
        return new Function<String, String>() {

            @Override
            public String apply(final String translationCode) {
                return translationService.translate(translationCode, locale);
            }
        };
    }

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DeviationWithOccurrencesDataProvider deviationWithOccurrencesDataProvider;

    @Autowired
    private DeviationSummariesDataProvider deviationSummariesDataProvider;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        addDocumentTitle(document, locale);

        DeviationsReportCriteria criteria = buildCriteria(model);

        VerticalLayout mainVerticalLayout = VerticalLayout.create();
        mainVerticalLayout.append(createHeaderTable(criteria.getSearchInterval(), locale));
        mainVerticalLayout.merge(createShortSummarySection(criteria, locale));
        mainVerticalLayout.merge(createDetailedSummarySection(criteria, locale));

        mainVerticalLayout.appendToDocument(document);

        return getDocumentFileName();
    }

    private String getDocumentFileName() {
        return String.format("odchylenia_planu-%s-%s", DateTime.now().toString(DateUtils.L_REPORT_DATE_TIME_FORMAT),
                securityService.getCurrentUserName());
    }

    private DeviationsReportCriteria buildCriteria(final Map<String, Object> model) {
        DateTime dateFrom = parseDateFromModel(model.get("dateFrom")).get();
        Optional<DateTime> maybeDateTo = parseDateFromModel(model.get("dateTo"));
        return DeviationsReportCriteria.forDates(dateFrom, maybeDateTo);
    }

    private Optional<DateTime> parseDateFromModel(final Object modelValue) {
        return FluentOptional.fromNullable(modelValue).flatMap(new Function<Object, Optional<DateTime>>() {

            @Override
            public Optional<DateTime> apply(final Object input) {
                return DateUtils.tryParse(input).fold(Functions.constant(Optional.<DateTime> absent()),
                        Functions.<Optional<DateTime>> identity());
            }
        }).toOpt();
    }

    private PdfPTable createHeaderTable(final Interval searchDatesRange, final Locale locale) {
        PdfPTable headerTable = pdfHelper.createPanelTable(2);
        headerTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        pdfHelper.addTableCellAsTwoColumnsTable(headerTable, translate("deviationCausesReporting.report.header.dateFrom.label",
                        locale),
                DateUtils.toDateString(searchDatesRange.getStart().toDate()));
        pdfHelper.addTableCellAsTwoColumnsTable(headerTable, translate("deviationCausesReporting.report.header.dateTo.label", locale),
                DateUtils.toDateString(searchDatesRange.getEnd().toDate()));
        return headerTable;
    }

    private VerticalLayout createShortSummarySection(final DeviationsReportCriteria criteria, final Locale locale) {
        List<DeviationWithOccurrencesCount> deviations = deviationWithOccurrencesDataProvider
                .getDeviationsWithOccurrencesCount(criteria);
        if (deviations.isEmpty()) {
            return VerticalLayout.empty();
        }
        PdfPTable table = pdfHelper.createTableWithHeader(3, translate(OCCURRENCES_TABLE_HEADERS, locale), false, new int[] { 10,
                120, 50 });
        int idx = 1;
        for (DeviationWithOccurrencesCount deviation : deviations) {
            table.addCell(Phrases.tableContent(idx++ + "."));
            table.addCell(Phrases.tableContent(deviation.getDeviationCause()));
            table.addCell(Phrases.tableContent(ObjectUtils.toString(deviation.getTotalNumberOfOccurrences())));
        }
        Paragraph header = Headers.big(translate("deviationCausesReporting.report.deviationsShortSummary.header", locale));
        return VerticalLayout.create().append(header).append(table);
    }

    private VerticalLayout createDetailedSummarySection(final DeviationsReportCriteria criteria, final Locale locale) {
        Multimap<String, DeviationSummary> summariesByType = deviationSummariesDataProvider.getDeviationsByCauseType(criteria);
        if (summariesByType.isEmpty()) {
            return VerticalLayout.empty();
        }
        Map<String, PdfPTable> deviationCausesWithSummary = Maps.transformValues(summariesByType.asMap(),
                getSummariesToTableConverter(locale));
        List<VerticalLayout> causeAndSummaryLayouts = FluentIterable.from(deviationCausesWithSummary.entrySet())
                .transform(CAUSE_AND_SUMMARY_TABLE_IN_VERTICAL_LAYOUT).toList();
        VerticalLayout tables = Fold.fold(causeAndSummaryLayouts, VerticalLayout.create(), VerticalLayout.REDUCE_BY_MERGE);
        Paragraph header = Headers.big(translate("deviationCausesReporting.report.deviationDetailsByType.header", locale));
        return VerticalLayout.create().append(header).merge(tables);
    }

    private final Function<Collection<DeviationSummary>, PdfPTable> getSummariesToTableConverter(final Locale locale) {
        return new Function<Collection<DeviationSummary>, PdfPTable>() {

            @Override
            public PdfPTable apply(final Collection<DeviationSummary> deviationSummaries) {
                PdfPTable table = pdfHelper.createTableWithHeader(5, translate(SUMMARIES_TABLE_HEADERS, locale), true, new int[] {
                        10, 20, 45, 45, 60 });
                table.setSpacingAfter(7.0f);
                int rowNumber = 1;
                for (DeviationSummary deviationSummary : deviationSummaries) {
                    table.addCell(Phrases.tableContent(rowNumber++ + "."));
                    table.addCell(Phrases.tableContent(deviationSummary.getDate().toString(DateUtils.L_DATE_FORMAT)));
                    table.addCell(Phrases.tableContent(deviationSummary.getOrderNumber()));
                    table.addCell(Phrases.tableContent(deviationSummary.getProductNumber()));
                    table.addCell(Phrases.tableContent(deviationSummary.getComment()));
                }
                return table;
            }
        };
    };

    private String translate(final String translationCode, final Locale locale) {
        return getTranslationFunction(locale).apply(translationCode);
    }

    private List<String> translate(final Iterable<String> translationCodes, final Locale locale) {
        return FluentIterable.from(translationCodes).transform(getTranslationFunction(locale)).toList();
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle("deviationCausesReporting.report.title");
    }

    private void addDocumentTitle(Document document, Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("deviationCausesReporting.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeaderThin(document, "", documentTitle, documentAuthor, new Date());
    }
}
