package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import java.util.List;

public class OperationProductHelper {

    private boolean resource = false;
    private boolean containsResources = false;
    private boolean lastResource = false;

    List<OperationProductColumnHelper> operationProductColumnHelpers;

    public List<OperationProductColumnHelper> getOperationProductColumnHelpers() {
        return operationProductColumnHelpers;
    }

    public void setOperationProductColumnHelpers(final List<OperationProductColumnHelper> operationProductColumnHelpers) {
        this.operationProductColumnHelpers = operationProductColumnHelpers;
    }

    public boolean isLastResource() {
        return lastResource;
    }

    public void setLastResource(boolean lastResource) {
        this.lastResource = lastResource;
    }

    public boolean isResource() {
        return resource;
    }

    public void setResource(boolean resource) {
        this.resource = resource;
    }

    public boolean isContainsResources() {
        return containsResources;
    }

    public void setContainsResources(boolean containsResources) {
        this.containsResources = containsResources;
    }
}
