package com.qcadoo.mes.materialFlowResources.print.helper;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.HeaderAlignment;

@Service
public class DocumentPdfHelper {

    private static final String L_DOCUMENT_TYPE_KEY = "materialFlowResources.report.type.value.";

    private static final String L_FILENAME = "materialFlowResources.report.filename";

    private static final String L_HEADER = "materialFlowResources.report.header";

    private static final String L_NUMBER = "materialFlowResources.document.number.label";

    private static final String L_TIME = "materialFlowResources.document.time.label";

    private static final String L_LOCATION_FROM = "materialFlowResources.document.locationFrom.label";

    private static final String L_LOCATION_TO = "materialFlowResources.document.locationTo.label";

    private static final String L_STATE = "materialFlowResources.document.state.label";

    private static final String L_DESCRIPTION = "materialFlowResources.document.description.label";

    private static final String L_COMPANY = "materialFlowResources.document.company.label";

    private static final String L_STATE_VALUE = "materialFlowResources.document.state.value.";

    private static final String L_POSITION_HEADER__INDEX = "materialFlowResources.report.positionsHeader.index";

    private static final String L_POSITION_HEADER__NUMBER = "materialFlowResources.report.positionsHeader.number";

    private static final String L_POSITION_HEADER__QUANTITY = "materialFlowResources.report.positionsHeader.quantity";

    private static final String L_POSITION_HEADER__UNIT = "materialFlowResources.report.positionsHeader.unit";

    private static final String L_POSITION_HEADER__PRICE = "materialFlowResources.report.positionsHeader.price";

    private static final String L_POSITION_HEADER__BATCH = "materialFlowResources.report.positionsHeader.batch";

    private static final String L_POSITION_HEADER__PRODUCTION_DATE = "materialFlowResources.report.positionsHeader.productionDate";

    private static final String L_POSITION_HEADER__VALUE = "materialFlowResources.report.positionsHeader.value";

    private static final String L_TABLE_HEADER = "materialFlowResources.report.tableHeader";

    private static final String L_TOTAL_VALUE = "materialFlowResources.report.tableHeader.totalValue";

    private static final String L_TOTAL = "materialFlowResources.report.tableHeader.total";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public Entity getDocumentEntity(final Long id) {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT).get(id);
    }

    /**
     * Returns translated short document type
     * 
     * @param documentEntity
     * @param locale
     * @return translated short document type
     */
    public String getDocumentType(final Entity documentEntity, final Locale locale) {
        return translationService.translate(L_DOCUMENT_TYPE_KEY + DocumentType.of(documentEntity).getStringValue(), locale);
    }

    /**
     * Returns translated products table header
     *
     * 
     * @param locale
     * @return translated short document type
     */
    public String getTableHeader(final Locale locale) {
        return translationService.translate(L_TABLE_HEADER, locale);
    }

    /**
     * Returns translated document report file name (with document type and number)
     * 
     * @param documentEntity
     * @param locale
     * @return translated document report file name
     */
    public String getFileName(final Entity documentEntity, final Locale locale) {
        return translationService.translate(L_FILENAME, locale, getDocumentType(documentEntity, locale),
                documentEntity.getStringField(DocumentFields.NUMBER));
    }

    /**
     * Returns translated document report header (with document type)
     * 
     * @param documentEntity
     * @param locale
     * @return translated document report header
     */
    public String getDocumentHeader(final Entity documentEntity, final Locale locale) {
        return translationService.translate(L_HEADER, locale, getDocumentType(documentEntity, locale));
    }

    /**
     * Returns total value label
     * 
     * @param locale
     * @return
     */
    public String getTotalValueLabel(final Locale locale) {
        return translationService.translate(L_TOTAL_VALUE, locale);
    }

    /**
     * Returns total row label
     * 
     * @param locale
     * @return
     */
    public String getTotaLabel(final Locale locale) {
        return translationService.translate(L_TOTAL, locale);
    }

    /**
     * Returns content of document header table in proper order as pair (label, value)
     * 
     * @param documentEntity
     * @param locale
     * @return document header table
     */
    public List<HeaderPair> getDocumentHeaderTableContent(final Entity documentEntity, final Locale locale) {
        List<HeaderPair> headerValues = Lists.newLinkedList();
        headerValues.add(new HeaderPair(translationService.translate(L_NUMBER, locale), DocumentDataProvider
                .number(documentEntity)));
        headerValues.add(new HeaderPair(translationService.translate(L_LOCATION_FROM, locale), DocumentDataProvider
                .locationFrom(documentEntity)));
        headerValues.add(new HeaderPair(translationService.translate(L_COMPANY, locale), DocumentDataProvider
                .company(documentEntity)));
        headerValues.add(new HeaderPair(translationService.translate(L_TIME, locale), DocumentDataProvider.time(documentEntity)));
        headerValues.add(new HeaderPair(translationService.translate(L_LOCATION_TO, locale), DocumentDataProvider
                .locationTo(documentEntity)));
        headerValues.add(new HeaderPair(translationService.translate(L_STATE, locale), translationService.translate(L_STATE_VALUE
                + DocumentDataProvider.state(documentEntity), locale)));
        return headerValues;
    }

    /**
     * Returns document desription with label
     * 
     * @param documentEntity
     * @param locale
     * @return
     */
    public HeaderPair getDescription(final Entity documentEntity, final Locale locale) {
        return new HeaderPair(translationService.translate(L_DESCRIPTION, locale),
                DocumentDataProvider.description(documentEntity));
    }

    /**
     * Returns map of translated positions' table headers and alignments
     * 
     * @param locale
     * @return
     */
    public Map<String, HeaderAlignment> getPositionsTableHeaderLabels(final Locale locale) {
        Map<String, HeaderAlignment> headerLabels = Maps.newLinkedHashMap();

        headerLabels.put(translationService.translate(L_POSITION_HEADER__INDEX, locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER__NUMBER, locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER__QUANTITY, locale), HeaderAlignment.RIGHT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER__UNIT, locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER__PRICE, locale), HeaderAlignment.RIGHT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER__BATCH, locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER__PRODUCTION_DATE, locale), HeaderAlignment.LEFT);
        headerLabels.put(translationService.translate(L_POSITION_HEADER__VALUE, locale), HeaderAlignment.RIGHT);
        return headerLabels;
    }

    public class HeaderPair {

        private String label;

        private String value;

        public HeaderPair(String label, String value) {
            this.setLabel(label);
            this.setValue(value);
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

    }
}
