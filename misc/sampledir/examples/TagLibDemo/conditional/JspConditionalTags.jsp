<html>
<head>
   <title>JSP Tags Test : Conditional Tags</title>
</head>

<%@ page errorPage="/JspTagsErrorPage.jsp" %> 
<%@taglib uri="/WEB-INF/lib/ietags.jar" prefix="cond" %>

<body>
<h1> JSP Tag Test : Conditional Tags </h1>
<a href="/index.html"><img src="/images/return.gif" width="24" height="24" align="right" border="0"></a>

<h2> Testing IF Tag </h2>

      <% 
            int foo = 1; 
            int bar = 2;             
            String passingLCTrue = "Passing Lower Case true  : FAILED ";
            String passingLCFalse = "Passing Lower Case false : PASSED ";
            String passingUCTrue = "Passing Upper Case TRUE   : FAILED ";
            String passingUCFalse = "Passing Upper Case FALSE  : PASSED ";
            String passingZeroString = "Passing 0 length string : PASSED ";
            String passingNonBooleanString = "Passing non Boolean string : PASSED ";
            String passingJSPExp = "Passing true jsp expression : FAILED ";
            String resultNestedIf = "Testing Nested If : FAILED ";
      %>

      <cond:if test="true" >
         <% passingLCTrue = "Passing Lower Case true  : PASSED"; %>
      </cond:if>
      
      <cond:if test="false" >         
        <% passingLCFalse = "Passing Lower Case false : FAILED "; %>
      </cond:if>

      <cond:if test="TRUE" >         
        <% passingUCTrue = "Passing Lower Case false : PASSED "; %>
      </cond:if>

      <cond:if test="FALSE" >         
        <% passingUCFalse = "Passing Lower Case false : FAILED "; %>
      </cond:if>

      <cond:if test="" >         
        <% passingZeroString = "Passing 0 length string : FAILED "; %>
      </cond:if>
      
      <cond:if test="not a true or false string" >         
        <% passingNonBooleanString = "Passing non Boolean string : FAILED "; %>
      </cond:if>
      
      <cond:if test="<%= foo+bar == 3 %>" >
        <% passingJSPExp = "Passing true jsp expression : PASSED "; %>
      </cond:if>
      
      <cond:if test="true" >
        <cond:if test="true" >           
          <% resultNestedIf = "Test for nested if : PASSED "; %>
        </cond:if>
      </cond:if>
       <br> <%=passingLCTrue%>
       <br> <%=passingLCFalse%>
       <br> <%=passingUCTrue%>
       <br> <%=passingZeroString%>
       <br> <%=passingNonBooleanString%>
       <br> <%=passingJSPExp%>
       <br> <%=resultNestedIf%>

<hr><h2> Testing choose Tag </h2>
    <% 
       String passingFirstWhen = "Passing true for the first when: PASSED";
       String passingSecondWhen = "Passing true for the second when: PASSED";
       String passingFirstOther = "Other: PASSED"; 
    %>
    <cond:choose>
      <cond:when test="true">
          <% passingFirstWhen = "Passing true for the first when: PASSED"; %>
      </cond:when>
      <cond:when test="true">
          <% passingSecondWhen = "Passing true for the second when: FAILED"; %>
      </cond:when>
      <cond:otherwise>
          <% passingFirstOther = "Other: FAILED"; %>
      </cond:otherwise>
    </cond:choose>

    <br><%=passingFirstWhen%>
    <br><%=passingSecondWhen%>
    <br><%=passingFirstOther%>
    <% 
       passingFirstOther = "Other: FAILED"; 
       String passingSecondOther = "Other second: PASSED"; 
    %>
    <cond:choose>
      <cond:when test="false">
          <% passingSecondWhen = "Passing true for the second when: FAILED";%> 
      </cond:when>
      <cond:otherwise>
          <% passingFirstOther = "Other: PASSED"; %>
      </cond:otherwise>
      <cond:otherwise>
          <% passingSecondOther = "Other second: PASSED"; %>
      </cond:otherwise>
    </cond:choose>

    <br><br><%=passingSecondWhen%>
    <br><%=passingFirstOther%>
    <br><%= passingSecondOther%>
     
<br><hr> <h2> Testing switch Tag </h2>
    <% String CaseTest0="Passing fasle to test: PASSED"; 
       String CaseTest1="Passing true to test: FAILED";
       String CaseTest2="Passing true to test: FAILED"; 
       String CaseTest3="Passing true to test: PASSED";
       String CaseTest4="Default: PASSED";
    %>
    <cond:switch>
      <cond:case test = "false" break = "false">
        <% CaseTest0="Passing fasle to test: FAILED"; %>
      </cond:case>
      <cond:case test = "true" break = "false">
        <% CaseTest1="Passing true to test: PASSED"; %>
      </cond:case>
      <cond:case test = "true">
        <% CaseTest2="Passing true to test: PASSED"; %>
      </cond:case>
      <cond:case test = "true">
        <% CaseTest3="Passing true to test: FAILED"; %>
      </cond:case>
      <cond:default>
        <% CaseTest4="Default: FAILED"; %>
      </cond:default>
    </cond:switch>
    <br><%=CaseTest0%>
    <br><%=CaseTest1%>
    <br><%=CaseTest2%>
    <br><%=CaseTest3%>
    <br><%=CaseTest4%>
    <% CaseTest3="Passing true to test: PASSED";
       CaseTest4="Default: FAILED";
    %>
    <cond:switch>
      <cond:case test = "false" break = "false">
        <% CaseTest3="Passing fasle to test: FAILED"; %>
      </cond:case>
      <cond:default>
        <% CaseTest4="Default: PASSED"; %>
      </cond:default>
    </cond:switch>
    <br><br><%=CaseTest3%>
    <br><%=CaseTest4%>
</body>
</html>
