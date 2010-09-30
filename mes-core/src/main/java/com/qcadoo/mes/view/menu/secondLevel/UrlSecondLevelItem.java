package com.qcadoo.mes.view.menu.secondLevel;

import com.qcadoo.mes.view.menu.SecondLevelItem;

public class UrlSecondLevelItem extends SecondLevelItem {

    private final String pageUrl;

    public UrlSecondLevelItem(String name, String label, String pageUrl) {
        super(name, label);
        this.pageUrl = pageUrl;
    }

    @Override
    public String getPage() {
        return pageUrl;
    }
}
