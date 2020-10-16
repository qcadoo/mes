package com.qcadoo.mes.materialFlowResources.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.LogService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.model.api.Entity;

@Service
public class DocumentErrorsLogger {

    @Autowired
    private LogService logService;

    @Autowired
    private TranslationService translationService;

    public void saveResourceStockLackErrorsToSystemLogs(final Entity document) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

            @Override
            public void afterCompletion(int status) {
                super.afterCompletion(status);
                if (TransactionSynchronization.STATUS_ROLLED_BACK == status) {
                    document.getGlobalErrors().stream()
                            .filter(errorMessage -> "materialFlow.error.position.quantity.notEnoughResources"
                                    .equals(errorMessage.getMessage()))
                            .forEach(errorMessage -> logService.add(LogService.Builder
                                    .error("document",
                                            translationService.translate("materialFlowResources.document.accept",
                                                    LocaleContextHolder.getLocale()))
                                    .withMessage(translationService.translate("materialFlow.error.position.quantity.notEnough",
                                            LocaleContextHolder.getLocale()))
                                    .withItem1(document.getStringField(DocumentFields.NUMBER))
                                    .withItem2(errorMessage.getVars()[0]).withItem3(errorMessage.getVars()[1])));
                }
            }
        });
    }
}