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

package com.qcadoo.view.internal;

import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Node;

import com.qcadoo.view.api.InternalViewDefinitionService;
import com.qcadoo.view.internal.internal.ComponentCustomEvent;
import com.qcadoo.view.internal.xml.ViewDefinitionParser;

public interface ComponentPattern {

    boolean initialize();

    void initializeAll();

    void registerViews(InternalViewDefinitionService viewDefinitionService);

    void unregisterComponent(InternalViewDefinitionService viewDefinitionService);

    ComponentState createComponentState(ViewDefinitionState viewDefinitionState);

    Map<String, Object> prepareView(Locale locale);

    String getName();

    String getPath();

    String getReference();

    String getFunctionalPath();

    void parse(Node componentNode, ViewDefinitionParser parser);

    void addCustomEvent(final ComponentCustomEvent customEvent);

    void removeCustomEvent(final ComponentCustomEvent customEvent);

}
