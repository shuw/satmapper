<%@page contentType="text/html"%>
<%@ page import="java.util.*, Cart, CartLineItem" %>

<html>
<head><title>Shopping Cart</title></head>
<%@taglib uri="/WEB-INF/lib/ietags.jar" prefix="pr" %>
<body>
<h1> Shopping Cart </h1>

<jsp:useBean id="myCart" scope="session" class="Cart" /> 
<%
    String myOperation = request.getParameter("operation");
        session.setAttribute("myLineItems", myCart.getLineItems());

    if (myOperation.equals("Add"))
        {
        CartLineItem lineItem = new CartLineItem();
        lineItem.setId(request.getParameter("cdId"));
        lineItem.setCdtitle(request.getParameter("cdTitle"));
        lineItem.setPrice(request.getParameter("cdPrice"));
        myCart.lineItems.addElement(lineItem);
        }
    if (myOperation.equals("Delete"))
        {
        String s = request.getParameter("cdId");
        System.out.println(s);
        int idVal = Integer.parseInt(s);
        myCart.removeLineItem(idVal);
        }
    if (((Vector)session.getAttribute("myLineItems")).size() == 0)
        {
        %>
        <jsp:forward page="EmptyCart.jsp" />
        <%
        }
        %>

<TABLE border=1>
  <TR>
    <TH>ID</TH>
    <TH>CD Title</TH>
    <TH>Price</TH>
  </TR>

<pr:iterator results="myLineItems" >
  <TR>
    <TD><pr:field name="id"/></TD>
    <TD><pr:field name="cdtitle"/></TD>
    <TD><pr:field name="price"/></TD>

<TD>
<form method=get action="ShopCart.jsp">
<input type=hidden name=cdId value="<pr:field name="id"/>">
<input type=hidden name=cdTitle value="<pr:field name="cdtitle"/>">
<input type=hidden name=cdPrice value="<pr:field name="price"/>">
<input type=submit name=operation value=Delete>
</form>

</TD>

  </TR>
</pr:iterator>

</TABLE>
<p>
<form method=get action="ProductList.jsp">
<input type=submit value="Resume Shopping">
</form>
<form method=get action="PlaceOrder.jsp">
<input type=submit value="Place Order">
</form>
<form method=get action="CancelOrder.jsp">
<input type=submit value="Cancel Order">
</form>

<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

</body>
</html>
