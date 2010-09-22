package com.qcadoo.mes.core.data.view.elements;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.view.AbstractComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;
import com.qcadoo.mes.core.data.view.elements.comboBox.ComboBoxValue;

public class DynamicComboBox extends AbstractComponent<ComboBoxValue> {

    public DynamicComboBox(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "dynamicComboBox";
    }

    @Override
    public void addComponentOptions(final Map<String, Object> viewOptions) {

    }

    @Override
    public ViewValue<ComboBoxValue> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        // JSONObject value = viewObject.getJSONObject("value");
        //
        // if (value != null) {
        // String selectedEntityId = value.getString("selectedEntityId");
        //
        // if (selectedEntityId != null && !"null".equals(selectedEntityId)) {
        // Entity selectedEntity = getDataDefinition().get(Long.parseLong(selectedEntityId));
        // selectedEntities.put(getPath(), selectedEntity);
        // }
        // }
        //
        // return new ViewValue<ComboBoxValue>();
        return null;
    }

    @Override
    public ViewValue<ComboBoxValue> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<ComboBoxValue> viewEntity, final Set<String> pathsToUpdate) {

        // if ((getSourceFieldPath() != null && getSourceComponent() != null) || getFieldPath() != null) {
        // if (entity == null) {
        // return new ViewValue<ListData>(new ListData(0, Collections.<Entity> emptyList()));
        // }
        // HasManyType hasManyType = null;
        // if (getFieldPath() != null) {
        // hasManyType = getHasManyType(getParentContainer().getDataDefinition(), getFieldPath());
        // } else {
        // hasManyType = getHasManyType(getSourceComponent().getDataDefinition(), getSourceFieldPath());
        // }
        // checkState(hasManyType.getDataDefinition().getName().equals(getDataDefinition().getName()),
        // "Grid and hasMany relation have different data definitions");
        // SearchCriteriaBuilder searchCriteriaBuilder = getDataDefinition().find();
        // searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(
        // getDataDefinition().getField(hasManyType.getFieldName()), entity.getId()));
        // SearchResult rs = searchCriteriaBuilder.list();
        // return new ViewValue<ListData>(generateListData(rs));
        // } else {
        // SearchResult rs = getDataDefinition().find().list();
        // return new ViewValue<ListData>(generateListData(rs));
        // }
        return null;
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewName() + "." + getPath() + ".label");
        messageCodes.add(getDataDefinition().getName() + "." + getName() + ".label");
        translationsMap.put(messageCodes.get(0), translationService.translate(messageCodes, locale));
    }
}
