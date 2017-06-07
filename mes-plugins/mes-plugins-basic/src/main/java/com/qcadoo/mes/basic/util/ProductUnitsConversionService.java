package com.qcadoo.mes.basic.util;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;

/**
 * Helper class to avoid boilerplate code when converting between units for a particular product
 */
@Service
public final class ProductUnitsConversionService {

    private final UnitConversionService unitConversionService;

    @Autowired
    public ProductUnitsConversionService(UnitConversionService unitConversionService) {
        this.unitConversionService = requireNonNull(unitConversionService);
    }

    public ProductSearchCriteriaHolder forProduct(Entity product) {
        return new ProductSearchCriteriaHolder(searchCriteriaBuilder -> searchCriteriaBuilder
                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)), unitConversionService);
    }

    public interface ConversionResultsHolder {

        Optional<BigDecimal> ratio();

        Optional<BigDecimal> convertValue(BigDecimal valueToConvert);

    }

    public static class ProductSearchCriteriaHolder {

        private final CustomRestriction productRestriction;

        private final UnitConversionService unitConversionService;

        ProductSearchCriteriaHolder(CustomRestriction productRestriction, UnitConversionService unitConversionService) {
            this.productRestriction = requireNonNull(productRestriction);
            this.unitConversionService = requireNonNull(unitConversionService);
        }

        public ToUnitConverter from(String unit) {
            Assert.state(StringUtils.isNotBlank(unit), "Convert-From unit is blank");
            return new ToUnitConverter(() -> unitConversionService.getPossibleConversions(unit, productRestriction), unit);
        }

    }

    public static class ToUnitConverter {

        private final Supplier<PossibleUnitConversions> possibleUnitConversionsSupplier;

        private final String fromUnit;

        ToUnitConverter(Supplier<PossibleUnitConversions> possibleUnitConversionsSupplier, String fromUnit) {
            this.possibleUnitConversionsSupplier = Suppliers.memoize(requireNonNull(possibleUnitConversionsSupplier));
            this.fromUnit = fromUnit;
        }

        public ConversionResultsHolder to(String unit) {
            Assert.state(StringUtils.isNotBlank(unit), "Convert-To unit is blank");

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
