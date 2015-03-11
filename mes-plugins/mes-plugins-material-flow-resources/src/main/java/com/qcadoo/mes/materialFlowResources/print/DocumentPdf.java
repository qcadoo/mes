package com.qcadoo.mes.materialFlowResources.print;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.print.helper.DocumentPdfHelper;
import com.qcadoo.mes.materialFlowResources.print.helper.DocumentPdfHelper.HeaderPair;
import com.qcadoo.mes.materialFlowResources.print.helper.PositionDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "documentPdf")
public class DocumentPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private DocumentPdfHelper documentPdfHelper;

    @Override
    protected String addContent(Document document, Map<String, Object> model, Locale locale, PdfWriter writer)
            throws DocumentException, IOException {
        Long id = Long.valueOf(model.get("id").toString());
        Entity documentEntity = documentPdfHelper.getDocumentEntity(id);
        String documentHeader = documentPdfHelper.getDocumentHeader(documentEntity, locale);
        pdfHelper.addDocumentHeader(document, "", documentHeader, "", new Date());
        addHeaderTable(document, documentEntity, locale);
        addPositionsTable(document, documentEntity, locale);
        return documentPdfHelper.getFileName(documentEntity, locale);
    }

    @Override
    protected void addTitle(Document document, Locale locale) {
        document.addTitle(translationService.translate("materialFlowResources.report.title", locale));
    }

    private void addHeaderTable(Document document, Entity documentEntity, Locale locale) throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(3);

        List<HeaderPair> headerValues = documentPdfHelper.getDocumentHeaderTableContent(documentEntity, locale);
        for (HeaderPair pair : headerValues) {
            if (pair.getValue() != null && !pair.getValue().isEmpty()) {
                pdfHelper.addTableCellAsOneColumnTable(table, pair.getLabel(), pair.getValue());
            } else {
                pdfHelper.addTableCellAsOneColumnTable(table, StringUtils.EMPTY, StringUtils.EMPTY);
            }
        }
        HeaderPair description = documentPdfHelper.getDescription(documentEntity, locale);

        Phrase label = new Phrase(description.getValue().isEmpty() ? StringUtils.EMPTY : description.getLabel(),
                FontUtils.getDejavuBold8Dark());
        PdfPCell descriptionLabelCell = new PdfPCell(label);
        descriptionLabelCell.setColspan(2);
        descriptionLabelCell.setBorder(PdfPCell.NO_BORDER);
        descriptionLabelCell.setPaddingLeft(7);
        table.addCell(descriptionLabelCell);

        Phrase totalValueLabel = new Phrase(documentPdfHelper.getTotalValueLabel(locale), FontUtils.getDejavuBold8Dark());
        PdfPCell totalValueLabelCell = new PdfPCell(totalValueLabel);
        totalValueLabelCell.setBorder(PdfPCell.NO_BORDER);
        totalValueLabelCell.setPaddingLeft(7);
        table.addCell(totalValueLabelCell);
        table.completeRow();

        Phrase value = new Phrase(description.getValue(), FontUtils.getDejavuRegular9Dark());
        PdfPCell descriptionValueCell = new PdfPCell(value);
        descriptionValueCell.setColspan(2);
        descriptionValueCell.setBorder(PdfPCell.NO_BORDER);
        descriptionValueCell.setPaddingLeft(7);
        descriptionValueCell.setPaddingBottom(15);
        table.addCell(descriptionValueCell);

        Phrase totalValue = new Phrase(PositionDataProvider.totalValue(documentEntity), FontUtils.getDejavuRegular9Dark());
        PdfPCell totalValueCell = new PdfPCell(totalValue);
        totalValueCell.setBorder(PdfPCell.NO_BORDER);
        totalValueCell.setPaddingLeft(7);
        totalValueCell.setPaddingBottom(15);
        table.addCell(totalValueCell);
        table.completeRow();

        table.setSpacingAfter(20);

        document.add(table);

    }

    private void addPositionsTable(Document document, Entity documentEntity, Locale locale) throws DocumentException {

        document.add(new Paragraph(documentPdfHelper.getTableHeader(locale), FontUtils.getDejavuBold10Dark()));

        int[] headerWidths = { 20, 100, 40, 30, 40, 50, 50, 50 };
        Map<String, HeaderAlignment> headerValues = documentPdfHelper.getPositionsTableHeaderLabels(locale);
        PdfPTable positionsTable = pdfHelper.createTableWithHeader(8, Lists.newArrayList(headerValues.keySet()), false,
                headerWidths, headerValues);
        positionsTable.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
        positionsTable.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
        positionsTable.setHeaderRows(1);
        List<Entity> positions = PositionDataProvider.getPositions(documentEntity);
        for (Entity position : positions) {
            positionsTable.addCell(createCell(PositionDataProvider.index(position), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(PositionDataProvider.product(position), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(PositionDataProvider.quantity(position), Element.ALIGN_RIGHT));
            positionsTable.addCell(createCell(PositionDataProvider.unit(position), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(PositionDataProvider.price(position), Element.ALIGN_RIGHT));
            positionsTable.addCell(createCell(PositionDataProvider.batch(position), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(PositionDataProvider.productionDate(position), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(PositionDataProvider.value(position), Element.ALIGN_RIGHT));

        }

        PdfPCell totalLabel = createCell(documentPdfHelper.getTotaLabel(locale), Element.ALIGN_LEFT);
        totalLabel.setColspan(7);
        totalLabel.setBorderWidth(0.7f);
        positionsTable.addCell(totalLabel);
        PdfPCell totalValue = createCell(PositionDataProvider.totalValue(documentEntity), Element.ALIGN_RIGHT);
        totalValue.setBorderWidth(0.7f);
        positionsTable.addCell(totalValue);
        positionsTable.setSpacingAfter(20);

        document.add(positionsTable);
    }

    private PdfPCell createCell(String content, int alignment) {
        PdfPCell cell = new PdfPCell();
        float border = 0.2f;
        cell.setPhrase(new Phrase(content, FontUtils.getDejavuRegular7Dark()));
        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(border);
        cell.disableBorderSide(PdfPCell.RIGHT);
        cell.disableBorderSide(PdfPCell.LEFT);
        cell.setPadding(5);
        return cell;
    }
}
