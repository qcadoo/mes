package com.qcadoo.model.api;

import java.util.List;


public interface EntityTreeNode extends Entity {

    List<EntityTreeNode> getChildren();

    String getEntityType();

}