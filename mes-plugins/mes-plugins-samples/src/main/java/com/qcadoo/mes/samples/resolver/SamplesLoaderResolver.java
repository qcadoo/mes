package com.qcadoo.mes.samples.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.samples.api.SamplesLoader;
import com.qcadoo.mes.samples.loader.DummySamplesLoader;
import com.qcadoo.mes.samples.loader.GeneratedSamplesLoader;
import com.qcadoo.mes.samples.loader.MinimalSamplesLoader;
import com.qcadoo.mes.samples.loader.TestSamplesLoader;
import com.qcadoo.tenant.api.MultiTenantService;
import com.qcadoo.tenant.api.SamplesDataset;

/**
 * This service select appropriate SamplesLoader using {@link SamplesDataset} returned by
 * {@link MultiTenantService#getTenantSamplesDataset()}
 * 
 * @since 1.1.6
 */
@Service
public class SamplesLoaderResolver {

    @Autowired
    private MinimalSamplesLoader minimalSamplesLoader;

    @Autowired
    private GeneratedSamplesLoader generatedSamplesLoader;

    @Autowired
    private TestSamplesLoader testSamplesLoader;

    @Autowired
    private DummySamplesLoader dummySamplesLoader;

    @Autowired
    private MultiTenantService multiTenantService;

    /**
     * Return appropriate SamplesLoader using {@link SamplesDataset} returned by
     * {@link MultiTenantService#getTenantSamplesDataset()}
     * 
     * @return SamplesLoader implementation for specified {@link SamplesDataset}
     * 
     * @since 1.1.6
     */
    public SamplesLoader resolve() {
        final SamplesDataset samplesDataset = multiTenantService.getTenantSamplesDataset();
        switch (samplesDataset) {
            case MINIMAL:
                return minimalSamplesLoader;
            case TEST:
                return testSamplesLoader;
            case GENERATED:
                return generatedSamplesLoader;
            case NONE:
                return dummySamplesLoader;
            default:
                throw new IllegalArgumentException("Unsupported dataset: " + samplesDataset);
        }
    }

}
