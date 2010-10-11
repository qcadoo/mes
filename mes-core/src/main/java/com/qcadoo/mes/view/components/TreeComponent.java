package com.qcadoo.mes.view.components;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import com.qcadoo.mes.view.components.grid.ListData;
import com.qcadoo.mes.view.components.tree.TreeData;
import com.qcadoo.mes.view.components.tree.TreeNode;

public class TreeComponent extends AbstractComponent<TreeData> implements SelectableComponent {

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
        return null;
    }

    @Override
    public ViewValue<TreeData> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<TreeData> viewValue, final Set<String> pathsToUpdate,
            final Locale locale) {
        TreeNode rootNode = new TreeNode(new Long(0), "root");
        /*
         * TreeNode child1 = new TreeNode(new Long(1), "child1"); child1.addChild(new TreeNode(new Long(2), "child1-1")); TreeNode
         * child12 = new TreeNode(new Long(3), "child1-2"); child12.addChild(new TreeNode(new Long(4), "child1-2-1"));
         * child1.addChild(child12); child1.addChild(new TreeNode(new Long(5), "child1-3")); root.addChild(child1); TreeNode
         * child2 = new TreeNode(new Long(6), "child1"); child2.addChild(new TreeNode(new Long(7), "child2-1"));
         * child2.addChild(new TreeNode(new Long(8), "child2-2")); root.addChild(child2); root.addChild(new TreeNode(new Long(9),
         * "child3"));
         */

        // return new ViewValue<TreeNode>(root);

        String joinFieldName = null;
        Long belongsToEntityId = null;
        SearchCriteriaBuilder searchCriteriaBuilder = null;

        if (getSourceFieldPath() != null || getFieldPath() != null) {
            if (entity == null) {
                return new ViewValue<TreeData>(new TreeData(rootNode, null, null));
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

        return new ViewValue<TreeData>(new TreeData(rootNode, joinFieldName, belongsToEntityId));
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        String messageCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                + ".header";
        translationsMap.put(messageCode, getTranslationService().translate(messageCode, locale));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long getSelectedEntityId(final ViewValue<Object> viewValue) {
        ViewValue<ListData> value = (ViewValue<ListData>) lookupViewValue(viewValue);
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
