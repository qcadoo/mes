/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.viewold.components;

import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.viewold.AbstractComponent;
import com.qcadoo.mes.viewold.ContainerComponent;
import com.qcadoo.mes.viewold.ViewValue;
import com.qcadoo.mes.viewold.components.combobox.EntityComboBoxValue;

/**
 * Represents combobox with entities element.
 */
@Deprecated
public final class EntityComboBoxComponent extends AbstractComponent<EntityComboBoxValue> {

    public EntityComboBoxComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    @Override
    public String getType() {
        return "entityComboBox";
    }

    @Override
    public ViewValue<EntityComboBoxValue> castComponentValue(final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        JSONObject valueObject = viewObject.getJSONObject("value");
        if (!valueObject.isNull("value")) {
            EntityComboBoxValue value = new EntityComboBoxValue();
            if (!"".equals(valueObject.getString("value"))) {
                Long selectedEntityId = Long.parseLong(valueObject.getString("value"));
                value.setSelectedValue(selectedEntityId);
                Entity selectedEntity = getDataDefinition().get(selectedEntityId);
                selectedEntities.put(getPath(), selectedEntity);
            }
            if (!viewObject.isNull("value") && !viewObject.getJSONObject("value").isNull("required")) {
                value.setRequired(viewObject.getJSONObject("value").getBoolean("required"));
            }
            return new ViewValue<EntityComboBoxValue>(value);
        }
        return new ViewValue<EntityComboBoxValue>(null);
    }

    @Override
    public ViewValue<EntityComboBoxValue> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<EntityComboBoxValue> viewValue,
            final Set<String> pathsToUpdate, final Locale locale) {

        DataDefinition parentDefinition = getParentContainer().getDataDefinition();
        EntityComboBoxValue value = new EntityComboBoxValue();

        BelongsToType belongsToType = getBelongsToType(parentDefinition, getFieldPath());
        if (getSourceFieldPath() != null) {
            DataDefinition listDataDefinition = getParentContainer().getDataDefinition();
            if (getSourceComponent() != null) {
                listDataDefinition = getSourceComponent().getDataDefinition();
            }
            HasManyType hasManyType = getHasManyType(listDataDefinition, getSourceFieldPath());

            checkState(hasManyType.getDataDefinition().getName().equals(belongsToType.getDataDefinition().getName()),
                    "EntityComboBox and hasMany relation have different data definitions");

            Entity selectedEntity = selectedEntities.get(getSourceComponent().getPath());

            Map<Long, String> valuesMap = new HashMap<Long, String>();
            if (selectedEntity != null) {
                SearchCriteriaBuilder searchCriteriaBuilder = hasManyType.getDataDefinition().find();
                searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(hasManyType
                        .getDataDefinition().getField(hasManyType.getJoinFieldName()), selectedEntity.getId()));
                SearchResult rs = searchCriteriaBuilder.list();

                for (Entity e : rs.getEntities()) {
                    valuesMap.put(e.getId(), e.getField(belongsToType.getLookupFieldName()).toString());
                }

            }
            value.setValues(valuesMap);
        } else {
            Map<Long, String> valuesMap = belongsToType.lookup("");
            value.setValues(valuesMap);
        }

        if (parentEntity != null && value.getValues() != null) {
            checkState(!getFieldPath().matches("\\."), "EntityComboBox doesn't support sequential path");

            if (viewValue == null || viewValue.getValue() == null) {
                Object field = parentEntity.getField(getFieldPath());
                if (field != null) {
                    Entity fieldEntity = null;
                    if (field instanceof Entity) {
                        fieldEntity = (Entity) field;
                    } else {
                        fieldEntity = belongsToType.getDataDefinition().get(Long.parseLong(field.toString()));
                    }

                    if (fieldEntity != null) {
                        Long entityId = fieldEntity.getId();
                        value.setSelectedValue(entityId);
                        selectedEntities.put(getPath(), fieldEntity);
                    }
                }
            } else {
                Long selectedValue = viewValue.getValue().getSelectedValue();
                value.setSelectedValue(selectedValue);
                Entity fieldEntity = belongsToType.getDataDefinition().get(selectedValue);
                selectedEntities.put(getPath(), fieldEntity);
            }

        }

        // combobox always must be submitted - we need to populate options, but don't change selected value if ignoreMode is true
        if (viewValue != null && viewValue.getValue() != null && viewValue.isIgnoreMode()) {
            value.setSelectedValue(viewValue.getValue().getSelectedValue());
        }

        ViewValue<EntityComboBoxValue> newViewValue = new ViewValue<EntityComboBoxValue>(value);

        FieldDefinition fieldDefinition = getFieldDefinition();

        if (fieldDefinition.isRequired() || (entity == null && fieldDefinition.isRequiredOnCreate())) {
            newViewValue.getValue().setRequired(true);
        }

        if (fieldDefinition.isReadOnly() || (entity != null && fieldDefinition.isReadOnlyOnUpdate())) {
            newViewValue.setEnabled(false);
        }

        return newViewValue;
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                + ".label");
        messageCodes.add(getTranslationService().getEntityFieldBaseMessageCode(getParentContainer().getDataDefinition(),
                getName())
                + ".label");
        translationsMap.put(messageCodes.get(0), getTranslationService().translate(messageCodes, locale));
        if (isHasDescription()) {
            String descriptionCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                    + getPath() + ".description";
            translationsMap.put(descriptionCode, getTranslationService().translate(descriptionCode, locale));
            String descriptionHeaderCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                    + getPath() + ".descriptionHeader";
            translationsMap.put(
                    descriptionHeaderCode,
                    getTranslationService().translate(
                            Arrays.asList(new String[] { descriptionHeaderCode, "core.form.descriptionHeader" }), locale));
        }
        translationsMap
                .put(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                        + ".blankValue",
                        getTranslationService().translate(
                                Arrays.asList(new String[] {
                                        getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                                                + getPath() + ".blankValue", "core.form.blankComboBoxValue" }), locale));
    }

    private BelongsToType getBelongsToType(final DataDefinition dataDefinition, final String fieldPath) {
        checkState(!fieldPath.matches("\\."), "EntityComboBox doesn't support sequential path");
        FieldDefinition fieldDefinition = dataDefinition.getField(fieldPath);
        if (fieldDefinition != null && fieldDefinition.getType() instanceof BelongsToType) {
            return (BelongsToType) fieldDefinition.getType();
        } else {
            throw new IllegalStateException("EntityComboBox data definition cannot be found");
        }
    }

    private HasManyType getHasManyType(final DataDefinition dataDefinition, final String fieldPath) {
        checkState(!fieldPath.matches("\\."), "EntityComboBox doesn't support sequential path");
        FieldDefinition fieldDefinition = dataDefinition.getField(fieldPath);
        if (fieldDefinition != null && fieldDefinition.getType() instanceof HasManyType) {
            return (HasManyType) fieldDefinition.getType();
        } else {
            throw new IllegalStateException("EntityComboBox data definition cannot be found");
        }
    }
}
