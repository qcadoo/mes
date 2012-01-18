package com.qcadoo.mes.workPlans.workPlansColumnExtension;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.workPlans.print.ColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

@Service
public class WorkPlansColumnFiller implements ColumnFiller {

    // TODO mici, those constants will end up as duplication somewhere,
    // in the columnLoader probably, they should be either here or there.

    private final static String PRODUCT_COLUMN = "productName";

    private final static String QUANTITY_COLUMN = "plannedQuantity";

    /**
     * 
     * @param orders
     *            List of orders
     * @return The Keys of the map are productComponents, values are Maps columnIdentifier -> columnValue
     */
    public Map<Entity, Map<String, String>> getValues(List<Entity> orders) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        fillProductNames(orders, values);
        fillPlannedQuantities(orders, values);

        return values;
    }

    private void fillProductNames(List<Entity> orders, Map<Entity, Map<String, String>> valuesMap) {
        for (Entity order : orders) {

            // TODO mici, change those to orderOperationComponents?
            Entity technology = order.getBelongsToField("technology");
            EntityTree operationComponents = technology.getTreeField("operationComponents");

            for (Entity operationComponent : operationComponents) {
                EntityList inputProducts = operationComponent.getHasManyField("operationProductInComponents");
                EntityList outputProducts = operationComponent.getHasManyField("operationProductOutComponents");

                for (Entity productComponent : outputProducts) {
                    if (valuesMap.get(productComponent) == null) {
                        valuesMap.put(productComponent, new HashMap<String, String>());
                    }
                    valuesMap.get(productComponent).put(PRODUCT_COLUMN, getProductName(productComponent));
                }

                for (Entity productComponent : inputProducts) {
                    if (valuesMap.get(productComponent) == null) {
                        valuesMap.put(productComponent, new HashMap<String, String>());
                    }
                    valuesMap.get(productComponent).put(PRODUCT_COLUMN, getProductName(productComponent));
                }

            }
        }
    }

    private void fillPlannedQuantities(List<Entity> orders, Map<Entity, Map<String, String>> valuesMap) {
        WorkPlansProductsService workPlansProductsSerivce = new WorkPlansProductsService();
        Map<Entity, BigDecimal> productQuantities = workPlansProductsSerivce.getProductQuantities(orders);

        Locale locale = LocaleContextHolder.getLocale();
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);

        for (Entity order : orders) {
            // TODO mici, change those to orderOperationComponents?
            Entity technology = order.getBelongsToField("technology");
            EntityTree operationComponents = technology.getTreeField("operationComponents");

            for (Entity operationComponent : operationComponents) {
                EntityList inputProducts = operationComponent.getHasManyField("operationProductInComponents");
                EntityList outputProducts = operationComponent.getHasManyField("operationProductOutComponents");

                for (Entity productComponent : outputProducts) {
                    if (valuesMap.get(productComponent) == null) {
                        valuesMap.put(productComponent, new HashMap<String, String>());
                    }
                    String unit = productComponent.getBelongsToField("product").getStringField("unit");
                    String quantityString = decimalFormat.format(productQuantities.get(productComponent)) + " " + unit;
                    valuesMap.get(productComponent).put(QUANTITY_COLUMN, quantityString);
                }

                for (Entity productComponent : inputProducts) {
                    if (valuesMap.get(productComponent) == null) {
                        valuesMap.put(productComponent, new HashMap<String, String>());
                    }
                    String unit = productComponent.getBelongsToField("product").getStringField("unit");
                    String quantityString = decimalFormat.format(productQuantities.get(productComponent)) + " " + unit;
                    valuesMap.get(productComponent).put(QUANTITY_COLUMN, quantityString);
                }

            }
        }
    }

    private String getProductName(Entity productComponent) {
        Entity product = productComponent.getBelongsToField("product");

        StringBuilder productString = new StringBuilder(product.getStringField("name"));
        productString.append(" (");
        productString.append(product.getStringField("number"));
        productString.append(")");

        return productString.toString();
    }
}
