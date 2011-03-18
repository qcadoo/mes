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

package com.qcadoo.model.internal.dictionaries;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.model.api.aop.Monitorable;
import com.qcadoo.model.beans.dictionaries.DictionariesDictionary;
import com.qcadoo.model.beans.dictionaries.DictionariesDictionaryItem;
import com.qcadoo.model.internal.api.InternalDictionaryService;

@Service
public final class DictionaryServiceImpl implements InternalDictionaryService {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    @SuppressWarnings("unchecked")
    public List<String> keys(final String dictionary) {
        checkArgument(hasText(dictionary), "dictionary name must be given");

        List<DictionariesDictionaryItem> items = sessionFactory.getCurrentSession()
                .createCriteria(DictionariesDictionaryItem.class).createAlias("dictionary", "dc")
                .add(Restrictions.eq("dc.name", dictionary)).addOrder(Order.asc("name")).list();

        List<String> keys = new ArrayList<String>();

        for (DictionariesDictionaryItem item : items) {
            keys.add(item.getName());
        }

        return keys;
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    @SuppressWarnings("unchecked")
    public Map<String, String> values(final String dictionary, final Locale locale) {
        checkArgument(hasText(dictionary), "dictionary name must be given");

        List<DictionariesDictionaryItem> items = sessionFactory.getCurrentSession()
                .createCriteria(DictionariesDictionaryItem.class).createAlias("dictionary", "dc")
                .add(Restrictions.eq("dc.name", dictionary)).addOrder(Order.asc("name")).list();

        Map<String, String> values = new LinkedHashMap<String, String>();

        // TODO - i18n

        for (DictionariesDictionaryItem item : items) {
            values.put(item.getName(), item.getName());
        }

        return values;
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    @SuppressWarnings("unchecked")
    public Set<String> dictionaries() {
        List<DictionariesDictionary> dictionaries = sessionFactory.getCurrentSession().createQuery("from Dictionary").list();

        Set<String> names = new HashSet<String>();

        for (DictionariesDictionary dictionary : dictionaries) {
            names.add(dictionary.getName());
        }

        return names;
    }

    @Override
    @Transactional
    @Monitorable
    public void createIfNotExists(final String name, final String... values) {
        if (sessionFactory.getCurrentSession().createCriteria(DictionariesDictionary.class).add(Restrictions.eq("name", name))
                .list().size() > 0) {
            return;
        }

        DictionariesDictionary dictionary = new DictionariesDictionary();
        dictionary.setName(name);
        dictionary.setLabel(name);

        sessionFactory.getCurrentSession().save(dictionary);

        for (String value : values) {
            DictionariesDictionaryItem item = new DictionariesDictionaryItem();
            item.setDictionary(dictionary);
            item.setDescription("");
            item.setName(value);

            sessionFactory.getCurrentSession().save(item);
        }
    }
}
