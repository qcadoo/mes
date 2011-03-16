package com.qcadoo.mes.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.internal.MenuService;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.mes.view.xml.ViewDefinitionParserImpl;

@Component
public class Application implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Autowired
    private MenuService menuService;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        // TODO after implementing plugins - move it to proper place
        ((ViewDefinitionParserImpl) viewDefinitionParser).onApplicationEvent(event);
        menuService.onApplicationEvent(event);

    }

}
