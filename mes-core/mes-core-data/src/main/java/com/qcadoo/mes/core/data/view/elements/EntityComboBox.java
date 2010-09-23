package com.qcadoo.mes.core.data.view.elements;

import static com.google.common.base.Preconditions.checkState;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.internal.types.BelongsToType;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
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
        EntityComboBoxValue value = new EntityComboBoxValue();
        if (!valueObject.isNull("selectedValue")) {
            value.setSelectedValue(Long.parseLong(valueObject.getString("selectedValue")));
        }
        return new ViewValue<EntityComboBoxValue>(value);
    }

    @Override
    public ViewValue<EntityComboBoxValue> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<EntityComboBoxValue> viewValue, final Set<String> pathsToUpdate) {

        DataDefinition parentDefinition = getParentContainer().getDataDefinition();
        EntityComboBoxValue value = new EntityComboBoxValue();

        if (getSourceFieldPath() != null) {
            // TODO mina
        } else {
            BelongsToType belongsToType = getBelongsToType(parentDefinition, getFieldPath());
            Map<Long, String> valuesMap = belongsToType.lookup("");
            value.setValues(valuesMap);
        }

        if (entity != null) {
            checkState(!getFieldPath().matches("\\."), "EntityComboBox doesn't support sequential path");
            Long entityId = ((Entity) entity.getField(getFieldPath())).getId();
            value.setSelectedValue(entityId);
        }

        return new ViewValue<EntityComboBoxValue>(value);
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        DataDefinition parentDefinition = getParentContainer().getDataDefinition();
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewName() + "." + getPath() + ".label");
        messageCodes.add(parentDefinition.getName() + "." + getName() + ".label");
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
}
