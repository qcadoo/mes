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

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}", method = RequestMethod.GET)
    public ModelAndView getView(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            final Locale locale) {
        Map<String, String> translationsMap = translationService.getCommonsTranslations(locale);

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

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/data", method = RequestMethod.GET)
    @ResponseBody
    public Object getData(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        Entity entity = null;

        if (arguments.get("entityId") != null) {
            entity = viewDefinition.getDataDefinition().get(Long.parseLong(arguments.get("entityId")));
        }

        return viewDefinition.getValue(entity, new HashMap<String, Entity>(), null, "", false);
    }

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/dataUpdate", method = RequestMethod.POST)
    @ResponseBody
    public Object getDataUpdate(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        JSONObject jsonBody = getJsonBody(body);
        JSONObject jsonObject = getJsonObject(jsonBody);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonObject);

        ViewValue<Object> newViewValue = viewDefinition.getValue(null, selectedEntities, viewValue, getComponentName(jsonBody),
                false);

        return newViewValue;
    }

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/save", method = RequestMethod.POST)
    @ResponseBody
    public Object performSave(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
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

        return viewDefinition.getValue(null, selectedEntities, viewValue, triggerComponentName, true);
    }

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/delete", method = RequestMethod.POST)
    @ResponseBody
    public Object performDelete(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
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

        return viewDefinition.getValue(null, selectedEntities, viewValue, triggerComponentName, true);
    }

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/move", method = RequestMethod.POST)
    @ResponseBody
    public Object performMove(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        JSONObject jsonBody = getJsonBody(body);
        JSONObject jsonObject = getJsonObject(jsonBody);

        String triggerComponentName = getComponentName(jsonBody);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonObject);

        if (StringUtils.hasText(arguments.get("offset"))) {
            int offset = Integer.valueOf(arguments.get("offset"));

            SelectableComponent component = (SelectableComponent) viewDefinition.lookupComponent(triggerComponentName);

            Long id = component.getSelectedEntityId(viewValue);

            if (id != null) {
                ((Component<?>) component).getDataDefinition().move(id, offset);
            }
        }

        return viewDefinition.getValue(null, selectedEntities, viewValue, triggerComponentName, true);
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
        return getJsonString(json, "componentName").replaceAll("-", ".");
    }

    private String getJsonString(final JSONObject json, final String name) {
        try {
            return json.getString(name);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private JSONObject getJsonObject(final JSONObject json) {
        try {
            return json.getJSONObject("data");
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
