package com.qcadoo.mes.view;

import java.util.Map;

public interface ContainerComponent<T> extends Component<T> {

    Map<String, Component<?>> getComponents();

}
