package com.qcadoo.mes.productionCounting.internal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class ProductionBalanceService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Value("${reportPath}")
    private String path;

    public void fillFieldsWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (orderLookup.getFieldValue() == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            return;
        }
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        productField.setFieldValue(order.getBelongsToField("product").getId());

        FieldComponent recordsNumberField = (FieldComponent) viewDefinitionState.getComponentByReference("recordsNumber");
        Integer recordsNumberValue = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD)
                .find("where order.id=" + order.getId().toString()).list().getEntities().size();
        recordsNumberField.setFieldValue(recordsNumberValue);
    }

    public void fillProductionTimesWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (orderLookup.getFieldValue() == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            return;
        }

        if (order.getField("typeOfProductionRecording") != null
                && order.getStringField("typeOfProductionRecording").equals("03forEach")) {
            viewDefinitionState.getComponentByReference("productionTimeGridLayout").setVisible(false);

        } else {
            viewDefinitionState.getComponentByReference("operationsTimeGrid").setVisible(false);

            Entity productionRecord = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD)
                    .find("where order.id=" + order.getId().toString()).uniqueResult();

            if (productionRecord != null) {
                // FieldComponent machinePlannedTimeField = (FieldComponent) viewDefinitionState
                // .getComponentByReference("machinePlannedTime");
                FieldComponent machineRegisteredTimeField = (FieldComponent) viewDefinitionState
                        .getComponentByReference("machineRegisteredTime");
                machineRegisteredTimeField.setFieldValue(productionRecord.getStringField("machineTime"));
                FieldComponent machineTimeBalanceField = (FieldComponent) viewDefinitionState
                        .getComponentByReference("machineTimeBalance");
                machineTimeBalanceField.setFieldValue(productionRecord.getStringField("machineTimeBalance"));
                // FieldComponent laborPlannedTimeField = (FieldComponent) viewDefinitionState
                // .getComponentByReference("laborPlannedTime");
                FieldComponent laborRegisteredTimeField = (FieldComponent) viewDefinitionState
                        .getComponentByReference("laborRegisteredTime");
                laborRegisteredTimeField.setFieldValue(productionRecord.getStringField("laborTime"));
                FieldComponent laborTimeBalanceField = (FieldComponent) viewDefinitionState
                        .getComponentByReference("laborTimeBalance");
                laborTimeBalanceField.setFieldValue(productionRecord.getStringField("laborTimeBalance"));
            }
        }
    }

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("date", null);
        entity.setField("generated", false);
        entity.setField("fileName", null);
        entity.setField("worker", null);
        return true;
    }

    public void setGenerateButtonState(final ViewDefinitionState state) {
        setGenerateButtonState(state, state.getLocale(), ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE);
    }

    public void setGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
            final String entityName) {
        WindowComponent window = (WindowComponent) state.getComponentByReference("window");
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        RibbonActionItem generateButton = window.getRibbon().getGroupByName("generate").getItemByName("generate");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (form.getEntityId() == null) {
            generateButton.setMessage("recordNotCreated");
            generateButton.setEnabled(false);
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {
            Entity productionBalance = dataDefinitionService.get(plugin, entityName).get(form.getEntityId());

            if (productionBalance.getField("generated") == null)
                productionBalance.setField("generated", "0");

            if ("1".equals(productionBalance.getField("generated"))) {
                generateButton.setMessage("orders.ribbon.message.recordAlreadyGenerated");
                generateButton.setEnabled(false);
                deleteButton.setMessage("orders.ribbon.message.recordAlreadyGenerated");
                deleteButton.setEnabled(false);
            } else {
                generateButton.setMessage(null);
                generateButton.setEnabled(true);
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            }

        }
        generateButton.requestUpdate(true);
        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
    }

    @Transactional
    public void generateProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity productionBalance = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PRODUCTION_BALANCE).get((Long) state.getFieldValue());

            if (productionBalance == null) {
                String message = translationService.translate("qcadooView.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(productionBalance.getStringField("fileName"))) {
                String message = translationService.translate(
                        "productionCounting.productionBalance.report.error.documentsWasGenerated", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            productionBalance = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PRODUCTION_BALANCE).get((Long) state.getFieldValue());

            try {
                generateProductionBalanceDocuments(state, productionBalance);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public void printProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity productionBalance = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PRODUCTION_BALANCE).get((Long) state.getFieldValue());
            if (productionBalance == null) {
                state.addMessage(translationService.translate("qcadooView.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(productionBalance.getStringField("fileName"))) {
                state.addMessage(translationService.translate(
                        "productionCounting.productionBalance.report.error.documentsWasNotGenerated", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo(
                        "/productionCounting/productionBalance." + args[0] + "?id=" + state.getFieldValue(), false, false);
            }
        } else {
            if (state instanceof FormComponent) {
                state.addMessage(translationService.translate("qcadooView.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("qcadooView.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    private void generateProductionBalanceDocuments(final ComponentState state, final Entity productionBalance)
            throws IOException, DocumentException {
        Entity productionBalanceWithFileName = updateFileName(productionBalance,
                getFullFileName((Date) productionBalance.getField("date"), productionBalance.getStringField("name")),
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE);
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .uniqueResult();
        productionBalancePdfService.generateDocument(productionBalanceWithFileName, company, state.getLocale());
    }

    private String getFullFileName(final Date date, final String fileName) {
        return path + fileName + "_" + new SimpleDateFormat(DateUtils.REPORT_DATE_TIME_FORMAT).format(date);
    }

    private Entity updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, entityName).save(entity);
    }

}
