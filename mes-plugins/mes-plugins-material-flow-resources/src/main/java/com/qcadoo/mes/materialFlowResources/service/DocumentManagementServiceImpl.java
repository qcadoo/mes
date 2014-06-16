package com.qcadoo.mes.materialFlowResources.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class DocumentManagementServiceImpl implements DocumentManagementService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private UserService userService;

    @Override
    public DocumentBuilder getDocumentBuilder() {
        return new DocumentBuilder(dataDefinitionService, resourceManagementService, userService, numberGeneratorService);
    }
}
