/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.TransferFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
public class TransformationsModelValidators {

    @Autowired
    private MaterialFlowService materialFlowService;

    public boolean checkIfTransfersAreValid(final DataDefinition transformationsDD, final Entity transformations) {
        List<Entity> transfersConsumption = transformations.getHasManyField(TRANSFERS_CONSUMPTION);
        List<Entity> transfersProduction = transformations.getHasManyField(TRANSFERS_PRODUCTION);

        Iterable<Boolean> validationResults = Lists.newArrayList(areTransfersValid(transfersConsumption),
                areTransfersValid(transfersProduction),
                checkIfTransfersNumbersAreDistinct(transfersConsumption, transfersProduction),
                checkIfTransfersNumbersAreDistinct(transfersProduction, transfersConsumption));
        return Iterables.all(validationResults, Predicates.equalTo(true));
    }

    private static final Function<Entity, Long> EXTRACT_TRANSFER_PRODUCT_ID = Functions.compose(EntityUtils.getIdExtractor(),
            EntityUtils.getBelongsToFieldExtractor(TransferFields.PRODUCT));

    private boolean areTransfersValid(final List<Entity> transfers) {
        final Iterable<Long> usedProductIds = Iterables.transform(transfers, EXTRACT_TRANSFER_PRODUCT_ID);
        return FluentIterable.from(transfers).filter(Predicates.notNull()).allMatch(new Predicate<Entity>() {

            @Override
            public boolean apply(final Entity transfer) {
                return transferIsValid(usedProductIds, transfer);
            }
        });
    }

    private boolean transferIsValid(final Iterable<Long> usedProductIds, final Entity transfer) {
        boolean isValid = true;
        isValid = transferQuantityIsNonEmptyDecimalValue(transfer) && isValid;
        isValid = transferNumberIsNotEmpty(transfer) && isValid;
        isValid = transferProductIsNotNullAndUnique(usedProductIds, transfer) && isValid;
        return isValid;
    }

    private boolean transferNumberIsNotEmpty(final Entity transfer) {
        String number = transfer.getStringField(NUMBER);
        if (StringUtils.isEmpty(number)) {
            appendErrorToModelField(transfer, NUMBER, "materialFlow.validate.global.error.fillRequired");
            return false;
        }
        return true;
    }

    private boolean transferProductIsNotNullAndUnique(final Iterable<Long> usedProductIds, final Entity transfer) {
        Entity product = transfer.getBelongsToField(PRODUCT);
        if (product == null) {
            appendErrorToModelField(transfer, PRODUCT, "materialFlow.validate.global.error.fillRequired");
            return false;
        } else {
            if (FluentIterable.from(usedProductIds).filter(Predicates.equalTo(product.getId())).limit(2).size() > 1) {
                appendErrorToModelField(transfer, PRODUCT, "materialFlow.validate.global.error.productAlreadyAdded");
                return false;
            }
        }
        return true;
    }

    private boolean transferQuantityIsNonEmptyDecimalValue(final Entity transfer) {
        Either<Exception, Optional<BigDecimal>> quantity = BigDecimalUtils.tryParse(
                ObjectUtils.toString(transfer.getField(TransferFields.QUANTITY)), LocaleContextHolder.getLocale());
        if (quantity.isLeft()) {
            appendErrorToModelField(transfer, QUANTITY, "qcadooView.validate.field.error.invalidNumericFormat");
            return false;
        }
        if (!quantity.getRight().isPresent()) {
            appendErrorToModelField(transfer, QUANTITY, "materialFlow.validate.global.error.fillRequired");
            return false;
        }
        return true;
    }

    private boolean checkIfTransfersNumbersAreDistinct(final List<Entity> transfersConsumption,
            final List<Entity> transfersProduction) {
        boolean isValid = true;

        if (transfersConsumption != null) {
            for (Entity transfer : transfersConsumption) {
                if (transfer.getId() == null) {
                    String number = transfer.getStringField(NUMBER);

                    if ((number != null)
                            && (((isNumberAlreadyUsed(transfersConsumption, number) + isNumberAlreadyUsed(transfersProduction,
                                    number)) > 1) || materialFlowService.numberAlreadyExist(MaterialFlowConstants.MODEL_TRANSFER,
                                    number))) {
                        appendErrorToModelField(transfer, NUMBER, "materialFlow.validate.global.error.numberAlreadyUsed");

                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }

    private int isNumberAlreadyUsed(final List<Entity> transfers, final String number) {
        if ((transfers != null) && (number != null)) {
            int count = 0;

            for (Entity transfer : transfers) {
                String numberAlreadyUsed = transfer.getStringField(NUMBER);

                if (number.equals(numberAlreadyUsed)) {
                    count++;

                    if (count > 0) {
                        return count;
                    }
                }
            }
        }

        return 0;
    }

    private void appendErrorToModelField(final Entity entity, final String fieldName, final String messageKey) {
        FieldDefinition productInFieldDef = entity.getDataDefinition().getField(fieldName);
        entity.addError(productInFieldDef, messageKey);
    }

    public boolean checkIfLocationFromOrLocationToHasExternalNumber(final DataDefinition transformationDD,
            final Entity transformation) {
        boolean isValid = true;

        if (materialFlowService.checkIfLocationHasExternalNumber(transformation.getBelongsToField(LOCATION_FROM))) {
            transformation.addError(transformationDD.getField(LOCATION_FROM),
                    "materialFlow.validate.global.error.locationHasExternalNumber");

            isValid = false;
        }

        if (materialFlowService.checkIfLocationHasExternalNumber(transformation.getBelongsToField(LOCATION_TO))) {
            transformation.addError(transformationDD.getField(LOCATION_TO),
                    "materialFlow.validate.global.error.locationHasExternalNumber");

            isValid = false;
        }

        return isValid;
    }

}
