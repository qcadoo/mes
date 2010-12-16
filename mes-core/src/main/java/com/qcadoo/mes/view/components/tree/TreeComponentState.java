package com.qcadoo.mes.view.components.tree;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.view.states.AbstractComponentState;

public final class TreeComponentState extends AbstractComponentState {

    public static final String JSON_SELECTED_ENTITY_ID = "selectedEntityId";

    public static final String JSON_BELONGS_TO_ENTITY_ID = "belongsToEntityId";

    public static final String JSON_ROOT_NODE_ID = "root";

    public static final String JSON_OPENED_NODES_ID = "openedNodes";

    private final TreeEventPerformer eventPerformer = new TreeEventPerformer();

    private TreeNode rootNode;

    // TODO krna
    private List<Long> openedNodes;

    private Long selectedEntityId;

    private final FieldDefinition belongsToFieldDefinition;

    private Long belongsToEntityId;

    private final String nodeLabelExpression;

    public TreeComponentState(final FieldDefinition scopeField, final String nodeLabelExpression) {
        belongsToFieldDefinition = scopeField;
        this.nodeLabelExpression = nodeLabelExpression;
        registerEvent("initialize", eventPerformer, "onInitialize");
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
        if (json.has(JSON_OPENED_NODES_ID) && !json.isNull(JSON_OPENED_NODES_ID)) {
            JSONArray openNodesArray = json.getJSONArray(JSON_OPENED_NODES_ID);
            for (int i = 0; i < openNodesArray.length(); i++) {
                addOpenedNode(openNodesArray.getLong(i));
            }
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

        if (openedNodes != null) {
            JSONArray openedNodesArray = new JSONArray();
            for (Long openedNodeId : openedNodes) {
                openedNodesArray.put(openedNodeId);
            }
            json.put(JSON_OPENED_NODES_ID, openedNodesArray);
        }

        json.put(JSON_ROOT_NODE_ID, rootNode.toJson());
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

    @Override
    public Object getFieldValue() {
        return getSelectedEntityId();
    }

    @Override
    public void setFieldValue(final Object value) {
        setValue((Long) value);
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    public void setValue(final Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
        notifyEntityIdChangeListeners(parseSelectedIdForListeners(selectedEntityId));
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

    private Long parseSelectedIdForListeners(final Long selectedEntityId) {
        if (selectedEntityId == null || selectedEntityId == 0) {
            return null;
        }
        return selectedEntityId;
    }

    private void reload() {
        rootNode = new TreeNode(null, getTranslationService().translate(getTranslationPath() + ".rootLabel", getLocale()));

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
    }

    private void createChildrenNodes(List<Pair<Entity, Boolean>> entities, TreeNode parent) {
        for (Pair<Entity, Boolean> entityPair : entities) {
            if (entityPair.getValue()) {
                continue;
            }
            Entity ent = entityPair.getKey();
            if (isKid(parent, ent)) {
                String nodeLabel = ExpressionUtil.getValue(ent, nodeLabelExpression, getLocale());
                TreeNode childNode = new TreeNode(ent.getId(), nodeLabel);
                parent.addChild(childNode);
                entityPair.setValue(true);
                createChildrenNodes(entities, childNode);
            }
        }
    }

    private boolean isKid(TreeNode parent, Entity ent) {
        Object entityParent = ent.getField("parent");
        if (entityParent == null) {
            if (parent.getId() == null) {
                return true;
            }
        } else {
            if (((Entity) entityParent).getId().equals(parent.getId())) {
                return true;
            }
        }
        return false;
    }

    protected class TreeEventPerformer {

        public void refresh(final String[] args) {
            // nothing interesting here
        }

        public void onInitialize(final String[] args) {
            addOpenedNode(0L);
        }

        public void selectEntity(final String[] args) {
            notifyEntityIdChangeListeners(parseSelectedIdForListeners(getSelectedEntityId()));
        }

        public void removeSelectedEntity(final String[] args) {
            getDataDefinition().delete(selectedEntityId);
            setValue(null);
            addMessage(translateMessage("deleteMessage"), MessageType.SUCCESS);
        }

    }
}
