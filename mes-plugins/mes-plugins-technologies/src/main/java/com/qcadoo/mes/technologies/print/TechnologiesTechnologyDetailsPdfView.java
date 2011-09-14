/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.7
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.print.utils.EntityOperationProductInOutComponentComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

public class TechnologiesTechnologyDetailsPdfView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected final String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        String documentTitle = getTranslationService().translate("technologies.technologiesTechnologyDetails.report.title",
                locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        DataDefinition dataDefTechnology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        Entity technologies = dataDefTechnology.find("where id = " + model.get("id").toString()).uniqueResult();

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate("technologies.technologiesTechnologyDetails.report.panel.technology.name",
                        locale), technologies.getStringField("name"), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate("technologies.technologiesTechnologyDetails.report.panel.technology.number",
                        locale), technologies.getStringField("number"), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate("technologies.technologiesTechnologyDetails.report.panel.technology.product",
                        locale), technologies.getBelongsToField("product").getStringField("name"), null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate("technologies.technologiesTechnologyDetails.report.panel.technology.default",
                        locale),
                (Boolean) technologies.getField("master") ? getTranslationService().translate("qcadooView.true", locale)
                        : getTranslationService().translate("qcadooView.false", locale), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate("technologies.technologiesTechnologyDetails.report.panel.technology.algorithm",
                        locale),
                getTranslationService().translate(
                        "technologies.technology.componentQuantityAlgorithm.value."
                                + technologies.getStringField("componentQuantityAlgorithm"), locale), null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(
                panelTable,
                getTranslationService().translate(
                        "technologies.technologiesTechnologyDetails.report.panel.technology.description", locale),
                technologies.getStringField("description"), null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        List<String> technologyDetailsTableHeader = new ArrayList<String>();
        technologyDetailsTableHeader.add(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.name", locale));
        technologyDetailsTableHeader.add(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.level", locale));
        technologyDetailsTableHeader.add(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.direction", locale));
        technologyDetailsTableHeader.add(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.product", locale));
        technologyDetailsTableHeader.add(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.quantity", locale));
        technologyDetailsTableHeader.add(getTranslationService().translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.unit", locale));
        PdfPTable table = PdfUtil.createTableWithHeader(6, technologyDetailsTableHeader, false);

        DataDefinition dataDefOperationProductInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
        DataDefinition dataDefOperationProductOutComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
        List<Entity> technologyDetailsTableContent = new ArrayList<Entity>();
        technologyDetailsTableContent.addAll(dataDefOperationProductInComponent
                .find("where operationComponent.technology.id = " + model.get("id").toString()).list().getEntities());
        technologyDetailsTableContent.addAll(technologyDetailsTableContent.size(),
                dataDefOperationProductOutComponent
                        .find("where operationComponent.technology.id = " + model.get("id").toString()).list().getEntities());
        Collections.sort(technologyDetailsTableContent, new EntityOperationProductInOutComponentComparator());

        for (Entity e : technologyDetailsTableContent) {
            table.addCell(new Phrase(e.getBelongsToField("operationComponent").getBelongsToField("operation")
                    .getStringField("name"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField("operationComponent").getBelongsToField("operation").getId().toString(),
                    PdfUtil.getArialRegular9Dark()));
            if (e.getDataDefinition().getName().toString().equals("operationProductInComponent"))
                table.addCell(new Phrase(getTranslationService().translate(
                        "technologies.technologiesTechnologyDetails.report.direction.in", locale), PdfUtil.getArialRegular9Dark()));
            else
                table.addCell(new Phrase(getTranslationService().translate(
                        "technologies.technologiesTechnologyDetails.report.direction.out", locale), PdfUtil
                        .getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField("product").getStringField("name"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getField("quantity").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField("product").getStringField("unit"), PdfUtil.getArialRegular9Dark()));
        }

        document.add(table);

        String text = getTranslationService().translate("qcadooReport.commons.endOfPrint.label", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("technologies.technologiesTechnologyDetails.report.fileName", locale);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("technologies.technologiesTechnologyDetails.report.title", locale));
    }

}
