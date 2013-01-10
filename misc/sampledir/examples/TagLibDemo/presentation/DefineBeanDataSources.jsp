
<%@page import="java.util.*, BeanPatterns, Employee " %>
<%@page import="org.netbeans.jsptags.results.BeanVectorResults" %>
<%@page import="org.netbeans.jsptags.results.BeanCollectionResults" %>
<%@page import="org.netbeans.jsptags.results.BeanArrayResults" %>
<%@page import="org.netbeans.jsptags.results.BeanEnumerationResults" %>
<%@page import="org.netbeans.jsptags.results.BeanListResults" %>
<%@page import="org.netbeans.jsptags.results.BeanIteratorResults" %>


<%!
     public static class Person {
          String firstName = "firstName";
          String lastName = "lastName";

          public String getFirstName() { return firstName; }
          public void   setFirstName(String fName) { firstName = fName;}

          public String getLastName() { return lastName; }
          public void   setLastName(String lName) { lastName = lName; }

          public Person(String fName, String lName) {
              this.firstName = fName;
              this.lastName = lName;
          }
     }

     public static class Address {
          String street = "myStreet";
          String city = "myCity";
          String state = "myState";
          int phone;

          public String getStreet() { return street; }
          public void   setStreet(String street) { this.street = street;}

          public String getCity() { return city; }
          public void   setCity(String city) { this.city = city;}

          public String getState() { return state; }
          public void   setState(String state) { this.state = state; }


          public void setPhone(int phone) { this.phone = phone; }
          public int getPhone() { return phone; }

          public void setPhone(String phone) {
            try {
              this.phone = Integer.parseInt(phone);
            } catch(Exception ex) { this.phone = 0; }
          }

          public Address(String street, String city, String state) {
              this.street = street;
              this.city = city;
              this.state = state;

          }

          public Address(String street, String city, String state, String phone) {
              this.street = street;
              this.city = city;
              this.state = state;
              try {
                this.phone = Integer.parseInt(phone);
              } catch(Exception ex) { this.phone = 0; }

          }
     }

%>

<%
int numBeans = 3;
String numBeansValue = pageContext.getRequest().getParameter("numBeans");
if(numBeansValue != null && numBeansValue.trim().length() > 0 ) {
   try {
      numBeans = Integer.parseInt(numBeansValue);
   }catch(Exception ex) {
      numBeans = 0;
   }
}
%>

<%
    String hetVectorResultsName = "hetVectorResults";
    String beanVectorResultsName = "beanVectorResults";
    String beanCollectionResultsName = "beanCollectionResults";
    String beanListResultsName = "beanListResults";
    String beanEnumerationResultsName = "beanEnumerationResults";
    String beanIteratorResultsName = "beanIteratorResults";
    String beanArrayResultsName = "beanArrayResults";

    String beanVectorOverLoadPropsName = "beanVectorOverLoadPropsResults";
    String beanPatternsVectorResultsName = "beanPatternsVectorResults";
%>

<%
   // Overloaded bean
   Vector beanPatternsVector = new Vector();
   for(int i=0; i < numBeans; ++i) {
      BeanPatterns beanPatterns = new BeanPatterns();
      beanPatterns.setIntValue("00"+i);
      beanPatterns.setFloatValue("77.00"+i);
      beanPatterns.setCharValue('c');
      beanPatterns.setBooleanValue(true);
      beanPatterns.setStringValue("String_"+i);
      beanPatterns.setIntegerObj(new java.lang.Integer("100"+i));
      beanPatterns.setWriteOnly("WriteOnly_"+i);
      beanPatternsVector.add(beanPatterns);
   }

   BeanVectorResults beanPatternsVectorResults = new BeanVectorResults(beanPatternsVector,BeanPatterns.class);
   pageContext.setAttribute(beanPatternsVectorResultsName, beanPatternsVectorResults, PageContext.SESSION_SCOPE);
%>


<%
   // Overloaded bean
   Vector overloadPropsVector = new Vector();
   for(int i=0; i < numBeans; ++i) {
      overloadPropsVector.add(new Address("OlPropsVecStreet_"+i,"OlPropsVecCity_"+i, "OlPropsVecState_"+i,"000"+i));
   }

   BeanVectorResults overloadPropsVectorResults = new BeanVectorResults(overloadPropsVector,Address.class);
   pageContext.setAttribute(beanVectorOverLoadPropsName, overloadPropsVectorResults, PageContext.SESSION_SCOPE);
%>


<%
   // Bean Vector non homogeneous
   Vector hetVector = new Vector();
   for(int i=0; i < numBeans; ++i) {
      hetVector.add(new Person("HetVecFirstName_"+i,"VecLastName_"+i));
   }
   for(int i=0; i < numBeans; ++i) {
      hetVector.add(new Address("HetVecStreet_"+i,"HetVecCity_"+i, "HetVecState_"+i));
   }

   BeanVectorResults hetVectorResults = new BeanVectorResults(hetVector);
   pageContext.setAttribute(hetVectorResultsName, hetVectorResults, PageContext.SESSION_SCOPE);
%>

<%
   // Bean Vector Datasource
   Vector beanVector = new Vector();
   for(int i=0; i < numBeans; ++i) {
      beanVector.add(new Person("VecFirstName_"+i,"VecLastName_"+i));
   }

   BeanVectorResults beanVectorResults = new BeanVectorResults(beanVector, Person.class);
   pageContext.setAttribute(beanVectorResultsName, beanVectorResults, PageContext.SESSION_SCOPE);
%>

<%
   // Bean Collection Datasource
   Vector beanCollection = new Vector();
   for(int i=0; i < numBeans; ++i) {
      beanCollection.add(new Person("ColFirstName_"+i,"ColLastName_"+i));
   }

   BeanCollectionResults beanCollectionResults = new BeanCollectionResults(beanCollection, Person.class);
   pageContext.setAttribute(beanCollectionResultsName, beanCollectionResults, PageContext.SESSION_SCOPE);
%>

<%
   // Bean List Datasource
   ArrayList beanList = new ArrayList();
   for(int i=0; i < numBeans; ++i) {
      beanList.add(new Person("ListFirstName_"+i,"ListLastName_"+i));
   }

   BeanListResults beanListResults = new BeanListResults(beanList, Person.class);
   pageContext.setAttribute(beanListResultsName, beanListResults, PageContext.SESSION_SCOPE);
%>

<%
   // Bean Enumeration Datasource
   Vector beanEnumationVector = new Vector();
   for(int i=0; i < numBeans; ++i) {
      beanEnumationVector.add(new Person("EnumFirstName_"+i,"EnumLastName_"+i));
   }

   BeanEnumerationResults beanEnumerationResults = new BeanEnumerationResults(beanEnumationVector.elements(), Person.class);
   pageContext.setAttribute(beanEnumerationResultsName, beanEnumerationResults, PageContext.SESSION_SCOPE);
%>

<%
   // Bean Iteration Datasource
   Vector beanIterationVector = new Vector();
   for(int i=0; i < numBeans; ++i) {
      beanIterationVector.add(new Person("ItrFirstName_"+i,"ItrLastName_"+i));
   }

   BeanIteratorResults beanIteratorResults = new BeanIteratorResults(beanIterationVector.iterator(), Person.class);
   pageContext.setAttribute(beanIteratorResultsName, beanIteratorResults, PageContext.SESSION_SCOPE);
%>


<%
   // Bean Array Datasource
   Person[] beanArray = new Person[numBeans];
   for(int i=0; i < beanArray.length; ++i) {
      beanArray[i] = new Person("ArrFirstName_"+i,"ArrLastName_"+i);
   }

   BeanArrayResults beanArrayResults = new BeanArrayResults(beanArray, Person.class);
   pageContext.setAttribute(beanArrayResultsName, beanArrayResults, PageContext.SESSION_SCOPE);

%>


