package com.qcadoo.mes.materialFlowResources.service;

import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.exceptions.InvalidResourceException;
import com.qcadoo.model.api.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ReceiptDocumentForReleaseHelper receiptDocumentForReleaseHelper;

    @Autowired
    private DocumentErrorsLogger documentErrorsLogger;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private TranslationService translationService;

    @Async
    public void createResourcesForDocument(Entity document) {
        if (document != null
                && !DocumentState.ACCEPTED.getStringValue().equals(document.getStringField(DocumentFields.STATE))
                && !getAcceptationInProgress(document.getId())) {

            setAcceptationInProgress(document, true);

            try {
                createResourcesForDocuments(document);
            } catch (Exception e) {
                LOG.error("Error in createResourcesForDocuments ", e);

                throw new IllegalStateException(e.getMessage(), e);
            } finally {
                setAcceptationInProgress(document, false);
            }
        }
    }

    public boolean getAcceptationInProgress(final Long documentId) {
        String sql = "SELECT acceptationinprogress FROM materialflowresources_document WHERE id = :id;";
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("id", documentId);

        return jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
    }

    public void setAcceptationInProgress(final Entity document, final boolean acceptationInProgress) {
        String sql = "UPDATE materialflowresources_document SET acceptationinprogress = :acceptationinprogress WHERE id = :id;";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("acceptationinprogress", acceptationInProgress);

        parameters.put("id", document.getId());

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);
        String message = String.format("DOCUMENT SET ACCEPTATION IN PROGRESS = %b  id = %d number = %s", acceptationInProgress,
                document.getId(), document.getStringField(DocumentFields.NUMBER));

        LOG.info(message);

        jdbcTemplate.update(sql, namedParameters);
    }

    public void setAcceptationInProgress(final List<Entity> documents, final boolean acceptationInProgress) {
        String sql = "UPDATE materialflowresources_document SET acceptationinprogress = :acceptationinprogress WHERE id IN (:ids);";

        List<Long> ids = documents.stream().map(Entity::getId).collect(Collectors.toList());
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("acceptationinprogress", acceptationInProgress);

        parameters.put("ids", ids);

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);
        LOG.info("DOCUMENT SET ACCEPTATION IN PROGRESS = " + acceptationInProgress + " ids ="
                + ids.stream().map(Object::toString).collect(Collectors.joining(", ")));
        jdbcTemplate.update(sql, namedParameters);
    }

    public String getBlockedResources(Entity document) {
        String sql = "SELECT string_agg(p.number::text, ',' ORDER BY p.number) FROM materialflowresources_position p JOIN materialflowresources_resource r "
                + "ON r.id = p.resource_id WHERE p.document_id = :id AND r.blockedforqualitycontrol";
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("id", document.getId());
        return jdbcTemplate.queryForObject(sql, parameters, String.class);
    }

    @Transactional
    private void createResourcesForDocuments(Entity document) {
        String message = String.format("DOCUMENT ACCEPT STARTED: id = %d number = %s", document.getId(),
                document.getStringField(DocumentFields.NUMBER));

        LOG.info(message);

        document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
        document.setField(DocumentFields.ACCEPTATION_IN_PROGRESS, false);

        document = document.getDataDefinition().save(document);

        String failedMessage = String.format("DOCUMENT ACCEPT FAILED: id = %d number = %s", document.getId(),
                document.getStringField(DocumentFields.NUMBER));
        if (!document.isValid()) {
            LOG.info(failedMessage);

            return;
        }

        if (!document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
            String blockedResources = getBlockedResources(document);
            if (blockedResources == null) {
                try {
                    resourceManagementService.createResources(document);
                } catch (InvalidResourceException ire) {
                    document.setNotValid();

                    if ("materialFlow.error.position.batch.required"
                            .equals(ire.getEntity().getError(ResourceFields.BATCH).getMessage())) {
                        String productNumber = ire.getEntity().getBelongsToField(ResourceFields.PRODUCT)
                                .getStringField(ProductFields.NUMBER);
                        LOG.error(translationService.translate("materialFlow.document.validate.global.error.positionsBlockedForQualityControl",
                                LocaleContextHolder.getLocale(), productNumber));
                    } else {
                        String resourceNumber = ire.getEntity().getStringField(ResourceFields.NUMBER);
                        String productNumber = ire.getEntity().getBelongsToField(ResourceFields.PRODUCT)
                                .getStringField(ProductFields.NUMBER);

                        LOG.error(translationService.translate("materialFlow.document.validate.global.error.invalidResource",
                                LocaleContextHolder.getLocale(), resourceNumber, productNumber));
                    }
                }
            } else {
                document.setNotValid();

                LOG.warn(translationService.translate("materialFlow.document.validate.global.error.positionsBlockedForQualityControl",
                        LocaleContextHolder.getLocale(), blockedResources));
            }
        } else {
            document.setNotValid();

            LOG.warn(translationService.translate("materialFlow.document.validate.global.error.emptyPositions",
                    LocaleContextHolder.getLocale()));
        }

        if (!document.isValid()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            documentErrorsLogger.saveResourceStockLackErrorsToSystemLogs(document);

            LOG.info(failedMessage);
        } else {
            LOG.info(translationService.translate("materialFlowResources.success.documentAccepted",
                    LocaleContextHolder.getLocale()));

            if (receiptDocumentForReleaseHelper.buildConnectedDocument(document)) {
                boolean created = receiptDocumentForReleaseHelper.tryBuildConnectedDocument(document, true);

                if (created) {
                    LOG.info(translationService.translate("materialFlow.document.info.createdConnected",
                            LocaleContextHolder.getLocale()));
                }
            }

            String successMessage = String.format("DOCUMENT ACCEPT SUCCESS: id = %d number = %s", document.getId(),
                    document.getStringField(DocumentFields.NUMBER));

            LOG.info(successMessage);
        }
    }
}
