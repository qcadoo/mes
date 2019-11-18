package com.qcadoo.mes.basic.imports.attribute;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttributeImportService {

    private static final Integer HEADER_ROW_NUMBER = 0;

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

        try {
            fillContainer(container, sheet, attributes, L_PRODUCT);
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

        try {
            fillContainer(container, sheet, attributes, L_RESOURCE);
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
        for (Map.Entry<String, Map<String, String>> entry : container.getAtribiutesValuesByType().entrySet()) {
            Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).find()
                    .add(SearchRestrictions.eq(ProductFields.NUMBER, entry.getKey())).setMaxResults(1).uniqueResult();
            if (Objects.isNull(product)) {
                container.getErrors().add(
                        translationService.translate("basic.attributeImport.productNotExists", LocaleContextHolder.getLocale(),
                                entry.getKey()));
                throw new IllegalStateException("No attribute defined");
            }
            Map<String, String> valueByAttribute = container.getAtribiutesValuesByType().get(entry.getKey());
            for (Map.Entry<String, String> valEntry : valueByAttribute.entrySet()) {
                Entity attribute = attributeEntitiesByNumber.get(valEntry.getKey());
                if (Objects.isNull(attribute)) {
                    container.getErrors().add(
                            translationService.translate("basic.attributeImport.attributeNotExists",
                                    LocaleContextHolder.getLocale(), valEntry.getKey()));
                    throw new IllegalStateException("No attribute defined");
                }
                if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
                    Entity attributeValue = getAttributeValue(valEntry, attribute);
                    if (Objects.isNull(attributeValue)) {
                        container.getErrors().add(
                                translationService.translate("basic.attributeImport.attributeValueNotExists",
                                        LocaleContextHolder.getLocale(), valEntry.getKey(), valEntry.getValue()));
                        throw new IllegalStateException("No attribute value defined");
                    } else {
                        Entity productAttributeVal = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                                "productAttributeValue").create();
                        productAttributeVal.setField(ProductAttributeValueFields.VALUE, valEntry.getValue());
                        productAttributeVal.setField(ProductAttributeValueFields.ATTRIBUTE, attribute.getId());
                        productAttributeVal.setField(ProductAttributeValueFields.PRODUCT, product.getId());
                        productAttributeVal.setField(ProductAttributeValueFields.ATTRIBUTE_VALUE, attributeValue.getId());
                        productAttributeVal = productAttributeVal.getDataDefinition().save(productAttributeVal);
                        if (!productAttributeVal.isValid()
                                && !Objects.nonNull(productAttributeVal.getError(ProductAttributeValueFields.ATTRIBUTE_VALUE))
                                && !"basic.attributeValue.error.valueExists".equals(productAttributeVal
                                        .getError(ProductAttributeValueFields.ATTRIBUTE_VALUE))) {
                            container.getErrors().add(
                                    translationService.translate("basic.attributeImport.attributeValueValidationErrors",
                                            LocaleContextHolder.getLocale(), valEntry.getKey(), valEntry.getValue()));
                            throw new IllegalStateException("No attribute value defined");
                        }
                    }
                } else {
                    Entity productAttributeVal = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                            "productAttributeValue").create();
                    if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
                        productAttributeVal.setField(ProductAttributeValueFields.VALUE, valEntry.getValue().replace(".", ","));
                    } else {
                        productAttributeVal.setField(ProductAttributeValueFields.VALUE, valEntry.getValue());
                    }
                    productAttributeVal.setField(ProductAttributeValueFields.ATTRIBUTE, attribute.getId());
                    productAttributeVal.setField(ProductAttributeValueFields.PRODUCT, product.getId());
                    productAttributeVal = productAttributeVal.getDataDefinition().save(productAttributeVal);
                    if (!productAttributeVal.isValid()
                            && (AttributeValueType.NUMERIC.getStringValue().equals(attribute
                                    .getStringField(AttributeFields.VALUE_TYPE)))) {
                        int scale = attribute.getIntegerField(AttributeFields.PRECISION);

                        if ("qcadooView.validate.field.error.invalidNumericFormat".equals(productAttributeVal.getError(
                                ProductAttributeValueFields.VALUE).getMessage())) {
                            container.getErrors().add(
                                    translationService.translate("basic.attributeImport.invalidNumericFormat",
                                            LocaleContextHolder.getLocale(), valEntry.getKey(), valEntry.getValue()));
                            throw new IllegalStateException("No attribute value defined");

                        } else if ("qcadooView.validate.field.error.invalidScale.max".equals(productAttributeVal.getError(
                                ProductAttributeValueFields.VALUE).getMessage())) {
                            container.getErrors().add(
                                    translationService.translate("basic.attributeImport.invalidScale",
                                            LocaleContextHolder.getLocale(), String.valueOf(scale), valEntry.getKey(),
                                            valEntry.getValue()));
                            throw new IllegalStateException("No attribute value defined");
                        }

                    }
                }
            }

        }
    }

    @Transactional
    private void storeResourceAttributes(AttributeImportContainer container, List<AttributePosition> attributes) {
        Map<String, Entity> attributeEntitiesByNumber = getAttributeEntityByNumberForResource(attributes);
        for (Map.Entry<String, Map<String, String>> entry : container.getAtribiutesValuesByType().entrySet()) {
            Entity resource = dataDefinitionService.get("materialFlowResources", "resource").find()
                    .add(SearchRestrictions.eq("number", entry.getKey())).setMaxResults(1).uniqueResult();
            if (Objects.isNull(resource)) {
                container.getErrors().add(
                        translationService.translate("basic.attributeImport.resourceNotExists", LocaleContextHolder.getLocale(),
                                entry.getKey()));
                throw new IllegalStateException("No attribute defined");
            }
            Map<String, String> valueByAttribute = container.getAtribiutesValuesByType().get(entry.getKey());
            for (Map.Entry<String, String> valEntry : valueByAttribute.entrySet()) {
                Entity attribute = attributeEntitiesByNumber.get(valEntry.getKey());
                if (Objects.isNull(attribute)) {
                    container.getErrors().add(
                            translationService.translate("basic.attributeImport.attributeNotExists",
                                    LocaleContextHolder.getLocale(), valEntry.getKey()));
                    throw new IllegalStateException("No attribute defined");
                }
                if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
                    Entity attributeValue = getAttributeValue(valEntry, attribute);
                    if (Objects.isNull(attributeValue)) {
                        container.getErrors().add(
                                translationService.translate("basic.attributeImport.attributeValueNotExists",
                                        LocaleContextHolder.getLocale(), valEntry.getKey(), valEntry.getValue()));
                        throw new IllegalStateException("No attribute value defined");
                    } else {
                        Entity resourceAttributeValue = dataDefinitionService.get("materialFlowResources",
                                "resourceAttributeValue").create();
                        resourceAttributeValue.setField(ProductAttributeValueFields.VALUE, valEntry.getValue());
                        resourceAttributeValue.setField(ProductAttributeValueFields.ATTRIBUTE, attribute.getId());
                        resourceAttributeValue.setField("resource", resource.getId());
                        resourceAttributeValue.setField("fromDefinition", true);
                        resourceAttributeValue.setField(ProductAttributeValueFields.ATTRIBUTE_VALUE, attributeValue.getId());
                        resourceAttributeValue = resourceAttributeValue.getDataDefinition().save(resourceAttributeValue);
                        if (!resourceAttributeValue.isValid()
                                && !Objects.nonNull(resourceAttributeValue.getError(ProductAttributeValueFields.ATTRIBUTE_VALUE))
                                && !"basic.attributeValue.error.valueExists".equals(resourceAttributeValue
                                        .getError(ProductAttributeValueFields.ATTRIBUTE_VALUE))) {
                            container.getErrors().add(
                                    translationService.translate("basic.attributeImport.attributeValueValidationErrors",
                                            LocaleContextHolder.getLocale(), valEntry.getKey(), valEntry.getValue()));
                            throw new IllegalStateException("No attribute value defined");
                        }
                    }
                } else {
                    Entity resourceAttributeValue = dataDefinitionService.get("materialFlowResources",
                            "resourceAttributeValue").create();
                    if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
                        resourceAttributeValue.setField(ProductAttributeValueFields.VALUE, valEntry.getValue().replace(".", ","));
                    } else {
                        resourceAttributeValue.setField(ProductAttributeValueFields.VALUE, valEntry.getValue());
                    }
                    resourceAttributeValue.setField(ProductAttributeValueFields.ATTRIBUTE, attribute.getId());
                    resourceAttributeValue.setField("resource", resource.getId());
                    resourceAttributeValue.setField("fromDefinition", true);
                    resourceAttributeValue = resourceAttributeValue.getDataDefinition().save(resourceAttributeValue);
                    if (!resourceAttributeValue.isValid()
                            && (AttributeValueType.NUMERIC.getStringValue().equals(attribute
                                    .getStringField(AttributeFields.VALUE_TYPE)))) {
                        int scale = attribute.getIntegerField(AttributeFields.PRECISION);

                        if ("qcadooView.validate.field.error.invalidNumericFormat".equals(resourceAttributeValue.getError(
                                ProductAttributeValueFields.VALUE).getMessage())) {
                            container.getErrors().add(
                                    translationService.translate("basic.attributeImport.invalidNumericFormat",
                                            LocaleContextHolder.getLocale(), valEntry.getKey(), valEntry.getValue()));
                            throw new IllegalStateException("No attribute value defined");

                        } else if ("qcadooView.validate.field.error.invalidScale.max".equals(resourceAttributeValue.getError(
                                ProductAttributeValueFields.VALUE).getMessage())) {
                            container.getErrors().add(
                                    translationService.translate("basic.attributeImport.invalidScale",
                                            LocaleContextHolder.getLocale(), String.valueOf(scale), valEntry.getKey(),
                                            valEntry.getValue()));
                            throw new IllegalStateException("No attribute value defined");
                        }

                    }
                }
            }

        }
    }

    private Entity getAttributeValue(Map.Entry<String, String> valEntry, Entity attribute) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.ATTRIBUTE_VALUE).find()
                .add(SearchRestrictions.belongsTo(AttributeValueFields.ATTRIBUTE, attribute))
                .add(SearchRestrictions.eq(AttributeValueFields.VALUE, valEntry.getValue())).setMaxResults(1).uniqueResult();
    }

    private Map<String, Entity> getAttributeEntityByNumberForProduct(List<AttributePosition> attributes) {
        List<String> numbers = attributes.stream().map(AttributePosition::getNumber).collect(Collectors.toList());
        List<Entity> attributeEntities = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.ATTRIBUTE)
                .find().add(SearchRestrictions.eq(AttributeFields.FOR_PRODUCT, true))
                .add(SearchRestrictions.in(AttributeFields.NUMBER, numbers)).list().getEntities();
        return attributeEntities.stream().collect(Collectors.toMap(ae -> ae.getStringField(AttributeFields.NUMBER), ae -> ae));
    }

    private Map<String, Entity> getAttributeEntityByNumberForResource(List<AttributePosition> attributes) {
        List<String> numbers = attributes.stream().map(AttributePosition::getNumber).collect(Collectors.toList());
        List<Entity> attributeEntities = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.ATTRIBUTE)
                .find().add(SearchRestrictions.eq(AttributeFields.FOR_RESOURCE, true))
                .add(SearchRestrictions.in(AttributeFields.NUMBER, numbers)).list().getEntities();
        return attributeEntities.stream().collect(Collectors.toMap(ae -> ae.getStringField(AttributeFields.NUMBER), ae -> ae));
    }

    private List<AttributePosition> prepareAttributesList(XSSFSheet sheet) {
        List<AttributePosition> positions = Lists.newArrayList();
        Row row = sheet.getRow(0);
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
                        Map<String, String> valueByAttribute = container.getAtribiutesValuesByType().get(number);
                        attributes.forEach(ap -> {
                            Cell valCell = row.getCell(ap.getPosition());
                            if (Objects.nonNull(valCell)) {
                                valCell.setCellType(Cell.CELL_TYPE_STRING);
                                String val = valCell.getStringCellValue();

                                if (StringUtils.isNoneEmpty(val)) {
                                    valueByAttribute.put(ap.getNumber(), val);
                                }
                            }
                        });
                    } else {
                        Map<String, String> valueByAttribute = Maps.newHashMap();
                        attributes.forEach(ap -> {
                            Cell valCell = row.getCell(ap.getPosition());
                            if (Objects.nonNull(valCell)) {
                                valCell.setCellType(Cell.CELL_TYPE_STRING);
                                String val = valCell.getStringCellValue();

                                if (StringUtils.isNoneEmpty(val)) {
                                    valueByAttribute.put(ap.getNumber(), val);
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

        for (Map.Entry<String, Map<String, String>> entry : container.getAtribiutesValuesByType().entrySet()) {
            if (entry.getValue().isEmpty()) {
                container.getErrors().add(
                        translationService.translate("basic.attributeImport.no" + attributeValueFor + "AttributeDefined",
                                LocaleContextHolder.getLocale(), entry.getKey()));
                throw new IllegalStateException("No attribute defined");
            }
        }
    }

}
