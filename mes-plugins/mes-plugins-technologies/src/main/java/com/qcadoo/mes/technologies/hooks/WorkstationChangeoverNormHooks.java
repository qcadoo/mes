package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormChangeoverType;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WorkstationChangeoverNormHooks {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_DASH = "-";

    @Autowired
    private TranslationService translationService;

    public boolean validatesWith(final DataDefinition workstationChangeoverNormDD,
                                 final Entity workstationChangeoverNorm) {
        boolean isValid = checkIfWorkstationTypeOrWorkstationAreEmpty(workstationChangeoverNorm);
        isValid = isValid && checkIfFromAndToAttributeValuesAreEmptyOrSame(workstationChangeoverNorm);
        isValid = isValid && checkIfWorkstationChangeoverNormIsUnique(workstationChangeoverNormDD, workstationChangeoverNorm);
        isValid = isValid && checkIfWorkstationChangeoverNormExistsWithOtherType(workstationChangeoverNormDD, workstationChangeoverNorm);

        return isValid;
    }

    private boolean checkIfWorkstationChangeoverNormExistsWithOtherType(DataDefinition workstationChangeoverNormDD,
                                                                        Entity workstationChangeoverNorm) {
        Entity workstationType = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION_TYPE);
        Entity workstation = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION);
        Entity attribute = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.ATTRIBUTE);
        String changeoverType = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.CHANGEOVER_TYPE);

        SearchCriteriaBuilder searchCriteriaBuilder = prepareWorkstationAndAttributeCriteria(workstationChangeoverNormDD, workstationType, workstation, attribute);

        if (WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue().equals(changeoverType)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.CHANGEOVER_TYPE, WorkstationChangeoverNormChangeoverType.ANY_CHANGE.getStringValue()));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.CHANGEOVER_TYPE, WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue()));
        }

        if (Objects.nonNull(searchCriteriaBuilder.setMaxResults(1).uniqueResult())) {
            workstationChangeoverNorm.addGlobalError("technologies.workstationChangeoverNorm.error.otherTypeExists");

            return false;
        }

        return true;
    }

    private SearchCriteriaBuilder prepareWorkstationAndAttributeCriteria(DataDefinition workstationChangeoverNormDD,
                                                                         Entity workstationType, Entity workstation,
                                                                         Entity attribute) {
        SearchCriteriaBuilder searchCriteriaBuilder = workstationChangeoverNormDD.find();

        if (Objects.nonNull(workstationType)) {
            searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.WORKSTATION_TYPE, WorkstationChangeoverNormFields.WORKSTATION_TYPE, JoinType.LEFT);
            searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.WORKSTATION_TYPE + L_DOT + L_ID, workstationType.getId()));
        }
        if (Objects.nonNull(workstation)) {
            searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.WORKSTATION, WorkstationChangeoverNormFields.WORKSTATION, JoinType.LEFT);
            searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.WORKSTATION + L_DOT + L_ID, workstation.getId()));
        }
        if (Objects.nonNull(attribute)) {
            searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.ATTRIBUTE, WorkstationChangeoverNormFields.ATTRIBUTE, JoinType.LEFT);
            searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.ATTRIBUTE + L_DOT + L_ID, attribute.getId()));
        }
        return searchCriteriaBuilder;
    }

    private boolean checkIfWorkstationTypeOrWorkstationAreEmpty(final Entity workstationChangeoverNorm) {
        Entity workstationType = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION_TYPE);
        Entity workstation = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION);

        if (Objects.isNull(workstationType) && Objects.isNull(workstation)) {
            workstationChangeoverNorm.addGlobalError("technologies.workstationChangeoverNorm.error.workstationTypeOrWorkstationAreEmpty");

            return false;
        }

        return true;
    }

    private boolean checkIfFromAndToAttributeValuesAreEmptyOrSame(final Entity workstationChangeoverNorm) {
        String changeoverType = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.CHANGEOVER_TYPE);
        Entity fromAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
        Entity toAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);

        if (WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue().equals(changeoverType)) {
            if (Objects.isNull(fromAttributeValue) || Objects.isNull(toAttributeValue)) {
                workstationChangeoverNorm.addGlobalError("technologies.workstationChangeoverNorm.error.fromAndToAttributeValuesAreEmpty");

                return false;
            } else {
                if (fromAttributeValue.getId().equals(toAttributeValue.getId())) {
                    workstationChangeoverNorm.addGlobalError("technologies.workstationChangeoverNorm.error.fromAndToAttributeValuesAreSame");

                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkIfWorkstationChangeoverNormIsUnique(final DataDefinition workstationChangeoverNormDD,
                                                             final Entity workstationChangeoverNorm) {
        Long workstationChangeoverNormId = workstationChangeoverNorm.getId();
        Entity workstationType = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION_TYPE);
        Entity workstation = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION);
        Entity attribute = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.ATTRIBUTE);
        String changeoverType = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.CHANGEOVER_TYPE);
        Entity fromAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
        Entity toAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);

        SearchCriteriaBuilder searchCriteriaBuilder = prepareWorkstationAndAttributeCriteria(workstationChangeoverNormDD, workstationType, workstation, attribute);

        searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.CHANGEOVER_TYPE, changeoverType));

        if (WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue().equals(changeoverType)) {
            searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE, WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE, JoinType.LEFT);
            searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE + L_DOT + L_ID, fromAttributeValue.getId()));
            searchCriteriaBuilder.createAlias(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE, WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE, JoinType.LEFT);
            searchCriteriaBuilder.add(SearchRestrictions.eq(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE + L_DOT + L_ID, toAttributeValue.getId()));
        }

        if (Objects.nonNull(workstationChangeoverNormId)) {
            searchCriteriaBuilder.add(SearchRestrictions.idNe(workstationChangeoverNormId));
        }

        if (Objects.nonNull(searchCriteriaBuilder.setMaxResults(1).uniqueResult())) {
            workstationChangeoverNorm.addGlobalError("technologies.workstationChangeoverNorm.error.isNotUnique");

            return false;
        }

        return true;
    }

    public void onSave(final DataDefinition workstationChangeoverNormDD, final Entity workstationChangeoverNorm) {
        String name = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.NAME);

        if (StringUtils.isEmpty(name)) {
            workstationChangeoverNorm.setField(WorkstationChangeoverNormFields.NAME, buildChangeoverName(workstationChangeoverNorm));
        }
    }

    private String buildChangeoverName(final Entity workstationChangeoverNorm) {
        StringBuilder nameBuilder = new StringBuilder();

        Entity workstationType = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION_TYPE);
        Entity workstation = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION);
        Entity attribute = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.ATTRIBUTE);
        String changeoverType = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.CHANGEOVER_TYPE);
        Entity fromAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
        Entity toAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);

        if ((Objects.nonNull(workstationType) || Objects.nonNull(workstation)) && Objects.nonNull(attribute)) {
            String workstationTypeOrWorkstationNumber = StringUtils.EMPTY;

            if (Objects.nonNull(workstationType)) {
                workstationTypeOrWorkstationNumber = workstationType.getStringField(WorkstationTypeFields.NUMBER);
            }
            if (Objects.nonNull(workstation)) {
                workstationTypeOrWorkstationNumber = workstation.getStringField(WorkstationFields.NUMBER);
            }

            String attributeNumber = attribute.getStringField(AttributeFields.NUMBER);

            nameBuilder.append(workstationTypeOrWorkstationNumber);
            nameBuilder.append(StringUtils.SPACE);
            nameBuilder.append(L_DASH);
            nameBuilder.append(StringUtils.SPACE);
            nameBuilder.append(attributeNumber);
            nameBuilder.append(StringUtils.SPACE);
            nameBuilder.append(L_DASH);
            nameBuilder.append(StringUtils.SPACE);

            if (WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue().equals(changeoverType)) {
                if (Objects.nonNull(fromAttributeValue) && Objects.nonNull(toAttributeValue)) {
                    String fromAttributeValueValue = fromAttributeValue.getStringField(AttributeValueFields.VALUE);
                    String toAttributeValueValue = toAttributeValue.getStringField(AttributeValueFields.VALUE);

                    nameBuilder.append(fromAttributeValueValue);
                    nameBuilder.append(StringUtils.SPACE);
                    nameBuilder.append(translationService.translate("technologies.workstationChangeoverNorm.fromTo", LocaleContextHolder.getLocale()));
                    nameBuilder.append(StringUtils.SPACE);
                    nameBuilder.append(toAttributeValueValue);
                }
            } else {
                nameBuilder.append(translationService.translate("technologies.workstationChangeoverNorm.anyChange", LocaleContextHolder.getLocale()));
            }
        }

        return nameBuilder.toString();
    }

}
