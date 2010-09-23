package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.beans.dictionaries.DictionariesDictionary;
import com.qcadoo.mes.beans.dictionaries.DictionariesDictionaryItem;
import com.qcadoo.mes.core.data.api.DictionaryService;

@Service
public final class DictionaryServiceImpl implements DictionaryService {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> values(final String dictionaryName) {
        checkArgument(StringUtils.hasText(dictionaryName), "dictionary name must be given");
        List<DictionariesDictionaryItem> items = (List<DictionariesDictionaryItem>) sessionFactory.getCurrentSession()
                .createQuery("from DictionaryItem where dictionary.name = :dictionaryName order by name")
                .setString("dictionaryName", dictionaryName).list();

        List<String> values = new ArrayList<String>();

        for (DictionariesDictionaryItem item : items) {
            values.add(item.getName());
        }

        return values;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Set<String> dictionaries() {
        List<DictionariesDictionary> dictionaries = (List<DictionariesDictionary>) sessionFactory.getCurrentSession().createQuery("from Dictionary")
                .list();

        Set<String> dictionariesNames = new HashSet<String>();

        for (DictionariesDictionary dictionary : dictionaries) {
            dictionariesNames.add(dictionary.getName());
        }

        return dictionariesNames;
    }

}
