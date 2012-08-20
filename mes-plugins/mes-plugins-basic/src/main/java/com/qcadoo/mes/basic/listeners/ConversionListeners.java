package com.qcadoo.mes.basic.listeners;


public class ConversionListeners {

    private static final String CONVERSION_ADL = "conversionADL";

    /**
     * Save outer AwesomeDynamicList entities in db and reset operation lookup & related components
     * 
     * @param viewState
     * @param componentState
     * @param args
     */
    // public void saveProgressForDays(final ViewDefinitionState viewState, final ComponentState componentState, final String[]
    // args) {
    // AwesomeDynamicListComponent conversionADL = (AwesomeDynamicListComponent) viewState
    // .getComponentByReference(CONVERSION_ADL);
    // @SuppressWarnings("unchecked")
    // List<Entity> progressForDays = (List<Entity>) conversionADL.getFieldValue();
    // String plannedProgressType = viewState.getComponentByReference(PLANNED_PROGRESS_TYPE).getFieldValue().toString();
    // for (Entity progressForDay : progressForDays) {
    // progressForDay.setField(CORRECTED, plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
    // }
    // Entity tioc = helper.getTiocFromOperationLookup(viewState);
    // boolean hasCorrections = helper.shouldHasCorrections(viewState);
    // if (tioc != null) {
    // tioc.setField(HAS_CORRECTIONS, hasCorrections);
    // tioc.setField(PROGRESS_FOR_DAYS, prepareProgressForDaysForTIOC(tioc, hasCorrections, progressForDays));
    // tioc = tioc.getDataDefinition().save(tioc);
    // if (!tioc.isValid()) {
    // List<ErrorMessage> errors = tioc.getGlobalErrors();
    // for (ErrorMessage error : errors) {
    // componentState.addMessage(error.getMessage(), MessageType.FAILURE, error.getVars());
    // }
    // }
    // if (componentState.isHasError()) {
    // componentState.performEvent(viewState, "initialize", new String[0]);
    // } else {
    // componentState.performEvent(viewState, "save");
    // }
    // }
    // }

}
