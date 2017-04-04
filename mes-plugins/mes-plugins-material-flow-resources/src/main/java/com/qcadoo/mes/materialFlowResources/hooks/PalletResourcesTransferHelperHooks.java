package com.qcadoo.mes.materialFlowResources.hooks;

import static com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields.LOCATION_NUMBER;
import static com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields.PALLET_NUMBER;
import static com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER;
import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.dto.MovedPalletDto;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PalletResourcesTransferHelperHooks {

    public static final String L_PALLET_STORAGE_STATE_DTOS = "palletStorageStateDtos";

    private static final String L_PALLET_NUMBER = "palletNumber";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        JSONObject context = view.getJsonContext();
        Set<Long> palletIds = Arrays.stream(
                context.getString("window.mainTab.helper.gridLayout.selectedEntities").replaceAll("[\\[\\]]", "").split(","))
                .map(Long::valueOf).collect(Collectors.toSet());
        String palletNumberFilter = context.getString("window.mainTab.helper.gridLayout.palletNumberFilter");

        Entity helper = form.getEntity();
        if (helper.getHasManyField(L_PALLET_STORAGE_STATE_DTOS).isEmpty()) {
            List<Entity> generatedEntities = createHelperEntities(palletIds);

            if (isNotBlank(palletNumberFilter) && !palletNumberFilter.equals("NULL")) {
                List<String> numbersOrder = Arrays.asList(palletNumberFilter.replaceAll("[\\[\\]]", "").split(","));
                generatedEntities.sort(comparing(ge -> numbersOrder.indexOf(ge.getStringField(L_PALLET_NUMBER))));
            }

            helper.setField(L_PALLET_STORAGE_STATE_DTOS, generatedEntities);
            form.setEntity(helper);
            setStorageLocationFilters(view);
        }
    }

    private List<Entity> createHelperEntities(final Set<Long> palletIds) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT palletNumber, storageLocationNumber, locationNumber ");
        sql.append("FROM materialflowresources_palletstoragestatedto ");
        sql.append("WHERE id IN (:ids)");

        Map<String, Object> params = Maps.newHashMap();
        params.put("ids", palletIds);
        List<MovedPalletDto> result = jdbcTemplate.query(sql.toString(), params, new BeanPropertyRowMapper(MovedPalletDto.class));
        return result.stream().map(this::mapDtoToEntity).collect(Collectors.toList());
    }

    private Entity mapDtoToEntity(final MovedPalletDto dto) {
        Entity entity = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "palletStorageStateDto")
                .create();
        entity.setField(PALLET_NUMBER, dto.getPalletNumber());
        entity.setField(STORAGE_LOCATION_NUMBER, dto.getStorageLocationNumber());
        entity.setField(LOCATION_NUMBER, dto.getLocationNumber());
        return entity;
    }

    private void setStorageLocationFilters(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(L_PALLET_STORAGE_STATE_DTOS);
        for (FormComponent form : adl.getFormComponents()) {
            LookupComponent newPalletNumber = (LookupComponent) form.findFieldComponentByName("newPalletNumber");
            FilterValueHolder filter = newPalletNumber.getFilterValue();
            Entity persistedEntity = form.getPersistedEntityWithIncludedFormValues();
            filter.put(LOCATION_NUMBER, persistedEntity.getStringField(LOCATION_NUMBER));
            filter.put(PALLET_NUMBER, persistedEntity.getStringField(PALLET_NUMBER));
            newPalletNumber.setFilterValue(filter);
        }
    }

}
