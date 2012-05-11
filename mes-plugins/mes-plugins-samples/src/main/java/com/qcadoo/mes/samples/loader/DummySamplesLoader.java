package com.qcadoo.mes.samples.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.samples.api.SamplesLoader;

@Service
public class DummySamplesLoader implements SamplesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DummySamplesLoader.class);

    @Override
    public void load() {
        LOG.debug("Database won't be changed ... ");
    }

}
