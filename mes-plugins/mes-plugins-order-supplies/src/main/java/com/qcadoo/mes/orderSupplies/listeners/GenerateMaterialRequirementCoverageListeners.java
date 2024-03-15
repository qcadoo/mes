/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.orderSupplies.listeners;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.CURRENCY;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.EXTERNAL_SYNCHRONIZED;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.qcadoo.mes.orderSupplies.coverage.coverageAnalysis.CoverageAnalysisForOrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.CoverageLocationFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductGeneratedFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductSelectedFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageService;
import com.qcadoo.mes.orderSupplies.print.MaterialRequirementCoverageReportPdfService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class GenerateMaterialRequirementCoverageListeners {

    private static final String L_GRID = "coverageProducts";

    public static final String L_REQUIRE_SUPPLIER_IDENTIFICATION = "requireSupplierIdentification";

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private MaterialRequirementCoverageService materialRequirementCoverageService;

    @Autowired
    private MaterialRequirementCoverageReportPdfService materialRequirementCoverageReportPdfService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private ParameterService parameterService;

    private DataDefinition coverageProductSelectedDataDefinition;

    private DataDefinition coverageProductGeneratedDataDefinition;

    private DataDefinition orderedProductDataDefinition;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private CoverageAnalysisForOrderService coverageAnalysisForOrderService;

    public void init() {
        coverageProductSelectedDataDefinition = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                CoverageProductSelectedFields.ENTITY_NAME);
        coverageProductGeneratedDataDefinition = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                CoverageProductGeneratedFields.ENTITY_NAME);
        orderedProductDataDefinition = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_ORDERED_PRODUCT);
    }

    public final void generateCoverageAnalysis(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();
        if (materialRequirementCoverageId != null) {
            coverageAnalysisForOrderService.coverageAnalysis(materialRequirementCoverageId);
            state.addMessage("orderSupplies.materialRequirementCoverage.report.generateCoverageAnalysis", MessageType.SUCCESS);
        }
    }
    public final void generateMaterialRequirementCoverage(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        state.performEvent(view, "save", args);

        if (!state.isHasError()) {
            generate(view, state, args);
        }
    }

    @Transactional
    public final void generate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

        if (materialRequirementCoverageId != null) {

            orderSuppliesService.clearMaterialRequirementCoverage(materialRequirementCoverageId);
            Entity materialRequirementCoverage = orderSuppliesService
                    .getMaterialRequirementCoverage(materialRequirementCoverageId);

            materialRequirementCoverage.setField(MaterialRequirementCoverageFields.GENERATED, true);
            materialRequirementCoverage.setField(MaterialRequirementCoverageFields.GENERATED_DATE,
                    new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, LocaleContextHolder.getLocale()).format(new Date()));
            materialRequirementCoverage.setField(MaterialRequirementCoverageFields.GENERATED_BY,
                    securityService.getCurrentUserName());

            materialRequirementCoverageService.estimateProductCoverageInTime(materialRequirementCoverage);
            Entity assignedOrder = materialRequirementCoverage.getBelongsToField("order");
            if(Objects.nonNull(assignedOrder) && Objects.isNull(assignedOrder.getDateField(OrderFields.START_DATE))) {
                state.addMessage("orderSupplies.materialRequirementCoverage.report.datesIsEmptyInOrder", MessageType.INFO, false);
            }

            state.performEvent(view, "reset", new String[0]);

            if (materialRequirementCoverage.getGlobalMessages().isEmpty()) {
                state.addMessage("orderSupplies.materialRequirementCoverage.report.generatedMessage", MessageType.SUCCESS);
                if (Objects.nonNull(assignedOrder) && orderSuppliesService
                        .getMaterialRequirementCoverage(materialRequirementCoverageId).getHasManyField(MaterialRequirementCoverageFields.COVERAGE_PRODUCTS).isEmpty()) {
                    state.addMessage("orderSupplies.materialRequirementCoverage.report.coverageProductsIsEmpty", MessageType.INFO);
                }
            } else {
                materialRequirementCoverage.getGlobalMessages()
                        .forEach(message -> view.addMessage(message.getMessage(), MessageType.INFO, false));
            }
            if (materialRequirementCoverage.getBooleanField(MaterialRequirementCoverageFields.AUTOMATIC_SAVE_COVERAGE)) {
                saveMaterialRequirementCoverage(view, state, args);
            }
        }
        state.performEvent(view, "refresh", new String[0]);
    }

    public final void printMaterialRequirementCoverage(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {

        if (state instanceof FormComponent) {
            state.performEvent(view, "save", args);

            if (!state.isHasError()) {
                FormComponent materialRequirementCoverageForm = (FormComponent) view
                        .getComponentByReference(QcadooViewConstants.L_FORM);
                Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

                boolean saved = orderSuppliesService.checkIfMaterialRequirementCoverageIsSaved(materialRequirementCoverageId);

                if (saved) {
                    reportService.printGeneratedReport(view, state,
                            new String[] { args[0], OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                                    OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE });
                } else {
                    view.redirectTo(
                            "/orderSupplies/materialRequirementCoverageReport." + args[0] + "?id=" + state.getFieldValue(), true,
                            false);
                }
            }
        } else {
            state.addMessage("orderSupplies.materialRequirementCoverage.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void saveMaterialRequirementCoverage(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        state.performEvent(view, "save", args);

        if (!state.isHasError()) {
            FormComponent materialRequirementCoverageForm = (FormComponent) view
                    .getComponentByReference(QcadooViewConstants.L_FORM);
            Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

            if (materialRequirementCoverageId != null) {
                Entity materialRequirementCoverage = orderSuppliesService
                        .getMaterialRequirementCoverage(materialRequirementCoverageId);

                materialRequirementCoverage.setField(MaterialRequirementCoverageFields.SAVED, true);

                materialRequirementCoverage = materialRequirementCoverage.getDataDefinition().save(materialRequirementCoverage);

                try {
                    generateMaterialRequirementCoverageReport(materialRequirementCoverage, state.getLocale());

                    state.performEvent(view, "clear", new String[0]);

                    state.addMessage("orderSupplies.materialRequirementCoverage.report.savedMessage", MessageType.SUCCESS,
                            materialRequirementCoverage.getStringField(MaterialRequirementCoverageFields.NUMBER));
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                } catch (DocumentException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        }
    }

    private void generateMaterialRequirementCoverageReport(final Entity materialRequirementCoverage, final Locale locale)
            throws IOException, DocumentException {
        String localePrefix = "orderSupplies.materialRequirementCoverage.report.fileName";

        Entity materialRequirementCoverageWithFileName = fileService.updateReportFileName(materialRequirementCoverage,
                MaterialRequirementCoverageFields.GENERATED_DATE, localePrefix);

        try {
            materialRequirementCoverageReportPdfService.generateDocument(materialRequirementCoverageWithFileName, locale,
                    PageSize.A4.rotate());
        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving materialRequirementCoverage report");
        } catch (DocumentException e) {
            throw new IllegalStateException("Problem with generating materialRequirementCoverage report");
        }
    }

    public final void showMaterialRequirementCoverages(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        String url = "../page/orderSupplies/materialRequirementCoveragesList.html";
        view.redirectTo(url, false, true);
    }

    public void showReplacementsAvailability(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();
        Entity materialRequirement = dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE)
                .get(materialRequirementCoverageId);
        GridComponent grid = (GridComponent) view.getComponentByReference("coverageProducts");
        Long cpId = grid.getSelectedEntitiesIds().stream().findFirst().get();

        Entity cp = dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT).get(cpId);
        Entity product = cp.getBelongsToField(CoverageProductFields.PRODUCT);

        JSONObject json = new JSONObject();

        try {
            json.put("product.id", product.getId());
            json.put("locationsIds",
                    Lists.newArrayList(materialRequirement.getHasManyField(MaterialRequirementCoverageFields.COVERAGE_LOCATIONS)
                            .stream().map(cl -> cl.getBelongsToField(CoverageLocationFields.LOCATION).getId())
                            .collect(Collectors.toList())));
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        String url = "/page/productFlowThruDivision/materialReplacementsAvailabilityList.html?context=" + json.toString();
        view.redirectTo(url, false, true);

    }

    /**
     * Deleting coverageProductSelected
     */
    private void deleteSelected() {
        coverageProductSelectedDataDefinition.find().list().getEntities().forEach(entity -> {
            coverageProductSelectedDataDefinition.delete(entity.getId());
        });
    }

    /**
     * Inserting selected items to coverageProductSelected
     *
     * @param grid
     */
    private void insertSelected(final GridComponent grid) {
        grid.getSelectedEntities().forEach(coverageProduct -> {
            Entity selected = coverageProductSelectedDataDefinition.create();
            selected.setField(CoverageProductSelectedFields.COVERAGE_PRODUCT, coverageProduct);
            coverageProductSelectedDataDefinition.save(selected);
        });
    }

    /**
     * Record of generated items
     *
     * @param grid
     */
    private void insertGenerated(final GridComponent grid) {
        grid.getSelectedEntities().forEach(coverageProduct -> {
            Entity generated = coverageProductGeneratedDataDefinition.create();
            Entity product = coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);
            generated.setField(CoverageProductGeneratedFields.PRODUCT_ID, product.getId().intValue());
            coverageProductGeneratedDataDefinition.save(generated);
        });
    }

    /**
     * createRequestForQuotation event handler
     *
     * @param view
     * @param state
     * @param args
     */
    public void createRequestForQuotation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        init();
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        deleteSelected();
        insertSelected(grid);
        // FIXME - kama - it looks completely unnecessary, it was reffered to only in unused method
        // com.qcadoo.mes.orderSupplies.hooks.GenerateMaterialRequirementCoverageHooks.fillRowStylesBasedOnGenerated
        // if fillRowStylesBasedOnGenerated is supposed to work somehow, this should be fixed
        // insertGenerated(grid);

        view.redirectTo("../page/" + OrderSuppliesConstants.PLUGIN_IDENTIFIER + "/supplierModalRequestForQuotation.html", false,
                true);
    }

    /**
     * createOffer event handler
     *
     * @param view
     * @param state
     * @param args
     */
    public void createDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        init();
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        Entity parameter = parameterService.getParameter();
        Entity systemCurrency = parameter.getBelongsToField(ParameterFields.CURRENCY);
        DataDefinition companyDataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_COMPANY);
        Map<Optional<Integer>, List<Entity>> groupedCoverageProducts = grid.getSelectedEntities().stream()
                .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getIntegerField("companyId"))));

        StringBuilder deliveryNumbers = new StringBuilder();

        for (Map.Entry<Optional<Integer>, List<Entity>> entry : groupedCoverageProducts.entrySet()) {
            String number = numberGeneratorService.generateNumber(DeliveriesConstants.PLUGIN_IDENTIFIER,
                    DeliveriesConstants.MODEL_DELIVERY);

            Entity delivery = deliveriesService.getDeliveryDD().create();
            delivery.setField(NUMBER, number);
            Entity currency = null;
            if (entry.getKey().isPresent()) {
                Entity supplier = companyDataDefinition.get(entry.getKey().get().longValue());
                delivery.setField(SUPPLIER, supplier);
                currency = supplier.getBelongsToField(CompanyFieldsD.CURRENCY);
            }
            if (currency == null) {
                currency = systemCurrency;
            }
            delivery.setField(CURRENCY, currency);
            delivery.setField(EXTERNAL_SYNCHRONIZED, true);

            Entity saved = deliveriesService.getDeliveryDD().save(delivery);
            if (saved.isValid()) {
                deliveryNumbers.append("<br/>").append(number);
                entry.getValue().forEach(coverageProduct -> {
                    Integer product = coverageProduct.getIntegerField("productId");
                    BigDecimal reserveMissingQuantity = coverageProduct
                            .getDecimalField(CoverageProductFields.RESERVE_MISSING_QUANTITY);

                    BigDecimal orderedQuantity = reserveMissingQuantity.min(BigDecimal.ZERO).abs();
                    BigDecimal conversion = getConversion(product);

                    Entity orderedProduct = orderedProductDataDefinition.create();
                    orderedProduct.setField("delivery", saved);
                    orderedProduct.setField("product", product.longValue());
                    orderedProduct.setField("orderedQuantity", reserveMissingQuantity.min(BigDecimal.ZERO).abs());

                    orderedProduct.setField(OrderedProductFields.CONVERSION, conversion);
                    orderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY,
                            orderedQuantity.multiply(conversion, numberService.getMathContext()));

                    orderedProductDataDefinition.save(orderedProduct);
                });
            }
            if (parameter.getBooleanField(L_REQUIRE_SUPPLIER_IDENTIFICATION)
                    && Objects.isNull(delivery.getBelongsToField(SUPPLIER))) {
                state.addMessage("orderSupplies.materialRequirementCoverage.deliveries.requireSupplierIdentification",
                        MessageType.INFO, false);
            }
        }
        if (StringUtils.isNoneEmpty(deliveryNumbers.toString())) {
            state.addMessage("orderSupplies.materialRequirementCoverage.deliveries.created", MessageType.SUCCESS, false,
                    deliveryNumbers.toString());
        } else {
            state.addMessage("orderSupplies.materialRequirementCoverage.deliveries.notCreated", MessageType.INFO, false);
        }
    }

    private BigDecimal getConversion(Integer productId) {
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)
                .get(productId.longValue());
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        if (additionalUnit == null) {
            return BigDecimal.ONE;
        }
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder
                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));
        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }
}
