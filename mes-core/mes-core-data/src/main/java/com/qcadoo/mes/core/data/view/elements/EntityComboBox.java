package com.qcadoo.mes.core.data.view.elements;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.internal.types.BelongsToType;
import com.qcadoo.mes.core.data.internal.types.HasManyType;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.view.AbstractComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;
import com.qcadoo.mes.core.data.view.elements.comboBox.EntityComboBoxValue;

public class EntityComboBox extends AbstractComponent<EntityComboBoxValue> {

    public EntityComboBox(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "entityComboBox";
    }

    @Override
    public void addComponentOptions(final Map<String, Object> viewOptions) {

    }

    @Override
    public ViewValue<EntityComboBoxValue> castComponentValue(final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        JSONObject valueObject = viewObject.getJSONObject("value");
        if (!valueObject.isNull("selectedValue")) {
            EntityComboBoxValue value = new EntityComboBoxValue();
            Long selectedEntityId = Long.parseLong(valueObject.getString("selectedValue"));
            value.setSelectedValue(selectedEntityId);
            Entity selectedEntity = getDataDefinition().get(selectedEntityId);
            selectedEntities.put(getPath(), selectedEntity);
            return new ViewValue<EntityComboBoxValue>(value);
        }
        return new ViewValue<EntityComboBoxValue>();
    }

    @Override
    public ViewValue<EntityComboBoxValue> getComponentValue(final Entity entity, Entity parentEntity,
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
            if (selectedEntity != null) {
                SearchCriteriaBuilder searchCriteriaBuilder = hasManyType.getDataDefinition().find();
                searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(hasManyType
                        .getDataDefinition().getField(hasManyType.getFieldName()), selectedEntity.getId()));
                SearchResult rs = searchCriteriaBuilder.list();

                Map<Long, String> valuesMap = new HashMap<Long, String>();
                for (Entity e : rs.getEntities()) {
                    valuesMap.put(e.getId(), e.getField(belongsToType.getLookupFieldName()).toString());
                }
                value.setValues(valuesMap);
            }

        } else {

            Map<Long, String> valuesMap = belongsToType.lookup("");
            value.setValues(valuesMap);
        }

        Entity selectedEntity = parentEntity;
        if (selectedEntity == null) {
            // selectedEntity = selectedEntities.get(getSourceComponent().getPath());
            // selectedEntity = getParentContainer().g
        }
        if (selectedEntity != null) {
            System.out.println("AAAAA: " + getName() + " - " + selectedEntity.getId() + " - "
                    + selectedEntity.getField(getFieldPath()));
            checkState(!getFieldPath().matches("\\."), "EntityComboBox doesn't support sequential path");
            Entity field = (Entity) selectedEntity.getField(getFieldPath());

            if (field != null) {
                System.out.println("AAAAA: " + getName() + " - " + field.getId());
                Long entityId = field.getId();
                value.setSelectedValue(entityId);
                selectedEntities.put(getPath(), field);
            }
        }

        return new ViewValue<EntityComboBoxValue>(value);
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewName() + "." + getPath() + ".label");
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
