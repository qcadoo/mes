package com.qcadoo.mes.materialFlowResources.states;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingPositionFields;
import com.qcadoo.mes.materialFlowResources.print.StocktakingReportService;
import com.qcadoo.mes.materialFlowResources.print.helper.Resource;
import com.qcadoo.mes.materialFlowResources.print.helper.ResourceDataProvider;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateChangeDescriber;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateStringValues;
import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StocktakingStateService extends BasicStateService implements StocktakingServiceMarker {

    private static final Logger LOG = LoggerFactory.getLogger(StocktakingStateService.class);

    @Autowired
    private StocktakingStateChangeDescriber stocktakingStateChangeDescriber;

    @Autowired
    private StocktakingReportService reportService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceDataProvider resourceDataProvider;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return stocktakingStateChangeDescriber;
    }

    @Override
    public Entity onValidate(Entity entity, String sourceState, String targetState, Entity stateChangeEntity, StateChangeEntityDescriber describer) {
        switch (targetState) {
            case StocktakingStateStringValues.IN_PROGRESS:
                if (entity.getHasManyField(StocktakingFields.STORAGE_LOCATIONS).isEmpty()) {
                    entity.addGlobalError("materialFlowResources.error.stocktaking.storageLocationsRequired");
                }

                break;
        }

        return entity;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                              StateChangeEntityDescriber describer) {
        switch (targetState) {
            case StocktakingStateStringValues.IN_PROGRESS:
                List<Resource> resources = resourceDataProvider.findResourcesAndGroup(entity
                        .getBelongsToField(StocktakingFields.LOCATION).getId(), entity.getHasManyField(StocktakingFields.STORAGE_LOCATIONS).stream().map(Entity::getId).collect(Collectors.toList()), entity
                        .getStringField(StocktakingFields.CATEGORY), true);
                List<Entity> positions = new ArrayList<>();
                DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_STOCKTAKING_POSITION);
                for (Resource resource : resources) {
                    Entity position = positionDD.create();
                    position.setField(StocktakingPositionFields.STORAGE_LOCATION, resource.getStorageLocationId());
                    position.setField(StocktakingPositionFields.PALLET_NUMBER, resource.getPalletNumberId());
                    position.setField(StocktakingPositionFields.TYPE_OF_LOAD_UNIT, resource.getTypeOfLoadUnitId());
                    position.setField(StocktakingPositionFields.PRODUCT, resource.getProductId());
                    position.setField(StocktakingPositionFields.BATCH, resource.getBatchId());
                    position.setField(StocktakingPositionFields.EXPIRATION_DATE, resource.getExpirationDate());
                    position.setField(StocktakingPositionFields.STOCK, resource.getQuantity());
                    position.setField(StocktakingPositionFields.CONVERSION, resource.getConversion());
                    positions.add(position);
                }
                entity.setField(StocktakingFields.POSITIONS, positions);
                entity.setField(StocktakingFields.GENERATION_DATE, new Date());
                entity = entity.getDataDefinition().save(entity);
                try {
                    reportService.generateReport(entity);
                } catch (Exception e) {
                    LOG.error("Error when generate stocktaking report", e);
                    throw new IllegalStateException(e.getMessage(), e);
                }
                break;
        }

        return entity;
    }
}
