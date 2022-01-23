var QCD = QCD || {};

QCD.operationalTasksDefinitionWizardContext = {};
QCD.operationalTasksDefinitionWizardContext.operationalTasksDefinitionWizardBody;
QCD.operationalTasksDefinitionWizardContext.order = {};
QCD.operationalTasksDefinitionWizardContext.order.product = null;
QCD.operationalTasksDefinitionWizardContext.order.technology = null;
QCD.operationalTasksDefinitionWizardContext.order.description = null;
QCD.operationalTasksDefinitionWizardContext.order.productionLine = null;
QCD.operationalTasksDefinitionWizardContext.operations = [];
QCD.operationalTasksDefinitionWizardContext.technologyOperations = [];
QCD.operationalTasksDefinitionWizardContext.node = 0;
QCD.operationalTasksDefinitionWizardContext.order.lastOperationIndex = null;
QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex = null;
QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex = null;
QCD.operationalTasksDefinitionWizardContext.order.productEvents = [];
QCD.operationalTasksDefinitionWizardContext.order.workstationEvents = [];
QCD.operationalTasksDefinitionWizardContext.workstationTypes = [];

QCD.operationalTasksDefinitionWizard = (function () {


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
		cleanContext();

		$("#otSelectProduct").prop('disabled', true);
		$("#otSelectTechnology").prop('disabled', true);

		$("#otTechnology").prop('disabled', true);
		$("#otGetTechnology").prop('disabled', true);

		$('#otProducts').on('check.bs.table', function (row, $element) {
			$("#otSelectProduct").prop('disabled', false);
		});
		$('#otProducts').on('uncheck.bs.table', function (row, $element) {
			$("#otSelectProduct").prop('disabled', true);
		});

		$('#otTechnologies').on('check.bs.table', function (row, $element) {
			$("#otSelectTechnology").prop('disabled', false);
		});
		$('#otTechnologies').on('uncheck.bs.table', function (row, $element) {
			$("#otSelectTechnology").prop('disabled', true);
		});


		QCD.operationalTasksDefinitionWizardContext.operationalTasksDefinitionWizardBody = $('#operationalTasksDefinitionWizard').clone();

		$("#operationalTasksDefinitionWizard").on('hidden.bs.modal', function () {
			$('#operationalTasksDefinitionWizard').remove();
			var myClone = QCD.operationalTasksDefinitionWizardContext.operationalTasksDefinitionWizardBody.clone();
			$('body').append(myClone);
		});

		$("#operationalTasksDefinitionWizard").modal();
		var form = $("#operationalTasksDefinitionForm");

		form.steps({
			headerTag: "h3",
			bodyTag: "fieldset",
			transitionEffect: "fade",
			labels: {
				previous: QCD.translate('basic.dashboard.orderDefinitionWizard.previous'),
				next: QCD.translate('basic.dashboard.orderDefinitionWizard.next'),
				finish: QCD.translate('basic.dashboard.orderDefinitionWizard.finish'),
				current: ''
			},
			titleTemplate: '<div class="title"><span class="number">#index#</span>#title#</div>',
			onStepChanging: function (event, currentIndex, newIndex) {
				if (currentIndex == 0) {
					var invalid = false;
					if ($("#otProduct").val() == null || $("#otProduct").val() === '' || QCD.operationalTasksDefinitionWizardContext.order.product == null) {
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
					var _startDate = getDate("otStartDate");
					if (_startDate == null) {
						$("#otStartDate").val(moment(new Date()).format('YYYY-MM-DD HH:mm:ss'));
						fillFinishDate();
					}
					if (invalid) {
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError.emptyField"),
							false);
					}
					if (!invalid) {
						var data = QCD.operationalTasksDefinitionWizardContext.technologyOperations;
						$.each(data, function (i, oper) {
							$.each(oper.materials, function (i, material) {
								var calculatedQuantity = material.quantityPerUnit * $('#otQuantity').val();
								material.quantity = parseFloat(calculatedQuantity.toFixed(5));
								$('#quantityProductIn-' + material.index).val(material.quantity);
							});
						});
					}
					return !invalid;
				} else if (currentIndex == 1) {
					var invalid = false;

					if ($("#otStartDate").val() == null || $("#otStartDate").val() === '') {
						$("#otStartDate").addClass('is-invalid');
						invalid = true;
					} else {
						$("#otStartDate").removeClass('is-invalid');

					}
					if ($("#otFinishDate").val() == null || $("#otFinishDate").val() === '') {
						$("#otFinishDate").addClass('is-invalid');
						invalid = true;
					} else {
						$("#otFinishDate").removeClass('is-invalid');

					}

					if (!invalid) {
						if (getDate("otStartDate") >= getDate("otFinishDate")) {
							showMessage(
								'failure',
								QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
								QCD.translate("basic.dashboard.orderDefinitionWizard.error.finishDate"),
								false);
							$("#otFinishDate").addClass('is-invalid');
							invalid = true;
						} else {
							$("#otFinishDate").removeClass('is-invalid');
						}
					}
					if (invalid) {
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError.emptyField"),
							false);
					} else {
						$('#technologyOperations').bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);
						if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
							$("#removeTechnologyOperation").prop('disabled', true);
							$("#newTechnologyOperation").prop('disabled', true);
						} else {
							$("#newTechnologyOperation").prop('disabled', false);
						}
					}
					return !invalid;

				} else if (currentIndex == 2) {
					var invalid = false;
					if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
						$.each(techOperations, function (i, op) {

							$('#operation-' + op.index).removeClass('is-invalid');

						});
					} else {
						var techOperations = QCD.operationalTasksDefinitionWizardContext.technologyOperations;
						$.each(techOperations, function (i, op) {
							if (op.operationId == null || $('#operation-' + op.index).find('option:selected').text() == '') {
								$('#operation-' + op.index).addClass('is-invalid');
								invalid = true;

							} else {
								$('#operation-' + op.index).removeClass('is-invalid');
							}


						});
					}
					if (invalid) {
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError.emptyField"),
							false);
					}
					if (QCD.operationalTasksDefinitionWizardContext.technologyOperations.length < 1) {
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError.technologyOperationsNotDefined"),
							false);
						invalid = true;

					}
					if (!invalid) {
						prepareOperationMaterials();

					}
					return !invalid;
				} else if (currentIndex == 3) {
					var invalid = false;
					var exist = false;

					var data = QCD.operationalTasksDefinitionWizardContext.technologyOperations;
					var last_element = data[data.length - 1];
					if (last_element.materials.length == 0) {
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
							QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.error.materialCannotBeEmpty"),
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
												QCD.operationalTasksDefinitionWizardContext.order.product.id == reMaterial.productId)) {

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
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError.emptyField"),
							false);
					}
					if (exist) {
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.productAlreadySelected"),
							false);
						invalid = true;
					}

					if (!invalid) {
						$("#workstations").bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);

					}
					return !invalid;
				} else if (currentIndex == 4) {
					var techOperations = QCD.operationalTasksDefinitionWizardContext.technologyOperations;
					$.each(techOperations, function (i, op) {
						if ($('#workstation-' + op.index).val() == '') {
							op.workstationId = null;
							op.workstation = null;
						}


					});
					preparePreview();
					return true;
				} else if (currentIndex == 5) {

					return true;
				}
			},
			onFinishing: function (event, currentIndex) {
				return true;
			},
			onFinished: function (event, currentIndex) {
				createOperationalTasks();
			},
			onInit: function (event, currentIndex) {
				$(".actions").find(".otCancelBtn").remove();

				var saveA = $("<a>").attr("href", "#").addClass("otCancelBtn").text(QCD.translate('basic.dashboard.orderDefinitionWizard.cancel'));
				var saveBtn = $("<li>").attr("aria-disabled", false).append(saveA);

				$(document).find(".actions ul").prepend(saveBtn)

				operations();
			},
		});
		$(".otCancelBtn").click(function () {
			$('#operationalTasksDefinitionWizard').modal('hide');
		});


		//step 1
		$("#otSaveProduct").unbind();
		$("#saveOperation").unbind();
		$("#otSaveWorkstation").unbind();

		$("#otGetProduct").click(function () {

			$("#otProductsLookup").appendTo("body").one('shown.bs.modal', function () {
				$("#otProducts").bootstrapTable('destroy');
				$("#operationalTasksDefinitionWizard").addClass('disableModal');
				fillProducts();
			}).modal('show');


		});

		$('#otProductsLookup').on('hidden.bs.modal', function () {
			$("#operationalTasksDefinitionWizard").removeClass('disableModal');
		});

		$("#otSelectProduct").click(function () {
			var selectedProducts = $("#otProducts").bootstrapTable('getSelections');
			QCD.operationalTasksDefinitionWizardContext.order.product = selectedProducts[0];
			$("#otProductsLookup").modal('hide');
			$("#otProduct").val(QCD.operationalTasksDefinitionWizardContext.order.product.number);
			$("#otUnit").val(QCD.operationalTasksDefinitionWizardContext.order.product.unit);
			$("#otTechnology").prop('disabled', false);
			$("#otGetTechnology").prop('disabled', false);
			QCD.operationalTasksDefinitionWizardContext.order.technology = null;
			$("#otTechnology").val("");
			$.getJSON(
				'rest/technologies', {
					query: "",
					master: true,
					forEach: true,
					productId: QCD.operationalTasksDefinitionWizardContext.order.product.id
				},
				function (data) {
					$.each(data.technologies, function (i, tech) {
						if (tech.master) {
							QCD.operationalTasksDefinitionWizardContext.node = 0;
							QCD.operationalTasksDefinitionWizardContext.order.technology = tech;
							$("#otTechnology").val(QCD.operationalTasksDefinitionWizardContext.order.technology.number);
							fillOperationMaterialsForTechnology();
						}
					});
				});

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
				QCD.operationalTasksDefinitionWizardContext.order.product = current;
				$("#otUnit").val(QCD.operationalTasksDefinitionWizardContext.order.product.unit);
				$("#otTechnology").prop('disabled', false);
				$("#otGetTechnology").prop('disabled', false);
				QCD.operationalTasksDefinitionWizardContext.order.technology = null;
				$("#otTechnology").val("");
				$.getJSON(
					'rest/technologies', {
						query: "",
						master: true,
						forEach: true,
						productId: QCD.operationalTasksDefinitionWizardContext.order.product.id
					},
					function (data) {
						logoutIfSessionExpired(data);
						$.each(data.technologies, function (i, tech) {
							if (tech.master) {
								QCD.operationalTasksDefinitionWizardContext.order.technology = tech;
								$("#otTechnology").val(QCD.operationalTasksDefinitionWizardContext.order.technology.number);
								fillOperationMaterialsForTechnology();
							}
						});
					});
			}

		});
		$('#otProduct').keyup(function (e) {
			if (e.keyCode != 13) {
				$('#otProduct').removeData("active");
				QCD.operationalTasksDefinitionWizardContext.order.product = null;
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
			$("#operationalTasksDefinitionWizard").addClass('disableModal');
		});

		$('#otProductDefinitionModal').on('hidden.bs.modal', function () {
			$("#operationalTasksDefinitionWizard").removeClass('disableModal');
		});
		$('#otWorkstationDefinitionModal').on('hidden.bs.modal', function () {
			$("#operationalTasksDefinitionWizard").removeClass('disableModal');
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
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError.emptyField"),
					false);
				return;
			}

			var product = {};
			product.number = $("#otProductNumber").val();
			product.name = $("#otProductName").val();
			product.unit = $("#otProductUnit").val();
			if (QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex) {
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
						if (QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex) {
							$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, e) {
								if (e.index == QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex) {
									$.each(e.materials, function (i, e) {
										if (e.index == QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex) {
											e.productId = data.id;;
											e.productNumber = data.number;
											e.product = data.number;
											e.unit = data.unit;
											$("#otProductDefinitionModal").modal('hide');
											$('#inProduct-' + QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex).val(data.number);
											$('#inProduct-' + QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex).removeClass('is-invalid');
											$('#unit-' + QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex).val(data.unit);

										}
									});
								}
							});
						} else {

							QCD.operationalTasksDefinitionWizardContext.order.product = {};
							QCD.operationalTasksDefinitionWizardContext.order.product.id = data.id;
							QCD.operationalTasksDefinitionWizardContext.order.product.number = data.number;
							QCD.operationalTasksDefinitionWizardContext.order.product.name = data.name;
							QCD.operationalTasksDefinitionWizardContext.order.product.unit = data.unit;
							$("#otProductDefinitionModal").modal('hide');
							$("#otProduct").val(QCD.operationalTasksDefinitionWizardContext.order.product.number);
							$("#otUnit").val(QCD.operationalTasksDefinitionWizardContext.order.product.unit);
							QCD.operationalTasksDefinitionWizardContext.order.technology = null;
							$("#otTechnology").val("");

						}
					} else {
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
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
						QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
						QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
						false);

				},
				complete: function () {
					$("#loader").modal('hide');
				}
			});


		});

		$('#otTechnology').typeahead({
			minLength: 3,
			source: function (query, process) {
				return $.getJSON(
					'rest/technologies', {
						query: $("#otTechnology").val(),
						forEach: true,
						productId: QCD.operationalTasksDefinitionWizardContext.order.product.id
					},
					function (data) {
						logoutIfSessionExpired(data);
						var resultList = data.technologies.map(function (item) {
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
				})

			}

		});
		$('#otTechnology').change(function () {
			var current = $('#otTechnology').typeahead("getActive");
			if (current) {
				QCD.operationalTasksDefinitionWizardContext.node = 0;
				QCD.operationalTasksDefinitionWizardContext.order.technology = current;
				fillOperationMaterialsForTechnology();
			}

		});

		$('#otTechnology').keyup(function (e) {
			if (e.keyCode != 13) {
				$('#otTechnology').removeData("active");
				QCD.operationalTasksDefinitionWizardContext.order.technology = null;
				QCD.operationalTasksDefinitionWizardContext.technologyOperations = [];
				$('#technologyOperations').bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);
				if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
					$("#removeTechnologyOperation").prop('disabled', true);
					$("#newTechnologyOperation").prop('disabled', true);
				} else {
					$("#newTechnologyOperation").prop('disabled', false);
				}
				prepareOperationMaterials();
				disableStepsAfterClearTechnology();
			}
		});

		$("#otGetTechnology").click(function () {

			$("#otTechnologiesLookup").appendTo("body").one('shown.bs.modal', function () {
				$("#otTechnologies").bootstrapTable('destroy');
				$("#operationalTasksDefinitionWizard").addClass('disableModal');

				fillTechnologies();
			}).modal('show');


		});
		$('#otTechnologiesLookup').on('hidden.bs.modal', function () {
			$("#operationalTasksDefinitionWizard").removeClass('disableModal');
		});

		$("#otSelectTechnology").click(function () {
			QCD.operationalTasksDefinitionWizardContext.node = 0;
			var selectedTechnology = $("#otTechnologies").bootstrapTable('getSelections');
			QCD.operationalTasksDefinitionWizardContext.order.technology = selectedTechnology[0];
			$("#otTechnologiesLookup").modal('hide');
			$("#otTechnology").val(QCD.operationalTasksDefinitionWizardContext.order.technology.number);
			fillOperationMaterialsForTechnology();
		});


		$('#otTechnologiesLookup').on('hidden.bs.modal', function (e) {
			$("#otSelectTechnology").prop('disabled', true);
		});

		$("#otTechnology").blur(function () {
			var technologyVal = $("#otTechnology").val();
			if (technologyVal == null || technologyVal === '') {
				QCD.operationalTasksDefinitionWizardContext.order.technology = null;
				QCD.operationalTasksDefinitionWizardContext.technologyOperations = [];
			}
		});

		//step 3
		$("#removeTechnologyOperation").prop('disabled', true);

		$('#technologyOperations').on('check.bs.table', function (row, $element) {
			if (!QCD.operationalTasksDefinitionWizardContext.order.technology) {
				$("#removeTechnologyOperation").prop('disabled', false);
			}
		});

		$('#technologyOperations').on('uncheck.bs.table', function (row, $element) {
			if (!QCD.operationalTasksDefinitionWizardContext.order.technology) {

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
				height: 320,
				locale: (QCD.currentLang + '-' + QCD.currentLang
					.toUpperCase())
			});
		$("#newTechnologyOperation").click(function () {
			var technologyOperation = {};
			technologyOperation.id = null;
			technologyOperation.operationId = null;
			technologyOperation.operation = null;
			QCD.operationalTasksDefinitionWizardContext.node = QCD.operationalTasksDefinitionWizardContext.node + 1;
			technologyOperation.node = QCD.operationalTasksDefinitionWizardContext.node + ".";
			technologyOperation.index = new Date().getTime();
			QCD.operationalTasksDefinitionWizardContext.technologyOperations.push(technologyOperation);
			$technologyOperations.bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);
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
			QCD.operationalTasksDefinitionWizardContext.node = 0;
			var reIndex = 1;
			$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, toc) {
				toc.node = reIndex + ".";
				QCD.operationalTasksDefinitionWizardContext.node = reIndex;
				reIndex = reIndex + 1;
			});
			$('#technologyOperations').bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);

		});

		$("#otStartDate").datetimepicker({
			format: 'YYYY-MM-DD HH:mm:ss',
			useStrict: true,
			useCurrent: false,
			locale: QCD.currentLang
		});

		$("#otFinishDate").datetimepicker({
			format: 'YYYY-MM-DD HH:mm:ss',
			useStrict: true,
			useCurrent: false,
			locale: QCD.currentLang
		});

		$("#otStartDatePicker").click(function () {
			$("#otStartDate").data("DateTimePicker").toggle();


		});

		$("#otFinishDatePicker").click(function () {
			$("#otFinishDate").data("DateTimePicker").toggle();
		});


		$('#operationDefinitionModal').on('hidden.bs.modal', function () {
			$("#operationalTasksDefinitionWizard").removeClass('disableModal');
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
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError.emptyField"),
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
						var techOperations = QCD.operationalTasksDefinitionWizardContext.technologyOperations;
						$.each(techOperations, function (i, e) {
							if (e.index == QCD.operationalTasksDefinitionWizardContext.order.lastOperationIndex) {
								e.operationId = data.id;
								e.operation = data.number;
								operations();
								$('#technologyOperations').bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);
							}
						});
					} else {
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
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
						QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
						QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
						false);

				},
				complete: function () {
					$("#loader").modal('hide');
				}
			});


		});

		$('#otMaterialsLookup').on('hidden.bs.modal', function () {
			$("#operationalTasksDefinitionWizard").removeClass('disableModal');
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
		$workstations.bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);

		$("#otSelectWorkstation").prop('disabled', true);


		$('#otWorkstationsLookup').on('hidden.bs.modal', function () {
			$("#operationalTasksDefinitionWizard").removeClass('disableModal');
		});


		$("#otSelectWorkstation").click(function () {
			otSelectWorkstation();
		});

		$("#otSaveWorkstation").click(function () {
			otSaveWorkstation();
		});

		var $prev_operationsTable = $("#prev_ot_operations")
			.bootstrapTable({
				uniqueId: 'index',
				search: true,
				undefinedText: '',
				showColumns: false,
				pagination: false,
				clickToSelect: false,
				singleSelect: false,
				maintainSelected: false,
				showFooter: false,
				height: 350,
				locale: (QCD.currentLang + '-' + QCD.currentLang
					.toUpperCase())
			});

		var $prev_materialsTable = $("#prev_ot_materials")
			.bootstrapTable({
				uniqueId: 'index',
				search: true,
				undefinedText: '',
				showColumns: false,
				pagination: false,
				clickToSelect: false,
				singleSelect: false,
				maintainSelected: false,
				showFooter: false,
				height: 350,
				locale: (QCD.currentLang + '-' + QCD.currentLang
					.toUpperCase())
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

	function fillTechnologies() {
		var $technologiesEntries = $("#otTechnologies")
			.bootstrapTable({
				url: 'rest/technologiesByPage',
				queryParams: function (p) {
					return {
						limit: p.limit,
						offset: p.offset,
						sort: p.sort,
						order: p.order,
						search: p.search,
						productId: QCD.operationalTasksDefinitionWizardContext.order.product.id,
						forEach: true

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
					QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
					false);
			},
			complete: function () {
				$("#loader").modal('hide');
			}
		});
	}


	function fillFinishDate() {
		if (getDate("otFinishDate")) {
			return;
		}
		var _startDate = getDate("otStartDate");
		if (_startDate == null) {
			return;
		}
		$.ajax({
			url: "rest/productionLines/default",
			type: "GET",
			async: false,
			beforeSend: function (data) {
				$("#loader").appendTo("body").modal('show');
			},
			success: function (data) {
				logoutIfSessionExpired(data);

				if (data.id) {
					QCD.orderDefinitionWizardContext.order.productionLine = {};
					QCD.orderDefinitionWizardContext.order.productionLine.id = data.id;
					QCD.orderDefinitionWizardContext.order.productionLine.number = data.number;
					QCD.orderDefinitionWizardContext.order.productionLine.name = data.name;
					$.ajax({
						url: "rest/productionLine/" + QCD.orderDefinitionWizardContext.order.productionLine.id + "/shiftWorkingDate",
						type: "GET",
						data: {
							date: getDate("otStartDate")
						},
						async: false,
						beforeSend: function (data) {
							$("#loader").appendTo("body").modal('show');
						},
						success: function (data) {
							logoutIfSessionExpired(data);
							$("#otFinishDate").val(moment(new Date(data.finish)).format('YYYY-MM-DD HH:mm:ss'));

						},
						error: function (data) {
							logoutIfSessionExpired(data);
							if (data.status == 401) {
								window.location = "/login.html?timeout=true";
							}
							showMessage('failure',
								QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
								QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
								false);
						},
						complete: function () {
							$("#loader").modal('hide');
						}
					});
				}
			},
			error: function (data) {
				logoutIfSessionExpired(data);
				if (data.status == 401) {
					window.location = "/login.html?timeout=true";
				}
				showMessage('failure',
					QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
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
				QCD.operationalTasksDefinitionWizardContext.operations = data.operations;
			},
			error: function (data) {
				logoutIfSessionExpired(data);
				if (data.status == 401) {
					window.location = "/login.html?timeout=true";
				}
				showMessage('failure',
					QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
					false);
			},
			complete: function () {
				$("#loader").modal('hide');
			}
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
				QCD.operationalTasksDefinitionWizardContext.workstationTypes = data.workstationTypes;
			},
			error: function (data) {
				logoutIfSessionExpired(data);
				if (data.status == 401) {
					window.location = "/login.html?timeout=true";
				}
				showMessage('failure',
					QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
					false);
			},
			complete: function () {
				$("#loader").modal('hide');
			}
		});
	}


	function cleanContext() {
		QCD.operationalTasksDefinitionWizardContext.order = {};
		QCD.operationalTasksDefinitionWizardContext.order.product = null;
		QCD.operationalTasksDefinitionWizardContext.order.technology = null;
		QCD.operationalTasksDefinitionWizardContext.order.description = null;
		QCD.operationalTasksDefinitionWizardContext.operations = [];
		QCD.operationalTasksDefinitionWizardContext.technologyOperations = [];

		QCD.operationalTasksDefinitionWizardContext.order.lastOperationIndex = null
		QCD.operationalTasksDefinitionWizardContext.order.productEvents = [];
		QCD.operationalTasksDefinitionWizardContext.order.workstationEvents = [];
		QCD.operationalTasksDefinitionWizardContext.node = 0;

	}

	function openOperationDefinition(element) {

		$("#operationDefinitionModal").one('shown.bs.modal', function () {
			$("#operationNumber").val("");
			$("#operationName").val("");
			$("#operationalTasksDefinitionWizard").addClass('disableModal');
			QCD.operationalTasksDefinitionWizardContext.order.lastOperationIndex = element;
		}).appendTo("body").modal('show');
	}

	function onOperationSelect(element) {
		var data = QCD.operationalTasksDefinitionWizardContext.technologyOperations;
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
		var data = QCD.operationalTasksDefinitionWizardContext.technologyOperations;
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
		descriptionDiv = descriptionDiv + '<label id="description-label" class="form-label">' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.form.operationMaterials.description") + '</label>';
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
		operElement = operElement + '<label class="form-label required small-label">' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.operation") + '</label>';
		operElement = operElement + '</div>';
		operElement = operElement + ' <input disabled type="text" class="form-control" tabindex="1" autocomplete="off" value="' + oper.operation + '"></input> ';
		operElement = operElement + '</div>';
		operElement = operElement + '</div>';
		operElement = operElement + '<div class="col-sm-2">';
		operElement = operElement + '<div class="input-group"><div class="input-group-prepend">';
		operElement = operElement + '<label class="form-label required small-label">' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.node") + '</label>';
		operElement = operElement + '</div>';
		operElement = operElement + ' <input disabled type="text" class="form-control" tabindex="1" autocomplete="off" value="' + oper.node + '"></input> ';
		operElement = operElement + '</div>';
		operElement = operElement + '</div>';
		operElement = operElement + '<div class="col-sm-4">';
		operElement = operElement + '<div>';
		operElement = operElement + '<button type="button" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.operationalTasksDefinitionWizard.addMaterialToOperation(' + new String(oper.index) + ')">';
		operElement = operElement + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.technologyOperations.addMaterial");
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
		operElement = operElement + '<th data-field="product" data-formatter="productInFormatter" data-sortable="false" data-align="center">' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.product") + '</th>';
		operElement = operElement + '<th data-field="quantity" data-formatter="quantityProductInFormatter" data-sortable="false" data-align="center">' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.quantity") + '</th>';
		operElement = operElement + '<th data-field="quantityPerUnit" data-formatter="quantityPerUnitProductInFormatter" data-sortable="false" data-align="center">' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.quantityPerUnit") + '</th>';
		operElement = operElement + '<th data-field="unit" data-width="70" data-formatter="unitFormatter" data-sortable="false" data-align="center">' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.unit") + '</th>';
		operElement = operElement + '<th data-field="action"  data-formatter="actionFormatter" data-sortable="false" data-align="center">' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.unit") + '</th>';
		operElement = operElement + ' </tr>';
		operElement = operElement + '</thead>';
		operElement = operElement + '</table>';
		operElement = operElement + '</div>';
		operElement = operElement + '</div>';
		operElement = operElement + '</div><hr></hr>';

		return operElement;
	}

	function addMaterialToOperation(operationIndex) {
		var data = QCD.operationalTasksDefinitionWizardContext.technologyOperations;
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
			QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex = materialIndex;
			QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex = tocIndex;
			$("#otMaterialsItem").bootstrapTable('destroy');
			$("#operationalTasksDefinitionWizard").addClass('disableModal');

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
		$('#inProduct-' + QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex).val(selectedMaterials[0].number);
		$('#inProduct-' + QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex).removeClass('is-invalid');

		$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, e) {
			if (e.index == QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex) {

				var data = e.materials;
				var exist = false;
				$.each(data, function (i, e) {
					if ((e.productId && e.productId == selectedMaterials[0].id) || QCD.operationalTasksDefinitionWizardContext.order.product.id == selectedMaterials[0].id) {

						$('#inProduct-' + QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex).addClass('is-invalid');
						$('#inProduct-' + QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex).val("");
						showMessage(
							'failure',
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
							QCD.translate("basic.dashboard.orderDefinitionWizard.error.productAlreadySelected"),
							false);
						exist = true;
						return false;
					}
				});

				if (!exist) {
					$.each(data, function (i, e) {
						if (e.index == QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex) {
							e.productId = selectedMaterials[0].id;
							e.productNumber = selectedMaterials[0].number;
							e.product = selectedMaterials[0].number;
							e.unit = selectedMaterials[0].unit;
							$('#unit-' + QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex).val(selectedMaterials[0].unit);

						}
					});
				}
			}
		});
	}

	function addProductTypeahead(tocIndex, element) {
		if (!QCD.operationalTasksDefinitionWizardContext.order.productEvents.includes(element)) {
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
					$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, e) {
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
					var data = QCD.operationalTasksDefinitionWizardContext.order.materials;

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
			QCD.operationalTasksDefinitionWizardContext.order.productEvents.push(element);

		}
	}

	function addWorkstationTypeahead(tocIndex) {
		if (!QCD.operationalTasksDefinitionWizardContext.order.workstationEvents.includes(tocIndex)) {
			if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
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
					$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, e) {
						if (e.index == tocIndex) {
							if (e.index == QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex) {
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
					var data = QCD.operationalTasksDefinitionWizardContext.order.materials;

					$.each(data, function (i, e) {
						if (e.index == tocIndex) {
							e.workstationId = null
							e.workstation = "";


						}
					});
				}
			});
			QCD.operationalTasksDefinitionWizardContext.order.workstationEvents.push(tocIndex);

		}
	}

	function openMaterialDefinition(tocIndex, element) {
		QCD.operationalTasksDefinitionWizardContext.order.currentMaterialIndex = element;
		QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex = tocIndex;
		units();
		$("#otProductNumber").removeClass('is-invalid');
		$("#otProductName").removeClass('is-invalid');
		$("#otProductUnit").removeClass('is-invalid');
		$("#otProductNumber").val(null);
		$("#otProductName").val(null);
		$("#otProductUnit").val(null);

		$('#otProductDefinitionModal').appendTo("body").modal('show');
		$("#operationalTasksDefinitionWizard").addClass('disableModal');


	}

	function openWorkstationDefinition(tocIndex) {
		workstationTypes();
		QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex = tocIndex;
		units();
		$("#otWorkstationNumber").removeClass('is-invalid');
		$("#otWorkstationName").removeClass('is-invalid');
		$("#otWorkstationType").removeClass('is-invalid');
		$("#otWorkstationNumber").val(null);
		$("#otWorkstationName").val(null);
		$("#otWorkstationType").val(null);
		fillWorkstationTypes();
		$('#otWorkstationDefinitionModal').appendTo("body").modal('show');
		$("#operationalTasksDefinitionWizard").addClass('disableModal');


	}

	function openOtWorkstationsLookup(tocIndex) {
		$("#otWorkstationsLookup").appendTo("body").one('shown.bs.modal', function () {
			QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex = tocIndex;
			$("#workstationItems").bootstrapTable('destroy');
			$("#operationalTasksDefinitionWizard").addClass('disableModal');

			fillOtWorkstations(tocIndex);
		}).modal('show');
	}

	function fillOtWorkstations(tocIndex) {
		if (QCD.operationalTasksDefinitionWizardContext.order.technology) {

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

			var $workstationsByPage = $("#workstationItems")
				.bootstrapTable({
					url: 'rest/workstationsByPage',
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

	}

	function otSelectWorkstation() {
		var selectedWorkstations = $("#workstationItems").bootstrapTable('getSelections');
		$("#otWorkstationsLookup").modal('hide');
		$('#workstation-' + QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex).val(selectedWorkstations[0].number);
		$('#workstation-' + QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex).removeClass('is-invalid');

		$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, e) {
			if (e.index == QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex) {
				e.workstationId = selectedWorkstations[0].id;
				e.workstation = selectedWorkstations[0].number;
			}
		});
	}

	function fillWorkstationTypes() {
		$("#otWorkstationType").empty();

		$("#otWorkstationType").append('<option value=""></option>');
		for (const [key, value] of Object.entries(QCD.operationalTasksDefinitionWizardContext.workstationTypes)) {


			$("#otWorkstationType").append(
				'<option value="' + value.id + '">' + value.number +
				'</option>');

		}
	}

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
				QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
				QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError.emptyField"),
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
					$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, e) {
						if (e.index == QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex) {
							e.workstationId = data.id;
							e.workstation = data.number;
							$("#otWorkstationDefinitionModal").modal('hide');
							$('#workstation-' + QCD.operationalTasksDefinitionWizardContext.order.currentTOCIndex).val(data.number);
						}
					});
				} else {
					showMessage(
						'failure',
						QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
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
					QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
					false);

			},
			complete: function () {
				$("#loader").modal('hide');
			}
		});

	}

	function preparePreview() {
		$("#prev_ot_product").val($("#otProduct").val());
		$("#prev_ot_quantity").val($("#otQuantity").val());
		$("#prev_ot_unit").val($("#otUnit").val());
		$("#prev_ot_description").val($("#otDescription").val());
		$("#prev_ot_technology").val($("#otTechnology").val());
		$("#prev_ot_startDate").val($("#otStartDate").val());
		$("#prev_ot_finishDate").val($("#otFinishDate").val());
		$("#prev_ot_operations").bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);
		var prevMaterials = [];
		$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, e) {
			$.each(e.materials, function (i, m) {
				prevMaterials.push(m);
			});
		});

		$("#prev_ot_materials").bootstrapTable('load', prevMaterials);

	}

	function quantityOnBlur(tocIndex, materialIndex) {
		$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, op) {
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
		$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, op) {
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


	function fillOperationMaterialsForTechnology() {
		$.ajax({
			url: "rest/technology/" + QCD.operationalTasksDefinitionWizardContext.order.technology.id + "/operationMaterials",
			type: "GET",
			async: false,
			beforeSend: function (data) {
				$("#loader").appendTo("body").modal('show');
			},
			success: function (data) {
				logoutIfSessionExpired(data);
				var tocs = [];
				$.each(data, function (i, toc) {
					var add = true;
					$.each(tocs, function (i, existToc) {
						if (existToc.id == toc.tocId) {
							add = false;
						}
					});
					if (add) {
						var technologyOperation = {};
						technologyOperation.id = toc.tocId;
						technologyOperation.operationId = toc.operationId;
						technologyOperation.operation = toc.operationNumber;
						technologyOperation.node = toc.node;
						technologyOperation.index = toc.tocId;
						technologyOperation.index = toc.tocId;
						technologyOperation.workstationId = toc.workstationId;
                        technologyOperation.workstation = toc.workstationNumber;
						technologyOperation.materials = [];
						tocs.push(technologyOperation);
					}
				});
				QCD.operationalTasksDefinitionWizardContext.technologyOperations = tocs;
				$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, toc) {
					$.each(data, function (i, opMaterial) {
						if (toc.id == opMaterial.tocId) {
							var operationMaterial = {};
							operationMaterial.operationIndex = toc.id;
							operationMaterial.product = opMaterial.productNumber;
							operationMaterial.productId = opMaterial.productId;
							var calculatedQuantity = opMaterial.quantityPerUnit * $('#otQuantity').val();
							operationMaterial.quantity = parseFloat(calculatedQuantity.toFixed(5));
							operationMaterial.quantityPerUnit = opMaterial.quantityPerUnit;
							operationMaterial.unit = opMaterial.unit;
							operationMaterial.index = opMaterial.index;
							toc.materials.push(operationMaterial);
						}
					});
				});
				$('#technologyOperations').bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);
				if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
					$("#removeTechnologyOperation").prop('disabled', true);
					$("#newTechnologyOperation").prop('disabled', true);
				} else {
					$("#newTechnologyOperation").prop('disabled', false);
				}
				prepareOperationMaterials();
				$("#workstations").bootstrapTable('load', QCD.operationalTasksDefinitionWizardContext.technologyOperations);
				preparePreview();

			},
			error: function (data) {
				logoutIfSessionExpired(data);
				if (data.status == 401) {
					window.location = "/login.html?timeout=true";
				}
				showMessage('failure',
					QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
					false);
			},
			complete: function () {
				$("#loader").modal('hide');
			}
		});
	}

	function createOperationalTasks() {
		var order = {};
		order.productId = QCD.operationalTasksDefinitionWizardContext.order.product.id;
		order.quantity = $("#otQuantity").val();
		order.description = $("#otDescription").val();
		if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
			order.technologyId = QCD.operationalTasksDefinitionWizardContext.order.technology.id;
		}
		order.startDate = getDate("otStartDate");
		order.typeOfProductionRecording = '03forEach';
		order.finishDate = getDate("otFinishDate");
		order.technologyOperations = [];
		$.each(QCD.operationalTasksDefinitionWizardContext.technologyOperations, function (i, toc) {
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
			order.technologyOperations.push(technologyOperation);

		});
		$.ajax({
			url: "rest/operationalTasks",
			type: "POST",
			data: JSON.stringify(order),
			contentType: "application/json",
			beforeSend: function () {
				$("#loader").appendTo("body").modal('show');
			},
			success: function (data) {
				logoutIfSessionExpired(data);
				if (data.code === 'OK') {

					$("#operationalTasksDefinitionWizard").modal('hide');
					$.each(data.operationalTasks, function (i, operationalTask) {
						QCD.dashboard.prependOperationalTask('operationalTasksPending', operationalTask);
					    QCD.dashboard.filterKanbanReload();
					});
					showMessage('success',
						QCD.translate("basic.dashboard.orderDefinitionWizard.success"),
						data.message, false);

					if (data.additionalInformation) {
						showMessage('information',
							QCD.translate("basic.dashboard.orderDefinitionWizard.information"),
							data.additionalInformation, false);
					}

				} else {
					showMessage(
						'failure',
						QCD.translate("basic.dashboard.orderDefinitionWizard.error.validationError"),
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
					QCD.translate("basic.dashboard.orderDefinitionWizard.error"),
					QCD.translate("basic.dashboard.orderDefinitionWizard.error.internalError"),
					false);

			},
			complete: function () {
				$("#loader").modal('hide');
			}
		});
	}

	function disableStepsAfterClearTechnology() {
		disableStep(3);
		disableStep(4);
		disableStep(5);
	}

	function disableStep(index) {
		$("#operationalTasksDefinitionForm-t-" + index).parent().addClass("disabled");
		$("#operationalTasksDefinitionForm-t-" + index).parent().removeClass("done")._enableAria(false);
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

function operationFormatter(value, row) {
	if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
		return '<div class="input-group">' +
			'<input type="text" class="form-control" disabled id="operation-' + row.index + '"  autocomplete="off" value="' + nullToEmptyValue(value) + '"/>' +
			'</div>';
	} else {
		var select = '<div class="input-group">' +
			'<select type="text" class="form-control custom-select" onchange="QCD.operationalTasksDefinitionWizard.onOperationSelect(' + new String(row.index) + ')" id="operation-' + row.index + '"  autocomplete="off">' +
			'<option value=""></option>';
		for (const [key, val] of Object.entries(QCD.operationalTasksDefinitionWizardContext.operations)) {
			if (row.operationId && val.id == row.operationId) {
				select = select + '<option selected value="' + row.operationId + '">' + value + '</option>'
			} else {
				select = select + '<option value="' + val.id + '">' + val.number + '</option>'
			}
		}
		select = select + '</select>';

		select = select + '<div class="input-group-append">' +
			'<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.operations.operation.tip") + '" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.operationalTasksDefinitionWizard.openOperationDefinition(' + new String(row.index) + ')">' +
			'<span class="glyphicon glyphicon-plus"></span>' +
			'</button>' +
			'</div></div>';
		return select;
	}
}

function actionFormatter(value, row) {
	return '<div class="input-group"><div class="input-group-append">' +
		'<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.materials.removeMaterial") + '" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.operationalTasksDefinitionWizard.removeMaterialFromOperation(' + new String(row.operationIndex) + ',' + new String(row.index) + ')">' +
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
		' <label class="form-label grid-label">' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.product") + '</label>' +
		'</div>' +
		'<input type="text" class="form-control q-auto-complete" onkeypress="QCD.operationalTasksDefinitionWizard.addProductTypeahead(' + new String(row.operationIndex) + ',' + new String(row.index) + ')" id="inProduct-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
		'<div class="input-group-append">' +
		'<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.materials.selectProduct") + '"  class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.operationalTasksDefinitionWizard.openOtMaterialsLookup(' + new String(row.operationIndex) + ',' + new String(row.index) + ')">' +
		'<span class="glyphicon glyphicon-search"></span>' +
		'</button>' +
		'</div>' +
		'<div class="input-group-append">' +
		'<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.materials.addProduct") + '" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.operationalTasksDefinitionWizard.openMaterialDefinition(' + new String(row.operationIndex) + ',' + new String(row.index) + ')">' +
		'<span class="glyphicon glyphicon-plus"></span>' +
		'</button>' +
		'</div>' +
		'</div>';
}

function workstationFormatter(value, row) {
	if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
		return '<div class="input-group">' +
			'<input type="text" class="form-control q-auto-complete" onkeypress="QCD.operationalTasksDefinitionWizard.addWorkstationTypeahead(' + new String(row.index) + ')" id="workstation-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
			'<div class="input-group-append">' +
			'<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.workstations.select.tip") + '"  class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.operationalTasksDefinitionWizard.openOtWorkstationsLookup(' + new String(row.index) + ')">' +
			'<span class="glyphicon glyphicon-search"></span>' +
			'</button>' +
			'</div>' +
			'</div>';
	} else {
		return '<div class="input-group">' +
			'<input type="text" class="form-control q-auto-complete" onkeypress="QCD.operationalTasksDefinitionWizard.addWorkstationTypeahead(' + new String(row.index) + ')" id="workstation-' + row.index + '" value="' + nullToEmptyValue(value) + '"/>' +
			'<div class="input-group-append">' +
			'<button type="button" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.workstations.select.tip") + '"  class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.operationalTasksDefinitionWizard.openOtWorkstationsLookup(' + new String(row.index) + ')">' +
			'<span class="glyphicon glyphicon-search"></span>' +
			'</button>' +
			'</div>' +
			'<div class="input-group-append">' +
			'<button type="button"  data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.workstations.new.tip") + '" class="btn btn-outline-secondary bg-primary text-white" onclick="QCD.operationalTasksDefinitionWizard.openWorkstationDefinition(' + new String(row.index) + ')">' +
			'<span class="glyphicon glyphicon-plus"></span>' +
			'</button>' +
			'</div>' +
			'</div>';
	}
}

function quantityPerUnitProductInFormatter(value, row) {
	if (QCD.operationalTasksDefinitionWizardContext.order.technology) {
		return '<div class="input-group">' +
			'<div class="input-group-prepend" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.materials.quantityPerUnit.tip") + '" >' +
			' <label class="form-label grid-label30">' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.quantityPerUnit") + '</label>' +
			'</div>' +
			'<input type="text" class="form-control right decimal" disabled onblur="QCD.operationalTasksDefinitionWizard.quantityPerUnitOnBlur(' + new String(row.operationIndex) + ',' + new String(row.index) + ')" id="quantityPerUnitProductIn-' + row.index + '" value="' + nullToEmptyValue(value) + '"></input>' +
			'</div>';
	} else {
		return '<div class="input-group">' +
			'<div class="input-group-prepend" data-toggle="tooltip" data-placement="top"  title="' + QCD.translate("basic.dashboard.operationalTasksDefinitionWizard.materials.quantityPerUnit.tip") + '" >' +
			' <label class="form-label grid-label30" >' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.quantityPerUnit") + '</label>' +
			'</div>' +
			'<input type="text" class="form-control right decimal"  onblur="QCD.operationalTasksDefinitionWizard.quantityPerUnitOnBlur(' + new String(row.operationIndex) + ',' + new String(row.index) + ')" id="quantityPerUnitProductIn-' + row.index + '" value="' + nullToEmptyValue(value) + '"></input>' +
			'</div>';
	}

}

function quantityProductInFormatter(value, row) {

	return '<div class="input-group">' +
		'<div class="input-group-prepend">' +
		' <label class="form-label grid-label30">' + QCD.translate("basic.dashboard.orderDefinitionWizard.materials.quantity") + '</label>' +
		'</div>' +
		'<input type="text" class="form-control right decimal" onblur="QCD.operationalTasksDefinitionWizard.quantityOnBlur(' + new String(row.operationIndex) + ',' + new String(row.index) + ')"  id="quantityProductIn-' + row.index + '" value="' + nullToEmptyValue(value) + '"></input>' +
		'</div>';

}
