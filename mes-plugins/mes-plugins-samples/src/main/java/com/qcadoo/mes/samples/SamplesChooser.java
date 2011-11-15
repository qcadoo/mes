package com.qcadoo.mes.samples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.qcadoo.plugin.api.Module;

@Component
public class SamplesChooser extends Module {

    @Autowired
    private SamplesGeneratorModule samplesGeneratorModule;

    @Autowired
    private SamplesLoaderModule samplesLoaderModule;

    @Autowired
    private SamplesMinimalDataset samplesMinimalDataset;

    @Value("${samplesBuildStrategy}")
    private String samplesBuildStrategy;

    @Override
    public void multiTenantEnable() {
        if ("LOADER".equals(samplesBuildStrategy.toUpperCase())) {
            samplesLoaderModule.multiTenantEnable();
        } else if ("GENERATOR".equals(samplesBuildStrategy.toUpperCase())) {
            samplesGeneratorModule.multiTenantEnable();
        } else if ("MINIMAL".equals(samplesBuildStrategy.toUpperCase())) {
            samplesMinimalDataset.multiTenantEnable();
        } else {
            throw new IllegalStateException("samples build strategy must be declared!");
        }
    }
}
