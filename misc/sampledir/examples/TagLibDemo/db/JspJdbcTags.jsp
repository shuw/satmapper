<html>
<head>
   <title>JSP Tags Test : JDBC DB Tags</title>
</head>

<%@ page errorPage="/utility/JspTagsErrorPage.jsp" %>  
<%@taglib uri="/WEB-INF/lib/dbtags.jar" prefix="jdbc" %>
<%@taglib uri="/WEB-INF/lib/ietags.jar" prefix="pr" %>

<body>
<h1> JDBC DBTag Tests </h1>
<a href="/index.html"><img src="/images/return.gif" width="24" height="24" align="right" border="0"></a>

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

<%-- query tag  --%>

<jdbc:query id="cdQuery1" connection="jdbcConn" resultsId="cdDS1" resultsScope="session" >
        SELECT * FROM CD
</jdbc:query>

<%-- iterator tag --%>

<h2> Testing Connection and Query Tags </h2>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="cdDS1" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
  <pr:iterator results="cdDS1" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
  </pr:iterator>
</TABLE>

<br><hr> 
<h2> Testing Connection , Query and Transaction clean up  Tags </h2>

<%-- transaction tag --%>

<jdbc:transaction id="cdTrans" connection="jdbcConn" />

<%-- query tag --%>

<jdbc:query id="cdQuery2" transaction="cdTrans" resultsId="cdDS2" resultsScope="session" >
        SELECT * FROM CD
</jdbc:query>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="cdDS2" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="cdDS2" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>

<%-- transaction closing tag --%>

<jdbc:transaction id="cdTrans" connection="jdbcConn" status="ok" />

<%-- clean up tag --%>

<jdbc:cleanup scope="session" status="ok" />

</body>
</html>
