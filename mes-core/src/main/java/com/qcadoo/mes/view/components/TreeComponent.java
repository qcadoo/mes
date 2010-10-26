package com.qcadoo.mes.view.components;

import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.SelectableComponent;
import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.components.tree.TreeData;
import com.qcadoo.mes.view.components.tree.TreeNode;

public class TreeComponent extends AbstractComponent<TreeData> implements SelectableComponent {

    private String rootExpression;

    private String expression;

    public TreeComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    @Override
    public String getType() {
        return "tree";
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("expression".equals(option.getType())) {
                expression = option.getValue();
            } else if ("rootExpression".equals(option.getType())) {
                rootExpression = option.getValue();
            } else if ("correspondingView".equals(option.getType())) {
                addOption("correspondingView", option.getValue());
            } else if ("height".equals(option.getType())) {
                addOption("height", Integer.parseInt(option.getValue()));
            } else if ("width".equals(option.getType())) {
                addOption("width", Integer.parseInt(option.getValue()));
            }
        }
    }

    @Override
    public ViewValue<TreeData> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        TreeData data = new TreeData();

        JSONObject value = viewObject.getJSONObject("value");

        if (value != null) {

            if (!value.isNull("selectedEntityId")) {
                String selectedEntityId = value.getString("selectedEntityId");
                if (selectedEntityId != null && !"null".equals(selectedEntityId)) {
                    Entity selectedEntity = getDataDefinition().get(Long.parseLong(selectedEntityId));
                    selectedEntities.put(getPath(), selectedEntity);
                    data.setSelectedEntityId(Long.parseLong(selectedEntityId));
                }
            }
            if (!value.isNull("opened")) {
                JSONArray opened = value.getJSONArray("opened");
                for (int i = 0; i < opened.length(); i++) {
                    data.addOpenedNode(Long.parseLong(opened.getString(i)));
                }
            }
        }

        return new ViewValue<TreeData>(data);
    }

    @Override
    public ViewValue<TreeData> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<TreeData> viewValue, final Set<String> pathsToUpdate,
            final Locale locale) {

        String joinFieldName = null;
        Long belongsToEntityId = null;
        SearchCriteriaBuilder searchCriteriaBuilder = null;

        if (getSourceFieldPath() != null || getFieldPath() != null) {
            if (entity == null) {
                return new ViewValue<TreeData>(
                        new TreeData(new TreeNode(Long.valueOf(0), getRootLabel(locale, null)), null, null));
            }
            DataDefinition gridDataDefinition = getParentContainer().getDataDefinition();
            if (getSourceComponent() != null) {
                gridDataDefinition = getSourceComponent().getDataDefinition();
            }
            HasManyType hasManyType = null;
            if (getFieldPath() != null) {
                hasManyType = getHasManyType(gridDataDefinition, getFieldPath());
            } else {
                hasManyType = getHasManyType(gridDataDefinition, getSourceFieldPath());
            }
            checkState(hasManyType.getDataDefinition().getName().equals(getDataDefinition().getName()),
                    "Tree and hasMany relation have different data definitions");
            searchCriteriaBuilder = getDataDefinition().find();
            searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(
                    getDataDefinition().getField(hasManyType.getJoinFieldName()), entity.getId()));
            joinFieldName = hasManyType.getJoinFieldName();
            belongsToEntityId = entity.getId();
        } else {
            searchCriteriaBuilder = getDataDefinition().find();
        }

        List<Entity> instructionComponents = searchCriteriaBuilder.list().getEntities();

        TreeNode rootNode = new TreeNode(Long.valueOf(0), getRootLabel(locale, entity));

        Map<Long, TreeNode> createdNodes = new HashMap<Long, TreeNode>();
        while (instructionComponents.size() > 0) {
            boolean somethingChange = false;
            Iterator<Entity> entityIterator = instructionComponents.iterator();
            while (entityIterator.hasNext()) {
                Entity e = entityIterator.next();
                TreeNode parent = null;
                if (e.getField("parent") == null) {
                    parent = rootNode;
                } else {
                    parent = createdNodes.get(((Entity) e.getField("parent")).getId());
                }
                if (parent != null) {
                    TreeNode cretedNode = new TreeNode(e.getId(), ExpressionUtil.getValue(e, expression));
                    parent.addChild(cretedNode);
                    createdNodes.put(cretedNode.getId(), cretedNode);
                    entityIterator.remove();
                    somethingChange = true;
                }
            }
            if (!somethingChange) {
                throw new IllegalStateException("error in instruction components database");
            }
        }

        TreeData data = new TreeData(rootNode, joinFieldName, belongsToEntityId);
        if (viewValue != null) {
            data.setOpenedNodes(viewValue.getValue().getOpenedNodes());
            data.setSelectedEntityId(viewValue.getValue().getSelectedEntityId());
        }

        return new ViewValue<TreeData>(data);
    }

    private String getRootLabel(final Locale locale, final Entity entity) {
        String suffix = entity != null ? "rootFor" : "root";
        String rootLabel = getTranslationService().translate(
                Arrays.asList(new String[] {
                        getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath() + "."
                                + suffix, "core.tree." + suffix }), locale);

        if (entity != null) {
            rootLabel += " " + ExpressionUtil.getValue(entity, rootExpression);
        }

        return rootLabel;
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        String messagePath = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath();
        translationsMap.put(messagePath + ".header", getTranslationService().translate(messagePath + ".header", locale));

        String[] gridMessages = new String[] { "new", "delete", "edit", "root" };

        for (String gridMessage : gridMessages) {
            translationsMap.put(
                    messagePath + "." + gridMessage,
                    getTranslationService().translate(
                            Arrays.asList(new String[] { messagePath + "." + gridMessage, "core.tree." + gridMessage }), locale));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long getSelectedEntityId(final ViewValue<Long> viewValue) {
        ViewValue<TreeData> value = (ViewValue<TreeData>) lookupViewValue(viewValue);
        return value.getValue().getSelectedEntityId();
    }

    private HasManyType getHasManyType(final DataDefinition dataDefinition, final String fieldPath) {
        checkState(!fieldPath.matches("\\."), "Tree doesn't support sequential path");
        FieldDefinition fieldDefinition = dataDefinition.getField(fieldPath);
        if (fieldDefinition != null && fieldDefinition.getType() instanceof HasManyType) {
            return (HasManyType) fieldDefinition.getType();
        } else {
            throw new IllegalStateException("Tree data definition cannot be found");
        }
    }
}
