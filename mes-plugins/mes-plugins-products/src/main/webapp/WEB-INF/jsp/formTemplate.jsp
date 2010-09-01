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

<div class="${formId}_validatorGlobalMessage validatorGlobalMessage"></div>
<form id="${formId}_form">
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
									<textarea name="fields[${fieldEntry.key}]">${entity.fields[fieldEntry.key]}</textarea>
								</c:when>
							
								<c:when test='${(fieldEntry.value.type.numericType == "8") }'>
									<input type="text" name="fields[${fieldEntry.key}]" value="${entity.fields[fieldEntry.key]}"/>
								</c:when>
								
								<c:when test='${(fieldEntry.value.type.numericType == "7") }'>
									<input type="text" name="fields[${fieldEntry.key}]" value="${entity.fields[fieldEntry.key]}"/>
								</c:when>
							
								<c:when test='${(fieldEntry.value.type.numericType == "6") }'>
									<input type="text" name="fields[${fieldEntry.key}]" value="${entity.fields[fieldEntry.key]}"/>
								</c:when>
							
								<c:when test='${(fieldEntry.value.type.numericType == "3") }'>
									<input type="text" name="fields[${fieldEntry.key}]" value="${entity.fields[fieldEntry.key]}"/>
								</c:when>
								
								<c:when test='${(fieldEntry.value.type.numericType == "4") || (fieldEntry.value.type.numericType == "5") }'>
									<select name="fields[${fieldEntry.key}]">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldEntry.key] }" var="dictionaryValue">
											<c:choose>
												<c:when test='${dictionaryValue.value  == entity.fields[fieldEntry.key]}'>
													<option selected="selected">${dictionaryValue.value } </option>
												</c:when>
												<c:otherwise>
													<option>${dictionaryValue.value }</option>
												</c:otherwise>
											</c:choose>		
										</c:forEach>
									</select>
								</c:when>
								
								<c:when test='${(fieldEntry.value.type.numericType == "10") }'>
									<select name="fields[${fieldEntry.key}]">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldEntry.key] }" var="dictionaryValue">
											<c:choose>
												<c:when test='${dictionaryValue.key  == entity.fields[fieldEntry.key].id}'>
													<option value="${dictionaryValue.key}" selected="selected">${dictionaryValue.value } </option>
												</c:when>
												<c:otherwise>
													<option value="${dictionaryValue.key}">${dictionaryValue.value }</option>
												</c:otherwise>
											</c:choose>		
										</c:forEach>
									</select>
								</c:when>
								
							</c:choose>
						</td>
						<td id="${formId}_${fieldEntry.key}_validateMessage" class="fieldValidatorMessage ${formId}_validateMessage">		
						</td>
					</c:when>
					<c:otherwise>
					
						<input type="hidden" name="fields[${fieldEntry.key}]" value="${entity.fields[fieldEntry.key].id}"/>
					
					</c:otherwise>
				</c:choose>
			</tr>
		</c:forEach>
	</table>
		
	<input id="entityId" type="hidden" name="id" value="${entity.id }"/>
			
</form>

<button id="${formId}_saveButton"><spring:message code="productsFormView.button"/></button>
<button id="${formId}_saveCloseButton">TODO</button>
<button id="${formId}_cancelButton"><spring:message code="productsFormView.cancel"/></button>

