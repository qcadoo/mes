package com.qcadoo.mes.products.print.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DataAccessService;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.internal.InternalDataDefinition;

public abstract class MaterialRequirementDocumentService {

    @Autowired
    protected TranslationService translationService;

    @Autowired
    protected DataAccessService dataAccessService;

    @Autowired
    protected DataDefinitionService dataDefinitionService;

    private static final String FILE_NAME = "MaterialRequirement";

    private static final String DATE_FORMAT = "yyyy_MM_dd_HH_mm";

    private static final SimpleDateFormat D_F = new SimpleDateFormat(DATE_FORMAT);

    // TODO KRNA properties
    // @Value("#{systemProperties.reportPath}")
    private String reportPath = "/tmp/";

    private String fileName = reportPath + FILE_NAME + "_" + D_F.format(new Date());

    protected final String getFileName() {
        return fileName;
    }

    protected final void updateFileName(final Entity entity, final String fileName) {
        entity.setField("fileName", fileName);
        DataDefinition dataDefinition = dataDefinitionService.get("products", "materialRequirement");
        dataAccessService.save((InternalDataDefinition) dataDefinition, entity);
    }

    protected final Map<ProxyEntity, BigDecimal> getBomSeries(final Entity entity, final List<Entity> instructions) {
        Map<ProxyEntity, BigDecimal> products = new HashedMap();
        for (Entity instruction : instructions) {
            List<Entity> bomComponents = (List<Entity>) instruction.getField("bomComponents");
            for (Entity bomComponent : bomComponents) {
                ProxyEntity product = (ProxyEntity) bomComponent.getField("product");
                if (!(Boolean) entity.getField("onlyComponents") || "component".equals(product.getField("typeOfMaterial"))) {
                    if (products.containsKey(product)) {
                        BigDecimal quantity = products.get(product);
                        quantity = ((BigDecimal) bomComponent.getField("quantity")).add(quantity);
                        products.put(product, quantity);
                    } else {
                        products.put(product, (BigDecimal) bomComponent.getField("quantity"));
                    }
                }
            }
        }
        return products;
    }

    public abstract void generateDocument(final Entity entity, final Locale locale) throws IOException, DocumentException;

}
