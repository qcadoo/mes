package com.qcadoo.mes.materialFlow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MaterialFlowTransferServiceImpl implements MaterialFlowTransferService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private MaterialFlowResourceService materialFlowResourceService;

    @Override
    public void createTransfer(final String type, final Entity stockAreasFrom, final Entity stockAreasTo, final Entity product,
            final BigDecimal quantity, final Entity staff, final Date time) {
        DataDefinition dd = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);

        Entity transfer = dd.create();
        String number = materialFlowService.generateNumberFromProduct(product, MaterialFlowConstants.MODEL_TRANSFER);

        transfer.setField(NUMBER, number);
        transfer.setField(TYPE, type);
        transfer.setField(STOCK_AREAS_FROM, stockAreasFrom);
        transfer.setField(STOCK_AREAS_TO, stockAreasTo);
        transfer.setField(PRODUCT, product);
        transfer.setField(QUANTITY, quantity);
        transfer.setField(STAFF, staff);
        transfer.setField(TIME, time);

        checkArgument(dd.save(transfer).isValid(), "invalid transfer id =" + transfer.getId());
    }

    @Override
    public List<Entity> getTransferTemplates(final Entity stockAreaFrom, final Entity stockAreaTo) {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER_TEMPLATE)
                .find().add(SearchRestrictions.belongsTo(STOCK_AREAS_FROM, stockAreaFrom))
                .add(SearchRestrictions.belongsTo(STOCK_AREAS_TO, stockAreaTo)).list().getEntities();
    }

    @Override
    public boolean isTransferValidAndAreResourcesSufficient(final Entity stockAreasFrom, final Entity product,
            final BigDecimal quantity) {
        return ((stockAreasFrom != null) && (product != null) && (quantity != null) && !materialFlowResourceService
                .areResourcesSufficient(stockAreasFrom, product, quantity));
    }
}
