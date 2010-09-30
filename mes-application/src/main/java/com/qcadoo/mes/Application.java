package com.qcadoo.mes;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.model.internal.DataDefinitionParser;
import com.qcadoo.mes.view.internal.ViewDefinitionParser;

@Service
public class Application {

    @Autowired
    private DataDefinitionParser dataDefinitionParser;

    private ViewDefinitionParser viewDefinitionParser;

    @PostConstruct
    public void init() {
        dataDefinitionParser.parse();
        viewDefinitionParser.parse();
    }

}
