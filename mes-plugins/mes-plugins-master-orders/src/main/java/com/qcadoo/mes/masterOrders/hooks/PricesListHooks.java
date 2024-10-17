package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.ParameterFieldsMO;
import com.qcadoo.mes.masterOrders.constants.PricesListFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PricesListHooks {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Optional<Entity> maybePreviousComponent = findPreviousPricesList(entity);
        if (maybePreviousComponent.isPresent()) {
            DateTime dateFrom = new DateTime(entity.getDateField(PricesListFields.DATE_FROM));
            Entity previousComponent = maybePreviousComponent.get();
            previousComponent.setField(PricesListFields.DATE_TO, new DateTime(dateFrom.toDate()).minusDays(1).toDate());
            Entity savedPrevious = dataDefinition.save(previousComponent);
            if (!savedPrevious.isValid()) {
                savedPrevious.getErrors().forEach((key, value) -> entity.addGlobalError(value.getMessage()));
            }
        }

        if (checkIfPricesListForGivenTimeExists(entity)) {
            entity.addError(dataDefinition.getField(PricesListFields.DATE_FROM),
                    "masterOrders.pricesList.validation.otherComponentsExist");
        }
    }

    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getDateField(PricesListFields.DATE_FROM) != null
                && entity.getDateField(PricesListFields.DATE_TO) != null) {
            entity.addGlobalError("masterOrders.pricesList.onDelete.hasDates");
            return false;
        }

        Optional<Entity> maybePreviousComponent = findPreviousPricesList(entity);
        if (maybePreviousComponent.isPresent()) {
            Entity previousComponent = maybePreviousComponent.get();
            previousComponent.setField(PricesListFields.DATE_TO, null);
            dataDefinition.fastSave(previousComponent);
        }
        return true;
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        boolean isValid = true;
        Date dateFrom = entity.getDateField(PricesListFields.DATE_FROM);
        Date dateTo = entity.getDateField(PricesListFields.DATE_TO);
        if (dateTo != null && dateTo.compareTo(dateFrom) < 0) {
            entity.addError(dataDefinition.getField(PricesListFields.DATE_FROM),
                    "masterOrders.pricesList.validation.datesInvalid");
            isValid = false;
        }
        Entity product = entity.getBelongsToField(PricesListFields.PRODUCT);
        String productCategory = entity.getStringField(PricesListFields.PRODUCT_CATEGORY);
        if (product == null && StringUtils.isEmpty(productCategory)) {
            entity.addError(dataDefinition.getField(PricesListFields.PRODUCT),
                    "masterOrders.pricesList.validation.productOrCategoryMissing");
            entity.addError(dataDefinition.getField(PricesListFields.PRODUCT_CATEGORY),
                    "masterOrders.pricesList.validation.productOrCategoryMissing");
            isValid = false;
        }
        Entity parameter = parameterService.getParameter();
        Entity priceListAttribute1 = parameter.getBelongsToField(ParameterFieldsMO.PRICE_LIST_ATTRIBUTE_1);
        Entity priceListAttribute2 = parameter.getBelongsToField(ParameterFieldsMO.PRICE_LIST_ATTRIBUTE_2);
        String value1 = entity.getStringField(PricesListFields.VALUE_1);
        if (priceListAttribute1 != null && value1 != null && AttributeDataType.CONTINUOUS.getStringValue().equals(priceListAttribute1.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(priceListAttribute1.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, com.google.common.base.Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    value1, LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                int scale = priceListAttribute1.getIntegerField(AttributeFields.PRECISION);
                int valueScale = eitherNumber.getRight().get().scale();
                if (valueScale > scale) {
                    entity.addError(dataDefinition.getField(PricesListFields.VALUE_1),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));
                    isValid = false;
                }
                entity
                        .setField(
                                PricesListFields.VALUE_1,
                                BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                        priceListAttribute1.getIntegerField(AttributeFields.PRECISION)));
            } else {
                entity.addError(dataDefinition.getField(PricesListFields.VALUE_1),
                        "qcadooView.validate.field.error.invalidNumericFormat");
                isValid = false;
            }
        }
        String value2 = entity.getStringField(PricesListFields.VALUE_2);
        if (priceListAttribute2 != null && value2 != null && AttributeDataType.CONTINUOUS.getStringValue().equals(priceListAttribute2.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(priceListAttribute2.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, com.google.common.base.Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    value2, LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                int scale = priceListAttribute2.getIntegerField(AttributeFields.PRECISION);
                int valueScale = eitherNumber.getRight().get().scale();
                if (valueScale > scale) {
                    entity.addError(dataDefinition.getField(PricesListFields.VALUE_2),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));
                    isValid = false;
                }
                entity
                        .setField(
                                PricesListFields.VALUE_2,
                                BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                        priceListAttribute2.getIntegerField(AttributeFields.PRECISION)));
            } else {
                entity.addError(dataDefinition.getField(PricesListFields.VALUE_2),
                        "qcadooView.validate.field.error.invalidNumericFormat");
                isValid = false;
            }
        }
        return isValid;
    }

    private Optional<Entity> findPreviousPricesList(final Entity pricesList) {
        Date dateFrom = pricesList.getDateField(PricesListFields.DATE_FROM);
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRICES_LIST)
                .find().addOrder(SearchOrders.desc(PricesListFields.DATE_FROM))
                .add(SearchRestrictions.lt(PricesListFields.DATE_FROM, dateFrom));

        Entity product = pricesList.getBelongsToField(PricesListFields.PRODUCT);
        if (product != null) {
            scb.add(SearchRestrictions.belongsTo(PricesListFields.PRODUCT, product));
        } else {
            String productCategory = pricesList.getStringField(PricesListFields.PRODUCT_CATEGORY);
            scb.add(SearchRestrictions.eq(PricesListFields.PRODUCT_CATEGORY, productCategory));
        }
        String value1 = pricesList.getStringField(PricesListFields.VALUE_1);
        if (value1 != null) {
            scb.add(SearchRestrictions.eq(PricesListFields.VALUE_1, value1));
        } else {
            scb.add(SearchRestrictions.isNull(PricesListFields.VALUE_1));
        }
        String value2 = pricesList.getStringField(PricesListFields.VALUE_2);
        if (value2 != null) {
            scb.add(SearchRestrictions.eq(PricesListFields.VALUE_2, value2));
        } else {
            scb.add(SearchRestrictions.isNull(PricesListFields.VALUE_2));
        }

        if (pricesList.getId() != null) {
            scb.add(SearchRestrictions.idNe(pricesList.getId()));
        }
        List<Entity> previousComponents = scb.list().getEntities();

        if (previousComponents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(previousComponents.get(0));
    }

    private boolean checkIfPricesListForGivenTimeExists(final Entity pricesList) {
        Date dateFrom = pricesList.getDateField(PricesListFields.DATE_FROM);
        Date dateTo = pricesList.getDateField(PricesListFields.DATE_TO);

        Entity product = pricesList.getBelongsToField(PricesListFields.PRODUCT);
        SearchCriterion scb;
        if (product != null) {
            scb = SearchRestrictions.belongsTo(PricesListFields.PRODUCT, product);
        } else {
            String productCategory = pricesList.getStringField(PricesListFields.PRODUCT_CATEGORY);
            scb = SearchRestrictions.eq(PricesListFields.PRODUCT_CATEGORY, productCategory);
        }

        String value1 = pricesList.getStringField(PricesListFields.VALUE_1);
        if (value1 != null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.eq(PricesListFields.VALUE_1, value1));
        } else {
            scb = SearchRestrictions.and(scb, SearchRestrictions.isNull(PricesListFields.VALUE_1));
        }
        String value2 = pricesList.getStringField(PricesListFields.VALUE_2);
        if (value2 != null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.eq(PricesListFields.VALUE_2, value2));
        } else {
            scb = SearchRestrictions.and(scb, SearchRestrictions.isNull(PricesListFields.VALUE_2));
        }

        if (dateTo == null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.or(SearchRestrictions.ge(
                    PricesListFields.DATE_FROM, dateFrom), SearchRestrictions.and(
                    SearchRestrictions.le(PricesListFields.DATE_FROM, dateFrom),
                    SearchRestrictions.gt(PricesListFields.DATE_TO, dateFrom))));
        } else {
            scb = SearchRestrictions.and(scb, SearchRestrictions.and(
                    SearchRestrictions.le(PricesListFields.DATE_FROM, dateFrom),
                    SearchRestrictions.gt(PricesListFields.DATE_TO, dateFrom)));
        }
        if (pricesList.getId() != null) {
            scb = SearchRestrictions.and(scb, SearchRestrictions.ne("id", pricesList.getId()));
        }
        long count = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_PRICES_LIST).count(scb);
        return count != 0;

    }
}
