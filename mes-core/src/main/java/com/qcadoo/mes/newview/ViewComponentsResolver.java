package com.qcadoo.mes.newview;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

@Service
public class ViewComponentsResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ViewComponentsResolver.class);

    private final Map<String, Class<? extends ComponentPattern>> viewComponentClasses = new HashMap<String, Class<? extends ComponentPattern>>();

    @SuppressWarnings("unchecked")
    public void refreshAvaliebleComponentsList() {

        LOG.info("Scan for view components...");

        ComponentClassScanner scn = new ComponentClassScanner();
        scn.addIncludeFilter(new AnnotationTypeFilter(ViewComponent.class));

        // TODO mina change package scope
        Collection<Class<?>> componentsCollection = scn.getComponentClasses("com");

        for (Object clazzObj : componentsCollection) {
            Class<? extends ComponentPattern> clazz = (Class<? extends ComponentPattern>) clazzObj;
            ViewComponent viewComponentAnnotation = clazz.getAnnotation(ViewComponent.class);
            String componentName = viewComponentAnnotation.value();
            LOG.info("found Component '" + componentName + "' - " + clazz.getCanonicalName());

            viewComponentClasses.put(componentName, clazz);
        }
        LOG.info("Found " + viewComponentClasses.size() + " view components.");
    }

    public boolean hasViewComponent(final String componentName) {
        return viewComponentClasses.containsKey(componentName);
    }

    public Set<String> getAvailableComponentsSet() {
        return viewComponentClasses.keySet();
    }

    public Class<? extends ComponentPattern> getViewComponentClass(final String componentName) {
        return viewComponentClasses.get(componentName);
    }

    public ComponentPattern getViewComponentInstance(final String componentName, final String name, final String fieldPath,
            final String sourceFieldPath, final ComponentPattern parent) {
        Class<? extends ComponentPattern> clazz = getViewComponentClass(componentName);

        if (clazz == null) {
            throw new IllegalStateException("Unsupported component: " + componentName);
        }

        try {
            Constructor<? extends ComponentPattern> constructor = clazz.getConstructor(String.class, String.class, String.class,
                    ComponentPattern.class);
            return constructor.newInstance(name, fieldPath, sourceFieldPath, parent);
        } catch (SecurityException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private class ComponentClassScanner extends ClassPathScanningCandidateComponentProvider {

        public ComponentClassScanner() {
            super(false);
        }

        @SuppressWarnings("rawtypes")
        public final Collection getComponentClasses(String basePackage) {
            basePackage = basePackage == null ? "" : basePackage;
            List<Class> classes = new ArrayList<Class>();
            for (BeanDefinition candidate : findCandidateComponents(basePackage)) {
                try {
                    Class cls = ClassUtils.resolveClassName(candidate.getBeanClassName(), ClassUtils.getDefaultClassLoader());
                    classes.add(cls);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            return classes;
        }

    }

}
