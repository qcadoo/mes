package com.qcadoo.mes.costCalculation.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.mes.basic.constants.FormsFields;
import com.qcadoo.mes.basic.constants.LabelFields;
import com.qcadoo.mes.basic.constants.ModelFields;
import com.qcadoo.mes.basic.constants.ProductAttachmentFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costCalculation.constants.ModelCardFields;
import com.qcadoo.mes.costCalculation.constants.ModelCardProductFields;
import com.qcadoo.mes.costCalculation.constants.ParameterFieldsCC;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyInputProductTypeFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public final class ModelCardPdfService extends PdfDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(ModelCardPdfService.class);

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final int[] defaultModelCardProductColumnWidth = new int[] { 10, 20, 5, 10, 10 };

    private static final int[] defaultModelCardMaterialsColumnWidth = new int[] { 9, 6, 15, 6, 6, 3, 3, 3, 3, 4, 3, 3 };

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("costCalculation.modelCard.report.title", locale);
    }

    @Override
    protected void buildPdfContent(Document document, Entity entity, Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("costCalculation.modelCard.report.header", locale,
                entity.getStringField(ModelCardFields.NAME));
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, StringUtils.EMPTY, documentTitle, documentAuthor,
                entity.getDateField(ModelCardFields.DATE));

        Entity parameter = parameterService.getParameter();
        Entity productAttribute = parameter.getBelongsToField(ParameterFieldsCC.PRODUCT_ATTRIBUTE);
        Entity materialAttribute = parameter.getBelongsToField(ParameterFieldsCC.MATERIAL_ATTRIBUTE);
        for (Entity modelCardProduct : entity.getHasManyField(ModelCardFields.MODEL_CARD_PRODUCTS)) {
            addProductTable(document, modelCardProduct, productAttribute, locale);
            addMaterialsTable(document, modelCardProduct, materialAttribute, locale);
            document.newPage();
        }
    }

    private void addMaterialsTable(Document document, Entity modelCardProduct, Entity materialAttribute, Locale locale)
            throws DocumentException {
        Map<String, HeaderAlignment> headersWithAlignments = com.google.common.collect.Maps.newLinkedHashMap();
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.productType.label", locale),
                HeaderAlignment.LEFT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.sizeGroup.label", locale),
                HeaderAlignment.LEFT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.product.label", locale),
                HeaderAlignment.LEFT);
        headersWithAlignments.put(
                materialAttribute != null ? materialAttribute.getStringField(AttributeFields.NUMBER) : StringUtils.EMPTY,
                HeaderAlignment.LEFT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.supplier.label", locale),
                HeaderAlignment.LEFT);
        headersWithAlignments.put(
                translationService.translate("costCalculation.modelCard.report.minimumOrderQuantity.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.norm.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.price.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.unitCost.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.demandQuantity.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.warehouseStock.label", locale),
                HeaderAlignment.RIGHT);
        headersWithAlignments.put(translationService.translate("costCalculation.modelCard.report.unit.label", locale),
                HeaderAlignment.LEFT);

        List<String> headers = com.google.common.collect.Lists.newLinkedList(headersWithAlignments.keySet());

        PdfPTable table = pdfHelper.createTableWithHeader(headersWithAlignments.size(), headers, true,
                defaultModelCardMaterialsColumnWidth, headersWithAlignments);

        Entity product = modelCardProduct.getBelongsToField(ModelCardProductFields.PRODUCT);
        Entity technology = modelCardProduct.getBelongsToField(ModelCardProductFields.TECHNOLOGY);
        BigDecimal quantity = modelCardProduct.getDecimalField(ModelCardProductFields.QUANTITY);

        Map<OperationProductComponentHolder, BigDecimal> neededQuantities = productQuantitiesService
                .getNeededProductQuantities(technology, product, quantity);

        for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : neededQuantities.entrySet()) {
            Long materialId = neededProductQuantity.getKey().getProductId();
            Long operationProductComponentId = neededProductQuantity.getKey().getOperationProductComponentId();
            Entity material = neededProductQuantity.getKey().getProduct();
            Entity operationProductComponent = neededProductQuantity.getKey().getOperationProductComponent();
            BigDecimal neededQuantity = neededProductQuantity.getValue();

            if (Objects.isNull(materialId)) {
                List<Entity> productBySizeGroups = getProductBySizeGroups(operationProductComponentId);

                for (Entity productBySizeGroup : productBySizeGroups) {
                }
            } else {
                addTechnologyInputProductTypeToReport(table, operationProductComponent);
                table.addCell(new Phrase(StringUtils.EMPTY));
                table.addCell(new Phrase(
                        material.getStringField(ProductFields.NUMBER) + ", " + material.getStringField(ProductFields.NAME),
                        FontUtils.getDejavuRegular7Dark()));
                Entity materialAttributeValue = getProductAttributeValue(materialAttribute, product);
                table.addCell(new Phrase(
                        materialAttributeValue != null ? materialAttributeValue.getStringField(AttributeValueFields.VALUE)
                                : StringUtils.EMPTY,
                        FontUtils.getDejavuRegular7Dark()));
                table.addCell(new Phrase(StringUtils.EMPTY));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase(StringUtils.EMPTY));
                table.addCell(new Phrase(
                        numberService
                                .format(operationProductComponent.getDecimalField(OperationProductInComponentFields.QUANTITY)),
                        FontUtils.getDejavuBold7Dark()));
                table.addCell(new Phrase(StringUtils.EMPTY));
                table.addCell(new Phrase(StringUtils.EMPTY));
                table.addCell(new Phrase(numberService.format(neededQuantity), FontUtils.getDejavuBold7Dark()));
                table.addCell(new Phrase(StringUtils.EMPTY));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(new Phrase(material.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Dark()));
            }
        }
        document.add(table);
    }

    private Entity getProductAttributeValue(Entity productAttribute, Entity product) {
        Entity productAttributeValue = null;
        if (productAttribute != null) {
            for (Entity pav : product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES)) {
                if (pav.getBelongsToField(AttributeValueFields.ATTRIBUTE).getId().equals(productAttribute.getId())) {
                    productAttributeValue = pav;
                    break;
                }
            }
        }
        return productAttributeValue;
    }

    private void addTechnologyInputProductTypeToReport(PdfPTable table, Entity operationProductComponent) {
        Entity technologyInputProductType = operationProductComponent
                .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
        String description = operationProductComponent.getStringField(OperationProductInComponentFields.DESCRIPTION);
        if (technologyInputProductType != null) {
            if (!StringUtils.isEmpty(description)) {
                table.addCell(new Phrase(technologyInputProductType.getStringField(TechnologyInputProductTypeFields.NAME) + " ("
                        + description + ")", FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase(technologyInputProductType.getStringField(TechnologyInputProductTypeFields.NAME),
                        FontUtils.getDejavuRegular7Dark()));
            }
        } else {
            if (!StringUtils.isEmpty(description)) {
                table.addCell(new Phrase(" (" + description + ")", FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
            }
        }
    }

    private List<Entity> getProductBySizeGroups(final Long operationProductComponentId) {
        return getProductBySizeGroupDD().find()
                .createAlias(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT,
                        ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT, JoinType.LEFT)
                .add(SearchRestrictions.eq(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT + L_DOT + L_ID,
                        operationProductComponentId))
                .list().getEntities();
    }

    private DataDefinition getProductBySizeGroupDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_BY_SIZE_GROUP);
    }

    private void addProductTable(Document document, Entity modelCardProduct, Entity productAttribute, Locale locale)
            throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(5);
        panelTable.setWidths(defaultModelCardProductColumnWidth);
        panelTable.addCell(new Phrase(translationService.translate("costCalculation.modelCard.report.product.label", locale),
                FontUtils.getDejavuBold7Dark()));
        Entity product = modelCardProduct.getBelongsToField(ModelCardProductFields.PRODUCT);
        PdfPCell cell = new PdfPCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular9Dark()));
        cell.setColspan(3);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(4.0f);
        panelTable.addCell(cell);

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
                com.lowagie.text.Image img = Image.getInstance(fileName);
                if (img.getWidth() > 130 || img.getHeight() > 90) {
                    img.scaleToFit(130, 90);
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
        cell.setRowspan(6);
        panelTable.addCell(cell);

        panelTable.addCell(new Phrase(StringUtils.EMPTY));
        cell = new PdfPCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular9Dark()));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(4.0f);
        cell.setColspan(3);
        panelTable.addCell(cell);

        Entity productModel = product.getBelongsToField(ProductFields.MODEL);
        panelTable.addCell(new Phrase(translationService.translate("costCalculation.modelCard.report.model.label", locale),
                FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(productModel != null ? productModel.getStringField(ModelFields.NAME) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular9Dark()));
        Entity form = null;
        if (productModel != null) {
            form = productModel.getBelongsToField(ModelFields.FORMS);
        }
        panelTable.addCell(new Phrase(translationService.translate("costCalculation.modelCard.report.form.label", locale),
                FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(form != null ? form.getStringField(FormsFields.NUMBER) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular9Dark()));

        panelTable.addCell(new Phrase(translationService.translate("costCalculation.modelCard.report.quantity.label", locale),
                FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(
                numberService.formatWithMinimumFractionDigits(modelCardProduct.getDecimalField(ModelCardProductFields.QUANTITY),
                        0) + " " + product.getStringField(ProductFields.UNIT),
                FontUtils.getDejavuRegular9Dark()));
        Entity label = null;
        if (productModel != null) {
            label = productModel.getBelongsToField(ModelFields.LABEL);
        }
        panelTable.addCell(new Phrase(translationService.translate("costCalculation.modelCard.report.label.label", locale),
                FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(label != null ? label.getStringField(LabelFields.NAME) : StringUtils.EMPTY,
                FontUtils.getDejavuRegular9Dark()));

        panelTable.addCell(
                new Phrase(translationService.translate("costCalculation.modelCard.report.materialUnitCost.label", locale),
                        FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(new Phrase(numberService.format(BigDecimal.ZERO), FontUtils.getDejavuRegular9Dark()));
        Entity productAttributeValue = getProductAttributeValue(productAttribute, product);
        panelTable.addCell(
                new Phrase(productAttribute != null ? productAttribute.getStringField(AttributeFields.NUMBER) : StringUtils.EMPTY,
                        FontUtils.getDejavuBold7Dark()));
        panelTable.addCell(
                new Phrase(productAttributeValue != null ? productAttributeValue.getStringField(AttributeValueFields.VALUE)
                        : StringUtils.EMPTY, FontUtils.getDejavuRegular9Dark()));

        cell = new PdfPCell(new Phrase(StringUtils.EMPTY));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(4.0f);
        cell.setColspan(4);
        panelTable.addCell(cell);

        document.add(panelTable);
    }
}
