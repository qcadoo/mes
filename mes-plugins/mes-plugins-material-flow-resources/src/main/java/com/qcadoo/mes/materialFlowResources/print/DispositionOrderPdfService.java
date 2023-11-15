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
package com.qcadoo.mes.materialFlowResources.print;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.print.helper.*;
import com.qcadoo.mes.materialFlowResources.print.helper.DocumentPdfHelper.HeaderPair;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.*;

@Service
public class DispositionOrderPdfService extends PdfDocumentService {

    private static final String L_POSITION_HEADER_PREFIX = "materialFlowResources.dispositionOrder.positionsHeader.";

    private static final String L_LOCATION_FROM = "materialFlowResources.document.locationFrom.label";

    private static final String L_STATE = "materialFlowResources.document.state.label";

    private static final String L_STATE_VALUE = "materialFlowResources.document.state.value.";

    private static final String L_TIME = "materialFlowResources.document.time.label";

    private static final String L_DESCRIPTION = "materialFlowResources.document.description.label";

    private static final String L_HEADER = "materialFlowResources.dispositionOrder.header";

    private static final String L_PZ = "materialFlowResources.dispositionOrder.locationPZ";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    private String getDocumentHeader(final Entity document, final Locale locale) {
        return translationService.translate(L_HEADER, locale, document.getStringField(DocumentFields.NUMBER));
    }

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documentHeader = getDocumentHeader(entity, locale);

        pdfHelper.addDocumentHeader(document, "", documentHeader, "", new Date());

        addHeaderTable(document, entity, locale);
        addPositionsTable(document, entity, locale);
        addPlaceForComments(document, locale);
        addPlaceForSignature(document, locale);
    }

    private void addHeaderTable(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(2);
        table.setSpacingAfter(20);

        List<HeaderPair> headerValues = getDocumentHeaderTableContent(entity, locale);

        for (HeaderPair pair : headerValues) {
            if (Objects.nonNull(pair.getValue()) && !pair.getValue().isEmpty()) {
                pdfHelper.addTableCellAsOneColumnTable(table, pair.getLabel(), pair.getValue(), pair.isBoldAndBigger());
            } else {
                pdfHelper.addTableCellAsOneColumnTable(table, StringUtils.EMPTY, StringUtils.EMPTY);
            }
        }

        document.add(table);
    }

    private List<HeaderPair> getDocumentHeaderTableContent(final Entity document, final Locale locale) {
        List<HeaderPair> headerValues = Lists.newLinkedList();

        headerValues.add(new HeaderPair(translationService.translate(L_LOCATION_FROM, locale), DocumentDataProvider
                .locationFrom(document), false));
        headerValues.add(new HeaderPair(translationService.translate(L_STATE, locale), translationService.translate(L_STATE_VALUE
                + DocumentDataProvider.state(document), locale), false));
        headerValues.add(new HeaderPair(translationService.translate(L_TIME, locale), DocumentDataProvider.time(document),
                false));
        headerValues.add(new HeaderPair(translationService.translate(L_DESCRIPTION, locale), DocumentDataProvider
                .description(document), false));
        headerValues.add(new HeaderPair(translationService.translate(L_PZ, locale), DocumentDataProvider
                .pzLocation(document), true));
        headerValues.add(new HeaderPair(StringUtils.EMPTY, StringUtils.EMPTY, true));

        return headerValues;
    }

    private void addPositionsTable(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        List<Integer> headerWidthsList = Lists.newArrayList(25, 50, 50, 50, 65, 90, 40, 35);

        int numOfColumns = 8;

        int[] headerWidths = headerWidthsList.stream().mapToInt(i -> i).toArray();

        Map<String, HeaderAlignment> headerValues = getPositionsTableHeaderLabels(locale);

        PdfPTable positionsTable = pdfHelper.createTableWithHeader(numOfColumns, Lists.newArrayList(headerValues.keySet()),
                false, headerWidths, headerValues);

        positionsTable.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
        positionsTable.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
        positionsTable.setHeaderRows(1);

        PositionsHolder positionsHolder = new PositionsHolder(numberService);

        fillPositions(positionsHolder, PositionDataProvider.getPositions(entity));

        List<Position> positions = positionsHolder.getPositions();

        positions.sort((p1, p2) -> ComparisonChain.start().compare(p1.getStorageLocation(), p2.getStorageLocation()).result());

        Integer index = 1;

        for (Position position : positions) {
            positionsTable.addCell(createCell(index.toString(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getStorageLocation(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getPalletNumber(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getTypeOfPallet(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getBatch(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getProductName(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(PositionDataProvider.quantity(position.getQuantity()), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getUnit(), Element.ALIGN_LEFT));

            index++;
        }

        positionsTable.setSpacingAfter(20);

        document.add(positionsTable);
    }

    private Map<String, HeaderAlignment> getPositionsTableHeaderLabels(final Locale locale) {
        Map<String, HeaderAlignment> headerLabels = Maps.newLinkedHashMap();

        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "index", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "storageLocation", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "pallet", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "typeOfPallet", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "batch", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "product", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "quantity", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "unit", locale), HeaderAlignment.LEFT);

        return headerLabels;
    }

    private void fillPositions(final PositionsHolder positionsHolder, final List<Entity> positions) {
        for (Entity position : positions) {
            PositionBuilder builder = new PositionBuilder();

            builder.setIndex(PositionDataProvider.index(position))
                    .setStorageLocation(getDataForStorageLocation(position))
                    .setPalletNumber(PositionDataProvider.palletNumber(position))
                    .setTypeOfPallet(PositionDataProvider.typeOfPallet(position))
                    .setProductName(getDataForProduct(position))
                    .setQuantity(position.getDecimalField(PositionFields.QUANTITY))
                    .setUnit(PositionDataProvider.unit(position))
                    .setProduct(position.getBelongsToField(PositionFields.PRODUCT).getId());

            if (Objects.nonNull(position.getBelongsToField(PositionFields.BATCH))) {
                builder.setBatch(position.getBelongsToField(PositionFields.BATCH).getStringField(BatchFields.NUMBER));
            }

            positionsHolder.addPosition(builder.createPosition());
        }
    }

    private PdfPCell createCell(final String content, final int alignment) {
        PdfPCell cell = new PdfPCell();

        float border = 0.2f;

        cell.setFixedHeight(30f);
        cell.setPhrase(new Phrase(content, FontUtils.getDejavuRegular9Dark()));
        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(border);
        cell.disableBorderSide(PdfPCell.RIGHT);
        cell.disableBorderSide(PdfPCell.LEFT);
        cell.setPadding(5);

        return cell;
    }

    private void addPlaceForComments(final Document document, final Locale locale) throws DocumentException {
        PdfPTable table = new PdfPTable(1);

        table.setWidthPercentage(100f);

        Paragraph commentParagraph = new Paragraph(new Phrase(translationService.translate(
                "materialFlowResources.dispositionOrder.comments", locale), FontUtils.getDejavuBold7Dark()));

        commentParagraph.setAlignment(Element.ALIGN_LEFT);
        commentParagraph.setSpacingAfter(6f);

        document.add(commentParagraph);

        PdfPCell commentCell = new PdfPCell(new Paragraph(""));

        commentCell.setBorder(Rectangle.BOX);
        commentCell.setFixedHeight(60f);

        table.addCell(commentCell);

        document.add(table);

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
    }

    private void addPlaceForSignature(final Document document, final Locale locale) throws DocumentException {
        PdfPTable table = new PdfPTable(1);

        table.setWidthPercentage(15);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph signatureParagraph = new Paragraph(new Phrase(translationService.translate(
                "materialFlowResources.dispositionOrder.sign", locale), FontUtils.getDejavuBold7Dark()));

        signatureParagraph.setAlignment(Element.ALIGN_CENTER);

        PdfPCell signatureCell = new PdfPCell(signatureParagraph);

        signatureCell.setBorder(Rectangle.TOP);
        signatureCell.setVerticalAlignment(Rectangle.ALIGN_CENTER);
        signatureCell.setHorizontalAlignment(Rectangle.ALIGN_CENTER);

        table.addCell(signatureCell);

        document.add(table);
    }

    private String getDataForStorageLocation(final Entity position) {
        Entity storageLocation = position.getBelongsToField(PositionFields.STORAGE_LOCATION);
        Entity locationFrom = position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_FROM);

        if (Objects.isNull(storageLocation) && Objects.nonNull(locationFrom)) {
            storageLocation = materialFlowResourcesService.findStorageLocationForProduct(locationFrom, position.getBelongsToField(PositionFields.PRODUCT)).orElse(null);
        }

        return Objects.nonNull(storageLocation) ? storageLocation.getStringField(StorageLocationFields.NUMBER) : StringUtils.EMPTY;
    }

    private String getDataForProduct(final Entity position) {
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);

        return Objects.nonNull(product) ? product.getStringField(ProductFields.NAME) : StringUtils.EMPTY;
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("materialFlowResources.dispositionOrder.title", locale);
    }

}
