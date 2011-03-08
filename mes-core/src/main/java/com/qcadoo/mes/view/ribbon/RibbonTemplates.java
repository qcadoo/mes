package com.qcadoo.mes.view.ribbon;

import com.qcadoo.mes.view.ViewDefinition;

public class RibbonTemplates {

    private RibbonTemplates() {

    }

    public static RibbonGroup getGroupTemplate(final String templateName, final ViewDefinition viewDefinition) {
        if ("navigation".equals(templateName)) {
            return createNavigationTemplate(viewDefinition);
        } else if ("gridNewAndRemoveAction".equals(templateName)) {
            return createGridNewAndRemoveActionsTemplate(viewDefinition);
        } else if ("gridNewCopyAndRemoveAction".equals(templateName)) {
            return createGridNewCopyAndRemoveActionTemplate(viewDefinition);
        } else if ("gridNewAndCopyAction".equals(templateName)) {
            return createGridNewAndCopyActionTemplate(viewDefinition);
        } else if ("formSaveCopyAndRemoveActions".equals(templateName)) {
            return createFormSaveCopyAndRemoveActionsTemplate(viewDefinition);
        } else if ("formSaveAndRemoveActions".equals(templateName)) {
            return createFormSaveAndRemoveActionsTemplate(viewDefinition);
        } else if ("formSaveAndBackAndRemoveActions".equals(templateName)) {
            return createFormSaveAndBackAndRemoveActionsTemplate(viewDefinition);
        } else if ("formSaveAction".equals(templateName)) {
            return createFormSaveActionTemplate(viewDefinition);
        } else {
            throw new IllegalStateException("Unsupported ribbon template : " + templateName);
        }
    }

    private static RibbonGroup createNavigationTemplate(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonBackAction = new RibbonActionItem();
        ribbonBackAction.setAction(RibbonUtils.translateRibbonAction("#{window}.performBack", viewDefinition));
        ribbonBackAction.setIcon("backIcon24.png");
        ribbonBackAction.setName("back");
        ribbonBackAction.setEnabled(true);
        ribbonBackAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("navigation");
        ribbonGroup.addItem(ribbonBackAction);

        return ribbonGroup;
    }

    private static RibbonGroup createGridNewAndRemoveActionsTemplate(final ViewDefinition viewDefinition) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction(viewDefinition));
        ribbonGroup.addItem(createGridDeleteAction(viewDefinition));
        return ribbonGroup;
    }

    private static RibbonGroup createGridNewAndCopyActionTemplate(final ViewDefinition viewDefinition) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction(viewDefinition));
        ribbonGroup.addItem(createGridCopyAction(viewDefinition));
        return ribbonGroup;
    }

    private static RibbonGroup createGridNewCopyAndRemoveActionTemplate(final ViewDefinition viewDefinition) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction(viewDefinition));
        ribbonGroup.addItem(createGridCopyAction(viewDefinition));
        ribbonGroup.addItem(createGridDeleteAction(viewDefinition));
        return ribbonGroup;
    }

    private static RibbonActionItem createGridDeleteAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonDeleteAction = new RibbonActionItem();
        ribbonDeleteAction.setAction(RibbonUtils.translateRibbonAction("#{grid}.performDelete;", viewDefinition));
        ribbonDeleteAction.setIcon("deleteIcon16.png");
        ribbonDeleteAction.setName("delete");
        ribbonDeleteAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        ribbonDeleteAction.setEnabled(false);
        // ribbonDeleteAction.setMessage("noRecordSelected");
        ribbonDeleteAction.setScript("var listener = {onChange: function(selectedArray) {if (selectedArray.length == 0) {"
                + "this.disable();} else {this.enable();}}}; #{grid}.addOnChangeListener(listener);");
        return ribbonDeleteAction;
    }

    private static RibbonActionItem createGridCopyAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonCopyAction = new RibbonActionItem();
        ribbonCopyAction.setAction(RibbonUtils.translateRibbonAction("#{grid}.performCopy;", viewDefinition));
        ribbonCopyAction.setIcon("copyIcon16.png");
        ribbonCopyAction.setName("copy");
        ribbonCopyAction.setEnabled(false);
        // ribbonCopyAction.setMessage("noRecordSelected");
        ribbonCopyAction.setScript("var listener = {onChange: function(selectedArray) {if (selectedArray.length == 0) {"
                + "this.disable();} else {this.enable();}}}; #{grid}.addOnChangeListener(listener);");
        ribbonCopyAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonCopyAction;
    }

    private static RibbonActionItem createGridNewAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonNewAction = new RibbonActionItem();
        ribbonNewAction.setAction(RibbonUtils.translateRibbonAction("#{grid}.performNew;", viewDefinition));
        ribbonNewAction.setIcon("newIcon24.png");
        ribbonNewAction.setName("new");
        ribbonNewAction.setEnabled(true);
        ribbonNewAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonNewAction;
    }

    private static RibbonGroup createFormSaveCopyAndRemoveActionsTemplate(final ViewDefinition viewDefinition) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createFormSaveAction(viewDefinition));
        ribbonGroup.addItem(createFormSaveAndBackAction(viewDefinition));
        ribbonGroup.addItem(createFormSaveAndNewAction(viewDefinition));
        ribbonGroup.addItem(createFormCopyAction(viewDefinition));
        ribbonGroup.addItem(createFormCancelAction(viewDefinition));
        ribbonGroup.addItem(createFormDeleteAction(viewDefinition));
        return ribbonGroup;
    }

    private static RibbonGroup createFormSaveAndRemoveActionsTemplate(final ViewDefinition viewDefinition) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createFormSaveAction(viewDefinition));
        ribbonGroup.addItem(createFormSaveAndBackAction(viewDefinition));
        ribbonGroup.addItem(createFormCancelAction(viewDefinition));
        ribbonGroup.addItem(createFormDeleteAction(viewDefinition));
        return ribbonGroup;
    }

    private static RibbonGroup createFormSaveAndBackAndRemoveActionsTemplate(final ViewDefinition viewDefinition) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createFormSaveAndBackAction(viewDefinition));
        ribbonGroup.addItem(createFormCancelAction(viewDefinition));
        ribbonGroup.addItem(createFormDeleteAction(viewDefinition));
        return ribbonGroup;
    }

    private static RibbonGroup createFormSaveActionTemplate(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonSaveAction = new RibbonActionItem();
        ribbonSaveAction.setAction(RibbonUtils
                .translateRibbonAction("#{form}.performSave; #{window}.performBack", viewDefinition));
        ribbonSaveAction.setIcon("saveBackIcon24.png");
        ribbonSaveAction.setName("save");
        ribbonSaveAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        ribbonSaveAction.setEnabled(true);
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(ribbonSaveAction);

        return ribbonGroup;
    }

    private static RibbonActionItem createFormDeleteAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonDeleteAction = new RibbonActionItem();
        ribbonDeleteAction.setAction(RibbonUtils.translateRibbonAction("#{form}.performDelete; #{window}.performBack",
                viewDefinition));
        ribbonDeleteAction.setIcon("deleteIcon16.png");
        ribbonDeleteAction.setName("delete");
        ribbonDeleteAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        ribbonDeleteAction.setEnabled(false);
        // ribbonDeleteAction.setMessage("recordNotCreated");
        ribbonDeleteAction
                .setScript("var listener = {onSetValue: function(value) {if (!value || !value.content) return; if (value.content.entityId) {"
                        + "this.enable();} else {this.disable();}}}; #{form}.addOnChangeListener(listener);");
        return ribbonDeleteAction;
    }

    private static RibbonActionItem createFormCancelAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonCancelAction = new RibbonActionItem();
        ribbonCancelAction.setAction(RibbonUtils.translateRibbonAction("#{form}.performCancel;", viewDefinition));
        ribbonCancelAction.setIcon("cancelIcon16.png");
        ribbonCancelAction.setName("cancel");
        ribbonCancelAction.setEnabled(true);
        ribbonCancelAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonCancelAction;
    }

    private static RibbonActionItem createFormCopyAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonCopyAction = new RibbonActionItem();
        ribbonCopyAction.setAction(RibbonUtils.translateRibbonAction("#{form}.performCopy;", viewDefinition));
        ribbonCopyAction.setIcon("copyIcon16.png");
        ribbonCopyAction.setName("copy");
        ribbonCopyAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        ribbonCopyAction.setEnabled(false);
        // ribbonCopyAction.setMessage("recordNotCreated");
        ribbonCopyAction
                .setScript("var listener = {onSetValue: function(value) {if (!value || !value.content) return; if (value.content.entityId) {"
                        + "this.enable();} else {this.disable();}}}; #{form}.addOnChangeListener(listener);");
        return ribbonCopyAction;
    }

    private static RibbonActionItem createFormSaveAndBackAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonSaveBackAction = new RibbonActionItem();
        ribbonSaveBackAction.setAction(RibbonUtils.translateRibbonAction("#{form}.performSave; #{window}.performBack;",
                viewDefinition));
        ribbonSaveBackAction.setIcon("saveBackIcon24.png");
        ribbonSaveBackAction.setName("saveBack");
        ribbonSaveBackAction.setEnabled(true);
        ribbonSaveBackAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonSaveBackAction;
    }

    private static RibbonActionItem createFormSaveAndNewAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonSaveNewAction = new RibbonActionItem();
        ribbonSaveNewAction.setAction(RibbonUtils.translateRibbonAction("#{form}.performSaveAndClear;", viewDefinition));
        ribbonSaveNewAction.setIcon("saveNewIcon16.png");
        ribbonSaveNewAction.setName("saveNew");
        ribbonSaveNewAction.setEnabled(true);
        ribbonSaveNewAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonSaveNewAction;
    }

    private static RibbonActionItem createFormSaveAction(final ViewDefinition viewDefinition) {
        RibbonActionItem ribbonSaveAction = new RibbonActionItem();
        ribbonSaveAction.setAction(RibbonUtils.translateRibbonAction("#{form}.performSave;", viewDefinition));
        ribbonSaveAction.setIcon("saveIcon24.png");
        ribbonSaveAction.setName("save");
        ribbonSaveAction.setEnabled(true);
        ribbonSaveAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonSaveAction;
    }

}
