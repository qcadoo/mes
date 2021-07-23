package com.qcadoo.mes.technologies.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.ModifyTechnologyHelperFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModifyTechnologyDetailsHooks {

    private static final String L_MODIFY_TECHNOLOGY_ADD_PRODUCTS = "modifyTechnologyAddProducts";

    private static final String PRODUCT = "product";

    private static final String L_REPLACE_PRODUCT_UNIT = "replaceProductUnit";



    private static final String L_MAIN_PRODUCT = "product";

    private static final String L_QUANTITY = "quantity";

    private static final String L_UNIT = "unit";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (view.isViewAfterRedirect()) {
            Entity mt = form.getEntity().getDataDefinition().get(form.getEntityId());

            LookupComponent replaceProductLookup = (LookupComponent) view.getComponentByReference(ModifyTechnologyHelperFields.REPLACE_PRODUCT);
            replaceProductLookup.setFieldValue(mt.getBelongsToField(L_MAIN_PRODUCT).getId());
            replaceProductLookup.requestComponentUpdateState();

            String selectedEntities = mt.getStringField(ModifyTechnologyHelperFields.SELECTED_ENTITIES);
            List<Long> ids = Lists.newArrayList(selectedEntities.split(",")).stream().map(Long::valueOf)
                    .collect(Collectors.toList());

            boolean sizeProduct = mt.getBooleanField("sizeProduct");
            if(sizeProduct) {
                List<Entity> producstBySize = dataDefinitionService
                        .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_BY_SIZE_GROUP)
                        .find().add(SearchRestrictions.in(L_ID, ids)).list().getEntities();

                Set<BigDecimal> quantities = producstBySize.stream().map(op -> op.getDecimalField(L_QUANTITY)).collect(Collectors.toSet());

                if(quantities.size() == 1) {
                    FieldComponent qnt = (FieldComponent) view.getComponentByReference(ModifyTechnologyHelperFields.REPLACE_PRODUCT_QUANTITY);
                    qnt.setFieldValue(quantities.stream().findFirst().get());
                    qnt.requestComponentUpdateState();
                }
            } else {
                List<Entity> opicDtos = dataDefinitionService
                        .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT_DTO)
                        .find().add(SearchRestrictions.in(L_ID, ids)).list().getEntities();

                Set<BigDecimal> quantities = opicDtos.stream().map(op -> op.getDecimalField(L_QUANTITY)).collect(Collectors.toSet());

                if(quantities.size() == 1) {
                    FieldComponent qnt = (FieldComponent) view.getComponentByReference(ModifyTechnologyHelperFields.REPLACE_PRODUCT_QUANTITY);
                    qnt.setFieldValue(quantities.stream().findFirst().get());
                    qnt.requestComponentUpdateState();
                }
            }


            FieldComponent replaceProductUnit = (FieldComponent) view.getComponentByReference(L_REPLACE_PRODUCT_UNIT);
            replaceProductUnit.setFieldValue(mt.getBelongsToField(L_MAIN_PRODUCT).getStringField(ProductFields.UNIT));
        }
        setForm(view);
    }

    private void setForm(final ViewDefinitionState view) {
        configureAddPart(view);
        configureReplacePart(view);

    }

    private void configureReplacePart(ViewDefinitionState view) {
        CheckBoxComponent replaceCheckBox = (CheckBoxComponent) view.getComponentByReference(ModifyTechnologyHelperFields.REPLACE);
        LookupComponent replaceProductLookup = (LookupComponent) view.getComponentByReference(ModifyTechnologyHelperFields.REPLACE_PRODUCT);
        FieldComponent replaceProductQuantityComponent = (FieldComponent) view
                .getComponentByReference(ModifyTechnologyHelperFields.REPLACE_PRODUCT_QUANTITY);
        if (replaceCheckBox.isChecked()) {
            replaceProductLookup.setEnabled(true);
            replaceProductQuantityComponent.setEnabled(true);
            FieldComponent replaceProductUnit = (FieldComponent) view.getComponentByReference(L_REPLACE_PRODUCT_UNIT);
            if (Objects.nonNull(replaceProductLookup.getFieldValue())) {
                replaceProductUnit.setFieldValue(replaceProductLookup.getEntity().getStringField(ProductFields.UNIT));
            }
        } else {
            replaceProductLookup.setEnabled(false);
            replaceProductQuantityComponent.setEnabled(false);
        }
    }

    private void configureAddPart(ViewDefinitionState view) {
        CheckBoxComponent addNewCheckBox = (CheckBoxComponent) view.getComponentByReference(ModifyTechnologyHelperFields.ADD_NEW);
        AwesomeDynamicListComponent addProductAdl = (AwesomeDynamicListComponent) view
                .getComponentByReference(L_MODIFY_TECHNOLOGY_ADD_PRODUCTS);
        if (addNewCheckBox.isChecked()) {
            addProductAdl.setEnabled(true);
            addProductAdl.getFormComponents().forEach(fc -> {
                fc.setFormEnabled(true);
                Entity addEntity = fc.getPersistedEntityWithIncludedFormValues();
                Entity product = addEntity.getBelongsToField(PRODUCT);
                if (Objects.nonNull(product)) {
                    addEntity.setField(L_UNIT, product.getStringField(ProductFields.UNIT));
                    fc.setEntity(addEntity);
                }
            });
        } else {
            addProductAdl.setEnabled(false);
            addProductAdl.getFormComponents().forEach(fc -> fc.setFormEnabled(false));
        }
    }
}
