<link rel="stylesheet" href="${pageContext.request.contextPath}/basic/public/css/style.css?ver=${buildNumber}"
      type="text/css"/>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/basic/public/css/fonts/material-icon/css/material-design-iconic-font.min.css">
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/qcadooView/public/css/core/lib/bootstrap-glyphicons.css?ver=${buildNumber}"
      type="text/css"/>
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-table.css?ver=${buildNumber}"
      rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-table-filter-control.css?ver=${buildNumber}"
      rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/basic/public/css/bootstrap-datetimepicker.css?ver=${buildNumber}"
      rel="stylesheet"/>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/qcadooView/public/js/core/lib/typeahead.min.js?ver=${buildNumber}"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/basic/public/js/jquery.steps.min.js?ver=${buildNumber}"></script>

<div class="modal" tabindex="-1" role="dialog" id="technologyConfiguratorWizard" data-backdrop="static"
     data-keyboard="false">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.title']}</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form method="POST" id="technologyConfiguratorWizardForm" class="order-definition-form"
                      autocomplete="off">
                    <h3>
                        <span class="title_text">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.1']}</span>
                    </h3>
                    <fieldset>
                        <div class="fieldset-content">
                            <div class="row">
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <label style="padding-bottom: 25px;" class="t_title form-label required">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.1.title']}</label>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="row">
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <div class="input-group-prepend">
                                                <label class="form-label required"
                                                       for="otProduct">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.product']}</label>
                                            </div>
                                            <input type="text" class="form-control" tabindex="1" id="otProduct"
                                                   name="otProduct" autocomplete="off"/>
                                            <div class="input-group-append">
                                                <button class="btn btn-outline-secondary bg-primary text-white "
                                                        id="otGetProduct" type="button">
                                                    <span class="glyphicon glyphicon-search"></span>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-sm-2"></div>
                                    <div class="col-sm-2">
                                        <button id="otAddProduct" class="btn btn-success btn-sm float-right newButton"
                                                type="button">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.addProduct']}</button>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="row">
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <div class="input-group-prepend">
                                                <label class="form-label required " data-toggle="tooltip" data-placement="top"  title='${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.quantity.tip']}'
                                                       for="otQuantity">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.quantity']}</label>
                                            </div>
                                            <input type="text" class="form-control right decimal" tabindex="2"
                                                   id="otQuantity" name="quantity" autocomplete="off"/>
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
                                                <label class="form-label"
                                                       for="otDescription">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.description']}</label>
                                            </div>
                                            <textarea class="form-control" tabindex="3" id="otDescription"
                                                      name="otDescription" rows="4" autocomplete="off"></textarea>
                                        </div>
                                    </div>
                                    <div class="col-sm-2"></div>
                                    <div class="col-sm-2"></div>
                                </div>
                            </div>
                        </div>
                        <div class="fieldset-footer" style="margin-top: 10px;">
                            <span>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step']} 1 ${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.of']} 5</span>
                        </div>
                    </fieldset>
                    <h3>
                        <span class="title_text">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.2']}</span>
                    </h3>
                    <fieldset>
                        <div class="fieldset-content">
                            <div class="row">
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <label class="t_title form-label required">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.2.title']}</label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group no-margin">
                                <div class="row">
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <div class="input-group-prepend">
                                                <label id="description-label"
                                                       class="form-label">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.howDoYouWantToProduce.description']}</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group" id="technologyOperations-group" style="width: 100%;">
                                <div id="technologyOperations-toolbar">
                                    <button id="newTechnologyOperation" type="button"
                                            class="btn btn-outline-secondary bg-primary text-white insert-row-btn">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.newTechnologyOperation']}</button>
                                    <button id="removeTechnologyOperation" type="button"
                                            class="btn btn-outline-secondary bg-primary text-white  remove-row-btn">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.removeTechnologyOperation']}</button>
                                </div>
                                <table id="technologyOperations" data-search="false"
                                       data-toolbar="#technologyOperations-toolbar">
                                    <thead>
                                    <tr>
                                        <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                                        <th data-field="index" data-sortable="false" data-switchable="false"
                                            data-visible="false"></th>
                                        <th data-field="node" data-sortable="false" data-formatter="nodeFormatter"
                                            data-align="center"
                                            data-width="150">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.node']}</th>
                                        <th data-field="operation" data-formatter="operationFormatter"
                                            data-sortable="false"
                                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.operation']}</th>
                                    </tr>
                                    </thead>
                                </table>
                            </div>
                        </div>

                        <div class="fieldset-footer" style="margin-top: 10px;">
                            <span>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step']} 2 ${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.of']} 5</span>
                        </div>
                    </fieldset>
                    <h3>
                        <span class="title_text">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.3']}</span>
                    </h3>
                    <fieldset>
                        <div class="row">
                            <div class="col-sm-8">
                                <div class="input-group">
                                    <div class="input-group-prepend">
                                        <label class="t_title form-label required">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.3.title']}</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="fieldset-content">
                            <div class="prev" id="operationMaterials-group">
                            </div>
                        </div>
                        <div class="fieldset-footer" style="margin-top: 10px;">
                            <span>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step']} 3 ${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.of']} 5</span>
                        </div>
                    </fieldset>
                    <h3>
                        <span class="title_text">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.4']}</span>
                    </h3>
                    <fieldset>
                        <div class="fieldset-content">
                            <div class="row">
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <label class="t_title form-label required">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.4.title']}</label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group no-margin">
                                <div class="row">
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <div class="input-group-prepend">
                                                <label id="description-label"
                                                       class="form-label">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.whereDoYouWantToProduce.description']}</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group" id="workstations-group" style="width: 100%;">
                                <table id="workstations" data-search="false">
                                    <thead>
                                    <tr>
                                        <th data-field="index" data-sortable="false" data-switchable="false"
                                            data-visible="false"></th>
                                        <th data-field="node" data-sortable="false" data-align="center"
                                            data-width="150">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.node']}</th>
                                        <th data-field="operation" data-sortable="false"
                                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.operation']}</th>
                                        <th data-field="workstation" data-formatter="workstationFormatter"
                                            data-sortable="false"
                                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.workstation']}</th>
                                    </tr>
                                    </thead>
                                </table>
                            </div>
                        </div>

                        <div class="fieldset-footer" style="margin-top: 10px;">
                            <span>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step']} 4 ${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.of']} 5</span>
                        </div>
                    </fieldset>
                    <h3>
                        <span class="title_text">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.5']}</span>
                    </h3>
                    <fieldset>
                        <div class="fieldset-content">
                            <div class="row">
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <label class="t_title form-label required">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.5.title']}</label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="prev">
                                <div id="operations"></div>

                            </div>
                        </div>
                        <div class="fieldset-footer" style="margin-top: 10px;">
                            <span>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step']} 5 ${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.step.of']} 5</span>
                        </div>
                    </fieldset>
                </form>
            </div>
        </div>
    </div>
</div>
<div id="otProductsLookup" class="modal qLookup" tabindex="-1" role="dialog" data-backdrop="static"
     data-keyboard="false">
    <div class="modal-dialog modal-dialog-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <div class="col-auto">
                    <h4 class="modal-title">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup']}</h4>
                </div>
            </div>
            <div class="modal-body">
                <table id="otProducts"
                       data-search="true">
                    <thead>
                    <tr>
                        <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                        <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false"
                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.column.id']}</th>
                        <th data-field="number" data-sortable="true"
                            data-align="left">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.column.number']}</th>
                        <th data-field="name" data-sortable="true"
                            data-align="left">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.column.name']}</th>
                        <th data-field="unit" data-sortable="true"
                            data-align="left">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.column.unit']}</th>
                    </tr>
                    </thead>
                </table>
            </div>
            <div class="modal-footer">
                <div class="btn-toolbar justify-content-between">
                    <button type="button" class="btn btn-primary mr-2"
                            id="otSelectProduct">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.selectButton']}</button>
                    <button type="button" class="btn btn-secondary mr-2"
                            data-dismiss="modal">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.cancelButton']}</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="otProductDefinitionModal" class="modal definition-modal" tabindex="-1" role="dialog" data-backdrop="static"
     data-keyboard="false">
    <div class="modal-dialog modal-dialog-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <div class="col-auto">
                    <h4 class="modal-title">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productDefinitionModal']}</h4>
                </div>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group" id="otProductDefinitionFormGroup">
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <label class="form-label required" for="otProductNumber" data-toggle="tooltip"
                                           title=${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.product.number']}>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.product.number']}</label>
                                </div>
                                <input type="text" class="form-control right" tabindex="101" id="otProductNumber"
                                       name="productNumber" autocomplete="off"/>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group" id="otProductNameFormGroup">
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <label class="form-label required" for="otProductName" data-toggle="tooltip"
                                           title=${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.product.name']}>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.product.name']}</label>
                                </div>
                                <input type="text" class="form-control" tabindex="102" id="otProductName"
                                       name="otProductName" autocomplete="off"/>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group" id="otProductUnitFormGroup">
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <label class="form-label required" for="otProductUnit" data-toggle="tooltip"
                                           title=${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.product.unit']}>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.product.unit']}</label>
                                </div>
                                <select type="text" tabindex="103" class="form-control custom-select" id="otProductUnit"
                                        name="productUnit" autocomplete="off"></select>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div class="btn-toolbar justify-content-between">
                    <button type="button" tabindex="104" class="btn btn-primary mr-2"
                            id="otSaveProduct">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.save']}</button>
                    <button type="button" tabindex="105" class="btn btn-secondary mr-2"
                            data-dismiss="modal">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.cancelButton']}</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="operationDefinitionModal" class="modal definition-modal" tabindex="-1" role="dialog" data-backdrop="static"
     data-keyboard="false">
    <div class="modal-dialog modal-dialog-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <div class="col-auto">
                    <h4 class="modal-title">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.operationDefinitionModal']}</h4>
                </div>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group" id="operationNumberFormGroup">
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <label class="form-label required" for="operationNumber" data-toggle="tooltip"
                                           title=${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.operation.number']}>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.operation.number']}</label>
                                </div>
                                <input type="text" class="form-control right" tabindex="201" id="operationNumber"
                                       name="operationNumber" autocomplete="off"/>
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
                                    <label class="form-label required" for="operationName" data-toggle="tooltip"
                                           title=${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.operation.name']}>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.form.operation.name']}</label>
                                </div>
                                <input type="text" class="form-control" tabindex="202" id="operationName"
                                       name="operationName" autocomplete="off"/>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div class="btn-toolbar justify-content-between">
                    <button type="button" tabindex="203" class="btn btn-primary mr-2"
                            id="saveOperation">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.saveOperation']}</button>
                    <button type="button" tabindex="204" class="btn btn-secondary mr-2"
                            data-dismiss="modal">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.cancelButton']}</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="otMaterialsLookup" class="modal qLookup" tabindex="-1" role="dialog" data-backdrop="static"
     data-keyboard="false">
    <div class="modal-dialog modal-dialog-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <div class="col-auto">
                    <h4 class="modal-title">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup']}</h4>
                </div>
            </div>
            <div class="modal-body">
                <table id="otMaterialsItem"
                       data-search="true">
                    <thead>
                    <tr>
                        <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                        <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false"
                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.column.id']}</th>
                        <th data-field="number" data-sortable="true"
                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.column.number']}</th>
                        <th data-field="name" data-sortable="true"
                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.column.name']}</th>
                        <th data-field="unit" data-sortable="true"
                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.column.unit']}</th>
                    </tr>
                    </thead>
                </table>
            </div>
            <div class="modal-footer">
                <div class="btn-toolbar justify-content-between">
                    <button type="button" class="btn btn-primary mr-2" id="selectOtMaterial"
                            onclick="QCD.technologyConfigurator.selectOtMaterialsItem()">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.selectButton']}</button>
                    <button type="button" class="btn btn-secondary mr-2"
                            data-dismiss="modal">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.productsLookup.cancelButton']}</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="otWorkstationsLookup" class="modal qLookup" tabindex="-1" role="dialog" data-backdrop="static"
     data-keyboard="false">
    <div class="modal-dialog modal-dialog-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <div class="col-auto">
                    <h4 class="modal-title">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.workstationsLookup']}</h4>
                </div>
            </div>
            <div class="modal-body">
                <table id="workstationItems"
                       data-search="true">
                    <thead>
                    <tr>
                        <th data-align="center" data-switchable="false" data-checkbox="true"></th>
                        <th data-field="id" data-sortable="true" data-switchable="false" data-visible="false"
                            data-align="center">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.workstationsLookup.column.id']}</th>
                        <th data-field="number" data-sortable="true"
                            data-align="left">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.workstationsLookup.column.number']}</th>
                        <th data-field="name" data-sortable="true"
                            data-align="left">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.workstationsLookup.column.name']}</th>
                    </tr>
                    </thead>
                </table>
            </div>
            <div class="modal-footer">
                <div class="btn-toolbar justify-content-between">
                    <button type="button" class="btn btn-primary mr-2"
                            id="otSelectWorkstation">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.workstationsLookup.selectButton']}</button>
                    <button type="button" class="btn btn-secondary mr-2"
                            data-dismiss="modal">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.workstationsLookup.cancelButton']}</button>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="otWorkstationDefinitionModal" class="modal definition-modal" tabindex="-1" role="dialog" data-backdrop="static"
     data-keyboard="false">
    <div class="modal-dialog modal-dialog-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <div class="col-auto">
                    <h4 class="modal-title">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal']}</h4>
                </div>

            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group" id="otWorkstationNumberFormGroup">
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <label class="form-label required" for="otWorkstationNumber" data-toggle="tooltip"
                                           title=${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal.form.workstation.number']}>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal.form.workstation.number']}</label>
                                </div>
                                <input type="text" class="form-control right" tabindex="501" id="otWorkstationNumber"
                                       autocomplete="off"/>
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
                                    <label class="form-label required" for="otWorkstationName" data-toggle="tooltip"
                                           title=${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal.form.workstation.name']}>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal.form.workstation.name']}</label>
                                </div>
                                <input type="text" class="form-control" tabindex="502" id="otWorkstationName"
                                       name="otWorkstationName" autocomplete="off"/>
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
                                    <label class="form-label required" for="otWorkstationType" data-toggle="tooltip"
                                           title=${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal.form.workstation.type']}>${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal.form.workstation.type']}</label>
                                </div>
                                <select type="text" tabindex="503" class="form-control custom-select"
                                        id="otWorkstationType" autocomplete="off"></select>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div class="btn-toolbar justify-content-between">
                    <button type="button" tabindex="104" class="btn btn-primary mr-2"
                            id="otSaveWorkstation">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal.save']}</button>
                    <button type="button" tabindex="105" class="btn btn-secondary mr-2"
                            data-dismiss="modal">${translationsMap['technologies.technologyConfigurator.technologyConfiguratorWizard.otWorkstationDefinitionModal.cancelButton']}</button>
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
        src="${pageContext.request.contextPath}/technologies/public/js/technologyConfiguratorWizard.js?ver=${buildNumber}"></script>

