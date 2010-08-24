package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.beans.Dictionary;
import com.qcadoo.mes.core.data.beans.DictionaryItem;

public final class DictionaryServiceImpl implements DictionaryService {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> values(String dictionaryName) {
        checkArgument(StringUtils.hasText(dictionaryName), "dictionary name must be given");
        List<DictionaryItem> items = (List<DictionaryItem>) sessionFactory.getCurrentSession()
                .createQuery("from DictionaryItem where dictionary.name = :dictionaryName order by name")
                .setString("dictionaryName", dictionaryName).list();

        List<String> values = new ArrayList<String>();

        for (DictionaryItem item : items) {
            values.add(item.getName());
        }

        return values;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Set<String> dictionaries() {
        List<Dictionary> dictionaries = (List<Dictionary>) sessionFactory.getCurrentSession().createQuery("from Dictionary")
                .list();

        Set<String> dictionariesNames = new HashSet<String>();

        for (Dictionary dictionary : dictionaries) {
            dictionariesNames.add(dictionary.getName());
        }

        return dictionariesNames;
    }

}
