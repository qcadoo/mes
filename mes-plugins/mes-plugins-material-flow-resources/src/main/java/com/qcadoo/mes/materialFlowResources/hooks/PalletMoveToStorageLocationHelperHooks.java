package com.qcadoo.mes.materialFlowResources.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.dto.MovedPalletDto;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PalletMoveToStorageLocationHelperHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_PALLET_NUMBER = "palletNumber";

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        JSONObject context = view.getJsonContext();
        List<Long> palletIds = Lists
                .newArrayList(
                        context.getString("window.mainTab.helper.gridLayout.selectedEntities").replace("[", "").replace("]", "")
                                .split(",")).stream().map(Long::valueOf).collect(Collectors.toList());
        String palletNumberFilter = context.getString("window.mainTab.helper.gridLayout.palletNumberFilter");

        Entity helper = form.getEntity();
        if (helper.getHasManyField("palletStorageStateDtos").isEmpty()) {
            List<Entity> generatedEntities = createHelperEntities(palletIds);

            if (!Strings.isNullOrEmpty(palletNumberFilter)) {
                ArrayList<String> numbersOrder = Lists.newArrayList(palletNumberFilter.replace("[", "").replace("]", "")
                        .split(","));
                generatedEntities.sort((o1, o2) -> {
                    String number1 = o1.getStringField(L_PALLET_NUMBER);
                    String number2 = o2.getStringField(L_PALLET_NUMBER);
                    return new Integer(numbersOrder.indexOf(number1)).compareTo(numbersOrder.indexOf(number2));
                });
            }

            helper.setField("palletStorageStateDtos", generatedEntities);
            form.setEntity(helper);
            setStorageLocationFilters(view);
        }
    }

    private List<Entity> createHelperEntities(final List<Long> palletIds) {
        StringBuilder sql = new StringBuilder();
        sql.append("select palletNumber, storageLocationNumber ");
        sql.append("from materialflowresources_palletstoragestatedto ");
        sql.append("where id in (:ids)");

        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", palletIds);
        List<MovedPalletDto> result = jdbcTemplate.query(sql.toString(), params, new BeanPropertyRowMapper(MovedPalletDto.class));
        return result.stream().map(this::mapDtoToEntity).collect(Collectors.toList());
    }

    private Entity mapDtoToEntity(final MovedPalletDto dto) {
        Entity entity = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "palletStorageStateDto")
                .create();
        entity.setField("palletNumber", dto.getPalletNumber());
        entity.setField("storageLocationNumber", dto.getStorageLocationNumber());
        return entity;
    }

    private void setStorageLocationFilters(final ViewDefinitionState view) {
        DataDefinition storageLocationDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("palletStorageStateDtos");

        for (FormComponent form : adl.getFormComponents()) {
            LookupComponent newStorageLocation = (LookupComponent) form.findFieldComponentByName("newStorageLocation");
            FilterValueHolder filter = newStorageLocation.getFilterValue();
            Entity dto = form.getPersistedEntityWithIncludedFormValues();
            String oldStorageLocationNumber = dto.getStringField("storageLocationNumber");
            Entity oldStorageLocation = storageLocationDD.find()
                    .add(SearchRestrictions.eq(StorageLocationFields.NUMBER, oldStorageLocationNumber)).setMaxResults(1)
                    .uniqueResult();
            Entity location = oldStorageLocation.getBelongsToField(StorageLocationFields.LOCATION);
            filter.put(StorageLocationFields.LOCATION, location.getId());
            newStorageLocation.setFilterValue(filter);

        }
    }
}
