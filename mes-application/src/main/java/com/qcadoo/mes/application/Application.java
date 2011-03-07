package com.qcadoo.mes.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.internal.MenuService;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.mes.view.xml.ViewDefinitionParserImpl;
import com.qcadoo.model.internal.api.ModelXmlToDefinitionConverter;
import com.qcadoo.model.internal.definitionconverter.ModelXmlToDefinitionConverterImpl;

@Component
public class Application implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Autowired
    private ModelXmlToDefinitionConverter modelXmlToDefinitionConverter;

    @Autowired
    private MenuService menuService;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        // TODO
        ((ModelXmlToDefinitionConverterImpl) modelXmlToDefinitionConverter).onApplicationEvent(event);
        ((ViewDefinitionParserImpl) viewDefinitionParser).onApplicationEvent(event);
        menuService.onApplicationEvent(event);

    }

}
