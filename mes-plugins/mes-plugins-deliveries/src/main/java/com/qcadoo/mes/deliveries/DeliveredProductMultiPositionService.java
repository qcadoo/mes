package com.qcadoo.mes.deliveries;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.ADDITIONAL_CODE;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;

@Service
public class DeliveredProductMultiPositionService {

    private static final String L_OFFER = "offer";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public BigDecimal findOrderedQuantity(final Entity delivery, final Entity product, final Entity additionalCode) {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(DELIVERY, delivery)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .add(SearchRestrictions.belongsTo(ADDITIONAL_CODE, additionalCode));

        Entity orderedProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (Objects.isNull(orderedProductFromDB)) {
            return BigDecimal.ZERO;
        }

        return BigDecimalUtils.convertNullToZero(orderedProductFromDB.getDecimalField(OrderedProductFields.ORDERED_QUANTITY));
    }

    public BigDecimal countAlreadyAssignedQuantityForProduct(final Entity product, final Entity additionalCode,
            final Entity offer, final List<Entity> deliveredProducts) {
        BigDecimal alreadyAssignedQuantity = deliveredProducts.stream()
                .filter(deliveredProduct -> product.getId()
                        .equals(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getId())
                        && additionalCodesTheSame(additionalCode, deliveredProduct) && offerTheSame(offer, deliveredProduct))
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
        return isBothCodeNull(offer, deliveredProductOffer) || isBothCodeEqual(offer, deliveredProductOffer);
    }

    public BigDecimal countAlreadyAssignedQuantity(final Entity orderedProduct, final Entity additionalCode, final Entity offer,
            final List<Entity> deliveredProducts) {
        Entity product = orderedProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        return countAlreadyAssignedQuantityForProduct(product, additionalCode, offer, deliveredProducts);
    }

    private boolean additionalCodesTheSame(final Entity additionalCode, final Entity deliveredProduct) {
        Entity deliveredProductAdditionalCode = deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE);
        return isAdditionalCodeTheSame(additionalCode, deliveredProductAdditionalCode);
    }

    private boolean isAdditionalCodeTheSame(final Entity additionalCode, final Entity deliveredProductAdditionalCode) {
        return isBothCodeNull(additionalCode, deliveredProductAdditionalCode)
                || isBothCodeEqual(additionalCode, deliveredProductAdditionalCode);
    }

    private boolean isBothCodeEqual(final Entity additionalCode, final Entity deliveredProductAdditionalCode) {
        return Objects.nonNull(additionalCode) && Objects.nonNull(deliveredProductAdditionalCode)
                && additionalCode.getId().equals(deliveredProductAdditionalCode.getId());
    }

    private boolean isBothCodeNull(final Entity additionalCode, final Entity deliveredProductAdditionalCode) {
        return Objects.isNull(additionalCode) && Objects.isNull(deliveredProductAdditionalCode);
    }

}
