package com.qcadoo.mes.masterOrders;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterOrderDocumentService {


    public static final String L_MASTER_ORDER_RELEASE_LOCATION = "masterOrderReleaseLocation";
    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TranslationService translationService;

    public void createReleaseDocument(List<Entity> masterOrderProducts, ViewDefinitionState view) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity masterOrderFormEntity = masterOrderForm.getEntity();

        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());

        Entity masterOrderReleaseLocation = parameterService.getParameter().getBelongsToField(L_MASTER_ORDER_RELEASE_LOCATION);
        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder(user);
        documentBuilder.release(masterOrderReleaseLocation);
        documentBuilder.setField(DocumentFields.DESCRIPTION,
                translationService.translate("masterOrders.masterOrder.releaseDocument.description",
                        LocaleContextHolder.getLocale(), masterOrderFormEntity.getStringField(MasterOrderFields.NUMBER)));
        documentBuilder.setField(DocumentFields.COMPANY, masterOrderFormEntity.getBelongsToField(MasterOrderFields.COMPANY));
        documentBuilder.setField(DocumentFields.ADDRESS, masterOrderFormEntity.getBelongsToField(MasterOrderFields.ADDRESS));

        for (Entity masterOrderProduct : masterOrderProducts) {
            Entity mo = masterOrderProduct.getDataDefinition().getMasterModelEntity(masterOrderProduct.getId());
            documentBuilder.addPosition(mo.getBelongsToField(MasterOrderProductFields.PRODUCT), mo.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY));
        }

        documentBuilder.buildWithEntityRuntimeException();
        view.addMessage("masterOrders.masterOrder.releaseDocument.created", ComponentState.MessageType.SUCCESS);
    }
}
