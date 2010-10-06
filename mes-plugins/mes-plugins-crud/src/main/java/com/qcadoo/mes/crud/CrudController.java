package com.qcadoo.mes.crud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.Component;
import com.qcadoo.mes.view.SaveableComponent;
import com.qcadoo.mes.view.SelectableComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewValue;

@Controller
public final class CrudController {

    private static final String VIEW_NAME_VARIABLE = "viewName";

    private static final String PLUGIN_IDENTIFIER_VARIABLE = "pluginIdentifier";

    private static final String CONTROLLER_PATH = "page/{" + PLUGIN_IDENTIFIER_VARIABLE + "}/{" + VIEW_NAME_VARIABLE + "}";

    private static final String JSON_BODY = "jsonBody";

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = CONTROLLER_PATH, method = RequestMethod.GET)
    public ModelAndView getView(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @RequestParam final Map<String, String> arguments,
            final Locale locale) {
        Map<String, String> translationsMap = translationService.getCommonsMessages(locale);

        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);
        viewDefinition.updateTranslations(translationsMap, locale);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("crudView");
        modelAndView.addObject("viewDefinition", viewDefinition);
        modelAndView.addObject("entityId", arguments.get("entityId"));
        modelAndView.addObject("contextEntityId", arguments.get("contextEntityId"));
        modelAndView.addObject("contextFieldName", arguments.get("contextFieldName"));
        modelAndView.addObject("translationsMap", translationsMap);

        addMessageToModel(arguments, modelAndView);

        return modelAndView;
    }

    @RequestMapping(value = CONTROLLER_PATH + "/dataUpdate", method = RequestMethod.POST)
    @ResponseBody
    public Object getDataUpdate(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @ModelAttribute(JSON_BODY) final StringBuilder body,
            final Locale locale) {
        // TODO masz remove me
        return getData(pluginIdentifier, viewName, body, locale);
    }

    @RequestMapping(value = CONTROLLER_PATH + "/data", method = RequestMethod.POST)
    @ResponseBody
    public Object getData(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @ModelAttribute(JSON_BODY) final StringBuilder body,
            final Locale locale) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        Entity entity = null;
        String componentName = "";
        ViewValue<Object> viewValue = null;

        if (body != null && StringUtils.hasText(body.toString())) {
            JSONObject jsonBody = getJsonBody(body);

            JSONObject jsonObject = getJsonObject(jsonBody);

            if (jsonObject != null) {
                viewValue = viewDefinition.castValue(selectedEntities, jsonObject);
            }

            componentName = getComponentName(jsonBody);

            String entityId = getJsonString(jsonBody, "entityId");

            if (entityId != null) {
                entity = viewDefinition.getDataDefinition().get(Long.parseLong(entityId));
            }
        }

        return viewDefinition.getValue(entity, selectedEntities, viewValue, componentName, false, locale);
    }

    @RequestMapping(value = CONTROLLER_PATH + "/save", method = RequestMethod.POST)
    @ResponseBody
    public Object performSave(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @ModelAttribute(JSON_BODY) final StringBuilder body,
            final Locale locale) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        JSONObject jsonBody = getJsonBody(body);
        JSONObject jsonObject = getJsonObject(jsonBody);

        String triggerComponentName = getComponentName(jsonBody);
        String contextFieldName = getJsonString(jsonBody, "contextFieldName");
        String contextEntityId = getJsonString(jsonBody, "contextEntityId");

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonObject);

        SaveableComponent component = (SaveableComponent) viewDefinition.lookupComponent(triggerComponentName);

        Entity entity = component.getSaveableEntity(viewValue);

        if (component.isRelatedToMainEntity() && StringUtils.hasText(contextFieldName) && StringUtils.hasText(contextEntityId)) {
            entity.setField(contextFieldName, Long.parseLong(contextEntityId));
        }

        entity = component.getDataDefinition().save(entity);

        selectedEntities.put(triggerComponentName, entity);

        ViewValue<Object> responseViewValue = viewDefinition.getValue(null, selectedEntities, viewValue, triggerComponentName,
                true, locale);

        responseViewValue.getComponent(viewDefinition.getRoot().getName()).addSuccessMessage(
                translationService.translate("commons.message.save", locale));

        return responseViewValue;
    }

    @RequestMapping(value = CONTROLLER_PATH + "/delete", method = RequestMethod.POST)
    @ResponseBody
    public Object performDelete(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @ModelAttribute(JSON_BODY) final StringBuilder body,
            final Locale locale) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        JSONObject jsonBody = getJsonBody(body);
        JSONObject jsonObject = getJsonObject(jsonBody);

        String triggerComponentName = getComponentName(jsonBody);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonObject);

        SelectableComponent component = (SelectableComponent) viewDefinition.lookupComponent(triggerComponentName);

        Long id = component.getSelectedEntityId(viewValue);

        if (id != null) {
            ((Component<?>) component).getDataDefinition().delete(id);
            selectedEntities.remove(triggerComponentName);
        }

        ViewValue<Object> responseViewValue = viewDefinition.getValue(null, selectedEntities, viewValue, triggerComponentName,
                true, locale);

        if (id != null) {
            responseViewValue.getComponent(viewDefinition.getRoot().getName()).addSuccessMessage(
                    translationService.translate("commons.message.delete", locale));
        }

        return responseViewValue;
    }

    @RequestMapping(value = CONTROLLER_PATH + "/move", method = RequestMethod.POST)
    @ResponseBody
    public Object performMove(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @ModelAttribute(JSON_BODY) final StringBuilder body,
            final Locale locale) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        JSONObject jsonBody = getJsonBody(body);
        JSONObject jsonObject = getJsonObject(jsonBody);

        String triggerComponentName = getComponentName(jsonBody);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonObject);

        Long id = null;

        String offset = getJsonString(jsonBody, "offset");

        if (offset != null) {
            SelectableComponent component = (SelectableComponent) viewDefinition.lookupComponent(triggerComponentName);

            id = component.getSelectedEntityId(viewValue);

            if (id != null) {
                ((Component<?>) component).getDataDefinition().move(id, Integer.valueOf(offset));
            }
        }

        ViewValue<Object> responseViewValue = viewDefinition.getValue(null, selectedEntities, viewValue, triggerComponentName,
                true, locale);

        if (id != null) {
            responseViewValue.getComponent(viewDefinition.getRoot().getName()).addSuccessMessage(
                    translationService.translate("commons.message.move", locale));
        }

        return responseViewValue;
    }

    private void addMessageToModel(final Map<String, String> arguments, final ModelAndView modelAndView) {
        if (arguments.get("message") != null) {
            modelAndView.addObject("message", arguments.get("message"));
            if (arguments.get("messageType") != null) {
                modelAndView.addObject("messageType", arguments.get("messageType"));
            } else {
                modelAndView.addObject("messageType", "info");
            }
        }
    }

    private String getComponentName(final JSONObject json) {
        String componentName = getJsonString(json, "componentName");
        if (componentName != null) {
            return componentName.replaceAll("-", ".");
        } else {
            return null;
        }
    }

    private String getJsonString(final JSONObject json, final String name) {
        try {
            if (!json.isNull(name)) {
                return json.getString(name);
            } else {
                return null;
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private JSONObject getJsonObject(final JSONObject json) {
        try {
            if (!json.isNull("data")) {
                return json.getJSONObject("data");
            } else {
                return null;
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private JSONObject getJsonBody(final StringBuilder body) {
        try {
            return new JSONObject(body.toString());
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @InitBinder(JSON_BODY)
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
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        }
        String body = stringBuilder.toString();
        ((StringBuilder) binder.getTarget()).append(body);
    }

}
