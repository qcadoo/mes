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

package com.qcadoo.mes.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.DictionaryService;
import com.qcadoo.mes.beans.dictionaries.DictionariesDictionary;
import com.qcadoo.mes.beans.dictionaries.DictionariesDictionaryItem;
import com.qcadoo.mes.model.aop.internal.Monitorable;

@Service
public final class DictionaryServiceImpl implements DictionaryService {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    @SuppressWarnings("unchecked")
    public List<String> values(final String dictionaryName) {
        checkArgument(hasText(dictionaryName), "dictionary name must be given");
        List<DictionariesDictionaryItem> items = sessionFactory.getCurrentSession()
                .createCriteria(DictionariesDictionaryItem.class).createAlias("dictionary", "dc")
                .add(Restrictions.eq("dc.name", dictionaryName)).addOrder(Order.asc("name")).list();

        List<String> values = new ArrayList<String>();

        for (DictionariesDictionaryItem item : items) {
            values.add(item.getName());
        }

        return values;
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    @SuppressWarnings("unchecked")
    public Set<String> dictionaries() {
        List<DictionariesDictionary> dictionaries = sessionFactory.getCurrentSession().createQuery("from Dictionary").list();

        Set<String> dictionariesNames = new HashSet<String>();

        for (DictionariesDictionary dictionary : dictionaries) {
            dictionariesNames.add(dictionary.getName());
        }

        return dictionariesNames;
    }

}
