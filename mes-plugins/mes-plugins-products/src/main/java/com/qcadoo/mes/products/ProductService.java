package com.qcadoo.mes.products;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.api.DataDefinitionService;
import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.enums.RestrictionOperator;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.search.Restrictions;
import com.qcadoo.mes.core.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.search.SearchResult;
import com.qcadoo.mes.core.view.ViewValue;
import com.qcadoo.mes.core.view.elements.comboBox.EntityComboBoxValue;

@Service
public final class ProductService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @SuppressWarnings("unchecked")
    public void afterOrderDetailsLoad(final ViewValue<Object> value, final String triggerComponentName) {
        ViewValue<EntityComboBoxValue> productValue = (ViewValue<EntityComboBoxValue>) value
                .lookupValue("mainWindow.orderDetailsForm.product");
        ViewValue<String> defaultInstructionValue = (ViewValue<String>) value
                .lookupValue("mainWindow.orderDetailsForm.defaultInstruction");
        ViewValue<EntityComboBoxValue> instructionValue = (ViewValue<EntityComboBoxValue>) value
                .lookupValue("mainWindow.orderDetailsForm.instruction");

        defaultInstructionValue.setEnabled(false);
        defaultInstructionValue.setValue("");

        if (productValue.getValue() != null && productValue.getValue().getSelectedValue() != null) {
            Entity defaultInstructionEntity = getDefaultInstruction(productValue);
            if (defaultInstructionEntity != null) {
                String defaultInstructionName = defaultInstructionEntity.getField("name").toString();
                defaultInstructionValue.setValue(defaultInstructionName);
                selectDefaultInstruction(triggerComponentName, instructionValue, defaultInstructionEntity);
            }
        }
    }

    private void selectDefaultInstruction(final String triggerComponentName,
            final ViewValue<EntityComboBoxValue> instructionValue, final Entity defaultInstructionEntity) {
        Long selectedInstructinId = instructionValue.getValue().getSelectedValue();
        if (selectedInstructinId == null && "mainWindow.orderDsetailsForm.product".equals(triggerComponentName)) {
            instructionValue.getValue().setSelectedValue(defaultInstructionEntity.getId());
        }
    }

    private Entity getDefaultInstruction(final ViewValue<EntityComboBoxValue> productValue) {
        DataDefinition instructionDD = dataDefinitionService.get("products", "instruction");

        SearchCriteriaBuilder searchCriteria = instructionDD
                .find()
                .withMaxResults(1)
                .restrictedWith(Restrictions.eq(instructionDD.getField("master"), true))
                .restrictedWith(
                        Restrictions.belongsTo(instructionDD.getField("product"), productValue.getValue().getSelectedValue()));

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

    public boolean checkInstructionDefault(final DataDefinition dataDefinition, final Entity entity) {
        Boolean master = (Boolean) entity.getField("master");

        if (!master) {
            return true;
        }

        SearchCriteriaBuilder searchCriteria = dataDefinition.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(dataDefinition.getField("master"), true))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("product"), entity.getField("product")));

        if (entity.getId() != null) {
            searchCriteria.restrictedWith(Restrictions.idRestriction(entity.getId(), RestrictionOperator.NE));
        }

        SearchResult searchResult = searchCriteria.list();

        return searchResult.getTotalNumberOfEntities() == 0;
    }

    public boolean checkSubstituteDates(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("effectiveDateFrom");
        Date dateTo = (Date) entity.getField("effectiveDateTo");

        return compareDates(dateFrom, dateTo);
    }

    public boolean checkOrderDates(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("dateFrom");
        Date dateTo = (Date) entity.getField("dateTo");

        return compareDates(dateFrom, dateTo);
    }

    public boolean checkInstructionDates(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("dateFrom");
        Date dateTo = (Date) entity.getField("dateTo");

        return compareDates(dateFrom, dateTo);
    }

    public void fillOrderDatesAndWorkers(final DataDefinition dataDefinition, final Entity entity) {
        if (("pending".equals(entity.getField("state")) || "done".equals(entity.getField("state")))
                && entity.getField("effectiveDateFrom") == null) {
            entity.setField("effectiveDateFrom", new Date());
            entity.setField("startWorker", "Jan Kowalski"); // TODO masz - fill field with current user
        }
        if ("done".equals(entity.getField("state")) && entity.getField("effectiveDateTo") == null) {
            entity.setField("effectiveDateTo", new Date());
            entity.setField("endWorker", "Jan Nowak"); // TODO masz - fill field with current user

        }

        if (entity.getField("effectiveDateTo") != null) {
            entity.setField("state", "done");
        } else if (entity.getField("effectiveDateFrom") != null) {
            entity.setField("state", "pending");
        }
    }

    private boolean compareDates(final Date dateFrom, final Date dateTo) {
        if (dateFrom == null || dateTo == null) {
            return true;
        }

        return !dateFrom.after(dateTo);
    }

}
