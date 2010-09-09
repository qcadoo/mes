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
	<table>
		<c:forEach items="${formElement.fields}" var="field">
		
			<c:choose>
				<c:when test='${(field.control.type == "11") }'>
					<c:set var="textInputType" value="password" scope="page" />
				</c:when>
				<c:otherwise>
					<c:set var="textInputType" value="text" scope="page" />
				</c:otherwise>
			</c:choose>
		
			<c:choose>
				<c:when test='${(field.control.type == "11") }'>
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
			
			<c:set var="inputClass" value="${valueType} ${field.dataField.required ? 'required' : ''} ${field.dataField.editable ? '' : 'editable'} ${field.dataField.readOnly ? 'readonly' : ''}" scope="page" />

			<c:set var="tdClass" value="${(field.dataField.required) ? 'fieldRequired' : ''}" scope="page" />
		
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
									<textarea id="${formElement.name}_field_${field.dataField.name}" name="fields[${field.dataField.name}]" class="${inputClass}"></textarea>
								</c:when>
								<c:when test='${(field.control.type == "1")}'>
									<input type="checkbox" id="${formElement.name}_field_${field.dataField.name}" name="fields[${field.dataField.name}]"/>
								</c:when>
								<c:when test='${(field.control.type == "4")}'>
									<select id="${formElement.name}_field_${field.dataField.name}" name="fields[${field.dataField.name}]" class="${inputClass}">
										<option></option>
										<c:forEach items="${dictionaryValues[field.dataField.name]}" var="dictionaryValue">
											<option>${dictionaryValue.value}</option>
										</c:forEach>
									</select>
								</c:when>
								<c:when test='${(field.control.type == "10")}'>
									<select id="${formElement.name}_field_${field.dataField.name}" name="fields[${field.dataField.name}]" class="${inputClass}">
										<option></option>
										<c:forEach items="${dictionaryValues[field.dataField.name]}" var="dictionaryValue">
											<option value="${dictionaryValue.key}">${dictionaryValue.value }</option>
										</c:forEach>
									</select>
								</c:when>
								<c:otherwise>
									<input type="${textInputType}" id="${formElement.name}_field_${field.dataField.name}" name="fields[${field.dataField.name}]" class="${inputClass} ${field.confirmable ? 'confirmable' : ''}"/>
								</c:otherwise>
							</c:choose>
						</td>
						<td id="${formElement.name}_field_${field.dataField.name}_error" class="errorMessage fieldValidatorMessage"></td>
						<c:if test='${(field.confirmable)}'>
							</tr><tr><td>
								${translationsMap["commons.form.field.confirmable.label"]}
							</td>
							<td class="${tdClass}">
								<input type="${textInputType}" id="${formElement.name}_field_${field.dataField.name}_confirmation" name="fields[${field.dataField.name}_confirmation]" class="${inputClass}"/>
							</td>
							<td id="${formElement.name}_field_${field.dataField.name}_confirmation_error" class="errorMessage fieldValidatorMessage"></td>
						</c:if>
					</c:when>
					<c:otherwise>
						<input type="hidden" id="${formElement.name}_field_${field.dataField.name}" name="fields[${field.dataField.name}]" class="${valueType}"/>
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

