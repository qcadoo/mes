package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.OrdersGroupIssuedMaterialFields;
import com.qcadoo.mes.materialFlowResources.constants.OrdersGroupIssuedMaterialPositionFields;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.exceptions.InvalidResourceException;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;

@Service
public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    public static final String ORDERS_GROUPS = "ordersGroups";

    public static final String MODEL_ORDERS_GROUP_ISSUED_MATERIAL = "ordersGroupIssuedMaterial";

    public static final String MODEL_ORDERS_GROUP_ISSUED_MATERIAL_POSITION = "ordersGroupIssuedMaterialPosition";

    public static final String DOCUMENTS = "documents";

    public static final String ORDERS_GROUP_ISSUED_MATERIALS = "ordersGroupIssuedMaterials";

    @Autowired
    private PluginManager pluginManager;

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

    @Autowired
    private DocumentStateChangeService documentStateChangeService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Async
    public void createResourcesForDocument(Entity document) {
        if (document != null && !DocumentState.ACCEPTED.getStringValue().equals(document.getStringField(DocumentFields.STATE))
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

            documentStateChangeService.buildFailureStateChange(document.getId());

            return;
        }

        if (!document.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
            String blockedResources = getBlockedResources(document);
            if (blockedResources == null) {
                try {
                    resourceManagementService.createResources(document);
                } catch (InvalidResourceException ire) {
                    document.setNotValid();

                    String productNumber = ire.getEntity().getBelongsToField(ResourceFields.PRODUCT)
                            .getStringField(ProductFields.NUMBER);
                    if ("materialFlow.error.position.batch.required"
                            .equals(ire.getEntity().getError(ResourceFields.BATCH).getMessage())) {
                        LOG.error(translationService.translate(
                                "materialFlow.document.validate.global.error.invalidResource.batchRequired",
                                LocaleContextHolder.getLocale(), productNumber));
                    } else {
                        String resourceNumber = ire.getEntity().getStringField(ResourceFields.NUMBER);

                        LOG.error(translationService.translate("materialFlow.document.validate.global.error.invalidResource",
                                LocaleContextHolder.getLocale(), resourceNumber, productNumber));
                    }
                }
            } else {
                document.setNotValid();

                LOG.warn(translationService.translate(
                        "materialFlow.document.validate.global.error.positionsBlockedForQualityControl",
                        LocaleContextHolder.getLocale(), document.getStringField(DocumentFields.NUMBER), blockedResources));
            }
        } else {
            document.setNotValid();

            LOG.warn(translationService.translate("materialFlow.document.validate.global.error.emptyPositions",
                    LocaleContextHolder.getLocale(), document.getStringField(DocumentFields.NUMBER)));
        }

        if (!document.isValid()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            documentErrorsLogger.saveResourceStockLackErrorsToSystemLogs(document);

            documentStateChangeService.buildFailureStateChangeAfterRollback(document.getId());

            LOG.info(failedMessage);
        } else {
            LOG.info(translationService.translate("materialFlowResources.success.documentAccepted",
                    LocaleContextHolder.getLocale()));

            boolean created = receiptDocumentForReleaseHelper.tryBuildConnectedDocument(document, true);

            if (created) {
                LOG.info(translationService.translate("materialFlow.document.info.createdConnected",
                        LocaleContextHolder.getLocale()));
            }

            updateOrdersGroupIssuedMaterials(document.getBelongsToField(OrdersGroupIssuedMaterialFields.ORDERS_GROUP), null);

            String successMessage = String.format("DOCUMENT ACCEPT SUCCESS: id = %d number = %s", document.getId(),
                    document.getStringField(DocumentFields.NUMBER));

            LOG.info(successMessage);
        }
    }

    public void updateOrdersGroupIssuedMaterials(Entity ordersGroup, Long skipDocumentId) {
        if (pluginManager.isPluginEnabled(ORDERS_GROUPS) && ordersGroup != null) {
            DataDefinition ordersGroupIssuedMaterialDD = dataDefinitionService.get(ORDERS_GROUPS,
                    MODEL_ORDERS_GROUP_ISSUED_MATERIAL);
            DataDefinition ordersGroupIssuedMaterialPositionDD = dataDefinitionService.get(ORDERS_GROUPS,
                    MODEL_ORDERS_GROUP_ISSUED_MATERIAL_POSITION);
            List<Entity> oldOrdersGroupIssuedMaterials = ordersGroupIssuedMaterialDD.find()
                    .add(SearchRestrictions.belongsTo(OrdersGroupIssuedMaterialFields.ORDERS_GROUP, ordersGroup)).list()
                    .getEntities();
            if (!oldOrdersGroupIssuedMaterials.isEmpty()) {
                ordersGroupIssuedMaterialDD
                        .delete(oldOrdersGroupIssuedMaterials.stream().map(Entity::getId).toArray(Long[]::new));
            }
            Map<Long, BigDecimal> productQuantities = Maps.newHashMap();
            Map<Long, BigDecimal> productValues = Maps.newHashMap();
            Map<Long, List<Entity>> productPositions = Maps.newHashMap();
            createOrdersGroupIssueMaterialPositions(ordersGroup, ordersGroupIssuedMaterialPositionDD, productQuantities,
                    productValues, productPositions, skipDocumentId);
            List<Entity> ordersGroupIssuedMaterials = createOrdersGroupIssueMaterials(ordersGroupIssuedMaterialDD,
                    productQuantities, productValues, productPositions);
            ordersGroup.setField(ORDERS_GROUP_ISSUED_MATERIALS, ordersGroupIssuedMaterials);
            ordersGroup.getDataDefinition().save(ordersGroup);
        }
    }

    private void createOrdersGroupIssueMaterialPositions(Entity ordersGroup, DataDefinition ordersGroupIssuedMaterialPositionDD,
            Map<Long, BigDecimal> productQuantities, Map<Long, BigDecimal> productValues,
            Map<Long, List<Entity>> productPositions, Long skipDocumentId) {
        for (Entity document : ordersGroup.getHasManyField(DOCUMENTS)) {
            if (document.getId().equals(skipDocumentId) || DocumentType.INTERNAL_INBOUND.equals(DocumentType.of(document))) {
                continue;
            }
            for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
                Long productId = position.getBelongsToField(PositionFields.PRODUCT).getId();
                BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
                BigDecimal price = BigDecimalUtils.convertNullToZero(position.getDecimalField(PositionFields.PRICE));
                BigDecimal value = numberService
                        .setScaleWithDefaultMathContext(quantity.multiply(price, numberService.getMathContext()));
                productQuantities.compute(productId,
                        (k, v) -> (v == null) ? quantity : v.add(quantity, numberService.getMathContext()));
                productValues.compute(productId, (k, v) -> (v == null) ? value : v.add(value));
                Entity ordersGroupIssuedMaterialPosition = ordersGroupIssuedMaterialPositionDD.create();
                ordersGroupIssuedMaterialPosition.setField(OrdersGroupIssuedMaterialPositionFields.DOCUMENT_NUMBER,
                        document.getStringField(DocumentFields.NUMBER));
                ordersGroupIssuedMaterialPosition.setField(OrdersGroupIssuedMaterialPositionFields.QUANTITY, quantity);
                ordersGroupIssuedMaterialPosition.setField(OrdersGroupIssuedMaterialPositionFields.PRICE, price);
                ordersGroupIssuedMaterialPosition.setField(OrdersGroupIssuedMaterialPositionFields.VALUE, value);
                productPositions.computeIfAbsent(productId, k -> new ArrayList<>()).add(ordersGroupIssuedMaterialPosition);
            }
        }
    }

    private List<Entity> createOrdersGroupIssueMaterials(DataDefinition ordersGroupIssuedMaterialDD,
            Map<Long, BigDecimal> productQuantities, Map<Long, BigDecimal> productValues,
            Map<Long, List<Entity>> productPositions) {
        List<Entity> ordersGroupIssuedMaterials = Lists.newArrayList();
        for (Map.Entry<Long, BigDecimal> entry : productQuantities.entrySet()) {
            Entity ordersGroupIssuedMaterial = ordersGroupIssuedMaterialDD.create();
            ordersGroupIssuedMaterial.setField(OrdersGroupIssuedMaterialFields.PRODUCT, entry.getKey());
            ordersGroupIssuedMaterial.setField(OrdersGroupIssuedMaterialFields.QUANTITY, entry.getValue());
            BigDecimal value = productValues.get(entry.getKey());
            ordersGroupIssuedMaterial.setField(OrdersGroupIssuedMaterialFields.VALUE, value);
            ordersGroupIssuedMaterial.setField(OrdersGroupIssuedMaterialFields.AVERAGE_PRICE, numberService
                    .setScaleWithDefaultMathContext(value.divide(entry.getValue(), numberService.getMathContext()), 2));
            ordersGroupIssuedMaterial.setField(OrdersGroupIssuedMaterialFields.ORDERS_GROUP_ISSUED_MATERIAL_POSITIONS,
                    productPositions.get(entry.getKey()));
            ordersGroupIssuedMaterials.add(ordersGroupIssuedMaterial);
        }
        return ordersGroupIssuedMaterials;
    }
}
