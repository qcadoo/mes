/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.internal;

import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceService {

    private static final String WORKER_FIELD = "worker";

    private static final String FILE_NAME_FIELD = "fileName";

    private static final String GENERATED_FIELD = "generated";

    private static final String DATE_FIELD = "date";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(FILE_NAME_FIELD, null);
        entity.setField(GENERATED_FIELD, false);
        entity.setField(DATE_FIELD, null);
        entity.setField(WORKER_FIELD, null);
        return true;
    }

    public boolean validateOrder(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getBelongsToField(MODEL_ORDER).getStringField("typeOfProductionRecording") == null
                || entity.getBelongsToField(MODEL_ORDER).getStringField("typeOfProductionRecording").equals("01none")) {
            entity.addError(dataDefinition.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRecordingType");
            return false;
        }
        List<Entity> productionRecordList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(MODEL_ORDER, entity.getBelongsToField(MODEL_ORDER))).list().getEntities();
        if (productionRecordList.size() == 0) {
            entity.addError(dataDefinition.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutProductionRecords");
            return false;
        }
        return true;
    }

    @Transactional
    public void generateProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference(GENERATED_FIELD);
            ComponentState date = viewDefinitionState.getComponentByReference(DATE_FIELD);
            ComponentState worker = viewDefinitionState.getComponentByReference(WORKER_FIELD);
            FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference("name");
            FieldComponent description = (FieldComponent) viewDefinitionState.getComponentByReference("description");
            FieldComponent order = (FieldComponent) viewDefinitionState.getComponentByReference(MODEL_ORDER);
            Entity productionBalance = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PRODUCTION_BALANCE).get((Long) state.getFieldValue());

            if (productionBalance == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(productionBalance.getStringField(FILE_NAME_FIELD))) {
                state.addMessage("productionCounting.productionBalance.report.error.documentsWasGenerated", MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                name.setEnabled(false);
                order.setEnabled(false);
                description.setEnabled(false);
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, LocaleContextHolder.getLocale())
                        .format(new Date()));
                requestComponentUpdateState(viewDefinitionState);
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

    private void requestComponentUpdateState(final ViewDefinitionState view) {
        for (String reference : Arrays.asList("name", "description", MODEL_ORDER, WORKER_FIELD, GENERATED_FIELD, DATE_FIELD)) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(reference);
            component.requestComponentUpdateState();
        }
    }

    public void printProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_BALANCE });
    }

    private void generateProductionBalanceDocuments(final ComponentState state, final Entity productionBalance)
            throws IOException, DocumentException {
        Entity productionBalanceWithFileName = fileService.updateReportFileName(productionBalance, "date",
                "productionCounting.productionBalance.report.fileName");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        productionBalancePdfService.generateDocument(productionBalanceWithFileName, company, state.getLocale());
    }

}
