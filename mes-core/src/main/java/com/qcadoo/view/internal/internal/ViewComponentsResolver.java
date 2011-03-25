/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.view.internal.internal;

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

import com.qcadoo.view.internal.ComponentDefinition;
import com.qcadoo.view.internal.ComponentPattern;
import com.qcadoo.view.internal.ViewComponent;

@Service
public final class ViewComponentsResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ViewComponentsResolver.class);

    private final Map<String, Class<? extends ComponentPattern>> viewComponentClasses = new HashMap<String, Class<? extends ComponentPattern>>();

    @SuppressWarnings("unchecked")
    public void refreshAvailableComponentsList() {

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

    public ComponentPattern getViewComponentInstance(final String componentName, final ComponentDefinition componentDefinition) {
        Class<? extends ComponentPattern> clazz = getViewComponentClass(componentName);

        if (clazz == null) {
            throw new IllegalStateException("Unsupported component: " + componentName);
        }

        try {
            Constructor<? extends ComponentPattern> constructor = clazz.getConstructor(ComponentDefinition.class);
            return constructor.newInstance(componentDefinition);
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
        public final Collection getComponentClasses(final String basePackage) {
            List<Class> classes = new ArrayList<Class>();
            for (BeanDefinition candidate : findCandidateComponents(basePackage == null ? "" : basePackage)) {
                Class cls = ClassUtils.resolveClassName(candidate.getBeanClassName(), ClassUtils.getDefaultClassLoader());
                classes.add(cls);
            }
            return classes;
        }

    }

}
