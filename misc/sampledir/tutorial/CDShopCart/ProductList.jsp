<%@page contentType="text/html"%>
<html>
<head><title>CD Catalog List</title></head>
<%@taglib uri="/WEB-INF/lib/dbtags.jar" prefix="jdbc" %>
<%@taglib uri="/WEB-INF/lib/ietags.jar" prefix="pr" %>
<body>
<h1> CD Catalog List </h1>

<jdbc:connection id="jdbcConn"
       driver="com.pointbase.jdbc.jdbcUniversalDriver"
       url="jdbc:pointbase://localhost/cdshopcart,database.home=d:\\forte4j\\pointbase\\network\\databases"
       user="PUBLIC" password="PUBLIC" />  
<%-- Use this url for Pointbase connection if you plan to use the debugger.
     Edit the database.home part of the string to point to your pointbase directory.
       url="jdbc:pointbase://localhost/cdshopcart,database.home=d:\\forte4j\\pointbase\\network\\databases"
--%>

<%--<jdbc:connection id="jdbcConn"
       driver="oracle.jdbc.driver.OracleDriver"
       url="jdbc:oracle:thin:@<hostname>:<port#>:<SID>"
       user="scott" password="tiger" /> --%>

<%--<jdbc:connection id="jdbcConn"
       driver="weblogic.jdbc.mssqlserver4.Driver"
       url="jdbc:weblogic:mssqlserver4:<database>@<hostname>:<port#>"
       user="userid" password="password" /> --%>

<jdbc:query id="productQuery" connection="jdbcConn" resultsId="productDS" resultsScope="session" >
        SELECT * FROM CD 
</jdbc:query>
<TABLE border=1>
  <TR>
    <TH>ID</TH>
    <TH>CD Title</TH>
    <TH>Artist</TH>
    <TH>Country</TH>
    <TH>Price</TH>
  </TR>
<pr:iterator results="productDS" >
  <TR>
    <TD><pr:field name="id"/></TD>
    <TD><pr:field name="cdtitle"/></TD>
    <TD><pr:field name="artist"/></TD>
    <TD><pr:field name="country"/></TD>
    <TD><pr:field name="price"/></TD>
<TD>
<form method=get action="ShopCart.jsp">
<input type=hidden name=cdId value="<pr:field name="id"/>">
<input type=hidden name=cdTitle value="<pr:field name="cdtitle"/>">
<input type=hidden name=cdPrice value="<pr:field name="price"/>">
<input type=submit name=operation value=Add>
</form>
</TD>
  </TR>
</pr:iterator>
</TABLE>
<jdbc:cleanup scope="session" status="ok" />
</body>
</html>
