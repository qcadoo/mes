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
package com.qcadoo.mes.technologies;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

public class TechnologyNameAndNumberGeneratorTest {

    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Mock
    private TranslationService translationService;

    @Mock
    private NumberGeneratorService numberGeneratorService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyNameAndNumberGenerator = new TechnologyNameAndNumberGenerator();
        ReflectionTestUtils.setField(technologyNameAndNumberGenerator, "translationService", translationService);
        ReflectionTestUtils.setField(technologyNameAndNumberGenerator, "numberGeneratorService", numberGeneratorService);
    }

    @Test
    public final void shouldGenerateNumber() {
        // given
        Entity product = mockEntity();
        stubStringField(product, ProductFields.NUMBER, "SomeProductNumber");

        // when
        technologyNameAndNumberGenerator.generateNumber(product);

        // then
        verify(numberGeneratorService).generateNumberWithPrefix(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY, 3, "SomeProductNumber-");
    }

    @Test
    public final void shouldGenerateName() {
        // given
        String productNumber = "SomeProductNumber";
        String productName = "Some product name";

        Entity product = mockEntity();
        stubStringField(product, ProductFields.NUMBER, productNumber);
        stubStringField(product, ProductFields.NAME, productName);

        // when
        technologyNameAndNumberGenerator.generateName(product);

        // then
        LocalDate date = LocalDate.now();
        String expectedThirdArgument = String.format("%s.%s", date.getYear(), date.getMonthValue());
        verify(translationService).translate("technologies.operation.name.default", LocaleContextHolder.getLocale(), productName,
                productNumber, expectedThirdArgument);
    }

}
