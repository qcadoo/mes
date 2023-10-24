package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

        oldEntries.forEach(oldEntry -> oldEntry.getDataDefinition().delete(oldEntry.getId()));
    }

    public final void showProductsRunningOutOfStock(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent isStockForDaysFilterCheckBox = (CheckBoxComponent) view.getComponentByReference("isStockForDaysFilter");

        isStockForDaysFilterCheckBox.setChecked(true);
        isStockForDaysFilterCheckBox.requestComponentUpdateState();
    }

    public final void showProductsAll(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent isStockForDaysFilterCheckBox = (CheckBoxComponent) view.getComponentByReference("isStockForDaysFilter");

        isStockForDaysFilterCheckBox.setChecked(false);
        isStockForDaysFilterCheckBox.requestComponentUpdateState();
    }

    private DataDefinition getSalesVolumeMultiDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_VOLUME_MULTI);
    }

}
