package com.qcadoo.mes.basic.imports.attribute;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AttributeImportService {

    public static final String L_PRODUCT = "product";

    public static final String L_RESOURCE = "resource";

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public boolean importProductAttributeValues(final String path, final ViewDefinitionState view) throws IOException {
        AttributeImportContainer container = new AttributeImportContainer();
        InputStream stream = fileService.getInputStream(path);

        XSSFWorkbook workbook = new XSSFWorkbook(stream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<AttributePosition> attributes = prepareAttributesList(sheet);
        if (attributes.isEmpty()) {
            view.addMessage("basic.attributeValuesImport.importFileEmpty", ComponentState.MessageType.INFO);
            return container.getErrors().isEmpty();
        }
        try {
            fillContainer(container, sheet, attributes, L_PRODUCT);
            if (container.getAtribiutesValuesByType().isEmpty()) {
                view.addMessage("basic.attributeValuesImport.importFileEmpty", ComponentState.MessageType.INFO);
                return container.getErrors().isEmpty();
            }
            storeProductAttributes(container, attributes);
            view.addMessage("basic.attributeValuesImport.success", ComponentState.MessageType.SUCCESS);
            return container.getErrors().isEmpty();
        } catch (Exception exc) {
            container.getErrors().forEach(err -> {
                view.addTranslatedMessage(err, ComponentState.MessageType.FAILURE);
            });
            return false;
        }
    }

    public boolean importResourceAttributeValues(final String path, final ViewDefinitionState view) throws IOException {
        AttributeImportContainer container = new AttributeImportContainer();
        InputStream stream = fileService.getInputStream(path);

        XSSFWorkbook workbook = new XSSFWorkbook(stream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<AttributePosition> attributes = prepareAttributesList(sheet);
        if (attributes.isEmpty()) {
            view.addMessage("basic.attributeValuesImport.importFileEmpty", ComponentState.MessageType.INFO);
            return container.getErrors().isEmpty();
        }
        try {
            fillContainer(container, sheet, attributes, L_RESOURCE);
            if (container.getAtribiutesValuesByType().isEmpty()) {
                view.addMessage("basic.attributeValuesImport.importFileEmpty", ComponentState.MessageType.INFO);
                return container.getErrors().isEmpty();
            }
            storeResourceAttributes(container, attributes);
            view.addMessage("basic.attributeValuesImport.success", ComponentState.MessageType.SUCCESS);
            return container.getErrors().isEmpty();
        } catch (Exception exc) {
            container.getErrors().forEach(err -> {
                view.addTranslatedMessage(err, ComponentState.MessageType.FAILURE);
            });
            return false;
        }
    }

    @Transactional
    private void storeProductAttributes(AttributeImportContainer container, List<AttributePosition> attributes) {
        Map<String, Entity> attributeEntitiesByNumber = getAttributeEntityByNumberForProduct(attributes);
        for (Map.Entry<String, Map<String, List<String>>> entry : container.getAtribiutesValuesByType().entrySet()) {
            Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).find()
                    .add(SearchRestrictions.eq(ProductFields.NUMBER, entry.getKey())).setMaxResults(1).uniqueResult();
            if (Objects.isNull(product)) {
                container.getErrors().add(
                        translationService.translate("basic.attributeImport.productNotExists", LocaleContextHolder.getLocale(),
                                entry.getKey()));
                throw new IllegalStateException("No attribute defined");
            }
            Map<String, List<String>> valueByAttribute = container.getAtribiutesValuesByType().get(entry.getKey());
            for (Map.Entry<String, List<String>> valEntry : valueByAttribute.entrySet()) {
                Entity attribute = attributeEntitiesByNumber.get(valEntry.getKey());
                if (Objects.isNull(attribute)) {
                    container.getErrors().add(
                            translationService.translate("basic.attributeImport.attributeNotExists",
                                    LocaleContextHolder.getLocale(), valEntry.getKey()));
                    throw new IllegalStateException("No attribute defined");
                }

                for (String valueAttr : valEntry.getValue()) {
                    String value = valueAttr;
                    if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
                        value = valueAttr.replace(".", ",");
                    }

                    if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
                        Entity attributeValue = getAttributeValue(value, attribute);
                        if (Objects.isNull(attributeValue)) {
                            container.getErrors().add(
                                    translationService.translate("basic.attributeImport.attributeValueNotExists",
                                            LocaleContextHolder.getLocale(), valEntry.getKey(), value));
                            throw new IllegalStateException("No attribute value defined");
                        }
                        Entity productAttributeVal = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                                "productAttributeValue").create();
                        productAttributeVal.setField(ProductAttributeValueFields.VALUE, value);
                        productAttributeVal.setField(ProductAttributeValueFields.ATTRIBUTE, attribute.getId());
                        productAttributeVal.setField(ProductAttributeValueFields.PRODUCT, product.getId());
                        productAttributeVal.setField(ProductAttributeValueFields.ATTRIBUTE_VALUE, attributeValue.getId());
                        productAttributeVal = productAttributeVal.getDataDefinition().save(productAttributeVal);
                        if (!productAttributeVal.isValid()) {
                            addErrorsForCalculated(container, valEntry.getKey(), value, productAttributeVal);
                        }
                    } else {
                        Entity productAttributeVal = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                                "productAttributeValue").create();
                        productAttributeVal.setField(ProductAttributeValueFields.VALUE, value);
                        productAttributeVal.setField(ProductAttributeValueFields.ATTRIBUTE, attribute.getId());
                        productAttributeVal.setField(ProductAttributeValueFields.PRODUCT, product.getId());
                        productAttributeVal = productAttributeVal.getDataDefinition().save(productAttributeVal);
                        if (!productAttributeVal.isValid()) {
                            addErrorsForContinuous(container, valEntry.getKey(), value, attribute, productAttributeVal);

                        }
                    }
                }
            }

        }
    }

    @Transactional
    private void storeResourceAttributes(AttributeImportContainer container, List<AttributePosition> attributes) {
        Map<String, Entity> attributeEntitiesByNumber = getAttributeEntityByNumberForResource(attributes);
        for (Map.Entry<String, Map<String, List<String>>> entry : container.getAtribiutesValuesByType().entrySet()) {
            Entity resource = dataDefinitionService.get("materialFlowResources", "resource").find()
                    .add(SearchRestrictions.eq("number", entry.getKey())).setMaxResults(1).uniqueResult();
            if (Objects.isNull(resource)) {
                container.getErrors().add(
                        translationService.translate("basic.attributeImport.resourceNotExists", LocaleContextHolder.getLocale(),
                                entry.getKey()));
                throw new IllegalStateException("No attribute defined");
            }
            Map<String, List<String>> valueByAttribute = container.getAtribiutesValuesByType().get(entry.getKey());
            for (Map.Entry<String, List<String>> valEntry : valueByAttribute.entrySet()) {
                Entity attribute = attributeEntitiesByNumber.get(valEntry.getKey());
                if (Objects.isNull(attribute)) {
                    container.getErrors().add(
                            translationService.translate("basic.attributeImport.attributeNotExists",
                                    LocaleContextHolder.getLocale(), valEntry.getKey()));
                    throw new IllegalStateException("No attribute defined");
                }
                for (String valueAttr : valEntry.getValue()) {
                    String value = valueAttr;
                    if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
                        value = valueAttr.replace(".", ",");
                    }
                    if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
                        Entity attributeValue = getAttributeValue(value, attribute);
                        if (Objects.isNull(attributeValue)) {
                            container.getErrors().add(
                                    translationService.translate("basic.attributeImport.attributeValueNotExists",
                                            LocaleContextHolder.getLocale(), valEntry.getKey(), value));
                            throw new IllegalStateException("No attribute value defined");
                        }

                        Entity resourceAttributeValue = dataDefinitionService.get("materialFlowResources",
                                "resourceAttributeValue").create();
                        resourceAttributeValue.setField(ProductAttributeValueFields.VALUE, value);
                        resourceAttributeValue.setField(ProductAttributeValueFields.ATTRIBUTE, attribute.getId());
                        resourceAttributeValue.setField("resource", resource.getId());
                        resourceAttributeValue.setField("fromDefinition", true);
                        resourceAttributeValue.setField(ProductAttributeValueFields.ATTRIBUTE_VALUE, attributeValue.getId());
                        resourceAttributeValue = resourceAttributeValue.getDataDefinition().save(resourceAttributeValue);
                        if (!resourceAttributeValue.isValid()) {
                            addErrorsForCalculated(container, valEntry.getKey(), value, resourceAttributeValue);
                        }
                    } else {
                        Entity resourceAttributeValue = dataDefinitionService.get("materialFlowResources",
                                "resourceAttributeValue").create();

                        resourceAttributeValue.setField(ProductAttributeValueFields.VALUE, value);
                        resourceAttributeValue.setField(ProductAttributeValueFields.ATTRIBUTE, attribute.getId());
                        resourceAttributeValue.setField("resource", resource.getId());
                        resourceAttributeValue.setField("fromDefinition", true);
                        resourceAttributeValue = resourceAttributeValue.getDataDefinition().save(resourceAttributeValue);
                        if (!resourceAttributeValue.isValid()) {
                            addErrorsForContinuous(container, valEntry.getKey(), value, attribute, resourceAttributeValue);
                        }
                    }
                }
            }

        }
    }

    private void addErrorsForCalculated(AttributeImportContainer container, String key, String value,
            Entity resourceAttributeValue) {
        if (!"basic.attributeValue.error.valueExists".equals(resourceAttributeValue.getError(ProductAttributeValueFields.ATTRIBUTE_VALUE)
                .getMessage())) {
            container.getErrors().add(
                    translationService.translate("basic.attributeImport.attributeValueValidationErrors",
                            LocaleContextHolder.getLocale(), key, value));
            throw new IllegalStateException("No attribute value defined");
        }
    }

    private void addErrorsForContinuous(AttributeImportContainer container, String key, String value, Entity attribute,
            Entity attributeVal) {
        Integer scale = attribute.getIntegerField(AttributeFields.PRECISION);

        if ("qcadooView.validate.field.error.invalidNumericFormat".equals(attributeVal
                .getError(ProductAttributeValueFields.VALUE).getMessage())) {
            container.getErrors().add(
                    translationService.translate("basic.attributeImport.invalidNumericFormat", LocaleContextHolder.getLocale(),
                            key, value));
            throw new IllegalStateException("No attribute value defined");

        } else if ("qcadooView.validate.field.error.invalidScale.max".equals(attributeVal.getError(
                ProductAttributeValueFields.VALUE).getMessage())) {
            container.getErrors().add(
                    translationService.translate("basic.attributeImport.invalidScale", LocaleContextHolder.getLocale(),
                            String.valueOf(scale), key, value));
            throw new IllegalStateException("No attribute value defined");
        }
    }

    private Entity getAttributeValue(String valEntry, Entity attribute) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_ATTRIBUTE_VALUE).find()
                .add(SearchRestrictions.belongsTo(AttributeValueFields.ATTRIBUTE, attribute))
                .add(SearchRestrictions.eq(AttributeValueFields.VALUE, valEntry)).setMaxResults(1).uniqueResult();
    }

    private Map<String, Entity> getAttributeEntityByNumberForProduct(List<AttributePosition> attributes) {
        List<String> numbers = attributes.stream().map(AttributePosition::getNumber).collect(Collectors.toList());
        List<Entity> attributeEntities = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_ATTRIBUTE)
                .find().add(SearchRestrictions.eq(AttributeFields.FOR_PRODUCT, true))
                .add(SearchRestrictions.in(AttributeFields.NUMBER, numbers)).list().getEntities();
        return attributeEntities.stream().collect(Collectors.toMap(ae -> ae.getStringField(AttributeFields.NUMBER), ae -> ae));
    }

    private Map<String, Entity> getAttributeEntityByNumberForResource(List<AttributePosition> attributes) {
        List<String> numbers = attributes.stream().map(AttributePosition::getNumber).collect(Collectors.toList());
        List<Entity> attributeEntities = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_ATTRIBUTE)
                .find().add(SearchRestrictions.eq(AttributeFields.FOR_RESOURCE, true))
                .add(SearchRestrictions.in(AttributeFields.NUMBER, numbers)).list().getEntities();
        return attributeEntities.stream().collect(Collectors.toMap(ae -> ae.getStringField(AttributeFields.NUMBER), ae -> ae));
    }

    private List<AttributePosition> prepareAttributesList(XSSFSheet sheet) {
        List<AttributePosition> positions = Lists.newArrayList();
        Row row = sheet.getRow(0);
        if(Objects.isNull(row)) {
            return positions;
        }
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            int index = cell.getColumnIndex();
            if (index > 0) {
                String number = cell.getStringCellValue();
                AttributePosition attributePosition = new AttributePosition();
                attributePosition.setNumber(number);
                attributePosition.setPosition(index);
                positions.add(attributePosition);
            }
        }
        return positions;
    }

    private void fillContainer(AttributeImportContainer container, XSSFSheet sheet, List<AttributePosition> attributes,
            String attributeValueFor) {
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() > 0) {
                Cell productCell = row.getCell(0);
                if (Objects.nonNull(productCell) && StringUtils.isNoneEmpty(productCell.getStringCellValue())) {
                    String number = productCell.getStringCellValue();
                    if (container.getAtribiutesValuesByType().containsKey(number)) {
                        Map<String, List<String>> valueByAttribute = container.getAtribiutesValuesByType().get(number);
                        attributes.forEach(ap -> {
                            Cell valCell = row.getCell(ap.getPosition());
                            if (Objects.nonNull(valCell)) {
                                valCell.setCellType(Cell.CELL_TYPE_STRING);
                                String val = valCell.getStringCellValue();

                                if (StringUtils.isNoneEmpty(val)) {
                                    List<String> vals = valueByAttribute.get(ap.getNumber());
                                    if (Objects.isNull(vals)) {
                                        vals = Lists.newArrayList();
                                    }
                                    vals.add(val);
                                    valueByAttribute.put(ap.getNumber(), vals);
                                }
                            }
                        });
                    } else {
                        Map<String, List<String>> valueByAttribute = Maps.newHashMap();
                        attributes.forEach(ap -> {
                            Cell valCell = row.getCell(ap.getPosition());
                            if (Objects.nonNull(valCell)) {
                                valCell.setCellType(Cell.CELL_TYPE_STRING);
                                String val = valCell.getStringCellValue();
                                List<String> vals = Lists.newArrayList(val);
                                if (StringUtils.isNoneEmpty(val)) {
                                    valueByAttribute.put(ap.getNumber(), vals);
                                }
                            }
                        });
                        container.getAtribiutesValuesByType().put(number, valueByAttribute);
                    }
                } else {
                    container.getErrors().add(
                            translationService.translate("basic.attributeImport." + attributeValueFor + "NotFilled",
                                    LocaleContextHolder.getLocale(), String.valueOf(row.getRowNum() + 1)));
                    throw new IllegalStateException("Product number not filled");
                }
            }
        }

        for (Map.Entry<String, Map<String, List<String>>> entry : container.getAtribiutesValuesByType().entrySet()) {
            if (entry.getValue().isEmpty()) {
                container.getErrors().add(
                        translationService.translate("basic.attributeImport.no" + attributeValueFor + "AttributeDefined",
                                LocaleContextHolder.getLocale(), entry.getKey()));
                throw new IllegalStateException("No attribute defined");
            }
        }
    }

}
