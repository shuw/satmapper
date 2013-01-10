/* Run with: sqlplus tutorial/tutorial@dbname @scriptname */
use cdcat
go
drop table CD
drop table ORDERITEM
drop table CDORDER
drop table SEQUENCE
go

create table CD( 
 	id              int,          
	cdtitle         varchar(20) null,
	artist		varchar(20) null,
	country         varchar(20) null,
	price           money null,  
PRIMARY KEY(id))
go
grant all on CD to public
go

insert into CD (id, cdtitle, artist, country, price) 
  values (1, 'Yuan','The Guo Brothers', 'China',14.95)
insert into CD (id, cdtitle, artist, country, price) 
  values (2, 'Drums of Passion','Babatunde Olatunji', 'Nigeria',16.95)
insert into CD (id, cdtitle, artist, country, price) 
  values (3, 'Kaira','Tounami Diabate', 'Mali',13.95)
insert into CD (id, cdtitle, artist, country, price) 
  values (4, 'The Lion is Loose','Eliades Ochoa', 'Cuba',12.95)
insert into CD (id, cdtitle, artist, country, price) 
  values (5, 'Dance the Devil Away','Outback', 'Australia',14.95)
go

create table SEQUENCE (
	tableName 	varchar(20),
	nextPK 		int,
	primary key(tablename))
go
grant all on SEQUENCE to public
go

insert into SEQUENCE (tableName, nextPK ) values ('CDORDER', 1)
go


create table CDORDER (
	id		int,
	orderDate	date null,
	primary key (id))
go
grant all on CDORDER to public	
go

create table ORDERITEM (
	orderID		int,
	lineItemID	int,
	productID	int,
	primary key (orderID, lineItemID),
	foreign key (orderID) references CDORDER (id),
	foreign key (productID) references CD(id))
go
grant all on ORDERITEM to public
go

