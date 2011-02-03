package com.qcadoo.mes.view.components.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.EntityTree;
import com.qcadoo.mes.internal.EntityTreeNode;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.components.FieldComponentState;

public final class TreeComponentState extends FieldComponentState {

    public static final String JSON_SELECTED_ENTITY_ID = "selectedEntityId";

    public static final String JSON_BELONGS_TO_ENTITY_ID = "belongsToEntityId";

    public static final String JSON_ROOT_NODE_ID = "root";

    public static final String JSON_OPENED_NODES_ID = "openedNodes";

    public static final String JSON_TREE_STRUCTURE = "treeStructure";

    private final TreeEventPerformer eventPerformer = new TreeEventPerformer();

    private TreeNode rootNode;

    private List<Long> openedNodes;

    private Long selectedEntityId;

    private JSONArray treeStructure;

    private final FieldDefinition belongsToFieldDefinition;

    private Long belongsToEntityId;

    private final String nodeLabelExpression;

    public TreeComponentState(final FieldDefinition scopeField, final String nodeLabelExpression) {
        belongsToFieldDefinition = scopeField;
        this.nodeLabelExpression = nodeLabelExpression;
        registerEvent("initialize", eventPerformer, "initialize");
        registerEvent("initializeAfterBack", eventPerformer, "initialize");
        registerEvent("refresh", eventPerformer, "refresh");
        registerEvent("select", eventPerformer, "selectEntity");
        registerEvent("remove", eventPerformer, "removeSelectedEntity");
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        super.initializeContent(json);

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

        if (json.has(JSON_TREE_STRUCTURE) && !json.isNull(JSON_TREE_STRUCTURE)) {
            treeStructure = json.getJSONArray(JSON_TREE_STRUCTURE);
        }

        if (belongsToEntityId == null) {
            setEnabled(false);
        }
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        if (rootNode == null) {
            reload();
        }

        JSONObject json = super.renderContent();
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
    public void onFieldEntityIdChange(final Long fieldEntityId) {
        if (belongsToEntityId != null && !belongsToEntityId.equals(fieldEntityId)) {
            setSelectedEntityId(null);
        }
        this.belongsToEntityId = fieldEntityId;
        setEnabled(fieldEntityId != null);
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
        if (treeStructure == null) {
            return null;
        }

        if (treeStructure.length() == 0) {
            return new ArrayList<Entity>();
        }

        if (treeStructure.length() > 1) {
            addMessage("core.validate.field.error.multipleRoots", MessageType.FAILURE);
            return null;
        }

        Entity entity = belongsToFieldDefinition.getDataDefinition().get(belongsToEntityId);

        EntityTree tree = entity.getTreeField(belongsToFieldDefinition.getName());

        Map<Long, Entity> nodes = new HashMap<Long, Entity>();

        for (Entity node : tree) {
            node.setField("children", new ArrayList<Entity>());
            node.setField("parent", null);
            nodes.put(node.getId(), node);
        }

        try {
            Entity parent = nodes.get(treeStructure.getJSONObject(0).getLong("id"));

            if (treeStructure.getJSONObject(0).has("children")) {
                reorganize(nodes, parent, treeStructure.getJSONObject(0).getJSONArray("children"));
            }

            return Collections.singletonList(parent);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void reorganize(final Map<Long, Entity> nodes, final Entity parent, final JSONArray children) throws JSONException {
        for (int i = 0; i < children.length(); i++) {
            Entity entity = nodes.get(children.getJSONObject(i).getLong("id"));
            ((List<Entity>) parent.getField("children")).add(entity);
            if (children.getJSONObject(i).has("children")) {
                reorganize(nodes, entity, children.getJSONObject(i).getJSONArray("children"));
            }
        }
    }

    @Override
    public void setFieldValue(final Object value) {
        requestRender();
        requestUpdateState();
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    public void setSelectedEntityId(final Long selectedEntityId) {
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
        if (belongsToEntityId != null) {
            Entity entity = belongsToFieldDefinition.getDataDefinition().get(belongsToEntityId);

            EntityTree tree = entity.getTreeField(belongsToFieldDefinition.getName());

            if (tree.getRoot() != null) {
                rootNode = createNode(tree.getRoot());
            } else {
                rootNode = new TreeNode(0L, getTranslationService().translate(getTranslationPath() + ".emptyRoot", getLocale()));
            }
        } else {
            rootNode = new TreeNode(0L, getTranslationService().translate(getTranslationPath() + ".emptyRoot", getLocale()));
        }
    }

    private TreeNode createNode(final EntityTreeNode entityTreeNode) {
        String nodeLabel = ExpressionUtil.getValue(entityTreeNode, nodeLabelExpression, getLocale());
        TreeNode node = new TreeNode(entityTreeNode.getId(), nodeLabel);

        for (EntityTreeNode childEntityTreeNode : entityTreeNode.getChildren()) {
            node.addChild(createNode(childEntityTreeNode));
        }

        return node;
    }

    protected class TreeEventPerformer {

        public void refresh(final String[] args) {
            // nothing interesting here
        }

        public void initialize(final String[] args) {
            addOpenedNode(0L);
            setSelectedEntityId(null);
            requestRender();
            requestUpdateState();
        }

        public void selectEntity(final String[] args) {
            notifyEntityIdChangeListeners(parseSelectedIdForListeners(getSelectedEntityId()));
        }

        public void removeSelectedEntity(final String[] args) {
            getDataDefinition().delete(selectedEntityId);
            setSelectedEntityId(null);
            addMessage(translateMessage("deleteMessage"), MessageType.SUCCESS);
            requestRender();
            requestUpdateState();
        }

    }
}
