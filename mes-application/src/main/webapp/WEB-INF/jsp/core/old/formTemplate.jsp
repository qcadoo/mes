<%--

    ***************************************************************************
    Copyright (c) 2010 Qcadoo Limited
    Project: Qcadoo MES
    Version: 0.2.0

    This file is part of Qcadoo.

    Qcadoo is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation; either version 3 of the License,
    or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    ***************************************************************************

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<tiles:useAttribute name="formElement" />

<form id="${formElement.name}_form">
	<div id="${formElement.name}_globalErrors" class="errorMessage validatorGlobalMessage"></div>
	<div id="${formElement.name}_globalInfo" class="infoMessage"></div>
	<table>
		<c:forEach items="${formElement.fields}" var="field">
		
			<c:choose>
				<c:when test='${(field.control.type == "11" || field.control.type == "13") }'>
					<c:set var="textInputType" value="password" scope="page" />
				</c:when>
				<c:otherwise>
					<c:set var="textInputType" value="text" scope="page" />
				</c:otherwise>
			</c:choose>
		
			<c:choose>
				<c:when test='${(field.control.type == "11" || field.control.type == "13") }'>
					<c:set var="valueType" value="type-password" scope="page" />
				</c:when>
				<c:when test='${(field.control.type == "7") }'>
					<c:set var="valueType" value="type-decimal" scope="page" />
				</c:when>
				<c:when test='${(field.control.type == "6") }'>
					<c:set var="valueType" value="type-integer" scope="page" />
				</c:when>
				<c:when test='${(field.control.type == "3") }'>
					<c:set var="valueType" value="type-datetime" scope="page" />
				</c:when>
				<c:when test='${(field.control.type == "2") }'>
					<c:set var="valueType" value="type-date" scope="page" />
				</c:when>
				<c:when test='${(field.control.type == "10") }'>
					<c:set var="valueType" value="type-reference" scope="page" />
				</c:when>
				<c:otherwise>
					<c:set var="valueType" value="" scope="page" />
				</c:otherwise>
			</c:choose>
			
			<c:set var="inputClass" value="${valueType} ${field.dataField.required ? 'required' : ''} ${field.dataField.requiredOnCreate ? 'required-on-create' : ''} ${field.dataField.readOnlyOnUpdate ? 'readonly-on-update' : ''} ${field.dataField.readOnly ? 'readonly' : ''} ${field.control.type == '13' ? 'confirmable' : ''}" scope="page" />

			<c:set var="tdClass" value="${(field.dataField.required) ? 'fieldRequired' : ''} ${field.dataField.requiredOnCreate ? 'fieldRequired' : ''}" scope="page" />
			
			<c:set var="fieldName" value="${field.dataField.name}${field.control.type == '13' ? '_confirmation' : ''}" scope="page" />
		
			<tr>
				<c:choose>
					<c:when test="${field.hidden == false}">
						<td>
							<c:set var="label" value="${viewDefinition.name}.${formElement.name}.field.${field.name}"/>
							${translationsMap[label]}
						</td>
						<td class="${tdClass}">		
							<c:choose>
								<c:when test='${(field.control.type == "9")}'>
									<textarea id="${formElement.name}_field_${fieldName}" name="fields[${fieldName}]" class="${inputClass}"></textarea>
								</c:when>
								<c:when test='${(field.control.type == "1")}'>
									<input type="checkbox" id="${formElement.name}_field_${fieldName}" name="fields[${fieldName}]"/>
								</c:when>
								<c:when test='${(field.control.type == "4")}'>
									<select id="${formElement.name}_field_${fieldName}" name="fields[${fieldName}]" class="${inputClass}">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldName]}" var="dictionaryValue">
											<option value="${dictionaryValue.key}">${dictionaryValue.value}</option>
										</c:forEach>
									</select>
								</c:when>
								<c:when test='${(field.control.type == "14")}'>
									<select id="${formElement.name}_field_${fieldName}" name="fields[${fieldName}]" class="${inputClass}">
									<option value="0">nie</option>
									<option value="1">tak</option>
									</select>
								</c:when>
								<c:when test='${(field.control.type == "10")}'>
									<select id="${formElement.name}_field_${fieldName}" name="fields[${fieldName}]" class="${inputClass}">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldName]}" var="dictionaryValue">
											<option value="${dictionaryValue.key}">${dictionaryValue.value }</option>
										</c:forEach>
									</select>
								</c:when>
								<c:otherwise>
									<input type="${textInputType}" id="${formElement.name}_field_${fieldName}" name="fields[${fieldName}]" class="${inputClass}"/>
								</c:otherwise>
							</c:choose>
						</td>
						<td id="${formElement.name}_field_${fieldName}_error" class="errorMessage fieldValidatorMessage"></td>
					</c:when>
					<c:otherwise>
						<input type="hidden" id="${formElement.name}_field_${fieldName}" name="fields[${fieldName}]" class="${valueType}"/>
					</c:otherwise>
				</c:choose>
			</tr>
		</c:forEach>
	</table>
		
	<input id="${formElement.name}_field_id" type="hidden" name="id"/>
			
</form>

<button id="${formElement.name}_saveButton">${translationsMap["commons.form.button.accept"] }</button>
<button id="${formElement.name}_saveCloseButton">${translationsMap["commons.form.button.acceptAndClose"] }</button>
<button id="${formElement.name}_cancelButton">${translationsMap["commons.form.button.cancel"] }</button>

