<html>
<head><title>JSP Tags Error Page</title></head>
<%@ page isErrorPage="true" %> 
<body>
<a href="/index.html"><img src="/images/return.gif" width="24" height="24" align="right" border="0"></a>

<b> Uncaught exception occured in this page <b>

<br><hr><b> Exception Message : </b> <%= exception.getMessage() %> <br><hr><br>

<br><br><hr><b> Exception Stack Trace : </b> 
    <%
    out.println("<p>");
    new Exception().printStackTrace(new java.io.PrintWriter(pageContext.getOut()) {    public void println(String s) {
           print("&nbsp;&nbsp;&nbsp;&nbsp; ");
           super.println(s);
           print("<br>");    }
    });
    out.println("</p>");
    %>
<br><hr><br>

</body>
</html>
