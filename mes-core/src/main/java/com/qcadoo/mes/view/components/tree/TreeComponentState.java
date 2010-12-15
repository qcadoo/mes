package com.qcadoo.mes.view.components.tree;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.view.states.AbstractComponentState;

public final class TreeComponentState extends AbstractComponentState {

    public static final String JSON_SELECTED_ENTITY_ID = "selectedEntityId";

    public static final String JSON_BELONGS_TO_ENTITY_ID = "belongsToEntityId";

    private final TreeEventPerformer eventPerformer = new TreeEventPerformer();

    private TreeNode rootNode;

    private List<Long> openedNodes;

    private Long selectedEntityId;

    private final FieldDefinition belongsToFieldDefinition;

    private Long belongsToEntityId;

    public TreeComponentState(final FieldDefinition scopeField) {
        belongsToFieldDefinition = scopeField;
        registerEvent("refresh", eventPerformer, "refresh");
        registerEvent("select", eventPerformer, "selectEntity");
        registerEvent("remove", eventPerformer, "removeSelectedEntity");
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        if (json.has(JSON_SELECTED_ENTITY_ID) && !json.isNull(JSON_SELECTED_ENTITY_ID)) {
            selectedEntityId = json.getLong(JSON_SELECTED_ENTITY_ID);
        }
        if (json.has(JSON_BELONGS_TO_ENTITY_ID) && !json.isNull(JSON_BELONGS_TO_ENTITY_ID)) {
            belongsToEntityId = json.getLong(JSON_BELONGS_TO_ENTITY_ID);
        }

        if (belongsToFieldDefinition != null && belongsToEntityId == null) {
            setEnabled(false);
        }

        requestRender();
        requestUpdateState();
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        if (rootNode == null) {
            reload();
        }

        JSONObject json = new JSONObject();
        json.put(JSON_SELECTED_ENTITY_ID, selectedEntityId);
        json.put(JSON_BELONGS_TO_ENTITY_ID, belongsToEntityId);

        return json;
    }

    @Override
    public void onScopeEntityIdChange(final Long scopeEntityId) {
        if (belongsToFieldDefinition != null) {
            this.belongsToEntityId = scopeEntityId;
            setEnabled(scopeEntityId != null);
        } else {
            throw new IllegalStateException("Tree doesn't have scopeField, it cannot set scopeEntityId");
        }
    }

    public List<Long> getOpenedNodes() {
        return openedNodes;
    }

    public void setOpenedNodes(final List<Long> openedNodes) {
        this.openedNodes = openedNodes;
    }

    public void addOpenedNode(final Long nodeId) {
        if (openedNodes == null) {
            openedNodes = new LinkedList<Long>();
        }
        openedNodes.add(nodeId);
    }

    public void setSelectedEntityId(final Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
        notifyEntityIdChangeListeners(selectedEntityId);
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(final TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    private String translateMessage(final String key) {
        List<String> codes = Arrays.asList(new String[] { getTranslationPath() + "." + key, "core.message." + key });
        return getTranslationService().translate(codes, getLocale());
    }

    private void reload() {
        rootNode = new TreeNode(null, "ROOT");

        if (belongsToFieldDefinition == null || belongsToEntityId != null) {
            SearchCriteriaBuilder criteria = getDataDefinition().find();

            if (belongsToFieldDefinition != null) {
                criteria.restrictedWith(Restrictions.belongsTo(belongsToFieldDefinition, belongsToEntityId));
            }

            SearchResult result = criteria.list();

            List<Pair<Entity, Boolean>> entities = new LinkedList<Pair<Entity, Boolean>>();
            for (Entity entity : result.getEntities()) {
                entities.add(Pair.of(entity, false));
            }

            createChildrenNodes(entities, rootNode);

            boolean existNotUsedEntity = false;
            for (Pair<Entity, Boolean> entityPair : entities) {
                if (!entityPair.getValue()) {
                    existNotUsedEntity = true;
                    break;
                }
            }
            if (existNotUsedEntity) {
                throw new IllegalStateException("Tree is not consistent");
            }

        }
        System.out.println("-----1----->");
        System.out.println(rootNode);
    }

    private void createChildrenNodes(List<Pair<Entity, Boolean>> entities, TreeNode parent) {
        for (Pair<Entity, Boolean> entityPair : entities) {
            if (entityPair.getValue()) {
                continue;
            }
            Entity ent = entityPair.getKey();
            if (ent.getField("parent") == parent.getId()) {
                TreeNode childNode = new TreeNode(ent.getId(), ent.getId().toString());
                rootNode.addChild(childNode);
                entityPair.setValue(true);
                createChildrenNodes(entities, childNode);
            }
        }
    }

    protected class TreeEventPerformer {

        public void refresh(final String[] args) {
            // nothing interesting here
        }

        public void selectEntity(final String[] args) {
            notifyEntityIdChangeListeners(getSelectedEntityId());
        }

        public void removeSelectedEntity(final String[] args) {
            getDataDefinition().delete(selectedEntityId);
            setSelectedEntityId(null);
            addMessage(translateMessage("deleteMessage"), MessageType.SUCCESS);
        }

    }
}
