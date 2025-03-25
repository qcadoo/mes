package com.qcadoo.mes.masterOrders.helpers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.ParameterFieldsMO;
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
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
                Entity masterOrderReleaseLocation = parameter.getBelongsToField(ParameterFieldsMO.MASTER_ORDER_RELEASE_LOCATION);
                if (masterOrderReleaseLocation != null) {
                    locations.add(masterOrderReleaseLocation);
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

    public void updateDeliveriesProductQuantities(ViewDefinitionState view, final List<Entity> masterOrderProducts,
                                                  Entity parameter) {
        deleteDeliveriesProductQuantitiesFromTable();
        Set<Long> positionsProductIds = masterOrderProducts.stream()
                .map(e -> e.getBelongsToField(MasterOrderProductFields.PRODUCT).getId()).collect(Collectors.toSet());
        Map<Long, BigDecimal> productAndQuantities = Maps.newHashMap();
        String includeInCalculationDeliveries = parameter.getStringField(ParameterFieldsD.INCLUDE_IN_CALCULATION_DELIVERIES);
        Entity masterOrderReleaseLocation = parameter.getBelongsToField(ParameterFieldsMO.MASTER_ORDER_RELEASE_LOCATION);
        estimateProductDeliveries(view, positionsProductIds, productAndQuantities, masterOrderReleaseLocation, includeInCalculationDeliveries);
        storeDeliveriesProductQuantities(productAndQuantities);
    }

    private void estimateProductDeliveries(ViewDefinitionState view, Set<Long> positionsProductIds,
                                           Map<Long, BigDecimal> productAndQuantities,
                                           Entity masterOrderReleaseLocation, String includeInCalculationDeliveries) {
        List<Entity> includedDeliveries = getDeliveriesFromDB(masterOrderReleaseLocation, includeInCalculationDeliveries);
        for (Entity delivery : includedDeliveries) {
            List<Entity> deliveryProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

            for (Entity deliveryProduct : deliveryProducts) {
                Long productId = deliveryProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getId();
                if (positionsProductIds.contains(productId)) {
                    BigDecimal quantity = numberService.setScaleWithDefaultMathContext(deliveryProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY));
                    if (productAndQuantities.containsKey(productId)) {
                        productAndQuantities.put(productId, productAndQuantities.get(productId).add(quantity, numberService.getMathContext()));
                    } else {
                        productAndQuantities.put(productId, quantity);
                    }
                }
            }
        }
    }

    private void deleteDeliveriesProductQuantitiesFromTable() {
        jdbcTemplate.update("DELETE FROM masterorders_position_deliveryproductquantityhelper;", Maps.newHashMap());
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

    private List<Entity> getDeliveriesFromDB(Entity masterOrderReleaseLocation, String includeInCalculationDeliveries) {
        List<String> states = IncludeInCalculationDeliveries.getStates(includeInCalculationDeliveries);

        SearchCriteriaBuilder scb = getDeliveryDD()
                .find()
                .add(SearchRestrictions.in(DeliveryFields.STATE, states))
                .add(SearchRestrictions.eq(DeliveryFields.ACTIVE, true));
        if (masterOrderReleaseLocation != null) {
            scb = scb.createAlias(DeliveryFields.LOCATION, DeliveryFields.LOCATION, JoinType.LEFT)
                    .add(SearchRestrictions.eq(DeliveryFields.LOCATION + L_DOT + L_ID, masterOrderReleaseLocation.getId()));
        }
        return scb.list().getEntities();
    }

    private DataDefinition getDeliveryDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY);
    }

}
