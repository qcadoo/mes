package com.qcadoo.mes.masterOrders;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;

public class GenerationOrderResult {

    private static final String PARAMETER_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS = "automaticallyGenerateOrdersForComponents";

    private TranslationService translationService;

    private ParameterService parameterService;

    public GenerationOrderResult(TranslationService translationService, ParameterService parameterService) {
        this.translationService = translationService;
        this.parameterService = parameterService;
    }

    private List<MasterOrderProductErrorContainer> productOrderErrors = Lists.newArrayList();

    private List<String> generatedOrderNumbers = Lists.newArrayList();

    private List<String> realizationFromStock = Lists.newArrayList();

    private List<String> ordersWithoutPps = Lists.newArrayList();

    private List<SubOrderErrorHolder> ordersWithoutGeneratedSubOrders = Lists.newArrayList();

    private List<String> ordersWithGeneratedSubOrders = Lists.newArrayList();

    private List<String> ordersWithNoGeneratedSubOrders = Lists.newArrayList();

    private List<String> productsWithoutAcceptedTechnologies = Lists.newArrayList();

    private List<String> productOrderSimpleErrors = Lists.newArrayList();

    public void addProductOrderSimpleError(final String product) {
        productOrderSimpleErrors.add(product);
    }

    public void addNotGeneratedProductError(final MasterOrderProductErrorContainer err) {
        productOrderErrors.add(err);
    }

    public void addGeneratedOrderNumber(final String number) {
        generatedOrderNumbers.add(number);
    }

    public void addRealizationFromStock(final String number) {
        realizationFromStock.add(number);
    }

    public void addOrderWithoutPps(final String number) {
        ordersWithoutPps.add(number);
    }

    public void addOrderWithoutGeneratedSubOrders(final SubOrderErrorHolder error) {
        ordersWithoutGeneratedSubOrders.add(error);
    }

    public void addOrderWithGeneratedSubOrders(final String number) {
        ordersWithGeneratedSubOrders.add(number);
    }

    public void addOrderWithNoGeneratedSubOrders(final String number) {
        ordersWithNoGeneratedSubOrders.add(number);
    }

    public void addProductWithoutAcceptedTechnology(final String number) {
        productsWithoutAcceptedTechnologies.add(number);
    }

    public void showMessage(final ViewDefinitionState view) {

        if (!realizationFromStock.isEmpty()) {
            view.addMessage("masterOrders.masterOrder.generationOrder.realizationFromStockNumbers",
                    ComponentState.MessageType.INFO, false, String.join(", ", realizationFromStock));
        }

        if (!ordersWithoutPps.isEmpty()) {
            view.addMessage("masterOrders.masterOrder.generationOrder.ordersWithoutPps", ComponentState.MessageType.INFO, false,
                    String.join(", ", ordersWithoutPps));
        }

        if (!ordersWithoutGeneratedSubOrders.isEmpty()) {
            ordersWithoutGeneratedSubOrders.forEach(error -> {
                view.addMessage("masterOrders.masterOrder.generationOrder.ordersWithoutGeneratedSubOrders",
                        ComponentState.MessageType.INFO, false, error.getNumber(),
                        translationService.translate(error.getError(), LocaleContextHolder.getLocale()));
            });

        }

        if (!ordersWithGeneratedSubOrders.isEmpty()) {
            view.addMessage("masterOrders.masterOrder.generationOrder.ordersWithGeneratedSubOrders",
                    ComponentState.MessageType.INFO, false, String.join(", ", ordersWithGeneratedSubOrders));
        }

        if (!ordersWithNoGeneratedSubOrders.isEmpty()
                && parameterService.getParameter().getBooleanField(PARAMETER_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS)) {
            view.addMessage("masterOrders.masterOrder.generationOrder.ordersWithNoGeneratedSubOrders",
                    ComponentState.MessageType.INFO, false, String.join(", ", ordersWithNoGeneratedSubOrders));
        }

        if (!generatedOrderNumbers.isEmpty()) {
            view.addMessage("masterOrders.masterOrder.generationOrder.generatedOrderNumbers", ComponentState.MessageType.INFO,
                    false, String.join(", ", generatedOrderNumbers));
        }

        if (!productOrderSimpleErrors.isEmpty()) {
            view.addMessage("masterOrders.masterOrder.generationOrder.productOrderSimpleError", ComponentState.MessageType.INFO,
                    false, String.join(", ", productOrderSimpleErrors));
        }

        if (!productOrderErrors.isEmpty()) {
            productOrderErrors.forEach(err -> {
                StringBuilder msg = new StringBuilder();
                msg.append(translationService.translate(
                        "masterOrders.masterOrder.generationOrder.productNumbersForNotGeneratedOrders", view.getLocale(),
                        err.getProduct(), err.getMasterOrder(), err.getQuantity().toPlainString()));
                msg.append("</br>");
                err.getErrorMessages().forEach(
                        errorMessage -> {
                            msg.append(translationService.translate(errorMessage.getMessage(), view.getLocale(),
                                    errorMessage.getVars()));
                        });
                view.addTranslatedMessage(msg.toString(), ComponentState.MessageType.INFO, false);
            });
        }

        if (!productsWithoutAcceptedTechnologies.isEmpty()) {
            view.addMessage("masterOrders.masterOrder.generationOrder.productsWithoutAcceptedTechnologies",
                    ComponentState.MessageType.INFO, false, String.join(", ", productsWithoutAcceptedTechnologies));
        }

    }

    public List<String> extractMessages() {
        List<String> messages = Lists.newArrayList();

        if (!realizationFromStock.isEmpty()) {
            messages.add(translationService.translate("masterOrders.masterOrder.generationOrder.realizationFromStockNumbers",
                    LocaleContextHolder.getLocale(), String.join(", ", realizationFromStock)));
        }

        if (!ordersWithoutPps.isEmpty()) {
            messages.add(translationService.translate("masterOrders.masterOrder.generationOrder.ordersWithoutPps",
                    LocaleContextHolder.getLocale(), String.join(", ", ordersWithoutPps)));
        }

        if (!ordersWithoutGeneratedSubOrders.isEmpty()) {
            ordersWithoutGeneratedSubOrders.forEach(error -> {
                messages.add(translationService.translate(
                        "masterOrders.masterOrder.generationOrder.ordersWithoutGeneratedSubOrders",
                        LocaleContextHolder.getLocale(), error.getNumber(),
                        translationService.translate(error.getError(), LocaleContextHolder.getLocale())));
            });
        }

        if (!ordersWithGeneratedSubOrders.isEmpty()) {
            messages.add(translationService.translate("masterOrders.masterOrder.generationOrder.ordersWithGeneratedSubOrders",
                    LocaleContextHolder.getLocale(), String.join(", ", ordersWithGeneratedSubOrders)));
        }

        if (!ordersWithNoGeneratedSubOrders.isEmpty()
                && parameterService.getParameter().getBooleanField(PARAMETER_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS)) {
            messages.add(translationService.translate("masterOrders.masterOrder.generationOrder.ordersWithNoGeneratedSubOrders",
                    LocaleContextHolder.getLocale(), String.join(", ", ordersWithNoGeneratedSubOrders)));
        }

        if (!generatedOrderNumbers.isEmpty()) {
            messages.add(translationService.translate("masterOrders.masterOrder.generationOrder.generatedOrderNumbers",
                    LocaleContextHolder.getLocale(), String.join(", ", generatedOrderNumbers)));
        }

        if (!productOrderSimpleErrors.isEmpty()) {
            messages.add(translationService.translate("masterOrders.masterOrder.generationOrder.productOrderSimpleError",
                    LocaleContextHolder.getLocale(), String.join(", ", productOrderSimpleErrors)));
        }

        if (!productOrderErrors.isEmpty()) {
            productOrderErrors.forEach(err -> {
                StringBuilder msg = new StringBuilder();
                msg.append(translationService.translate(
                        "masterOrders.masterOrder.generationOrder.productNumbersForNotGeneratedOrders", LocaleContextHolder.getLocale(),
                        err.getProduct(), err.getMasterOrder(), err.getQuantity().toPlainString()));
                msg.append("</br>");
                err.getErrorMessages().forEach(
                        errorMessage -> {
                            msg.append(translationService.translate(errorMessage.getMessage(), LocaleContextHolder.getLocale(),
                                    errorMessage.getVars()));
                        });
                messages.add(msg.toString());
            });
        }

        if (!productsWithoutAcceptedTechnologies.isEmpty()) {
            messages.add(translationService.translate("masterOrders.masterOrder.generationOrder.productsWithoutAcceptedTechnologies",
                    LocaleContextHolder.getLocale(), String.join(", ", productsWithoutAcceptedTechnologies)));
        }

        return messages;
    }

    public List<String> getRealizationFromStock() {
        return realizationFromStock;
    }

    public void setRealizationFromStock(final List<String> realizationFromStock) {
        this.realizationFromStock = realizationFromStock;
    }

    public List<MasterOrderProductErrorContainer> getProductOrderErrors() {
        return productOrderErrors;
    }

    public void setProductOrderErrors(final List<MasterOrderProductErrorContainer> productOrderErrors) {
        this.productOrderErrors = productOrderErrors;
    }

}
