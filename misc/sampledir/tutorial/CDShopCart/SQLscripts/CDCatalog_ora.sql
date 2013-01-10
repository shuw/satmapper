/* Run with: sqlplus tutorial/tutorial@dbname @scriptname */
drop table SEQUENCE;
drop table ORDERITEM;
drop table CDORDER;
drop table CD;

create table CD( 
 	id              int,          
	cdtitle         char(20),
	artist		char(20),
	country         char(20),
	price           number(8,2),  
	primary key(id));
grant all on CD to public;

insert into CD (id, cdtitle, artist, country, price) 
  values (1, 'Yuan','The Guo Brothers', 'China',14.95);
insert into CD (id, cdtitle, artist, country, price) 
  values (2, 'Drums of Passion','Babatunde Olatunji', 'Nigeria',16.95);
insert into CD (id, cdtitle, artist, country, price) 
  values (3, 'Kaira','Tounami Diabate', 'Mali',13.95);
insert into CD (id, cdtitle, artist, country, price) 
  values (4, 'The Lion is Loose','Eliades Ochoa', 'Cuba',12.95);
insert into CD (id, cdtitle, artist, country, price) 
  values (5, 'Dance the Devil Away','Outback', 'Australia',14.95);
commit;


create table SEQUENCE (
	tableName 	varchar2(20),
	nextPK 		int,
	primary key(tablename));
grant all on SEQUENCE to public;
insert into SEQUENCE (tableName, nextPK ) values ('CDORDER', 1);
commit;


create table CDORDER (
	id		int,
	orderDate	date,
	primary key (id));
grant all on CDORDER to public;	


create table ORDERITEM (
	orderID		int,
	lineItemID	int,
	productID	int,
	primary key (orderID, lineItemID),
	foreign key (orderID) references CDORDER (id),
	foreign key (productID) references CD(id));
grant all on ORDERITEM to public;


