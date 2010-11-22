/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
                .add(Restrictions.eq("dc.name", dictionaryName)).add(Restrictions.ne(EntityService.FIELD_DELETED, true))
                .addOrder(Order.asc("name")).list();

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
