package com.qcadoo.mes.materialFlowResources.controllers;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
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
public class ProductsWithPriceControllerMFR {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @ResponseBody
    @RequestMapping(value = "/materialFlowResources/productsWithPrice", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductsWithPriceResponse productsWithPrice(@RequestBody ProductsWithPriceRequest productsWithPriceRequest) {

        List<ProductWithPricePosition> positions = productsWithPriceRequest.getPositions();
        ProductsWithPriceResponse productsWithPriceResponse = new ProductsWithPriceResponse();

        try {
            tryUpdatePositions(positions);
        } catch (EntityRuntimeException exc) {
            productsWithPriceResponse.setStatus(ProductsWithPriceResponse.SimpleResponseStatus.ERROR);
            productsWithPriceResponse.setMessage(translationService.translate("materialFlowResources.productsWithPrice.updateErrorForProduct",
                    LocaleContextHolder.getLocale(), exc.getEntity().getBelongsToField(PositionFields.PRODUCT)
                            .getStringField(ProductFields.NUMBER)));
        }

        return productsWithPriceResponse;
    }


    @Transactional
    public void tryUpdatePositions(List<ProductWithPricePosition> positions) {
        for (ProductWithPricePosition position : positions) {
            Entity positionEntity = dataDefinitionService
                    .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_POSITION).get(position.getId());

            positionEntity.setField(PositionFields.PRICE, position.getValue());
            positionEntity = positionEntity.getDataDefinition().save(positionEntity);
            if (!positionEntity.isValid()) {
                throw new EntityRuntimeException(positionEntity);
            }
        }
    }
}
