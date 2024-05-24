/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.masterOrders.imports.masterOrder;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.qcadoo.mes.basic.imports.dtos.CellBinder;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.dtos.ImportStatus;
import com.qcadoo.mes.basic.imports.helpers.RowProcessorHelper;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.hooks.MasterOrderPositionStatus;
import com.qcadoo.mes.masterOrders.listeners.MasterOrdersImportListeners;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.constants.VersionableConstants;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class MasterOrderXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_MASTER_ORDERS_MASTER_ORDERS_IMPORT_VALIDATE_ERROR_NOT_SAME = "masterOrders.masterOrdersImport.validate.error.notSame";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Override
    protected Entity getEntityToSkip(String pluginIdentifier, String modelName,
                                  Function<Entity, SearchCriterion> criteriaSupplier, Entity entity) {
        Entity masterOrderFromDB = getMasterOrderFromDB(entity);
        Entity product = entity.getBelongsToField(MasterOrderFields.PRODUCT);
        if (Objects.nonNull(masterOrderFromDB)) {
            return getMasterOrderProductDD().find().add(SearchRestrictions.belongsTo(MasterOrderProductFields.PRODUCT, product))
                    .add(SearchRestrictions.belongsTo(MasterOrderProductFields.MASTER_ORDER, masterOrderFromDB)).setMaxResults(1).uniqueResult();
        } else {
            return null;
        }
    }

    @Override
    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        Entity masterOrder = getDataDefinition(pluginIdentifier, modelName).create();

        setRequiredFields(masterOrder);

        return masterOrder;
    }

    private void setRequiredFields(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
        masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.NEW.getStringValue());
    }

    @Override
    public void validateEntity(final Entity masterOrder, final DataDefinition masterOrderDD) {
        Entity masterOrderFromDB = getMasterOrderFromDB(masterOrder);

        validateMasterOrder(masterOrder, masterOrderFromDB, masterOrderDD);
        validateDates(masterOrder, masterOrderDD);
        validateTechnology(masterOrder, masterOrderDD);
        validateMasterOrderPositionStatus(masterOrder, masterOrderDD);
        validateMasterOrderProduct(masterOrder, masterOrderFromDB, masterOrderDD);
    }

    private Entity getMasterOrderFromDB(final Entity masterOrder) {
        return getEntity(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER,
                MasterOrdersImportListeners.createRestrictionForMasterOrder(masterOrder));
    }

    private void validateMasterOrder(final Entity masterOrder, final Entity masterOrderFromDB,
                                     final DataDefinition masterOrderDD) {
        if (Objects.nonNull(masterOrderFromDB)) {
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.NUMBER);
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.NAME);
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.DESCRIPTION);
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.COMPANY);
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.START_DATE);
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.FINISH_DATE);
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.DEADLINE);
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.DATE_OF_RECEIPT);
            checkIfFieldsAreSame(masterOrder, masterOrderFromDB, masterOrderDD, MasterOrderFields.MASTER_ORDER_STATE);
        }
    }

    private void checkIfFieldsAreSame(final Entity masterOrder, final Entity masterOrderFromDB,
                                      final DataDefinition masterOrderDD, final String fieldName) {
        Object fieldValue = masterOrder.getField(fieldName);
        Object fieldValueFromDB = masterOrderFromDB.getField(fieldName);

        boolean areSame = (Objects.isNull(fieldValue) ? Objects.isNull(fieldValueFromDB) : fieldValue.equals(fieldValueFromDB));

        if (!areSame) {
            masterOrder.addGlobalError(L_MASTER_ORDERS_MASTER_ORDERS_IMPORT_VALIDATE_ERROR_NOT_SAME);

            masterOrder.addError(masterOrderDD.getField(fieldName), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }
    }

    private void validateDates(final Entity masterOrder, final DataDefinition masterOrderDD) {
        Date startDate = masterOrder.getDateField(MasterOrderFields.START_DATE);
        Date finishDate = masterOrder.getDateField(MasterOrderFields.FINISH_DATE);

        if (Objects.nonNull(startDate) && Objects.nonNull(finishDate)) {
            if (startDate.after(finishDate) || startDate.equals(finishDate)) {
                masterOrder.addError(masterOrderDD.getField(MasterOrderFields.FINISH_DATE),
                        L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
            }
        }
    }

    private void validateTechnology(final Entity masterOrder, final DataDefinition masterOrderDD) {
        Entity technology = masterOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY);
        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);

        if (Objects.nonNull(product)) {
            if (Objects.isNull(technology)) {
                technology = technologyServiceO.getDefaultTechnology(product);

                masterOrder.setField(MasterOrderFields.TECHNOLOGY, technology);
            } else {
                String technologyState = technology.getStringField(TechnologyFields.STATE);
                Entity technologyProduct = technology.getBelongsToField(TechnologyFields.PRODUCT);

                if (!TechnologyStateStringValues.ACCEPTED.equals(technologyState)
                        || !technologyProduct.getId().equals(product.getId())) {
                    masterOrder.addError(masterOrderDD.getField(MasterOrderFields.TECHNOLOGY),
                            L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
                }
            }
        }
    }

    private void validateMasterOrderPositionStatus(final Entity masterOrder, final DataDefinition masterOrderDD) {
        String masterOrderPositionStatus = masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_POSITION_STATUS);

        if (Objects.isNull(masterOrderPositionStatus)) {
            masterOrder.setField(MasterOrderFields.MASTER_ORDER_POSITION_STATUS, MasterOrderPositionStatus.NEW.getText());
        }
    }

    private void validateMasterOrderProduct(final Entity masterOrder, final Entity masterOrderFromDB,
                                            final DataDefinition masterOrderDD) {
        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);
        BigDecimal masterOrderQuantity = masterOrder.getDecimalField(MasterOrderFields.MASTER_ORDER_QUANTITY);
        Entity technology = masterOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY);
        String comments = masterOrder.getStringField(MasterOrderFields.COMMENTS);
        String masterOrderPositionStatus = masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_POSITION_STATUS);

        if (Objects.nonNull(product) && Objects.nonNull(masterOrderQuantity)) {
            List<Entity> masterOrderProducts = Lists.newArrayList();

            if (Objects.nonNull(masterOrderFromDB)) {
                setIdAndVersion(masterOrder, masterOrderFromDB);

                masterOrderProducts.addAll(masterOrderFromDB.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS));
            }

            Entity masterOrderProduct = createMasterOrderProduct(product, masterOrderQuantity, technology, comments,
                    masterOrderPositionStatus);

            masterOrderProducts.add(masterOrderProduct);

            masterOrder.setField(MasterOrderFields.MASTER_ORDER_PRODUCTS, masterOrderProducts);
        }
    }

    private void setIdAndVersion(final Entity masterOrder, final Entity masterOrderFromDB) {
        masterOrder.setId(masterOrderFromDB.getId());
        masterOrder.setField(VersionableConstants.VERSION_FIELD_NAME,
                masterOrderFromDB.getLongField(VersionableConstants.VERSION_FIELD_NAME));
    }

    private Entity createMasterOrderProduct(final Entity product, final BigDecimal masterOrderQuantity,
                                            final Entity technology,
                                            final String comments, final String masterOrderPositionStatus) {
        Entity masterOrderProduct = getMasterOrderProductDD().create();

        masterOrderProduct.setField(MasterOrderProductFields.PRODUCT, product);
        masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER_QUANTITY, masterOrderQuantity);
        masterOrderProduct.setField(MasterOrderProductFields.TECHNOLOGY, technology);
        masterOrderProduct.setField(MasterOrderProductFields.COMMENTS, comments);
        masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS, masterOrderPositionStatus);

        return masterOrderProduct;
    }

    private DataDefinition getMasterOrderProductDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT);
    }

}
