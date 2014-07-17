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
package com.qcadoo.mes.basic;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ExchangeRatesNbpServiceImplTest {

    private ExchangeRatesNbpServiceImpl service;
    private InputStream inputStream;

    @Before
    public void setUp() throws Exception {
        service = new ExchangeRatesNbpServiceImpl();
        inputStream = getClass().getResourceAsStream("/xml/NBPExchangeRatesExample.xml");
    }

    @Test
    public void shouldParseCorrectlyGivenExampleDocument() throws Exception {
        Map<String, BigDecimal> map = service.parse(inputStream, ExchangeRatesNbpService.NbpProperties.LAST_C);
        assertTrue(map.containsKey("USD"));
        assertTrue(map.containsKey("CAD"));
        assertTrue(map.containsKey("AUD"));
        assertTrue(map.containsValue(new BigDecimal("3.0756")));
        assertTrue(map.containsValue(new BigDecimal("2.8927")));
        assertTrue(map.containsValue(new BigDecimal("2.8732")));
    }

    @Test
    public void shouldParseAlwaysCloseInputStream() throws Exception {
        InputStream inputStreamSpied = spy(inputStream);
        service.parse(inputStreamSpied, ExchangeRatesNbpService.NbpProperties.LAST_C);
        verify(inputStreamSpied , atLeastOnce()).close();
    }

}