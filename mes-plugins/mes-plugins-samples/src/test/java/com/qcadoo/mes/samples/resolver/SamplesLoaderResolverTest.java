/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.samples.resolver;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.qcadoo.mes.samples.api.SamplesLoader;
import com.qcadoo.mes.samples.loader.DummySamplesLoader;
import com.qcadoo.mes.samples.loader.GeneratedSamplesLoader;
import com.qcadoo.mes.samples.loader.MinimalSamplesLoader;
import com.qcadoo.mes.samples.loader.TestSamplesLoader;
import com.qcadoo.tenant.api.MultiTenantService;
import com.qcadoo.tenant.api.SamplesDataset;

public class SamplesLoaderResolverTest {

    private SamplesLoaderResolver samplesLoaderResolver;

    @Mock
    private MinimalSamplesLoader minimalSamplesLoader;

    @Mock
    private TestSamplesLoader testSamplesLoader;

    @Mock
    private GeneratedSamplesLoader generatedSamplesLoader;

    @Mock
    private DummySamplesLoader dummySamplesLoader;

    @Mock
    private MultiTenantService multiTenantService;

    @Before
    public final void init() {
        samplesLoaderResolver = new SamplesLoaderResolver();
        initMocks(this);
        setField(samplesLoaderResolver, "minimalSamplesLoader", minimalSamplesLoader);
        setField(samplesLoaderResolver, "testSamplesLoader", testSamplesLoader);
        setField(samplesLoaderResolver, "generatedSamplesLoader", generatedSamplesLoader);
        setField(samplesLoaderResolver, "dummySamplesLoader", dummySamplesLoader);
        setField(samplesLoaderResolver, "multiTenantService", multiTenantService);
    }

    @Test
    public final void shouldReturnMinimalSamplesLoader() throws Exception {
        // given
        given(multiTenantService.getTenantSamplesDataset()).willReturn(SamplesDataset.MINIMAL);

        // when
        SamplesLoader samplesLoader = samplesLoaderResolver.resolve();

        // then
        assertEquals(minimalSamplesLoader, samplesLoader);
    }

    @Test
    public final void shouldReturnTestSamplesLoader() throws Exception {
        // given
        given(multiTenantService.getTenantSamplesDataset()).willReturn(SamplesDataset.TEST);

        // when
        SamplesLoader samplesLoader = samplesLoaderResolver.resolve();

        // then
        assertEquals(testSamplesLoader, samplesLoader);
    }

    @Test
    public final void shouldReturnGeneratedSamplesLoader() throws Exception {
        // given
        given(multiTenantService.getTenantSamplesDataset()).willReturn(SamplesDataset.GENERATED);

        // when
        SamplesLoader samplesLoader = samplesLoaderResolver.resolve();

        // then
        assertEquals(generatedSamplesLoader, samplesLoader);
    }

    @Test
    public final void shouldReturnDummySamplesLoader() throws Exception {
        // given
        given(multiTenantService.getTenantSamplesDataset()).willReturn(SamplesDataset.NONE);

        // when
        SamplesLoader samplesLoader = samplesLoaderResolver.resolve();

        // then
        assertEquals(dummySamplesLoader, samplesLoader);
    }
}
