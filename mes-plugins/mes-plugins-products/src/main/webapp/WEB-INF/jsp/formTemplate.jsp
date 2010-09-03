<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<tiles:useAttribute name="formId" />
<tiles:useAttribute name="dataDefinition" />
<tiles:useAttribute name="entity" ignore="true"/>

<form id="${formId}_form">
	<div id="${formId}_globalErrors" class="errorMessage validatorGlobalMessage"></div>
	<table>
		<c:forEach items="${dataDefinition.fields}" var="fieldEntry">
		
			<c:choose>
				<c:when test='${(fieldEntry.value.type.numericType == "11") }'>
					<c:set var="textInputType" value="password" scope="page" />
				</c:when>
				<c:otherwise>
					<c:set var="textInputType" value="text" scope="page" />
				</c:otherwise>
			</c:choose>
		
			<c:choose>
				<c:when test='${(fieldEntry.value.type.numericType == "11") }'>
					<c:set var="valueType" value="type-password" scope="page" />
				</c:when>
				<c:when test='${(fieldEntry.value.type.numericType == "7") }'>
					<c:set var="valueType" value="type-decimal" scope="page" />
				</c:when>
				<c:when test='${(fieldEntry.value.type.numericType == "6") }'>
					<c:set var="valueType" value="type-integer" scope="page" />
				</c:when>
				<c:when test='${(fieldEntry.value.type.numericType == "3") }'>
					<c:set var="valueType" value="type-datetime" scope="page" />
				</c:when>
				<c:when test='${(fieldEntry.value.type.numericType == "2") }'>
					<c:set var="valueType" value="type-date" scope="page" />
				</c:when>
				<c:when test='${(fieldEntry.value.type.numericType == "10") }'>
					<c:set var="valueType" value="type-reference" scope="page" />
				</c:when>
				<c:otherwise>
					<c:set var="valueType" value="" scope="page" />
				</c:otherwise>
			</c:choose>
			
			<c:set var="inputClass" value="${valueType} ${fieldEntry.value.required ? 'required' : ''} ${fieldEntry.value.requiredOnCreation ? 'required-on-creation' : ''} ${fieldEntry.value.editable ? '' : 'readonly'}" scope="page" />

			<c:set var="tdClass" value="${(fieldEntry.value.required || fieldEntry.value.requiredOnCreation) ? 'fieldRequired' : ''}" scope="page" />
		
			<tr>
				<c:choose>
					<c:when test="${fieldEntry.value.hidden == false}">
						<td>
							<spring:message code="${dataDefinition.entityName}.field.${fieldEntry.key}"/>
						</td>
						<td class="${tdClass}">		
							<c:choose>
								<c:when test='${(fieldEntry.value.type.numericType == "9")}'>
									<textarea id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]" class="${inputClass}"></textarea>
								</c:when>
								<c:when test='${(fieldEntry.value.type.numericType == "1")}'>
									<input type="checkbox" id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]"/>
								</c:when>
								<c:when test='${(fieldEntry.value.type.numericType == "4") || (fieldEntry.value.type.numericType == "5")}'>
									<select id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]" class="${inputClass}">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldEntry.key]}" var="dictionaryValue">
											<option>${dictionaryValue.value }</option>
										</c:forEach>
									</select>
								</c:when>
								<c:when test='${(fieldEntry.value.type.numericType == "10")}'>
									<select id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]" class="${inputClass}">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldEntry.key]}" var="dictionaryValue">
											<option value="${dictionaryValue.key}">${dictionaryValue.value }</option>
										</c:forEach>
									</select>
								</c:when>
								<c:otherwise>
									<input type="${textInputType}" id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]" class="${inputClass} ${fieldEntry.value.confirmable ? 'confirmable' : ''}"/>
								</c:otherwise>
							</c:choose>
						</td>
						<td id="${formId}_field_${fieldEntry.key}_error" class="errorMessage fieldValidatorMessage"></td>
						<c:if test='${(fieldEntry.value.confirmable)}'>
							</tr><tr><td>
								<spring:message code="${dataDefinition.entityName}.field.${fieldEntry.key}_confirmation"/>
							</td>
							<td class="${tdClass}">
								<input type="${textInputType}" id="${formId}_field_${fieldEntry.key}_confirmation" name="fields[${fieldEntry.key}_confirmation]" class="${inputClass}"/>
							</td>
							<td id="${formId}_field_${fieldEntry.key}_confirmation_error" class="errorMessage fieldValidatorMessage"></td>
						</c:if>
					</c:when>
					<c:otherwise>
						<input type="hidden" id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]" class="${valueType}"/>
					</c:otherwise>
				</c:choose>
			</tr>
		</c:forEach>
	</table>
		
	<input id="${formId}_field_id" type="hidden" name="id"/>
			
</form>

<button id="${formId}_saveButton"><spring:message code="commons.form.button.accept"/></button>
<button id="${formId}_saveCloseButton"><spring:message code="commons.form.button.acceptAndClose"/></button>
<button id="${formId}_cancelButton"><spring:message code="commons.form.button.cancel"/></button>

