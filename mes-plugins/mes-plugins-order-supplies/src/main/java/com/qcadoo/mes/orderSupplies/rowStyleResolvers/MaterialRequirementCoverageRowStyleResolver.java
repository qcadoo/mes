package com.qcadoo.mes.orderSupplies.rowStyleResolvers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.orderSupplies.constants.IncludeInCalculationDeliveries;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.constants.RowStyle;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaterialRequirementCoverageRowStyleResolver {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Set<String> fillRowStyles(final Entity coverageProduct) {
        final Set<String> rowStyles = Sets.newHashSet();

        Entity materialRequirementCoverage = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE).get(
                coverageProduct.getIntegerField("materialRequirementCoverageId").longValue());
        Date coverageToDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.COVERAGE_TO_DATE);
        String includeInCalculationDeliveries = materialRequirementCoverage
                .getStringField(MaterialRequirementCoverageFields.INCLUDE_IN_CALCULATION_DELIVERIES);

        String style = determineRowStyle(coverageProduct.getIntegerField("productId").longValue(), coverageToDate, includeInCalculationDeliveries);
        if (style != null) {
            rowStyles.add(style);
        }
        return rowStyles;
    }

    private String determineRowStyle(final Long productId, final Date coverageToDate, final String includeInCalculationDeliveries) {
        StringBuilder query = new StringBuilder();
        query.append("select delivery.state as state ");
        query.append("from #deliveries_delivery delivery ");
        query.append("join delivery.orderedProducts orderedProduct ");
        query.append("where delivery.active = true and delivery.deliveryDate <= :deliveryDate ");
        query.append("and orderedProduct.product = :productId and delivery.state in (:states) ");
        List<String> states = Lists.newArrayList(DeliveryStateStringValues.APPROVED);

        if (IncludeInCalculationDeliveries.CONFIRMED_DELIVERIES.getStringValue().equals(includeInCalculationDeliveries)) {
            states.add(DeliveryStateStringValues.APPROVED);
        } else if (IncludeInCalculationDeliveries.UNCONFIRMED_DELIVERIES.getStringValue().equals(includeInCalculationDeliveries)) {
            states.add(DeliveryStateStringValues.APPROVED);
            states.add(DeliveryStateStringValues.PREPARED);
            states.add(DeliveryStateStringValues.DURING_CORRECTION);
            states.add(DeliveryStateStringValues.DRAFT);
        } else {
            states.add(DeliveryStateStringValues.APPROVED);
            states.add(DeliveryStateStringValues.PREPARED);
            states.add(DeliveryStateStringValues.DURING_CORRECTION);
        }

        List<Entity> orderedDeliveries = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY).find(query.toString())
                .setLong("productId", productId).setDate("deliveryDate", coverageToDate).setParameterList("states", states)
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
