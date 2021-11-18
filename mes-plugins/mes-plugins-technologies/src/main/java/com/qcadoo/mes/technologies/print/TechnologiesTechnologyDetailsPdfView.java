/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.valueOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductAttachmentFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyInputProductTypeFields;
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

    private static final Logger LOG = LoggerFactory.getLogger(TechnologiesTechnologyDetailsPdfView.class);

    public static final String TECHNOLOGIES_TECHNOLOGIES_TECHNOLOGY_DETAILS_REPORT_PANEL_TECHNOLOGY = "technologies.technologiesTechnologyDetails.report.panel.technology.";

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
        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

        PdfPTable panelTable = pdfHelper.createPanelTable(3);
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate(
                        TECHNOLOGIES_TECHNOLOGIES_TECHNOLOGY_DETAILS_REPORT_PANEL_TECHNOLOGY + TechnologyFields.NAME, locale),
                technology.getStringField(TechnologyFields.NAME));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate(
                        TECHNOLOGIES_TECHNOLOGIES_TECHNOLOGY_DETAILS_REPORT_PANEL_TECHNOLOGY + TechnologyFields.NUMBER, locale),
                technology.getStringField(TechnologyFields.NUMBER));
        panelTable.addCell(createImageCell(product));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate(
                        TECHNOLOGIES_TECHNOLOGIES_TECHNOLOGY_DETAILS_REPORT_PANEL_TECHNOLOGY + TechnologyFields.PRODUCT, locale),
                product.getStringField(ProductFields.NAME));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("technologies.technologiesTechnologyDetails.report.panel.technology.default",
                        locale),
                technology.getBooleanField(TechnologyFields.MASTER) ? translationService.translate("qcadooView.true", locale)
                        : translationService.translate("qcadooView.false", locale));
        if (StringUtils.isEmpty(technology.getStringField(ProductFields.DESCRIPTION))) {
            panelTable.addCell(StringUtils.EMPTY);
        } else {
            pdfHelper.addTableCellAsOneColumnTable(panelTable,
                    translationService.translate(
                            TECHNOLOGIES_TECHNOLOGIES_TECHNOLOGY_DETAILS_REPORT_PANEL_TECHNOLOGY + TechnologyFields.DESCRIPTION,
                            locale),
                    technology.getStringField(ProductFields.DESCRIPTION));
        }
        panelTable.addCell(StringUtils.EMPTY);

        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        List<String> technologyDetailsTableHeader = new ArrayList<>();
        technologyDetailsTableHeader.add(
                translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.level", locale));
        technologyDetailsTableHeader
                .add(translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.name", locale));
        technologyDetailsTableHeader.add(
                translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.direction", locale));
        technologyDetailsTableHeader.add(translationService.translate(
                "technologies.technologiesTechnologyDetails.report.columnHeader.technologyInputProductTypeName", locale));
        technologyDetailsTableHeader.add(translationService
                .translate("technologies.technologiesTechnologyDetails.report.columnHeader.productNumber", locale));
        technologyDetailsTableHeader.add(translationService
                .translate("technologies.technologiesTechnologyDetails.report.columnHeader.productName", locale));
        technologyDetailsTableHeader.add(
                translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.quantity", locale));
        technologyDetailsTableHeader
                .add(translationService.translate("technologies.technologiesTechnologyDetails.report.columnHeader.unit", locale));
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
        alignments.put(
                translationService.translate(
                        "technologies.technologiesTechnologyDetails.report.columnHeader.technologyInputProductTypeName", locale),
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

        PdfPTable table = pdfHelper.createTableWithHeader(8, technologyDetailsTableHeader, false, alignments);

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

            addProducts(locale, table, nodeNumber, operationName, operationProductComponents);
        }

        document.add(table);

        return translationService.translate("technologies.technologiesTechnologyDetails.report.fileName", locale);
    }

    private void addProducts(Locale locale, PdfPTable table, String nodeNumber, String operationName,
            List<Entity> operationProductComponents) {
        for (Entity operationProductComponent : operationProductComponents) {
            table.getDefaultCell().enableBorderSide(PdfCell.TOP);
            table.getDefaultCell().enableBorderSide(PdfCell.BOTTOM);
            Entity product = operationProductComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

            String productType = "technologies.technologiesTechnologyDetails.report.direction.out";
            String productNumber = StringUtils.EMPTY;
            String productName = StringUtils.EMPTY;
            String productUnit = StringUtils.EMPTY;
            if (product != null) {
                productNumber = product.getStringField(ProductFields.NUMBER);
                productName = product.getStringField(ProductFields.NAME);
                productUnit = product.getStringField(ProductFields.UNIT);
            }
            String technologyInputProductTypeName = StringUtils.EMPTY;
            List<Entity> productBySizeGroups = Lists.newArrayList();

            if (operationProductComponent.getDataDefinition().getName().equals("operationProductInComponent")) {
                productType = "technologies.technologiesTechnologyDetails.report.direction.in";
                Entity technologyInputProductType = operationProductComponent
                        .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
                if (technologyInputProductType != null) {
                    productUnit = operationProductComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT);
                    technologyInputProductTypeName = technologyInputProductType
                            .getStringField(TechnologyInputProductTypeFields.NAME);
                }
                if (operationProductComponent
                        .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES)) {
                    productNumber = translationService
                            .translate("technologies.technologiesTechnologyDetails.report.productsBySize.label", locale);
                    productBySizeGroups = operationProductComponent
                            .getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS);
                    table.getDefaultCell().disableBorderSide(PdfCell.TOP);
                    table.getDefaultCell().disableBorderSide(PdfCell.BOTTOM);
                }
            }

            table.addCell(new Phrase(nodeNumber, FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(operationName, FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(translationService.translate(productType, locale), FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(technologyInputProductTypeName, FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(productNumber, FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(productName, FontUtils.getDejavuRegular7Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(
                    numberService.format(operationProductComponent.getField(OperationProductInComponentFields.QUANTITY)),
                    FontUtils.getDejavuRegular7Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(new Phrase(productUnit, FontUtils.getDejavuRegular7Dark()));
            addProductsBySize(table, productBySizeGroups);
        }
    }

    private void addProductsBySize(PdfPTable table, List<Entity> productBySizeGroups) {
        for (Entity productBySizeGroup : productBySizeGroups) {
            Entity productBySize = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
            table.addCell(new Phrase(StringUtils.EMPTY));
            table.addCell(new Phrase(StringUtils.EMPTY));
            table.addCell(new Phrase(StringUtils.EMPTY));
            table.addCell(new Phrase(StringUtils.EMPTY));
            table.addCell(new Phrase(productBySize.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            table.addCell(new Phrase(productBySize.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(numberService.format(productBySizeGroup.getDecimalField(ProductBySizeGroupFields.QUANTITY)),
                    FontUtils.getDejavuRegular7Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(new Phrase(productBySize.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Dark()));
        }
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("technologies.technologiesTechnologyDetails.report.title", locale));
    }

    private PdfPCell createImageCell(Entity product) {
        PdfPCell cell;
        String fileName = null;
        for (Entity productAttachment : product.getHasManyField(ProductFields.PRODUCT_ATTACHMENTS)) {
            String ext = productAttachment.getStringField(ProductAttachmentFields.EXT);
            if ("JPG".equalsIgnoreCase(ext) || "JPEG".equalsIgnoreCase(ext) || "PNG".equalsIgnoreCase(ext)) {
                fileName = productAttachment.getStringField(ProductAttachmentFields.ATTACHMENT);
                break;
            }
        }
        if (fileName != null) {
            try {
                Image img = Image.getInstance(fileName);
                if (img.getWidth() > 150 || img.getHeight() > 90) {
                    img.scaleToFit(150, 90);
                }

                cell = new PdfPCell(img);
            } catch (IOException | DocumentException e) {
                LOG.error(e.getMessage(), e);
                cell = new PdfPCell(new Phrase(StringUtils.EMPTY));
            }
        } else {
            cell = new PdfPCell(new Phrase(StringUtils.EMPTY));
        }
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(4.0f);
        cell.setRowspan(3);
        return cell;
    }

}
