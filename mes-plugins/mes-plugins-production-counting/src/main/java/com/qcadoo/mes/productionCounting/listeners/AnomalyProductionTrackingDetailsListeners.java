package com.qcadoo.mes.productionCounting.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.AnomalyFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.newstates.ProductionTrackingStateServiceMarker;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AnomalyProductionTrackingDetailsListeners {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnomalyProductionTrackingDetailsListeners.class);

    public static final String COMPONENTS_LOCATION = "componentsLocation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private SecurityService securityService;

    public void perform(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        try {
            perform(view);
        } catch (EntityRuntimeException ex) {
            ex.getGlobalErrors().forEach(view::addMessage);
        } catch (Exception ex) {
            LOGGER.warn("Error when perform create anomalies", ex);
            view.addMessage(new ErrorMessage("qcadooView.errorPage.error.internalError.explanation", false));
        }
    }

    public void performAndAccept(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        try {
            boolean performed = perform(view);
            tryAcceptProductionTracking(view, performed);
        } catch (EntityRuntimeException ex) {
            ex.getGlobalErrors().forEach(view::addMessage);
        } catch (Exception ex) {
            LOGGER.warn("Error when perform create anomalies", ex);
            view.addMessage(new ErrorMessage("qcadooView.errorPage.error.internalError.explanation", false));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void tryAcceptProductionTracking(ViewDefinitionState view, boolean performed) throws JSONException {
        if (performed) {
            Long productionRecordId = view.getJsonContext().getLong("window.mainTab.form.productionTrackingId");
            Entity productionRecord = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).get(productionRecordId);
            Long userId = securityService.getCurrentUserId();

            productionRecord.setField("user", userId);
            String userLogin = securityService.getCurrentUserName();

            stateExecutorService.changeState(ProductionTrackingStateServiceMarker.class, productionRecord, userLogin,
                    ProductionTrackingStateStringValues.ACCEPTED);

            if (productionRecord.isValid()) {
                view.addMessage("productionCounting.anomalyProductionTrackingDetails.productionTrackingAccepted",
                        ComponentState.MessageType.SUCCESS);
            } else {
                view.addMessage("productionCounting.anomalyProductionTrackingDetails.productionTrackingNotAccepted",
                        ComponentState.MessageType.FAILURE);
                productionRecord.getGlobalErrors().forEach(view::addMessage);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private boolean perform(ViewDefinitionState view) {
        boolean valid = validate(view);
        if (valid) {
            createAnomalies(view);
            clearQuantitiesInRR(view);
            view.addMessage("productionCounting.anomalyProductionTrackingDetails.reasonsCreated",
                    ComponentState.MessageType.SUCCESS);
            CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
            generated.setChecked(true);
            return true;
        } else {
            view.addMessage(new ErrorMessage("productionCounting.anomalyReasonDetails.reasonIsRequired", false));
            return false;
        }
    }

    private void clearQuantitiesInRR(ViewDefinitionState view) {
        AwesomeDynamicListComponent anomalyProductionTrackingEntriesADL = (AwesomeDynamicListComponent) view
                .getComponentByReference("anomalyProductionTrackingEntries");
        List<Entity> entities = anomalyProductionTrackingEntriesADL.getEntities();
        for (Entity entity : entities) {
            Entity trackingOperationProductInComponent = entity.getBelongsToField("trackingOperationProductInComponent");
            trackingOperationProductInComponent
                    .setField(TrackingOperationProductInComponentFields.USED_QUANTITY, BigDecimal.ZERO);
            trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                    BigDecimal.ZERO);
            trackingOperationProductInComponent = trackingOperationProductInComponent.getDataDefinition().save(
                    trackingOperationProductInComponent);
            if (!trackingOperationProductInComponent.isValid()) {
                throw new EntityRuntimeException(trackingOperationProductInComponent);
            }
        }
    }

    private void createAnomalies(final ViewDefinitionState view) {
        AwesomeDynamicListComponent anomalyProductionTrackingEntriesADL = (AwesomeDynamicListComponent) view
                .getComponentByReference("anomalyProductionTrackingEntries");
        List<Entity> entities = anomalyProductionTrackingEntriesADL.getEntities();
        for (Entity entity : entities) {
            createAnomaly(entity);
        }
    }

    private void createAnomaly(final Entity entity) {
        Entity anomaly = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_ANOMALY).create();
        Entity productionTracking = entity.getBelongsToField("trackingOperationProductInComponent").getBelongsToField(
                TrackingOperationProductInComponentFields.PRODUCTION_TRACKING);
        Entity product = entity.getBelongsToField("trackingOperationProductInComponent").getBelongsToField(
                TrackingOperationProductInComponentFields.PRODUCT);
        anomaly.setField(AnomalyFields.NUMBER, numberGeneratorService.generateNumber(
                ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_ANOMALY));
        anomaly.setField(AnomalyFields.PRODUCTION_TRACKING, productionTracking);
        anomaly.setField(AnomalyFields.PRODUCT, product);
        anomaly.setField(AnomalyFields.USED_QUANTITY, entity.getDecimalField("usedQuantity"));
        anomaly.setField(AnomalyFields.STATE, "01draft");
        anomaly.setField(AnomalyFields.ISSUED, false);
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        if (Objects.isNull(order.getBelongsToField("root"))) {
            anomaly.setField(AnomalyFields.MASTER_PRODUCT, order.getBelongsToField(OrderFields.PRODUCT));
        } else {
            anomaly.setField(AnomalyFields.MASTER_PRODUCT, order.getBelongsToField("root").getBelongsToField(OrderFields.PRODUCT));
        }
        List<Entity> reasons = Lists.newArrayList();
        List<Entity> anomalyContainers = entity.getHasManyField("anomalyReasons");
        anomalyContainers.forEach(ac -> {
            reasons.add(ac.getBelongsToField("anomalyReason"));
        });
        anomaly.setField(AnomalyFields.ANAOMALY_REASONS, reasons);

        anomaly.setField(AnomalyFields.LOCATION, findLocation(productionTracking, product));

        anomaly = anomaly.getDataDefinition().save(anomaly);
        if (!anomaly.isValid()) {
            throw new EntityRuntimeException(anomaly);
        }
    }

    private Entity findLocation(final Entity productionTracking, final Entity product) {

        Optional<Entity> maybe = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER,
                        productionTracking.getBelongsToField(ProductionTrackingFields.ORDER)))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                        ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue())).list().getEntities().stream()
                .findFirst();

        if (maybe.isPresent()) {
            return maybe.get().getBelongsToField(COMPONENTS_LOCATION);
        }
        return null;
    }

    private boolean validate(final ViewDefinitionState view) {
        AwesomeDynamicListComponent anomalyProductionTrackingEntriesADL = (AwesomeDynamicListComponent) view
                .getComponentByReference("anomalyProductionTrackingEntries");
        List<Entity> entities = anomalyProductionTrackingEntriesADL.getEntities();
        boolean valid = Boolean.TRUE;
        for (Entity entry : entities) {
            List<Entity> anomalyContainers = entry.getHasManyField("anomalyReasons");
            if (anomalyContainers.isEmpty()) {
                valid = Boolean.FALSE;
            }
            for (Entity ac : anomalyContainers) {
                if (Objects.isNull(ac.getBelongsToField("anomalyReason"))) {
                    valid = Boolean.FALSE;
                }
            }
        }
        return valid;
    }

}
