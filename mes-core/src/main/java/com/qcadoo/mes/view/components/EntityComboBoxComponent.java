package com.qcadoo.mes.view.components;

import static com.google.common.base.Preconditions.checkState;

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
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.components.combobox.EntityComboBoxValue;

public final class EntityComboBoxComponent extends AbstractComponent<EntityComboBoxValue> {

    public EntityComboBoxComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "entityComboBox";
    }

    @Override
    public ViewValue<EntityComboBoxValue> castComponentValue(final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        JSONObject valueObject = viewObject.getJSONObject("value");
        if (!valueObject.isNull("selectedValue")) {
            EntityComboBoxValue value = new EntityComboBoxValue();
            if (!"".equals(valueObject.getString("selectedValue"))) {
                Long selectedEntityId = Long.parseLong(valueObject.getString("selectedValue"));
                value.setSelectedValue(selectedEntityId);
                Entity selectedEntity = getDataDefinition().get(selectedEntityId);
                selectedEntities.put(getPath(), selectedEntity);
            }
            return new ViewValue<EntityComboBoxValue>(value);
        }
        return new ViewValue<EntityComboBoxValue>(null);
    }

    @Override
    public ViewValue<EntityComboBoxValue> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<EntityComboBoxValue> viewValue,
            final Set<String> pathsToUpdate) {

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
        return new ViewValue<EntityComboBoxValue>(value);
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                + ".label");
        messageCodes.add(translationService.getEntityFieldMessageCode(getParentContainer().getDataDefinition(), getName()));
        translationsMap.put(messageCodes.get(0), translationService.translate(messageCodes, locale));
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
