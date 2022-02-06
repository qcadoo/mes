package com.qcadoo.mes.masterOrders.controllers.productBySize;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SizeFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.ProductsBySizeHelperFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;

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
        Entity helper = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_HELPER)
                .get(productsBySizeRequest.getEntityId());
        List<Entity> helperEntries = helper.getHasManyField(ProductsBySizeHelperFields.PRODUCTS_BY_SIZE_ENTRY_HELPERS);
        Entity mo = helper.getBelongsToField(ProductsBySizeHelperFields.MASTER_ORDER);
        if (mo != null) {
            return createMOPositions(mo, helperEntries, productsBySizeRequest.getPositions());
        } else {
            return createSPPositions(helper.getBelongsToField(ProductsBySizeHelperFields.SALES_PLAN), helperEntries,
                    productsBySizeRequest.getPositions());
        }
    }

    public ProductsBySizeResponse createSPPositions(Entity salesPlan, List<Entity> helperEntries,
            List<ProductBySizePosition> positions) {
        ProductsBySizeResponse productsBySizeResponse = new ProductsBySizeResponse();
        try {
            tryCreateSPPositions(salesPlan, helperEntries, positions);
        } catch (EntityRuntimeException exc) {
            productsBySizeResponse.setStatus(ProductsBySizeResponse.SimpleResponseStatus.ERROR);
            productsBySizeResponse
                    .setMessage(translationService.translate("masterOrders.salesPlan.productsBySize.creationErrorForSize",
                            LocaleContextHolder.getLocale(), exc.getEntity().getBelongsToField(SalesPlanProductFields.PRODUCT)
                                    .getBelongsToField(ProductFields.SIZE).getStringField(SizeFields.NUMBER)));
        }

        return productsBySizeResponse;
    }

    @Transactional
    public void tryCreateSPPositions(Entity salesPlan, List<Entity> helperEntries, List<ProductBySizePosition> positions) {
        for (ProductBySizePosition position : positions) {
            Entity product = helperEntries.stream().filter(e -> e.getId().equals(position.getId())).findAny().get()
                    .getBelongsToField("product");

            Entity spProduct = dataDefinitionService
                    .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT).create();

            spProduct.setField(SalesPlanProductFields.SALES_PLAN, salesPlan);
            spProduct.setField(SalesPlanProductFields.PRODUCT, product);
            spProduct.setField(SalesPlanProductFields.TECHNOLOGY,
                    technologyServiceO.getDefaultTechnology(spProduct.getBelongsToField(SalesPlanProductFields.PRODUCT)));

            spProduct.setField(SalesPlanProductFields.PLANNED_QUANTITY, position.getValue());
            spProduct.setField(SalesPlanProductFields.ORDERED_QUANTITY, 0);
            spProduct.setField(SalesPlanProductFields.ORDERED_TO_WAREHOUSE, 0);
            spProduct.setField(SalesPlanProductFields.SURPLUS_FROM_PLAN, position.getValue());
            spProduct = spProduct.getDataDefinition().save(spProduct);
            if (!spProduct.isValid()) {
                throw new EntityRuntimeException(spProduct);
            }
        }
    }

    public ProductsBySizeResponse createMOPositions(Entity mo, List<Entity> helperEntries,
            List<ProductBySizePosition> positions) {
        ProductsBySizeResponse productsBySizeResponse = new ProductsBySizeResponse();

        try {
            tryCreateMOPositions(mo, helperEntries, positions);
        } catch (EntityRuntimeException exc) {
            productsBySizeResponse.setStatus(ProductsBySizeResponse.SimpleResponseStatus.ERROR);
            productsBySizeResponse.setMessage(translationService.translate("masterOrders.productsBySize.creationErrorForSize",
                    LocaleContextHolder.getLocale(), exc.getEntity().getBelongsToField(MasterOrderProductFields.PRODUCT)
                            .getBelongsToField(ProductFields.SIZE).getStringField(SizeFields.NUMBER)));
        }

        return productsBySizeResponse;
    }

    @Transactional
    public void tryCreateMOPositions(Entity mo, List<Entity> helperEntries, List<ProductBySizePosition> positions) {
        for (ProductBySizePosition position : positions) {
            Entity product = helperEntries.stream().filter(e -> e.getId().equals(position.getId())).findAny().get()
                    .getBelongsToField("product");

            Entity moProduct = dataDefinitionService
                    .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).create();

            moProduct.setField(MasterOrderProductFields.MASTER_ORDER, mo);
            moProduct.setField(MasterOrderProductFields.PRODUCT, product);
            moProduct.setField(MasterOrderProductFields.TECHNOLOGY,
                    technologyServiceO.getDefaultTechnology(moProduct.getBelongsToField(MasterOrderProductFields.PRODUCT)));

            moProduct.setField(MasterOrderProductFields.MASTER_ORDER_QUANTITY, position.getValue());
            moProduct = moProduct.getDataDefinition().save(moProduct);
            if (!moProduct.isValid()) {
                throw new EntityRuntimeException(moProduct);
            }
        }
    }
}
