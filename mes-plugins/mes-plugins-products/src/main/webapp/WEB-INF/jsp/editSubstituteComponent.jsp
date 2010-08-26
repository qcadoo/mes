<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<link rel="stylesheet" href="../css/jqModal.css" type="text/css" />

</head>
<body>
	<div class="modalHeader">substitute component</div>
	<div class="modalContent">
		<div class="validatorGlobalMessage"></div>
		<form id="substituteComponentForm">
		<table>
			<c:forEach items="${substituteComponentDataDefinition}" var="substituteComponentFieldDefinition">
			<tr>
				<c:choose>
					<c:when test="${substituteComponentFieldDefinition.hidden == false}">
						<td>
							<spring:message code="substituteComponent.field.${substituteComponentFieldDefinition.name}"/>
						</td>
						<td>
							<c:choose>
			
								<c:when test='${(substituteComponentFieldDefinition.type.numericType == "9") }'>
									<td>
										<textarea name="fields[${substituteComponentFieldDefinition.name}]">${substituteComponent.fields[substituteComponentFieldDefinition.name]}</textarea>
									</td>
								</c:when>
								
								<c:when test='${(substituteComponentFieldDefinition.type.numericType == "8") }'>
									<td>
										<input type="text" name="fields[${substituteComponentFieldDefinition.name}]" value="${substituteComponent.fields[substituteComponentFieldDefinition.name]}"/>
									</td>
								</c:when>
								
								<c:when test='${(substituteComponentFieldDefinition.type.numericType == "6") }'>
									<td>
										<input type="text" name="fields[${substituteComponentFieldDefinition.name}]" value="${substituteComponent.fields[substituteComponentFieldDefinition.name]}"/>
									</td>
								</c:when>
								
								<c:when test='${(substituteComponentFieldDefinition.type.numericType == "3") }'>
									<td>
										<input type="text" name="fields[${substituteComponentFieldDefinition.name}]" value="${substituteComponent.fields[substituteComponentFieldDefinition.name]}"/>
									</td>
								</c:when>
								
								<c:when test='${(substituteComponentFieldDefinition.type.numericType == "7") }'>
									<td>
										<input type="text" name="fields[${substituteComponentFieldDefinition.name}]" value="${substituteComponent.fields[substituteComponentFieldDefinition.name]}"/>
									</td>
								</c:when>
								
								<c:when test='${(substituteComponentFieldDefinition.type.numericType == "10") }'>
									<td>
										<select name="fields[${substituteComponentFieldDefinition.name}]">
											<c:forEach items="${options[substituteComponentFieldDefinition.name] }" var="option">
												<c:choose>
													<c:when test='${option.key  == substituteComponent.fields[substituteComponentFieldDefinition.name].id}'>
														<option value="${option.key}" selected="selected">${option.value } </option>
													</c:when>
													<c:otherwise>
														<option value="${option.key}">${option.value }</option>
													</c:otherwise>
												</c:choose>		
											</c:forEach>
										</select>
									</td>
								</c:when>
								
							</c:choose>
						</td>
						<td id="${substituteComponentFieldDefinition.name}_validateMessage" class="fieldValidatorMessage">		
							
						</td>
					</c:when>
					<c:otherwise>
					
						<input type="hidden" name="fields[${substituteComponentFieldDefinition.name}]" value="${substituteComponent.fields[substituteComponentFieldDefinition.name].id}"/>
					
					</c:otherwise>
				</c:choose>
			</tr>
			</c:forEach>
		</table>
		
		<input type="hidden" name="id" value="${substituteComponent.id }"/>
		</form>
	</div>
	<div class="modalFooter">
		<button id="ajaxSubmit" onclick="editSubstituteComponentApplyClick()">Apply</button>
		<button class="jqmClose">zamknij</button>
	</div>
</body>
</html>