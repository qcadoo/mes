package com.qcadoo.mes.products;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.EntityTree;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.products.util.NumberGeneratorService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public class QualityControlService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void autoGenerateQualityControl(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        boolean inProgressState = Boolean.parseBoolean(args[0]);
        if (inProgressState && isQualityControlAutoGenEnabled()) {
            generateQualityControl(viewDefinitionState, state, args);
        }
    }

    private boolean isQualityControlAutoGenEnabled() {
        SearchResult searchResult = dataDefinitionService.get("basic", "parameter").find().withMaxResults(1).list();

        Entity parameter = null;
        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }

        if (parameter != null) {
            return (Boolean) parameter.getField("checkDoneOrderForQuality");
        } else {
            return false;
        }
    }

    public void generateOnSaveQualityControl(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField("order");
        Entity technology = order.getBelongsToField("technology");
        boolean qualityControlType = technology.getField("qualityControlType").toString().equals("01forBatch");

        if (isQualityControlAutoGenEnabled() || qualityControlType) {
            createAndSaveControlForSingleBatch(order, entity);
        }
    }

    public void generateQualityControl(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() != null) {
            DataDefinition orderDataDefinition = dataDefinitionService.get("products", "order");
            Entity order = orderDataDefinition.get((Long) state.getFieldValue());

            Entity technology = (Entity) order.getField("technology");

            String qualityControlType = technology.getField("qualityControlType").toString();

            generateQualityControlForGivenType(qualityControlType, technology, order);

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

    private void generateQualityControlForGivenType(final String qualityControlType, final Entity technology, final Entity order) {
        if (qualityControlType.equals("01forBatch")) {
            List<Entity> genealogies = getGenealogiesForOrder(order.getId());

            for (Entity genealogy : genealogies) {
                createAndSaveControlForSingleBatch(order, genealogy);
            }

        } else if (qualityControlType.equals("02forUnit")) {
            BigDecimal sampling = (BigDecimal) technology.getField("unitSamplingNr");

            BigDecimal doneQuantity = (BigDecimal) order.getField("doneQuantity");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            BigDecimal numberOfControls = doneQuantity != null ? doneQuantity.divide(sampling) : plannedQuantity.divide(sampling);

            for (int i = 0; i <= numberOfControls.intValue(); i++) {
                DataDefinition qualityForUnitDataDefinition = dataDefinitionService.get("products", "qualityForUnit");

                Entity forUnit = new DefaultEntity("products", "qualityForUnit");
                forUnit.setField("order", order);
                forUnit.setField("number", numberGeneratorService.generateNumber("qualityForUnit"));

                if (i < numberOfControls.intValue()) {
                    forUnit.setField("controlledQuantity", sampling);
                } else {
                    BigDecimal numberOfRemainders = doneQuantity != null ? doneQuantity.divideAndRemainder(sampling)[1]
                            : plannedQuantity.divideAndRemainder(sampling)[1];
                    forUnit.setField("controlledQuantity", numberOfRemainders);
                }
                forUnit.setField("staff", securityService.getCurrentUserName());
                forUnit.setField("date", new Date());

                qualityForUnitDataDefinition.save(forUnit);
            }
        } else if (qualityControlType.equals("03forOrder")) {
            createAndSaveControlForSingleOrder(order);
        } else if (qualityControlType.equals("04forOperation")) {
            EntityTree tree = technology.getTreeField("operationComponents");
            for (Entity entity : tree) {
                if (entity.getField("qualityControlRequired") != null && (Boolean) entity.getField("qualityControlRequired")) {
                    createAndSaveControlForOperation(order, entity);
                }

            }
        }

    }

    private void createAndSaveControlForOperation(final Entity order, final Entity entity) {
        DataDefinition qualityForOperationDataDefinition = dataDefinitionService.get("products", "qualityForOperation");

        Entity forOperation = new DefaultEntity("products", "qualityForOperation");
        forOperation.setField("order", order);
        forOperation.setField("number", numberGeneratorService.generateNumber("qualityForOperation"));
        forOperation.setField("operation", entity.getBelongsToField("operation"));

        qualityForOperationDataDefinition.save(forOperation);
    }

    private void createAndSaveControlForSingleOrder(final Entity order) {
        DataDefinition qualityForOrderDataDefinition = dataDefinitionService.get("products", "qualityForOrder");

        Entity forOrder = new DefaultEntity("products", "qualityForOrder");
        forOrder.setField("order", order);
        forOrder.setField("number", numberGeneratorService.generateNumber("qualityForOrder"));
        forOrder.setField("staff", securityService.getCurrentUserName());
        forOrder.setField("date", new Date());

        qualityForOrderDataDefinition.save(forOrder);
    }

    private void createAndSaveControlForSingleBatch(final Entity order, final Entity genealogy) {
        DataDefinition qualityForBatchDataDefinition = dataDefinitionService.get("products", "qualityForBatch");

        Entity forBatch = new DefaultEntity("products", "qualityForBatch");
        forBatch.setField("order", order);
        forBatch.setField("number", numberGeneratorService.generateNumber("qualityForBatch"));
        forBatch.setField("batchNr", genealogy.getField("batch"));

        BigDecimal doneQuantity = (BigDecimal) order.getField("doneQuantity");
        BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");

        if (doneQuantity != null) {
            forBatch.setField("controlledQuantity", doneQuantity);
        } else if (plannedQuantity != null) {
            forBatch.setField("controlledQuantity", plannedQuantity);
        }

        forBatch.setField("staff", securityService.getCurrentUserName());
        forBatch.setField("date", new Date());

        qualityForBatchDataDefinition.save(forBatch);
    }

    private List<Entity> getGenealogiesForOrder(final Long id) {
        DataDefinition genealogyDD = dataDefinitionService.get("genealogies", "genealogy");

        SearchCriteriaBuilder searchCriteria = genealogyDD.find().restrictedWith(Restrictions.eq("order.id", id));

        return searchCriteria.list().getEntities();
    }

}
