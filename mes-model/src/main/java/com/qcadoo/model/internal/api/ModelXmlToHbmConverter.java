package com.qcadoo.model.internal.api;

import java.io.InputStream;
import java.util.Collection;

import org.springframework.core.io.Resource;

public interface ModelXmlToHbmConverter {

    Collection<InputStream> convert(Resource... resources);

}
