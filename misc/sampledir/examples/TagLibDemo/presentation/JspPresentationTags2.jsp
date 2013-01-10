<html>
<head>
   <title>JSP Tag Test : Presentation Tags</title>
</head>

<%@ page errorPage="/utility/JspTagsErrorPage.jsp" %> 
<%@taglib uri="/WEB-INF/lib/ietags.jar" prefix="pr" %>
<%@page import="org.netbeans.jsptags.results.BeanVectorResults, Employee" %>

<%
    BeanVectorResults vds = Employee.generateTestResults ();
    pageContext.setAttribute("ds1", vds, PageContext.SESSION_SCOPE);
%>

<body>
<h1> JSP Tag Test : Presentation Tags </h1>
<a href="/index.html"><img src="/images/return.gif" width="24" height="24" align="right" border="0"></a>

<P>

<P>
<h2>Iterator on results. </h2><BR>
<%!
     public boolean even(int n) {
         return (n == 2 * (n/2));
     }

     String colorA="#FFFFDD";
     String colorB="#DDDDFF";
%>
<TABLE border=1>
<pr:iterator results="ds1" indexVar="i" iterationsVar="it" direction="reverse">
<TR bgcolor="<%= (even(it.intValue())?colorA:colorB) %>" >
<TD>
index=<%= i %>
<P>
iterations=<%=it%>: <pr:field index="0" />
</TD>

<pr:fieldIterator indexVar="fi" iterationsVar = "fit">
<TD>
<%=fi%> . <%=fit%> .. <pr:field op="name" /> 
<p>
<pr:field /><p> 
</TD>
</pr:fieldIterator>
</TR>
</pr:iterator>
</TABLE>

</body>
</html>
