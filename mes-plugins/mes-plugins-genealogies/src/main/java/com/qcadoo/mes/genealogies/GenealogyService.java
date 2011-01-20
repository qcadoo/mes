package com.qcadoo.mes.genealogies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.awesomeDynamicList.AwesomeDynamicListState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;

@Service
public final class GenealogyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public void newBatch(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String[] args) {
        ((GridComponentState) viewDefinitionState.getComponentByReference("grid")).setSelectedEntityId(null);
    }

    public void showGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = ((FormComponentState) triggerState).getEntityId();

        if (orderId != null) {
            String url = "../page/genealogies/orderGenealogies.html?context={\"order.id\":\"" + orderId + "\",\"form.order\":\""
                    + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false);
        }
    }

    @SuppressWarnings({ "unchecked" })
    public void fillProductInComponents(final ViewDefinitionState state, final Locale locale) {
        FormComponentState orderForm = (FormComponentState) state.getComponentByReference("order");
        FormComponentState genealogyForm = (FormComponentState) state.getComponentByReference("form");
        AwesomeDynamicListState list = (AwesomeDynamicListState) state.getComponentByReference("productInComponentsList");

        Entity genealogy = null;
        List<Entity> existingProductInComponents = Collections.EMPTY_LIST;

        if (genealogyForm.getEntityId() != null) {
            genealogy = dataDefinitionService.get("genealogies", "genealogy").get(orderForm.getEntityId());
            existingProductInComponents = genealogy.getHasManyField("productInComponents");
        }

        Entity order = dataDefinitionService.get("products", "order").get(orderForm.getEntityId());
        Entity technology = order.getBelongsToField("technology");

        List<Entity> targetProductInComponents = Collections.emptyList();

        for (Entity operationComponent : technology.getHasManyField("operationComponents")) {
            for (Entity operationProductInComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                if ((Boolean) operationProductInComponent.getField("batchRequired")) {
                    targetProductInComponents.add(createGenealogyProductInComponent(genealogy, operationProductInComponent,
                            existingProductInComponents));
                }
            }
        }

        list.setFieldValue(targetProductInComponents);
    }

    private Entity createGenealogyProductInComponent(final Entity genealogy, final Entity operationProductInComponent,
            final List<Entity> existingProductInComponents) {
        for (Entity e : existingProductInComponents) {
            if (e.getBelongsToField("productInComponent").getId().equals(operationProductInComponent.getId())) {
                return operationProductInComponent;
            }
        }
        Entity genealogyProductInComponent = new DefaultEntity("genealogies", "genealogyProductInComponent");
        genealogyProductInComponent.setField("genealogy", genealogy);
        genealogyProductInComponent.setField("productInComponent", operationProductInComponent);
        genealogyProductInComponent.setField("batch", new ArrayList<Entity>());
        return genealogyProductInComponent;
    }

    // public void addProductInToForm(final ViewDefinition viewDefinition, final JSONObject jsonObject, final Locale locale) {
    // Long orderId = 1950L;
    //
    // dataDefinitionService.get("products", "order").get(orderId);
    // Entity technology = order.getBelongsToField("technology");
    // Entity product = order.getBelongsToField("product");
    // List<Entity> operationProductInComponents = new ArrayList<Entity>();
    // for (Entity operationComponent : technology.getHasManyField("operationComponents")) {
    // for (Entity operationProductInComponent : operationComponent.getHasManyField("operationProductInComponents")) {
    // if ((Boolean) operationProductInComponent.getField("batchRequired")) {
    // operationProductInComponents.add(operationProductInComponent);
    // }
    // }
    // }
    // GridLayoutPattern layout = (GridLayoutPattern) viewDefinition.getComponentByReference("layout");
    // for (int i = 0; i < 3; i++) {
    // layout.getCells()[i][0].setRightBorder(false);
    // }
    // BorderLayoutPattern borderLayout = createBorderLayout("featureBorderLayout",
    // "Wyprodukowano przy następujących atrybutach:", layout, viewDefinition); // TODO
    // layout.getCells()[1][0].addComponent(borderLayout);
    // borderLayout.initialize();
    // if (hasShiftFeatures) {
    // borderLayout.addChild(createFeature("shiftFeatures", layout, viewDefinition));
    // }
    // if (hasPostFeatures) {
    // borderLayout.addChild(createFeature("postFeatures", layout, viewDefinition));
    // }
    // if (hasOtherFeatures) {
    // borderLayout.addChild(createFeature("otherFeatures", layout, viewDefinition));
    // }
    // for (Entity productInComponent : operationProductInComponents) {
    // layout.getCells()[2][0].addComponent(createProductIn(productInComponent, layout, viewDefinition));
    // }
    // }
    //
    // public void addBelongsToEntityToProductIn(final ViewDefinitionState state, final Locale locale) {
    // GridComponentState grid = (GridComponentState) state.getComponentByReference("grid");
    // ((FormComponentState) state.getComponentByReference("productIn37274Form")).setEntityId(1L);
    // ((FormComponentState) state.getComponentByReference("productIn37285Form")).setEntityId(2L);
    // ((FormComponentState) state.getComponentByReference("productIn37292Form")).setEntityId(3L);
    // ((FormComponentState) state.getComponentByReference("productIn37302Form")).setEntityId(4L);
    // }
    //
    // private ComponentPattern createProductIn(final Entity productInComponent, final GridLayoutPattern parent,
    // final ViewDefinition viewDefinition) {
    // BorderLayoutPattern layout = createBorderLayout("productIn" + productInComponent.getId() + "BorderLayout",
    // "Wyprodukowano z " + productInComponent.getId() + " użuwając następujących numerów partii:", parent,
    // viewDefinition); // TODO
    // ComponentDefinition formComponentDefinition = new ComponentDefinition();
    // formComponentDefinition.setName("productIn" + productInComponent.getId() + "Form");
    // formComponentDefinition.setReference("productIn" + productInComponent.getId() + "Form");
    // formComponentDefinition.setSourceFieldPath("#{form}.productInComponents");
    // formComponentDefinition.setParent(layout);
    // formComponentDefinition.setTranslationService(translationService);
    // formComponentDefinition.setViewDefinition(viewDefinition);
    // FormComponentPattern mainForm = new FormComponentPattern(formComponentDefinition);
    // ComponentDefinition listComponentDefinition = new ComponentDefinition();
    // listComponentDefinition.setName("productIn" + productInComponent.getId() + "List");
    // listComponentDefinition.setFieldPath("batch");
    // listComponentDefinition.setParent(mainForm);
    // listComponentDefinition.setTranslationService(translationService);
    // listComponentDefinition.setViewDefinition(viewDefinition);
    // AwesomeDynamicListPattern list = new AwesomeDynamicListPattern(listComponentDefinition);
    // FormComponentPattern form = list.getFormComponentPattern();
    // ComponentDefinition gridComponentDefinition = new ComponentDefinition();
    // gridComponentDefinition.setName("productIn" + productInComponent.getId() + "GridLayoutForm");
    // gridComponentDefinition.setParent(form);
    // gridComponentDefinition.setTranslationService(translationService);
    // gridComponentDefinition.setViewDefinition(viewDefinition);
    // GridLayoutPattern gridLayout = new GridLayoutPattern(gridComponentDefinition);
    // gridLayout.createCells(1, 1);
    // ComponentDefinition componentDefinition = new ComponentDefinition();
    // componentDefinition.setName("batch");
    // componentDefinition.setFieldPath("batch");
    // componentDefinition.setParent(gridLayout);
    // componentDefinition.setTranslationService(translationService);
    // componentDefinition.setViewDefinition(viewDefinition);
    // ComponentPattern batch = new TextInputComponentPattern(componentDefinition);
    // mainForm.initialize();
    // layout.addChild(mainForm);
    // batch.initialize();
    // gridLayout.addChild(batch);
    // gridLayout.getCells()[0][0].addComponent(batch);
    // layout.initialize();
    // form.addChild(gridLayout);
    // list.initialize();
    // mainForm.addChild(list);
    // return layout;
    // }
    //
    // private ComponentPattern createFeature(final String name, final GridLayoutPattern parent, final ViewDefinition
    // viewDefinition) {
    // ComponentDefinition listComponentDefinition = new ComponentDefinition();
    // listComponentDefinition.setName(name + "Form");
    // listComponentDefinition.setFieldPath("#{form}." + name);
    // listComponentDefinition.setParent(parent);
    // listComponentDefinition.setTranslationService(translationService);
    // listComponentDefinition.setViewDefinition(viewDefinition);
    // AwesomeDynamicListPattern list = new AwesomeDynamicListPattern(listComponentDefinition);
    // FormComponentPattern form = list.getFormComponentPattern();
    // ComponentDefinition gridComponentDefinition = new ComponentDefinition();
    // gridComponentDefinition.setName(name + "GridLayoutForm");
    // gridComponentDefinition.setParent(form);
    // gridComponentDefinition.setTranslationService(translationService);
    // gridComponentDefinition.setViewDefinition(viewDefinition);
    // GridLayoutPattern gridLayout = new GridLayoutPattern(gridComponentDefinition);
    // gridLayout.createCells(1, 1);
    // ComponentDefinition componentDefinition = new ComponentDefinition();
    // componentDefinition.setName("value");
    // componentDefinition.setFieldPath("value");
    // componentDefinition.setParent(gridLayout);
    // componentDefinition.setTranslationService(translationService);
    // componentDefinition.setViewDefinition(viewDefinition);
    // ComponentPattern batch = new TextInputComponentPattern(componentDefinition);
    // batch.initialize();
    // gridLayout.addChild(batch);
    // gridLayout.getCells()[0][0].addComponent(batch);
    // gridLayout.initialize();
    // form.addChild(gridLayout);
    // list.initialize();
    // return list;
    // }
    //
    // private BorderLayoutPattern createBorderLayout(final String name, final String label, final GridLayoutPattern parent,
    // final ViewDefinition viewDefinition) {
    // ComponentDefinition layoutComponentDefinition = new ComponentDefinition();
    // layoutComponentDefinition.setName(name);
    // layoutComponentDefinition.setParent(parent);
    // layoutComponentDefinition.setTranslationService(translationService);
    // layoutComponentDefinition.setViewDefinition(viewDefinition);
    // BorderLayoutPattern layout = new BorderLayoutPattern(layoutComponentDefinition);
    // layout.addOption(new ComponentOption("label", ImmutableMap.of("value", label)));
    // layout.initialize();
    // parent.addChild(layout);
    // return layout;
    // }
    //
    // private JSONObject getJsonObject(JSONObject jsonObject, final String... path) throws JSONException {
    // for (String p : path) {
    // if (!jsonObject.has(p)) {
    // return null;
    // }
    // jsonObject = jsonObject.getJSONObject(p);
    // }
    // return jsonObject;
    // }

}
