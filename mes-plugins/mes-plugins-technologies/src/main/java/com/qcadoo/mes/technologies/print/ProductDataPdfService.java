/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologies.print;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.FormsFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SizeFields;
import com.qcadoo.mes.technologies.constants.ProductDataAttachmentFields;
import com.qcadoo.mes.technologies.constants.ProductDataFields;
import com.qcadoo.mes.technologies.constants.ProductDataInputFields;
import com.qcadoo.mes.technologies.constants.ProductDataOperationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.*;

@Service
public class ProductDataPdfService extends PdfDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductDataPdfService.class);

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected void buildPdfContent(final Document document, final Entity productData, final Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("technologies.productData.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        PdfPTable panelTable = pdfHelper.createPanelTable(1);

        panelTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
        panelTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        panelTable.setTableEvent(null);

        panelTable.addCell(addInfoPanel(productData, locale));

        document.add(panelTable);

        document.add(new Paragraph(translationService.translate("technologies.productData.report.paragraph", locale),
                FontUtils.getDejavuBold11Dark()));
        document.add(addMaterialsTable(productData, locale));

        document.add(Chunk.NEWLINE);

        document.add(new Paragraph(translationService.translate("technologies.productData.report.paragraph2", locale),
                FontUtils.getDejavuBold11Dark()));
        document.add(addOperationsTable(productData, locale));

        String description = productData.getStringField(ProductDataFields.DESCRIPTION);

        if (!StringUtils.isEmpty(description)) {
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(translationService.translate("technologies.productData.report.paragraph4", locale),
                    FontUtils.getDejavuBold11Dark()));

            document.add(new Paragraph(description, FontUtils.getDejavuRegular9Dark()));
        }

        document.add(Chunk.NEWLINE);

        List<Entity> productDataAttachments = productData.getHasManyField(ProductDataFields.PRODUCT_DATA_ATTACHMENTS);

        productDataAttachments.forEach(productDataAttachment -> {
            try {
                String path = productDataAttachment.getStringField(ProductDataAttachmentFields.ATTACHMENT);

                Image img = Image.getInstance(path);

                float effectiveWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
                float effectiveHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();

                img.scaleToFit(effectiveWidth, effectiveHeight);

                document.add(img);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        });
    }

    private PdfPTable addInfoPanel(final Entity productData, final Locale locale) throws DocumentException {
        Entity product = productData.getBelongsToField(ProductDataFields.PRODUCT);
        String productName = product.getStringField(ProductFields.NAME);
        String ean = product.getStringField(ProductFields.EAN);
        Entity size = product.getBelongsToField(ProductFields.SIZE);

        String header = StringUtils.isEmpty(ean) ? productName : productName + ", " + ean;

        float[] panelWidths = {35f, 65f};
        Entity form = product.getBelongsToField(ProductFields.PRODUCT_FORM);

        PdfPTable outer = pdfHelper.createPanelTable(1);

        outer.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
        outer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        outer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        outer.addCell(new Paragraph(header, FontUtils.getDejavuBold11Dark()));

        PdfPTable panel = pdfHelper.createPanelTableWithSimpleFormat(2);

        panel.setWidthPercentage(100f);
        panel.setWidths(panelWidths);
        panel.getDefaultCell().setBorder(Rectangle.TOP);

        PdfPTable leftColumn = pdfHelper.createPanelTableWithSimpleFormat(1);
        leftColumn.getDefaultCell().setPadding(0.0f);

        PdfPTable rightColumn = pdfHelper.createPanelTableWithSimpleFormat(1);
        rightColumn.getDefaultCell().setPadding(0.0f);

        if (Objects.nonNull(size)) {
            String sizeNumber = size.getStringField(SizeFields.NUMBER);

            leftColumn.addCell(createTwoColumnsHeaderTable(
                    translationService.translate("technologies.productData.report.headerTable.size", locale) + ":", sizeNumber));
        }

        if (Objects.nonNull(form)) {
            Entity company = form.getBelongsToField(FormsFields.COMPANY);
            String companyNumber = StringUtils.EMPTY;

            if (Objects.nonNull(company)) {
                companyNumber = company.getStringField(CompanyFields.NAME) + " - ";
            }

            String mouldSize = StringUtils.EMPTY;

            if (Objects.nonNull(form.getDecimalField(FormsFields.SIZE))) {
                mouldSize = " - " + form.getDecimalField(FormsFields.SIZE).stripTrailingZeros().toPlainString();
            }

            rightColumn.addCell(createTwoColumnsHeaderTable(
                    translationService.translate("technologies.productData.report.headerTable.form", locale) + ":",
                    companyNumber + form.getStringField(FormsFields.NUMBER) + mouldSize));
        }

        panel.addCell(leftColumn);
        panel.addCell(rightColumn);

        outer.addCell(panel);

        return outer;
    }

    private PdfPTable createTwoColumnsHeaderTable(final String label, final String value) throws DocumentException {
        final float[] columnWidths = {30f, 70f};

        PdfPTable cellTable = new PdfPTable(2);

        cellTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        cellTable.setWidths(columnWidths);

        cellTable.addCell(new Phrase(label, FontUtils.getDejavuBold7Dark()));
        cellTable.addCell(new Phrase(value, FontUtils.getDejavuRegular9Dark()));

        return cellTable;
    }

    public PdfPTable addMaterialsTable(final Entity productData, final Locale locale) {
        List<String> materialsTableHeader = Lists.newArrayList();

        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        for (String translate : Arrays.asList("technologies.productData.report.material.columnHeader.number",
                "technologies.productData.report.material.columnHeader.name",
                "technologies.productData.report.material.columnHeader.quantity",
                "technologies.productData.report.material.columnHeader.unit")) {
            materialsTableHeader.add(translationService.translate(translate, locale));
        }

        alignments.put(translationService.translate("technologies.productData.report.material.columnHeader.number", locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate("technologies.productData.report.material.columnHeader.name", locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate("technologies.productData.report.material.columnHeader.quantity", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService.translate("technologies.productData.report.material.columnHeader.unit", locale),
                HeaderAlignment.LEFT);

        PdfPTable materialsTable = pdfHelper.createTableWithHeader(materialsTableHeader.size(), materialsTableHeader, false,
                alignments);

        materialsTable.getDefaultCell().setBorder(Rectangle.BOX);

        try {
            float[] columnWidths = {0.4f, 0.95f, 0.2f, 0.2f};

            materialsTable.setWidths(columnWidths);
        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        List<Entity> productDataInputs = Lists.newArrayList(productData.getHasManyField(ProductDataFields.PRODUCT_DATA_INPUTS));

        productDataInputs.sort(Comparator.comparing(productDataInput -> productDataInput.getIntegerField(ProductDataInputFields.SUCCESSION)));

        productDataInputs.forEach(productDataInput -> {
            String number = productDataInput.getStringField(ProductDataInputFields.NUMBER);
            String name = productDataInput.getStringField(ProductDataInputFields.NAME);
            String quantity = productDataInput.getDecimalField(ProductDataInputFields.QUANTITY).stripTrailingZeros().toPlainString();
            String unit = productDataInput.getStringField(ProductDataInputFields.UNIT);

            materialsTable.addCell(new Phrase(number, FontUtils.getDejavuRegular7Dark()));
            materialsTable.addCell(new Phrase(name, FontUtils.getDejavuRegular7Dark()));
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            materialsTable.addCell(new Phrase(quantity, FontUtils.getDejavuRegular7Dark()));
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            materialsTable.addCell(new Phrase(unit, FontUtils.getDejavuRegular7Dark()));
        });

        return materialsTable;
    }

    public PdfPTable addOperationsTable(final Entity productData, final Locale locale) {
        List<String> operationsTableHeader = Lists.newArrayList();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        for (String translate : Arrays.asList("technologies.productData.report.operation.columnHeader.number",
                "technologies.productData.report.operation.columnHeader.name")) {
            operationsTableHeader.add(translationService.translate(translate, locale));
        }

        alignments.put(translationService.translate("technologies.productData.report.operation.columnHeader.number", locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate("technologies.productData.report.operation.columnHeader.name", locale),
                HeaderAlignment.LEFT);

        PdfPTable operationsTable = pdfHelper.createTableWithHeader(operationsTableHeader.size(), operationsTableHeader, false,
                alignments);
        operationsTable.getDefaultCell().setBorder(Rectangle.BOX);

        try {
            float[] columnWidths = {0.5f, 1f};

            operationsTable.setWidths(columnWidths);
        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        List<Entity> productDataOperations = Lists.newArrayList(productData.getHasManyField(ProductDataFields.PRODUCT_DATA_OPERATIONS));

        productDataOperations.sort(Comparator.comparing(productDataOperation -> productDataOperation.getIntegerField(ProductDataOperationFields.SUCCESSION)));

        productDataOperations.forEach(productDataOperation -> {
            String name = productDataOperation.getStringField(ProductDataOperationFields.NAME);
            String number = productDataOperation.getStringField(ProductDataOperationFields.NUMBER);
            String description = productDataOperation.getStringField(ProductDataOperationFields.DESCRIPTION);

            operationsTable.addCell(new Phrase(number, FontUtils.getDejavuRegular7Dark()));
            operationsTable.addCell(
                    new Phrase(name + (Objects.nonNull(description) ? " (" + description + ")" : ""), FontUtils.getDejavuRegular7Dark()));
        });

        return operationsTable;
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("technologies.productData.report.title", locale);
    }

}
