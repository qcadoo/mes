/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

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

/**
 * Represents tree.<br/>
 * <br/>
 * XML declaration: <br/>
 * 
 * <pre>
 *      {@code <component type="tree" name="{identifier of component}" source="{source of component content}">}
 * </pre>
 * 
 * XML options:
 * <ul>
 * <li>height - integer - height of component</li>
 * <li>width - integer - width of component</li>
 * <li>expression - String - expression that defines how to display entities inside tree</li>
 * <li>rootExpression - String - expression that defines what to display ass root label</li>
 * <li>correspondingView - {pluginName}/{viewName} - defines content entity details view</li>
 * </ul>
 */
public final class TreeComponent extends AbstractComponent<TreeData> implements SelectableComponent {

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
            if (entity == null || entity.getId() == null) {
                return new ViewValue<TreeData>(new TreeData(new TreeNode(Long.valueOf(0), getRootLabel(locale)), null, null));
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

        TreeNode rootNode = new TreeNode(Long.valueOf(0), getRootLabel(locale));

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

    private String getRootLabel(final Locale locale) {
        String rootLabel = getTranslationService().translate(
                Arrays.asList(new String[] {
                        getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                                + ".root", "core.tree.root" }), locale);

        return rootLabel;
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        String messagePath = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath();
        translationsMap.put(messagePath + ".header", getTranslationService().translate(messagePath + ".header", locale));

        String[] gridMessages = new String[] { "new", "delete", "edit", "root", "confirmDeleteMessage" };

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
