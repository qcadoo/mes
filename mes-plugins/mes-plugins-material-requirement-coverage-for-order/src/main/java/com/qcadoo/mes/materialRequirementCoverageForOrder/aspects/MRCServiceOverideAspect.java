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
package com.qcadoo.mes.materialRequirementCoverageForOrder.aspects;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costCalculation.constants.MaterialCostsUsed;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.MaterialRequirementCoverageForOrderConstans;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

@Aspect
@Configurable
@Service
@RunIfEnabled(MaterialRequirementCoverageForOrderConstans.PLUGIN_IDENTIFIER)
public class MRCServiceOverideAspect {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ParameterService parameterService;

    private static final String L_PRODUCT_TYPE = "productType";

    private static final String L_PLANNED_QUANTITY = "planedQuantity";

    @Pointcut("execution(public void com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageServiceImpl.additionalProcessProductCoverage(..)) "
            + "&& args(materialRequirementCoverage, productAndCoverageProducts)")
    public void additionalProcessProductCoverageA(final Entity materialRequirementCoverage,
            final Map<Long, Entity> productAndCoverageProducts) {
    }

    @Around("additionalProcessProductCoverageA(materialRequirementCoverage, productAndCoverageProducts)")
    public void aroundAdditionalProcessProductCoverage(final ProceedingJoinPoint pjp, final Entity materialRequirementCoverage,
            final Map<Long, Entity> productAndCoverageProducts) throws Throwable {

        Entity order = materialRequirementCoverage.getBelongsToField("order");
        if (order == null) {
            pjp.proceed();
        } else {

            List<Entity> orders = Lists.newArrayList(order);
            materialRequirementCoverage.setField("coverageOrders", orders);

            String sql;

            sql = "SELECT distinct registry.productId AS productId FROM #orderSupplies_productionCountingQuantityInput AS registry "
                    + "WHERE registry.orderId IN :ids AND eventType IN ('04orderInput','03operationInput')";
            List<Entity> regs = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, "productionCountingQuantityInput").find(sql)
                    .setParameter("ids", order.getId().intValue()).list().getEntities();
            List<Long> pids = getIdsFromRegisterProduct(regs);
            for (Map.Entry<Long, Entity> productAndCoverageProduct : productAndCoverageProducts.entrySet()) {
                Entity addedCoverageProduct = productAndCoverageProduct.getValue();
                if (pids.contains(productAndCoverageProduct.getKey())) {
                    addedCoverageProduct.setField(CoverageProductFields.FROM_SELECTED_ORDER, true);
                } else {
                    addedCoverageProduct.setField(CoverageProductFields.FROM_SELECTED_ORDER, false);
                }
            }

        }
    }

    private List<Long> getIdsFromRegisterProduct(List<Entity> registerProducts) {

        return registerProducts.stream().map(p -> ((Number) p.getField("productId")).longValue()).collect(Collectors.toList());
    }

    @Pointcut("execution(private void com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageServiceImpl.saveCoverageProduct(..)) "
            + "&& args(materialRequirementCoverage, covProduct)")
    public void saveCoverageProductA(Entity materialRequirementCoverage, Entity covProduct) {
    }

    @Around("saveCoverageProductA(materialRequirementCoverage, covProduct)")
    public void aroundSaveCoverageProduct(final ProceedingJoinPoint pjp, Entity materialRequirementCoverage, Entity covProduct) {
        String sql = "INSERT INTO ordersupplies_coverageproduct "
                + "(materialrequirementcoverage_id, product_id, lackfromdate, demandquantity, coveredquantity, "
                + "reservemissingquantity, deliveredquantity, locationsquantity, state, productnumber, productname, "
                + "productunit, productType, planedQuantity, produceQuantity,fromSelectedOrder, company_id, price) "
                + "VALUES (:materialrequirementcoverage_id, :product_id, :lackfromdate, :demandquantity, :coveredquantity, "
                + ":reservemissingquantity, :deliveredquantity, :locationsquantity, :state, :productnumber, :productname, "
                + ":productunit, :productType, :planedQuantity, :produceQuantity,:fromSelectedOrder, :company_id, :price)";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(L_PRODUCT_TYPE, covProduct.getStringField(L_PRODUCT_TYPE));
        parameters.put(L_PLANNED_QUANTITY, covProduct.getDecimalField(L_PLANNED_QUANTITY));
        parameters.put("materialrequirementcoverage_id", materialRequirementCoverage.getId());
        parameters.put("product_id", covProduct.getBelongsToField(CoverageProductFields.PRODUCT).getId());
        Entity company = covProduct.getBelongsToField(CoverageProductFields.COMPANY);
        if (company != null) {
            parameters.put("company_id", company.getId());
        } else {
            parameters.put("company_id", null);
        }
        parameters.put("lackfromdate", covProduct.getDateField(CoverageProductFields.LACK_FROM_DATE));
        parameters.put("demandquantity", covProduct.getDecimalField(CoverageProductFields.DEMAND_QUANTITY));
        parameters.put("coveredquantity", covProduct.getDecimalField(CoverageProductFields.COVERED_QUANTITY));
        parameters.put("reservemissingquantity", covProduct.getDecimalField(CoverageProductFields.RESERVE_MISSING_QUANTITY));
        parameters.put("deliveredquantity", covProduct.getDecimalField(CoverageProductFields.DELIVERED_QUANTITY));
        parameters.put("locationsquantity", covProduct.getDecimalField(CoverageProductFields.LOCATIONS_QUANTITY));
        parameters.put("produceQuantity", covProduct.getDecimalField(CoverageProductFields.PRODUCE_QUANTITY));
        parameters.put("state", covProduct.getStringField(CoverageProductFields.STATE));
        parameters.put("productnumber",
                covProduct.getBelongsToField(CoverageProductFields.PRODUCT).getStringField(ProductFields.NUMBER));
        parameters.put("productname",
                covProduct.getBelongsToField(CoverageProductFields.PRODUCT).getStringField(ProductFields.NAME));
        parameters.put("productunit",
                covProduct.getBelongsToField(CoverageProductFields.PRODUCT).getStringField(ProductFields.UNIT));
        parameters.put("fromSelectedOrder", covProduct.getBooleanField(CoverageProductFields.FROM_SELECTED_ORDER));

        parameters.put("price", covProduct.getDecimalField(CoverageProductFields.PRICE));

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);
        jdbcTemplate.update(sql, namedParameters);
    }

}
