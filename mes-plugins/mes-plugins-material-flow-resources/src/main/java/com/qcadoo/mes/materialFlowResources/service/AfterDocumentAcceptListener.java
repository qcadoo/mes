package com.qcadoo.mes.materialFlowResources.service;

import com.qcadoo.model.api.Entity;

public interface AfterDocumentAcceptListener {

    public void run(Entity document);
}
