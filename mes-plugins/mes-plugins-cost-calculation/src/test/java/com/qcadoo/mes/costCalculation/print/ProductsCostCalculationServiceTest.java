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
package com.qcadoo.mes.costCalculation.print;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostFields;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.testing.model.NumberServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubDecimalField;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ProductsCostCalculationServiceTest {

    private ProductsCostCalculationService productsCostCalculationService;

    @Mock
    private CurrencyService currencyService;

    private String currency = "PLN";

    @Before
    public void init() {
        productsCostCalculationService = new ProductsCostCalculationServiceImpl();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(productsCostCalculationService, "numberService", NumberServiceMock.scaleAware());
        ReflectionTestUtils.setField(productsCostCalculationService, "currencyService", currencyService);

    }

    private Entity mockProduct(final BigDecimal averageCost,
                               final BigDecimal forQuantity) {
        Entity product = mockEntity(1L);
        stubDecimalField(product, ProductFieldsCNFP.AVERAGE_COST, averageCost);
        stubDecimalField(product, ProductFieldsCNFP.NOMINAL_COST, null);
        stubDecimalField(product, ProductFieldsCNFP.COST_FOR_NUMBER, forQuantity);
        return product;
    }

    @Test
    public void shouldCalculateProductCostUsingAppropriateCostField() throws Exception {
        for (ProductsCostFields productsCostFields : ProductsCostFields.values()) {
            performCalculationUsingGivenCostsType(productsCostFields);
        }
    }

    private void performCalculationUsingGivenCostsType(final ProductsCostFields costFields) {
        // given
        BigDecimal costForNumber = BigDecimal.valueOf(5L);

        Entity product = mockProduct(null, costForNumber);
        for (ProductsCostFields productsCostFields : ProductsCostFields.values()) {
            stubDecimalField(product, productsCostFields.getStrValue(), BigDecimal.valueOf(9999L));
        }
        stubDecimalField(product, costFields.getStrValue(), BigDecimal.valueOf(5L));

        when(currencyService.getCurrencyAlphabeticCode()).thenReturn(currency);

        // when
        BigDecimal result = productsCostCalculationService.calculateProductCostPerUnit(product,
                costFields.getMode(), false, null);

        // then
        assertTrue(BigDecimalUtils.valueEquals(result, BigDecimal.valueOf(1L)));
    }

    @Test
    public void shouldCalculateProductCostCopeWithZeroInCostForNumber() throws Exception {
        // given
        BigDecimal costForNumber = BigDecimal.ZERO.setScale(30);
        BigDecimal averageCost = BigDecimal.TEN;

        String materialCostsUsed = "02average";

        Entity product = mockProduct(averageCost, costForNumber);

        when(currencyService.getCurrencyAlphabeticCode()).thenReturn(currency);

        // when
        BigDecimal result = productsCostCalculationService.calculateProductCostPerUnit(product,
                materialCostsUsed, false, null);

        // then
        assertTrue(BigDecimalUtils.valueEquals(result, BigDecimal.valueOf(10L)));
    }
}
