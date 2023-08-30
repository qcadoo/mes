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
import com.lowagie.text.pdf.*;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.print.helper.*;
import com.qcadoo.mes.materialFlowResources.print.helper.DocumentPdfHelper.HeaderPair;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ColorUtils;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentWithWriterService;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.*;

@Service
public class DispositionOrderPdfService extends PdfDocumentWithWriterService {

    private static final String L_POSITION_HEADER_PREFIX = "materialFlowResources.dispositionOrder.positionsHeader.";

    private static final String L_LOCATION_FROM = "materialFlowResources.document.locationFrom.label";

    private static final String L_STATE = "materialFlowResources.document.state.label";

    private static final String L_STATE_VALUE = "materialFlowResources.document.state.value.";

    private static final String L_TIME = "materialFlowResources.document.time.label";

    private static final String L_DESCRIPTION = "materialFlowResources.document.description.label";

    private static final String L_HEADER = "materialFlowResources.dispositionOrder.header";

    private static final String L_PZ = "materialFlowResources.dispositionOrder.locationPZ";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    private boolean acceptanceOfDocumentBeforePrinting;

    private void addHeaderTable(Document document, Entity documentEntity, Locale locale) throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(2);
        table.setSpacingAfter(20);

        List<HeaderPair> headerValues = getDocumentHeaderTableContent(documentEntity, locale);
        for (HeaderPair pair : headerValues) {
            if (pair.getValue() != null && !pair.getValue().isEmpty()) {
                pdfHelper.addTableCellAsOneColumnTable(table, pair.getLabel(), pair.getValue(), pair.isBoldAndBigger());
            } else {
                pdfHelper.addTableCellAsOneColumnTable(table, StringUtils.EMPTY, StringUtils.EMPTY);
            }
        }

        document.add(table);

    }

    private void addPositionsTable(Document document, Entity documentEntity, Locale locale) throws DocumentException {
        List<Integer> headerWidthsList = new ArrayList<>(Arrays.asList(25, 50, 50, 50, 65, 90, 40, 35));
        int numOfColumns = 8;
        if (acceptanceOfDocumentBeforePrinting) {
            headerWidthsList.add(45);
            numOfColumns++;
        }
        int[] headerWidths = headerWidthsList.stream().mapToInt(i -> i).toArray();

        Map<String, HeaderAlignment> headerValues = getPositionsTableHeaderLabels(locale);
        PdfPTable positionsTable = pdfHelper.createTableWithHeader(numOfColumns, Lists.newArrayList(headerValues.keySet()),
                false, headerWidths, headerValues);
        positionsTable.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
        positionsTable.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
        positionsTable.setHeaderRows(1);
        List<Entity> positions = PositionDataProvider.getPositions(documentEntity);
        PositionsHolder positionsHolder = new PositionsHolder(numberService);
        fillPositions(positionsHolder, positions);
        List<Position> _positions = positionsHolder.getPositions();
        if (acceptanceOfDocumentBeforePrinting) {
            Collections.sort(_positions, new Comparator<Position>() {
                @Override
                public int compare(Position p1, Position p2) {
                    return ComparisonChain.start().compare(p1.getTargetPallet(), p2.getTargetPallet())
                            .compare(p1.getStorageLocation(), p2.getStorageLocation()).result();
                }
            });
        } else {
            Collections.sort(_positions, new Comparator<Position>() {

                @Override
                public int compare(Position p1, Position p2) {
                    return ComparisonChain.start().compare(p1.getStorageLocation(), p2.getStorageLocation()).result();
                }
            });
        }
        Integer index = 1;
        for (Position position : _positions) {
            positionsTable.addCell(createCell(index.toString(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getStorageLocation(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getPalletNumber(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getTypeOfPallet(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getBatch(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getProductName(), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(PositionDataProvider.quantity(position.getQuantity()), Element.ALIGN_LEFT));
            positionsTable.addCell(createCell(position.getUnit(), Element.ALIGN_LEFT));

            if (acceptanceOfDocumentBeforePrinting) {
                positionsTable.addCell(createCell(position.getTargetPallet(), Element.ALIGN_LEFT));
            }
            index++;
        }

        positionsTable.setSpacingAfter(20);

        document.add(positionsTable);
    }

    private void fillPositions(PositionsHolder positionsHolder, List<Entity> positions) {
        for (Entity position : positions) {
            PositionBuilder builder = new PositionBuilder();
            builder.setIndex(PositionDataProvider.index(position)).setStorageLocation(getDataForStorageLocation(position))
                    .setPalletNumber(PositionDataProvider.palletNumber(position))
                    .setTypeOfPallet(PositionDataProvider.typeOfPallet(position))
                    .setProductName(getDataForProduct(position))
                    .setQuantity(position.getDecimalField(PositionFields.QUANTITY)).setUnit(PositionDataProvider.unit(position))
                    .setProduct(position.getBelongsToField(PositionFields.PRODUCT).getId());
            if(Objects.nonNull(position.getBelongsToField(PositionFields.BATCH))) {
                builder.setBatch(position.getBelongsToField(PositionFields.BATCH).getStringField(BatchFields.NUMBER));
            }

            if (acceptanceOfDocumentBeforePrinting) {
                builder.setTargetPallet(getDataForTargetPallet(position));
            }
            positionsHolder.addPosition(builder.createPosition());
        }
    }

    private PdfPCell createCell(String content, int alignment) {
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(30f);
        float border = 0.2f;
        cell.setPhrase(new Phrase(content, FontUtils.getDejavuRegular9Dark()));
        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(border);
        cell.disableBorderSide(PdfPCell.RIGHT);
        cell.disableBorderSide(PdfPCell.LEFT);
        cell.setPadding(5);
        return cell;
    }

    private void addPlaceForComments(Document document, Locale locale) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);

        Paragraph paragraph = new Paragraph(new Phrase(translationService.translate(
                "materialFlowResources.dispositionOrder.comments", locale), FontUtils.getDejavuBold7Dark()));
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setSpacingAfter(6f);
        document.add(paragraph);

        PdfPCell cell1 = new PdfPCell(new Paragraph(""));
        cell1.setBorder(Rectangle.BOX);
        cell1.setFixedHeight(60f);
        table.addCell(cell1);

        document.add(table);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
    }

    private void addPlaceForSignature(final Document document, final Locale locale) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(15);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph signParagraph = new Paragraph(new Phrase(translationService.translate(
                "materialFlowResources.dispositionOrder.sign", locale), FontUtils.getDejavuBold7Dark()));
        signParagraph.setAlignment(Element.ALIGN_CENTER);
        PdfPCell cell1 = new PdfPCell(signParagraph);
        cell1.setBorder(Rectangle.TOP);
        cell1.setVerticalAlignment(Rectangle.ALIGN_CENTER);
        cell1.setHorizontalAlignment(Rectangle.ALIGN_CENTER);

        table.addCell(cell1);

        document.add(table);
    }

    private Map<String, HeaderAlignment> getPositionsTableHeaderLabels(Locale locale) {
        Map<String, HeaderAlignment> headerLabels = Maps.newLinkedHashMap();

        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "index", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "batch", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "pallet", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "typeOfPallet", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "batch", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "product", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "quantity", locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "unit", locale), HeaderAlignment.LEFT);
        if (acceptanceOfDocumentBeforePrinting) {
            headerLabels.put(translationService.translate(L_POSITION_HEADER_PREFIX + "targetPallet", locale),
                    HeaderAlignment.LEFT);
        }

        return headerLabels;
    }

    private List<HeaderPair> getDocumentHeaderTableContent(final Entity documentEntity, final Locale locale) {
        List<HeaderPair> headerValues = Lists.newLinkedList();

        headerValues.add(new HeaderPair(translationService.translate(L_LOCATION_FROM, locale), DocumentDataProvider
                .locationFrom(documentEntity), false));
        headerValues.add(new HeaderPair(translationService.translate(L_STATE, locale), translationService.translate(L_STATE_VALUE
                + DocumentDataProvider.state(documentEntity), locale), false));
        headerValues.add(new HeaderPair(translationService.translate(L_TIME, locale), DocumentDataProvider.time(documentEntity),
                false));
        headerValues.add(new HeaderPair(translationService.translate(L_DESCRIPTION, locale), DocumentDataProvider
                .description(documentEntity), false));
        headerValues.add(new HeaderPair(translationService.translate(L_PZ, locale), DocumentDataProvider
                .pzLocation(documentEntity), true));
        headerValues.add(new HeaderPair(StringUtils.EMPTY, StringUtils.EMPTY, true));
        return headerValues;
    }

    private String getDataForTargetPallet(Entity position) {
        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
        Entity locationFrom = position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_FROM);
        Entity palletNumber = position.getBelongsToField(PositionFields.PALLET_NUMBER);

        if (palletNumber == null) {
            return "";
        }

        SearchCriterion criterionLocation = SearchRestrictions.belongsTo(ResourceFields.LOCATION, locationFrom);
        SearchCriterion criterionPallet = SearchRestrictions.belongsTo(ResourceFields.PALLET_NUMBER, palletNumber);

        long count = resourceDD.count(SearchRestrictions.and(criterionLocation, criterionPallet));

        return count > 0 ? "N" : palletNumber.getStringField(PalletNumberFields.NUMBER);
    }

    private String getDataForStorageLocation(Entity position) {
        Entity storageLocation = position.getBelongsToField(PositionFields.STORAGE_LOCATION);
        Entity locationFrom = position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_FROM);

        if (storageLocation == null && locationFrom != null) {
            DataDefinition storageLocationDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
            SearchCriterion criterionLocation = SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, locationFrom);
            SearchCriterion criterionProduct = SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT,
                    position.getBelongsToField(PositionFields.PRODUCT));
            storageLocation = storageLocationDD.find().add(SearchRestrictions.and(criterionLocation, criterionProduct))
                    .uniqueResult();
        }

        return storageLocation == null ? "" : storageLocation.getStringField(StorageLocationFields.NUMBER);
    }

    private String getDataForProduct(Entity position) {
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        return product != null ? product.getStringField(ProductFields.NAME) : StringUtils.EMPTY;
    }

    private String getDocumentHeader(final Entity documentEntity, final Locale locale) {
        return translationService.translate(L_HEADER, locale, documentEntity.getStringField(DocumentFields.NUMBER));
    }

    @Override
    protected void buildPdfContent(PdfWriter writer, Document document, Entity entity, Locale locale) throws DocumentException {
        Entity documentPositionParameters = parameterService.getParameter().getBelongsToField("documentPositionParameters");
        acceptanceOfDocumentBeforePrinting = documentPositionParameters.getBooleanField("acceptanceOfDocumentBeforePrinting");

        class DispositionOrderHeader extends PdfPageEventHelper {

            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                try {
                    PdfContentByte cb = writer.getDirectContent();
                    cb.saveState();
                    float textBase = document.top();

                    cb.setColorFill(ColorUtils.getLightColor());
                    cb.setColorStroke(ColorUtils.getLightColor());
                    cb.beginText();
                    cb.setFontAndSize(FontUtils.getDejavu(), 7);

                    cb.setTextMatrix(document.left(), textBase + 20);
                    cb.showText((translationService.translate(L_PZ, locale) + ": " + DocumentDataProvider.pzLocation(entity)));
                    cb.endText();
                    cb.stroke();
                    cb.restoreState();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        }
        writer.setPageEvent(new DispositionOrderHeader());

        String documentHeader = getDocumentHeader(entity, locale);
        pdfHelper.addDocumentHeader(document, "", documentHeader, "", new Date());
        addHeaderTable(document, entity, locale);
        addPositionsTable(document, entity, locale);
        addPlaceForComments(document, locale);
        addPlaceForSignature(document, locale);
    }

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("materialFlowResources.dispositionOrder.title", locale);
    }
}
