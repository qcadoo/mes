package com.qcadoo.mes.crud.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.api.TranslationService;
import com.qcadoo.mes.core.api.ViewDefinitionService;
import com.qcadoo.mes.core.view.SaveableComponent;
import com.qcadoo.mes.core.view.ViewDefinition;
import com.qcadoo.mes.core.view.ViewValue;
import com.qcadoo.mes.core.view.elements.GridComponent;
import com.qcadoo.mes.core.view.elements.grid.ListData;

@Controller
public final class CrudController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "page/{viewName}", method = RequestMethod.GET)
    public ModelAndView getView(@PathVariable("viewName") final String viewName,
            @RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("crudView");

        Map<String, String> translationsMap = translationService.getCommonsTranslations(locale);

        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        mav.addObject("viewDefinition", viewDefinition);
        // translationService.updateTranslationsForViewDefinition(viewDefinition, translationsMap, locale);

        Map<String, Map<String, String>> dictionaryValues = new HashMap<String, Map<String, String>>();

        // CrudControllerUtils.generateJsonViewElementOptions(viewDefinition, viewElementsOptionsJson, null);

        viewDefinition.addComponentTranslations(translationsMap, translationService, locale);

        // for (CastableComponent component : viewDefinition.getRoot().getComponents().values()) {
        // viewElementsOptionsJson.put(component.getName(), CrudControllerUtils.generateJsonViewElementOptions(component));

        //
        // if (viewElement instanceof FormDefinition) {
        // FormDefinition form = (FormDefinition) viewElement;
        // for (FormFieldDefinition fieldDefEntry : form.getFields()) {
        // if (fieldDefEntry.getDataField().getType() instanceof BelongsToType) {
        // BelongsToType belongsToField = (BelongsToType) fieldDefEntry.getDataField().getType();
        // Map<Long, String> options = belongsToField.lookup(null);
        // Map<String, String> fieldOptionsMap = new HashMap<String, String>();
        // for (Map.Entry<Long, String> option : options.entrySet()) {
        // fieldOptionsMap.put(Long.toString(option.getKey()), option.getValue());
        // }
        // dictionaryValues.put(fieldDefEntry.getDataField().getName(), fieldOptionsMap);
        // } else if (fieldDefEntry.getDataField().getType() instanceof EnumeratedFieldType) {
        // EnumeratedFieldType enumeratedField = (EnumeratedFieldType) fieldDefEntry.getDataField().getType();
        // List<String> options = enumeratedField.values();
        // Map<String, String> fieldOptionsMap = new HashMap<String, String>();
        // for (String option : options) {
        // fieldOptionsMap.put(option, option);
        // }
        // dictionaryValues.put(fieldDefEntry.getDataField().getName(), fieldOptionsMap);
        // }
        // }
        // }
        // }
        mav.addObject("dictionaryValues", dictionaryValues);

        mav.addObject("entityId", arguments.get("entityId"));
        mav.addObject("contextEntityId", arguments.get("contextEntityId"));

        mav.addObject("translationsMap", translationsMap);

        return mav;
    }

    @RequestMapping(value = "page/{viewName}/data", method = RequestMethod.GET)
    @ResponseBody
    public Object getData(@PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments) {
        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        if (arguments.get("entityId") != null) {
            Entity entity = viewDefinition.getRoot().getDataDefinition().get(Long.parseLong(arguments.get("entityId")));
            return viewDefinition.getValue(entity, new HashMap<String, Entity>(), null, null, "");
        } else {
            return viewDefinition.getValue(null, new HashMap<String, Entity>(), null, null, "");
        }
    }

    @InitBinder("jsonBody")
    public void initBinder(final WebDataBinder binder, final HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            // throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    // throw ex;
                }
            }
        }
        String body = stringBuilder.toString();
        ((StringBuilder) binder.getTarget()).append(body);
    }

    @RequestMapping(value = "page/{viewName}/dataUpdate", method = RequestMethod.POST)
    @ResponseBody
    public Object getDataUpdate(@PathVariable("viewName") final String viewName,
            @RequestParam final Map<String, String> arguments, @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);

        try {
            JSONObject jsonBody = new JSONObject(body.toString());
            String componentName = jsonBody.getString("componentName").replaceAll("-", ".");
            JSONObject jsonValues = jsonBody.getJSONObject("data");

            Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

            Set<String> pathsToUpdate = viewDefinition.getRoot().lookupListeners(componentName);

            ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonValues);

            viewDefinition.cleanSelectedEntities(selectedEntities, pathsToUpdate);

            ViewValue<Object> newViewValue = viewDefinition.getValue(null, selectedEntities, viewValue, pathsToUpdate,
                    componentName);

            return newViewValue;
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "page/{viewName}/save", method = RequestMethod.POST)
    @ResponseBody
    public Object performSave(@PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);

        try {
            JSONObject jsonBody = new JSONObject(body.toString());
            String componentName = jsonBody.getString("componentName").replaceAll("-", ".");
            JSONObject jsonValues = jsonBody.getJSONObject("data");

            Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

            Set<String> pathsToUpdate = viewDefinition.getRoot().lookupListeners(componentName);
            pathsToUpdate.add(componentName);

            ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonValues);

            SaveableComponent saveableComponent = (SaveableComponent) viewDefinition.getRoot().lookupComponent(componentName);

            Entity entity = saveableComponent.getSaveableEntity(viewValue);

            entity = saveableComponent.getDataDefinition().save(entity);

            viewDefinition.cleanSelectedEntities(selectedEntities, pathsToUpdate);

            selectedEntities.put(componentName, entity);

            return viewDefinition.getValue(null, selectedEntities, viewValue, pathsToUpdate, componentName);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "page/{viewName}/delete", method = RequestMethod.POST)
    @ResponseBody
    public Object performDelete(@PathVariable("viewName") final String viewName,
            @RequestParam final Map<String, String> arguments, @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);

        try {
            JSONObject jsonBody = new JSONObject(body.toString());
            String componentName = jsonBody.getString("componentName").replaceAll("-", ".");
            JSONObject jsonValues = jsonBody.getJSONObject("data");

            Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

            Set<String> pathsToUpdate = viewDefinition.getRoot().lookupListeners(componentName);
            pathsToUpdate.add(componentName);

            ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonValues);

            GridComponent gridComponent = (GridComponent) viewDefinition.getRoot().lookupComponent(componentName);

            ViewValue<ListData> gridValue = (ViewValue<ListData>) viewValue.lookupValue(componentName);

            gridComponent.getDataDefinition().delete(gridValue.getValue().getSelectedEntityId());

            viewDefinition.cleanSelectedEntities(selectedEntities, pathsToUpdate);

            return viewDefinition.getValue(null, selectedEntities, viewValue, pathsToUpdate, componentName);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // @RequestMapping(value = "page/{viewName}/dataUpdate", method = RequestMethod.GET)
    // @ResponseBody
    // public Object getDataUpdate(@PathVariable("viewName") final String viewName, @RequestParam final Map<String, String>
    // arguments) {
    // ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
    //
    // Map<String, String> argComponents = new HashMap<String, String>();
    // for (Entry<String, String> argEntry : arguments.entrySet()) {
    // if ("comp-".equals(argEntry.getKey().substring(0, 5))) {
    // argComponents.put(argEntry.getKey().substring(5), argEntry.getValue());
    // }
    // }
    //
    // return null; // TODO viewDefinition.getUpdateValues(argComponents);
    // }

    // @RequestMapping(value = "page/{viewName}/{elementName}/list", method = RequestMethod.GET)
    // @ResponseBody
    // public ListData getGridData(@PathVariable("viewName") final String viewName,
    // @PathVariable("elementName") final String elementName, @RequestParam final Map<String, String> arguments) {

    // ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
    // ComponentDefinition element = viewDefinition.getElementByName(elementName);
    //
    // GridDefinition gridDefinition = (GridDefinition) element;
    // DataDefinition dataDefinition = gridDefinition.getDataDefinition();
    //
    // SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(dataDefinition);
    //
    // if (arguments.get("entityId") != null && gridDefinition.getParent() != null) {
    // Long parentId = Long.parseLong(arguments.get("entityId"));
    // DataFieldDefinition parentField = dataDefinition.getField(gridDefinition.getParentField());
    // searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(parentField, parentId));
    // }
    // if (arguments.get("maxResults") != null) {
    // int maxResults = Integer.parseInt(arguments.get("maxResults"));
    // checkArgument(maxResults >= 0, "Max results must be greater or equals 0");
    // searchCriteriaBuilder = searchCriteriaBuilder.withMaxResults(maxResults);
    // }
    // if (arguments.get("firstResult") != null) {
    // int firstResult = Integer.parseInt(arguments.get("firstResult"));
    // checkArgument(firstResult >= 0, "First result must be greater or equals 0");
    // searchCriteriaBuilder = searchCriteriaBuilder.withFirstResult(firstResult);
    // }
    // if (arguments.get("sortField") != null && arguments.get("sortOrder") != null) {
    // if ("desc".equals(arguments.get("sortOrder"))) {
    // searchCriteriaBuilder = searchCriteriaBuilder.orderBy(Order.desc(arguments.get("sortField")));
    // } else {
    // searchCriteriaBuilder = searchCriteriaBuilder.orderBy(Order.asc(arguments.get("sortField")));
    // }
    // }
    //
    // for (int i = 0;; i++) {
    // if (arguments.get("filterObject[" + i + "][fieldName]") == null) {
    // break;
    // }
    // String fieldName = arguments.get("filterObject[" + i + "][fieldName]");
    // String operator = arguments.get("filterObject[" + i + "][operator]");
    // String value = arguments.get("filterObject[" + i + "][filterValue]");
    //
    // DataFieldDefinition field = dataDefinition.getField(fieldName);
    // if ("=".equals(operator)) {
    // searchCriteriaBuilder.restrictedWith(Restrictions.eq(field, value));
    // } else if ("<".equals(operator)) {
    // searchCriteriaBuilder.restrictedWith(Restrictions.lt(field, value));
    // } else if (">".equals(operator)) {
    // searchCriteriaBuilder.restrictedWith(Restrictions.gt(field, value));
    // } else if ("<=".equals(operator)) {
    // searchCriteriaBuilder.restrictedWith(Restrictions.le(field, value));
    // } else if (">=".equals(operator)) {
    // searchCriteriaBuilder.restrictedWith(Restrictions.ge(field, value));
    // } else if ("<>".equals(operator)) {
    // searchCriteriaBuilder.restrictedWith(Restrictions.ne(field, value));
    // } else if ("null".equals(operator)) {
    // searchCriteriaBuilder.restrictedWith(Restrictions.isNull(field));
    // } else if ("not null".equals(operator)) {
    // searchCriteriaBuilder.restrictedWith(Restrictions.isNotNull(field));
    // }
    // }
    //
    // SearchCriteria searchCriteria = searchCriteriaBuilder.build();
    //
    // SearchResult rs = dataAccessService.find(searchCriteria);
    //
    // return ListDataUtils.generateListData(rs, gridDefinition);
    // return null;
    // }

    @RequestMapping(value = "page/{viewName}/{elementName}/entity", method = RequestMethod.GET)
    @ResponseBody
    public Entity getEntityData(@PathVariable("viewName") final String viewName,
            @PathVariable("elementName") final String elementName, @RequestParam final Map<String, String> arguments) {

        // ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        // ComponentDefinition element = viewDefinition.getElementByName(elementName);
        //
        // Entity entity = dataAccessService.get(element.getDataDefinition(), Long.parseLong(arguments.get("entityId")));
        //
        // return EntityDataUtils.generateEntityData(entity, element.getDataDefinition());
        return null;
    }

    // @RequestMapping(value = "page/{viewName}/{elementName}/save", method = RequestMethod.POST)
    // @ResponseBody
    // public Entity saveEntity(@PathVariable("viewName") final String viewName,
    // @PathVariable("elementName") final String elementName, @ModelAttribute final Entity entity, final Locale locale) {
    // ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
    // ComponentDefinition element = viewDefinition.getElementByName(elementName);
    //
    // ValidationResults validationResult = dataAccessService.save(element.getDataDefinition(), entity);
    //
    // translationService.translateValidationResults(validationResult, locale);
    //
    // return EntityDataUtils.generateValidationResultWithEntityData(validationResult, element.getDataDefinition());
    // return null;
    // }

    // @RequestMapping(value = "page/{viewName}/{elementName}/delete", method = RequestMethod.POST)
    // @ResponseBody
    // public String deleteEntities(@PathVariable("viewName") final String viewName,
    // @PathVariable("elementName") final String elementName, @RequestBody final List<Integer> selectedRows) {
    // ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
    // ComponentDefinition element = viewDefinition.getElementByName(elementName);
    //
    // if (selectedRows.size() > 0) {
    // Long[] entitiesId = new Long[selectedRows.size()];
    // int i = 0;
    // for (Integer selectedRowId : selectedRows) {
    // entitiesId[i++] = Long.valueOf(selectedRowId);
    // }
    // dataAccessService.delete(element.getDataDefinition(), entitiesId);
    // }
    // return "ok";
    // }

    // @RequestMapping(value = "page/{viewName}/{elementName}/move", method = RequestMethod.POST)
    // @ResponseBody
    // public String moveEntities(@PathVariable("viewName") final String viewName,
    // @PathVariable("elementName") final String elementName, @RequestParam final Integer entityId,
    // @RequestParam final Integer direction) {
    // ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
    // ComponentDefinition element = viewDefinition.getElementByName(elementName);
    //
    // dataAccessService.move(element.getDataDefinition(), Long.valueOf(entityId), direction);

    // return "ok";
    // }
}
