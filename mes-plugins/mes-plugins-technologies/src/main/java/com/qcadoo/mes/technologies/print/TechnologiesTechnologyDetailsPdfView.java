/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.FIELD_NAME;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_BASIC_PRODUCT;
import static com.qcadoo.model.api.types.TreeType.NODE_NUMBER_FIELD;
import static java.lang.Long.valueOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "technologiesTechnologyDetailsPdfView")
public class TechnologiesTechnologyDetailsPdfView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected final String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved technology! (missing id)");

        String documentTitle = translationService.translate("technologies.technologiesTechnologyDetails.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        Entity technology = technologyDD.get(valueOf((model.get("id").toString())));
        Map<String, String> panelTableValues = newLinkedHashMap();
        panelTableValues.put(FIELD_NAME, technology.getStringField(FIELD_NAME));
        panelTableValues.put("number", technology.getStringField("number"));
        panelTableValues.put(MODEL_BASIC_PRODUCT, technology.getBelongsToField(MODEL_BASIC_PRODUCT).getStringField(FIELD_NAME));
        panelTableValues.put("default",
                (Boolean) technology.getField("master") ? translationService.translate("qcadooView.true", locale)
                        : translationService.translate("qcadooView.false", locale));

        panelTableValues.put("description", technology.getStringField("description"));

        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        for (Map.Entry<String, String> panelEntry : panelTableValues.entrySet()) {
            pdfHelper.addTableCellAsOneColumnTable(
                    panelTable,
                    translationService.translate("technologies.technologiesTechnologyDetails.report.panel.technology."
                            + panelEntry.getKey(), locale), panelEntry.getValue());
        }

        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        List<String> technologyDetailsTableHeader = new ArrayList<String>();
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.level", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.name", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.direction", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.product", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.quantity", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.unit", locale));
        PdfPTable table = pdfHelper.createTableWithHeader(6, technologyDetailsTableHeader, false);

        EntityTree technologyTree = technology.getTreeField("operationComponents");
        treeNumberingService.generateTreeNumbers(technologyTree);

        List<Entity> technologyOperationsList = Lists.newLinkedList(technologyTree);
        Collections.sort(technologyOperationsList, treeNumberingService.getTreeNodesNumberComparator());

        for (Entity technologyOperation : technologyOperationsList) {
            String nodeNumber = technologyOperation.getStringField(NODE_NUMBER_FIELD);
            String operationName = technologyOperation.getBelongsToField("operation").getStringField(FIELD_NAME);
            List<Entity> technologyOperationProducts = newArrayList();
            technologyOperationProducts.addAll(technologyOperation.getHasManyField("operationProductInComponents"));
            technologyOperationProducts.addAll(technologyOperation.getHasManyField("operationProductOutComponents"));

            for (Entity product : technologyOperationProducts) {
                String productType = "technologies.technologiesTechnologyDetails.report.direction.out";
                if (product.getDataDefinition().getName().equals("operationProductInComponent")) {
                    productType = "technologies.technologiesTechnologyDetails.report.direction.in";
                }
                table.addCell(new Phrase(nodeNumber, FontUtils.getDejavuRegular9Dark()));
                table.addCell(new Phrase(operationName, FontUtils.getDejavuRegular9Dark()));
                table.addCell(new Phrase(translationService.translate(productType, locale), FontUtils.getDejavuRegular9Dark()));
                table.addCell(new Phrase(product.getBelongsToField(MODEL_BASIC_PRODUCT).getStringField(FIELD_NAME), FontUtils
                        .getDejavuRegular9Dark()));
                table.addCell(new Phrase(numberService.format(product.getField("quantity")), FontUtils.getDejavuRegular9Dark()));
                table.addCell(new Phrase(product.getBelongsToField(MODEL_BASIC_PRODUCT).getStringField("unit"), FontUtils
                        .getDejavuRegular9Dark()));
            }
        }

        document.add(table);

        String text = translationService.translate("qcadooReport.commons.endOfPrint.label", locale);
        pdfHelper.addEndOfDocument(document, writer, text);
        return translationService.translate("technologies.technologiesTechnologyDetails.report.fileName", locale);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("technologies.technologiesTechnologyDetails.report.title", locale));
    }

}
