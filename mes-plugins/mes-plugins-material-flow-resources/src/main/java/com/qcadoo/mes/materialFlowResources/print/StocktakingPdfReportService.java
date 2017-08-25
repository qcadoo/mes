package com.qcadoo.mes.materialFlowResources.print;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.print.helper.ResourceDataProvider;
import com.qcadoo.mes.materialFlowResources.print.helper.ResourceDto;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StocktakingPdfReportService extends PdfDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(StocktakingPdfReportService.class);

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ResourceDataProvider resourceDataProvider;

    @Autowired
    private NumberService numberService;

    @Override
    protected void buildPdfContent(Document document, Entity entity, Locale locale) throws DocumentException {

        appendDocumentHeader(document, locale);
        appendDocumentContextTable(document, entity, locale);
        appendDocumentData(document, entity, locale);
    }

    private void appendDocumentData(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        PdfPTable dataTable = prepareDataTable(locale);
        dataTable.setHeaderRows(1);
        List<Long> storageLocationIdsToQuery = Lists.newArrayList();
        List<Entity> storageLocations = entity.getHasManyField(StocktakingFields.STORAGE_LOCATIONS);
        if (!storageLocations.isEmpty()) {
            storageLocationIdsToQuery = storageLocations.stream().map(e -> e.getId()).collect(Collectors.toList());
        }
        String currentStorageLocation = StringUtils.EMPTY;
        List<ResourceDto> resourceDtos = resourceDataProvider.findResourcesByLocation(
                entity.getBelongsToField(StocktakingFields.LOCATION).getId(), storageLocationIdsToQuery,
                entity.getStringField("category"), entity.getStringField("wasteMode"), true);
        for (ResourceDto resource : resourceDtos) {
            dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            String storageLocation = resource.getStorageLocationNumber();
            if (!storageLocation.equals(currentStorageLocation)) {
                currentStorageLocation = storageLocation;
                dataTable.addCell(new Phrase(currentStorageLocation, FontUtils.getDejavuBold10Dark()));

            } else {
                dataTable.addCell(new Phrase("", FontUtils.getDejavuBold10Dark()));
            }
            dataTable.addCell(new Phrase(extractPalletNumber(resource), FontUtils.getDejavuRegular10Dark()));
            dataTable.addCell(new Phrase(extractProductNumberAndCode(resource), FontUtils.getDejavuRegular10Dark()));
            dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            dataTable.addCell(new Phrase(extractProductName(resource), FontUtils.getDejavuRegular10Dark()));
            dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            dataTable.addCell(new Phrase(extractConversion(resource), FontUtils.getDejavuRegular10Dark()));
            dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            dataTable.addCell(new Phrase(extractExpirationDate(resource), FontUtils.getDejavuRegular10Dark()));
            dataTable.getDefaultCell().enableBorderSide(Rectangle.LEFT);
            dataTable.getDefaultCell().enableBorderSide(Rectangle.RIGHT);
            dataTable.addCell(new Phrase("", FontUtils.getDejavuRegular10Dark()));
            dataTable.getDefaultCell().disableBorderSide(Rectangle.LEFT);
            dataTable.getDefaultCell().disableBorderSide(Rectangle.RIGHT);
        }
        document.add(dataTable);

    }

    private String extractExpirationDate(ResourceDto resource) {
        return Objects.isNull(resource.getExpirationDate()) ? "" : DateUtils.toDateString(resource.getExpirationDate());
    }

    private String extractConversion(ResourceDto resource) {
        return Objects.isNull(resource.getConversion()) ? "" : numberService.format(resource.getConversion());
    }

    private String extractProductName(ResourceDto resource) {
        return StringUtils.isNoneBlank(resource.getProductName()) ? resource.getProductName().substring(0,
                Math.min(18, resource.getProductName().length())) : "";
    }

    private String extractPalletNumber(ResourceDto resource) {
        return StringUtils.isNoneBlank(resource.getPalletNumberNumber()) ? resource.getPalletNumberNumber() : "";
    }

    private String extractProductNumberAndCode(ResourceDto resource) {
        StringBuilder text = new StringBuilder("");
        text.append(resource.getProductNumber());
        if (StringUtils.isNoneBlank(resource.getAdditionalCodeCode())) {
            text.append("\n");
            text.append(resource.getAdditionalCodeCode());
        }
        return text.toString();
    }

    private PdfPTable prepareDataTable(Locale locale) {
        List<String> header = Lists.newArrayList();

        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        header.add(translationService.translate("materialFlowResources.stocktaking.report.data.storageLocation", locale));
        alignments.put(translationService.translate("materialFlowResources.stocktaking.report.data.storageLocation", locale),
                HeaderAlignment.RIGHT);

        header.add(translationService.translate("materialFlowResources.stocktaking.report.data.pallet", locale));
        alignments.put(translationService.translate("materialFlowResources.stocktaking.report.data.pallet", locale),
                HeaderAlignment.RIGHT);

        header.add(translationService.translate("materialFlowResources.stocktaking.report.data.productNumberAndCode", locale));
        alignments.put(
                translationService.translate("materialFlowResources.stocktaking.report.data.productNumberAndCode", locale),
                HeaderAlignment.RIGHT);

        header.add(translationService.translate("materialFlowResources.stocktaking.report.data.productName", locale));
        alignments.put(translationService.translate("materialFlowResources.stocktaking.report.data.productName", locale),
                HeaderAlignment.LEFT);

        header.add(translationService.translate("materialFlowResources.stocktaking.report.data.conversion", locale));
        alignments.put(translationService.translate("materialFlowResources.stocktaking.report.data.conversion", locale),
                HeaderAlignment.RIGHT);

        header.add(translationService.translate("materialFlowResources.stocktaking.report.data.expirationDate", locale));
        alignments.put(translationService.translate("materialFlowResources.stocktaking.report.data.expirationDate", locale),
                HeaderAlignment.LEFT);

        header.add(translationService.translate("materialFlowResources.stocktaking.report.data.quantity", locale));
        alignments.put(translationService.translate("materialFlowResources.stocktaking.report.data.quantity", locale),
                HeaderAlignment.LEFT);

        int[] columnWidths = { 70, 70, 90, 170, 60, 100, 80 };

        return pdfHelper.createTableWithHeader(7, header, false, columnWidths, alignments);
    }

    private void appendDocumentContextTable(final Document document, final Entity entity, final Locale locale)
            throws DocumentException {
        PdfPTable dynamicHeaderTable = pdfHelper.createPanelTable(2);
        dynamicHeaderTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable firstColumnHeaderTable = new PdfPTable(1);
        PdfPTable secondColumnHeaderTable = new PdfPTable(1);

        setSimpleFormat(firstColumnHeaderTable);
        setSimpleFormat(secondColumnHeaderTable);

        dynamicHeaderTable.setSpacingBefore(5);

        Map<String, Object> firstColumn = new LinkedHashMap<String, Object>();
        Map<String, Object> secondColumn = new LinkedHashMap<String, Object>();

        firstColumn.put("materialFlowResources.stocktaking.report.number", entity.getStringField(StocktakingFields.NUMBER));

        firstColumn.put("materialFlowResources.stocktaking.report.stocktakingDate",
                DateUtils.toDateString(entity.getDateField(StocktakingFields.STOCKTAKING_DATE)));
        firstColumn.put("materialFlowResources.stocktaking.report.category", entity.getStringField("category"));

        secondColumn.put("materialFlowResources.stocktaking.report.location", entity
                .getBelongsToField(StocktakingFields.LOCATION).getStringField(LocationFields.NUMBER));

        secondColumn.put(
                "materialFlowResources.stocktaking.report.storageLocationMode",
                translationService.translate(
                        "materialFlowResources.warehouseStockReport.storageLocationMode.value."
                                + entity.getStringField(StocktakingFields.STORAGE_LOCATION_MODE), locale));
        secondColumn.put("materialFlowResources.stocktaking.report.wasteMode", translationService.translate(
                "materialFlowResources.warehouseStockReport.wasteMode.value." + entity.getStringField("wasteMode"), locale));

        int maxSize = pdfHelper.getMaxSizeOfColumnsRows(Lists.newArrayList(Integer.valueOf(firstColumn.values().size()),
                Integer.valueOf(secondColumn.values().size())));

        for (int i = 0; i < maxSize; i++) {
            firstColumnHeaderTable = pdfHelper.addDynamicHeaderTableCellOneRow(firstColumnHeaderTable, firstColumn, locale);
            secondColumnHeaderTable = pdfHelper.addDynamicHeaderTableCellOneRow(secondColumnHeaderTable, secondColumn, locale);
        }

        dynamicHeaderTable.addCell(firstColumnHeaderTable);
        dynamicHeaderTable.addCell(secondColumnHeaderTable);

        document.add(dynamicHeaderTable);
    }

    private void appendDocumentHeader(final Document document, final Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("materialFlowResources.stocktaking.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());
    }

    private void setSimpleFormat(final PdfPTable headerTable) {
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        headerTable.getDefaultCell().setPadding(2.0f);
        headerTable.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_TOP);
    }

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("materialFlowResources.stocktaking.report.titleWithDate", locale,
                DateUtils.toDateString(new Date()));
    }
}
