package com.qcadoo.mes.masterOrders.controllers;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProductsBySizeController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private TranslationService translationService;

    @ResponseBody
    @RequestMapping(value = "/masterOrders/productsBySize", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductsBySizeResponse productsBySize(@RequestBody ProductsBySizeRequest productsBySizeRequest) {
        return createMOPositions(productsBySizeRequest);
    }

    private ProductsBySizeResponse createMOPositions(ProductsBySizeRequest productsBySizeRequest) {
        boolean valid = true;
        Entity helper = dataDefinitionService.get("masterOrders", "productsBySizeHelper")
                .get(productsBySizeRequest.getEntityId());
        List<Entity> helperEntries = helper.getHasManyField("productsBySizeEntryHelpers");
        Entity mo = helper.getBelongsToField("masterOrder");

        for (ProductBySizePosition position : productsBySizeRequest.getPositions()) {
            Entity product = helperEntries.stream().filter(e -> e.getId().equals(position.getId())).findAny().get()
                    .getBelongsToField("product");

            Entity moProduct = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                    MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).create();

            moProduct.setField(MasterOrderProductFields.MASTER_ORDER, mo);
            moProduct.setField(MasterOrderProductFields.PRODUCT, product);
            moProduct.setField(MasterOrderProductFields.TECHNOLOGY,
                    technologyServiceO.getDefaultTechnology(moProduct.getBelongsToField(MasterOrderProductFields.PRODUCT)));

            moProduct.setField(MasterOrderProductFields.MASTER_ORDER_QUANTITY, position.getValue());
            moProduct = moProduct.getDataDefinition().save(moProduct);
            if (!moProduct.isValid()) {
                valid = false;
            }
        }

        ProductsBySizeResponse productsBySizeResponse = new ProductsBySizeResponse();
        if (!valid) {
            productsBySizeResponse.setStatus(ProductsBySizeResponse.SimpleResponseStatus.ERROR);
            productsBySizeResponse.setMessage(translationService.translate("masterOrders.productsBySize.creationError",
                    LocaleContextHolder.getLocale()));
        }
        return productsBySizeResponse;
    }
}
