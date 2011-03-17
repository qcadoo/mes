package com.qcadoo.model.internal.api;

import org.springframework.core.io.Resource;

public interface ModelXmlToHbmConverter {

    Resource[] convert(Resource... resources);

}
