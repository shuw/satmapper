<%@page contentType="text/html"%>
<%@ page import="java.util.*,Cart,CheckOutBean" %>

     <html>
     <head><title>Place Order</title></head>
     <body>
     <h1> Place Order </h1>

     <jsp:useBean id="myCart" scope="session" type="Cart" />
     <jsp:useBean id="checker" scope="session" class="CheckOutBean" />

     <%! int ordNum; %>

     <%
     ordNum = checker.checkout(myCart);
     session.invalidate();
     %>

     Your order has been placed. For future reference, your order number is <%=ordNum%>.

     Thank you for shopping.

     <p>

     <a href="ProductList.jsp">Resume Shopping</a> 

     </body>
     </html>
