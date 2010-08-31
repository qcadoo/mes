package com.qcadoo.mes.plugins.products.controller;


public class CrudTemplate {

    // @Autowired
    // private ReloadableResourceBundleMessageSource messageSource;
    //
    // @Autowired
    // protected DataDefinitionService dataDefinitionService;
    //
    // @Autowired
    // private DataAccessService dataAccessService;
    //
    // @Autowired
    // private ValidationService validationUtils;
    //
    // private Logger logger;
    //
    // public CrudTemplate(Logger logger) {
    // this.logger = logger;
    // }
    //
    // public ModelAndView getEntityListView(String viewName, String entityType, String message) {
    // ModelAndView mav = new ModelAndView();
    // mav.setViewName(viewName);
    // if (message != null) {
    // mav.addObject("message", message);
    // }
    // mav.addObject("message", message);
    // DataDefinition dataDefinition = dataDefinitionService.get(entityType);
    // List<GridDefinition> grids = dataDefinition.getGrids();
    // GridDefinition gridDefinition = grids.get(0);
    // mav.addObject("gridDefinition", gridDefinition);
    //
    // return mav;
    // }
    //
    // protected ListData getEntitiesGridData(String entityType) {
    // return getEntitiesGridData(entityType, null, null, null, null, null, null);
    // }
    //
    // protected ListData getEntitiesGridData(String entityType, Long parentEntityId, String entityParentField) {
    // return getEntitiesGridData(entityType, parentEntityId, entityParentField, null, null, null, null);
    // }
    //
    // protected ListData getEntitiesGridData(String entityType, Integer maxResults, Integer firstResult, String sortColumn,
    // String sortOrder) {
    // return getEntitiesGridData(entityType, null, null, maxResults, firstResult, sortColumn, sortOrder);
    // }
    //
    // protected ListData getEntitiesGridData(String entityType, Long parentEntityId, String entityParentField, Integer
    // maxResults,
    // Integer firstResult, String sortColumn, String sortOrder) {
    //
    // SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(entityType);
    // if (parentEntityId != null && entityParentField != null) {
    // searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions
    // .belongsTo(entityParentField, parentEntityId));
    // }
    // if (maxResults != null) {
    // checkArgument(maxResults >= 0, "Max results must be greater or equals 0");
    // searchCriteriaBuilder = searchCriteriaBuilder.withMaxResults(maxResults);
    // }
    // if (firstResult != null) {
    // checkArgument(firstResult >= 0, "First result must be greater or equals 0");
    // searchCriteriaBuilder = searchCriteriaBuilder.withFirstResult(firstResult);
    // }
    // if (sortColumn != null && sortOrder != null) {
    // if ("desc".equals(sortOrder)) {
    // searchCriteriaBuilder = searchCriteriaBuilder.orderBy(Order.desc(sortColumn));
    // } else {
    // searchCriteriaBuilder = searchCriteriaBuilder.orderBy(Order.asc(sortColumn));
    // }
    // }
    // SearchCriteria searchCriteria = searchCriteriaBuilder.build();
    //
    // ResultSet rs = dataAccessService.find(entityType, searchCriteria);
    //
    // DataDefinition dataDefinition = dataDefinitionService.get(entityType);
    // GridDefinition gridDefinition = dataDefinition.getGrids().get(0);
    //
    // return ListDataUtils.generateListData(rs, gridDefinition);
    // }
    //
    // protected ModelAndView getEntityFormView(String viewName, Long entityId, String entityType, Long parentEntityId,
    // String entityParentField) {
    // ModelAndView mav = new ModelAndView();
    // mav.setViewName(viewName);
    //
    // DataDefinition entityDataDefinition = dataDefinitionService.get(entityType);
    // mav.addObject("entityFieldsDefinition", entityDataDefinition.getFields());
    //
    // Entity entity = null;
    // if (entityId != null) {
    // entity = dataAccessService.get(entityType, entityId);
    // } else {
    // if (parentEntityId != null) {
    // entity = new Entity();
    // Entity parentEntity = new Entity(parentEntityId);
    // entity.setField(entityParentField, parentEntity);
    // }
    // }
    // mav.addObject("entity", entity);
    //
    // mav.addObject("entityType", entityType);
    //
    // insertDictionaryValues(mav, entityDataDefinition);
    //
    // return mav;
    // }
    //
    // protected ValidationResult saveEntity(Entity entity, String entityType, Locale locale) {
    // DataDefinition entityDataDefinition = dataDefinitionService.get(entityType);
    // ValidationResult validationResult = validationUtils.validateEntity(entity, entityDataDefinition.getFields());
    //
    // if (validationResult.isValid()) {
    // dataAccessService.save(entityType, validationResult.getValidEntity());
    // }
    //
    // if (locale != null) {
    // translateValidationResult(validationResult, locale);
    // }
    //
    // return validationResult;
    // }
    //
    // protected String deleteEntity(List<Integer> selectedRows, String entityType) {
    // if (logger.isDebugEnabled()) {
    // logger.debug("DELETE - " + entityType + " - SELECTED: " + selectedRows);
    // }
    // for (Integer recordId : selectedRows) {
    // dataAccessService.delete(entityType, (long) recordId);
    // }
    // return "ok";
    // }
    //
    // private void insertDictionaryValues(ModelAndView mav, DataDefinition entityDataDefinition) {
    // Map<String, Map<Long, String>> dictionaryValues = new HashMap<String, Map<Long, String>>();
    // for (FieldDefinition fieldDef : entityDataDefinition.getFields()) {
    // switch (fieldDef.getType().getNumericType()) {
    // case FieldTypeFactory.NUMERIC_TYPE_BELONGS_TO:
    // BelongsToFieldType belongsToField = (BelongsToFieldType) fieldDef.getType();
    // Map<Long, String> fieldOptions = belongsToField.lookup(null);
    // dictionaryValues.put(fieldDef.getName(), fieldOptions);
    // break;
    // case FieldTypeFactory.NUMERIC_TYPE_DICTIONARY:
    // case FieldTypeFactory.NUMERIC_TYPE_ENUM:
    // EnumeratedFieldType enumeratedField = (EnumeratedFieldType) fieldDef.getType();
    // List<String> options = enumeratedField.values();
    // Map<Long, String> fieldOptionsMap = new HashMap<Long, String>();
    // Long key = (long) 0;
    // for (String option : options) {
    // fieldOptionsMap.put(key++, option);
    // }
    // dictionaryValues.put(fieldDef.getName(), fieldOptionsMap);
    // break;
    // }
    // }
    // mav.addObject("dictionaryValues", dictionaryValues);
    // }
    //
    // private void translateValidationResult(ValidationResult validationResult, Locale locale) {
    // Map<String, String> messages = validationResult.getFieldMessages();
    // if (messages != null) {
    //
    // for (Entry<String, String> entry : messages.entrySet()) {
    // messages.put(entry.getKey(), messageSource.getMessage(entry.getValue(), null, locale));
    // }
    // if (validationResult.getGlobalMessage() != null) {
    // validationResult.setGlobalMessage(messageSource.getMessage(validationResult.getGlobalMessage(), null, locale));
    // }
    // }
    // }
}
