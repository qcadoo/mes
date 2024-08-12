var QCD = QCD || {};

var getConfiguratorConfigurationPath = " /rest/technologyConfigurator/configuration";

QCD.technologyConfiguratorContext = {};
QCD.technologyConfiguratorContext.configuration = {};
QCD.technologyConfiguratorContext.technology = {};
QCD.technologyConfiguratorContext.technology.product = null;
QCD.technologyConfiguratorContext.technology.description = null;
QCD.technologyConfiguratorContext.operations = [];
QCD.technologyConfiguratorContext.technologyOperations = [];
QCD.technologyConfiguratorContext.node = 0;
QCD.technologyConfiguratorContext.technology.lastOperationIndex = null;
QCD.technologyConfiguratorContext.technology.currentMaterialIndex = null;
QCD.technologyConfiguratorContext.technology.currentTOCIndex = null;
QCD.technologyConfiguratorContext.technology.productEvents = [];
QCD.technologyConfiguratorContext.technology.workstationEvents = [];

QCD.technologyConfiguratorContext.workstationTypes = [];

QCD.technologyConfiguratorContext.operationRowAdded = false;

var messagesController = new QCD.MessagesController();

QCD.technologyConfigurator = (function () {

    function intConfiguration() {

        $.ajax({
            url : getConfiguratorConfigurationPath,
            type : "GET",
            async : false,
            beforeSend : function() {
            },
            success : function(data) {
                QCD.technologyConfiguratorContext.configuration = data;
                if(QCD.technologyConfiguratorContext.configuration.typeOfProductionRecording === '02cumulated') {
                    disableStep(3);
                }
            },
            error : function(data) {
                QCD.terminalView.showMessage('failure', QCD
                    .translate(failureMessage), QCD
                    .translate(data.message), false);
            },
            complete : function() {
            }
        });
    }

    function init() {
        (function ($) {
            'use strict';

            $.fn.bootstrapTable.locales['en-US'] = {
                formatLoadingMessage: function () {
                    return 'Loading, please wait...';
                },
                formatRecordsPerPage: function (pageNumber) {
                    return pageNumber + ' rows per page';
                },
                formatShowingRows: function (pageFrom, pageTo, totalRows) {
                    return 'Showing ' + pageFrom + ' to ' + pageTo + ' of ' + totalRows + ' rows';
                },
                formatSearch: function () {
                    return 'Search';
                },
                formatNoMatches: function () {
                    return '';
                },
                formatPaginationSwitch: function () {
                    return 'Hide/Show pagination';
                },
                formatRefresh: function () {
                    return 'Refresh';
                },
                formatToggle: function () {
                    return 'Toggle';
                },
                formatColumns: function () {
                    return 'Columns';
                },
                formatAllRows: function () {
                    return 'All';
                },
                formatExport: function () {
                    return 'Export data';
                },
                formatClearFilters: function () {
                    return 'Clear filters';
                }
            };

            $.extend($.fn.bootstrapTable.defaults, $.fn.bootstrapTable.locales['en-US']);

        })(jQuery);

        (function ($) {
            'use strict';

            $.fn.bootstrapTable.locales['pl-PL'] = {
                formatLoadingMessage: function () {
                    return 'Ładowanie, proszę czekać...';
                },
                formatRecordsPerPage: function (pageNumber) {
                    return pageNumber + ' rekordów na stronę';
                },
                formatShowingRows: function (pageFrom, pageTo, totalRows) {
                    return 'Wyświetlanie rekordów od ' + pageFrom + ' do ' + pageTo + ' z ' + totalRows;
                },
                formatSearch: function () {
                    return 'Szukaj';
                },
                formatNoMatches: function () {
                    return '';
                },
                formatRefresh: function () {
                    return 'Odśwież';
                },
                formatToggle: function () {
                    return 'Przełącz';
                },
                formatColumns: function () {
                    return 'Kolumny';
                }
            };

            $.extend($.fn.bootstrapTable.defaults, $.fn.bootstrapTable.locales['pl-PL']);

        })(jQuery);


        $("#otSelectProduct").prop('disabled', true);
        $('#otProducts').on('check.bs.table', function (row, $element) {
            $("#otSelectProduct").prop('disabled', false);
        });
        $('#otProducts').on('uncheck.bs.table', function (row, $element) {
            $("#otSelectProduct").prop('disabled', true);
        });

        QCD.technologyConfiguratorContext.technologyConfiguratorWizardBody = $('#technologyConfiguratorWizard').clone();

        $("#technologyConfiguratorWizard").on('hidden.bs.modal', function () {
            $('#technologyConfiguratorWizard').remove();
            var myClone = QCD.technologyConfiguratorContext.technologyConfiguratorWizardBody.clone();
            $('body').append(myClone);
        });

        $("#technologyConfiguratorWizard").modal();

        var form = $("#technologyConfiguratorWizardForm");

        form.steps({
            headerTag: "h3",
            bodyTag: "fieldset",
            transitionEffect: "fade",
            labels: {
                previous: QCD.translate('technologies.technologyConfigurator.technologyConfiguratorWizard.previous'),
                next: QCD.translate('technologies.technologyConfigurator.technologyConfiguratorWizard.next'),
                finish: QCD.translate('technologies.technologyConfigurator.technologyConfiguratorWizard.finish'),
                current: ''
            },
            titleTemplate: '<div class="title"><span class="number">#index#</span>#title#</div>',
            onStepChanging: function (event, currentIndex, newIndex) {


                if (currentIndex == 0) {
                    var invalid = false;
                    if ($("#otProduct").val() == null || $("#otProduct").val() === '' || QCD.technologyConfiguratorContext.technology.product == null) {
                        $("#otProduct").addClass('is-invalid');
                        invalid = true;

                    } else {
                        $("#otProduct").removeClass('is-invalid');
                    }

                    if ($("#otQuantity").val() == null || $("#otQuantity").val() === '') {
                        $("#otQuantity").addClass('is-invalid');
                        invalid = true;
                    } else {
                        $("#otQuantity").removeClass('is-invalid');
                        var validQ = validQuantity("otQuantity");
                        if (!validQ) {
                            return false;
                        }
                    }

                    if (invalid) {
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.emptyField"),
                            false);
                    }
                    if (!invalid) {
                        var data = QCD.technologyConfiguratorContext.technologyOperations;
                        $.each(data, function (i, oper) {
                            $.each(oper.materials, function (i, material) {
                                var calculatedQuantity = material.quantityPerUnit * $('#otQuantity').val();
                                material.quantity = parseFloat(calculatedQuantity.toFixed(5));
                                $('#quantityProductIn-' + material.index).val(material.quantity);
                            });
                        });
                        if(newIndex == 4) {
                            preparePreview();
                        }
                    }
                    return !invalid;
                } else if (currentIndex == 1) {
                    var invalid = false;

                    var techOperations = QCD.technologyConfiguratorContext.technologyOperations;
                    $.each(techOperations, function (i, op) {
                        if (op.operationId == null || $('#operation-' + op.index).find('option:selected').text() == '') {
                            $('#operation-' + op.index).addClass('is-invalid');
                            invalid = true;

                        } else {
                            $('#operation-' + op.index).removeClass('is-invalid');
                        }


                    });

                    if (invalid) {
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.emptyField"),
                            false);
                    }
                    if (QCD.technologyConfiguratorContext.technologyOperations.length < 1) {
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.technologyOperationsNotDefined"),
                            false);
                        invalid = true;

                    }
                    if (!invalid) {
                        if(QCD.technologyConfiguratorContext.operationRowAdded
                            && newIndex !== 2) {

                            var invalid = false;

                            var data = QCD.technologyConfiguratorContext.technologyOperations;
                            var last_element = data[data.length - 1];
                            if (!last_element.materials || last_element.materials.length == 0) {
                                invalid = true;
                            }
                            if(!invalid) {
                                $.each(data, function (i, oper) {

                                    $.each(oper.materials, function (i, material) {
                                        if (material.product == null || material.product === '' || material.productId == null) {
                                            invalid = true;
                                        }

                                        if (material.quantity == null || material.quantity === '') {
                                            invalid = true;
                                        } else {
                                            var validQ = validQuantity("quantityProductIn-" + material.index);
                                            if (!validQ) {
                                                invalid = true;
                                            }
                                        }

                                        if (material.quantityPerUnit == null || material.quantityPerUnit === '') {
                                            invalid = true;
                                        } else {
                                            var validQ = validQuantity("quantityPerUnitProductIn-" + material.index);
                                            if (!validQ) {
                                                invalid = true;
                                            }
                                        }
                                    });

                                });
                            }
                            if (invalid) {
                                gotToStep(2);
                                return false;
                            }
                        }
                        QCD.technologyConfiguratorContext.operationRowAdded = false;
                        prepareOperationMaterials();
                        if(newIndex == 4) {
                            preparePreview();
                        }
                    }
                    return !invalid;
                } else if (currentIndex == 2) {
                    var invalid = false;
                    var exist = false;

                    var data = QCD.technologyConfiguratorContext.technologyOperations;
                    var last_element = data[data.length - 1];
                    if (last_element.materials.length == 0) {
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.materialCannotBeEmpty"),
                            false);
                        invalid = true;
                    }
                    $.each(data, function (i, oper) {

                        $.each(oper.materials, function (i, material) {
                            if (material.product == null || material.product === '' || material.productId == null) {
                                $('#inProduct-' + material.index).addClass('is-invalid');
                                invalid = true;

                            } else {
                                $('#inProduct-' + material.index).removeClass('is-invalid');
                            }

                            if (material.quantity == null || material.quantity === '') {
                                $('#quantityProductIn-' + material.index).addClass('is-invalid');
                                invalid = true;

                            } else {
                                $('#quantityProductIn-' + material.index).removeClass('is-invalid');
                                var validQ = validQuantity("quantityProductIn-" + material.index);
                                if (!validQ) {
                                    invalid = true;
                                }
                            }

                            if (material.quantityPerUnit == null || material.quantityPerUnit === '') {
                                $('#quantityPerUnitProductIn-' + material.index).addClass('is-invalid');
                                invalid = true;

                            } else {
                                $('#quantityPerUnitProductIn-' + material.index).removeClass('is-invalid');
                                var validQ = validQuantity("quantityPerUnitProductIn-" + material.index);
                                if (!validQ) {
                                    invalid = true;
                                }
                            }
                        });
                        if (!invalid) {
                            $.each(oper.materials, function (i, material) {

                                if (!exist) {
                                    $.each(oper.materials, function (i, reMaterial) {
                                        if ($('#inProduct-' + reMaterial.index).val() !== '' && material.index != reMaterial.index && (material.productId == reMaterial.productId ||
                                            QCD.technologyConfiguratorContext.technology.product.id == reMaterial.productId)) {

                                            if (!exist) {
                                                $('#inProduct-' + reMaterial.index).addClass('is-invalid');
                                            }
                                            exist = true;
                                        }
                                    });
                                }
                            });
                        }
                    });
                    if (invalid) {
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.emptyField"),
                            false);
                    }
                    if (exist) {
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.productAlreadySelected"),
                            false);
                        invalid = true;
                    }

                    if (!invalid) {
                        $("#workstations").bootstrapTable('load', QCD.technologyConfiguratorContext.technologyOperations);
                        if(newIndex == 4) {
                            preparePreview();
                        }
                    }
                    return !invalid;
                } else if (currentIndex == 3) {
                    var techOperations = QCD.technologyConfiguratorContext.technologyOperations;
                    $.each(techOperations, function (i, op) {
                        if ($('#workstation-' + op.index).val() == '') {
                            op.workstationId = null;
                            op.workstation = null;
                        }


                    });
                    if(newIndex == 4) {
                        preparePreview();
                    }
                    return true;
                } else if (currentIndex == 4) {
                    return true;
                }
            },
            onStepChanged: function (event, currentIndex, priorIndex) {
                if (currentIndex == 3) {
                    if(QCD.technologyConfiguratorContext.configuration.typeOfProductionRecording === '02cumulated') {
                        if(priorIndex < 3) {
                            $("#technologyConfiguratorWizardForm-t-" + 4).parent().removeClass("disabled");
                            $("#technologyConfiguratorWizardForm-t-" + 4).parent().addClass("done");
                            $("#technologyConfiguratorWizardForm-t-" + 4).parent()._enableAria(true);
                            gotToStep(4);
                            disableStep(3);
                        } else {
                            $("#technologyConfiguratorWizardForm-t-" + 2).parent().removeClass("disabled");
                            $("#technologyConfiguratorWizardForm-t-" + 2).parent().addClass("done");
                            $("#technologyConfiguratorWizardForm-t-" + 2).parent()._enableAria(true);
                            gotToStep(2);
                            disableStep(3);
                        }

                    }
                }
                return true;

            },
            onFinishing: function (event, currentIndex) {

                return true;
            },
            onFinished: function (event, currentIndex) {
                createTechnology();
            },
            onInit: function (event, currentIndex) {
                $("#otQuantity").val("1");

                $(".actions").find(".cancelBtn").remove();

                var saveA = $("<a>").attr("href", "#").addClass("cancelBtn").text(QCD.translate('technologies.technologyConfigurator.technologyConfiguratorWizard.cancel'));
                var saveBtn = $("<li>").attr("aria-disabled", false).append(saveA);

                $(document).find(".actions ul").prepend(saveBtn);
                operations();
                intConfiguration();


            },
        });

        $(".close").click(function () {
            window.parent.goToPage('/page/technologies/technologiesList.html', false, false);
        });
        $(".cancelBtn").click(function () {
            window.parent.goToPage('/page/technologies/technologiesList.html', false, false);
        });

        $("#otSaveProduct").unbind();
        $("#saveOperation").unbind();
        $("#otSaveWorkstation").unbind();

        $("#otGetProduct").click(function () {

            $("#otProductsLookup").appendTo("body").one('shown.bs.modal', function () {
                $("#otProducts").bootstrapTable('destroy');
                $("#technologyConfiguratorWizard").addClass('disableModal');
                fillProducts();
            }).modal('show');


        });

        $('#otProductsLookup').on('hidden.bs.modal', function () {
            $("#technologyConfiguratorWizard").removeClass('disableModal');
        });

        $("#otSelectProduct").click(function () {
            var selectedProducts = $("#otProducts").bootstrapTable('getSelections');
            QCD.technologyConfiguratorContext.technology.product = selectedProducts[0];
            $("#otProductsLookup").modal('hide');
            $("#otProduct").val(QCD.technologyConfiguratorContext.technology.product.number);
            $("#otUnit").val(QCD.technologyConfiguratorContext.technology.product.unit);

        });

        $('#otProductsLookup').on('hidden.bs.modal', function (e) {
            $("#otSelectProduct").prop('disabled', true);
        });

        $('#otProduct').typeahead({
            minLength: 3,
            source: function (query, process) {
                return $.getJSON(
                    'rest/productsTypeahead', {
                        query: $("#otProduct").val()
                    },
                    function (data) {
                        logoutIfSessionExpired(data);
                        var resultList = data.entities.map(function (item) {
                            var aItem = {
                                id: item.id,
                                name: item.code,
                                number: item.code,
                                unit: item.unit
                            };
                            return aItem;
                        });
                        return process(resultList);
                    }).fail(function (jqxhr, textStatus, error) {
                    if (jqxhr.status == 401) {
                        window.location = "/login.html?timeout=true";
                    }
                });
            }

        });


        $('#otProduct').change(function () {
            var current = $('#otProduct').typeahead("getActive");
            if (current) {
                QCD.technologyConfiguratorContext.technology.product = current;
                $("#otUnit").val(QCD.technologyConfiguratorContext.technology.product.unit);
            }

        });
        $('#otProduct').keyup(function (e) {
            if (e.keyCode != 13) {
                $('#otProduct').removeData("active");
                QCD.technologyConfiguratorContext.technology.product = null;
                $("#otUnit").val("");
                $("#otTechnology").prop('disabled', true);
                $("#otGetTechnology").prop('disabled', true);
            }
        });
        $("#otAddProduct").click(function () {
            units();
            $("#otProductNumber").removeClass('is-invalid');
            $("#otProductName").removeClass('is-invalid');
            $("#otProductUnit").removeClass('is-invalid');
            $("#otProductNumber").val(null);
            $("#otProductName").val(null);
            $("#otProductUnit").val(null);

            $('#otProductDefinitionModal').appendTo("body").modal('show');
            $("#technologyConfiguratorWizard").addClass('disableModal');
        });

        $('#otProductDefinitionModal').on('hidden.bs.modal', function () {
            $("#technologyConfiguratorWizard").removeClass('disableModal');
        });

        $("#otSaveProduct").click(function () {
            var invalid = false;
            $("#otProductNumber").removeClass('is-invalid');
            $("#otProductName").removeClass('is-invalid');
            $("#otProductUnit").removeClass('is-invalid');

            if ($("#otProductNumber").val() == null || $("#otProductNumber").val() === '') {
                $("#otProductNumber").addClass('is-invalid');
                invalid = true;

            }

            if ($("#otProductName").val() == null || $("#otProductName").val() === '') {
                $("#otProductName").addClass('is-invalid');
                invalid = true;

            }

            if ($("#otProductUnit").val() == null || $("#otProductUnit").val() === '') {
                $("#otProductUnit").addClass('is-invalid');
                invalid = true;

            }
            if (invalid) {
                showMessage(
                    'failure',
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.emptyField"),
                    false);
                return;
            }

            var product = {};
            product.number = $("#otProductNumber").val();
            product.name = $("#otProductName").val();
            product.unit = $("#otProductUnit").val();
            if (QCD.technologyConfiguratorContext.technology.currentMaterialIndex) {
                product.globalTypeOfMaterial = '01component';

            } else {
                product.globalTypeOfMaterial = '03finalProduct';
            }


            $.ajax({
                url: "rest/product",
                type: "POST",
                data: JSON.stringify(product),
                contentType: "application/json",
                beforeSend: function () {
                    $("#loader").appendTo("body").modal('show');
                },
                success: function (data) {
                    logoutIfSessionExpired(data);
                    if (data.code === 'OK') {
                        if (QCD.technologyConfiguratorContext.technology.currentMaterialIndex) {
                            $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, e) {
                                if (e.index == QCD.technologyConfiguratorContext.technology.currentTOCIndex) {
                                    $.each(e.materials, function (i, e) {
                                        if (e.index == QCD.technologyConfiguratorContext.technology.currentMaterialIndex) {
                                            e.productId = data.id;
                                            ;
                                            e.productNumber = data.number;
                                            e.product = data.number;
                                            e.unit = data.unit;
                                            $("#otProductDefinitionModal").modal('hide');
                                            $('#inProduct-' + QCD.technologyConfiguratorContext.technology.currentMaterialIndex).val(data.number);
                                            $('#inProduct-' + QCD.technologyConfiguratorContext.technology.currentMaterialIndex).removeClass('is-invalid');
                                            $('#unit-' + QCD.technologyConfiguratorContext.technology.currentMaterialIndex).val(data.unit);

                                        }
                                    });
                                }
                            });
                        } else {

                            QCD.technologyConfiguratorContext.technology.product = {};
                            QCD.technologyConfiguratorContext.technology.product.id = data.id;
                            QCD.technologyConfiguratorContext.technology.product.number = data.number;
                            QCD.technologyConfiguratorContext.technology.product.name = data.name;
                            QCD.technologyConfiguratorContext.technology.product.unit = data.unit;
                            $("#otProductDefinitionModal").modal('hide');
                            $("#otProduct").val(QCD.technologyConfiguratorContext.technology.product.number);
                            $("#otUnit").val(QCD.technologyConfiguratorContext.technology.product.unit);
                            QCD.technologyConfiguratorContext.technology.technology = null;
                            $("#otTechnology").val("");

                        }
                    } else {
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            data.message,
                            false);
                    }

                    $("#loader").modal('hide');

                },
                error: function (data) {
                    logoutIfSessionExpired(data);
                    if (data.status == 401) {
                        window.location = "/login.html?timeout=true";
                    }
                    showMessage('failure',
                        QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error"),
                        QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.internalError"),
                        false);

                },
                complete: function () {
                    $("#loader").modal('hide');
                }
            });


        });

        $("#removeTechnologyOperation").prop('disabled', true);

        $('#technologyOperations').on('check.bs.table', function (row, $element) {
            if (!QCD.technologyConfiguratorContext.technology.technology) {
                $("#removeTechnologyOperation").prop('disabled', false);
            }
        });

        $('#technologyOperations').on('uncheck.bs.table', function (row, $element) {
            if (!QCD.technologyConfiguratorContext.technology.technology) {

                $("#removeTechnologyOperation").prop('disabled', true);
            }
        });

        var $technologyOperations = $("#technologyOperations")
            .bootstrapTable({
                uniqueId: 'index',
                search: false,
                undefinedText: '',
                showColumns: false,
                pagination: false,
                clickToSelect: true,
                singleSelect: true,
                maintainSelected: false,
                sortName: 'node',
                sortOrder: 'asc',
                showFooter: false,
                height: 280,
                locale: (QCD.currentLang + '-' + QCD.currentLang
                    .toUpperCase())
            });
        $("#newTechnologyOperation").click(function () {
            var technologyOperation = {};
            technologyOperation.id = null;
            technologyOperation.operationId = null;
            technologyOperation.operation = null;
            QCD.technologyConfiguratorContext.node = QCD.technologyConfiguratorContext.node + 1;
            technologyOperation.node = QCD.technologyConfiguratorContext.node + ".";
            technologyOperation.index = new Date().getTime();
            QCD.technologyConfiguratorContext.technologyOperations.push(technologyOperation);
            $technologyOperations.bootstrapTable('load', QCD.technologyConfiguratorContext.technologyOperations);
            QCD.technologyConfiguratorContext.operationRowAdded = true;
        });

        $("#removeTechnologyOperation").click(function () {
            var ids = $.map($("#technologyOperations").bootstrapTable('getSelections'), function (row) {
                return row.index
            })
            $("#technologyOperations").bootstrapTable('remove', {
                field: 'index',
                values: ids
            })
            $("#removeTechnologyOperation").prop('disabled', true);
            QCD.technologyConfiguratorContext.node = 0;
            var reIndex = 1;
            $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, toc) {
                toc.node = reIndex + ".";
                QCD.technologyConfiguratorContext.node = reIndex;
                reIndex = reIndex + 1;
            });
            $('#technologyOperations').bootstrapTable('load', QCD.technologyConfiguratorContext.technologyOperations);

        });


        $('#operationDefinitionModal').on('hidden.bs.modal', function () {
            $("#technologyConfiguratorWizard").removeClass('disableModal');
        });

        $("#saveOperation").click(function () {
            var invalid = false;
            $("#operationNumber").removeClass('is-invalid');
            $("#operationName").removeClass('is-invalid');

            if ($("#operationNumber").val() == null || $("#operationNumber").val() === '') {
                $("#operationNumber").addClass('is-invalid');
                invalid = true;

            }

            if ($("#operationName").val() == null || $("#operationName").val() === '') {
                $("#operationName").addClass('is-invalid');
                invalid = true;

            }

            if (invalid) {
                showMessage(
                    'failure',
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.emptyField"),
                    false);
                return;
            }

            var oper = {};
            oper.number = $("#operationNumber").val();
            oper.name = $("#operationName").val();

            $.ajax({
                url: "rest/operation",
                type: "POST",
                data: JSON.stringify(oper),
                contentType: "application/json",
                beforeSend: function () {
                    $("#loader").appendTo("body").modal('show');
                },
                success: function (data) {
                    logoutIfSessionExpired(data);
                    if (data.code === 'OK') {
                        $("#operationDefinitionModal").modal('hide');
                        var techOperations = QCD.technologyConfiguratorContext.technologyOperations;
                        $.each(techOperations, function (i, e) {
                            if (e.index == QCD.technologyConfiguratorContext.technology.lastOperationIndex) {
                                e.operationId = data.id;
                                e.operation = data.number;
                                operations();
                                $('#technologyOperations').bootstrapTable('load', QCD.technologyConfiguratorContext.technologyOperations);
                            }
                        });
                    } else {
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            data.message,
                            false);
                    }
                },
                error: function (data) {
                    logoutIfSessionExpired(data);
                    if (data.status == 401) {
                        window.location = "/login.html?timeout=true";
                    }
                    showMessage('failure',
                        QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error"),
                        QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.internalError"),
                        false);

                },
                complete: function () {
                    $("#loader").modal('hide');
                }
            });


        });

        $('#otMaterialsLookup').on('hidden.bs.modal', function () {
            $("#technologyConfiguratorWizard").removeClass('disableModal');
        });
        $("#selectOtMaterial").prop('disabled', true);

        $('#otMaterialsItem').on('check.bs.table', function (row, $element) {
            $("#selectOtMaterial").prop('disabled', false);
        });

        $('#otMaterialsItem').on('uncheck.bs.table', function (row, $element) {
            $("#selectOtMaterial").prop('disabled', true);
        });

        $('#otMaterialsLookup').on('hidden.bs.modal', function (e) {
            $("#selectOtMaterial").prop('disabled', true);
        });

        $('[data-toggle="tooltip"]').tooltip({
            trigger: 'hover'
        })

        $('input.decimal').on('input', function () {
            this.value = this.value.replace(/[^0-9.,]/g, '').replace(/(\..*)\./g, '$1').replace(/(\,.*)\,/g, '$1');
        });


        var operationMaterialsObserver = new MutationObserver(function (mutations) {
            $('[data-toggle="tooltip"]').tooltip({
                trigger: 'hover'
            })

            mutations.forEach(function (mutation) {
                $("input.decimal").unbind();
                $('input.decimal').on('input', function () {
                    this.value = this.value.replace(/[^0-9.,]/g, '').replace(/(\..*)\./g, '$1').replace(/(\,.*)\,/g, '$1');
                });

            });
        });
        const operationMaterialsGroupNode = document.getElementById('operationMaterials-group');

        operationMaterialsObserver.observe(operationMaterialsGroupNode, {
            attributes: true,
            characterData: false,
            childList: false,
            subtree: true,
            attributeOldValue: false,
            characterDataOldValue: false
        });

        var operationObserver = new MutationObserver(function (mutations) {
            $('[data-toggle="tooltip"]').tooltip({
                trigger: 'hover'
            })


        });
        const operationGroupNode = document.getElementById('technologyOperations-group');

        operationObserver.observe(operationGroupNode, {
            attributes: true,
            characterData: false,
            childList: false,
            subtree: true,
            attributeOldValue: false,
            characterDataOldValue: false
        });

        var workstationsObserver = new MutationObserver(function (mutations) {
            $('[data-toggle="tooltip"]').tooltip({
                trigger: 'hover'
            })


        });
        const workstationsGroupNode = document.getElementById('workstations-group');

        workstationsObserver.observe(workstationsGroupNode, {
            attributes: true,
            characterData: false,
            childList: false,
            subtree: true,
            attributeOldValue: false,
            characterDataOldValue: false
        });

        $('#workstationItems').on('check.bs.table', function (row, $element) {
            $("#otSelectWorkstation").prop('disabled', false);
        });
        $('#workstationItems').on('uncheck.bs.table', function (row, $element) {
            $("#otSelectWorkstation").prop('disabled', true);
        });
        //workstations
        var $workstations = $("#workstations")
            .bootstrapTable({
                uniqueId: 'index',
                search: false,
                undefinedText: '',
                showColumns: false,
                pagination: false,
                clickToSelect: true,
                singleSelect: true,
                maintainSelected: false,
                sortName: 'node',
                sortOrder: 'asc',
                showFooter: false,
                height: 320,
                locale: (QCD.currentLang + '-' + QCD.currentLang
                    .toUpperCase())
            });
        $workstations.bootstrapTable('load', QCD.technologyConfiguratorContext.technologyOperations);

        $("#otSelectWorkstation").prop('disabled', true);


        $('#otWorkstationsLookup').on('hidden.bs.modal', function () {
            $("#technologyConfiguratorWizard").removeClass('disableModal');
        });


        $("#otSelectWorkstation").click(function () {
            otSelectWorkstation();
        });

        $("#otSaveWorkstation").click(function () {
            otSaveWorkstation();
        });

    }


    function fillProducts() {
        var $productsEntries = $("#otProducts")
            .bootstrapTable({
                url: 'rest/productsByPage',
                queryParams: function (p) {
                    return {
                        limit: p.limit,
                        offset: p.offset,
                        sort: p.sort,
                        order: p.order,
                        search: p.search

                    };
                },
                uniqueId: 'id',
                undefinedText: '',
                sidePagination: 'server',
                responseHandler: function (res) {
                    logoutIfSessionExpired(res);
                    return res;
                },
                search: true,
                showColumns: true,
                pagination: true,
                clickToSelect: true,
                singleSelect: true,
                maintainSelected: true,
                showFooter: false,
                sortName: 'number',
                sortOrder: 'asc',
                height: 500,
                locale: (QCD.currentLang + '-' + QCD.currentLang
                    .toUpperCase())
            });
    }

    function units() {
        $.ajax({
            url: "rest/units",
            type: "GET",
            async: false,
            beforeSend: function (data) {
                $("#loader").appendTo("body").modal('show');

            },
            success: function (data) {
                logoutIfSessionExpired(data);
                $("#otProductUnit").empty();

                $("#otProductUnit").append('<option value=""></option>');
                for (const [key, value] of Object.entries(data)) {


                    $("#otProductUnit").append(
                        '<option value="' + value.key + '">' + value.key +
                        '</option>');

                }

            },
            error: function (data) {
                logoutIfSessionExpired(data);
                if (data.status == 401) {
                    window.location = "/login.html?timeout=true";
                }
                showMessage('failure',
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error"),
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.internalError"),
                    false);
            },
            complete: function () {
                $("#loader").modal('hide');
            }
        });
    }

    function operations() {
        $.ajax({
            url: "rest/operations",
            type: "GET",
            async: false,
            beforeSend: function (data) {
                $("#loader").appendTo("body").modal('show');

            },
            success: function (data) {
                logoutIfSessionExpired(data);
                QCD.technologyConfiguratorContext.operations = data.operations;
            },
            error: function (data) {
                logoutIfSessionExpired(data);
                if (data.status == 401) {
                    window.location = "/login.html?timeout=true";
                }
                showMessage('failure',
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error"),
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.internalError"),
                    false);
            },
            complete: function () {
                $("#loader").modal('hide');
            }
        });

    }

    function openOperationDefinition(element) {

        $("#operationDefinitionModal").one('shown.bs.modal', function () {
            $("#operationNumber").val("");
            $("#operationName").val("");
            $("#technologyConfiguratorWizard").addClass('disableModal');
            QCD.technologyConfiguratorContext.technology.lastOperationIndex = element;
        }).appendTo("body").modal('show');
    }

    function onOperationSelect(element) {
        var data = QCD.technologyConfiguratorContext.technologyOperations;
        $.each(data, function (i, e) {
            if (e.index == element) {
                e.operationId = $('#operation-' + element).val();
                e.operation = $('#operation-' + element).find('option:selected').text();
            }
        });
    }

    function prepareOperationMaterials() {
        var operationMaterialsDiv = $('#operationMaterials-group');
        operationMaterialsDiv.empty();
        operationMaterialsDiv.append(createTOCDivDescription());
        var data = QCD.technologyConfiguratorContext.technologyOperations;
        $.each(data, function (i, oper) {
            operationMaterialsDiv.append(createTOCDiv(oper));
            var $operationMaterialsEntries = $("#operation-materials-" + oper.index)
                .bootstrapTable({
                    uniqueId: 'index',
                    undefinedText: '',
                    search: false,
                    showColumns: false,
                    pagination: false,
                    clickToSelect: false,
                    showHeader: false,
                    singleSelect: false,
                    maintainSelected: true,
                    showFooter: false,
                    sortName: 'number',
                    sortOrder: 'asc',
                    height: 170,
                    locale: (QCD.currentLang + '-' + QCD.currentLang
                        .toUpperCase())
                });

            if (oper.materials && oper.materials.length > 0) {
                $operationMaterialsEntries.bootstrapTable('load', oper.materials);
            } else {
                var operationMaterial = {};
                operationMaterial.operationIndex = oper.index;
                operationMaterial.product = null;
                operationMaterial.productId = null;
                operationMaterial.quantity = null;
                operationMaterial.quantityPerUnit = null;
                operationMaterial.unit = null;
                operationMaterial.index = new Date().getTime();
                oper.materials = [];
                oper.materials.push(operationMaterial);
                $operationMaterialsEntries.bootstrapTable('load', oper.materials);
            }

        });
    }

    function createTOCDivDescription() {
        var descriptionDiv = '<div class="form-group no-margin" >';
        descriptionDiv = descriptionDiv + '<div class="row">';
        descriptionDiv = descriptionDiv + '<div class="col-sm-8">';
        descriptionDiv = descriptionDiv + '<div class="input-group">';
        descriptionDiv = descriptionDiv + '<div class="input-group-prepend">';
        descriptionDiv = descriptionDiv + '<label id="description-label" class="form-label">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.form.operationMaterials.description") + '</label>';
        descriptionDiv = descriptionDiv + '</div>';
        descriptionDiv = descriptionDiv + '</div>';
        descriptionDiv = descriptionDiv + '</div>';
        descriptionDiv = descriptionDiv + '</div>';
        descriptionDiv = descriptionDiv + '</div><hr></hr>';

        return descriptionDiv;
    }

    function createTOCDiv(oper) {
        var operElement = '<div> <div class="form-group"><div class="row">';
        operElement = operElement + '<div class="col-sm-4">';
        operElement = operElement + '<div class="input-group"><div class="input-group-prepend">';
        operElement = operElement + '<label class="form-label required small-label">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.operation") + '</label>';
        operElement = operElement + '</div>';
        operElement = operElement + ' <input disabled type="text" class="form-control" tabindex="1" autocomplete="off" value="' + oper.operation + '"></input> ';
        operElement = operElement + '</div>';
        operElement = operElement + '</div>';
        operElement = operElement + '<div class="col-sm-2">';
        operElement = operElement + '<div class="input-group"><div class="input-group-prepend">';
        operElement = operElement + '<label class="form-label required small-label">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.node") + '</label>';
        operElement = operElement + '</div>';
        operElement = operElement + ' <input disabled type="text" class="form-control" tabindex="1" autocomplete="off" value="' + oper.node + '"></input> ';
        operElement = operElement + '</div>';
        operElement = operElement + '</div>';
        operElement = operElement + '<div class="col-sm-4">';
        operElement = operElement + '<div>';
        operElement = operElement + '<button type="button" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.technologyConfigurator.addMaterialToOperation(' + new String(oper.index) + ')">';
        operElement = operElement + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.technologyOperations.addMaterial");
        operElement = operElement + '</button>';
        operElement = operElement + '</div>';
        operElement = operElement + '</div>';
        operElement = operElement + '</div>';
        operElement = operElement + '</div>';

        operElement = operElement + '<div class="form-group">';
        operElement = operElement + '<div class="row">';
        operElement = operElement + '<table id="operation-materials-' + oper.index + '" data-search="false" >';
        operElement = operElement + '<thead>';
        operElement = operElement + '<tr>';
        operElement = operElement + '<th data-field="index" data-sortable="false" data-switchable="false" data-visible="false"></th>';
        operElement = operElement + '<th data-field="product" data-formatter="productInFormatter" data-sortable="false" data-align="center">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.product") + '</th>';
        operElement = operElement + '<th data-field="quantity" data-formatter="quantityProductInFormatter" data-sortable="false" data-align="center">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.quantity") + '</th>';
        operElement = operElement + '<th data-field="quantityPerUnit" data-formatter="quantityPerUnitProductInFormatter" data-sortable="false" data-align="center">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.quantityPerUnit") + '</th>';
        operElement = operElement + '<th data-field="unit" data-width="70" data-formatter="unitFormatter" data-sortable="false" data-align="center">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.unit") + '</th>';
        operElement = operElement + '<th data-field="action"  data-formatter="actionFormatter" data-sortable="false" data-align="center">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.unit") + '</th>';
        operElement = operElement + ' </tr>';
        operElement = operElement + '</thead>';
        operElement = operElement + '</table>';
        operElement = operElement + '</div>';
        operElement = operElement + '</div>';
        operElement = operElement + '</div><hr></hr>';

        return operElement;
    }

    function addMaterialToOperation(operationIndex) {
        var data = QCD.technologyConfiguratorContext.technologyOperations;
        $.each(data, function (i, oper) {
            if (oper.index == operationIndex) {
                var operationMaterial = {};
                operationMaterial.operationIndex = oper.index;
                operationMaterial.product = null;
                operationMaterial.productId = null;
                operationMaterial.quantity = null;
                operationMaterial.quantityPerUnit = null;
                operationMaterial.unit = null;
                operationMaterial.index = new Date().getTime();
                oper.materials.push(operationMaterial);
                $("#operation-materials-" + oper.index).bootstrapTable('load', oper.materials);
            }
        });
    }

    function removeMaterialFromOperation(tocIndex, materialIndex) {
        $("div[role=tooltip]").remove();
        $("#operation-materials-" + tocIndex).bootstrapTable('remove', {
            field: 'index',
            values: [materialIndex]
        });

    }

    function openOtMaterialsLookup(tocIndex, materialIndex) {
        $("#otMaterialsLookup").appendTo("body").one('shown.bs.modal', function () {
            QCD.technologyConfiguratorContext.technology.currentMaterialIndex = materialIndex;
            QCD.technologyConfiguratorContext.technology.currentTOCIndex = tocIndex;
            $("#otMaterialsItem").bootstrapTable('destroy');
            $("#technologyConfiguratorWizard").addClass('disableModal');

            fillOtMaterials();
        }).modal('show');
    }

    function fillOtMaterials() {
        var $productsEntries = $("#otMaterialsItem")
            .bootstrapTable({
                url: 'rest/productsByPage',
                queryParams: function (p) {
                    return {
                        limit: p.limit,
                        offset: p.offset,
                        sort: p.sort,
                        order: p.order,
                        search: p.search

                    };
                },
                uniqueId: 'id',
                undefinedText: '',
                sidePagination: 'server',
                responseHandler: function (res) {
                    logoutIfSessionExpired(res);
                    return res;
                },
                search: true,
                showColumns: true,
                pagination: true,
                clickToSelect: true,
                singleSelect: true,
                maintainSelected: false,
                showFooter: false,
                sortName: 'number',
                sortOrder: 'asc',
                height: 500,
                locale: (QCD.currentLang + '-' + QCD.currentLang
                    .toUpperCase())
            });


    }

    function selectOtMaterialsItem() {
        var selectedMaterials = $("#otMaterialsItem").bootstrapTable('getSelections');
        $("#otMaterialsLookup").modal('hide');
        $('#inProduct-' + QCD.technologyConfiguratorContext.technology.currentMaterialIndex).val(selectedMaterials[0].number);
        $('#inProduct-' + QCD.technologyConfiguratorContext.technology.currentMaterialIndex).removeClass('is-invalid');

        $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, e) {
            if (e.index == QCD.technologyConfiguratorContext.technology.currentTOCIndex) {

                var data = e.materials;
                var exist = false;
                $.each(data, function (i, e) {
                    if ((e.productId && e.productId == selectedMaterials[0].id) || QCD.technologyConfiguratorContext.technology.product.id == selectedMaterials[0].id) {

                        $('#inProduct-' + QCD.technologyConfiguratorContext.technology.currentMaterialIndex).addClass('is-invalid');
                        $('#inProduct-' + QCD.technologyConfiguratorContext.technology.currentMaterialIndex).val("");
                        showMessage(
                            'failure',
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                            QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.productAlreadySelected"),
                            false);
                        exist = true;
                        return false;
                    }
                });

                if (!exist) {
                    $.each(data, function (i, e) {
                        if (e.index == QCD.technologyConfiguratorContext.technology.currentMaterialIndex) {
                            e.productId = selectedMaterials[0].id;
                            e.productNumber = selectedMaterials[0].number;
                            e.product = selectedMaterials[0].number;
                            e.unit = selectedMaterials[0].unit;
                            $('#unit-' + QCD.technologyConfiguratorContext.technology.currentMaterialIndex).val(selectedMaterials[0].unit);

                        }
                    });
                }
            }
        });
    }

    function addProductTypeahead(tocIndex, element) {
        if (!QCD.technologyConfiguratorContext.technology.productEvents.includes(element)) {
            $('#inProduct-' + element).typeahead({
                minLength: 3,
                source: function (query, process) {
                    return $.getJSON(
                        'rest/productsTypeahead', {
                            query: $('#inProduct-' + element).val()
                        },
                        function (data) {
                            logoutIfSessionExpired(data);
                            var resultList = data.entities.map(function (item) {
                                var aItem = {
                                    id: item.id,
                                    name: item.code,
                                    number: item.code,
                                    unit: item.unit
                                };
                                return aItem;
                            });
                            return process(resultList);
                        }).fail(function (jqxhr, textStatus, error) {
                        if (jqxhr.status == 401) {
                            window.location = "/login.html?timeout=true";
                        }
                    });
                }

            });

            $('#inProduct-' + element).change(function () {
                var current = $('#inProduct-' + element).typeahead("getActive");
                if (current) {

                    $('#inProduct-' + element).removeClass('is-invalid');
                    $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, e) {
                        if (e.index == tocIndex) {

                            var data = e.materials;


                            $.each(data, function (i, e) {
                                if (e.index == element) {
                                    e.productId = current.id;
                                    e.productNumber = current.number;
                                    e.product = current.number;
                                    e.unit = current.unit;
                                    $('#unit-' + element).val(current.unit);

                                }
                            });

                        }
                    });
                } else {
                    $('#inProduct-' + element).val("");
                }
            });

            $('#inProduct-' + element).keyup(function (e) {
                if (e.keyCode != 13) {

                    $('#inProduct-' + element).removeData("active");
                    var data = QCD.technologyConfiguratorContext.technology.materials;

                    $.each(data, function (i, e) {
                        if (e.index == element) {
                            e.productId = null;
                            e.productNumber = null;
                            e.product = "";
                            e.unit = null;

                            $('#unit-' + element).val("");

                        }
                    });
                }
            });
            QCD.technologyConfiguratorContext.technology.productEvents.push(element);

        }
    }

    function addWorkstationTypeahead(tocIndex) {
        if (!QCD.technologyConfiguratorContext.technology.workstationEvents.includes(tocIndex)) {
            if (QCD.technologyConfiguratorContext.technology.technology) {
                $('#workstation-' + tocIndex).typeahead({
                    minLength: 3,
                    source: function (query, process) {
                        return $.getJSON(
                            'rest/workstations', {
                                query: $('#workstation-' + tocIndex).val(),
                                tocId: tocIndex
                            },
                            function (data) {
                                logoutIfSessionExpired(data);
                                var resultList = data.workstations.map(function (item) {
                                    var aItem = {
                                        id: item.id,
                                        name: item.number,
                                        number: item.number
                                    };
                                    return aItem;
                                });
                                return process(resultList);
                            }).fail(function (jqxhr, textStatus, error) {
                            if (jqxhr.status == 401) {
                                window.location = "/login.html?timeout=true";
                            }
                        });
                    }

                });
            } else {
                $('#workstation-' + tocIndex).typeahead({
                    minLength: 3,
                    source: function (query, process) {
                        return $.getJSON(
                            'rest/workstations', {
                                query: $('#workstation-' + tocIndex).val()
                            },
                            function (data) {
                                logoutIfSessionExpired(data);
                                var resultList = data.workstations.map(function (item) {
                                    var aItem = {
                                        id: item.id,
                                        name: item.number,
                                        number: item.number
                                    };
                                    return aItem;
                                });
                                return process(resultList);
                            }).fail(function (jqxhr, textStatus, error) {
                            if (jqxhr.status == 401) {
                                window.location = "/login.html?timeout=true";
                            }
                        });
                    }

                });
            }

            $('#workstation-' + tocIndex).change(function () {
                var current = $('#workstation-' + tocIndex).typeahead("getActive");
                if (current) {

                    $('#workstation-' + tocIndex).removeClass('is-invalid');
                    $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, e) {
                        if (e.index == tocIndex) {
                            if (e.index == QCD.technologyConfiguratorContext.technology.currentTOCIndex) {
                                e.workstationId = current.id;
                                e.workstation = current.number;
                            }
                        }
                    });
                } else {
                    $('#workstation-' + tocIndex).val("");
                }
            });

            $('#workstation-' + tocIndex).keyup(function (e) {
                if (e.keyCode != 13) {

                    $('#workstation-' + tocIndex).removeData("active");
                    var data = QCD.technologyConfiguratorContext.technology.materials;

                    $.each(data, function (i, e) {
                        if (e.index == tocIndex) {
                            e.workstationId = null
                            e.workstation = "";


                        }
                    });
                }
            });
            QCD.technologyConfiguratorContext.technology.workstationEvents.push(tocIndex);

        }
    }

    function openMaterialDefinition(tocIndex, element) {
        QCD.technologyConfiguratorContext.technology.currentMaterialIndex = element;
        QCD.technologyConfiguratorContext.technology.currentTOCIndex = tocIndex;
        units();
        $("#otProductNumber").removeClass('is-invalid');
        $("#otProductName").removeClass('is-invalid');
        $("#otProductUnit").removeClass('is-invalid');
        $("#otProductNumber").val(null);
        $("#otProductName").val(null);
        $("#otProductUnit").val(null);

        $('#otProductDefinitionModal').appendTo("body").modal('show');
        $("#technologyConfiguratorWizard").addClass('disableModal');


    }

    function openWorkstationDefinition(tocIndex) {
        workstationTypes();
        QCD.technologyConfiguratorContext.technology.currentTOCIndex = tocIndex;
        units();
        $("#otWorkstationNumber").removeClass('is-invalid');
        $("#otWorkstationName").removeClass('is-invalid');
        $("#otWorkstationType").removeClass('is-invalid');
        $("#otWorkstationNumber").val(null);
        $("#otWorkstationName").val(null);
        $("#otWorkstationType").val(null);
        fillWorkstationTypes();
        $('#otWorkstationDefinitionModal').appendTo("body").modal('show');
        $("#technologyConfiguratorWizard").addClass('disableModal');


    }

    function openOtWorkstationsLookup(tocIndex) {
        $("#otWorkstationsLookup").appendTo("body").one('shown.bs.modal', function () {
            QCD.technologyConfiguratorContext.technology.currentTOCIndex = tocIndex;
            $("#workstationItems").bootstrapTable('destroy');
            $("#technologyConfiguratorWizard").addClass('disableModal');

            fillOtWorkstations(tocIndex);
        }).modal('show');
    }

    function fillOtWorkstations(tocIndex) {

        if (QCD.technologyConfiguratorContext.technology.technology) {

            var $workstationsByPage = $("#workstationItems")
                .bootstrapTable({
                    url: 'rest/workstationsByPage',
                    queryParams: function (p) {
                        return {
                            limit: p.limit,
                            offset: p.offset,
                            sort: p.sort,
                            order: p.order,
                            search: p.search,
                            tocId: tocIndex

                        };
                    },
                    uniqueId: 'id',
                    undefinedText: '',
                    sidePagination: 'server',
                    responseHandler: function (res) {
                        logoutIfSessionExpired(res);
                        return res;
                    },
                    search: true,
                    showColumns: true,
                    pagination: true,
                    clickToSelect: true,
                    singleSelect: true,
                    maintainSelected: false,
                    showFooter: false,
                    sortName: 'number',
                    sortOrder: 'asc',
                    height: 500,
                    locale: (QCD.currentLang + '-' + QCD.currentLang
                        .toUpperCase())
                });
        } else {

            var operationId;
            $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, e) {
                if (e.index == QCD.technologyConfiguratorContext.technology.currentTOCIndex) {
                    operationId = e.operationId;
                }
            });

            var $workstationsByPage = $("#workstationItems")
                .bootstrapTable({
                    url: 'rest/workstationsByPage',
                    queryParams: function (p) {
                        return {
                            limit: p.limit,
                            offset: p.offset,
                            sort: p.sort,
                            order: p.order,
                            search: p.search,
                            operation: operationId
                        };
                    },
                    uniqueId: 'id',
                    undefinedText: '',
                    sidePagination: 'server',
                    responseHandler: function (res) {
                        logoutIfSessionExpired(res);
                        return res;
                    },
                    search: true,
                    showColumns: true,
                    pagination: true,
                    clickToSelect: true,
                    singleSelect: true,
                    maintainSelected: false,
                    showFooter: false,
                    sortName: 'number',
                    sortOrder: 'asc',
                    height: 500,
                    locale: (QCD.currentLang + '-' + QCD.currentLang
                        .toUpperCase())
                });
        }

    }

    function otSelectWorkstation() {
        var selectedWorkstations = $("#workstationItems").bootstrapTable('getSelections');
        $("#otWorkstationsLookup").modal('hide');
        $('#workstation-' + QCD.technologyConfiguratorContext.technology.currentTOCIndex).val(selectedWorkstations[0].number);
        $('#workstation-' + QCD.technologyConfiguratorContext.technology.currentTOCIndex).removeClass('is-invalid');

        $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, e) {
            if (e.index == QCD.technologyConfiguratorContext.technology.currentTOCIndex) {
                e.workstationId = selectedWorkstations[0].id;
                e.workstation = selectedWorkstations[0].number;
            }
        });
    }

    function fillWorkstationTypes() {
        $("#otWorkstationType").empty();

        $("#otWorkstationType").append('<option value=""></option>');
        for (const [key, value] of Object.entries(QCD.technologyConfiguratorContext.workstationTypes)) {


            $("#otWorkstationType").append(
                '<option value="' + value.id + '">' + value.number +
                '</option>');

        }
    }


    $('#otWorkstationDefinitionModal').on('hidden.bs.modal', function () {
        $("#technologyConfiguratorWizard").removeClass('disableModal');
    });

    function otSaveWorkstation() {
        var invalid = false;
        $("#otWorkstationNumber").removeClass('is-invalid');
        $("#otWorkstationName").removeClass('is-invalid');
        $("#otWorkstationType").removeClass('is-invalid');

        if ($("#otWorkstationNumber").val() == null || $("#otWorkstationNumber").val() === '') {
            $("#otWorkstationNumber").addClass('is-invalid');
            invalid = true;

        }

        if ($("#otWorkstationName").val() == null || $("#otWorkstationName").val() === '') {
            $("#otWorkstationName").addClass('is-invalid');
            invalid = true;

        }

        if ($("#otWorkstationType").val() == null || $("#otWorkstationType").val() === '') {
            $("#otWorkstationType").addClass('is-invalid');
            invalid = true;

        }
        if (invalid) {
            showMessage(
                'failure',
                QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.emptyField"),
                false);
            return;
        }

        var wt = {};
        wt.number = $("#otWorkstationNumber").val();
        wt.name = $("#otWorkstationName").val();
        wt.type = $("#otWorkstationType").val();

        $.ajax({
            url: "rest/workstation",
            type: "POST",
            data: JSON.stringify(wt),
            contentType: "application/json",
            beforeSend: function () {
                $("#loader").appendTo("body").modal('show');
            },
            success: function (data) {
                logoutIfSessionExpired(data);
                if (data.code === 'OK') {
                    $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, e) {
                        if (e.index == QCD.technologyConfiguratorContext.technology.currentTOCIndex) {
                            e.workstationId = data.id;
                            e.workstation = data.number;
                            $("#otWorkstationDefinitionModal").modal('hide');
                            $('#workstation-' + QCD.technologyConfiguratorContext.technology.currentTOCIndex).val(data.number);
                        }
                    });
                } else {
                    showMessage(
                        'failure',
                        QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                        data.message,
                        false);
                }
                $("#loader").modal('hide');

            },
            error: function (data) {
                logoutIfSessionExpired(data);
                if (data.status == 401) {
                    window.location = "/login.html?timeout=true";
                }
                showMessage('failure',
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error"),
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.internalError"),
                    false);

            },
            complete: function () {
                $("#loader").modal('hide');
            }
        });

    }

    function gotToStep(index) {
        $("#technologyConfiguratorWizardForm-t-" + index).click();
    }

    function disableStep(index) {
        $("#technologyConfiguratorWizardForm-t-" + index).parent().addClass("disabled");
        $("#technologyConfiguratorWizardForm-t-" + index).parent().removeClass("done")._enableAria(false);
    }

    function quantityOnBlur(tocIndex, materialIndex) {
        $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, op) {
            var data = op.materials;
            $.each(data, function (i, e) {
                if (e.index == materialIndex) {
                    e.quantity = evaluateExpression($('#quantityProductIn-' + materialIndex).val());
                    if (e.quantity) {
                        var calculatedQuantityPerUnit = e.quantity / $('#otQuantity').val();
                        e.quantityPerUnit = parseFloat(calculatedQuantityPerUnit.toFixed(5));
                        $('#quantityPerUnitProductIn-' + materialIndex).val(e.quantityPerUnit);
                    }
                }
            });
        });
    }

    function quantityPerUnitOnBlur(tocIndex, materialIndex) {
        $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, op) {
            var data = op.materials;
            $.each(data, function (i, e) {
                if (e.index == materialIndex) {
                    e.quantityPerUnit = evaluateExpression($('#quantityPerUnitProductIn-' + materialIndex).val());
                    if (e.quantityPerUnit) {
                        var calculatedQuantity = e.quantityPerUnit * $('#otQuantity').val();
                        e.quantity = parseFloat(calculatedQuantity.toFixed(5));
                        $('#quantityProductIn-' + materialIndex).val(e.quantity);
                    }
                }
            });
        });
    }


    function workstationTypes() {
        $.ajax({
            url: "rest/workstationTypes",
            type: "GET",
            async: false,
            beforeSend: function (data) {
                $("#loader").appendTo("body").modal('show');
            },
            success: function (data) {
                logoutIfSessionExpired(data);
                QCD.technologyConfiguratorContext.workstationTypes = data.workstationTypes;
            },
            error: function (data) {
                logoutIfSessionExpired(data);
                if (data.status == 401) {
                    window.location = "/login.html?timeout=true";
                }
                showMessage('failure',
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error"),
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.internalError"),
                    false);
            },
            complete: function () {
                $("#loader").modal('hide');
            }
        });
    }

    function preparePreview() {


        var tree =
                [],
            leaf, operation, map = {};
        var currentNode = {};
        var nodeExist = false;

        for (let i = 0; i < QCD.technologyConfiguratorContext.technologyOperations.length; i++) {

            if (i == 0) {
                var element = QCD.technologyConfiguratorContext.technologyOperations[i];
                var node = {};
                node.text = QCD.technologyConfiguratorContext.technology.product.number + ' - ' + $("#otQuantity").val()
                    + ' ' + $("#otUnit").val() + ' -  ' + element.operation;
                currentNode = node;
                tree.push(currentNode);
            } else {
                var element = QCD.technologyConfiguratorContext.technologyOperations[i];
                var node = {};
                if (element.operation.productNumber) {
                    node.text = element.operation.productNumber + ' - ' + $("#otQuantity").val() + element.operation.productUnit + ' -  ' + element.operation;
                } else {
                    node.text = element.operation + ' - ' + $("#otQuantity").val() + ' ' + $("#otUnit").val() + ' -  ' + element.operation;
                }

                currentNode.nodes = [];
                currentNode.nodes.push(node);


                currentNode = node;
            }

            console.log(currentNode);

        }
        var first = tree[0];

        for (let i = 0; i < QCD.technologyConfiguratorContext.technologyOperations.length; i++) {
            var element = QCD.technologyConfiguratorContext.technologyOperations[i];

            $.each(element.materials, function (i, m) {
                var mnode = {};
                mnode.text = '*  ' + m.productNumber + ' - ' + m.quantity + ' ' + m.unit;
                if (!node.nodes) {
                    node.nodes = [];
                }
                if (first.nodes) {
                    first.nodes.unshift(mnode);
                }

            });

            if (first.nodes) {
                first = first.nodes[first.nodes.length - 1];
            }
        }

        $("#operations").treeview({
            levels: QCD.technologyConfiguratorContext.technologyOperations.length + 1,
            expandIcon: "glyphicon glyphicon-chevron-right",
            collapseIcon: "glyphicon glyphicon-chevron-down",
            highlightSelected: false,
            data: tree
        });

    }

    function createTechnology() {
        var technology = {};
        technology.productId = QCD.technologyConfiguratorContext.technology.product.id;
        technology.quantity = $("#otQuantity").val();
        technology.description = $("#otDescription").val();
        technology.typeOfProductionRecording = '03forEach';
        technology.technologyOperations = [];
        $.each(QCD.technologyConfiguratorContext.technologyOperations, function (i, toc) {
            var technologyOperation = {};
            technologyOperation.id = toc.id;
            technologyOperation.operationId = toc.operationId;
            technologyOperation.workstationId = toc.workstationId;
            technologyOperation.node = toc.node;
            technologyOperation.materials = [];
            $.each(toc.materials, function (i, material) {
                var operationMaterial = {};
                operationMaterial.productInId = material.productInId;
                operationMaterial.productId = material.productId;
                operationMaterial.quantity = material.quantity;
                operationMaterial.quantityPerUnit = material.quantityPerUnit;
                technologyOperation.materials.push(operationMaterial);
            });
            technology.technologyOperations.push(technologyOperation);

        });
        $.ajax({
            url: "rest/createTechnology",
            type: "POST",
            data: JSON.stringify(technology),
            contentType: "application/json",
            beforeSend: function () {
                $("#loader").appendTo("body").modal('show');
            },
            success: function (data) {
                logoutIfSessionExpired(data);
                if (data.code === 'OK') {
                    showMessage('success',
                        QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.success"),
                        data.message,
                        false);
                    setTimeout(function () {
                        window.parent.goToPage('/page/technologies/technologiesList.html', false, false);
                    }, 1000);
                } else {
                    showMessage(
                        'failure',
                        QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                        data.message,
                        false);
                }

            },
            error: function (data) {
                logoutIfSessionExpired(data);
                if (data.status == 401) {
                    window.location = "/login.html?timeout=true";
                }
                showMessage('failure',
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error"),
                    QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.internalError"),
                    false);

            },
            complete: function () {
                $("#loader").modal('hide');
            }
        });
    }

    return {
        init: init,
        onOperationSelect: onOperationSelect,
        openOperationDefinition: openOperationDefinition,
        addMaterialToOperation: addMaterialToOperation,
        removeMaterialFromOperation: removeMaterialFromOperation,
        openOtMaterialsLookup: openOtMaterialsLookup,
        selectOtMaterialsItem: selectOtMaterialsItem,
        addProductTypeahead: addProductTypeahead,
        openMaterialDefinition: openMaterialDefinition,
        openOtWorkstationsLookup: openOtWorkstationsLookup,
        otSelectWorkstation: otSelectWorkstation,
        addWorkstationTypeahead: addWorkstationTypeahead,
        openWorkstationDefinition: openWorkstationDefinition,
        quantityOnBlur: quantityOnBlur,
        quantityPerUnitOnBlur: quantityPerUnitOnBlur
    }

})();

QCD.technologyConfigurator.init();

function logoutIfSessionExpired(data) {
    if ($.trim(data) == "sessionExpired" || $.trim(data).substring(0, 20) == "<![CDATA[ERROR PAGE:") {
        window.location = "/login.html?timeout=true";
    }
}


function operationFormatter(value, row) {

    var select = '<div class="input-group">' +
        '<select type="text" class="form-control custom-select" onchange="QCD.technologyConfigurator.onOperationSelect(' + new String(row.index) + ')" id="operation-' + row.index + '"  autocomplete="off">' +
        '<option value=""></option>';
    for (const [key, val] of Object.entries(QCD.technologyConfiguratorContext.operations)) {
        if (row.operationId && val.id == row.operationId) {
            select = select + '<option selected value="' + row.operationId + '">' + value + '</option>'
        } else {
            select = select + '<option value="' + val.id + '">' + val.number + '</option>'
        }
    }
    select = select + '</select>';

    select = select + '<div class="input-group-append">' +
        '<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.operations.operation.tip") + '" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.technologyConfigurator.openOperationDefinition(' + new String(row.index) + ')">' +
        '<span class="glyphicon glyphicon-plus"></span>' +
        '</button>' +
        '</div></div>';
    return select;

}

function actionFormatter(value, row) {
    return '<div class="input-group"><div class="input-group-append">' +
        '<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.removeMaterial") + '" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.technologyConfigurator.removeMaterialFromOperation(' + new String(row.operationIndex) + ',' + new String(row.index) + ')">' +
        '<span class="glyphicon glyphicon-minus"></span>' +
        '</button>' +
        '</div></div>';
}

function nodeFormatter(value, row) {
    return '<div class="input-group">' +
        '<input type="text" class="form-control" disabled value="' + nullToEmptyValue(value) + '"/>' +
        '</div>';
}

function productInFormatter(value, row) {
    return '<div class="input-group">' +
        '<div class="input-group-prepend">' +
        ' <label class="form-label grid-label">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.product") + '</label>' +
        '</div>' +
        '<input type="text" class="form-control q-auto-complete" onkeypress="QCD.technologyConfigurator.addProductTypeahead(' + new String(row.operationIndex) + ',' + new String(row.index) + ')" id="inProduct-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
        '<div class="input-group-append">' +
        '<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.selectProduct") + '"  class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.technologyConfigurator.openOtMaterialsLookup(' + new String(row.operationIndex) + ',' + new String(row.index) + ')">' +
        '<span class="glyphicon glyphicon-search"></span>' +
        '</button>' +
        '</div>' +
        '<div class="input-group-append">' +
        '<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.addProduct") + '" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.technologyConfigurator.openMaterialDefinition(' + new String(row.operationIndex) + ',' + new String(row.index) + ')">' +
        '<span class="glyphicon glyphicon-plus"></span>' +
        '</button>' +
        '</div>' +
        '</div>';
}

function workstationFormatter(value, row) {

    return '<div class="input-group">' +
        '<input type="text" class="form-control q-auto-complete" onkeypress="QCD.technologyConfigurator.addWorkstationTypeahead(' + new String(row.index) + ')" id="workstation-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
        '<div class="input-group-append">' +
        '<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.workstations.select.tip") + '"  class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.technologyConfigurator.openOtWorkstationsLookup(' + new String(row.index) + ')">' +
        '<span class="glyphicon glyphicon-search"></span>' +
        '</button>' +
        '</div>' +
        '<div class="input-group-append">' +
        '<button type="button"  data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.workstations.new.tip") + '" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.technologyConfigurator.openWorkstationDefinition(' + new String(row.index) + ')">' +
        '<span class="glyphicon glyphicon-plus"></span>' +
        '</button>' +
        '</div>' +
        '</div>';

}

function quantityPerUnitProductInFormatter(value, row) {
    return '<div class="input-group">' +
        '<div class="input-group-prepend" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.quantityPerUnit.tip") + '" >' +
        ' <label class="form-label grid-label30" >' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.quantityPerUnit") + '</label>' +
        '</div>' +
        '<input type="text" class="form-control right decimal"  onblur="QCD.technologyConfigurator.quantityPerUnitOnBlur(' + new String(row.operationIndex) + ',' + new String(row.index) + ')" id="quantityPerUnitProductIn-' + row.index + '" value="' + nullToEmptyValue(value) + '"></input>' +
        '</div>';

}

function quantityProductInFormatter(value, row) {

    return '<div class="input-group">' +
        '<div class="input-group-prepend">' +
        ' <label class="form-label grid-label30">' + QCD.translate("technologies.technologyConfigurator.technologyConfiguratorWizard.materials.quantity") + '</label>' +
        '</div>' +
        '<input type="text" class="form-control right decimal" onblur="QCD.technologyConfigurator.quantityOnBlur(' + new String(row.operationIndex) + ',' + new String(row.index) + ')"  id="quantityProductIn-' + row.index + '" value="' + nullToEmptyValue(value) + '"></input>' +
        '</div>';

}

function showMessage(type, title, content, autoClose) {
    messagesController.addMessage({
        type: type,
        title: title,
        content: content,
        autoClose: autoClose,
        extraLarge: false
    });
}

function nullToEmptyValue(value) {
    if (value) {
        return value;
    } else {
        return "";
    }
}


function validQuantity(field) {
    var valid = true;

    var value = $("#" + field).val();

    if (value.includes(',') && value.includes('.')) {
        valid = false;
        $("#" + field).addClass('is-invalid');
        return;
    }


    value = evaluateExpression(value);


    $("#" + field).val(value).change();

    if ((value != null) && (value != '') &&
        isNaN(value)) {
        valid = false;
    }

    if (valid && (value <= 0)) {
        valid = false;
    }

    var validationResult;

    if ((value != null) && (value != '') && valid) {
        validationResult = validateDecimal(value);

        if (!validationResult.validPrecision ||
            !validationResult.validScale) {
            valid = false;
        }
    }

    if (!valid) {
        isValid = false;

        if ((typeof validationResult !== "undefined") &&
            !validationResult.validPrecision) {
            showMessage(
                'failure',
                QCD
                    .translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                QCD
                    .translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.wrongDecimalPrecision"),
                false);
        } else if ((typeof validationResult !== "undefined") &&
            !validationResult.validScale) {
            showMessage(
                'failure',
                QCD
                    .translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError"),
                QCD
                    .translate("technologies.technologyConfigurator.technologyConfiguratorWizard.error.validationError.wrongDecimalScale"),
                false);
        }

        $("#" + field).addClass('is-invalid');
    } else {
        $("#" + field).removeClass('is-invalid');
    }
    return valid;
}


function getDate(element) {
    var date = $("#" + element).val();

    if (date == null ||
        date == '') {
        return null;
    } else {
        return moment(date, 'YYYY-MM-DD HH:mm:ss')
            .toDate();
    }
}


function evaluateExpression(s) {
    if (s == '') {
        return s;
    }
    s = s.replace(/,/g, '.');
    return s;
}

var defaultPrecision = 14;
var defaultScale = 5;

function validateDecimal(value) {
    return validateDecimalWithPrecisionAndScale(value, defaultPrecision, defaultScale);
}

function validateDecimalWithPrecisionAndScale(value, precision, scale) {

    var validPrecision = true;
    var validScale = true;
    var isScale = false;
    var parts;

    if (value.toString().indexOf('.') > 0) {
        parts = value.toString().split('.');
        isScale = true;
    } else if (value.toString().indexOf(',') > 0) {
        parts = value.toString().split(',');
        isScale = true;
    }


    if (isScale && parts[1].length > scale) {
        validScale = false;
    }

    var cleanValueLength = value.toString().length;
    if (isScale) {
        cleanValueLength = cleanValueLength - 1 - parts[1].length;
    }

    if (cleanValueLength > (precision - scale)) {
        validPrecision = false;
    }

    return {
        validPrecision: validPrecision,
        validScale: validScale
    };

}


function quantityPerUnitFormatter(value, row) {
    if (row.productInId || QCD.orderDefinitionWizardContext.order.technology) {
        return '<div class="input-group">' +
            '<input type="text" disabled class="form-control right decimal" tabindex="1" onblur="QCD.orderDefinitionWizard.quantityPerUnitOnBlur(' + new String(row.index) + ')" id="quantityPerUnit-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
            '</div>';
    } else {
        return '<div class="input-group">' +
            '<input type="text" class="form-control right decimal" tabindex="1" onchange="QCD.orderDefinitionWizard.quantityPerUnitOnBlur(' + new String(row.index) + ')" onblur="QCD.orderDefinitionWizard.quantityPerUnitOnBlur(' + new String(row.index) + ')" id="quantityPerUnit-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
            '</div>';
    }

}

function unitFormatter(value, row) {
    if (row.productInId) {
        return '<div class="input-group">' +
            '<input type="text" disabled class="form-control" disabled tabindex="1" id="unit-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
            '</div>';
    } else {
        return '<div class="input-group">' +
            '<input type="text" class="form-control" disabled tabindex="1" id="unit-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
            '</div>';
    }

}