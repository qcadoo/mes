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
package com.qcadoo.mes.orderSupplies;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orderSupplies.constants.ColumnForCoveragesFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RunIfEnabled(OrderSuppliesConstants.PLUGIN_IDENTIFIER)
public class OrderSuppliesServiceImpl implements OrderSuppliesService {

    public static final String L_COVERAGEPRODUCTSELECTED_COVERAGEPRODUCT_FKEY = "coverageproductselected_coverageproduct_fkey";

    public static final String L_COVERAGEPRODUCTLOGGING_COVERAGEPRODUCT_FKEY = "coverageproductlogging_coverageproduct_fkey";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MultiTenantService multiTenantService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Entity getMaterialRequirementCoverage(final Long materialRequirementCoverageId) {
        return getMaterialRequirementCoverageDD().get(materialRequirementCoverageId);
    }

    @Override
    public Entity getCoverageLocation(final Long coverageLocationId) {
        return getCoverageLocationDD().get(coverageLocationId);
    }

    @Override
    public Entity getCoverageProduct(final Long coverageProductId) {
        return getCoverageProductDD().get(coverageProductId);
    }

    @Override
    public Entity getCoverageProductLogging(final Long coverageProductLoggingId) {
        return getCoverageProductLoggingDD().get(coverageProductLoggingId);
    }

    @Override
    public List<Entity> getColumnsForCoverages() {
        return getColumnForCoveragesDD().find().addOrder(SearchOrders.asc(ColumnForCoveragesFields.SUCCESSION)).list()
                .getEntities();
    }

    @Override
    public DataDefinition getMaterialRequirementCoverageDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE);
    }

    @Override
    public DataDefinition getCoverageLocationDD() {
        return dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_LOCATION);
    }

    @Override
    public DataDefinition getCoverageOrderStateDD() {

        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COVERAGE_ORDER_STATE);
    }

    @Override
    public DataDefinition getCoverageProductDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT);
    }

    @Override
    public DataDefinition getCoverageProductLoggingDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT_LOGGING);
    }

    @Override
    public DataDefinition getColumnForCoveragesDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COLUMN_FOR_COVERAGES);
    }

    @Override
    public boolean checkIfMaterialRequirementCoverageIsSaved(final Long materialRequirementCoverageId) {
        if (materialRequirementCoverageId != null) {
            Entity materialRequirementCoverage = getMaterialRequirementCoverage(materialRequirementCoverageId);

            if (materialRequirementCoverage != null) {
                return materialRequirementCoverage.getBooleanField(MaterialRequirementCoverageFields.SAVED);
            }
        }

        return false;
    }

    @Override
    public void deleteUnsavedMaterialRequirementCoveragesTrigger() {
        multiTenantService.doInMultiTenantContext(new MultiTenantCallback() {

            @Override
            public void invoke() {
                deleteUnsavedMaterialRequirementCoverages();
            }

        });

    }

    private void deleteUnsavedMaterialRequirementCoverages() {
        Date currentDate = new DateTime(new Date()).minusDays(1).toDate();
        List<Entity> materialRequirementCoverages = getMaterialRequirementCoverageDD().find()
                .add(SearchRestrictions.eq(MaterialRequirementCoverageFields.SAVED, false))
                .add(SearchRestrictions.lt("updateDate", currentDate)).list().getEntities();

        if (!materialRequirementCoverages.isEmpty()) {
            List<Long> idsList = materialRequirementCoverages.stream().map(mrc -> mrc.getId()).filter(Objects::nonNull)
                    .collect(Collectors.toList());
            deleteMaterialRequirementCoverageAndReferences(idsList);
        }
    }

    @Override
    @Transactional
    public void deleteMaterialRequirementCoverageAndReferences(final List<Long> idsList) {
        Map<String, Object> coverageProductConstraintNames = getCoverageProductConstraintNames();

        dropCoverageProductConstraints(coverageProductConstraintNames);

        String query = "BEGIN;\n"

                + "SET CONSTRAINTS ALL DEFERRED;\n"

                + "DELETE FROM jointable_materialrequirementcoverage_order WHERE materialrequirementcoverage_id = :id;\n"
                + "DELETE FROM orderSupplies_coverageLocation WHERE materialrequirementcoverage_id = :id;\n"
                + "DELETE FROM orderSupplies_coverageOrderState WHERE materialrequirementcoverage_id = :id;\n"
                + "DELETE FROM orderSupplies_coverageProductLogging logging USING orderSupplies_coverageProduct coverageProduct"
                + " WHERE logging.coverageproduct_id = coverageProduct.id AND coverageProduct.materialrequirementcoverage_id = :id;\n"
                + "DELETE FROM orderSupplies_coverageProduct WHERE materialrequirementcoverage_id = :id;\n"
                + "DELETE FROM ordersupplies_coverageAnalysisForOrder WHERE materialrequirementcoverage_id = :id;\n"
                + "DELETE FROM orderSupplies_materialRequirementCoverage WHERE id = :id;\n"

                + "SET CONSTRAINTS ALL IMMEDIATE;\n"

                + "END;";

        for (int i = 0; i < idsList.size(); i++) {
            Map<String, Object> params = Maps.newHashMap();

            params.put("id", idsList.get(i));

            jdbcTemplate.update(query, params);
        }

        addCoverageProductConstraints(coverageProductConstraintNames);
    }

    @Override
    public void clearMaterialRequirementCoverage(final Long id) {
        Map<String, Object> coverageProductConstraintNames = getCoverageProductConstraintNames();

        dropCoverageProductConstraints(coverageProductConstraintNames);

        String query = "BEGIN;\n"

                + "SET CONSTRAINTS ALL DEFERRED;\n"

                + "DELETE FROM orderSupplies_coverageProductLogging logging USING orderSupplies_coverageProduct coverageProduct"
                + " WHERE logging.coverageproduct_id = coverageProduct.id AND coverageProduct.materialrequirementcoverage_id = :id;\n"
                + "DELETE FROM orderSupplies_coverageProduct WHERE materialrequirementcoverage_id = :id;\n"

                + "SET CONSTRAINTS ALL IMMEDIATE;\n"

                + "END;";

        Map<String, Object> params = Maps.newHashMap();

        params.put("id", id);

        jdbcTemplate.update(query, params);

        addCoverageProductConstraints(coverageProductConstraintNames);
    }

    private Map<String, Object> getCoverageProductConstraintNames() {
        Map<String, Object> coverageProductConstraintNames = Maps.newHashMap();

        String coverageProductSelectedCoverageProuductFkey = getConstraintName("ordersupplies_coverageproductselected",
                "ordersupplies_coverageproduct");
        String coverageProductLoggingCoverageProductFkey = getConstraintName("ordersupplies_coverageproductlogging",
                "ordersupplies_coverageproduct");

        if (coverageProductSelectedCoverageProuductFkey != null) {
            coverageProductConstraintNames.put(L_COVERAGEPRODUCTSELECTED_COVERAGEPRODUCT_FKEY,
                    coverageProductSelectedCoverageProuductFkey);
        }
        if (coverageProductLoggingCoverageProductFkey != null) {
            coverageProductConstraintNames.put(L_COVERAGEPRODUCTLOGGING_COVERAGEPRODUCT_FKEY,
                    coverageProductLoggingCoverageProductFkey);
        }

        return coverageProductConstraintNames;
    }

    private String getConstraintName(final String tableName, final String foreignTableName) {
        String query = "SELECT tc.constraint_name FROM information_schema.table_constraints AS tc"
                + " JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name "
                + " WHERE tc.constraint_schema = 'public' AND tc.constraint_type = 'FOREIGN KEY' "
                + " AND tc.table_name = :table_name AND ccu.table_name = :foreign_table_name;";

        Map<String, Object> tableNames = Maps.newHashMap();

        tableNames.put("table_name", tableName);
        tableNames.put("foreign_table_name", foreignTableName);

        try {
            return jdbcTemplate.queryForObject(query, tableNames, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void addCoverageProductConstraints(final Map<String, Object> coverageProductConstraintNames) {
        if (!coverageProductConstraintNames.isEmpty()) {
            StringBuilder queryBuilder = new StringBuilder();

            String coverageProductSelectedCoverageProuductFkey = (String) coverageProductConstraintNames
                    .get(L_COVERAGEPRODUCTSELECTED_COVERAGEPRODUCT_FKEY);
            String coverageProductLoggingCoverageProductFkey = (String) coverageProductConstraintNames
                    .get(L_COVERAGEPRODUCTLOGGING_COVERAGEPRODUCT_FKEY);

            if (coverageProductSelectedCoverageProuductFkey != null) {
                queryBuilder.append(String.format(
                        "ALTER TABLE ordersupplies_coverageproductselected ADD CONSTRAINT %s FOREIGN KEY (coverageproduct_id)"
                                + " REFERENCES ordersupplies_coverageproduct (id) DEFERRABLE;\n",
                        coverageProductSelectedCoverageProuductFkey));
            }
            if (coverageProductLoggingCoverageProductFkey != null) {
                queryBuilder.append(String.format(
                        "ALTER TABLE ordersupplies_coverageproductlogging ADD CONSTRAINT %s FOREIGN KEY (coverageproduct_id)"
                                + " REFERENCES ordersupplies_coverageproduct (id) DEFERRABLE\n",
                        coverageProductLoggingCoverageProductFkey));
            }

            String query = queryBuilder.toString();

            jdbcTemplate.update(query, Maps.newHashMap());
        }
    }

    private void dropCoverageProductConstraints(final Map<String, Object> coverageProductConstraintNames) {
        if (!coverageProductConstraintNames.isEmpty()) {
            StringBuilder queryBuilder = new StringBuilder();

            String coverageProductSelectedCoverageProuductFkey = (String) coverageProductConstraintNames
                    .get(L_COVERAGEPRODUCTSELECTED_COVERAGEPRODUCT_FKEY);
            String coverageProductLoggingCoverageProductFkey = (String) coverageProductConstraintNames
                    .get(L_COVERAGEPRODUCTLOGGING_COVERAGEPRODUCT_FKEY);

            if (coverageProductSelectedCoverageProuductFkey != null) {
                queryBuilder.append(String.format("ALTER TABLE ordersupplies_coverageproductselected DROP CONSTRAINT %s;\n",
                        coverageProductSelectedCoverageProuductFkey));
            }
            if (coverageProductLoggingCoverageProductFkey != null) {
                queryBuilder.append(String.format("ALTER TABLE ordersupplies_coverageproductlogging DROP CONSTRAINT %s;\n",
                        coverageProductLoggingCoverageProductFkey));
            }

            String query = queryBuilder.toString();

            jdbcTemplate.update(query, Maps.newHashMap());
        }
    }

}
