package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.print.StocktakingReportService;
import com.qcadoo.mes.materialFlowResources.print.helper.Resource;
import com.qcadoo.mes.materialFlowResources.print.helper.ResourceDataProvider;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StocktakingDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(StocktakingDetailsListeners.class);

    @Autowired
    private StocktakingReportService reportService;

    @Autowired
    private ResourceDataProvider resourceDataProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void generate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save");

        if (state.isHasError()) {
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity report = form.getEntity();
        Entity reportDb = report.getDataDefinition().get(report.getId());
        List<Resource> resources = resourceDataProvider.findResourcesAndGroup(reportDb
                .getBelongsToField(StocktakingFields.LOCATION).getId(), reportDb.getHasManyField(StocktakingFields.STORAGE_LOCATIONS).stream().map(Entity::getId).collect(Collectors.toList()), reportDb
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
            positions.add(position);
        }
        reportDb.setField(StocktakingFields.POSITIONS, positions);
        reportDb.setField(StocktakingFields.GENERATED, Boolean.TRUE);
        reportDb.setField(StocktakingFields.GENERATION_DATE, new Date());
        reportDb = reportDb.getDataDefinition().save(reportDb);
        try {
            reportService.generateReport(state, reportDb);
        } catch (Exception e) {
            LOG.error("Error when generate stocktaking report", e);
            throw new IllegalStateException(e.getMessage(), e);
        }
        state.performEvent(view, "reset", new String[0]);

    }

    public void copyFromStock(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        for (Entity position : entity.getHasManyField(StocktakingFields.POSITIONS)) {
            position.setField(StocktakingPositionFields.QUANTITY, position.getDecimalField(StocktakingPositionFields.STOCK));
            position.getDataDefinition().save(position);
        }
    }

    public void settle(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        List<Entity> differences = new ArrayList<>();
        DataDefinition differenceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STOCKTAKING_DIFFERENCE);
        for (Entity position : entity.getHasManyField(StocktakingFields.POSITIONS)) {
            BigDecimal positionQuantity = position.getDecimalField(StocktakingPositionFields.QUANTITY);
            BigDecimal positionStock = position.getDecimalField(StocktakingPositionFields.STOCK);
            if (positionQuantity != null && positionStock.compareTo(positionQuantity) != 0) {
                Entity differenceEntity = differenceDD.create();
                differenceEntity.setField(StocktakingDifferenceFields.STORAGE_LOCATION, position.getBelongsToField(StocktakingPositionFields.STORAGE_LOCATION));
                differenceEntity.setField(StocktakingDifferenceFields.PALLET_NUMBER, position.getBelongsToField(StocktakingPositionFields.PALLET_NUMBER));
                differenceEntity.setField(StocktakingDifferenceFields.TYPE_OF_LOAD_UNIT, position.getBelongsToField(StocktakingPositionFields.TYPE_OF_LOAD_UNIT));
                differenceEntity.setField(StocktakingDifferenceFields.PRODUCT, position.getBelongsToField(StocktakingPositionFields.PRODUCT));
                differenceEntity.setField(StocktakingDifferenceFields.BATCH, position.getBelongsToField(StocktakingPositionFields.BATCH));
                differenceEntity.setField(StocktakingDifferenceFields.EXPIRATION_DATE, position.getDateField(StocktakingPositionFields.EXPIRATION_DATE));
                BigDecimal difference = positionQuantity.subtract(positionStock);
                differenceEntity.setField(StocktakingDifferenceFields.QUANTITY, difference);
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    differenceEntity.setField(StocktakingDifferenceFields.TYPE, StocktakingDifferenceType.SURPLUS.getStringValue());
                } else {
                    differenceEntity.setField(StocktakingDifferenceFields.TYPE, StocktakingDifferenceType.SHORTAGE.getStringValue());
                }
                differences.add(differenceEntity);
            }
        }
        entity.setField(StocktakingFields.DIFFERENCES, differences);
        entity.getDataDefinition().save(entity);
    }

    public void print(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printReport(view, state);
    }
}
