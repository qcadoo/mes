package com.qcadoo.model.internal.api;

import java.util.Collection;

import org.springframework.core.io.Resource;

public interface ModelXmlToClassConverter {

    Collection<Class<?>> convert(Resource... resources);

}
