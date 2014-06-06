package com.qcadoo.mes.materialFlowDocuments.service;

import com.qcadoo.model.api.Entity;

public interface ResourceManagementService {

    void createResourcesForReceiptDocuments(final Entity document);

    void updateResourcesForReleaseDocuments(final Entity document);

    void moveResourcesForTransferDocument(Entity document);
}
