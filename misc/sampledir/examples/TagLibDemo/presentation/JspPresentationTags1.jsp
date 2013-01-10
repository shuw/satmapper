<html>
<head>
   <title>JSP Tags Test : Presentation Tags</title>
</head>

<%@ page errorPage="/utility/JspTagsErrorPage.jsp" %>
<%@ include file="/presentation/DefineBeanDataSources.jsp" %>
<%@taglib uri="/WEB-INF/lib/ietags.jar" prefix="pr" %>

<body>
<h2> JSP Tag Test : Presentation Tags </h2>
<a href="/index.html"><img src="/images/return.gif" width="24" height="24" align="right" border="0"></a>

<br><hr> <h3> Testing  with Bean Patterns VectorResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=beanPatternsVectorResultsName%>">
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=beanPatternsVectorResultsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>


<br><hr> <h3> Testing  with Overloaded Bean Properties VectorResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=beanVectorOverLoadPropsName%>">
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=beanVectorOverLoadPropsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>

<br><hr> <h3> Testing  with BeanVectorResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=beanVectorResultsName%>">
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=beanVectorResultsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>


<br><hr> <h3> Testing  with BeanCollectionResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=beanCollectionResultsName%>" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=beanCollectionResultsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>

<br><hr> <h3> Testing  with BeanListResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=beanListResultsName%>" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=beanListResultsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>

<br><hr> <h3> Testing  with BeanEnumerationResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=beanEnumerationResultsName%>" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=beanEnumerationResultsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD> 
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>

<br><hr> <h3> Testing  with BeanIteratorResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=beanIteratorResultsName%>" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=beanIteratorResultsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>


<br><hr> <h3> Testing  with BeanArrayResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=beanArrayResultsName%>" >
    <TH>
    <pr:field op="name"/>
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=beanArrayResultsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>

<br><hr> <h3> Testing  with Non Homogeneous VectorResults </h3><br>

<TABLE border=1>
  <TR>
  <pr:fieldIterator results="<%=hetVectorResultsName%>">
    <TH>
    <pr:field op="name"/> &nbsp;
    </TH>
  </pr:fieldIterator>
  </TR>
<pr:iterator results="<%=hetVectorResultsName%>" >
  <TR>
  <pr:fieldIterator>
    <TD>
    <pr:field /> &nbsp;
    </TD>
  </pr:fieldIterator>
  </TR>
</pr:iterator>
</TABLE>


<br><hr> <h3> numFields test for Homogeneous Results</h3><br>
<%
try {
out.println("Homogeneous Num filed of index 0 "+beanVectorResults.numFields(0)+"<br>");
out.println("Homogeneous Num filed of index 5 "+beanVectorResults.numFields(5)+"<br>");
out.println("Homogeneous Num filed of index 10 "+beanVectorResults.numFields(10)+"<br>");
} catch(Exception ex) { out.println("Exception : "+ex.getMessage()+"<br>"); }
%>

<br><hr> <h3> numFields test for non Homogeneous Results</h3><br>
<%
try {
out.println("Non Homogeneous Num filed of index 0 "+hetVectorResults.numFields(0)+"<br>");
out.println("Non Homogeneous Num filed of index 5 "+hetVectorResults.numFields(5)+"<br>");
out.println("Non Homogeneous Num filed of index 10 "+hetVectorResults.numFields(10)+"<br>");
} catch(Exception ex) { out.println("Exception : "+ex.getMessage()+"<br>"); }
%>



</body>
</html>
