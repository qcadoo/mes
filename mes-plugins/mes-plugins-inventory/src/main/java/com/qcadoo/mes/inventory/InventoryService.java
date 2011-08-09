package com.qcadoo.mes.inventory;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class InventoryService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    BigDecimal calculateShouldBe(String warehouse, String product, String forDate) {

        BigDecimal countProductIn = BigDecimal.ZERO;
        BigDecimal countProductOut = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal countProduct = BigDecimal.ZERO;
        Date lastCorrectionDate = null;

        DataDefinition transferDataCorrection = dataDefinitionService.get("inventory", "correction");
        DataDefinition transferTo = dataDefinitionService.get("inventory", "transfer");
        DataDefinition transferFrom = dataDefinitionService.get("inventory", "transfer");

        SearchResult resultDataCorrection = transferDataCorrection.find(
                "where warehouse = " + warehouse + " order by correctionDate asc").list();

        for (Entity e : resultDataCorrection.getEntities()) {
            lastCorrectionDate = (Date) e.getField("correctionDate");
            quantity = (BigDecimal) e.getField("found");
            countProduct = quantity;
        }

        SearchResult resultTo = null;
        SearchResult resultFrom = null;

        if (lastCorrectionDate == null) {
            resultTo = transferTo.find(
                    "where warehouseTo = " + warehouse + " and product = " + product + " and date <= '" + forDate + "'").list();

            resultFrom = transferFrom.find(
                    "where warehouseFrom = " + warehouse + " and product = " + product + " and date <= '" + forDate + "'").list();

        } else {
            resultTo = transferTo.find(
                    "where warehouseTo = " + warehouse + " and product = " + product + " and date <= '" + forDate
                            + "' and date > '" + lastCorrectionDate + "'").list();

            resultFrom = transferFrom.find(
                    "where warehouseFrom = " + warehouse + " and product = " + product + " and date <= '" + forDate
                            + "' and date > '" + lastCorrectionDate + "'").list();
        }

        for (Entity e : resultTo.getEntities()) {
            quantity = (BigDecimal) e.getField("quantity");
            countProductIn = countProductIn.add(quantity);
        }

        for (Entity e : resultFrom.getEntities()) {
            quantity = (BigDecimal) e.getField("quantity");
            countProductOut = countProductOut.add(quantity);
        }

        if (lastCorrectionDate == null) {

            countProductIn = countProductIn.subtract(countProductOut);
        } else {
            countProductIn = countProductIn.add(countProduct);
            countProductIn = countProductIn.subtract(countProductOut);
        }

        if (countProductIn.compareTo(BigDecimal.ZERO) == -1)
            countProductIn = BigDecimal.ZERO;
        return countProductIn;
    }

    public void refreshShouldBe(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {

        ComponentState warehouse = (ComponentState) state.getComponentByReference("warehouse");
        ComponentState product = (ComponentState) state.getComponentByReference("product");
        ComponentState Date = (ComponentState) state.getComponentByReference("correctionDate");
        FieldComponent should = (FieldComponent) state.getComponentByReference("shouldBe");

        if (warehouse.getFieldValue() != null && product.getFieldValue() != null && Date.getFieldValue() != null) {

            String warehouseNumber = warehouse.getFieldValue().toString();
            String productNumber = product.getFieldValue().toString();
            String forDate = Date.getFieldValue().toString();

            BigDecimal shouldBe = calculateShouldBe(warehouseNumber, productNumber, forDate);

            if (shouldBe != null && shouldBe != BigDecimal.ZERO) {
                should.setFieldValue(shouldBe);
            } else {
                should.setFieldValue(BigDecimal.ZERO);
            }
        }

    }

    public void printInventory(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

    }

    public void generateInventory(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

    }

    public void generateMaterialRequirement(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
    }

}