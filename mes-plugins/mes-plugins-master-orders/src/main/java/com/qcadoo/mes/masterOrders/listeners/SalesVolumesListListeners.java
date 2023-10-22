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

@Service
public class SalesVolumesListListeners {

    private static final String L_LT = "<";

    private static final String L_SPACE = " ";

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

        oldEntries.forEach(oldEntry -> oldEntry.getDataDefinition().delete(oldEntry.getId()));
    }

    public final void showProductsRunningOutOfStock(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent salesVolumesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Integer runningOutOfStockDays = IntegerUtils.convertNullToZero(getDocumentPositionParameters().getIntegerField(DocumentPositionParametersFieldsMO.RUNNING_OUT_OF_STOCK_DAYS));

        Map<String, String> filters = salesVolumesGrid.getFilters();

        filters.put(SalesVolumeFields.STOCK_FOR_DAYS, L_LT + runningOutOfStockDays);

        salesVolumesGrid.setFilters(filters);
    }

    public final void showProductsAll(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent salesVolumesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Map<String, String> filters = salesVolumesGrid.getFilters();

        filters.put(SalesVolumeFields.STOCK_FOR_DAYS, L_SPACE);

        salesVolumesGrid.setFilters(filters);
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