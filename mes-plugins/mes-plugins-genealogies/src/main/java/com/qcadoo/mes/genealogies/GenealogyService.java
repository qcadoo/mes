package com.qcadoo.mes.genealogies;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.TextInputComponentPattern;
import com.qcadoo.mes.view.components.awesomeDynamicList.AwesomeDynamicListPattern;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.components.layout.BorderLayoutPattern;
import com.qcadoo.mes.view.components.layout.GridLayoutPattern;

@Service
public final class GenealogyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public void addProductInToForm(final ViewDefinition viewDefinition, final JSONObject jsonObject, final Locale locale) {
        Long orderId = 1950L; // TODO

        // try {
        // if (jsonObject.has("window.product.id") && !jsonObject.isNull("window.product.id")) {
        // id = jsonObject.getLong("window.product.id");
        // } else {
        // JSONObject jsonObject1 = getJsonObject(jsonObject, "components", "window", "components", "product", "context");
        // if (jsonObject1 != null && jsonObject1.has("id") && !jsonObject1.isNull("id")) {
        // id = jsonObject1.getLong("id");
        // }
        // } catch (JSONException e) {
        // throw new IllegalStateException(e.getMessage(), e);
        // }

        boolean hasShiftFeatures = true; // TODO
        boolean hasPostFeatures = false; // TODO
        boolean hasOtherFeatures = true; // TODO

        Entity order = dataDefinitionService.get("products", "order").get(orderId);
        Entity technology = order.getBelongsToField("technology");
        Entity product = order.getBelongsToField("product");

        List<Entity> operationProductInComponents = new ArrayList<Entity>();

        for (Entity operationComponent : technology.getHasManyField("operationComponents")) {
            for (Entity operationProductInComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                if ((Boolean) operationProductInComponent.getField("batchRequired")) {
                    operationProductInComponents.add(operationProductInComponent);
                }
            }
        }

        GridLayoutPattern layout = (GridLayoutPattern) viewDefinition.getComponentByReference("layout");

        for (int i = 0; i < 3; i++) {
            layout.getCells()[i][0].setRightBorder(false);
        }

        BorderLayoutPattern borderLayout = createBorderLayout("featureBorderLayout",
                "Wyprodukowano przy następujących atrybutach:", layout, viewDefinition); // TODO
        layout.getCells()[1][0].addComponent(borderLayout);
        borderLayout.initialize();

        if (hasShiftFeatures) {
            borderLayout.addChild(createFeature("shiftFeatures", layout, viewDefinition));
        }

        if (hasPostFeatures) {
            borderLayout.addChild(createFeature("postFeatures", layout, viewDefinition));
        }

        if (hasOtherFeatures) {
            borderLayout.addChild(createFeature("otherFeatures", layout, viewDefinition));
        }

        for (Entity productInComponent : operationProductInComponents) {
            layout.getCells()[2][0].addComponent(createProductIn(productInComponent, layout, viewDefinition));
        }
    }

    public void addBelongsToEntityToProductIn(final ViewDefinitionState state, final Locale locale) {
        GridComponentState grid = (GridComponentState) state.getComponentByReference("grid");
        ((FormComponentState) state.getComponentByReference("productIn37274Form")).setEntityId(1L);
        ((FormComponentState) state.getComponentByReference("productIn37285Form")).setEntityId(2L);
        ((FormComponentState) state.getComponentByReference("productIn37292Form")).setEntityId(3L);
        ((FormComponentState) state.getComponentByReference("productIn37302Form")).setEntityId(4L);
    }

    private ComponentPattern createProductIn(final Entity productInComponent, final GridLayoutPattern parent,
            final ViewDefinition viewDefinition) {
        BorderLayoutPattern layout = createBorderLayout("productIn" + productInComponent.getId() + "BorderLayout",
                "Wyprodukowano z " + productInComponent.getId() + " użuwając następujących numerów partii:", parent,
                viewDefinition); // TODO

        ComponentDefinition formComponentDefinition = new ComponentDefinition();
        formComponentDefinition.setName("productIn" + productInComponent.getId() + "Form");
        formComponentDefinition.setReference("productIn" + productInComponent.getId() + "Form");
        formComponentDefinition.setSourceFieldPath("#{form}.productInComponents");
        formComponentDefinition.setParent(layout);
        formComponentDefinition.setTranslationService(translationService);
        formComponentDefinition.setViewDefinition(viewDefinition);

        FormComponentPattern mainForm = new FormComponentPattern(formComponentDefinition);

        ComponentDefinition listComponentDefinition = new ComponentDefinition();
        listComponentDefinition.setName("productIn" + productInComponent.getId() + "List");
        listComponentDefinition.setFieldPath("batch");
        listComponentDefinition.setParent(mainForm);
        listComponentDefinition.setTranslationService(translationService);
        listComponentDefinition.setViewDefinition(viewDefinition);

        AwesomeDynamicListPattern list = new AwesomeDynamicListPattern(listComponentDefinition);

        FormComponentPattern form = list.getFormComponentPattern();

        ComponentDefinition gridComponentDefinition = new ComponentDefinition();
        gridComponentDefinition.setName("productIn" + productInComponent.getId() + "GridLayoutForm");
        gridComponentDefinition.setParent(form);
        gridComponentDefinition.setTranslationService(translationService);
        gridComponentDefinition.setViewDefinition(viewDefinition);

        GridLayoutPattern gridLayout = new GridLayoutPattern(gridComponentDefinition);
        gridLayout.createCells(1, 1);

        ComponentDefinition componentDefinition = new ComponentDefinition();
        componentDefinition.setName("batch");
        componentDefinition.setFieldPath("batch");
        componentDefinition.setParent(gridLayout);
        componentDefinition.setTranslationService(translationService);
        componentDefinition.setViewDefinition(viewDefinition);

        ComponentPattern batch = new TextInputComponentPattern(componentDefinition);

        mainForm.initialize();
        layout.addChild(mainForm);

        batch.initialize();
        gridLayout.addChild(batch);
        gridLayout.getCells()[0][0].addComponent(batch);

        layout.initialize();
        form.addChild(gridLayout);

        list.initialize();
        mainForm.addChild(list);

        return layout;
    }

    private ComponentPattern createFeature(final String name, final GridLayoutPattern parent, final ViewDefinition viewDefinition) {
        ComponentDefinition listComponentDefinition = new ComponentDefinition();
        listComponentDefinition.setName(name + "Form");
        listComponentDefinition.setFieldPath("#{form}." + name);
        listComponentDefinition.setParent(parent);
        listComponentDefinition.setTranslationService(translationService);
        listComponentDefinition.setViewDefinition(viewDefinition);

        AwesomeDynamicListPattern list = new AwesomeDynamicListPattern(listComponentDefinition);

        FormComponentPattern form = list.getFormComponentPattern();

        ComponentDefinition gridComponentDefinition = new ComponentDefinition();
        gridComponentDefinition.setName(name + "GridLayoutForm");
        gridComponentDefinition.setParent(form);
        gridComponentDefinition.setTranslationService(translationService);
        gridComponentDefinition.setViewDefinition(viewDefinition);

        GridLayoutPattern gridLayout = new GridLayoutPattern(gridComponentDefinition);
        gridLayout.createCells(1, 1);

        ComponentDefinition componentDefinition = new ComponentDefinition();
        componentDefinition.setName("value");
        componentDefinition.setFieldPath("value");
        componentDefinition.setParent(gridLayout);
        componentDefinition.setTranslationService(translationService);
        componentDefinition.setViewDefinition(viewDefinition);

        ComponentPattern batch = new TextInputComponentPattern(componentDefinition);

        batch.initialize();
        gridLayout.addChild(batch);
        gridLayout.getCells()[0][0].addComponent(batch);

        gridLayout.initialize();
        form.addChild(gridLayout);

        list.initialize();

        return list;
    }

    private BorderLayoutPattern createBorderLayout(final String name, final String label, final GridLayoutPattern parent,
            final ViewDefinition viewDefinition) {
        ComponentDefinition layoutComponentDefinition = new ComponentDefinition();
        layoutComponentDefinition.setName(name);
        layoutComponentDefinition.setParent(parent);
        layoutComponentDefinition.setTranslationService(translationService);
        layoutComponentDefinition.setViewDefinition(viewDefinition);

        BorderLayoutPattern layout = new BorderLayoutPattern(layoutComponentDefinition);
        layout.addOption(new ComponentOption("label", ImmutableMap.of("value", label)));
        layout.initialize();
        parent.addChild(layout);

        return layout;
    }

    private JSONObject getJsonObject(JSONObject jsonObject, final String... path) throws JSONException {
        for (String p : path) {
            if (!jsonObject.has(p)) {
                return null;
            }

            jsonObject = jsonObject.getJSONObject(p);
        }

        return jsonObject;
    }

}
