package com.qcadoo.mes.orderSupplies.rowStyleResolvers;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.constants.RowStyle;

@Service
public class MaterialRequirementCoverageRowStyleResolver {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Set<String> fillRowStyles(final Entity coverageProduct) {
        final Set<String> rowStyles = Sets.newHashSet();

        Entity materialRequirementCoverage = coverageProduct
                .getBelongsToField(CoverageProductFields.MATERIAL_REQUIREMENT_COVERAGE);
        Date coverageToDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.COVERAGE_TO_DATE);
        boolean includeDraftDeliveries = materialRequirementCoverage
                .getBooleanField(MaterialRequirementCoverageFields.INCLUDE_DRAFT_DELIVERIES);

        String style = determineRowStyle(coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT), coverageToDate,
                includeDraftDeliveries);
        if (style != null) {
            rowStyles.add(style);
        }
        return rowStyles;
    }

    private String determineRowStyle(final Entity product, final Date coverageToDate, final boolean includeDraftDeliveries) {
        StringBuilder query = new StringBuilder();
        query.append("select delivery.state as state ");
        query.append("from #deliveries_delivery delivery ");
        query.append("join delivery.orderedProducts orderedProduct ");
        query.append("where delivery.active = true and delivery.deliveryDate <= :deliveryDate ");
        query.append("and orderedProduct.product = :productId and delivery.state in (:states) ");
        List<String> states = Lists.newArrayList(DeliveryStateStringValues.APPROVED);
        if (includeDraftDeliveries) {
            states.add(DeliveryStateStringValues.DRAFT);
            states.add(DeliveryStateStringValues.PREPARED);
            states.add(DeliveryStateStringValues.DURING_CORRECTION);
        }
        StringBuilder queryForDelivered = new StringBuilder();

        queryForDelivered.append("select count(delivery.id) as cnt ");
        queryForDelivered.append("from #deliveries_delivery delivery ");
        queryForDelivered.append("join delivery.deliveredProducts deliveredProduct ");
        queryForDelivered.append("where delivery.active = true and delivery.deliveryDate <= :deliveryDate ");
        queryForDelivered.append("and deliveredProduct.product = :productId and delivery.state = '07receiveConfirmWaiting' ");

        long deliveredCount = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY)
                .find(queryForDelivered.toString()).setLong("productId", product.getId()).setDate("deliveryDate", coverageToDate)
                .setMaxResults(1).uniqueResult().getLongField("cnt");
        if (deliveredCount > 0) {
            return RowStyle.YELLOW_BACKGROUND;
        }
        List<Entity> orderedDeliveries = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY).find(query.toString())
                .setLong("productId", product.getId()).setDate("deliveryDate", coverageToDate).setParameterList("states", states)
                .list().getEntities();
        if (orderedDeliveries.isEmpty()) {
            return null;
        }
        List<String> orderedStates = orderedDeliveries.stream().map(delivery -> delivery.getStringField(DeliveryFields.STATE))
                .collect(Collectors.toList());
        if (orderedStates.stream().allMatch(state -> state.equals(DeliveryStateStringValues.APPROVED))) {
            return RowStyle.GREEN_BACKGROUND;
        } else {
            return RowStyle.YELLOW_BACKGROUND;
        }
    }

}
