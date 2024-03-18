<link rel="stylesheet" href="${pageContext.request.contextPath}/basic/public/css/style.css?ver=${buildNumber}" type="text/css" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/basic/public/css/fonts/material-icon/css/material-design-iconic-font.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap-glyphicons.css?ver=${buildNumber}" type="text/css" />
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-table.css?ver=${buildNumber}" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-table-filter-control.css?ver=${buildNumber}" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-datetimepicker.css?ver=${buildNumber}" rel="stylesheet" />
<script type="text/javascript" src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/typeahead.min.js?ver=${buildNumber}"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/basic/public/js/jquery.steps.min.js?ver=${buildNumber}"></script>
<div class="modal" tabindex="-1" role="dialog" id="orderDefinitionWizard" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <h5 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.title']}</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
            </button>
         </div>
         <div class="modal-body">
            <form method="POST" id="orderDefinitionForm" class="order-definition-form" autocomplete="off">
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.orderDefinitionWizard.step.whatYouWantToProduce']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label required" for="product">${translationsMap['basic.dashboard.orderDefinitionWizard.form.product']}</label>
                                 </div>
                                 <input type="text" class="form-control" tabindex="1" id="product" name="product" autocomplete="off"/>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white " id="getProduct" type="button">
                                    <span class="glyphicon glyphicon-search"></span>
                                    </button>
                                 </div>
                              </div>
                           </div>
                           <div class="col-sm-2"></div>
                           <div class="col-sm-2">
                              <button id="addProduct" class="btn btn-success btn-sm float-right newButton" type="button">${translationsMap['basic.dashboard.orderDefinitionWizard.form.addProduct']}</button>
                           </div>
                        </div>
                     </div>
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label required " for="quantity">${translationsMap['basic.dashboard.orderDefinitionWizard.form.quantity']}</label>
                                 </div>
                                 <input type="text" class="form-control right decimal" tabindex="2" id="quantity" name="quantity" autocomplete="off"/>
                              </div>
                           </div>
                           <div class="col-sm-2">
                              <input type="text" class="form-control unit" id="unit" name="unit" disabled/>
                           </div>
                           <div class="col-sm-2"></div>
                        </div>
                     </div>
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label" for="description">${translationsMap['basic.dashboard.orderDefinitionWizard.form.description']}</label>
                                 </div>
                                 <textarea class="form-control" tabindex="3" id="description" name="description" rows="4" autocomplete="off"></textarea>
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
                                    <label class="form-label" for="technology"  data-toggle="tooltip" data-placement="top" autocomplete="off" title="${translationsMap['basic.dashboard.orderDefinitionWizard.form.technology.tip']}">${translationsMap['basic.dashboard.orderDefinitionWizard.form.technology']}</label>
                                 </div>
                                 <input type="text" class="form-control" tabindex="4" id="technology" name="technology"/>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white" id="getTechnology" type="button">
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
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 1 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 4</span>
                  </div>
               </fieldset>
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.orderDefinitionWizard.step.whereAndWhenYouWantToProduce']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label required" for="productionLine">${translationsMap['basic.dashboard.orderDefinitionWizard.form.productionLine']}</label>
                                 </div>
                                 <input type="text" class="form-control" tabindex="1" id="productionLine" name="productionLine" autocomplete="off"/>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white " id="getProductionLine" type="button">
                                    <span class="glyphicon glyphicon-search"></span>
                                    </button>
                                 </div>
                              </div>
                           </div>
                           <div class="col-sm-2"></div>
                           <div class="col-sm-2">
                              <button id="addProductionLine" class="btn btn-success btn-sm float-right newButton" type="button">${translationsMap['basic.dashboard.orderDefinitionWizard.form.addProductionLine']}</button>
                           </div>
                        </div>
                     </div>
                     <div class="form-group">
                        <div class="row">
                           <div class="col-sm-8">
                              <div class="input-group">
                                 <div class="input-group-prepend">
                                    <label class="form-label required " for="startDate">${translationsMap['basic.dashboard.orderDefinitionWizard.form.startDate']}</label>
                                 </div>
                                 <input type="text" class="form-control" id="startDate" autocomplete="off"//>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white " id="startDatePicker" type="button">
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
                                    <label class="form-label required " for="finishDate">${translationsMap['basic.dashboard.orderDefinitionWizard.form.finishDate']}</label>
                                 </div>
                                 <input type="text" class="form-control" id="finishDate" autocomplete="off"//>
                                 <div class="input-group-append">
                                    <button class="btn btn-outline-secondary bg-primary text-white " id="finishDatePicker" type="button">
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
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 2 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 4</span>
                  </div>
               </fieldset>
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.orderDefinitionWizard.step.whatDoYouWantToUseForProduction']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="form-group" id="materials-group" style="width: 100%;">
                        <div id="materials-toolbar">
                           <button id="newMaterial" type="button" class="btn btn-outline-secondary bg-primary text-white insert-row-btn">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.addMaterial']}</button>
                           <button id="removeMaterial" type="button" class="btn btn-outline-secondary bg-primary text-white  remove-row-btn">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.removeMaterial']}</button>
                        </div>
                        <table id="materials" data-search="false" data-toolbar="#materials-toolbar">
                           <thead>
                              <tr>
                                 <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                                 <th data-field="index" data-sortable="false" data-switchable="false" data-visible="false"></th>
                                 <th data-field="product" data-formatter="productFormatter" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.product']}</th>
                                 <th data-field="quantity" data-formatter="quantityFormatter" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.quantity']}</th>
                                 <th data-field="quantityPerUnit" data-formatter="quantityPerUnitFormatter" data-switchable="false" data-sortable="false" data-align="center"  data-toggle="tooltip" data-placement="top" autocomplete="off" title="${translationsMap['basic.dashboard.orderDefinitionWizard.materials.quantityPerUnit.tip']}">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.quantityPerUnit']}</th>
                                 <th data-field="unit"  data-formatter="unitFormatter" data-sortable="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.materials.unit']}</th>
                              </tr>
                           </thead>
                        </table>
                     </div>
                  </div>
                  <div class="fieldset-footer">
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 3 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 4</span>
                  </div>
               </fieldset>
               <h3>
                  <span class="title_text">${translationsMap['basic.dashboard.orderDefinitionWizard.step.summary']}</span>
               </h3>
               <fieldset>
                  <div class="fieldset-content">
                     <div class="prev">
                        <div class="form-group">
                           <div class="row">
                              <div class="col-sm-8">
                                 <div class="input-group">
                                    <div class="input-group-prepend">
                                       <label class="form-label required" for="prev_product">${translationsMap['basic.dashboard.orderDefinitionWizard.form.product']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" tabindex="1" id="prev_product" name="prev_product" autocomplete="off"/>
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
                                       <label class="form-label required " for="prev_quantity">${translationsMap['basic.dashboard.orderDefinitionWizard.form.quantity']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control right" tabindex="2" id="prev_quantity" name="prev_quantity" autocomplete="off"/>
                                 </div>
                              </div>
                              <div class="col-sm-2">
                                 <input disabled type="text" class="form-control unit" id="prev_unit" name="prev_unit" disabled/>
                              </div>
                              <div class="col-sm-2"></div>
                           </div>
                        </div>
                        <div class="form-group">
                           <div class="row">
                              <div class="col-sm-8">
                                 <div class="input-group">
                                    <div class="input-group-prepend">
                                       <label class="form-label" for="prev_description">${translationsMap['basic.dashboard.orderDefinitionWizard.form.description']}</label>
                                    </div>
                                    <textarea disabled class="form-control" tabindex="3" id="prev_description" name="prev_description" rows="4" autocomplete="off"></textarea>
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
                                       <label class="form-label" for="prev_technology">${translationsMap['basic.dashboard.orderDefinitionWizard.form.technology']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" tabindex="4" id="prev_technology" name="prev_technology"/>
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
                                       <label class="form-label required" for="prev_productionLine">${translationsMap['basic.dashboard.orderDefinitionWizard.form.productionLine']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" tabindex="1" id="prev_productionLine" name="prev_productionLine" autocomplete="off"/>
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
                                       <label class="form-label required " for="prev_startDate">${translationsMap['basic.dashboard.orderDefinitionWizard.form.startDate']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" id="prev_startDate" autocomplete="off"//>
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
                                       <label class="form-label required " for="prev_finishDate">${translationsMap['basic.dashboard.orderDefinitionWizard.form.finishDate']}</label>
                                    </div>
                                    <input disabled type="text" class="form-control" id="prev_finishDate" autocomplete="off"//>
                                 </div>
                              </div>
                              <div class="col-sm-2">
                              </div>
                              <div class="col-sm-2"></div>
                           </div>
                        </div>
                        <div class="form-group" style="width: 100%;">
                           <div id="prev_materials-toolbar">
                              <label class="form-label required ">${translationsMap['basic.dashboard.orderDefinitionWizard.form.materials']}</label>
                           </div>
                           <table id="prev_materials" data-search="false"  data-toolbar="#prev_materials-toolbar">
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
                        </div>
                     </div>
                  </div>
                  <div class="fieldset-footer">
                     <span>${translationsMap['basic.dashboard.orderDefinitionWizard.step']} 4 ${translationsMap['basic.dashboard.orderDefinitionWizard.of']} 4</span>
                  </div>
               </fieldset>
            </form>
         </div>
      </div>
   </div>
</div>

<div id="productsLookup" class="modal" tabindex="-1" role="dialog" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <table id="products"
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
               <button type="button" class="btn btn-primary mr-2" id="selectProduct">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.selectButton']}</button>
               <button type="button" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="materialsLookup" class="modal" tabindex="-1" role="dialog" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <table id="materialsItem"
               data-search="true">
               <thead>
                  <tr>
                     <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                     <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.id']}</th>
                     <th data-field="number" data-sortable="true" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.number']}</th>
                     <th data-field="name" data-sortable="true" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.name']}</th>
                     <th  data-field="unit" data-sortable="true" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.column.unit']}</th>
                  </tr>
               </thead>
            </table>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" class="btn btn-primary mr-2" id="selectMaterial" onclick="QCD.orderDefinitionWizard.selectMaterialsItem()" >${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.selectButton']}</button>
               <button type="button" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="productDefinitionModal" class="modal qLookup" tabindex="-1" role="dialog"  data-backdrop="static" data-keyboard="false">
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
                  <div class="form-group" id="productDefinitionFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="productNumber"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.number']}>${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.number']}</label>
                        </div>
                        <input type="text" class="form-control right" tabindex="101" id="productNumber" name="productNumber"  autocomplete="off"/>
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
                           <label class="form-label required" for="productName"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.name']}>${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.name']}</label>
                        </div>
                        <input type="text" class="form-control" tabindex="102" id="productName" name="productName"  autocomplete="off"/>
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
                           <label class="form-label required" for="productUnit"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.unit']}>${translationsMap['basic.dashboard.orderDefinitionWizard.form.product.unit']}</label>
                        </div>
                        <select type="text" tabindex="103" class="form-control custom-select" id="productUnit" name="productUnit"  autocomplete="off"></select>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" tabindex="104" class="btn btn-primary mr-2" id="saveProduct">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.save']}</button>
               <button type="button" tabindex="105" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="technologiesLookup" class="modal" tabindex="-1" role="dialog" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <table id="technologies"
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
               <button type="button" class="btn btn-primary mr-2" id="selectTechnology">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.selectButton']}</button>
               <button type="button" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.technologiesLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="productionLinesLookup" class="modal" tabindex="-1" role="dialog" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.productionLinesLookup']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.productionLinesLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <table id="productionLines"
               data-search="true">
               <thead>
                  <tr>
                     <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                     <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false" data-align="center">${translationsMap['basic.dashboard.orderDefinitionWizard.productionLinesLookup.column.id']}</th>
                     <th data-field="number" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.productionLinesLookup.column.number']}</th>
                     <th data-field="name" data-sortable="true" data-align="left">${translationsMap['basic.dashboard.orderDefinitionWizard.productionLinesLookup.column.name']}</th>
                  </tr>
               </thead>
            </table>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" class="btn btn-primary mr-2" id="selectProductionLine">${translationsMap['basic.dashboard.orderDefinitionWizard.productionLinesLookup.selectButton']}</button>
               <button type="button" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productionLinesLookup.cancelButton']}</button>
            </div>
         </div>
      </div>
   </div>
</div>
<div id="productionLineDefinitionModal" class="modal definition-modal" tabindex="-1" role="dialog"  data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-dialog-lg" role="document">
      <div class="modal-content">
         <div class="modal-header">
            <div class="col-auto">
               <h4 class="modal-title">${translationsMap['basic.dashboard.orderDefinitionWizard.productionLineDefinitionModal']}</h4>
            </div>
            <div class="col text-right"><button type="button" class="close" data-dismiss="modal" aria-label="${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.closeButton']}"><span aria-hidden="true">&times;</span></button></div>
         </div>
         <div class="modal-body">
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="productionLineNumberFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="productionLineNumber"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.orderDefinitionWizard.form.productionLine.number']}>${translationsMap['basic.dashboard.orderDefinitionWizard.form.productionLine.number']}</label>
                        </div>
                        <input type="text" class="form-control right" tabindex="201" id="productionLineNumber" name="productionLineNumber"  autocomplete="off"/>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
            <div class="row">
               <div class="col-md-6">
                  <div class="form-group" id="productionLineNameFormGroup">
                     <div class="input-group">
                        <div class="input-group-prepend">
                           <label class="form-label required" for="productionLineName"  data-toggle="tooltip" title=${translationsMap['basic.dashboard.orderDefinitionWizard.form.productionLine.name']}>${translationsMap['basic.dashboard.orderDefinitionWizard.form.productionLine.name']}</label>
                        </div>
                        <input type="text" class="form-control" tabindex="202" id="productionLineName" name="productionLineName"  autocomplete="off"/>
                     </div>
                  </div>
               </div>
               <div class="col-md-6">
               </div>
            </div>
         </div>
         <div class="modal-footer">
            <div class="btn-toolbar justify-content-between">
               <button type="button" tabindex="203" class="btn btn-primary mr-2" id="saveProductionLine">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.save']}</button>
               <button type="button" tabindex="204" class="btn btn-secondary mr-2" data-dismiss="modal">${translationsMap['basic.dashboard.orderDefinitionWizard.productsLookup.cancelButton']}</button>
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
<script type="text/javascript"
<script type="text/javascript" src="${pageContext.request.contextPath}/basic/public/js/orderDefinitionWizard.js?ver=${buildNumber}"></script>
