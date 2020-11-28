/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
$(function() {
	$('#fileupload')
			.fileupload(
					{
						pasteZone: null,
						dataType : 'json',
						acceptFileTypes : QCDMultiUpload.acceptFileTypes,

						submit : function(e, data) {
							var locale = window.mainController
							.getComponentByReferenceName(
								"locale")
									.getValue().content.value;
							var batchId = window.mainController
								.getComponentByReferenceName(
									"batchIdForMultiUpload")
										.getValue();
							var batchIdValue = batchId.content;
							if(!batchIdValue.value || 0 === batchIdValue.value){
							    $.each(data.files, function (index, file) {
									if(locale === "pl_PL" || locale === "pl"){
							    	showMessage("failure",
											"Partia niezapisana",
											"Pominięto wgranie pliku: "
													+ file.name);
									} else {
								    	showMessage("failure",
												"Batch is not saved",
												"Omitted file upload: "
														+ file.name);
									}
									});

								return false;
							}
						},
						done : function(e, data) {
							var gr = window.mainController
									.getComponentByReferenceName("form");
							gr.performRefresh;
							mainController.performRefresh;
							var mainViewComponent = mainController
									.getComponentByReferenceName("form")
									|| mainController
											.getComponentByReferenceName("grid");
							if (mainViewComponent) {
								mainViewComponent.performRefresh();
							}

							var locale = window.mainController
							.getComponentByReferenceName(
								"locale")
									.getValue().content.value;
							var filetype = QCDMultiUpload.acceptFileTypes;

							$.each(data.files, function(index, file) {
								if (filetype.test(file.name)) {
								if(locale === "pl_PL" || locale === "pl"){
									showMessage("success", "Wgrywanie zakończone",
											"Wgrano plik: " + file.name);
								} else {
									showMessage("success", "Uploading completed",
											"Loaded a file: " + file.name);
								}
								}
							});
						},

						progressall : function(e, data) {
							var progress = parseInt(data.loaded / data.total
									* 100, 10);
							$('#progress .progress-bar').css('width',
									progress + '%');
						},
						dropZone : $('#dropzone')
					}).bind(
					'fileuploadsubmit',
					function(e, data) {
						var batchId = window.mainController
								.getComponentByReferenceName(
										"batchIdForMultiUpload")
								.getValue();
						var batchIdValue = batchId.content;
						data.formData = {
							batchId : batchIdValue.value
						};
					
					}).bind(
					'fileuploadadd',
					function(e, data) {
						var filetype = QCDMultiUpload.acceptFileTypes;
                        var maxUploadFileMessage = $('#maxUploadFileMessage').text();
						var locale = window.mainController
						.getComponentByReferenceName(
							"locale")
								.getValue().content.value;
						$.each(data.files, function(index, file) {
							if (!filetype.test(file.name)) {
								if(locale === "pl_PL" || locale === "pl"){
								showMessage("failure",
										"Pominięto wgranie pliku",
										"Niedopuszczalny typ pliku: "
												+ file.name);
								} else {
									showMessage("failure",
											"Omitted file upload",
											"Invalid file type: "
													+ file.name);
								}
								return false;
							}
                            if (file.size/1048576 > maxUploadFileMessage.replace( /^\D+/g, '')) {
                                if(locale === "pl_PL" || locale === "pl"){
                                    showMessage("failure",
                                        "Pominięto wgranie pliku",
                                        "Plik: "
                                        + file.name + " "
                                        + maxUploadFileMessage);
                                } else {
                                    showMessage("failure",
                                        "Omitted file upload",
                                        "File: "
                                        + file.name + " "
                                        + maxUploadFileMessage);
                                }
                                return false;
                            }
						});
					});

});

function showMessage(type, title, content) {
	mainController.showMessage({
		type : type,
		title : title,
		content : content
	});
}

