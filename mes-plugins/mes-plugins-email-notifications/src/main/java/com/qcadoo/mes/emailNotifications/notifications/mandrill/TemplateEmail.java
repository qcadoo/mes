/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.emailNotifications.notifications.mandrill;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


import com.google.common.collect.Lists;

public class TemplateEmail {

    @JsonProperty("key")
    private String apiKey;

    @JsonProperty("template_name")
    private String templateName;

    @JsonProperty("template_content")
    private List<ContentItem> contentItems = Lists.newArrayList();

    private Message message = new Message();

    public void setContentItems(List<ContentItem> contentItems) {
        this.contentItems = contentItems;
    }

    public List<ContentItem> getContentItems() {
        return contentItems;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
