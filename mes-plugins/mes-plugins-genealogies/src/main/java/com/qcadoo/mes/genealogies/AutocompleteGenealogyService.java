package com.qcadoo.mes.genealogies;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.genealogies.GenealogiesGenealogyProductInComponent;
import com.qcadoo.mes.beans.products.ProductsOrder;
import com.qcadoo.mes.beans.products.ProductsProduct;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public class AutocompleteGenealogyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    public void fillLastUsedBatchForProduct(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        // TODO masz why we get hibernate entities here?
        ProductsProduct product = ((GenealogiesGenealogyProductInComponent) entity.getField("productInComponent"))
                .getProductInComponent().getProduct();
        DataDefinition productInDef = dataDefinitionService.get("products", "product");
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    public void fillLastUsedBatchForGenealogy(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        // TODO masz why we get hibernate entities here?
        ProductsProduct product = ((ProductsOrder) entity.getField("order")).getProduct();
        DataDefinition productInDef = dataDefinitionService.get("products", "product");
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    @Transactional
    public void autocompleteGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get("products", "order").get((Long) state.getFieldValue());
            if (order == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                boolean success = createGenealogy(order, Boolean.parseBoolean(args[0]));
                if (success) {
                    state.addMessage(
                            translationService.translate("genealogies.message.autoGenealogy.success", state.getLocale()),
                            MessageType.SUCCESS);
                } else {
                    state.addMessage(
                            translationService.translate("genealogies.message.autoGenealogy.failure", state.getLocale()),
                            MessageType.INFO);
                }
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

    public void fillLastUsedShiftFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get("genealogies", "currentAttribute");
        SearchResult searchResult = featureDef.find().withMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField("lastUsedShift", entity.getField("value"));
            featureDef.save(currentAttribute);
        }
    }

    public void fillLastUsedPostFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get("genealogies", "currentAttribute");
        SearchResult searchResult = featureDef.find().withMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField("lastUsedPost", entity.getField("value"));
            featureDef.save(currentAttribute);
        }
    }

    public void fillLastUsedOtherFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get("genealogies", "currentAttribute");
        SearchResult searchResult = featureDef.find().withMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField("lastUsedOther", entity.getField("value"));
            featureDef.save(currentAttribute);
        }
    }

    private void fillUserAndDate(final Entity entity) {
        if (entity.getField("date") == null) {
            entity.setField("date", new Date());
        }
        if (entity.getField("worker") == null) {
            entity.setField("worker", securityService.getCurrentUserName());
        }
    }

    private boolean createGenealogy(final Entity order, final boolean lastUsedMode) {
        Entity mainProduct = (Entity) order.getField("product");
        Entity technology = (Entity) order.getField("technology");
        if (mainProduct == null || technology == null) {
            return false;
        }
        Object mainBatch = null;
        if (lastUsedMode) {
            mainBatch = mainProduct.getField("lastUsedBatch");
        } else {
            mainBatch = mainProduct.getField("batch");
        }
        if (mainBatch == null) {
            return false;
        }
        Entity genealogy = new DefaultEntity("genealogies", "genealogy");
        genealogy.setField("order", order);
        genealogy.setField("batch", mainBatch);
        if (order.getField("plannedQuantity") != null) {
            genealogy.setField("quantity", order.getField("plannedQuantity"));
        } else if (order.getField("doneQuantity") != null) {
            genealogy.setField("quantity", order.getField("doneQuantity"));
        }
        DataDefinition genealogyDef = dataDefinitionService.get("genealogies", "genealogy");
        Entity savedGenealogy = genealogyDef.save(genealogy);
        completeAttributesForGenealogy(technology, savedGenealogy, lastUsedMode);
        completeBatchForComponents(technology, savedGenealogy, lastUsedMode);
        return true;
    }

    private void completeAttributesForGenealogy(final Entity technology, final Entity genealogy, final boolean lastUsedMode) {
        SearchResult searchResult = dataDefinitionService.get("genealogies", "currentAttribute").find().withMaxResults(1).list();
        Entity currentAttribute = null;
        if (searchResult.getEntities().size() > 0) {
            currentAttribute = searchResult.getEntities().get(0);
        }

        if ((Boolean) technology.getField("shiftFeatureRequired")) {
            Entity shift = new DefaultEntity("genealogies", "shiftFeature");
            shift.setField("genealogy", genealogy);
            if (currentAttribute == null) {
                shift.setField("value", null);
            } else if (lastUsedMode) {
                shift.setField("value", currentAttribute.getField("lastUsedShift"));
            } else {
                shift.setField("value", currentAttribute.getField("shift"));
            }
            DataDefinition shiftInDef = dataDefinitionService.get("genealogies", "shiftFeature");
            shiftInDef.save(shift);
        }
        if ((Boolean) technology.getField("otherFeatureRequired")) {
            Entity other = new DefaultEntity("genealogies", "otherFeature");
            other.setField("genealogy", genealogy);
            if (currentAttribute == null) {
                other.setField("value", null);
            } else if (lastUsedMode) {
                other.setField("value", currentAttribute.getField("lastUsedOther"));
            } else {
                other.setField("value", currentAttribute.getField("other"));
            }
            DataDefinition otherInDef = dataDefinitionService.get("genealogies", "otherFeature");
            otherInDef.save(other);
        }
        if ((Boolean) technology.getField("postFeatureRequired")) {
            Entity post = new DefaultEntity("genealogies", "postFeature");
            post.setField("genealogy", genealogy);
            if (currentAttribute == null) {
                post.setField("value", null);
            } else if (lastUsedMode) {
                post.setField("value", currentAttribute.getField("lastUsedPost"));
            } else {
                post.setField("value", currentAttribute.getField("post"));
            }
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
                        DataDefinition batchDef = dataDefinitionService.get("genealogies", "productInBatch");
                        batchDef.save(productBatch);
                    }
                }
            }
        }
    }
}
