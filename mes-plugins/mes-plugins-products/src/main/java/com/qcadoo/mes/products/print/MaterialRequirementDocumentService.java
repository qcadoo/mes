/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.products.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.model.types.internal.DateType;

public abstract class MaterialRequirementDocumentService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String FILE_NAME = "MaterialRequirement";

    private static final String MATERIAL_COMPONENT = "component";

    private static final SimpleDateFormat D_F = new SimpleDateFormat(DateType.REPORT_DATE_TIME_FORMAT);

    @Value("${windowsFonts}")
    private String windowsFontsPath;

    @Value("${macosFonts}")
    private String macosFontsPath;

    @Value("${linuxFonts}")
    private String linuxFontsPath;

    @Value("${reportPath}")
    private String path;

    protected final String getFileName(final Date date) {
        return path + FILE_NAME + "_" + D_F.format(date);
    }

    protected final void updateFileName(final Entity entity, final String fileName) {
        entity.setField("fileName", fileName);
        dataDefinitionService.get("products", "materialRequirement").save(entity);
    }

    protected final Map<ProxyEntity, BigDecimal> getBomSeries(final Entity entity, final List<Entity> orders) {
        Map<ProxyEntity, BigDecimal> products = new HashMap<ProxyEntity, BigDecimal>();
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            if (technology != null && plannedQuantity != null && plannedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                List<Entity> bomComponents = technology.getHasManyField("bomComponents");
                for (Entity bomComponent : bomComponents) {
                    ProxyEntity product = (ProxyEntity) bomComponent.getField("product");
                    if (!(Boolean) entity.getField("onlyComponents")
                            || MATERIAL_COMPONENT.equals(product.getField("typeOfMaterial"))) {
                        if (products.containsKey(product)) {
                            BigDecimal quantity = products.get(product);
                            quantity = ((BigDecimal) bomComponent.getField("quantity")).multiply(plannedQuantity).add(quantity);
                            products.put(product, quantity);
                        } else {
                            products.put(product, ((BigDecimal) bomComponent.getField("quantity")).multiply(plannedQuantity));
                        }
                    }
                }
            }
        }
        return products;
    }

    public abstract void generateDocument(final Entity entity, final Locale locale) throws IOException, DocumentException;

    protected final TranslationService getTranslationService() {
        return translationService;
    }

    protected final String getFontsPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return windowsFontsPath;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            return macosFontsPath;
        } else if (SystemUtils.IS_OS_LINUX) {
            return linuxFontsPath;
        }
        return null;
    }

}
