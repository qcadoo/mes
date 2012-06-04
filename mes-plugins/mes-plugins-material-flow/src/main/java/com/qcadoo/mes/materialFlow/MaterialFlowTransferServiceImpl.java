package com.qcadoo.mes.materialFlow;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void createTransfer(final String type, final Entity stockAreaFrom, final Entity stockAreaTo, final Entity product,
            final BigDecimal quantity, final Entity staff, final Date time) {
        DataDefinition dd = dataDefinitionService.get("materialFlow", "transfer");

        Entity transfer = dd.create();
        String number = materialFlowService.generateNumberFromProduct(product, "transfer");

        transfer.setField("number", number);
        transfer.setField("type", type);
        transfer.setField("stockAreasFrom", stockAreaFrom);
        transfer.setField("stockAreasTo", stockAreaTo);
        transfer.setField("product", product);
        transfer.setField("quantity", quantity);
        transfer.setField("staff", staff);
        transfer.setField("time", time);

        checkArgument(dd.save(transfer).isValid(), "invalid transfer id =" + transfer.getId());
    }

    @Override
    public List<Entity> getTransferTemplates(final Entity stockAreaFrom, final Entity stockAreaTo) {
        return dataDefinitionService.get("materialFlow", "transferTemplate").find()
                .add(SearchRestrictions.belongsTo("stockAreasFrom", stockAreaFrom))
                .add(SearchRestrictions.belongsTo("stockAreasTo", stockAreaTo)).list().getEntities();
    }
}
