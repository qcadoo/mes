<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ page import="com.qcadoo.mes.core.data.definition.FieldTypeFactory" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	
</head>
<body>
	<div class="modalHeader">substitute</div>
	<div class="modalContent">
		<div class="validatorGlobalMessage"></div>
		<form id="substituteForm">
		<table>
			<c:forEach items="${substituteFieldsDefinition}" var="substituteFieldDefinition">
			<tr>
				<c:choose>
					<c:when test="${substituteFieldDefinition.hidden == false}">
						<td>
							<spring:message code="substitutes.field.${substituteFieldDefinition.name}"/>
						</td>
						<td>
							<c:choose>
			
								<c:when test='${(substituteFieldDefinition.type.numericType == "9") }'>
									<td>
										<textarea name="fields[${substituteFieldDefinition.name}]">${substitute.fields[substituteFieldDefinition.name]}</textarea>
									</td>
								</c:when>
								
								<c:when test='${(substituteFieldDefinition.type.numericType == "8") }'>
									<td>
										<input type="text" name="fields[${substituteFieldDefinition.name}]" value="${substitute.fields[substituteFieldDefinition.name]}"/>
									</td>
								</c:when>
								
								<c:when test='${(substituteFieldDefinition.type.numericType == "6") }'>
									<td>
										<input type="text" name="fields[${substituteFieldDefinition.name}]" value="${substitute.fields[substituteFieldDefinition.name]}"/>
									</td>
								</c:when>
								
								<c:when test='${(substituteFieldDefinition.type.numericType == "3") }'>
									<td>
										<input type="text" name="fields[${substituteFieldDefinition.name}]" value="${substitute.fields[substituteFieldDefinition.name]}"/>
									</td>
								</c:when>
								
							</c:choose>
						</td>
						<td id="${substituteFieldDefinition.name}_validateMessage" class="fieldValidatorMessage">		
							
						</td>
					</c:when>
					<c:otherwise>
					
						<input type="hidden" name="fields[${substituteFieldDefinition.name}]" value="${substitute.fields[substituteFieldDefinition.name].id}"/>
					
					</c:otherwise>
				</c:choose>
			</tr>
			</c:forEach>
		</table>
		
		<input type="hidden" name="id" value="${substitute.id }"/>
		</form>
	</div>
	<div class="modalFooter">
		<button id="ajaxSubmit" onclick="editSubstituteApplyClick()">Apply</button>
		<button class="jqmClose">zamknij</button>
	</div>
	
</body>
</html>