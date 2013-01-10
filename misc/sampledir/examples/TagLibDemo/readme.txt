readme.txt

TagLibDemo Readme File

This example illustrates the use of three standard tag libraries:
ietags.jar (presentation and conditional tags)
dbtags.jar (database tags)
tptags.jar (transparent persistence tags)

If you have not run the CDShopCart tutorial, you must load the CD table
in a Pointbase database called cdshopcart:

1. Make the the Pointbase network server is running. Then invoke the PointBase 
console by navigating in your Start Menu to pbconsole.

2. Create a new database called cdshopcart. If this is your first time using 
PointBase, the defualt URL will be jdbc:pointbase://localhost/sample. Change 
'sample' to 'cdshopcart'.  Use the default driver, username and password. 
Check the Create New Database box, and click OK. Create and populate four 
tables in the cdshopcart database, by using File | Open and browsing to the 
tutorial/CDShopCart/SQLscripts directory and choosing CDCatalog_pb.sql. 
Then select SQL | Execute All.

The JavaServer Pages (TM) that exercise the database and transparent 
persistence taglibs will use the CD table. Now prepare to run the 
TagLibDemo example in the Forte for Java, Community Edition IDE.

3. Start the IDE. In the Explorer Filesystems tab page,
select File | New, then open JSP and select Web Module. Click Next. Use the 
file browser to find the sampledir/examples/TagLibDemo/ subdirectory
under your user work directory. Create the new web module.
  
4. Select the TagLibDemo Web Module, then select Build | Build All.

5. Right click on the "startHere" JSP and select Execute. You will see a list 
of links. Each one demonstrates a set of tags: DB, JDO, Conditional, and
Presentation. Select one, observe the results, back up, and select another
until you have seen them all. 

6. You can also run the TabLibDemo from a web browser by calling the URL
	http://localhost:8080/index.html
   provided that you have already started up your internal web server.