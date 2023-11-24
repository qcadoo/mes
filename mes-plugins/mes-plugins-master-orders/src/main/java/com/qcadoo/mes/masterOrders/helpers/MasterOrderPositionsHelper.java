package com.qcadoo.mes.masterOrders.helpers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class MasterOrderPositionsHelper {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void updateWarehouseStates(final List<Entity> masterOrderProducts, Entity parameter) {
        deleteWarehouseStatesFromTable();
        MultiMap locationsProductsMap = new MultiHashMap();

        for (Entity masterOrderProduct : masterOrderProducts) {
            Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
            Entity technology = masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY);
            if (technology == null) {
                technology = technologyServiceO.getDefaultTechnology(product);
            }
            List<Entity> locations = new ArrayList<>();
            if (technology == null) {
                for (Entity coverageLocation : parameter.getHasManyField("coverageLocations")) {
                    locations.add(coverageLocation.getBelongsToField("location"));
                }
            } else {
                Entity root = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();
                Entity mainProduct = technologyService.getMainOutputProductComponent(root);
                locations.add(mainProduct.getBelongsToField("productsInputLocation"));
            }
            for (Entity location : locations) {
                locationsProductsMap.put(location, product);
            }
        }
        for (Object key : locationsProductsMap.keySet()) {
            Entity location = (Entity) key;
            Collection value = (Collection) locationsProductsMap.get(key);
            Set<Entity> products = Sets.newHashSet();
            for (Object obj : value) {
                Entity product = (Entity) obj;
                products.add(product);
            }
            Map<Long, BigDecimal> productAndQuantities = materialFlowResourcesService
                    .getQuantitiesForProductsAndLocation(new ArrayList<>(products), location);
            storeWarehouseStates(location, productAndQuantities);
        }
    }

    private void deleteWarehouseStatesFromTable() {
        jdbcTemplate.update("DELETE FROM masterorders_position_warehousestatehelper;", Maps.newHashMap());
    }

    private boolean storeWarehouseStates(final Entity location, final Map<Long, BigDecimal> productAndQuantities) {
        for (Map.Entry<Long, BigDecimal> entry : productAndQuantities.entrySet()) {
            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("location_id", location.getId());
            parameters.put("product_id", entry.getKey());
            parameters.put("quantity", entry.getValue());

            SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);

            jdbcTemplate.update("INSERT INTO masterorders_position_warehousestatehelper(location_id, product_id, quantity) VALUES (:location_id, :product_id, :quantity); ", namedParameters);
        }
        return true;
    }

    public void updateDeliveriesProductQuantities(ViewDefinitionState view, final List<Entity> masterOrderProducts, Entity parameter) {
        deleteDeliveriesProductQuantitiesFromTable();
        Set<Long> positionsProductIds = masterOrderProducts.stream()
                .map(e -> e.getBelongsToField(MasterOrderProductFields.PRODUCT).getId()).collect(Collectors.toSet());
        Map<Long, BigDecimal> productAndQuantities = Maps.newHashMap();
        boolean includeDraftDeliveries = parameter.getBooleanField("includeDraftDeliveries");
        List<Entity> coverageLocations = parameter.getHasManyField("coverageLocations");
        estimateProductDeliveries(view, positionsProductIds, productAndQuantities, coverageLocations, includeDraftDeliveries);
        storeDeliveriesProductQuantities(productAndQuantities);
    }

    private void estimateProductDeliveries(ViewDefinitionState view, Set<Long> positionsProductIds, Map<Long, BigDecimal> productAndQuantities,
                                           List<Entity> coverageLocations, Boolean includeDraftDeliveries) {
        List<Entity> includedDeliveries = getDeliveriesFromDB(coverageLocations, includeDraftDeliveries);
        for (Entity delivery : includedDeliveries) {
            List<Entity> deliveryProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

            for (Entity deliveryProduct : deliveryProducts) {
                if (positionsProductIds.contains(deliveryProduct.getId())) {
                    BigDecimal quantity = numberService.setScaleWithDefaultMathContext(getDeliveryQuantity(delivery, deliveryProduct));
                    if (productAndQuantities.containsKey(deliveryProduct.getId())) {
                        productAndQuantities.put(deliveryProduct.getId(), productAndQuantities.get(deliveryProduct.getId()).add(quantity, numberService.getMathContext()));
                    } else {
                        productAndQuantities.put(deliveryProduct.getId(), quantity);
                    }
                }
            }
        }
    }

    private void deleteDeliveriesProductQuantitiesFromTable() {
        jdbcTemplate.update("DELETE FROM masterorders_position_deliveryproductquantityhelper;", Maps.newHashMap());
    }

    private BigDecimal getDeliveryQuantity(Entity delivery, Entity deliveryProduct) {
        return deliveryProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
    }

    private boolean storeDeliveriesProductQuantities(final Map<Long, BigDecimal> productAndQuantities) {
        for (Map.Entry<Long, BigDecimal> entry : productAndQuantities.entrySet()) {
            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("product_id", entry.getKey());
            parameters.put("quantity", entry.getValue());

            SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);

            jdbcTemplate.update("INSERT INTO masterorders_position_deliveryproductquantityhelper(product_id, quantity) VALUES (:product_id, :quantity); ", namedParameters);
        }

        return true;
    }

    private List<Entity> getDeliveriesFromDB(List<Entity> coverageLocations, boolean includeDraftDeliveries) {
        if (includeDraftDeliveries) {
            SearchCriteriaBuilder scb = getDeliveryDD()
                    .find()
                    .add(SearchRestrictions.or(SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.DRAFT),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.PREPARED),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.DURING_CORRECTION),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.APPROVED)))
                    .add(SearchRestrictions.eq(DeliveryFields.ACTIVE, true));
            if (!coverageLocations.isEmpty()) {
                scb = scb.createAlias(DeliveryFields.LOCATION, DeliveryFields.LOCATION, JoinType.LEFT).add(
                        SearchRestrictions.in(
                                DeliveryFields.LOCATION + L_DOT + L_ID,
                                coverageLocations.stream()
                                        .map(cl -> cl.getBelongsToField("location").getId())
                                        .collect(Collectors.toList())));
            }
            return scb.list().getEntities();
        } else {
            SearchCriteriaBuilder scb = getDeliveryDD()
                    .find()
                    .add(SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.APPROVED))
                    .add(SearchRestrictions.eq(DeliveryFields.ACTIVE, true));
            if (!coverageLocations.isEmpty()) {
                scb = scb.createAlias(DeliveryFields.LOCATION, DeliveryFields.LOCATION, JoinType.LEFT).add(
                        SearchRestrictions.in(
                                DeliveryFields.LOCATION + L_DOT + L_ID,
                                coverageLocations.stream()
                                        .map(cl -> cl.getBelongsToField("location").getId())
                                        .collect(Collectors.toList())));
            }
            return scb.list().getEntities();

        }
    }

    private DataDefinition getDeliveryDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY);
    }

}
