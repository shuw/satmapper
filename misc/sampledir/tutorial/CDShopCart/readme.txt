readme.txt

CDShopCart Tutorial Readme File

This tutorial is thoroughly documented in Forte(TM) for Java(TM), Community 
Edition Tutorial. Refer to that document to build the tutorial application 
yourself. 

If you simply want to load an existing version of the tutorial and see it run, 
you can use the files in this subdirectory. This application demonstrates the 
use of database and presentation tag libraries and of transparent persistance.

1. First make sure the Pointbase netserver is running. Then invoke the 
PointBase console by navigating in your Start Menu to the pbconsole.

2. Create a new database called cdshopcart. If this is your first time using 
Pointbase, the default URL will be jdbc:pointbase://localhost/sample. Change 
'sample' to 'cdshopcart'.  Use the default driver, username and password. 
Check the Create New Database box, and click OK. Create and populate four 
tables in the cdshopcart database, by using File | Open and browsing to the 
tutorial/CDShopCart/SQLscripts directory and choosing CDCatalog_pb.sql. 
Then select SQL | Execute All. The Pointbase console creates databases in 
FORTE4J_HOME/pointbase/network/databases.

3. Start the Forte for Java executable. If you haven't already defined a 
browser, do so now. Select Tools > Options > Web Browser > External Browser
and fill in the Executable Browser field.
Select File | New | JSP & Servlet | WebModule. Use the file browser to find 
the CDShopCart directory under ~sampledir/tutorial in your user work area. 
Select it to create a new Web Module pointing at CDShopCart.

3a. Edit ProductList.jsp and the constructor for CheckOutBean. In both of those
files, you will see a URL string for the Pointbase connection.
Edit the database.home portion of the string to point to your Pointbase database location.
 
4. If you prefer to use Oracle or SQLServer instead of Pointbase, you will 
need to remove the CDclasses jar file from the WEB-INF/lib directory, and
follow the instructions in Forte(TM) for Java(TM), Community Edition Tutorial,
Chapter 4, to recreate the persistence capable classes for your database.
Follow the instructions given in Chapter 4 to jar those classes and place them
in the WEB-INF/lib directory. Then edit the database connection code in 
ProductList.jsp to point to your database. After making your changes,
right-click on ProductList.jsp in the file browser and select Compile.
Then edit the database connection code in CheckOutBean's constructor, and 
compile CheckOutBean. (If you are using Oracle, make sure a copy of your
classes12.zip file is in the %Forte4J%/lib/ext/ directory. If it is not there,
copy it there now, then exit and reinvoke the ide.)

4a. If you are using Pointbase, and would like to see the persistence capable
classes that were packaged into the CDclasses jar file, follow the instuctions 
in Forte(TM) for Java(TM), Community Edition Tutorial, Chapter 4 to recreate
the persistence capable files. Remember to
unmount the directory where you create the persistence capable files, so that
at runtime, the enhanced files in the jar will be used.

Note: The CDclasses.jarContents file was removed from the WEB-INF/lib directory.
Since the source classes for the CDclasses.jar file are not provided, the
CDclasses.jar file should not be rebuilt. Removing the .jarContents file allows
you to run Build All without the compiler attempting to rebuild the .jar file.

5. Select your new Web Module, then select Build | Build All.

6. Right click on ProductList.jsp and select Execute. You will see a list 
of CD's. The behavior of the application is straightforward. Add CD's to 
your cart, delete them from your cart, resume shopping, then place or cancel 
your order.

