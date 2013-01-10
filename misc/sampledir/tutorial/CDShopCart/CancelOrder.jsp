<%@page contentType="text/html"%>
<html>
<head><title>Cancel Order</title></head>
<body>
<h1> Cancel Order </h1>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="package.class" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>
<%
session.invalidate();
%>
Your order has been cancelled. Thank you for shopping.
<p>
<form method=get action="ProductList.jsp">
<input type=submit value="Resume Shopping">
</form>
</body>
</html>
