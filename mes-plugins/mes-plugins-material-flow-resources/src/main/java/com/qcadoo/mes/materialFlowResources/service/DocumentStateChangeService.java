package com.qcadoo.mes.materialFlowResources.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;
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

    public Entity buildStateChange(Entity document, StateChangeStatus stateChangeStatus) {
        Entity stateChangeEntity = internalBuild(DocumentState.ACCEPTED, stateChangeStatus);
        stateChangeEntity.setField(SOURCE_STATE, DocumentState.DRAFT.getStringValue());
        stateChangeEntity.setField(DOCUMENT, document);
        stateChangeEntity = stateChangeEntity.getDataDefinition().save(stateChangeEntity);
        return stateChangeEntity;
    }

    public void buildStateChange(Long documentId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

            @Override
            public void afterCompletion(int status) {
                super.afterCompletion(status);
                if (TransactionSynchronization.STATUS_ROLLED_BACK == status) {
                    Map<String, Object> params = Maps.newHashMap();
                    params.put(SOURCE_STATE, DocumentState.DRAFT.getStringValue());
                    params.put(TARGET_STATE, DocumentState.ACCEPTED.getStringValue());
                    params.put(STATUS, StateChangeStatus.FAILURE.getStringValue());
                    params.put(DOCUMENT, documentId);
                    params.put(DATE_AND_TIME, new Date());
                    params.put(WORKER, securityService.getCurrentUserName());
                    String query = "INSERT INTO materialflowresources_documentstatechange (id, document_id, worker, dateandtime, sourcestate, targetstate, status) "
                            + "VALUES (:document, :worker, :dateAndTime, :sourceState, :targetState, :status)";
                    jdbcTemplate.queryForObject(query, params, Long.class);
                }
            }
        });
    }

    public Entity buildInitialStateChange(Entity document) {
        Entity stateChangeEntity = internalBuild(DocumentState.DRAFT, StateChangeStatus.SUCCESSFUL);
        document.setField(DocumentFields.STATE_CHANGES, Lists.newArrayList(stateChangeEntity));
        return stateChangeEntity;
    }

    private Entity internalBuild(DocumentState targetState, StateChangeStatus stateChangeStatus) {
        DataDefinition dataDefinition = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT_STATE_CHANGE);
        Entity stateChangeEntity = dataDefinition.create();

        stateChangeEntity.setField(DATE_AND_TIME, new Date());

        stateChangeEntity.setField(TARGET_STATE, targetState.getStringValue());

        stateChangeEntity.setField(WORKER, securityService.getCurrentUserName());
        stateChangeEntity.setField(STATUS, stateChangeStatus.getStringValue());
        return stateChangeEntity;
    }
}
