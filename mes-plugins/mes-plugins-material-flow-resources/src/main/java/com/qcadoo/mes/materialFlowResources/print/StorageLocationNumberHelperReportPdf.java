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
package com.qcadoo.mes.materialFlowResources.print;

import com.lowagie.text.*;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationNumberHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ColorUtils;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.pdf.PdfPageNumbering;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

@Component(value = "storageLocationNumberHelperReportPdf")
public class StorageLocationNumberHelperReportPdf extends ReportPdfView {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("materialFlowResources.storageLocation.report.title", locale));
    }

    @Override
    protected void setPageEvent(final PdfWriter writer) {
        writer.setPageEvent(new PdfPageNumbering(new Footer(), false, false));
    }

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(Objects.nonNull(model.get("id")), "Unable to generate report for unsaved offer! (missing id)");

        Long storageLocationNumberHelperId = Long.valueOf(model.get("id").toString());

        Entity storageLocationNumberHelper = getStorageLocationNumberHelper(storageLocationNumberHelperId);

        if (Objects.nonNull(storageLocationNumberHelper)) {
            List<Entity> storageLocations = storageLocationNumberHelper.getManyToManyField(StorageLocationNumberHelperFields.STORAGE_LOCATIONS);

            addStorageLocationNumbers(document, writer, storageLocations);
        }

        return translationService.translate("materialFlowResources.storageLocation.report.fileName", locale, storageLocationNumberHelperId.toString());
    }

    private void addStorageLocationNumbers(final Document document, final PdfWriter writer, final List<Entity> storageLocations) throws DocumentException {
        int i = 0;

        for (Entity storageLocation : storageLocations) {
            Entity location = storageLocation.getBelongsToField(StorageLocationFields.LOCATION);
            String number = storageLocation.getStringField(StorageLocationFields.NUMBER);

            if (i % 2 == 0) {
                if (i == 0) {
                    Paragraph newLineParagraph = createNewLineParagraph(0F, 90F);

                    document.add(newLineParagraph);
                }

                Paragraph firstNumberParagraph = createNumberParagraph(number, 60F, 30F);
                Paragraph firstLocationParagraph = createLocationParagraph(location,0F,    110F);
                Image numberImage = createNumberImage(writer, number);
                LineSeparator lineSeparator = createLineSeparator();

                document.add(numberImage);
                document.add(firstNumberParagraph);
                document.add(firstLocationParagraph);
                document.add(lineSeparator);
            }

            if (i % 2 != 0) {
                Paragraph secondNumberParagraph = createNumberParagraph(number, 60F, 30F);
                Paragraph secondLocationParagraph = createLocationParagraph(location,0F, 60F);
                Image numberImage = createNumberImage(writer, number);
                Paragraph spacingParagraph = createNewLineParagraph(0F, 100F);

                document.add(spacingParagraph);
                document.add(numberImage);
                document.add(secondNumberParagraph);
                document.add(secondLocationParagraph);

                if (i < storageLocations.size() - 1) {
                    document.newPage();

                    Paragraph newLineParagraph = createNewLineParagraph(0F, 90F);

                    document.add(newLineParagraph);
                }
            }

            i++;
        }
    }

    private static Paragraph createNewLineParagraph(final float spacingBefore, final float spacingAfter) {
        Paragraph newLineParagraph = new Paragraph(new Phrase("\n"));

        newLineParagraph.setSpacingBefore(spacingBefore);
        newLineParagraph.setSpacingAfter(spacingAfter);

        return newLineParagraph;
    }

    private static Paragraph createNumberParagraph(final String number, final float spacingBefore, final float spacingAfter) {
        Font font;

        if (number.length() > 40) {
            font =  FontUtils.getDejavuBold14Dark();
        } else {
            font = FontUtils.getDejavuBold19Dark();
        }

        Paragraph numberParagraph = new Paragraph(new Phrase(number, font));

        numberParagraph.setAlignment(Element.ALIGN_CENTER);
        numberParagraph.setSpacingBefore(spacingBefore);
        numberParagraph.setSpacingAfter(spacingAfter);
        numberParagraph.setLeading(0, 0);

        return numberParagraph;
    }

    private static Paragraph createLocationParagraph(final Entity location, final float spacingBefore, final float spacingAfter) {
        String number = location.getStringField(LocationFields.NUMBER);
        String name = location.getStringField(LocationFields.NAME);

        String numberAndName = number + " - " + name;

        Font font;

        if (number.length() > 50) {
            font =  FontUtils.getDejavuBold11Dark();
        } else {
            font = FontUtils.getDejavuBold14Dark();
        }

        Paragraph numberAndNameParagraph = new Paragraph(new Phrase(numberAndName, font));

        numberAndNameParagraph.setAlignment(Element.ALIGN_CENTER);
        numberAndNameParagraph.setSpacingBefore(spacingBefore);
        numberAndNameParagraph.setSpacingAfter(spacingAfter);
        numberAndNameParagraph.setLeading(0, 0);

        return numberAndNameParagraph;
    }

    private Image createNumberImage(final PdfWriter writer, final String code) {
        Barcode128 code128 = new Barcode128();

        code128.setCode(code);
        code128.setBarHeight(50F);
        code128.setX(1.0F);
        code128.setSize(5F);
        code128.setFont(null);

        PdfContentByte cb = writer.getDirectContent();

        Image numberImage = code128.createImageWithBarcode(cb, null, null);

        numberImage.setAlignment(Element.ALIGN_CENTER);

        return numberImage;
    }

    private static LineSeparator createLineSeparator() {
        return new LineSeparator(1, 100F, ColorUtils.getLineDarkColor(), Element.ALIGN_LEFT, 0);
    }

    public Entity getStorageLocationNumberHelper(final Long storageLocationNumberHelperId) {
        return getStorageLocationNumberHelperDD().get(storageLocationNumberHelperId);
    }

    private DataDefinition getStorageLocationNumberHelperDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION_NUMBER_HELPER);
    }

}
