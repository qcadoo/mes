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
package com.qcadoo.mes.orderSupplies.aop;

import com.google.common.collect.Sets;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orderSupplies.constants.ParameterFieldsOS;
import com.qcadoo.mes.warehouseMinimalState.WarehouseMinimalStateHelper;
import com.qcadoo.mes.warehouseMinimalState.print.DocumentPdf;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.view.constants.RowStyle;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Aspect
@Configurable
@RunIfEnabled(OrderSuppliesConstants.PLUGIN_IDENTIFIER)
public class WarehouseMinimalStateAspectOS {

    @Autowired
    private WarehouseMinimalStateHelper warehouseMinimalStateHelper;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Pointcut("execution(public boolean com.qcadoo.mes.warehouseMinimalState.WarehouseMinimalStateHelper.checkIfLowerThanMinimum(..)) "
            + "&& args(productId, quantity, minimumState)")
    public void checkIfLowerThanMinimum(long productId, BigDecimal quantity, BigDecimal minimumState) {
    }

    @Around("checkIfLowerThanMinimum(productId, quantity, minimumState)")
    public boolean checkIfLowerThanMinimumWithPlanned(final ProceedingJoinPoint pjp, long productId, BigDecimal quantity,
            BigDecimal minimumState) throws Throwable {
        if (includeRequirements()) {
            int ordersIncludePeriod = getOrdersIncludePeriod();
            BigDecimal planned = getPlannedQuantity(productId, ordersIncludePeriod);
            BigDecimal quantityWithoutPlanned = quantity.subtract(planned, numberService.getMathContext());
            return quantityWithoutPlanned.compareTo(minimumState) < 0;
        } else {
            return (boolean) pjp.proceed();
        }
    }

    private boolean includeRequirements() {
        Entity parameter = parameterService.getParameter();
        return parameter.getBooleanField(ParameterFieldsOS.INCLUDE_REQUIREMENTS);
    }

    private int getOrdersIncludePeriod() {
        Entity parameter = parameterService.getParameter();
        Integer includePeriod = parameter.getIntegerField(ParameterFieldsOS.ORDERS_INCLUDE_PERIOD);
        return includePeriod == null ? 0 : includePeriod;
    }

    private BigDecimal getPlannedQuantity(long productId, int daysForward) {
        DateTime today = DateTime.now();
        DateTime endDate = today.plusDays(daysForward);
        String requirementQuery = "select COALESCE(sum(o.quantity),0) as required from #orderSupplies_productionCountingQuantityInput o where o.productId = :productId "
                + "and eventType in ('03operationInput', '04orderInput') and o.startDate <= :endTimestamp group by o.productId";
        DataDefinition ordersDataDefinition = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                "productionCountingQuantityInput");
        Entity entity = ordersDataDefinition.find(requirementQuery).setParameter("endTimestamp", endDate.toDate())
                .setLong("productId", productId).setMaxResults(1).uniqueResult();
        if (entity != null) {
            return BigDecimalUtils.convertNullToZero(entity.getDecimalField("required"));
        } else {
            return BigDecimal.ZERO;
        }
    }

    @Pointcut("execution(public java.util.Set<java.lang.String> com.qcadoo.mes.materialFlowResources.rowStyleResolvers.WarehouseStocksListResolver.fillRowStyles(..)) "
            + "&& args(warehouseStocks)")
    public void fillRowStyles(final Entity warehouseStocks) {
    }

    @Around("fillRowStyles(warehouseStocks)")
    public Set<String> fillRowStylesWithPlanned(final ProceedingJoinPoint pjp, Entity warehouseStocks) throws Throwable {
        if (includeRequirements()) {
            final Set<String> rowStyles = Sets.newHashSet();

            if (warehouseStocks.getDecimalField("minimumState") != null
                    && BigDecimal.ZERO.compareTo(BigDecimalUtils.convertNullToZero(warehouseStocks.getDecimalField("minimumState"))) != 0) {
                Long productId = warehouseStocks.getIntegerField("product_id").longValue();
                BigDecimal minimumState = BigDecimalUtils.convertNullToZero(warehouseStocks.getDecimalField("minimumState"));
                BigDecimal quantity = BigDecimalUtils.convertNullToZero(warehouseStocks.getDecimalField("quantity"));
                if (warehouseMinimalStateHelper.checkIfLowerThanMinimum(productId, quantity, minimumState)) {
                    rowStyles.add(RowStyle.RED_BACKGROUND);
                }
            }
            return rowStyles;
        } else {
            return (Set<String>) pjp.proceed();
        }
    }

    @Pointcut("execution(private void com.qcadoo.mes.warehouseMinimalState.print.DocumentPdf.addAdditionalHeaders(..)) "
            + "&& args(headerLabels, locale)")
    public void addAdditionalHeaders(Map<String, HeaderAlignment> headerLabels, Locale locale) {
    }

    @Around("addAdditionalHeaders(headerLabels, locale)")
    public void addAdditionalHeaders(final ProceedingJoinPoint pjp, Map<String, HeaderAlignment> headerLabels, Locale locale)
            throws Throwable {
        if (includeRequirements()) {
            headerLabels.put(translationService.translate("deliveries.minimalStateReport.columnHeader.plannedQuantity", locale),
                    HeaderAlignment.RIGHT);
        }
        pjp.proceed();
    }

    @Pointcut("execution(private void com.qcadoo.mes.warehouseMinimalState.print.DocumentPdf.addAdditionalCells(..)) "
            + "&& args(table, product)")
    public void addAdditionalCells(PdfPTable table, Entity product) {
    }

    @Around("addAdditionalCells(table, product)")
    public void addAdditionalCells(final ProceedingJoinPoint pjp, PdfPTable table, Entity product) throws Throwable {
        if (includeRequirements()) {
            int ordersIncludePeriod = getOrdersIncludePeriod();
            BigDecimal planned = getPlannedQuantity(product.getId(), ordersIncludePeriod);
            DocumentPdf document = (DocumentPdf) pjp.getThis();
            document.addSmallCell(table, planned);
        }
        pjp.proceed();
    }

    @Pointcut("execution(private java.util.List<java.lang.Integer> com.qcadoo.mes.warehouseMinimalState.print.DocumentPdf.getHeaderWidths(..))")
    public void getHeaderWidths() {
    }

    @Around("getHeaderWidths()")
    public List<Integer> getHeaderWidths(final ProceedingJoinPoint pjp) throws Throwable {
        List<Integer> widths = (List<Integer>) pjp.proceed();
        if (includeRequirements()) {
            widths.add(widths.size() - 2, 45);
        }
        return widths;
    }
}
