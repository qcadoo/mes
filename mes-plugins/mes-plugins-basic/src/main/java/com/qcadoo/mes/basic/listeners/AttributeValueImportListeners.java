package com.qcadoo.mes.basic.listeners;

import com.qcadoo.mes.basic.imports.attribute.AttributeImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AttributeValueImportListeners {

    

    private static final String L_XLSX = "xlsx";

    private static final String L_POSITIONS_FILE = "positionsFile";

    public static final String IMPORTED = "imported";

    @Autowired
    private AttributeImportService attributeImportService;

    public void openProductAttrValueImport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        String url = "../page/basic/importProductAttrValue.html";

        view.openModal(url);
    }

    public void importProductAttrValues(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent importedCheckBoxComponent = (CheckBoxComponent) view.getComponentByReference(IMPORTED);

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity formEntity = form.getPersistedEntityWithIncludedFormValues();

        String positionsFilePath = formEntity.getStringField(L_POSITIONS_FILE);
        if (canImportPositions(view, positionsFilePath)) {
            return;
        }
        try {
            boolean imported = attributeImportService.importProductAttributeValues(positionsFilePath, view);
            importedCheckBoxComponent.setChecked(imported);
        } catch (IOException e) {
            view.addMessage("basic.attributeValuesImport.error", ComponentState.MessageType.FAILURE);
        }

    }


    public void openResourceAttrValueImport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        String url = "../page/basic/importResourceAttrValue.html";

        view.openModal(url);
    }

    public void importResourceAttrValue(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent importedCheckBoxComponent = (CheckBoxComponent) view.getComponentByReference(IMPORTED);

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity formEntity = form.getPersistedEntityWithIncludedFormValues();

        String positionsFilePath = formEntity.getStringField(L_POSITIONS_FILE);
        if (canImportPositions(view, positionsFilePath)) {
            return;
        }
        try {
            boolean imported = attributeImportService.importResourceAttributeValues(positionsFilePath, view);
            importedCheckBoxComponent.setChecked(imported);
        } catch (IOException e) {
            view.addMessage("basic.attributeValuesImport.error", ComponentState.MessageType.FAILURE);
        }

    }

    private boolean canImportPositions(ViewDefinitionState view, String positionsFilePath) {
        if (StringUtils.isEmpty(positionsFilePath)) {
            view.addMessage("basic.attributeValuesImport.fileNotSelected", ComponentState.MessageType.FAILURE);

            return true;
        }

        String extension = positionsFilePath.substring(positionsFilePath.lastIndexOf(".") + 1);

        if (!L_XLSX.equalsIgnoreCase(extension)) {
            view.addMessage("basic.attributeValuesImport.extension", ComponentState.MessageType.FAILURE);

            return true;
        }

        return false;
    }
}
