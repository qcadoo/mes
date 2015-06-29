package com.qcadoo.mes.materialFlowResources.rowStyleResolvers;

import com.google.common.collect.Sets;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.constants.RowStyle;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WarehouseStocksListResolver {

    public Set<String> fillRowStyles(final Entity warehouseStocks) {
        final Set<String> rowStyles = Sets.newHashSet();

        if (warehouseStocks.getDecimalField("minimumState") != null) {

            if (BigDecimalUtils.convertNullToZero(warehouseStocks.getDecimalField("minimumState"))
                    .compareTo(BigDecimalUtils.convertNullToZero(warehouseStocks.getDecimalField("quantity"))) == 1) {
                rowStyles.add(RowStyle.RED_BACKGROUND);
            }

        }

        return rowStyles;
    }

}
