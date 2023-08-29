package com.qcadoo.mes.deliveries;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;

@Service
public class DeliveredProductMultiPositionService {

    private static final String L_OFFER = "offer";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public BigDecimal findOrderedQuantity(final Entity delivery, final Entity product) {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(DELIVERY, delivery)).add(SearchRestrictions.belongsTo(PRODUCT, product));

        Entity orderedProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (Objects.isNull(orderedProductFromDB)) {
            return BigDecimal.ZERO;
        }

        return BigDecimalUtils.convertNullToZero(orderedProductFromDB.getDecimalField(OrderedProductFields.ORDERED_QUANTITY));
    }

    public BigDecimal countAlreadyAssignedQuantityForProduct(final Entity product, final Entity offer, final List<Entity> deliveredProducts) {
        BigDecimal alreadyAssignedQuantity = deliveredProducts.stream()
                .filter(deliveredProduct -> product.getId()
                        .equals(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getId())
                        && offerTheSame(offer, deliveredProduct))
                .map(deliveredProduct -> {
                    BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);

                    return BigDecimalUtils.convertNullToZero(deliveredQuantity);
                }).reduce(BigDecimal.ZERO, BigDecimal::add);

        return alreadyAssignedQuantity;
    }

    private boolean offerTheSame(final Entity offer, final Entity deliveredProduct) {
        if (!PluginUtils.isEnabled("supplyNegotiations")) {
            return true;
        }

        Entity deliveredProductOffer = deliveredProduct.getBelongsToField(L_OFFER);

        return isOfferTheSame(offer, deliveredProductOffer);
    }

    private boolean isOfferTheSame(final Entity offer, final Entity deliveredProductOffer) {
        return isBothEntitiesNull(offer, deliveredProductOffer) || isBothEntitiesEqual(offer, deliveredProductOffer);
    }

    public BigDecimal countAlreadyAssignedQuantity(final Entity orderedProduct, final Entity offer,
            final List<Entity> deliveredProducts) {
        Entity product = orderedProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        return countAlreadyAssignedQuantityForProduct(product, offer, deliveredProducts);
    }

    private boolean isBothEntitiesEqual(final Entity entity, final Entity otherEntity) {
        return Objects.nonNull(entity) && Objects.nonNull(otherEntity)
                && entity.getId().equals(otherEntity.getId());
    }

    private boolean isBothEntitiesNull(final Entity entity, final Entity otherEntity) {
        return Objects.isNull(entity) && Objects.isNull(otherEntity);
    }

}
