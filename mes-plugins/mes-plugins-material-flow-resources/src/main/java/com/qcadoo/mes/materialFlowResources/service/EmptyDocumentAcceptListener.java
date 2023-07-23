package com.qcadoo.mes.materialFlowResources.service;

import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class EmptyDocumentAcceptListener implements AfterDocumentAcceptListener {

    public void run(Entity document) {

    }
}
