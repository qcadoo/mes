<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page import="com.qcadoo.mes.core.data.definition.FieldTypeFactory" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<div class="validatorGlobalMessage"></div>
<table>
			<c:forEach items="${entityFieldsDefinition}" var="fieldDefinition">
			<tr>
				<c:choose>
					<c:when test="${fieldDefinition.hidden == false}">
						<td>
							<spring:message code="${entityType}.field.${fieldDefinition.name}"/>
						</td>
						<td>
							<c:choose>
								<c:when test='${(fieldDefinition.type.numericType == "9") }'>
									<textarea name="fields[${fieldDefinition.name}]">${entity.fields[fieldDefinition.name]}</textarea>
								</c:when>
							
								<c:when test='${(fieldDefinition.type.numericType == "8") }'>
									<input type="text" name="fields[${fieldDefinition.name}]" value="${entity.fields[fieldDefinition.name]}"/>
								</c:when>
								
								<c:when test='${(fieldDefinition.type.numericType == "7") }'>
									<input type="text" name="fields[${fieldDefinition.name}]" value="${entity.fields[fieldDefinition.name]}"/>
								</c:when>
							
								<c:when test='${(fieldDefinition.type.numericType == "6") }'>
									<input type="text" name="fields[${fieldDefinition.name}]" value="${entity.fields[fieldDefinition.name]}"/>
								</c:when>
							
								<c:when test='${(fieldDefinition.type.numericType == "3") }'>
									<input type="text" name="fields[${fieldDefinition.name}]" value="${entity.fields[fieldDefinition.name]}"/>
								</c:when>
								
								<c:when test='${(fieldDefinition.type.numericType == "4") || (fieldDefinition.type.numericType == "5") }'>
									<select name="fields[${fieldDefinition.name}]">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldDefinition.name] }" var="dictionaryValue">
											<c:choose>
												<c:when test='${dictionaryValue.value  == entity.fields[fieldDefinition.name]}'>
													<option selected="selected">${dictionaryValue.value } </option>
												</c:when>
												<c:otherwise>
													<option>${dictionaryValue.value }</option>
												</c:otherwise>
											</c:choose>		
										</c:forEach>
									</select>
								</c:when>
								
								<c:when test='${(fieldDefinition.type.numericType == "10") }'>
									<select name="fields[${fieldDefinition.name}]">
										<option></option>
										<c:forEach items="${dictionaryValues[fieldDefinition.name] }" var="dictionaryValue">
											<c:choose>
												<c:when test='${dictionaryValue.key  == entity.fields[fieldDefinition.name].id}'>
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
						<td id="${fieldDefinition.name}_validateMessage" class="fieldValidatorMessage">		
							
						</td>
					</c:when>
					<c:otherwise>
					
						<input type="hidden" name="fields[${fieldDefinition.name}]" value="${entity.fields[fieldDefinition.name].id}"/>
					
					</c:otherwise>
				</c:choose>
			</tr>
			</c:forEach>
		</table>
		
		<input id="entityId" type="hidden" name="id" value="${entity.id }"/>