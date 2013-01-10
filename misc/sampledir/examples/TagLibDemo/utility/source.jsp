
<%  String jspFile=request.getQueryString(); %>

<html>
<head>
   <title> Source : <%=jspFile%></title>
</head>

<%@page import="java.io.*" %>

<br><hr><br>
<a href="/index.html"><img src="/images/return.gif" width="24" height="24" align="right" border="0"></a>
<b><h4>Source : <%=jspFile%> </h4></b>
<br><hr>
<%
    // String jspFile=request.getQueryString();

	if (jspFile.indexOf( ".." ) >= 0)
	    throw new JspException("Invalid JSP file " + jspFile);

        InputStream in
            = pageContext.getServletContext().getResourceAsStream(jspFile);

        if (in == null)
            throw new JspException("Unable to find JSP file: "+jspFile);

        InputStreamReader reader = new InputStreamReader(in);
	    // JspWriter out = pageContext.getOut();


        try {
            // out.println("<body>");
            out.println("<pre>");
            for(int ch = in.read(); ch != -1; ch = in.read())
                if (ch == '<')
                    out.print("&lt;");
                else
                    out.print((char) ch);
            out.println("</pre>");
            // out.println("</body>");
        } catch (IOException ex) {
            throw new JspException("IOException: "+ex.toString());
        }

%>
<br><hr>
</body>
</html>

