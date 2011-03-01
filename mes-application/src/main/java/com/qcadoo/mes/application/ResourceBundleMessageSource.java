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

package com.qcadoo.mes.application;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;

public class ResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceBundleMessageSource.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${ignoreMissingTranslations}")
    private boolean ignoreMissingTranslations;

    @PostConstruct
    public void init() {
        setFallbackToSystemLocale(ignoreMissingTranslations);

        LOG.info("Locking for localization resources ...");

        Set<String> basenames = new HashSet<String>();

        basenames.add("WEB-INF/locales/messages");
        LOG.info("Add the WEB-INF/locales/messages as localization resource");

        try {
            Resource[] resources = applicationContext.getResources("classpath*:locales/*.properties");

            Pattern pattern = Pattern.compile("([a-z][a-zA-Z0-9]*)\\_\\w+\\.properties");

            for (Resource resource : resources) {
                Matcher matcher = pattern.matcher(resource.getFilename());

                if (matcher.matches()) {
                    if (basenames.add("classpath:locales/" + matcher.group(1))) {
                        LOG.info("Add the classpath:locales/" + matcher.group(1) + " as localization resource");
                    }
                } else {
                    LOG.warn("Invalid name of the localization resource: " + resource.getFilename());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find localization resources", e);
        }

        setBasenames(basenames.toArray(new String[basenames.size()]));
    }

}
