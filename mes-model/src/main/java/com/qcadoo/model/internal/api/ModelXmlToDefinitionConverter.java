package com.qcadoo.model.internal.api;

import java.util.Collection;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;

import com.qcadoo.model.api.DataDefinition;

public interface ModelXmlToDefinitionConverter {

    void onApplicationEvent(ContextRefreshedEvent event);

    Collection<DataDefinition> convert(Resource... resources);

}
