/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.MenuService;
import com.qcadoo.mes.internal.TranslationServiceImpl;
import com.qcadoo.mes.model.internal.DataDefinitionParser;
import com.qcadoo.mes.view.internal.ViewDefinitionParser;

@Service
public final class Application {

    @Autowired
    private DataDefinitionParser dataDefinitionParser;

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private MenuService menuService;

    @PostConstruct
    public void init() {
        dataDefinitionParser.parse();
        viewDefinitionParser.parse();
        ((TranslationServiceImpl) translationService).init();
        menuService.updateViewDefinitionDatabase();
    }

}
