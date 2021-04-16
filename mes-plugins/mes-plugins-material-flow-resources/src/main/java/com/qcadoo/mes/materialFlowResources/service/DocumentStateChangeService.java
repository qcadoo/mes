package com.qcadoo.mes.materialFlowResources.service;

import static com.qcadoo.mes.materialFlowResources.constants.DocumentStateChangeConstants.DATE_AND_TIME;
import static com.qcadoo.mes.materialFlowResources.constants.DocumentStateChangeConstants.DOCUMENT;
import static com.qcadoo.mes.materialFlowResources.constants.DocumentStateChangeConstants.SOURCE_STATE;
import static com.qcadoo.mes.materialFlowResources.constants.DocumentStateChangeConstants.STATUS;
import static com.qcadoo.mes.materialFlowResources.constants.DocumentStateChangeConstants.TARGET_STATE;
import static com.qcadoo.mes.materialFlowResources.constants.DocumentStateChangeConstants.WORKER;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;

@Service
public class DocumentStateChangeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void buildFailureStateChange(Long documentId) {
        if (checkIfDocumentExist(documentId)) {
            Map<String, Object> params = Maps.newHashMap();
            params.put(SOURCE_STATE, DocumentState.DRAFT.getStringValue());
            params.put(TARGET_STATE, DocumentState.ACCEPTED.getStringValue());
            params.put(STATUS, StateChangeStatus.FAILURE.getStringValue());
            params.put(DOCUMENT, documentId);
            params.put(DATE_AND_TIME, new Date());
            params.put(WORKER, securityService.getCurrentUserName());
            String query = "INSERT INTO materialflowresources_documentstatechange (document_id, worker, dateandtime, sourcestate, targetstate, status) "
                    + "VALUES (:document, :worker, :dateAndTime, :sourceState, :targetState, :status)";
            jdbcTemplate.queryForObject(query, params, Long.class);
        }
    }

    public boolean checkIfDocumentExist(Long documentId) {
        String sql = "SELECT count(*) > 0 FROM materialflowresources_document WHERE id = :id;";
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("id", documentId);

        return jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
    }

    public void buildFailureStateChangeAfterRollback(Long documentId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

            @Override
            public void afterCompletion(int status) {
                super.afterCompletion(status);
                if (TransactionSynchronization.STATUS_ROLLED_BACK == status) {
                    buildFailureStateChange(documentId);
                }
            }
        });
    }

    public void buildInitialStateChange(Entity document) {
        Entity stateChangeEntity = internalBuild(DocumentState.DRAFT);
        document.setField(DocumentFields.STATE_CHANGES, Lists.newArrayList(stateChangeEntity));
    }

    public void buildSuccessfulStateChange(Entity document) {
        List<Entity> stateChanges = document.getHasManyField(DocumentFields.STATE_CHANGES);
        if (stateChanges.stream().noneMatch(e -> DocumentState.ACCEPTED.getStringValue().equals(e.getStringField(TARGET_STATE))
                && StateChangeStatus.SUCCESSFUL.getStringValue().equals(e.getStringField(STATUS)))) {
            Entity stateChangeEntity = internalBuild(DocumentState.ACCEPTED);
            stateChangeEntity.setField(SOURCE_STATE, DocumentState.DRAFT.getStringValue());
            List<Entity> newStateChanges = Lists.newArrayList(stateChangeEntity);
            newStateChanges.addAll(stateChanges);
            document.setField(DocumentFields.STATE_CHANGES, newStateChanges);
        }
    }

    private Entity internalBuild(DocumentState targetState) {
        DataDefinition dataDefinition = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT_STATE_CHANGE);
        Entity stateChangeEntity = dataDefinition.create();

        stateChangeEntity.setField(DATE_AND_TIME, new Date());

        stateChangeEntity.setField(TARGET_STATE, targetState.getStringValue());

        stateChangeEntity.setField(WORKER, securityService.getCurrentUserName());
        stateChangeEntity.setField(STATUS, StateChangeStatus.SUCCESSFUL.getStringValue());
        return stateChangeEntity;
    }
}
