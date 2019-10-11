package com.qcadoo.mes.materialFlowResources.hooks;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceAttributeValueCorrectionAfterFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceAttributeValueCorrectionBeforeFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceAttributeValueFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ResourceAttributeValueHooks {

    public static final String L_RESOURCE_CORRECTION = "resourceCorrection";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    public boolean validate(final DataDefinition resourceAttributeValueDD, final Entity resourceAttributeValue) {
        Entity attribute = resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && Objects.isNull(resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE_VALUE))) {
            resourceAttributeValue.addError(resourceAttributeValueDD.getField(ResourceAttributeValueFields.ATTRIBUTE_VALUE),
                    "qcadooView.validate.field.error.missing");
            return false;
        }

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParse(
                    resourceAttributeValue.getStringField(ResourceAttributeValueFields.VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                int scale = attribute.getIntegerField(AttributeFields.PRECISION);
                int valueScale = eitherNumber.getRight().get().scale();
                if (valueScale > scale) {
                    resourceAttributeValue.addError(resourceAttributeValueDD.getField(ResourceAttributeValueFields.VALUE),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));
                    return false;
                }
            } else {
                resourceAttributeValue.addError(resourceAttributeValueDD.getField(ResourceAttributeValueFields.VALUE),
                        "qcadooView.validate.field.error.invalidNumericFormat");
                return false;
            }
        }
        if (checkIfValueExists(resourceAttributeValueDD, resourceAttributeValue)) {
            return false;
        }

        return true;
    }

    private boolean checkIfValueExists(DataDefinition resourceAttributeValueDD, Entity resourceAttributeValue) {
        Entity attribute = resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE);
        Entity resource = resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.RESOURCE);
        Entity attributeValue = resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE_VALUE);

        List<Entity> values = resource.getHasManyField(ResourceFields.RESOURCE_ATTRIBIUTE_VALUES);

        List sameValue = Lists.newArrayList();
        if (Objects.nonNull(attributeValue)) {
            sameValue = values
                    .stream()
                    .filter(val -> val.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE).getId()
                            .equals(attribute.getId())
                            && Objects.nonNull(val.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE_VALUE))
                            && val.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE_VALUE).getId()
                                    .equals(attributeValue.getId()))
                    .filter(val -> !val.getId().equals(resourceAttributeValue.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                resourceAttributeValue.addError(resourceAttributeValueDD.getField(ResourceAttributeValueFields.ATTRIBUTE_VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        } else {
            sameValue = values
                    .stream()
                    .filter(val -> val.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE).getId()
                            .equals(attribute.getId())
                            && Objects.isNull(val.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE_VALUE))
                            && val.getStringField(ResourceAttributeValueFields.VALUE).equals(
                                    resourceAttributeValue.getStringField(ResourceAttributeValueFields.VALUE)))
                    .filter(val -> !val.getId().equals(resourceAttributeValue.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                resourceAttributeValue.addError(resourceAttributeValueDD.getField(ResourceAttributeValueFields.VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        }

        return false;
    }

    public void onSave(final DataDefinition resourceAttributeValueDD, final Entity resourceAttributeValue) {
        if (resourceAttributeValue.getBooleanField(ResourceAttributeValueFields.FROM_DEFINITION)) {
            Entity resource = resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.RESOURCE);
            java.util.Optional<Entity> maybeCorrection = resourceCorrectionService.createCorrectionForResource(resource, true);
            if (maybeCorrection.isPresent()) {
                Entity correction = maybeCorrection.get();
                Entity attributeAfterCorrection = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE_ATTRIBUTE_VALUE_AFTER_CORRECTION).create();
                Entity attributeBeforeCorrection = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE_ATTRIBUTE_VALUE_BEFORE_CORRECTION).create();

                if (Objects.nonNull(resourceAttributeValue.getId())) {
                    Entity resourceAttributeValueDb = resourceAttributeValueDD.get(resourceAttributeValue.getId());
                    attributeBeforeCorrection.setField(ResourceAttributeValueCorrectionBeforeFields.ATTRIBUTE,
                            resourceAttributeValueDb.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE).getId());
                    attributeBeforeCorrection.setField(ResourceAttributeValueCorrectionBeforeFields.VALUE,
                            resourceAttributeValueDb.getStringField(ResourceAttributeValueFields.VALUE));
                    attributeBeforeCorrection.setField(L_RESOURCE_CORRECTION, correction.getId());
                    attributeBeforeCorrection.getDataDefinition().save(attributeBeforeCorrection);

                }
                attributeAfterCorrection.setField(ResourceAttributeValueCorrectionAfterFields.ATTRIBUTE, resourceAttributeValue
                        .getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE).getId());
                attributeAfterCorrection.setField(ResourceAttributeValueCorrectionAfterFields.VALUE,
                        resourceAttributeValue.getStringField(ResourceAttributeValueFields.VALUE));
                attributeAfterCorrection.setField(L_RESOURCE_CORRECTION, correction.getId());
                attributeAfterCorrection.getDataDefinition().save(attributeAfterCorrection);

            }
        }
    }

    public boolean onDelete(final DataDefinition resourceAttributeValueDD, final Entity resourceAttributeValue) {
        Entity resource = resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.RESOURCE);
        java.util.Optional<Entity> maybeCorrection = resourceCorrectionService.createCorrectionForResource(resource, true);
        if (maybeCorrection.isPresent()) {
            Entity correction = maybeCorrection.get();
            Entity attributeBeforeCorrection = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE_ATTRIBUTE_VALUE_BEFORE_CORRECTION).create();
            attributeBeforeCorrection.setField(ResourceAttributeValueCorrectionAfterFields.ATTRIBUTE, resourceAttributeValue
                    .getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE).getId());
            attributeBeforeCorrection.setField(ResourceAttributeValueCorrectionAfterFields.VALUE,
                    resourceAttributeValue.getStringField(ResourceAttributeValueFields.VALUE));
            attributeBeforeCorrection.setField(L_RESOURCE_CORRECTION, correction.getId());
            attributeBeforeCorrection.getDataDefinition().save(attributeBeforeCorrection);

        }

        return true;
    }
}
