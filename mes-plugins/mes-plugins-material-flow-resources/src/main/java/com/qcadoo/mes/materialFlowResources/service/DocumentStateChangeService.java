package com.qcadoo.mes.materialFlowResources.service;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.qcadoo.mes.materialFlowResources.constants.DocumentStateChangeConstants.*;

@Service
public class DocumentStateChangeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void buildFailureStateChange(Long documentId) {
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
        Entity stateChangeEntity = internalBuild(DocumentState.ACCEPTED);
        stateChangeEntity.setField(SOURCE_STATE, DocumentState.DRAFT.getStringValue());
        List<Entity> newStateChanges = Lists.newArrayList(stateChangeEntity);
        newStateChanges.addAll(document.getHasManyField(DocumentFields.STATE_CHANGES));
        document.setField(DocumentFields.STATE_CHANGES, newStateChanges);
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
