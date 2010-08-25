<%@ page language="java" contentType="text/html; charset=ISO-8859-2"
    pageEncoding="ISO-8859-2"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Modify entity</title>
	<link rel="stylesheet" href="../css/jquery-ui-1.8.4.custom.css" type="text/css" />
	<link rel="stylesheet" href="../css/ui.jqgrid.css" type="text/css" />
	<link rel="stylesheet" href="../css/productGrid.css" type="text/css" />
	<link rel="stylesheet" href="../css/jqModal.css" type="text/css" />
	
	<script type="text/javascript" src="../js/json_sans_eval.js"></script>
	<script type="text/javascript" src="../js/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="../js/jquery.blockUI.js"></script>
	<script type="text/javascript" src="../js/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="../js/qcdGrid.js"></script>
	<script type="text/javascript" src="../js/jqModal.js"></script>

</head>
<body>
	<h2 id="pageHeader"><spring:message code="productsFormView.header"/></h2>
	
	
		<c:choose><c:when test="${message == null}"> </c:when><c:otherwise><spring:message code="productsFormView.${message }"/> <br/></c:otherwise></c:choose>
		
		<form action="saveEntity.html" method="POST">
			<table>
				<c:forEach items="${fieldsDefinition}" var="entry">
					<tr>
						<c:choose>
							<c:when test="${entry.hidden=='false'}">
									<td><spring:message code="products.field.${entry.name}"/></td>
									<c:choose>
										<c:when test='${(fieldsTypes[entry.name] == "4") || (fieldsTypes[entry.name] == "5") }'>
											<td>
												<select name="fields[${entry.name}]">
													<c:forEach items="${lists[entry.name] }" var="listEntry">
														<c:choose>
															<c:when test='${listEntry  == entity[entry.name]}'>
																<option selected="selected">${listEntry } </option>
															</c:when>
															<c:otherwise>
																<option>${listEntry }</option>
															</c:otherwise>
														</c:choose>		
													</c:forEach>
												</select>
											</td>
										</c:when>
										<c:when test='${(fieldsTypes[entry.name] == "9") }'>
											<td>
												<textarea name="fields[${entry.name}]">${entity[entry.name]}</textarea>
											</td>
										</c:when>
										<c:otherwise>
											<td>
											<input type="text" name="fields[${entry.name}]"
											<c:if test="${entry.editable=='true'}">
												readonly="readonly"
											</c:if> 
											value="${entity[entry.name]}" />
											</td>
										</c:otherwise>
									</c:choose>
									
									<c:choose>
										<c:when test="${fieldsValidationInfo[entry.name] == null}"> </c:when>
										<c:otherwise><td><spring:message code="productsFormView.${fieldsValidationInfo[entry.name] }"/></td> </c:otherwise>
									</c:choose>
							</c:when> 
							<c:otherwise>
								<input type="hidden" name="fields[${entry.name}]" value="${entity[entry.name]}" />
							</c:otherwise> 
						</c:choose>  
					</tr>
				</c:forEach>	
			</table>	
			<input type="hidden" name="id" value="${entityId }"/>
			<input type="submit" name="button" value="<spring:message code="productsFormView.button"/>" />
			<input type="button" name="button" value="<spring:message code="productsFormView.cancel"/>" onClick="window.location='list.html'" />
		</form><br />
		
		<%@ include file="substitutes.jsp" %>

</body>
</html>



