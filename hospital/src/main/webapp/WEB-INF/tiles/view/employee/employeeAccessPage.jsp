<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>


<div class="page-header">
	<h3>Set Employee Access</h3>
</div>

<strong>Select Employee whose access you want to set:</strong>

<form:form class="form" method="post">

<div class="form-group">

	<select id="personId" name="personId" class="btn btn-default btn-sm dropdown-toggle">
		<c:forEach var="p" items="${employees}" >
			<option id="${p[0]}" value="${p[0]}" <c:if test="${p[0] eq selected}"> selected='selected'</c:if> > ${p[1]} ${p[2]} - ${p[3]} </option>
		</c:forEach>
	</select>
	
	<button type="submit" class="btn btn-primary">Show Access</button>
	<hr/>
	
	<c:forEach var="a" items="${authorities}" >
		<input type="checkbox" value="${a.authority}">&nbsp;&nbsp;${a.authority}<br/>
	</c:forEach>
	
	

</div>	

</form:form>


