<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<tiles:useAttribute name="formId" />
<tiles:useAttribute name="dataDefinition" />
<tiles:useAttribute name="entity" ignore="true"/>

<form id="${formId}_form">
	<div id="${formId}_globalErrors" class="errorMessage"></div>
	<table>
		<c:forEach items="${dataDefinition.fields}" var="fieldEntry">
			<tr>
				<c:choose>
					<c:when test="${fieldEntry.value.hidden == false}">
						<td>
							<spring:message code="${dataDefinition.entityName}.field.${fieldEntry.key}"/>
						</td>
						<td>		
							<c:choose>
								<c:when test='${(fieldEntry.value.type.numericType == "9") }'>
									<textarea id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]"></textarea>
								</c:when>
							
								<c:when test='${(fieldEntry.value.type.numericType == "8") }'>
									<input type="text" id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]"/>
								</c:when>
								
								<c:when test='${(fieldEntry.value.type.numericType == "7") }'>
									<input type="text" id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]""/>
								</c:when>
							
								<c:when test='${(fieldEntry.value.type.numericType == "6") }'>
									<input type="text" id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]"/>
								</c:when>
							
								<c:when test='${(fieldEntry.value.type.numericType == "3") }'>
									<input type="text" id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]"/>
								</c:when>
								
								<c:when test='${(fieldEntry.value.type.numericType == "4") || (fieldEntry.value.type.numericType == "5") }'>
									<select id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldEntry.key] }" var="dictionaryValue">
											<option>${dictionaryValue.value }</option>
										</c:forEach>
									</select>
								</c:when>
								
								<c:when test='${(fieldEntry.value.type.numericType == "10") }'>
									<select id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldEntry.key] }" var="dictionaryValue">
											<option value="${dictionaryValue.key}">${dictionaryValue.value }</option>
										</c:forEach>
									</select>
								</c:when>
								
							</c:choose>
						</td>
						<td id="${formId}_field_${fieldEntry.key}_error" class="errorMessage"></td>
					</c:when>
					<c:otherwise>
						<input type="hidden" id="${formId}_field_${fieldEntry.key}" name="fields[${fieldEntry.key}]"/>
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

