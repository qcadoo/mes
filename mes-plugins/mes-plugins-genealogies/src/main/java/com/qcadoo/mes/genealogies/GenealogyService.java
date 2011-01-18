package com.qcadoo.mes.genealogies;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.genealogies.GenealogiesGenealogyProductInComponent;
import com.qcadoo.mes.beans.products.ProductsOrder;
import com.qcadoo.mes.beans.products.ProductsProduct;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public class GenealogyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    public void fillLastUsedBatchForProduct(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsProduct product = ((GenealogiesGenealogyProductInComponent) entity.getField("productInComponent"))
                .getProductInComponent().getProduct();
        DataDefinition productInDef = dataDefinitionService.get("products", "product");
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    public void fillLastUsedBatchForGenealogy(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsProduct product = ((ProductsOrder) entity.getField("order")).getProduct();
        DataDefinition productInDef = dataDefinitionService.get("products", "product");
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    public void autocompleteGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get("products", "order").get((Long) state.getFieldValue());
            if (order == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                createGenealogy(order, Boolean.parseBoolean(args[0]));
            }
        } else {
            if (state instanceof FormComponentState) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    private void createGenealogy(final Entity order, final boolean lastUsedMode) {
        Entity mainProduct = (Entity) order.getField("product");
        Entity technology = (Entity) order.getField("technology");
        if (mainProduct == null || technology == null) {
            return;
        }
        Object mainBatch = null;
        if (lastUsedMode) {
            mainBatch = mainProduct.getField("lastUsedBatch");
        } else {
            mainBatch = mainProduct.getField("batch");
        }
        if (mainBatch == null) {
            return;
        }
        Entity genealogy = new DefaultEntity("genealogies", "genealogy");
        genealogy.setField("order", order);
        genealogy.setField("batch", mainBatch);
        if (order.getField("plannedQuantity") != null) {
            genealogy.setField("quantity", order.getField("plannedQuantity"));
        } else if (order.getField("doneQuantity") != null) {
            genealogy.setField("quantity", order.getField("doneQuantity"));
        }
        genealogy.setField("date", new Date());
        genealogy.setField("worker", securityService.getCurrentUserName());
        DataDefinition genealogyDef = dataDefinitionService.get("genealogies", "genealogy");
        Entity savedGenealogy = genealogyDef.save(genealogy);
        completeAttributesForGenealogy(technology, savedGenealogy);
        completeBatchForComponents(technology, savedGenealogy, lastUsedMode);

    }

    private void completeAttributesForGenealogy(final Entity technology, final Entity genealogy) {
        if ((Boolean) technology.getField("shiftFeatureRequired")) {
            Entity shift = new DefaultEntity("genealogies", "shiftFeature");
            shift.setField("genealogy", genealogy);
            shift.setField("value", "");
            shift.setField("date", new Date());
            shift.setField("worker", securityService.getCurrentUserName());
            DataDefinition shiftInDef = dataDefinitionService.get("genealogies", "shiftFeature");
            shiftInDef.save(shift);
        }
        if ((Boolean) technology.getField("otherFeatureRequired")) {
            Entity other = new DefaultEntity("genealogies", "otherFeature");
            other.setField("genealogy", genealogy);
            other.setField("value", "");
            other.setField("date", new Date());
            other.setField("worker", securityService.getCurrentUserName());
            DataDefinition otherInDef = dataDefinitionService.get("genealogies", "otherFeature");
            otherInDef.save(other);
        }
        if ((Boolean) technology.getField("postFeatureRequired")) {
            Entity post = new DefaultEntity("genealogies", "postFeature");
            post.setField("genealogy", genealogy);
            post.setField("value", "");
            post.setField("date", new Date());
            post.setField("worker", securityService.getCurrentUserName());
            DataDefinition postInDef = dataDefinitionService.get("genealogies", "postFeature");
            postInDef.save(post);
        }
    }

    private void completeBatchForComponents(final Entity technology, final Entity genealogy, final boolean lastUsedMode) {
        for (Entity operationComponent : technology.getHasManyField("operationComponents")) {
            for (Entity operationProductComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                if ((Boolean) operationProductComponent.getField("batchRequired")) {
                    Entity productIn = new DefaultEntity("genealogies", "genealogyProductInComponent");
                    productIn.setField("genealogy", genealogy);
                    productIn.setField("productInComponent", operationProductComponent);
                    DataDefinition productInDef = dataDefinitionService.get("genealogies", "genealogyProductInComponent");
                    Entity savedProductIn = productInDef.save(productIn);
                    Entity product = (Entity) operationProductComponent.getField("product");
                    Object batch = null;
                    if (lastUsedMode) {
                        batch = product.getField("lastUsedBatch");
                    } else {
                        batch = product.getField("batch");
                    }
                    if (batch != null) {
                        Entity productBatch = new DefaultEntity("genealogies", "productInBatch");
                        productBatch.setField("batch", batch);
                        productBatch.setField("productInComponent", savedProductIn);
                        productBatch.setField("date", new Date());
                        productBatch.setField("worker", securityService.getCurrentUserName());
                        DataDefinition batchDef = dataDefinitionService.get("genealogies", "productInBatch");
                        batchDef.save(productBatch);
                    }
                }
            }
        }
    }

}
