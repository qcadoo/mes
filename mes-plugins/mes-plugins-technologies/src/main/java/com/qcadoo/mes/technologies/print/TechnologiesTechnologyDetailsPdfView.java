/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import static java.lang.Long.valueOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "technologiesTechnologyDetailsPdfView")
public class TechnologiesTechnologyDetailsPdfView extends ReportPdfView {

    private static final String L_PRODUCT = "product";

    private static final String L_QUANTITY = "quantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

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

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        Entity technology = technologyDD.get(valueOf(model.get("id").toString()));

        Map<String, String> panelTableValues = newLinkedHashMap();
        panelTableValues.put(TechnologyFields.NAME, technology.getStringField(TechnologyFields.NAME));
        panelTableValues.put(TechnologyFields.NUMBER, technology.getStringField(TechnologyFields.NUMBER));
        panelTableValues.put(TechnologyFields.PRODUCT,
                technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(ProductFields.NAME));
        panelTableValues.put("default",
                technology.getBooleanField(TechnologyFields.MASTER) ? translationService.translate("qcadooView.true", locale)
                        : translationService.translate("qcadooView.false", locale));

        panelTableValues.put(TechnologyFields.DESCRIPTION, technology.getStringField(TechnologyFields.DESCRIPTION));

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
                "technologies.technologiesTechnologyDetails.report.columnHeader.productNumber", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.productName", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.quantity", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.unit", locale));
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();
        alignments.put(
                translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.level", locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.name", locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.direction", locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.productNumber", locale), HeaderAlignment.LEFT);
        alignments.put(translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.productName",
                locale), HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.quantity", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.unit", locale),
                HeaderAlignment.LEFT);

        PdfPTable table = pdfHelper.createTableWithHeader(7, technologyDetailsTableHeader, false, alignments);

        EntityTree technologyTree = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        treeNumberingService.generateTreeNumbers(technologyTree);

        List<Entity> technologyOperationComponents = entityTreeUtilsService.getSortedEntities(technologyTree);

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            String nodeNumber = technologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);
            String operationName = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                    .getStringField(OperationFields.NAME);

            List<Entity> operationProductComponents = newArrayList();

            operationProductComponents.addAll(technologyOperationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS));
            operationProductComponents.addAll(technologyOperationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS));

            for (Entity operationProductComponent : operationProductComponents) {
                Entity product = operationProductComponent.getBelongsToField(L_PRODUCT);

                String productType = "technologies.technologiesTechnologyDetails.report.direction.out";

                if (operationProductComponent.getDataDefinition().getName().equals("operationProductInComponent")) {
                    productType = "technologies.technologiesTechnologyDetails.report.direction.in";
                }

                table.addCell(new Phrase(nodeNumber, FontUtils.getDejavuRegular7Dark()));
                table.addCell(new Phrase(operationName, FontUtils.getDejavuRegular7Dark()));
                table.addCell(new Phrase(translationService.translate(productType, locale), FontUtils.getDejavuRegular7Dark()));
<<<<<<< HEAD
                table.addCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
                table.addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Dark()));
                table.addCell(new Phrase(numberService.format(operationProductComponent.getField(L_QUANTITY)), FontUtils
=======
                table.addCell(new Phrase(product.getBelongsToField(MODEL_BASIC_PRODUCT).getStringField(ProductFields.NUMBER),
                        FontUtils.getDejavuRegular7Dark()));
                table.addCell(new Phrase(product.getBelongsToField(MODEL_BASIC_PRODUCT).getStringField(FIELD_NAME), FontUtils
                        .getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase(numberService.format(product.getField("quantity")), FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(new Phrase(product.getBelongsToField(MODEL_BASIC_PRODUCT).getStringField("unit"), FontUtils
>>>>>>> master
                        .getDejavuRegular7Dark()));
                table.addCell(new Phrase(product.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Dark()));
            }
        }

        document.add(table);

        return translationService.translate("technologies.technologiesTechnologyDetails.report.fileName", locale);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("technologies.technologiesTechnologyDetails.report.title", locale));
    }

}
