package com.qcadoo.mes.deliveries.controllers;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ProductsWithPriceController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @ResponseBody
    @RequestMapping(value = "/deliveries/productsWithPrice", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductsWithPriceResponse productsWithPrice(@RequestBody ProductsWithPriceRequest productsWithPriceRequest) {

        List<ProductWithPricePosition> positions = productsWithPriceRequest.getPositions();
        ProductsWithPriceResponse productsWithPriceResponse = new ProductsWithPriceResponse();

        try {
            tryUpdatePositions(positions);
        } catch (EntityRuntimeException exc) {
            productsWithPriceResponse.setStatus(ProductsWithPriceResponse.SimpleResponseStatus.ERROR);
            productsWithPriceResponse.setMessage(translationService.translate("deliveries.productsWithPrice.updateErrorForProduct",
                    LocaleContextHolder.getLocale(), exc.getEntity().getBelongsToField(DeliveredProductFields.PRODUCT)
                            .getStringField(ProductFields.NUMBER)));
        }

        return productsWithPriceResponse;
    }


    @Transactional
    public void tryUpdatePositions(List<ProductWithPricePosition> positions) {
        for (ProductWithPricePosition position : positions) {
            Entity deliveredProduct = dataDefinitionService
                    .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT).get(position.getId());

            deliveredProduct.setField(DeliveredProductFields.PRICE_PER_UNIT, position.getValue());
            if (position.getValue() == null) {
                deliveredProduct.setField(DeliveredProductFields.TOTAL_PRICE, position.getValue());
            }
            try {
                deliveredProduct = deliveredProduct.getDataDefinition().save(deliveredProduct);
            } catch (IllegalStateException exception) {
                throw new EntityRuntimeException(deliveredProduct);
            }
            if (!deliveredProduct.isValid()) {
                throw new EntityRuntimeException(deliveredProduct);
            }
        }
    }
}
