package com.qcadoo.mes.plugins.service;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.internal.EntityService;
import com.qcadoo.mes.plugins.PluginManagementService;

@Service
public final class PluginManagementServiceImpl implements PluginManagementService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private EntityService entityService;

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementServiceImpl.class);

}
