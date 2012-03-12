package com.qcadoo.mes.productionCountingWithCosts;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.mes.productionCountingWithCosts.pdf.ProductionBalanceWithCostsPdfService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class GenerateProductionBalanceWithCosts implements Observer {

    @Autowired
    private NumberService numberService;

    @Autowired
    private CostCalculationService costCalculationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProductionBalanceWithCostsPdfService productionBalanceWithCostsPdfService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private Locale locale = LocaleContextHolder.getLocale();

    @Override
    public void update(Observable arg0, Object arg1) {
        Entity balance = (Entity) arg1;

        doTheCostsPart(balance);
        generateBalanceWithCostsReport(balance);
    }

    private void generateBalanceWithCostsReport(Entity balance) {
        String localePrefix = "productionCounting.productionBalanceWithCosts.report.fileName";

        Entity productionBalanceWithFileName = fileService.updateReportFileName(balance, "date", localePrefix);

        String localePrefixToMatch = localePrefix;

        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();

        try {
            productionBalanceWithCostsPdfService.generateDocument(productionBalanceWithFileName, company, locale,
                    localePrefixToMatch);
        } catch (IOException e) {
            throw new RuntimeException("Problem with saving productionBalanceWithCosts report");
        } catch (DocumentException e) {
            throw new RuntimeException("Problem with generating productionBalanceWithCosts report");
        }
    }

    void doTheCostsPart(final Entity balance) {
        Entity order = balance.getBelongsToField("order");
        Entity technology = order.getBelongsToField("technology");

        BigDecimal quantity = (BigDecimal) order.getField("plannedQuantity");
        balance.setField("quantity", quantity);
        balance.setField("technology", technology);

        costCalculationService.calculateTotalCost(balance);

        BigDecimal totalTechnicalProductionCosts = (BigDecimal) balance.getField("totalTechnicalProductionCosts");
        BigDecimal perUnit = totalTechnicalProductionCosts.divide(quantity, numberService.getMathContext());
        balance.setField("totalTechnicalProductionCostPerUnit", numberService.setScale(perUnit));

        balance.getDataDefinition().save(balance);
    }
}
