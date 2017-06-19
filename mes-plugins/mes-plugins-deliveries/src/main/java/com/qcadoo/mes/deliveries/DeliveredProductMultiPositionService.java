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

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.ADDITIONAL_CODE;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;

@Service
public class DeliveredProductMultiPositionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public BigDecimal findOrderedQuantity(Entity delivery, Entity product, Entity additionalCode) {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(DELIVERY, delivery)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .add(SearchRestrictions.belongsTo(ADDITIONAL_CODE, additionalCode));
        Entity orderedProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();
        if (orderedProductFromDB == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimalUtils.convertNullToZero(orderedProductFromDB.getDecimalField(OrderedProductFields.ORDERED_QUANTITY));
    }

    public BigDecimal countAlreadyAssignedQuantityForProduct(final Entity product, final Entity additionalCode, final Entity offer,
            List<Entity> deliveredProducts) {
        BigDecimal alreadyAssignedQuantity = deliveredProducts
                .stream()
                .filter(p -> product.getId().equals(p.getBelongsToField(DeliveredProductFields.PRODUCT).getId())
                        && additionalCodesTheSame(additionalCode, p) && offerTheSame(offer, p)).map(p -> {
                    BigDecimal deliveredQuantity = p.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
                    return deliveredQuantity == null ? BigDecimal.ZERO : deliveredQuantity;
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
        return alreadyAssignedQuantity;
    }

    private boolean offerTheSame(final Entity offer, final Entity deliveredProduct) {
        if (!PluginUtils.isEnabled("supplyNegotiations")) {
            return true;
        }
        Entity deliveredProductOffer = deliveredProduct.getBelongsToField("offer");
        return isOfferTheSame(offer, deliveredProductOffer);
    }

    private boolean isOfferTheSame(final Entity offer, final Entity deliveredProductOffer) {
        return isBothCodeNull(offer, deliveredProductOffer)
                || isBothCodeEqual(offer, deliveredProductOffer);
    }

    public BigDecimal countAlreadyAssignedQuantity(final Entity orderedProduct, final Entity additionalCode, final Entity offer, List<Entity> deliveredProducts) {
        Entity product = orderedProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        return countAlreadyAssignedQuantityForProduct(product, additionalCode, offer, deliveredProducts);
    }

    private boolean additionalCodesTheSame(Entity additionalCode, Entity deliveredProduct) {
        Entity deliveredProductAdditionalCode = deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE);
        return isAdditionalCodeTheSame(additionalCode, deliveredProductAdditionalCode);
    }

    private boolean isAdditionalCodeTheSame(Entity additionalCode, Entity deliveredProductAdditionalCode) {
        return isBothCodeNull(additionalCode, deliveredProductAdditionalCode)
                || isBothCodeEqual(additionalCode, deliveredProductAdditionalCode);
    }


    private boolean isBothCodeEqual(Entity additionalCode, Entity deliveredProductAdditionalCode) {
        return additionalCode != null &&  deliveredProductAdditionalCode != null && additionalCode.getId().equals(deliveredProductAdditionalCode.getId());
    }

    private boolean isBothCodeNull(Entity additionalCode, Entity deliveredProductAdditionalCode) {
        return additionalCode == null && deliveredProductAdditionalCode == null;
    }

    private boolean isBothOfferEqual(Entity offer, Entity deliveredProductOffer) {
        return offer != null &&  deliveredProductOffer != null && offer.getId().equals(deliveredProductOffer.getId());
    }

    private boolean isOfferNull(Entity offer, Entity deliveredProductOffer) {
        return offer == null && deliveredProductOffer == null;
    }
}
