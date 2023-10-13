package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.masterOrders.constants.DocumentPositionParametersFieldsMO;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesVolumeFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesVolumesListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void addSalesVolumes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deleteOldEntries();

        Entity salesVolumeMulti = getSalesVolumeMultiDD().create();

        salesVolumeMulti = salesVolumeMulti.getDataDefinition().save(salesVolumeMulti);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", salesVolumeMulti.getId());

        String url = "../page/masterOrders/salesVolumeAddMulti.html";

        view.openModal(url, parameters);
    }

    private void deleteOldEntries() {
        DateTime currentDate = DateTime.now().minusDays(1);

        List<Entity> oldEntries = getSalesVolumeMultiDD().find().add(SearchRestrictions.lt("updateDate", currentDate.toDate()))
                .list().getEntities();

        oldEntries.forEach(e -> e.getDataDefinition().delete(e.getId()));
    }

    public final void showProductsRunningOutOfStock(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent salesVolumesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> salesVolumes = salesVolumesGrid.getEntities();

        Integer runningOutOfStockDays = IntegerUtils.convertNullToZero(getDocumentPositionParameters().getIntegerField(DocumentPositionParametersFieldsMO.RUNNING_OUT_OF_STOCK_DAYS));

        salesVolumesGrid.setEntities(salesVolumes.stream().filter(salesVolume ->
                        runningOutOfStockDays.compareTo(salesVolume.getIntegerField(SalesVolumeFields.STOCK_FOR_DAYS)) > 0)
                .collect(Collectors.toList())
        );
    }

    public final void showProductsAll(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent salesVolumesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        salesVolumesGrid.performEvent(view, "refresh");
    }

    private DataDefinition getSalesVolumeMultiDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_VOLUME_MULTI);
    }

    private Entity getDocumentPositionParameters() {
        return getDocumentPositionParametersDD().find().setMaxResults(1).uniqueResult();
    }

    private DataDefinition getDocumentPositionParametersDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS);
    }

}
