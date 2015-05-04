/*
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
						
						dataType : 'json',
						acceptFileTypes : /(\.|\/)(gif|jpe?g|png|pdf)$/i,

						submit : function(e, data) {
							var locale = window.mainController
							.getComponentByReferenceName(
								"subassemblyMultiUploadLocale")
									.getValue().content.value;
							var techId = window.mainController
								.getComponentByReferenceName(
									"subassemblyIdForMultiUpload")
										.getValue();
							var techIdValue = techId.content;
							if(!techIdValue.value || 0 === techIdValue.value){
							    $.each(data.files, function (index, file) {
									if(locale === "pl"){
							    	showMessage("failure",
											"Podzespół niezapisany",
											"Pominięto wgranie pliku : "
													+ file.name);
									} else {
								    	showMessage("failure",
												"Subassembly is not saved",
												"Omitted file upload : "
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
								"subassemblyMultiUploadLocale")
									.getValue().content.value;
							var filetype = /(\.|\/)(gif|jpe?g|png|pdf)$/i;

							$.each(data.files, function(index, file) {
								if (filetype.test(file.name)) {
								if(locale === "pl"){
									showMessage("success", "Wgrywanie zakończone",
											"Wgrano plik : " + file.name);
								} else {
									showMessage("success", "Uploading completed",
											"Loaded a file : " + file.name);
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
						var techId = window.mainController
								.getComponentByReferenceName(
										"subassemblyIdForMultiUpload")
								.getValue();
						var techIdValue = techId.content;
						data.formData = {
							techId : techIdValue.value
						};
					
					}).bind(
					'fileuploadadd',
					function(e, data) {
						var filetype = /(\.|\/)(gif|jpe?g|png|pdf)$/i;
						var locale = window.mainController
						.getComponentByReferenceName(
							"subassemblyMultiUploadLocale")
								.getValue().content.value;
						$.each(data.files, function(index, file) {
							/*if (!filetype.test(file.name)) {
								if(locale === "pl"){
								showMessage("failure",
										"Pominięto wgranie pliku",
										"Niedopuszczalny typ pliku : "
												+ file.name);
								} else {
									showMessage("failure",
											"Omitted file upload",
											"Invalid file type : "
													+ file.name);
								}
								return false;
							}*/

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

