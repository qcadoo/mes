package com.qcadoo.mes.productionCounting.hooks;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentDtoFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AnomalyProductionTrackingDetailsHooks {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnomalyProductionTrackingDetailsHooks.class);



    private static final String L_ACTIONS = "actions";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        updateRibbon(view);
        if (view.isViewAfterRedirect()) {
            try {
                fillADL(view);
            } catch (JSONException e) {
                LOGGER.warn("Error when get value from JsonContext", e);
            }
        }
    }

    private void fillADL(final ViewDefinitionState view) throws JSONException {
        String selectedTOPICs = view.getJsonContext().getString("window.mainTab.form.selectedTOPICs");
        boolean performAndAcceptFlag = view.getJsonContext().getBoolean("window.mainTab.form.performAndAccept");
        Entity defaultReason = findDefaultReason();
        List<Long> ids = Lists.newArrayList(Longs.stringConverter().convertAll(
                Splitter.on(',').trimResults().omitEmptyStrings().splitToList(selectedTOPICs)));
        List<Entity> entries = Lists.newArrayList();
        ids.forEach(id -> {
            createADLEntry(entries, id, performAndAcceptFlag, defaultReason);
        });
        updateADLState(view, entries);
    }

    private Entity findDefaultReason() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, "anomalyReason").find()
                .add(SearchRestrictions.eq("defaultReason", true)).setMaxResults(1).uniqueResult();
    }

    private void createADLEntry(final List<Entity> entries, final Long id, final boolean fillDefaultReason,
            final Entity defalutReason) {
        Entity entry = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                "anomalyProductionTrackingEntryHelper").create();
        Entity trackingOperationProductInComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT_DTO).get(id);

        entry.setField("trackingOperationProductInComponent", trackingOperationProductInComponent);
        entry.setField("productNumber",
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentDtoFields.PRODUCT_NUMBER));
        entry.setField("productName",
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentDtoFields.PRODUCT_NAME));
        entry.setField("plannedQuantity", trackingOperationProductInComponent
                .getDecimalField(TrackingOperationProductInComponentDtoFields.PLANNED_QUANTITY));
        entry.setField("usedQuantity",
                trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentDtoFields.USED_QUANTITY));
        entry.setField("productUnit",
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentDtoFields.PRODUCT_UNIT));
        if (fillDefaultReason && Objects.nonNull(defalutReason)) {
            List<Entity> anomalyReasons = Lists.newArrayList();
            Entity reason = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, "anomalyReasonContainer")
                    .create();
            reason.setField("anomalyReason", defalutReason);
            anomalyReasons.add(reason);
            entry.setField("anomalyReasons", anomalyReasons);
        }
        entries.add(entry);
    }

    private void updateADLState(final ViewDefinitionState view, final List<Entity> entries) {
        AwesomeDynamicListComponent anomalyProductionTrackingEntriesADL = (AwesomeDynamicListComponent) view
                .getComponentByReference("anomalyProductionTrackingEntries");
        anomalyProductionTrackingEntriesADL.setFieldValue(entries);
        anomalyProductionTrackingEntriesADL.requestComponentUpdateState();
    }

    private void updateRibbon(final ViewDefinitionState view) {
        try {
            WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
            RibbonGroup actions = window.getRibbon().getGroupByName(L_ACTIONS);

            RibbonActionItem perform = actions.getItemByName("perform");
            RibbonActionItem performAndAccept = actions.getItemByName("performAndAccept");

            boolean performAndAcceptFlag = view.getJsonContext().getBoolean("window.mainTab.form.performAndAccept");
            perform.setEnabled(!performAndAcceptFlag);
            performAndAccept.setEnabled(performAndAcceptFlag);

            perform.requestUpdate(true);
            performAndAccept.requestUpdate(true);
        } catch (JSONException e) {

        }
    }
}
