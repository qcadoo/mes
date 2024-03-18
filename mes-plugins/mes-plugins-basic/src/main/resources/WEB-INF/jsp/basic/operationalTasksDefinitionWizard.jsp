<link rel="stylesheet" href="${pageContext.request.contextPath}/basic/public/css/style.css?ver=${buildNumber}" type="text/css" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/basic/public/css/fonts/material-icon/css/material-design-iconic-font.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap-glyphicons.css?ver=${buildNumber}" type="text/css" />
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-table.css?ver=${buildNumber}" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-table-filter-control.css?ver=${buildNumber}" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-datetimepicker.css?ver=${buildNumber}" rel="stylesheet" />
<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/typeahead.min.js?ver=${buildNumber}"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/basic/public/js/jquery.steps.min.js?ver=${buildNumber}"></script>
<div class="modal" tabindex="-1" role="dialog" id="operationalTasksDefinitionWizard" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <h5 class="modal-title">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.title']}</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
            </button>
         </div>
         <div class="modal-body">
            <form method="POST" id="operationalTasksDefinitionForm" class="order-definition-form" autocomplete="off">
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.step.1']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label required" for="otProduct">${translationsMap['basic.dashboard.orderDefinitionWizard.form.product']}</label>
                                 </div>
                                 <input type="text" class="form-control" tabindex="1" id="otProduct" name="otProduct" autocomplete="off"/>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white " id="otGetProduct" type="button">
                                    <span class="glyphicon glyphicon-search"></span>
                                    </button>
                                 </div>
                              </div>
                           </div>
                           <div class="col-sm-2"></div>
                           <div class="col-sm-2">
                              <button id="otAddProduct" class="btn btn-success btn-sm float-right newButton" type="button">${translationsMap['basic.dashboard.orderDefinitionWizard.form.addProduct']}</button>
                           </div>
                        </div>
                     </div>
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label required " for="otQuantity">${translationsMap['basic.dashboard.orderDefinitionWizard.form.quantity']}</label>
                                 </div>
                                 <input type="text" class="form-control right decimal" tabindex="2" id="otQuantity" name="quantity" autocomplete="off"/>
                              </div>
                           </div>
                           <div class="col-sm-2">
                              <input type="text" class="form-control unit" id="otUnit" name="unit" disabled/>
                           </div>
                           <div class="col-sm-2"></div>
                        </div>
                     </div>
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label" for="otDescription">${translationsMap['basic.dashboard.orderDefinitionWizard.form.description']}</label>
                                 </div>
                                 <textarea class="form-control" tabindex="3" id="otDescription" name="otDescription" rows="4" autocomplete="off"></textarea>
                              </div>
                           </div>
                           <div class="col-sm-2"></div>
                           <div class="col-sm-2"></div>
                        </div>
                     </div>
                     <div id="technology-form-group" class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label" for="otTechnology"  data-toggle="tooltip" data-placement="top" autocomplete="off" title="${translationsMap['basic.dashboard.orderDefinitionWizard.form.technology.tip']}">${translationsMap['basic.dashboard.orderDefinitionWizard.form.technology']}</label>
                                 </div>
                                 <input type="text" class="form-control" tabindex="4" id="otTechnology" name="technology"/>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white" id="otGetTechnology" type="button">
                                    <span class="glyphicon glyphicon-search"></span>
                                    </button>
                                 </div>
                              </div>
                           </div>
                           <div class="col-sm-2"></div>
                           <div class="col-sm-2">
                           </div>
                        </div>
                     </div>
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label id="description-label" class="form-label">${translationsMap['basic.dashboard.orderDefinitionWizard.form.technology.description']}</label>
                                 </div>
                              </div>
                           </div>
                        </div>
                     </div>
                  </div>
                  <div class="fieldset-footer">
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 1 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 6</span>
                  </div>
               </fieldset>
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.step.2']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label required " for="otStartDate">${translationsMap['basic.dashboard.orderDefinitionWizard.form.startDate']}</label>
                                 </div>
                                 <input type="text" class="form-control" id="otStartDate" autocomplete="off"/>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white " id="otStartDatePicker" type="button">
                                    <span class="glyphicon glyphicon-calendar"></span>
                                    </button>
                                 </div>
                              </div>
                           </div>
                           <div class="col-sm-2">
                           </div>
                           <div class="col-sm-2"></div>
                        </div>
                     </div>
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label required " for="otFinishDate">${translationsMap['basic.dashboard.orderDefinitionWizard.form.finishDate']}</label>
                                 </div>
                                 <input type="text" class="form-control" id="otFinishDate" autocomplete="off"/>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white " id="otFinishDatePicker" type="button">
                                    <span class="glyphicon glyphicon-calendar"></span>
                                    </button>
                                 </div>
                              </div>
                           </div>
                           <div class="col-sm-2">
                           </div>
                           <div class="col-sm-2"></div>
                        </div>
                     </div>
                  </div>
                  <div class="fieldset-footer">
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 2 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 6</span>
                  </div>
               </fieldset>
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.step.3']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="form-group no-margin" >
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label id="description-label" class="form-label">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.form.howDoYouWantToProduce.description']}</label>
                                 </div>
                              </div>
                           </div>
                        </div>
                     </div>
                     <div class="form-group" id="technologyOperations-group" style="width: 100%;">
                        <div id="technologyOperations-toolbar">
                           <button id="newTechnologyOperation" type="button" class="btn btn-outline-secondary bg-primary text-white insert-row-btn">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.newTechnologyOperation']}</button>
                           <button id="removeTechnologyOperation" type="button" class="btn btn-outline-secondary bg-primary text-white  remove-row-btn">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.removeTechnologyOperation']}</button>
                        </div>
                        <table id="technologyOperations" data-search="false" data-toolbar="#technologyOperations-toolbar">
                           <thead>
                              <tr>
                                 <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                                 <th data-field="index" data-sortable="false" data-switchable="false" data-visible="false"></th>
                                 <th data-field="node" data-sortable="false" data-formatter="nodeFormatter" data-align="center" data-width="150">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.node']}</th>
                                 <th data-field="operation" data-formatter="operationFormatter" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.operation']}</th>
                              </tr>
                           </thead>
                        </table>
                     </div>
                  </div>
                  <div class="fieldset-footer">
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 3 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 6</span>
                  </div>
               </fieldset>
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.step.4']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="prev" id="operationMaterials-group">
                     </div>
                  </div>
                  <div class="fieldset-footer">
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 4 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 6</span>
                  </div>
               </fieldset>
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.step.5']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="form-group no-margin" >
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label id="description-label" class="form-label">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.form.whereDoYouWantToProduce.description']}</label>
                                 </div>
                              </div>
                           </div>
                        </div>
                     </div>
                     <div class="form-group"  id="workstations-group" style="width: 100%;">
                        <table id="workstations" data-search="false">
                           <thead>
                              <tr>
                                 <th data-field="index" data-sortable="false" data-switchable="false" data-visible="false"></th>
                                 <th data-field="node" data-sortable="false" data-align="center" data-width="150">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.node']}</th>
                                 <th data-field="operation" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.operation']}</th>
                                 <th data-field="workstation" data-formatter="workstationFormatter" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.workstation']}</th>
                              </tr>
                           </thead>
                        </table>
                     </div>
                  </div>
                  <div class="fieldset-footer">
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 5 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 6</span>
                  </div>
               </fieldset>
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.step.6']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="prev">
                        <div class="form-group">
                           <div class="row">
                              <div class="col-sm-8">
                                 <div class="input-group">
                                    <div class="input-group-prepend">
                                       <label class="form-label required" for="prev_ot_product">${translationsMap['basic.dashboard.orderDefinitionWizard.form.product']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" tabindex="1" id="prev_ot_product" name="prev_ot_product" autocomplete="off"/>
                                 </div>
                              </div>
                              <div class="col-sm-2"></div>
                              <div class="col-sm-2">
                              </div>
                           </div>
                        </div>
                        <div class="form-group">
                           <div class="row">
                              <div class="col-sm-8">
                                 <div class="input-group">
                                    <div class="input-group-prepend">
                                       <label class="form-label required " for="prev_ot_quantity">${translationsMap['basic.dashboard.orderDefinitionWizard.form.quantity']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control right" tabindex="2" id="prev_ot_quantity" name="prev_ot_quantity" autocomplete="off"/>
                                 </div>
                              </div>
                              <div class="col-sm-2">
                                 <input disabled type="text" class="form-control unit" id="prev_ot_unit" name="prev_ot_unit" disabled/>
                              </div>
                              <div class="col-sm-2"></div>
                           </div>
                        </div>
                        <div class="form-group">
                           <div class="row">
                              <div class="col-sm-8">
                                 <div class="input-group">
                                    <div class="input-group-prepend">
                                       <label class="form-label" for="prev_ot_description">${translationsMap['basic.dashboard.orderDefinitionWizard.form.description']}</label>
                                    </div>
                                    <textarea disabled class="form-control" tabindex="3" id="prev_ot_description" name="prev_ot_description" rows="4" autocomplete="off"></textarea>
                                 </div>
                              </div>
                              <div class="col-sm-2"></div>
                              <div class="col-sm-2"></div>
                           </div>
                        </div>
                        <div class="form-group">
                           <div class="row">
                              <div class="col-sm-8">
                                 <div class="input-group">
                                    <div class="input-group-prepend">
                                       <label class="form-label" for="prev_ot_technology">${translationsMap['basic.dashboard.orderDefinitionWizard.form.technology']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" tabindex="4" id="prev_ot_technology" />
                                 </div>
                              </div>
                              <div class="col-sm-2"></div>
                              <div class="col-sm-2">
                              </div>
                           </div>
                        </div>
                        <div class="form-group">
                           <div class="row">
                              <div class="col-sm-8">
                                 <div class="input-group">
                                    <div class="input-group-prepend">
                                       <label class="form-label required " for="prev_ot_startDate">${translationsMap['basic.dashboard.orderDefinitionWizard.form.startDate']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" id="prev_ot_startDate" autocomplete="off"//>
                                 </div>
                              </div>
                              <div class="col-sm-2">
                              </div>
                              <div class="col-sm-2"></div>
                           </div>
                        </div>
                        <div class="form-group">
                           <div class="row">
                              <div class="col-sm-8">
                                 <div class="input-group">
                                    <div class="input-group-prepend">
                                       <label class="form-label required " for="prev_ot_finishDate">${translationsMap['basic.dashboard.orderDefinitionWizard.form.finishDate']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" id="prev_ot_finishDate" autocomplete="off"//>
                                 </div>
                              </div>
                              <div class="col-sm-2">
                              </div>
                              <div class="col-sm-2"></div>
                           </div>
                        </div>
                        <div class="form-group" style="width: 95%;">
                           <div id="prev_ot_operations_toolbar">
                              <label class="form-label required ">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.form.operations']}</label>
                           </div>
                           <table id="prev_ot_operations" data-search="false"  data-toolbar="#prev_ot_operations_toolbar">
                              <thead>
                                 <tr>
                                    <th data-field="index" data-sortable="false" data-switchable="false" data-visible="false"></th>
                                    <th data-field="operation" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.operation']}</th>
                                    <th data-field="node" data-sortable="false" data-align="center" data-width="150">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.node']}</th>
                                    <th data-field="workstation" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.workstation']}</th>
                                 </tr>
                              </thead>
                           </table>
                           </dv>
                        </div>
                        <div class="form-group" style="width: 95%;">
                           <div id="prev_ot_materials-toolbar">
                              <label class="form-label required ">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.form.materials']}</label>
                           </div>
                           <table id="prev_ot_materials" data-search="false"  data-toolbar="#prev_ot_materials-toolbar">
                              <thead>
                                 <tr>
                                    <th data-field="index" data-sortable="false" data-switchable="false" data-visible="false"></th>
                                    <th data-field="product" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.product']}</th>
                                    <th data-field="quantity" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.quantity']}</th>
                                    <th data-field="quantityPerUnit" data-switchable="false" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.quantityPerUnit']}</th>
                                    <th data-field="unit"  data-sortable="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.unit']}</th>
                                 </tr>
                              </thead>
                           </table>
                           </dv>
                        </div>
                     </div>
                  </div>
                  <div class="fieldset-footer">
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 6 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 6</span>
                  </div>
               </fieldset>
            </form>
         </div>
      </div>
   </div>
</div>
<div id="otProductsLookup" class="modal qLookup" tabindex="-1" role="dialog" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <table id="otProducts"
               data-search="true">
               <thead>
                  <tr>
                     <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                     <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.id']}</th>
                     <th data-field="number" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.number']}</th>
                     <th data-field="name" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.name']}</th>
                     <th  data-field="unit" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.unit']}</th>
                  </tr>
               </thead>
            </table>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" class="btn btn-primary mr-2" id="otSelectProduct">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.selectButton']}</button>
               <button type="button" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="otProductDefinitionModal" class="modal definition-modal" tabindex="-1" role="dialog"  data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.productDefinitionModal']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="otProductDefinitionFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="otProductNumber"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.number']}>${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.number']}</label>
                        </div>
                        <input type="text" class="form-control right" tabindex="101" id="otProductNumber" name="productNumber"  autocomplete="off"/>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="usedBatchQuantityFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="otProductName"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.name']}>${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.name']}</label>
                        </div>
                        <input type="text" class="form-control" tabindex="102" id="otProductName" name="otProductName"  autocomplete="off"/>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="usedBatchQuantityFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="otProductUnit"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.unit']}>${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.unit']}</label>
                        </div>
                        <select type="text" tabindex="103" class="form-control custom-select" id="otProductUnit" name="productUnit"  autocomplete="off"></select>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" tabindex="104" class="btn btn-primary mr-2" id="otSaveProduct">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.save']}</button>
               <button type="button" tabindex="105" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="otTechnologiesLookup" class="modal qLookup" tabindex="-1" role="dialog" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <table id="otTechnologies"
               data-search="true">
               <thead>
                  <tr>
                     <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                     <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.column.id']}</th>
                     <th data-field="number" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.column.number']}</th>
                     <th data-field="name" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.column.name']}</th>
                  </tr>
               </thead>
            </table>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" class="btn btn-primary mr-2" id="otSelectTechnology">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.selectButton']}</button>
               <button type="button" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="otWorkstationsLookup" class="modal qLookup" tabindex="-1" role="dialog" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.workstationsLookup']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <table id="workstationItems"
               data-search="true">
               <thead>
                  <tr>
                     <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                     <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.workstationsLookup.column.id']}</th>
                     <th data-field="number" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.workstationsLookup.column.number']}</th>
                     <th data-field="name" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.workstationsLookup.column.name']}</th>
                  </tr>
               </thead>
            </table>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" class="btn btn-primary mr-2" id="otSelectWorkstation">${translationsMap['basic.dashboard.orderDefinitionWizard.workstationsLookup.selectButton']}</button>
               <button type="button" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.workstationsLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="operationDefinitionModal" class="modal definition-modal" tabindex="-1" role="dialog"  data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.operationDefinitionModal']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="operationNumberFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="operationNumber"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.form.operation.number']}>${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.form.operation.number']}</label>
                        </div>
                        <input type="text" class="form-control right" tabindex="201" id="operationNumber" name="operationNumber"  autocomplete="off"/>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="operationNameFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="operationName"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.form.operation.name']}>${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.form.operation.name']}</label>
                        </div>
                        <input type="text" class="form-control" tabindex="202" id="operationName" name="operationName"  autocomplete="off"/>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" tabindex="203" class="btn btn-primary mr-2" id="saveOperation">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.saveOperation']}</button>
               <button type="button" tabindex="204" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="otMaterialsLookup" class="modal qLookup" tabindex="-1" role="dialog" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <table id="otMaterialsItem"
               data-search="true">
               <thead>
                  <tr>
                     <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                     <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.id']}</th>
                     <th data-field="number" data-sortable="true" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.number']}</th>
                     <th data-field="name" data-sortable="true" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.name']}</th>
                     <th data-field="unit" data-sortable="true" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.unit']}</th>
                  </tr>
               </thead>
            </table>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" class="btn btn-primary mr-2" id="selectOtMaterial" onclick="QCD.operationalTasksDefinitionWizard.selectOtMaterialsItem()" >${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.selectButton']}</button>
               <button type="button" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="otWorkstationDefinitionModal" class="modal definition-modal" tabindex="-1" role="dialog"  data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.otWorkstationDefinitionModal']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.operationalTasksDefinitionWizard.otWorkstationDefinitionModal.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="otWorkstationNumberFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="otWorkstationNumber"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.otWorkstationDefinitionModal.form.workstation.number']}>${translationsMap['basic.dashboard.otWorkstationDefinitionModal.form.workstation.number']}</label>
                        </div>
                        <input type="text" class="form-control right" tabindex="501" id="otWorkstationNumber" autocomplete="off"/>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="otWorkstationNumberFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="otWorkstationName"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.otWorkstationDefinitionModal.form.workstation.name']}>${translationsMap['basic.dashboard.otWorkstationDefinitionModal.form.workstation.name']}</label>
                        </div>
                        <input type="text" class="form-control" tabindex="502" id="otWorkstationName" name="otWorkstationName"  autocomplete="off"/>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="otWorkstationTypeNumberFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="otWorkstationType"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.otWorkstationDefinitionModal.form.workstation.type']}>${translationsMap['basic.dashboard.otWorkstationDefinitionModal.form.workstation.type']}</label>
                        </div>
                        <select type="text" tabindex="503" class="form-control custom-select" id="otWorkstationType" autocomplete="off"></select>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" tabindex="104" class="btn btn-primary mr-2" id="otSaveWorkstation">${translationsMap['basic.dashboard.otWorkstationDefinitionModal.save']}</button>
               <button type="button" tabindex="105" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.otWorkstationDefinitionModal.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<script type="text/javascript"
   src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/bootstrap/bootstrap-treeview.js?ver=${buildNumber}"></script>
<script type="text/javascript"
   src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/bootstrap/bootstrap-datetimepicker.js?ver=${buildNumber}"></script>
<script type="text/javascript"
   src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/bootstrap/bootstrap-table.js?ver=${buildNumber}"></script>
<script type="text/javascript"
   src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/bootstrap/bootstrap-table-locale-all.js?ver=${buildNumber}"></script>
<script type="text/javascript"
   src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/bootstrap/bootstrap-table-i18n-enhance.js?ver=${buildNumber}"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/basic/public/js/operationalTasksDefinitionWizard.js?ver=${buildNumber}"></script>
