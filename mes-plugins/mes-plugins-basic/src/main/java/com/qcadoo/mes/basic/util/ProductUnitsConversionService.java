package com.qcadoo.mes.basic.util;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;

/**
 * Helper class to avoid boilerplate code when converting between units for a particular product.
 */
@Service
public final class ProductUnitsConversionService {

    private final UnitConversionService unitConversionService;

    @Autowired
    public ProductUnitsConversionService(UnitConversionService unitConversionService) {
        this.unitConversionService = requireNonNull(unitConversionService);
    }

    /**
     * @param product
     *            A product that will be used in conversion operations
     * @return Unit conversion chain helper class that allows to specify target unit. Returned object can be reused.
     * @throws IllegalArgumentException
     *             If argument is not a product
     */
    public FromUnitConverter forProduct(Entity product) {
        DataDefinition dataDefinition = product.getDataDefinition();
        Assert.isTrue(BasicConstants.PLUGIN_IDENTIFIER.equals(dataDefinition.getPluginIdentifier()));
        Assert.isTrue(BasicConstants.MODEL_PRODUCT.equals(dataDefinition.getName()));
        return new FromUnitConverter(product, unitConversionService);
    }

    public interface ConversionResultsHolder {

        /**
         * @return The optional containing conversion ratio between source and target units or an empty optional in case there is
         *         no such defined conversion for a product.
         */
        Optional<BigDecimal> ratio();

        /**
         * @param valueToConvert
         *            The value in source units.
         * @return The calculated value in target units obtained by multiplying valueToConvert and ratio.
         */
        Optional<BigDecimal> convertValue(BigDecimal valueToConvert);

    }

    public static class FromUnitConverter {

        private final Entity product;

        private final CustomRestriction productRestriction;

        private final UnitConversionService unitConversionService;

        private FromUnitConverter(Entity product, UnitConversionService unitConversionService) {
            this.product = requireNonNull(product);
            this.unitConversionService = requireNonNull(unitConversionService);
            this.productRestriction = searchCriteriaBuilder -> searchCriteriaBuilder
                    .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product));
        }

        /**
         * Use this method if you want to specify conversion source unit by yourself.
         * 
         * @param unit
         *            The unit that should be used as a source unit.
         * @return Unit conversion chain helper class that allows to specify target unit. Returned object can be reused.
         * @throws IllegalStateException
         *             When unit is null or blank string.
         */
        public ToUnitConverter from(String unit) {
            Assert.state(isNotBlank(unit), "Convert-From unit is blank");
            return new ToUnitConverter(() -> unitConversionService.getPossibleConversions(unit, productRestriction), unit);
        }

        /**
         * Use this method if you are willing to make unit conversions based on product's primary unit.
         * 
         * @return Unit conversion chain helper class that allows to specify target unit. Returned object can be reused.
         */
        public ToUnitConverter fromPrimaryUnit() {
            return from(product.getStringField(ProductFields.UNIT));
        }

    }

    public static class ToUnitConverter {

        private final Supplier<PossibleUnitConversions> possibleUnitConversionsSupplier;

        private final String fromUnit;

        private ToUnitConverter(Supplier<PossibleUnitConversions> possibleUnitConversionsSupplier, String fromUnit) {
            // Memoization technique used to prevent unnecessary database requests. Only one (lazy) request will be issued.
            this.possibleUnitConversionsSupplier = Suppliers.memoize(requireNonNull(possibleUnitConversionsSupplier));
            this.fromUnit = fromUnit;
        }

        /**
         * This method allows to specify target unit to be used in conversion operation
         * 
         * @param unit
         *            Target unit to use in conversion
         * @return Unit conversion chain helper class that allows to perform conversion operations. Returned object can be reused.
         * @throws IllegalStateException
         *             When unit is null or blank string.
         */
        public ConversionResultsHolder to(String unit) {
            Assert.state(isNotBlank(unit), "Convert-To unit is blank");

            class SameUnitsConverter implements ConversionResultsHolder {

                @Override
                public Optional<BigDecimal> ratio() {
                    return Optional.of(BigDecimal.ONE);
                }

                @Override
                public Optional<BigDecimal> convertValue(BigDecimal valueToConvert) {
                    Assert.notNull(valueToConvert);
                    return Optional.of(valueToConvert);
                }
            }

            class DifferentUnitsConverter implements ConversionResultsHolder {

                @Override
                public Optional<BigDecimal> ratio() {
                    return Optional.of(possibleUnitConversionsSupplier.get().asUnitToConversionMap().get(unit));
                }

                @Override
                public Optional<BigDecimal> convertValue(BigDecimal valueToConvert) {
                    Assert.notNull(valueToConvert);
                    return Optional.of(possibleUnitConversionsSupplier.get().convertTo(valueToConvert, unit));
                }
            }

            class EmptyUnitsConverter implements ConversionResultsHolder {

                @Override
                public Optional<BigDecimal> ratio() {
                    return Optional.empty();
                }

                @Override
                public Optional<BigDecimal> convertValue(BigDecimal valueToConvert) {
                    Assert.notNull(valueToConvert);
                    return Optional.empty();
                }
            }

            if (fromUnit.equals(unit)) {
                return new SameUnitsConverter();
            } else if (possibleUnitConversionsSupplier.get().isDefinedFor(unit)) {
                return new DifferentUnitsConverter();
            } else {
                return new EmptyUnitsConverter();
            }
        }

    }

}
