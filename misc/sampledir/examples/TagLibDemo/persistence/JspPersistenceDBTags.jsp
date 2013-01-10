<html>
<head>
   <title>JSP Tags Test : TP DB Tags </title>
</head>

<%@ page errorPage="/utility/JspTagsErrorPage.jsp" %> 
<%@ page import="CDPackage.*" %>

<%@taglib uri="/WEB-INF/lib/dbtags.jar" prefix="jdbc" %>
<%@taglib uri="/WEB-INF/lib/tptags.jar" prefix="jdo" %>
<%@taglib uri="/WEB-INF/lib/ietags.jar" prefix="pr" %>


<body>
<h1> JSP Persistence DB Tag Tests </h1>
<a href="/index.html"><img src="/images/return.gif" width="24" height="24" align="right" border="0"></a>

<hr> <h2> Testing Connection & Persistence Manager & Query Tags </h2>

<%-- connection tag for MS sqlserver --%>
<%--
<jdbc:connection id="jdbcConn"
       driver="weblogic.jdbc.mssqlserver4.Driver"
       url="jdbc:weblogic:mssqlserver4:pubs@jediknight:1433"
       user="sa" password="" />
--%>

<%-- connection tag for oracle --%>
<%--
<jdbc:connection id="jdbcConn"
       driver="oracle.jdbc.driver.OracleDriver"
       url="jdbc:oracle:thin:@<machine>:1521:<instance>"
       user="scott" password="tiger" /> 
--%>

<%-- connection tag for pointbase --%>

<jdbc:connection id="jdbcConn"
       driver="com.pointbase.jdbc.jdbcUniversalDriver"
       url="jdbc:pointbase://localhost/cdshopcart"
       user="PUBLIC" password="PUBLIC" />

<%-- persistence Manager tag --%>

<jdo:persistenceManager id="cdPM" connection="jdbcConn" />

<b><h4>Showing results with no ordering </h4></b>

<jdo:jdoQuery id="cdQuery"
        persistenceManager="cdPM"
        className="CDPackage.Cd"
        resultsId="cdDS" resultsScope="session" />

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="cdDS" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="cdDS" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>

<br>
<hr><b><h4>Showing results with ordering on artist name </h4></b>

<jdo:jdoQuery id="employeeQuery"
        persistenceManager="cdPM"
        className="CDPackage.Cd"
        ordering="artist ascending"
        resultsId="cdDS" resultsScope="session" />

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="cdDS" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="cdDS" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>

<%-- clean up tag --%>

<jdbc:cleanup scope="session" status="ok" />

</body>
</html>
