/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

/*
 * Employee.java
 *
 * Created on July 5, 2000, 4:20 PM
 */

import java.util.*;
import java.util.Collection;
import java.util.Vector;
import java.util.Date;
import java.util.GregorianCalendar;
import org.netbeans.jsptags.results.BeanVectorResults;

public class Employee extends Object { 

    /** Creates new Employee */
    public Employee() {
    }

    long empId; 
    String lastName; 
    String firstName; 
    Date hireDate; 
    Date birthDate; 
    Employee mgr; 
    float salary;
    // Collection projs;

    public long getEmpId() {
        return this.empId;
    }

    public void setEmpId(long empId) {
        this.empId = empId;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public float getSalary() {
        return this.salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String name) {
        this.lastName = name;
    }

    public Employee getMgr() {
        return this.mgr;
    }

    public void setMgr(Employee mgr) {
        this.mgr = mgr;
    }

    public Date getHireDate() {
        return this.hireDate;
    }

    public void setHireDate(Date date) {
	this.hireDate = date;
    }

    public Date getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(Date date) {
	this.birthDate = date;
    }

    public String toString() {
	return "empId = " + getEmpId() + "\n" +
	       "lastName = " + getLastName() + "\n" +
	       "firstName = " + getFirstName() + "\n" +
	       "birthDate = " + getBirthDate() + "\n" +
	       "hireDate = " + getHireDate() + "\n" +
	       "salary = " + getSalary() + "\n";
    }
    public void initData (long id,
                          String firstName,
                          String lastName,
                          Date   hireDate,
                          Date   birthDate,
                          float  salary,
                          Employee mgr) {
       setEmpId (id);
       setFirstName (firstName);
       setLastName (lastName); 
       setHireDate (hireDate);
       setBirthDate (birthDate);
       setSalary (salary);
       setMgr (mgr);
    }

    public static BeanVectorResults generateTestResults (){
        Vector v = new Vector ();
        Employee mgr = new Employee ();
        String firstNames[]={"John", "Mary", "Kelly", "George", "Paul"};
        String lastNames[] = {"Ross", "OConnor", "Opp", "West", "Park"};
 
        Employee emp;
        Calendar cal1 = new GregorianCalendar();
        cal1.set(1988, 1, 15);
        Calendar cal2 = new GregorianCalendar();
        cal2.set(1952, 1, 15);
        mgr.initData (1234, "John", "Foo", 
                      cal1.getTime(),
                      cal2.getTime(),
                      100000, null);

        for (int i=0; i < firstNames.length; i++) {
            emp = new Employee ();
            cal1.set(1988+i, 1, 15);
            cal2.set(1952+i, 1, 15);
            emp.initData (1234 + 10 * i,
                          firstNames[i], lastNames[i],
                          cal1.getTime(),
                          cal2.getTime(),
                          100000+9*i, mgr); 
            v.add (emp);
        }
        return new BeanVectorResults(v, Employee.class);
    }
        
}




